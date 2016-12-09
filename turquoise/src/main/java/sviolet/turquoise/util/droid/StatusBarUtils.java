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

package sviolet.turquoise.util.droid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

/**
 * <p>状态栏工具(顶部状态栏/底部按钮栏)</p>
 *
 * Created by S.Violet on 2016/3/9.
 */
public class StatusBarUtils {

    private boolean statusBarTranslucent = false;
    private boolean navigationBarTranslucent = false;
    private boolean statusBarIconLight = false;
    private boolean isCustomStatusBarColor = false;
    private int statusBarColor;
    private boolean isCustomNavigationBarColor = false;
    private int navigationBarColor;

    /**
     * 设置状态栏透明, API21
     */
    public StatusBarUtils setStatusBarTranslucent(){
        statusBarTranslucent = true;
        return this;
    }

    /**
     * 设置导航栏透明, API21
     */
    public StatusBarUtils setNavigationBarTranslucent(){
        navigationBarTranslucent = true;
        return this;
    }

    /**
     * 设置状态栏ICON深色, API23
     */
    public StatusBarUtils setStatusBarIconLight(){
        statusBarIconLight = true;
        return this;
    }

    /**
     * 设置状态栏颜色, API21
     */
    public StatusBarUtils setStatusBarColor(int color){
        isCustomStatusBarColor = true;
        statusBarColor = color;
        return this;
    }

    /**
     * 设置导航栏颜色, API21
     */
    public StatusBarUtils setNavigationBarColor(int color){
        isCustomNavigationBarColor = true;
        navigationBarColor = color;
        return this;
    }

    /**
     * 生效
     * @param activity activity
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void apply(Activity activity){
        if (activity == null){
            return;
        }
        if (DeviceUtils.getVersionSDK() >= Build.VERSION_CODES.LOLLIPOP) {
            int flag = 0x00000000;
            //状态栏透明
            if (statusBarTranslucent){
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }
            //导航栏透明
            if (navigationBarTranslucent){
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            }
            //状态栏Icon深色
            if (statusBarIconLight && DeviceUtils.getVersionSDK() >= Build.VERSION_CODES.M){
                flag |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            activity.getWindow().getDecorView().setSystemUiVisibility(flag);

            //设置状态栏颜色
            if (isCustomStatusBarColor){
                //必须清除标志
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                //必须设置标志
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                activity.getWindow().setStatusBarColor(statusBarColor);
            }

            //设置导航栏颜色
            if (isCustomNavigationBarColor){
                //必须清除标志
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                //必须设置标志
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                activity.getWindow().setNavigationBarColor(navigationBarColor);
            }
        }
    }

}
