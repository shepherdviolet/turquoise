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
import sviolet.turquoise.x.imageloader.engine.Engine;
import sviolet.turquoise.x.imageloader.entity.EngineSettings;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;
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

    private RequestQueue cacheRequestQueue;//缓存加载等待队列
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
            NodeTask nodeTask = manager.getNodeTaskFactory().newNodeTask(this, task);
            NodeTask obsoleteNodeTask = cacheRequestQueue.put(nodeTask);
            manager.getCacheEngine().ignite();
            postObsoleteTask(obsoleteNodeTask);
        }
    }

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
    NodeSettings getNodeSettings() {
        return settings;
    }

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
        cacheRequestQueue = new RequestQueueImpl(settings.getCacheQueueSize());
        diskRequestQueue = new RequestQueueImpl(settings.getDiskQueueSize());
        netRequestQueue = new RequestQueueImpl(settings.getNetQueueSize());
    }

    @Override
    NodeTask pullNodeTask(Engine.Type type) {
        return null;
    }

    @Override
    void response(NodeTask task) {
        responseQueue.put(task);
        postDispatch();
    }

    private void postObsoleteTask(NodeTask obsoleteNodeTask){
        if (obsoleteNodeTask == null)
            return;

        TaskGroup obsoleteTaskGroup;
        try {
            taskPoolLock.lock();
            obsoleteTaskGroup = taskPool.remove(obsoleteNodeTask.getKey());
        } finally {
            taskPoolLock.unlock();
        }

        Message msg = myHandler.obtainMessage(MyHandler.HANDLER_OBSOLETE_TASK);
        msg.obj = obsoleteTaskGroup;
        msg.sendToTarget();
    }

    private void obsoleteTask(TaskGroup obsoleteTaskGroup){
        if (obsoleteTaskGroup != null){
            obsoleteTaskGroup.onLoadCanceled();
        }
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

        private static final int HANDLER_OBSOLETE_TASK = 1;

        public MyHandler(Looper looper, NodeControllerImpl host) {
            super(looper, host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, NodeControllerImpl host) {
            switch (msg.what){
                case HANDLER_OBSOLETE_TASK:
                    host.obsoleteTask((TaskGroup) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

}
