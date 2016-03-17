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
import sviolet.turquoise.x.imageloader.task.Task;
import sviolet.turquoise.x.imageloader.task.TaskGroup;

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

    private Map<String, TaskGroup> taskPool = new HashMap<>();//等待执行的Task任务池
    private final ReentrantLock taskPoolLock = new ReentrantLock();

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
    public void executeTask(Task task) {
        String key = task.getKey();
        TaskGroup taskGroup;
        boolean newTaskGroup = false;
        try{
            taskPoolLock.lock();
            taskGroup = taskPool.get(key);
            if (taskGroup == null){
                taskGroup = new TaskGroup();
                taskPool.put(key, taskGroup);
                newTaskGroup = true;
            }
        }finally {
            taskPoolLock.unlock();
        }
        taskGroup.add(task);

        if (newTaskGroup) {
            NodeTask nodeTask = manager.getServerSettings().getNodeTaskFactory().newNodeTask(this, task);
            nodeTask.setServerType(Server.Type.CACHE);
            nodeTask.setState(NodeTask.State.STAND_BY);
            executeNodeTask(nodeTask);
        }
    }

    @Override
    NodeTask pullNodeTask(Server.Type type) {
        switch (type){
            case DISK:
                return diskRequestQueue.get();
            case NET:
                return netRequestQueue.get();
            default:
                manager.getLogger().e("NodeControllerImpl:pullNodeTask illegal Server.Type:<" + type.toString() + ">");
                break;
        }
        return null;
    }

    @Override
    void response(NodeTask task) {
        responseQueue.put(task);
        postDispatch();
    }

    /****************************************************
     * private
     */

    private void executeNodeTask(NodeTask nodeTask){

        if (nodeTask == null){
            manager.getLogger().e("NodeControllerImpl can't execute null NodeTask");
            return;
        }

        if (nodeTask.getState() == NodeTask.State.SUCCEED){
            callback(nodeTask);
            return;
        }else if (nodeTask.getState() == NodeTask.State.CANCELED){
            callback(nodeTask);
            return;
        }

        switch (nodeTask.getServerType()){
            case CACHE:
                executeNodeTaskToCache(nodeTask);
                break;
            case DISK:
                executeNodeTaskToDisk(nodeTask);
                break;
            case NET:
                executeNodeTaskToNet(nodeTask);
                break;
            default:
                throw new RuntimeException("[TILoader:NodeControllerImpl] illegal ServerType of NodeTask");
        }
    }

    private void executeNodeTaskToCache(NodeTask nodeTask){
        ImageResource<?> resource = manager.getCacheServer().get(nodeTask.getKey());
        if (resource != null) {
            nodeTask.setState(NodeTask.State.SUCCEED);
            callback(nodeTask);
        }else{
            nodeTask.setServerType(Server.Type.DISK);
            nodeTask.setState(NodeTask.State.STAND_BY);
            executeNodeTask(nodeTask);
        }
    }

    private void executeNodeTaskToDisk(NodeTask nodeTask){
        if (nodeTask.getState() == NodeTask.State.STAND_BY){
            NodeTask obsoleteNodeTask = diskRequestQueue.put(nodeTask);
            manager.getDiskEngine().ignite();
            callbackToObsolete(obsoleteNodeTask);
        }else{
            nodeTask.setServerType(Server.Type.NET);
            nodeTask.setState(NodeTask.State.STAND_BY);
            executeNodeTask(nodeTask);
        }
    }

    private void executeNodeTaskToNet(NodeTask nodeTask){
        if (nodeTask.getState() == NodeTask.State.STAND_BY){
            NodeTask obsoleteNodeTask = netRequestQueue.put(nodeTask);
            manager.getNetEngine().ignite();
            callbackToObsolete(obsoleteNodeTask);
        }else{
            nodeTask.setState(NodeTask.State.FAILED);
            callback(nodeTask);
        }
    }

    private void callback(NodeTask nodeTask){
        if (nodeTask == null){
            return;
        }
        Message msg = myHandler.obtainMessage(MyHandler.HANDLER_CALLBACK);
        msg.obj = nodeTask;
        msg.sendToTarget();
    }

    private void callbackToObsolete(NodeTask obsoleteNodeTask){
        if (obsoleteNodeTask == null) {
            return;
        }
        obsoleteNodeTask.setState(NodeTask.State.CANCELED);
        callback(obsoleteNodeTask);
    }

    private void callbackInUiThread(NodeTask nodeTask){
        if (nodeTask == null){
            return;
        }

        TaskGroup taskGroup;
        try {
            taskPoolLock.lock();
            taskGroup = taskPool.remove(nodeTask.getKey());
        } finally {
            taskPoolLock.unlock();
        }

        if (taskGroup == null){
            return;
        }

        switch (nodeTask.getState()){
            case SUCCEED:
                ImageResource<?> resource = manager.getCacheServer().get(nodeTask.getKey());
                if (TILoaderUtils.isImageResourceValid(resource)){
                    taskGroup.onLoadSucceed(resource);
                }else{
                    taskGroup.onLoadFailed();
                }
                break;
            case FAILED:
                taskGroup.onLoadFailed();
                break;
            case CANCELED:
                taskGroup.onLoadCanceled();
                break;
            default:
                throw new RuntimeException("[TILoader:NodeControllerImpl] can't callback(callbackInUiThread) when NodeTask.state = " + nodeTask.getState());
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
                NodeTask nodeTask;
                while((nodeTask = responseQueue.get()) != null){
                    executeNodeTask(nodeTask);
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
                    host.callbackInUiThread((NodeTask) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

}
