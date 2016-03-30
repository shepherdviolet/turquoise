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

package sviolet.turquoise.x.imageloader.server;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.model.common.LazySingleThreadPool;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 *
 *
 * Created by S.Violet on 2016/2/19.
 */
public abstract class Engine implements ComponentManager.Component, Server {

    private ComponentManager manager;

    private LazySingleThreadPool dispatchThreadPool = new LazySingleThreadPool();
    private ExecutorService taskThreadPool = Executors.newCachedThreadPool();

    private volatile int taskCount = 0;

    private final ReentrantLock taskCountLock = new ReentrantLock();

    /**
     * single Thread to operate the cache!
     */
    private List<Task> cache;

    @Override
    public void init(ComponentManager manager){
        this.manager = manager;
    }

    /**
     * single Thread to operate the method!
     */
    private Task getTask(){
        if (cache == null || cache.size() <= 0){
            cache = manager.getNodeManager().pullTasks(getServerType());
        }
        if (cache != null && cache.size() > 0){
            return cache.remove(0);
        }
        return null;
    }

    /**
     * notify engine to work
     */
    public void ignite(){
        dispatchThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                while (competeThread()){
                    Task task = getTask();
                    if (task != null){
                        executeTask(task);
                    }else{
                        break;
                    }
                }
            }
        });
    }

    private void executeTask(final Task task) {
        taskThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                executeNewTask(task);
            }
        });
    }

    protected void response(Task task){
        manager.getNodeManager().response(task);
        releaseThread();
        ignite();
    }

    protected ComponentManager getComponentManager(){
        return manager;
    }

    /**
     * the method invoked on single Thread, which "dispatch thread"
     * @param task the task to execute
     */
    protected abstract void executeNewTask(Task task);

    protected abstract int getMaxThread();

    /**
     * compete thread to execute new task
     * @return ready to execute new task
     */
    private boolean competeThread(){
        final int maxThread = getMaxThread();
        try{
            taskCountLock.lock();
            if (taskCount < maxThread){
                taskCount++;
                return true;
            }
        }finally {
            taskCountLock.unlock();
        }
        return false;
    }

    private void releaseThread(){
        try{
            taskCountLock.lock();
            taskCount--;
        }finally {
            taskCountLock.unlock();
        }
    }

}
