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
import android.view.View;

import sviolet.turquoise.x.imageloader.entity.NodeSettings;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.server.Server;
import sviolet.turquoise.x.imageloader.stub.Stub;
import sviolet.turquoise.x.imageloader.server.Engine;

/**
 * <p>Node</p>
 *
 * <p>Manage loading tasks / context lifecycle / settings.
 * Maintain the relationship between the {@link Stub} and the {@link Server}/{@link Engine}.</p>
 *
 * <p>User-oriented, implement by the {@link NodeController} actually.</p>
 *
 * Created by S.Violet on 2016/2/16.
 */
public abstract class Node {

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * [Initialize Node]this method will initialize Node<br/>
     *
     * <p>loading image to view.</p>
     *
     * <p>Loaded Image will be cached by MemoryCache/DiskCache, you should not recycle Bitmap manually.
     * Loading task will be affected by lifecycle of context, it will frozen while Context->OnStop and
     * destroyed while Context->OnDestroy. Each LoadNode has request queue, when the queue is full earliest
     * task will be discarded, and callback to cancel. This mechanism is used to filter excessive tasks,
     * specially in ListView. You can use {@link NodeSettings.Builder#setRequestQueueSize(int)}
     * methods to make screen accommodate more pictures.</p>
     *
     * @param url URL
     * @param view target View
     */
    public abstract void load(String url, View view);

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * [Initialize Node]this method will initialize Node<br/>
     *
     * <p>loading image to view.</p>
     *
     * <p>Loaded Image will be cached by MemoryCache/DiskCache, you should not recycle Bitmap manually.
     * Loading task will be affected by lifecycle of context, it will frozen while Context->OnStop and
     * destroyed while Context->OnDestroy. Each LoadNode has request queue, when the queue is full earliest
     * task will be discarded, and callback to cancel. This mechanism is used to filter excessive tasks,
     * specially in ListView. You can use {@link NodeSettings.Builder#setRequestQueueSize(int)}
     * methods to make screen accommodate more pictures.</p>
     *
     * @param url URL
     * @param params loading params
     * @param view target View
     */
    public abstract void load(String url, Params params, View view);

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * [Initialize Node]this method will initialize Node<br/>
     *
     * <p>loading image to background.</p>
     *
     * <p>Loaded Image will be cached by MemoryCache/DiskCache, you should not recycle Bitmap manually.
     * Loading task will be affected by lifecycle of context, it will frozen while Context->OnStop and
     * destroyed while Context->OnDestroy. Each LoadNode has request queue, when the queue is full earliest
     * task will be discarded, and callback to cancel. This mechanism is used to filter excessive tasks,
     * specially in ListView. You can use {@link NodeSettings.Builder#setRequestQueueSize(int)}
     * methods to make screen accommodate more pictures.</p>
     *
     * @param url URL
     * @param view View
     */
    public abstract void loadBackground(String url, View view);

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * [Initialize Node]this method will initialize Node<br/>
     *
     * <p>loading image to background.</p>
     *
     * <p>Loaded Image will be cached by MemoryCache/DiskCache, you should not recycle Bitmap manually.
     * Loading task will be affected by lifecycle of context, it will frozen while Context->OnStop and
     * destroyed while Context->OnDestroy. Each LoadNode has request queue, when the queue is full earliest
     * task will be discarded, and callback to cancel. This mechanism is used to filter excessive tasks,
     * specially in ListView. You can use {@link NodeSettings.Builder#setRequestQueueSize(int)}
     * methods to make screen accommodate more pictures.</p>
     *
     * @param url URL
     * @param params loading params
     * @param view View
     */
    public abstract void loadBackground(String url, Params params, View view);

    /**
     * Node Setting, you should setting before Node initialized (invoke TILoader.node().load() will initialize Node)<br/>
     * e.g setting in Activity.onCreate()<br/>
     * @param settings Node Settings
     * @return true : setting effective. it will be false when setting after Node initialized, and make no effect.
     */
    public abstract boolean setting(NodeSettings settings);

    /***************************************************
     * protected
     */

    /**
     * @return get Id of Node
     */
    abstract String getId();

    /**
     * @return pull a Task to executing by Engine
     */
    abstract Task pullTask(Server.Type type);

    /**
     * @param task response when Task executed by Engine
     */
    abstract void response(Task task);

    protected abstract void attachLifeCycle(Context context);

    /***************************************************
     * NodeRemoter
     */

    /**
     * @see NodeRemoter
     */
    public abstract NodeRemoter newNodeRemoter();
}
