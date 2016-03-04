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

import android.view.View;

import sviolet.turquoise.utilx.lifecycle.listener.LifeCycle;
import sviolet.turquoise.x.imageloader.engine.Engine;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.task.Task;

/**
 * Node<p/>
 *
 * 
 *
 * Created by S.Violet on 2016/2/16.
 */
public abstract class Node implements LifeCycle{

    /**
     * loading image to view
     * @param url URL
     * @param view target View
     */
    public abstract void load(String url, View view);

    /**
     * loading image to view
     * @param url URL
     * @param params loading params
     * @param view target View
     */
    public abstract void load(String url, Params params, View view);

    /**
     * loading image to background of view
     * @param url URL
     * @param view View
     */
    public abstract void loadBackground(String url, View view);

    /**
     * loading image to background of view
     * @param url URL
     * @param params loading params
     * @param view View
     */
    public abstract void loadBackground(String url, Params params, View view);

    /**
     * extract bitmap, without memory cache and disk cache<br/>
     * you should recycle Bitmap by yourself<br/>
     * @param url URL
     * @param params loading params
     * @param listener callback when loading succeed / canceled / failed
     */
    public abstract void extract(String url, Params params, OnLoadedListener listener);

    /**
     * Node Setting, first time effective for each Node
     * @param settings Node Settings
     */
    public abstract void setting(NodeSettings settings);

    /**
     * @return get Id of Node
     */
    protected abstract String getId();

    /**
     * @return pull a NodeTask to executing by Engine
     */
    protected abstract NodeTask pullNodeTask(Engine.Type type);

    /**
     * @param task response when NodeTask executed by Engine
     */
    protected abstract void response(NodeTask task);
}
