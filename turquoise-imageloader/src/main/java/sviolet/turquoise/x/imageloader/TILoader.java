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

package sviolet.turquoise.x.imageloader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresPermission;

import java.lang.ref.WeakReference;

import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.entity.ServerSettings;
import sviolet.turquoise.x.imageloader.node.Node;
import sviolet.turquoise.x.imageloader.stub.StubRemoter;

/**
 * <p>Turquoise Image Loader</p>
 *
 * Created by S.Violet on 2016/2/16.
 */
public class TILoader {

    /**
     * fetch node to load image
     * @param context context
     * @return get Node of Context, used to loading image
     */
    @RequiresPermission(allOf = {"android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"})
    public static Node node(Activity context){
        checkContext(context);
        return ComponentManager.getInstance().getNodeManager().fetchNode(context);
    }

    /**
     * fetch node to load image
     * @param context context
     * @return get Node of Context, used to loading image
     */
    @RequiresPermission(allOf = {"android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"})
    public static Node node(android.support.v4.app.FragmentActivity context){
        checkContext(context);
        return ComponentManager.getInstance().getNodeManager().fetchNode(context);
    }

    /**
     * fetch node to load image
     * @param context context
     * @return get Node of Context, used to loading image
     */
    @RequiresPermission(allOf = {"android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"})
    public static Node node(android.support.v4.app.Fragment context){
        checkContext(context);
        return ComponentManager.getInstance().getNodeManager().fetchNode(context.getActivity());
    }

    /**
     * fetch node to load image
     * @param context context
     * @return get Node of Context, used to loading image
     */
    @RequiresPermission(allOf = {"android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"})
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Node node(Fragment context){
        checkContext(context);
        return ComponentManager.getInstance().getNodeManager().fetchNode(context.getActivity());
    }

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * [Initialize Node]this method will initialize Node<br/>
     *
     * <p>Extract Image. Extracted Image will not be cached by MemoryCache, you should recycle Bitmap
     * by yourself. Extract task will not be affected by lifecycle of context, it will not be discarded
     * from queue.</p>
     *
     * {@link OnLoadedListener}:<br/>
     * <p>Improper use may cause a memory leak, ExtractNode will hold this listener util extract finished,
     * avoid being an internal class and holding Context/View object, you can use method
     * {@link OnLoadedListener#setWeakRegister(Object)} to hold Context/View by {@link WeakReference},
     * and {@link OnLoadedListener#getWeakRegister()} when callback.</p>
     *
     * <pre>{@code
     *      TILoader.extract(this, url, params, new OnLoadedListener<XXXActivity>() {
     *         protected void onLoadSucceed(String url, Params params, ImageResource resource) {
     *              XXXActivity activity = getWeakRegister();
     *              if (activity != null){
     *                  //do something
     *              }
     *         }
     *         protected void onLoadCanceled(String url, Params params) {
     *
     *         }
     *      }.setWeakRegister(this));
     * }</pre>
     *
     * @param context applicationContext or activity/fragment context
     * @param url URL
     * @param params loading params
     * @param listener callback when loading succeed / canceled / failed
     * @return {@link StubRemoter}
     */
    @RequiresPermission(allOf = {"android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"})
    public static StubRemoter extract(Context context, String url, Params params, OnLoadedListener listener) {
        checkContext(context);
        return ComponentManager.getInstance().getNodeManager().fetchExtractNode(context).extract(url, params, listener);
    }

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * Server Setting, you should setting before TILoader initialized (invoke TILoader.setting() or TILoader.node().load() will initialize TILoader).<br/>
     * e.g setting in Application.onCreate()<br/>
     * @param settings Server Settings
     * @return true : setting effective. it will be false when setting after TILoader initialized, and make no effect.
     */
    public static boolean setting(ServerSettings settings){
        return ComponentManager.getInstance().settingServer(settings);
    }

    /**
     * check if context valid
     * @param context context
     */
    private static void checkContext(Object context){
        if (context == null){
            throw new RuntimeException("[TILoader]can not get loading Node with out Activity or Fragment");
        }
    }

}
