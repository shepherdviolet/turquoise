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

import android.os.Looper;
import android.os.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.enhance.common.WeakHandler;
import sviolet.turquoise.model.common.LazySingleThreadPool;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.TILoaderUtils;
import sviolet.turquoise.x.imageloader.drawable.BackgroundDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.FailedDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.LoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.entity.ServerSettings;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;
import sviolet.turquoise.x.imageloader.server.Server;
import sviolet.turquoise.x.imageloader.stub.Stub;
import sviolet.turquoise.x.imageloader.stub.StubGroup;

/**
 *
 * Created by S.Violet on 2016/2/18.
 */
public class NodeControllerImpl extends NodeController {

    private ComponentManager manager;
    private String nodeId;
    private Node node;
    private NodeSettings settings;

    private RequestQueue diskRequestQueue;//磁盘加载等待队列
    private RequestQueue netRequestQueue;//网络加载等待队列
    private ResponseQueue responseQueue = new ResponseQueueImpl();//响应队列

    private Map<String, StubGroup> stubPool = new HashMap<>();//等待执行的Stub池
    private final ReentrantLock stubPoolLock = new ReentrantLock();

    private boolean nodeInitialized = false;
    private final ReentrantLock nodeInitializeLock = new ReentrantLock();

    NodeControllerImpl(ComponentManager manager, Node node, String nodeId){
        this.manager = manager;
        this.node = node;
        this.nodeId = nodeId;
    }

    /*******************************************************
     * init
     */

    @Override
    void waitingForInitialized() {
        if (nodeInitialized){
            return;
        }
        try {
            nodeInitializeLock.lock();
            if (!nodeInitialized){
                onInitialize();
                nodeInitialized = true;
            }
        } finally {
            nodeInitializeLock.unlock();
        }
    }

    private void onInitialize(){
        if (settings == null){
            settings = new NodeSettings.Builder().build();
        }
        diskRequestQueue = new RequestQueueImpl(settings.getDiskQueueSize());
        netRequestQueue = new RequestQueueImpl(settings.getNetQueueSize());
    }

    /****************************************************
     * I/O
     */

    @Override
    public void execute(Stub stub) {
        String key = stub.getKey();
        StubGroup stubGroup;
        boolean newStubGroup = false;
        try{
            stubPoolLock.lock();
            stubGroup = stubPool.get(key);
            if (stubGroup == null){
                stubGroup = new StubGroup();
                stubPool.put(key, stubGroup);
                newStubGroup = true;
            }
        }finally {
            stubPoolLock.unlock();
        }
        stubGroup.add(stub);

        if (newStubGroup) {
            Task task = manager.getServerSettings().getTaskFactory().newTask(this, stub);
            task.setServerType(Server.Type.CACHE);
            task.setState(Task.State.STAND_BY);
            executeTask(task);
        }
    }

    @Override
    Task pullTask(Server.Type type) {
        switch (type){
            case DISK:
                return diskRequestQueue.get();
            case NET:
                return netRequestQueue.get();
            default:
                manager.getLogger().e("NodeControllerImpl:pullTask illegal Server.Type:<" + type.toString() + ">");
                break;
        }
        return null;
    }

    @Override
    void response(Task task) {
        responseQueue.put(task);
        postDispatch();
    }

    /****************************************************
     * private
     */

    private void executeTask(Task task){

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
            case CACHE:
                executeTaskToCache(task);
                break;
            case DISK:
                executeTaskToDisk(task);
                break;
            case NET:
                executeTaskToNet(task);
                break;
            default:
                throw new RuntimeException("[TILoader:NodeControllerImpl] illegal ServerType of Task");
        }
    }

    private void executeTaskToCache(Task task){
        ImageResource<?> resource = manager.getCacheServer().get(task.getKey());
        if (resource != null) {
            task.setState(Task.State.SUCCEED);
            callback(task);
        }else{
            task.setServerType(Server.Type.DISK);
            task.setState(Task.State.STAND_BY);
            executeTask(task);
        }
    }

    private void executeTaskToDisk(Task task){
        if (task.getState() == Task.State.STAND_BY){
            Task obsoleteTask = diskRequestQueue.put(task);
            manager.getDiskEngine().ignite();
            callbackToObsolete(obsoleteTask);
        }else{
            task.setServerType(Server.Type.NET);
            task.setState(Task.State.STAND_BY);
            executeTask(task);
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

        StubGroup stubGroup;
        try {
            stubPoolLock.lock();
            stubGroup = stubPool.remove(task.getKey());
        } finally {
            stubPoolLock.unlock();
        }

        if (stubGroup == null){
            return;
        }

        switch (task.getState()){
            case SUCCEED:
                ImageResource<?> resource = manager.getCacheServer().get(task.getKey());
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
        boolean result = false;
        if (!nodeInitialized){
            try{
                nodeInitializeLock.lock();
                if (!nodeInitialized){
                    this.settings = settings;
                    result = true;
                }else{
                    manager.getLogger().e("[TILoader]setting Node failed, you should invoke TILoader.node(context).setting() before Node used (load image)");
                }
            }finally {
                nodeInitializeLock.unlock();
            }
        }else{
            manager.getLogger().e("[TILoader]setting Node failed, you should invoke TILoader.node(context).setting() before Node used (load image)");
        }
        return result;
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
    public BackgroundDrawableFactory BackgroundDrawableFactory() {
        BackgroundDrawableFactory factory = getNodeSettings().getBackgroundDrawableFactory();
        if (factory == null){
            factory = getServerSettings().getBackgroundDrawableFactory();
        }
        return factory;
    }

    /******************************************************************
     * Dispatch Thread
     */

    private LazySingleThreadPool dispatchThreadPool = new LazySingleThreadPool();

    @Override
    public void postDispatch() {
        dispatchThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Task task;
                while((task = responseQueue.get()) != null){
                    executeTask(task);
                }
            }
        });
    }

    /*********************************************
     * lifecycle
     */

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

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
