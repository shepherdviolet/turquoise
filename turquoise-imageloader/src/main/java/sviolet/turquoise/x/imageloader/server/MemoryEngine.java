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

import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>Memory cache Load Engine</p>
 *
 * Created by S.Violet on 2016/4/21.
 */
public class MemoryEngine extends Engine {

    @Override
    protected boolean preCheck(Task task) {
        return true;
    }

    @Override
    protected void executeNewTask(Task task) {
        ImageResource resource;
        try {
            resource = getComponentManager().getMemoryCacheServer().get(task.getKey());
        } catch (Exception e) {
            try {
                getComponentManager().getServerSettings().getExceptionHandler().onMemoryCacheCommonException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), e, getComponentManager().getLogger());
            } catch (Exception e2) {
                getComponentManager().getLogger().e("exception in ExceptionHandler", e2);
            }
            task.setState(Task.State.FAILED);
            response(task);
            return;
        }
        if (resource != null) {
            task.setState(Task.State.SUCCEED);
            response(task);
        }else{
            task.setState(Task.State.FAILED);
            response(task);
        }
    }

    @Override
    protected int getMaxThread() {
        return getComponentManager().getServerSettings().getMemoryLoadMaxThread();
    }

    @Override
    public Type getServerType() {
        return Type.MEMORY_ENGINE;
    }
}
