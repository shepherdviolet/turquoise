/*
 * Copyright (C) 2015 S.Violet
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

package sviolet.turquoise.enhanced;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhanced.utils.InjectUtils;
import sviolet.turquoise.utils.Logger;
import sviolet.turquoise.utils.sys.ApplicationUtils;
import sviolet.turquoise.utils.sys.DeviceUtils;

/**
 * [组件扩展]Activity<br>
 * <br>
 * 0.Activity注释式设置<br/>
 * {@link ActivitySettings};<br/>
 * <br/>
 * 1.InjectUtils注释式注入控件对象/绑定监听<br/>
 * {@link InjectUtils};<br/>
 * <br>
 *
 * Created by S.Violet on 2015/11/27.
 */
public class TActivityProvider {

    private ActivitySettings settings;

    /**
     * 获得@ActivitySettings设置标签
     *
     * @return
     */
    ActivitySettings getActivitySettings(Activity activity) {
        if (settings == null) {
            if (activity.getClass().isAnnotationPresent(ActivitySettings.class)) {
                settings = activity.getClass().getAnnotation(ActivitySettings.class);
            }
        }
        return settings;
    }

    /**
     * 根据@ActivitySettings标签进行窗口设置
     */
    @SuppressLint("NewApi")
    void windowSetting(Activity activity) {
        if (getActivitySettings(activity) == null)
            return;

        //硬件加速
        if(getActivitySettings(activity).enableHardwareAccelerated()){
            ApplicationUtils.enableHardwareAccelerated(activity.getWindow());

        }

        //无标题
        if (getActivitySettings(activity).noTitle()) {
            activity.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        //5.0效果
        if (DeviceUtils.getVersionSDK() >= Build.VERSION_CODES.LOLLIPOP) {
            //透明状态栏/底部按钮, 最大化Activity
            if (getActivitySettings(activity).translucentBar()) {
                activity.getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                activity.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
            //状态栏颜色
            if (getActivitySettings(activity).statusBarColor() != ActivitySettings.DEF_STATUS_BAR_COLOR) {
                activity.getWindow().setStatusBarColor(getActivitySettings(activity).statusBarColor());
            }
            //底部按钮颜色
            if (getActivitySettings(activity).navigationBarColor() != ActivitySettings.DEF_NAVIGATION_BAR_COLOR) {
                activity.getWindow().setNavigationBarColor(getActivitySettings(activity).navigationBarColor());
            }
        }
    }

    void onCreate(Activity activity){
        //注释注入
        InjectUtils.inject(activity);

        //将自身加入TApplication
        if (activity.getApplication() instanceof TApplication){
            try {
                ((TApplication) activity.getApplication()).addActivity(activity);
            }catch (Exception ignored){}
        }
    }

    /**
     * 根据Activity的@OptionsMenuId标签, 注入OptionsMenu菜单布局文件<br>
     * 只需复写onOptionsItemSelected方法截获事件即可<br>
     */
    boolean onCreateOptionsMenu(Menu menu, Activity activity) {
        //菜单创建
        if (getActivitySettings(activity) != null) {
            int optionsMenuId = getActivitySettings(activity).optionsMenuId();
            if (optionsMenuId != ActivitySettings.DEF_OPTIONS_MENU_ID) {
                activity.getMenuInflater().inflate(optionsMenuId, menu);
                return true;
            }
        }
        return false;
    }

    void onDestroy(Activity activity) {
        //将自身从TApplication移除
        if (activity.getApplication() instanceof TApplication){
            try {
                ((TApplication) activity.getApplication()).removeActivity(activity);
            }catch (Exception ignored){}
        }
    }

    /*********************************
     * Utils
     */

    /**
     * 获得日志打印器(需配合TApplication)<br/>
     * 由TApplication子类标签设置日志打印权限<br/>
     * 若本应用不采用TApplication, 则返回的日志打印器无用.<br/>
     */
    Logger getLogger(Activity activity){
        if (activity.getApplication() instanceof TApplication){
            try {
                return ((TApplication) activity.getApplication()).getLogger();
            }catch (Exception ignored){}
        }
        return Logger.newInstance("", false, false, false);//返回无效的日志打印器
    }

}
