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

package sviolet.turquoise.ui.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import sviolet.turquoise.util.droid.DeviceUtils;

/**
 * <p>状态栏工具(顶部状态栏/底部按钮栏)</p>
 *
 * Created by S.Violet on 2016/3/9.
 */
public class StatusBarUtils {

    /**
     * [API21]透明状态栏/底部按钮, 最大化Activity
     * @param activity activity
     * @return true:设置成功
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean setTranslucent(Activity activity){
        if (activity == null){
            return false;
        }
        if (DeviceUtils.getVersionSDK() >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                            | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            activity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            return true;
        }
        return false;
    }

    /**
     * [API21]设置状态栏颜色, 若需要透明, 请先调用{@link StatusBarUtils#setTranslucent(Activity)}
     * @param activity activity
     * @param color 颜色
     * @return true:设置成功
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean setStatusBarColor(Activity activity, int color){
        if (activity == null){
            return false;
        }
        if (DeviceUtils.getVersionSDK() >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(color);
            return true;
        }
        return false;
    }

    /**
     * [API21]设置底部按钮颜色, 若需要透明, 请先调用{@link StatusBarUtils#setTranslucent(Activity)}
     * @param activity activity
     * @param color 颜色
     * @return true:设置成功
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean setNavigationBarColor(Activity activity, int color){
        if (activity == null){
            return false;
        }
        if (DeviceUtils.getVersionSDK() >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setNavigationBarColor(color);
            return true;
        }
        return false;
    }

}
