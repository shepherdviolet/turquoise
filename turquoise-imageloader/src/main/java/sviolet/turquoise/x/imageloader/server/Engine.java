/*
 * Copyright (C) 2015-2017 S.Violet
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

package sviolet.turquoise.x.imageloader.server;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;
import sviolet.turquoise.x.imageloader.node.Node;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>Asynchronous task processor, with fixed thread pool.</p>
 *
 * <p>Pull tasks from {@link Node}, execute tasks, and callback to {@link Node}</p>
 *
 * Created by S.Violet on 2016/2/19.
 */
public abstract class Engine implements ComponentManager.Component, Server {

    private ComponentManager manager;

    private ExecutorService dispatchThreadPool = ThreadPoolExecutorUtils.createLazy(60L, "TLoader-Engine-dispatcher-%d");
    private ExecutorService taskThreadPool = ThreadPoolExecutorUtils.createCached(0, Integer.MAX_VALUE, 60L, "TLoader-Engine-worker-%d");

    private AtomicInteger taskCount = new AtomicInteger(0);
    private List<Task> cache;//single Thread to operate the cache!

    /***************************************************************************
     * abstract
     */

    /**
     * Pre-check if the task can be executed by this server
     */
    protected abstract boolean preCheck(Task task);

    /**
     * the method invoked on single Thread, which "dispatch thread"
     * @param task the task to execute
     */
    protected abstract void executeNewTask(Task task);

    protected abstract int getMaxThread();

    /***************************************************************************
     * public
     */

    @Override
    public void init(ComponentManager manager){
        this.manager = manager;
    }

    protected void response(Task task){
        manager.getNodeManager().response(task);
    }

    /**
     * notify engine to work
     */
    public void ignite(){
        dispatchThreadPool.execute(dispatchRunnable);
    }

    private Runnable dispatchRunnable = new Runnable() {
        @Override
        public void run() {
            while (taskCount.get() < getMaxThread()){
                Task task = getTask();
                if (task != null){
                    executeTask(task);
                }else{
                    break;
                }
            }
        }
    };

    /**
     * single Thread to operate the method!
     */
    private Task getTask(){
        if (cache == null || cache.size() <= 0){
            cache = manager.getNodeManager().pullTasks(getServerType());
//            manager.getLogger().d("[Engine:" + getServerType() + "]get task from nodeManager");
        }
        if (cache != null && cache.size() > 0){
//            manager.getLogger().d("[Engine:" + getServerType() + "]get task from cache");
            return cache.remove(0);
        }
//        manager.getLogger().d("[Engine:" + getServerType() + "]get task failed, nothing");
        return null;
    }

    private void executeTask(final Task task) {
        taskCount.incrementAndGet();
        taskThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!preCheck(task)) {
                        getComponentManager().getLogger().e("[Engine]This task can not be executed in this server, serverType:" + getServerType() + ", task:" + task);
                        task.setState(Task.State.CANCELED);
                        response(task);
                        return;
                    }
                    //EXECUTE
                    executeNewTask(task);
                } finally {
                    taskCount.decrementAndGet();
                    ignite();
                }
            }
        });
    }

    /***************************************************************************
     * getter setter
     */

    protected ComponentManager getComponentManager(){
        return manager;
    }

    public NetworkLoadHandler getNetworkLoadHandler(Task task){
        if (task.getNodeSettings() != null){
            if (task.getNodeSettings().getNetworkLoadHandler() != null){
                return task.getNodeSettings().getNetworkLoadHandler();
            }
        }
        return getComponentManager().getServerSettings().getNetworkLoadHandler();
    }

    public DecodeHandler getDecodeHandler(Task task){
        return getComponentManager().getServerSettings().getDecodeHandler();
    }

    /**
     * @return milli second
     */
    public long getNetworkConnectTimeout(Task task){
        if (task.getNodeSettings() != null){
            if (task.getNodeSettings().getNetworkConnectTimeout() > 0){
                return task.getNodeSettings().getNetworkConnectTimeout();
            }
        }
        return getComponentManager().getServerSettings().getNetworkConnectTimeout();
    }

    /**
     * @return milli second
     */
    public long getNetworkReadTimeout(Task task){
        if (task.getNodeSettings() != null){
            if (task.getNodeSettings().getNetworkReadTimeout() > 0){
                return task.getNodeSettings().getNetworkReadTimeout();
            }
        }
        return getComponentManager().getServerSettings().getNetworkReadTimeout();
    }

}
