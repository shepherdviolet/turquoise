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
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import sviolet.turquoise.x.imageloader.entity.EngineSettings;
import sviolet.turquoise.x.imageloader.node.Node;

/**
 * Turquoise Image Loader
 *
 * Created by S.Violet on 2016/2/16.
 */
public class TILoader {

    /**
     * @param context context
     * @return get Node of Context, used to loading image
     */
    public static Node node(Activity context){
        checkContext(context);
        return ComponentManager.getInstance().getNodeManager().fetchNode(context);
    }

    /**
     * @param context context
     * @return get Node of Context, used to loading image
     */
    public static Node node(FragmentActivity context){
        checkContext(context);
        return ComponentManager.getInstance().getNodeManager().fetchNode(context);
    }

    /**
     * @param context context
     * @return get Node of Context, used to loading image
     */
    public static Node node(Fragment context){
        checkContext(context);
        return ComponentManager.getInstance().getNodeManager().fetchNode(context.getActivity());
    }

    /**
     * above API level 11
     * @param context context
     * @return get Node of Context, used to loading image
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static Node node(android.app.Fragment context){
        checkContext(context);
        return ComponentManager.getInstance().getNodeManager().fetchNode(context.getActivity());
    }

    /**
     * Engine Setting, you should setting before used<br/>
     * e.g setting in Application.onCreate()<br/>
     * @param settings Engine Settings
     * @return true : setting effective. it will be false when setting after used, and make no effect
     */
    public static boolean setting(EngineSettings settings){
        return ComponentManager.getInstance().settingEngine(settings);
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
