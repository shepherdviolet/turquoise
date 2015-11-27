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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhanced.utils.InjectUtils;
import sviolet.turquoise.utils.CheckUtils;
import sviolet.turquoise.utils.Logger;
import sviolet.turquoise.utils.sys.ApplicationUtils;
import sviolet.turquoise.utils.sys.DeviceUtils;
import sviolet.turquoise.utils.sys.MeasureUtils;

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

    /**********************************************
     * Public
     *
     * Runtime Permission
     */

    //权限请求请求码
    private AtomicInteger mPermissionRequestCode = new AtomicInteger(Integer.MAX_VALUE);
    //权限请求任务池
    private SparseArray<RequestPermissionTask> mPermissionRequestTaskPool = new SparseArray<>();

    /**
     * 权限请求任务(结果监听器)
     */
    public interface RequestPermissionTask {
        /**
         * 权限请求结果
         *
         * @param permissions 权限列表 android.Manifest.permission....
         * @param grantResults 结果列表 PackageManager.PERMISSION_....
         * @param allGranted 是否所有请求的权限都被允许
         */
        public void onResult(String[] permissions, int[] grantResults, boolean allGranted);
    }

    /**
     * 权限结果回调
     */
    interface RequestPermissionsCallback {
        void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, boolean allGranted);
    }

    /**
     * 执行一个需要权限的任务<br/>
     * 检查权限->显示说明->请求权限->回调监听器<br/>
     * 目的任务在监听器中实现, 需要判断权限是否被授予<br/>
     *
     * @param permissions 任务需要的权限 android.Manifest.permission....
     * @param rationaleTitle 权限说明标题(标题和内容都送空, 则不提示)
     * @param rationaleContent 权限说明内容(标题和内容都送空, 则不提示)
     * @param task 在监听器中判断权限是否授予, 并执行目的任务
     */
    void executePermissionTask(final Activity activity, final String[] permissions, String rationaleTitle, String rationaleContent, final RequestPermissionTask task){
        if (permissions == null || permissions.length <= 0){
            throw new IllegalArgumentException("permissions is null");
        }
        //判断权限是否已开启
        boolean allGranted = true;
        for (String permission : permissions){
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED){
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            //权限已开启则直接回调任务
            int[] grantResults = new int[permissions.length];
            for (int i = 0 ; i < grantResults.length ; i++){
                grantResults[i] = PackageManager.PERMISSION_GRANTED;
            }
            task.onResult(permissions, grantResults, true);
        }else{
            //有权限未开启, 则请求权限

            //判断是否需要显示提示
            boolean shouldShowRationale = false;
            for (String permission : permissions){
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
                    shouldShowRationale = true;
                    break;
                }
            }

            if ((!CheckUtils.isEmpty(rationaleTitle) || !CheckUtils.isEmpty(rationaleContent)) && shouldShowRationale) {
                //显示权限提示
                new PermissionRationaleDialog(activity, rationaleTitle, rationaleContent, new DialogInterface.OnCancelListener(){
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //请求权限
                        requestPermissions(activity, permissions, task);
                    }
                }).show();
            }else{
                //请求权限
                requestPermissions(activity, permissions, task);
            }
        }
    }

    /**
     * 请求权限<br/>
     * 支持低版本<br/>
     *
     * @param permissions 权限列表 android.Manifest.permission....
     * @param task 任务
     */
    private void requestPermissions(Activity activity, final String[] permissions, RequestPermissionTask task) {
        //递减的请求号
        final int requestCode = mPermissionRequestCode.getAndDecrement();
        //任务加入任务池
        mPermissionRequestTaskPool.put(requestCode, task);
        //请求权限
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * 权限请求结果处理
     */
    void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        //判断权限是否全部允许
        boolean allGranted = true;
        if(grantResults == null || grantResults.length <= 0){
            allGranted = false;
        }else {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
        }
        //任务池中取出任务
        RequestPermissionTask task = mPermissionRequestTaskPool.get(requestCode);

        if(task != null) {
            //回调任务
            task.onResult(permissions, grantResults, allGranted);
            //从任务池中移除
            mPermissionRequestTaskPool.remove(requestCode);
        }else if (activity instanceof RequestPermissionsCallback){
            //任务不存在则调用Activity方法
            ((RequestPermissionsCallback)activity).onRequestPermissionsResult(requestCode, permissions, grantResults, allGranted);
        }
    }

    /**
     * 权限说明窗口
     */
    private static final class PermissionRationaleDialog extends Dialog {

        public PermissionRationaleDialog(Context context, String rationaleTitle, String rationaleContent, DialogInterface.OnCancelListener listener) {
            super(context, true, listener);

            if(!CheckUtils.isEmpty(rationaleTitle)) {
                setTitle(rationaleTitle);
            }

            TextView textView = new TextView(getContext());
            if (!CheckUtils.isEmpty(rationaleContent)) {
                textView.setText(rationaleContent);
            }
            textView.setTextColor(0xFF808080);
            final int dp15 = MeasureUtils.dp2px(getContext(), 15);
            final int dp10 = MeasureUtils.dp2px(getContext(), 10);
            textView.setPadding(dp15, dp15, dp10, dp10);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            addContentView(textView, params);

        }
    }

}
