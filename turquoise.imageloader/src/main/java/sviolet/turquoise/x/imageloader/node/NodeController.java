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

import sviolet.turquoise.utilx.lifecycle.listener.LifeCycle;
import sviolet.turquoise.x.imageloader.drawable.BackgroundDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.FailedDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.LoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.entity.ServerSettings;
import sviolet.turquoise.x.imageloader.server.Engine;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;
import sviolet.turquoise.x.imageloader.task.Task;

/**
 * Controller of Node<p/>
 *
 * 1.logic of Task execution <br/>
 * 2.Task Queues <br/>
 *
 * Created by S.Violet on 2016/2/18.
 */
public abstract class NodeController implements LifeCycle {

    abstract void waitingForInitialized();

    /**
     * execute a Task
     * @param task
     */
    public abstract void executeTask(Task task);

    abstract NodeTask pullNodeTask(Engine.Type type);

    /**
     * @param task response when NodeTask executed by Engine
     */
    abstract void response(NodeTask task);

    public abstract String getNodeId();

    abstract boolean settingNode(NodeSettings settings);

    /**
     * node must be initialized before get NodeSettings
     * @return get NodeSettings
     */
    public abstract NodeSettings getNodeSettings();

    /**
     * TLoader must be initialized before get ServerSettings
     * @return get ServerSettings
     */
    public abstract ServerSettings getServerSettings();

    /**
     * @return get LoadingDrawableFactory from Node, if it's null, get from ServerSettings
     */
    public abstract LoadingDrawableFactory getLoadingDrawableFactory();

    /**
     * @return get FailedDrawableFactory from Node, if it's null, get from ServerSettings
     */
    public abstract FailedDrawableFactory getFailedDrawableFactory();

    /**
     * @return get BackgroundDrawableFactory from Node, if it's null, get from ServerSettings
     */
    public abstract BackgroundDrawableFactory BackgroundDrawableFactory();

    /**
     * notify the DispatchThread to dispatch tasks
     */
    public abstract void postDispatch();
}
