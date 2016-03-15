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

import sviolet.turquoise.model.common.LazySingleThreadPool;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.node.NodeTask;

/**
 *
 *
 * Created by S.Violet on 2016/2/19.
 */
public abstract class Engine implements ComponentManager.Component, Server {

    private ComponentManager manager;

    private LazySingleThreadPool dispatchThreadPool = new LazySingleThreadPool();

    /**
     * single Thread to operate the cache!
     */
    private List<NodeTask> cache;

    @Override
    public void init(ComponentManager manager){
        this.manager = manager;
    }

    /**
     * single Thread to operate the method!
     */
    private NodeTask getNodeTask(){
        if (cache == null || cache.size() <= 0){
            cache = manager.getNodeManager().pullNodeTasks(getServerType());
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
                if (isReady()){
                    NodeTask task = getNodeTask();
                    if (task != null){
                        executeNewTask(task);
                    }
                }
            }
        });
    }

    protected void response(NodeTask task){
        manager.getNodeManager().response(task);
    }

    /**
     * the method invoked on single Thread, which "dispatch thread"
     * @param task the Task to execute
     */
    protected abstract void executeNewTask(NodeTask task);

    /**
     * @return is Engine ready to execute new task
     */
    protected abstract boolean isReady();

}
