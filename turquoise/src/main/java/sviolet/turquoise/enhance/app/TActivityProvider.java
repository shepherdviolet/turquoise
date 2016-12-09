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

package sviolet.turquoise.enhance.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.Menu;
import android.view.Window;

import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.app.utils.InjectUtils;
import sviolet.turquoise.util.droid.ApplicationUtils;
import sviolet.turquoise.util.droid.StatusBarUtils;

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

    TActivityProvider(){

    }

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
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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

        //状态栏效果
        StatusBarUtils statusBarUtils = new StatusBarUtils();
        //状态栏透明
        if (getActivitySettings(activity).translucentStatus()) {
            statusBarUtils.setStatusBarTranslucent();
        }
        //底部按钮透明
        if (getActivitySettings(activity).translucentNavigation()){
            statusBarUtils.setNavigationBarTranslucent();
        }
        //状态栏ICON深色
        if (getActivitySettings(activity).lightStatusIcon()){
            statusBarUtils.setStatusBarIconLight();
        }
        //状态栏颜色
        if (getActivitySettings(activity).statusBarColor() != ActivitySettings.DEF_STATUS_BAR_COLOR) {
            statusBarUtils.setStatusBarColor(getActivitySettings(activity).statusBarColor());
        }
        //底部按钮颜色
        if (getActivitySettings(activity).navigationBarColor() != ActivitySettings.DEF_NAVIGATION_BAR_COLOR) {
            statusBarUtils.setNavigationBarColor(getActivitySettings(activity).navigationBarColor());
        }
        //应用
        statusBarUtils.apply(activity);
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

}
