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

import android.app.Activity;
import android.os.*;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;

import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.app.utils.InjectUtils;
import sviolet.turquoise.enhance.app.utils.RuntimePermissionManager;
import sviolet.turquoise.utilx.lifecycle.LifeCycleUtils;
import sviolet.turquoise.utilx.lifecycle.listener.LifeCycle;
import sviolet.turquoise.utilx.tlogger.TLogger;

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

public class TActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private final TActivityProvider provider = new TActivityProvider();
    private RuntimePermissionManager runtimePermissionManager = new RuntimePermissionManager(this);

    private int contentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        provider.windowSetting(this);//窗口设置
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
        runtimePermissionManager.onDestroy();
    }

    /**
     * use annotation "@ResourceId()" instead of setContentView()<p/>
     *
     * <pre>{@code
     *      @ResourceId(R.layout.main)
     *      public class TempActivity extends TActivity {
     *          ...
     *      }
     * }</pre>
     *
     * @deprecated use annotation "@ResourceId()" instead of setContentView()
     */
    @Override
    @Deprecated
    public void setContentView(int layoutResID) {
        if (contentId != layoutResID) {
            contentId = layoutResID;
            super.setContentView(layoutResID);
        }else{
            TLogger.get(this).e("[TActivity]please use annotation \"@ResourceId()\" instead of setContentView()");
        }
    }

    /**********************************************
     * public
     */

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
     * 检查权限->显示说明->请求权限->回调{@link RuntimePermissionManager.RequestPermissionTask}<br/>
     * 目的任务在{@link RuntimePermissionManager.RequestPermissionTask}中实现, 需要判断权限是否被授予<br/>
     *
     * @param permission 所需权限 android.Manifest.permission....
     * @param task 需要权限的任务
     */
    public void executePermissionTask(String permission, RuntimePermissionManager.RequestPermissionTask task){
        runtimePermissionManager.executePermissionTask(new String[]{permission}, null, null, task);
    }

    /**
     * 执行一个需要权限的任务, 兼容低版本<br/>
     * 检查权限->显示说明->请求权限->回调{@link RuntimePermissionManager.RequestPermissionTask}<br/>
     * 目的任务在{@link RuntimePermissionManager.RequestPermissionTask}中实现, 需要判断权限是否被授予<br/>
     *
     * @param permissions 所需权限 android.Manifest.permission....
     * @param task 需要权限的任务
     */
    public void executePermissionTask(String[] permissions, RuntimePermissionManager.RequestPermissionTask task){
        runtimePermissionManager.executePermissionTask(permissions, null, null, task);
    }

    /**
     * 执行一个需要权限的任务, 兼容低版本<br/>
     * 检查权限->显示说明->请求权限->回调{@link RuntimePermissionManager.RequestPermissionTask}<br/>
     * 目的任务在{@link RuntimePermissionManager.RequestPermissionTask}中实现, 需要判断权限是否被授予<br/>
     *
     * @param permission 所需权限 android.Manifest.permission....
     * @param rationaleTitle 权限说明标题(标题和内容都送空, 则不提示)
     * @param rationaleContent 权限说明内容(标题和内容都送空, 则不提示)
     * @param task 需要权限的任务
     */
    public void executePermissionTask(String permission, String rationaleTitle, String rationaleContent, RuntimePermissionManager.RequestPermissionTask task){
        runtimePermissionManager.executePermissionTask(new String[]{permission}, rationaleTitle, rationaleContent, task);
    }

    /**
     * 执行一个需要权限的任务, 兼容低版本<br/>
     * 检查权限->显示说明->请求权限->回调{@link RuntimePermissionManager.RequestPermissionTask}<br/>
     * 目的任务在{@link RuntimePermissionManager.RequestPermissionTask}中实现, 需要判断权限是否被授予<p/>
     *
     * 注意:会占用requestCode 201-250 , 建议不要与原生方法requestPermission同时使用.<br/>
     *
     * @param permissions 所需权限 android.Manifest.permission....
     * @param rationaleTitle 权限说明标题(标题和内容都送空, 则不提示)
     * @param rationaleContent 权限说明内容(标题和内容都送空, 则不提示)
     * @param task 需要权限的任务
     */
    public void executePermissionTask(String[] permissions, String rationaleTitle, String rationaleContent, RuntimePermissionManager.RequestPermissionTask task){
        runtimePermissionManager.executePermissionTask(permissions, rationaleTitle, rationaleContent, task);
    }

    /**
     * 原生权限请求结果回调方法, 已被改造<p/>
     */
    @Override
    public final void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        runtimePermissionManager.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
