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

import android.app.Activity;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;

import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;
import sviolet.turquoise.utils.Logger;
import sviolet.turquoise.enhanced.utils.InjectUtils;
import sviolet.turquoise.utils.lifecycle.LifeCycleUtils;
import sviolet.turquoise.utils.lifecycle.listener.LifeCycle;

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
 * @author S.Violet
 */

public class TActivity extends Activity implements TActivityProvider.RequestPermissionsCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final TActivityProvider provider = new TActivityProvider();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        provider.windowSetting(this);//窗口设置
        super.onCreate(savedInstanceState);
        provider.onCreate(this);
    }

    /**
     * 根据Activity的@OptionsMenuId标签, 注入OptionsMenu菜单布局文件<br>
     * 只需复写onOptionsItemSelected方法截获事件即可<br>
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return provider.onCreateOptionsMenu(menu, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        provider.onDestroy(this);
    }

    /**********************************************
     * public
     *
     * Utils: Logger / BitmapUtils
     */

    /**
     * 获得日志打印器(需配合TApplication)<br/>
     * 由TApplication子类标签设置日志打印权限<br/>
     * 若本应用不采用TApplication, 则返回的日志打印器无用.<br/>
     *
     */
    public Logger getLogger(){
        return provider.getLogger(this);
    }

    /**
     * 将生命周期监听器绑定在该Activity上<p/>
     *
     * LifeCycleUtils不会强引用监听器, 需自行持有对象.<p/>
     *
     * @param lifeCycle 生命周期监听器
     */
    public void attachLifeCycle(LifeCycle lifeCycle){
        LifeCycleUtils.attach(this, lifeCycle);
    }

    /**********************************************
     * Public
     *
     * Runtime Permission
     */

    /**
     * 执行一个需要权限的任务, 兼容低版本<br/>
     * 检查权限->显示说明->请求权限->回调{@link TActivityProvider.RequestPermissionTask}<br/>
     * 目的任务在{@link TActivityProvider.RequestPermissionTask}中实现, 需要判断权限是否被授予<br/>
     *
     * @param permission 所需权限 android.Manifest.permission....
     * @param task 需要权限的任务
     */
    public void executePermissionTask(String permission, TActivityProvider.RequestPermissionTask task){
        provider.executePermissionTask(this, new String[]{permission}, null, null, task);
    }

    /**
     * 执行一个需要权限的任务, 兼容低版本<br/>
     * 检查权限->显示说明->请求权限->回调{@link TActivityProvider.RequestPermissionTask}<br/>
     * 目的任务在{@link TActivityProvider.RequestPermissionTask}中实现, 需要判断权限是否被授予<br/>
     *
     * @param permissions 所需权限 android.Manifest.permission....
     * @param task 需要权限的任务
     */
    public void executePermissionTask(String[] permissions, TActivityProvider.RequestPermissionTask task){
        provider.executePermissionTask(this, permissions, null, null, task);
    }

    /**
     * 执行一个需要权限的任务, 兼容低版本<br/>
     * 检查权限->显示说明->请求权限->回调{@link TActivityProvider.RequestPermissionTask}<br/>
     * 目的任务在{@link TActivityProvider.RequestPermissionTask}中实现, 需要判断权限是否被授予<br/>
     *
     * @param permission 所需权限 android.Manifest.permission....
     * @param rationaleTitle 权限说明标题(标题和内容都送空, 则不提示)
     * @param rationaleContent 权限说明内容(标题和内容都送空, 则不提示)
     * @param task 需要权限的任务
     */
    public void executePermissionTask(String permission, String rationaleTitle, String rationaleContent, TActivityProvider.RequestPermissionTask task){
        provider.executePermissionTask(this, new String[]{permission}, rationaleTitle, rationaleContent, task);
    }

    /**
     * 执行一个需要权限的任务, 兼容低版本<br/>
     * 检查权限->显示说明->请求权限->回调{@link TActivityProvider.RequestPermissionTask}<br/>
     * 目的任务在{@link TActivityProvider.RequestPermissionTask}中实现, 需要判断权限是否被授予<br/>
     *
     * @param permissions 所需权限 android.Manifest.permission....
     * @param rationaleTitle 权限说明标题(标题和内容都送空, 则不提示)
     * @param rationaleContent 权限说明内容(标题和内容都送空, 则不提示)
     * @param task 需要权限的任务
     */
    public void executePermissionTask(String[] permissions, String rationaleTitle, String rationaleContent, TActivityProvider.RequestPermissionTask task){
        provider.executePermissionTask(this, permissions, rationaleTitle, rationaleContent, task);
    }

    /**
     * 原生权限请求结果回调方法<p/>
     *
     * 已被改造, 若采用原生方法获取权限, 请复写{@link TActivity#onRequestPermissionsResult(int, String[], int[], boolean)}<p/>
     */
    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        provider.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    /**
     * 权限请求结果回调方法<p/>
     *
     * 仅用于原生方法获取权限. 若使用{@link TActivity#executePermissionTask}请求权限,
     * 无需复写此方法, 程序会回调{@link TActivityProvider.RequestPermissionTask}处理.<br/>
     *
     * @param requestCode 请求码
     * @param permissions 权限列表 android.Manifest.permission....
     * @param grantResults 结果列表
     * @param allGranted 是否所有请求的权限都被允许
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, boolean allGranted) {

    }

}
