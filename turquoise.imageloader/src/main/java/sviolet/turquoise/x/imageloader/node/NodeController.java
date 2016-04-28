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

import java.util.concurrent.atomic.AtomicInteger;

import sviolet.turquoise.utilx.lifecycle.listener.LifeCycle;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.drawable.BackgroundDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.FailedDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.LoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;
import sviolet.turquoise.x.imageloader.entity.ServerSettings;
import sviolet.turquoise.x.imageloader.server.Engine;
import sviolet.turquoise.x.imageloader.server.Server;
import sviolet.turquoise.x.imageloader.stub.Stub;

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
 * <p>Pause:: Engine will not execute tasks which in paused Node. Node will pause util all NodeRemotes are resumed(not pause).
 * As long as there is a paused NodeRemoter, Node will keep pause status.</p>
 *
 * <p>Frozen:: Engine will not execute tasks of this Node, skip dispatch (skip callback to stub, skip handle response from Engine).
 * Node will frozen when Context->OnStop(), and unfreeze when Context->OnStart().</p>
 *
 * <p>Destroy:: Engine will destroy when Context->OnDestroy(), and skip all process.</p>
 *
 * Created by S.Violet on 2016/2/18.
 */
public abstract class NodeController implements LifeCycle {

    public static final int DESTROYED = -1;
    public static final int INITIAL = 0;
    public static final int FROZEN = 1;
    public static final int NORMAL = 2;

    abstract void waitingForInitialized();

    /***********************************************************
     * load
     */

    /**
     * execute a Stub
     * @param stub
     */
    public abstract void execute(Stub stub);

    /***********************************************************
     * control
     */

    abstract Task pullTask(Engine.Type type);

    /**
     * @param task response when Task executed by Engine
     */
    abstract void response(Task task);

    abstract boolean settingNode(NodeSettings settings);

    /**
     * notify the DispatchThread to dispatch tasks
     */
    public abstract void postDispatch();

    /**
     * notify Engines to process
     */
    abstract void postIgnite();

    /***********************************************************
     * getter
     */

    public abstract String getNodeId();

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

    public abstract Context getApplicationContextImage();

    public abstract Context getContextImage();

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
    public abstract BackgroundDrawableFactory getBackgroundDrawableFactory();

    public abstract boolean isDestroyed();

    public abstract TLogger getLogger();

    /**************************************************************
     * NodeRemoter
     */

    abstract AtomicInteger getNodePauseCount();

    /**
     * @see NodeRemoter
     */
    abstract NodeRemoter newNodeRemoter();

}
