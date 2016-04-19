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
import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.server.Server;

/**
 * Node<p/>
 *
 * 
 *
 * Created by S.Violet on 2016/2/16.
 */
public abstract class Node {

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * [Initialize Node]this method will initialize Node<br/>
     * loading image to view
     * @param url URL
     * @param view target View
     */
    public abstract void load(String url, View view);

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * [Initialize Node]this method will initialize Node<br/>
     * loading image to view
     * @param url URL
     * @param params loading params
     * @param view target View
     */
    public abstract void load(String url, Params params, View view);

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * [Initialize Node]this method will initialize Node<br/>
     * loading image to background of view
     * @param url URL
     * @param view View
     */
    public abstract void loadBackground(String url, View view);

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * [Initialize Node]this method will initialize Node<br/>
     * loading image to background of view
     * @param url URL
     * @param params loading params
     * @param view View
     */
    public abstract void loadBackground(String url, Params params, View view);

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * [Initialize Node]this method will initialize Node<br/>
     * extract Image, without memory cache and disk cache<br/>
     * you should recycle Bitmap by yourself<br/>
     * @param url URL
     * @param params loading params
     * @param listener callback when loading succeed / canceled / failed
     */
    public abstract void extract(String url, Params params, OnLoadedListener listener);

    /**
     * Node Setting, you should setting before Node initialized (invoke TILoader.node().load() will initialize Node)<br/>
     * e.g setting in Activity.onCreate()<br/>
     * @param settings Node Settings
     * @return true : setting effective. it will be false when setting after Node initialized, and make no effect.
     */
    public abstract boolean setting(NodeSettings settings);

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

    abstract void attachLifeCycle(Context context);
}
