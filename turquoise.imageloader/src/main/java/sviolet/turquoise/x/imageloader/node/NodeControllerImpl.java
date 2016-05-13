/*
 * Copyright (C) 2015-2016 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.turquoise.x.imageloader.node;

import android.content.Context;
import android.os.Looper;
import android.os.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.enhance.common.WeakHandler;
import sviolet.turquoise.model.thread.LazySingleThreadPool;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.TILoaderUtils;
import sviolet.turquoise.x.imageloader.drawable.BackgroundDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.FailedDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.LoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;
import sviolet.turquoise.x.imageloader.entity.ServerSettings;
import sviolet.turquoise.x.imageloader.node.queue.InfiniteRequestQueue;
import sviolet.turquoise.x.imageloader.node.queue.InfiniteResponseQueue;
import sviolet.turquoise.x.imageloader.node.queue.LossyRequestQueue;
import sviolet.turquoise.x.imageloader.node.queue.RequestQueue;
import sviolet.turquoise.x.imageloader.node.queue.ResponseQueue;
import sviolet.turquoise.x.imageloader.server.Engine;
import sviolet.turquoise.x.imageloader.server.Server;
import sviolet.turquoise.x.imageloader.stub.Stub;
import sviolet.turquoise.x.imageloader.stub.StubGroup;

/**
 * <p>Node Controller</p>
 *
 * <p>Manage loading tasks / context lifecycle / settings.
 * Maintain the relationship between the {@link Stub} and the {@link Server}/{@link Engine}.</p>
 *
 * <p>The actual core controller of the {@link Node}. Maintain task queue, attach context lifecycle, holding node settings.
 * {@link Stub} initiated request to {@link NodeController}, {@link NodeController} construct a {@link Task}, then
 * push into task queue, {@link Task}s will be executed by {@link Server} or {@link Engine},
 * when {@link Task} execute finished, {@link NodeController} will callback to {@link Stub}.</p>
 *
 * <p>When the life cycle of context changes, {@link Node} will change status (freeze / unfreeze / destroy).</p>
 *
 * <p>************************************************************************************</p>
 *
 * <p>Status::</p>
 *
 * <p>Pause:: Engine will not execute tasks which in paused Node, and node will skip dispatch (skip callback
 * to stub, skip handle response from Engine). Node will pause util all NodeRemotes are resumed(not pause).
 * As long as there is a paused NodeRemoter, Node will keep pause status.</p>
 *
 * <p>Frozen:: Engine will not execute tasks of this Node, and node will skip dispatch (skip callback to stub,
 * skip handle response from Engine). Node will frozen when Context->OnStop(), and unfreeze when
 * Context->OnStart().</p>
 *
 * <p>Destroy:: Node will destroy when Context->OnDestroy(), and skip all process.</p>
 *
 * Created by S.Violet on 2016/2/18.
 */
public class NodeControllerImpl extends NodeController {

    private ComponentManager manager;
    private String nodeId;
    private Node node;
    private NodeSettings settings;
    private boolean infiniteRequestQueue = false;

    private RequestQueue memoryRequestQueue;
    private RequestQueue diskRequestQueue;
    private RequestQueue netRequestQueue;
    private ResponseQueue responseQueue = new InfiniteResponseQueue();

    private Map<String, StubGroup> stubPool = new ConcurrentHashMap<>();
    private final ReentrantLock stubPoolLock = new ReentrantLock();

    private final ReentrantLock initializeLock = new ReentrantLock();
    private final AtomicInteger status = new AtomicInteger(INITIAL);

    /**
     * <p>Pause:: Engine will not execute tasks which in paused Node, and node will skip dispatch (skip callback
     * to stub, skip handle response from Engine). Node will pause util all NodeRemotes are resumed(not pause).
     * As long as there is a paused NodeRemoter, Node will keep pause status.</p>
     */
    private AtomicInteger nodePauseCount = new AtomicInteger(0);

    NodeControllerImpl(ComponentManager manager, Node node, String nodeId, boolean infiniteRequestQueue){
        this.manager = manager;
        this.node = node;
        this.nodeId = nodeId;
        this.infiniteRequestQueue = infiniteRequestQueue;
    }

    /*******************************************************
     * init
     */

    @Override
    void waitingForInitialized() {
        if (status.get() != INITIAL){
            return;
        }
        try {
            initializeLock.lock();
            if (status.compareAndSet(INITIAL, NORMAL)){
                onInitialize();
                manager.getLogger().i("[NodeControllerImpl]initialized nodeId:" + nodeId);
            }
        } finally {
            initializeLock.unlock();
        }
    }

    private void onInitialize(){
        if (settings == null){
            settings = new NodeSettings.Builder().build();
        }
        if (infiniteRequestQueue){
            memoryRequestQueue = new InfiniteRequestQueue();
            diskRequestQueue = new InfiniteRequestQueue();
            netRequestQueue = new InfiniteRequestQueue();
        }else {
            memoryRequestQueue = new LossyRequestQueue(settings.getMemoryQueueSize(), manager.getLogger());
            diskRequestQueue = new LossyRequestQueue(settings.getDiskQueueSize(), manager.getLogger());
            netRequestQueue = new LossyRequestQueue(settings.getNetQueueSize(), manager.getLogger());
        }
    }

    /****************************************************
     * I/O
     */

    @Override
    public void execute(Stub stub) {
        if (status.get() <= INITIAL){
            getLogger().d("[NodeControllerImpl]destroyed/initial, skip execute");
            return;
        }

        //stub key
        String key = stub.getKey();
        //get stubGroup
        boolean newStubGroup = false;
        StubGroup stubGroup = null;
        try {
            stubPoolLock.lock();
            stubGroup = stubPool.get(key);
            if (stubGroup == null) {
                stubGroup = new StubGroup();
                stubPool.put(key, stubGroup);
                newStubGroup = true;
            }
        } finally {
            stubPoolLock.unlock();
        }
        //add into group
        stubGroup.add(stub);

        //execute if new
        if (newStubGroup) {
            Task task = manager.getServerSettings().getTaskFactory().newTask(this, stub, stubGroup);
            task.setNodeSettings(settings);
            executeTask(task);
        }
    }

    @Override
    Task pullTask(Server.Type type) {
        if (nodePauseCount.get() > 0 || status.get() < NORMAL){
            getLogger().d("[NodeControllerImpl]pause/destroyed/initial/frozen, skip pullTask");
            return null;
        }

        switch (type){
            case MEMORY_ENGINE:
                return memoryRequestQueue.get();
            case DISK_ENGINE:
                return diskRequestQueue.get();
            case NETWORK_ENGINE:
                return netRequestQueue.get();
            default:
                manager.getLogger().e("NodeControllerImpl:pullTask illegal Server.Type:<" + type.toString() + ">");
                break;
        }
        return null;
    }

    @Override
    void response(Task task) {
        if (status.get() <= INITIAL){
            getLogger().d("[NodeControllerImpl]destroyed/initial, skip response");
            return;
        }

        responseQueue.put(task);
        postDispatch();
    }

    /****************************************************
     * private
     */

    private void  executeTask(Task task){

        if (task == null){
            manager.getLogger().e("NodeControllerImpl can't execute null Task");
            return;
        }

        if (task.getState() == Task.State.SUCCEED){
            callback(task);
            return;
        }else if (task.getState() == Task.State.CANCELED){
            callback(task);
            return;
        }

        switch (task.getServerType()){
            case MEMORY_ENGINE:
                executeTaskToMemory(task);
                break;
            case DISK_ENGINE:
                executeTaskToDisk(task);
                break;
            case NETWORK_ENGINE:
                executeTaskToNet(task);
                break;
            default:
                throw new RuntimeException("[TILoader:NodeControllerImpl] illegal ServerType of Task");
        }
    }

    private void executeTaskToMemory(Task task){
        if (task.getState() == Task.State.STAND_BY) {
            Task obsoleteTask = memoryRequestQueue.put(task);
            manager.getMemoryEngine().ignite();
            callbackToObsolete(obsoleteTask);
        }else{
            task.setServerType(Server.Type.DISK_ENGINE);
            task.setState(Task.State.STAND_BY);
            executeTaskToDisk(task);
        }
    }

    private void executeTaskToDisk(Task task){
        if (task.getState() == Task.State.STAND_BY){
            Task obsoleteTask = diskRequestQueue.put(task);
            manager.getDiskEngine().ignite();
            callbackToObsolete(obsoleteTask);
        }else{
            task.setServerType(Server.Type.NETWORK_ENGINE);
            task.setState(Task.State.STAND_BY);
            executeTaskToNet(task);
        }
    }

    private void executeTaskToNet(Task task){
        if (task.getState() == Task.State.STAND_BY){
            Task obsoleteTask = netRequestQueue.put(task);
            manager.getNetEngine().ignite();
            callbackToObsolete(obsoleteTask);
        }else{
            task.setState(Task.State.FAILED);
            callback(task);
        }
    }

    private void callback(Task task){
        if (task == null){
            return;
        }
        if (!manager.getLogger().isNullLogger()) {
            manager.getLogger().d("[NodeControllerImpl]task finish, callback to stub, task:" + task.getTaskInfo());
        }
        Message msg = myHandler.obtainMessage(MyHandler.HANDLER_CALLBACK);
        msg.obj = task;
        msg.sendToTarget();
    }

    private void callbackToObsolete(Task obsoleteTask){
        if (obsoleteTask == null) {
            return;
        }
        obsoleteTask.setState(Task.State.CANCELED);
        callback(obsoleteTask);
    }

    private void callbackInUiThread(Task task){
        if (task == null){
            return;
        }

        //remove stubGroup
        StubGroup stubGroup = stubPool.remove(task.getKey());
        if (stubGroup == null){
            return;
        }

        switch (task.getState()){
            case SUCCEED:
                ImageResource resource;
                if (task.getType() == Stub.Type.EXTRACT){
                    //image will remove from memory cache
                    resource = manager.getMemoryCacheServer().extract(task.getKey());
                }else{
                    resource = manager.getMemoryCacheServer().get(task.getKey());
                }
                if (TILoaderUtils.isImageResourceValid(resource)){
                    stubGroup.onLoadSucceed(resource);
                }else{
                    stubGroup.onLoadFailed();
                }
                break;
            case FAILED:
                stubGroup.onLoadFailed();
                break;
            case CANCELED:
                stubGroup.onLoadCanceled();
                break;
            default:
                throw new RuntimeException("[TILoader:NodeControllerImpl] can't callback(callbackInUiThread) when Task.state = " + task.getState());
        }
    }

    /****************************************************
     * settings
     */

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    boolean settingNode(NodeSettings settings) {
        if (status.get() == INITIAL){
            try{
                initializeLock.lock();
                if (status.get() == INITIAL){
                    this.settings = settings;
                    return true;
                }else{
                    manager.getLogger().e("[TILoader]setting Node failed, you should invoke TILoader.node(context).setting() before Node initialized (invoke TILoader.node().load() will initialize Node)");
                }
            }finally {
                initializeLock.unlock();
            }
        }else{
            manager.getLogger().e("[TILoader]setting Node failed, you should invoke TILoader.node(context).setting() before Node initialized (invoke TILoader.node().load() will initialize Node)");
        }
        return false;
    }

    @Override
    public NodeSettings getNodeSettings() {
        return settings;
    }

    @Override
    public ServerSettings getServerSettings() {
        return manager.getServerSettings();
    }

    @Override
    public Context getApplicationContextImage() {
        return manager.getApplicationContextImage();
    }

    @Override
    public Context getContextImage() {
        return manager.getContextImage();
    }

    @Override
    public LoadingDrawableFactory getLoadingDrawableFactory() {
        LoadingDrawableFactory factory = getNodeSettings().getLoadingDrawableFactory();
        if (factory == null){
            factory = getServerSettings().getLoadingDrawableFactory();
        }
        return factory;
    }

    @Override
    public FailedDrawableFactory getFailedDrawableFactory() {
        FailedDrawableFactory factory = getNodeSettings().getFailedDrawableFactory();
        if (factory == null){
            factory = getServerSettings().getFailedDrawableFactory();
        }
        return factory;
    }

    @Override
    public BackgroundDrawableFactory getBackgroundDrawableFactory() {
        BackgroundDrawableFactory factory = getNodeSettings().getBackgroundDrawableFactory();
        if (factory == null){
            factory = getServerSettings().getBackgroundDrawableFactory();
        }
        return factory;
    }

    @Override
    public int getReloadTimes() {
        if (getNodeSettings().getReloadTimes() < 0){
            return getServerSettings().getReloadTimes();
        }
        return getNodeSettings().getReloadTimes();
    }

    @Override
    public boolean isDestroyed() {
        return status.get() == DESTROYED;
    }

    @Override
    public TLogger getLogger() {
        return manager.getLogger();
    }

    /**************************************************************
     * NodeRemoter
     */

    @Override
    AtomicInteger getNodePauseCount() {
        return nodePauseCount;
    }

    /**
     * @see NodeRemoter
     */
    @Override
    NodeRemoter newNodeRemoter() {
        return new NodeRemoter(this);
    }

    /******************************************************************
     * Dispatch Thread
     */

    private LazySingleThreadPool dispatchThreadPool = new LazySingleThreadPool();

    @Override
    public void postDispatch() {
        //check state
        if (nodePauseCount.get() > 0 || status.get() < NORMAL){
            getLogger().d("[NodeControllerImpl]paused/destroyed/initial/frozen, skip dispatch");
            return;
        }
        dispatchThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                //check state
                if (nodePauseCount.get() > 0 || status.get() < NORMAL){
                    getLogger().d("[NodeControllerImpl]paused/destroyed/initial/frozen, skip dispatch");
                    return;
                }
                Task task;
                while((task = responseQueue.get()) != null){
                    //execute
                    executeTask(task);
                    //check state
                    if (nodePauseCount.get() > 0 || status.get() < NORMAL){
                        getLogger().d("[NodeControllerImpl]paused/destroyed/initial/frozen, skip dispatch");
                        return;
                    }
                }
            }
        });
    }

    @Override
    void postIgnite(){
        if (status.get() <= INITIAL){
            getLogger().d("[NodeControllerImpl]destroyed/initial, skip postIgnite");
            return;
        }
        manager.getMemoryEngine().ignite();
        manager.getNetEngine().ignite();
        manager.getDiskEngine().ignite();
    }

    /*********************************************
     * lifecycle
     */

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {
        if (status.compareAndSet(FROZEN, NORMAL)){
            postDispatch();
            postIgnite();
            manager.getLogger().i("[NodeControllerImpl]lifecycle: unfreeze nodeId:" + nodeId);
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {
        if (status.compareAndSet(NORMAL, FROZEN)) {
            manager.getLogger().i("[NodeControllerImpl]lifecycle: freeze nodeId:" + nodeId);
        }
    }

    @Override
    public void onDestroy() {
        //destroy if not destroyed
        if (status.get() != DESTROYED){
            status.set(DESTROYED);
            //scrap node
            manager.getNodeManager().scrapNode(node);
            //shutdown thread pool
            dispatchThreadPool.shutdown();
            //clear queue
            netRequestQueue.clear();
            diskRequestQueue.clear();
            memoryRequestQueue.clear();
            responseQueue.clear();
            //clear stubs
            try {
                for (Map.Entry<String, StubGroup> entry : stubPool.entrySet()) {
                    entry.getValue().onDestroy();
                }
            }catch (Exception ignored){
            }
            stubPool.clear();
            //destroy settings
            if (settings != null) {
                settings.onDestroy();
            }
            manager.getLogger().i("[NodeControllerImpl]lifecycle: destroyed nodeId:" + nodeId);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        onDestroy();
    }

    /******************************************************************
     * Main Thread Handler
     */

    private final MyHandler myHandler = new MyHandler(Looper.getMainLooper(), this);

    private static class MyHandler extends WeakHandler<NodeControllerImpl>{

        private static final int HANDLER_CALLBACK = 1;

        public MyHandler(Looper looper, NodeControllerImpl host) {
            super(looper, host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, NodeControllerImpl host) {
            switch (msg.what){
                case HANDLER_CALLBACK:
                    host.callbackInUiThread((Task) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

}
