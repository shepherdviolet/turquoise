/*
 * Copyright (C) 2015-2017 S.Violet
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
import android.content.Intent;
import android.os.*;
import android.view.Menu;

import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.app.mvp.TView;
import sviolet.turquoise.enhance.app.utils.ActivityResultCallbackManager;
import sviolet.turquoise.enhance.app.utils.InjectUtils;
import sviolet.turquoise.enhance.app.utils.RuntimePermissionManager;
import sviolet.turquoise.x.common.tlogger.TLogger;

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

public abstract class TActivity extends Activity implements EnhancedContext, TView, RuntimePermissionManager.OnRequestPermissionsResultCallback {

    private final TActivityProvider provider = new TActivityProvider();
    private RuntimePermissionManager runtimePermissionManager = new RuntimePermissionManager(this);
    private ActivityResultCallbackManager activityResultCallbackManager = new ActivityResultCallbackManager();

    private int contentId;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        provider.windowSetting(this);//窗口设置
        beforeCreate();
        super.onCreate(savedInstanceState);
        provider.onCreate(this);
        if (savedInstanceState == null) {
            onInitFragments();
        } else {
            onRelaunchFragments(savedInstanceState);
        }
        onCreateEnhanced(savedInstanceState);
        onInitViews(savedInstanceState);
    }

    protected void beforeCreate(){

    }

    /**
     * <p>[Can Override]</p>
     * <p>初始化Fragment, 该方法只在Activity初次创建时调用, 重建(屏幕旋转/长时间后重开)时不会调用该方法</p>
     *
     * <p>用于一般的Fragment用法, 防止Fragment在旋转屏幕时不停的创建实例并重叠显示,
     * ViewPager+FragmentPagerAdapter的场合不需要这样处理, FragmentPagerAdapter会管理好Fragment.</p>
     *
     * <pre>{@code
     *      protected void onInitFragments(){
     *          //实例化Fragment
     *          this.aFragment = new AFragment();
     *          this.bFragment = new BFragment();
     *          //添加Fragment
     *          FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
     *          transaction.add(R.id.aaa, aFragment, A_FRAGMENT_TAG);
     *          transaction.add(R.id.bbb, bFragment, B_FRAGMENT_TAG);
     *          transaction.commit();
     *      }
     * }</pre>
     */
    protected void onInitFragments(){

    }

    /**
     * <p>[Can Override]</p>
     * <p>重建Fragment, 该方法只在Activity重建(屏幕旋转/长时间后重开)时调用, Activity初次创建时不调用该方法</p>
     *
     * <p>用于一般的Fragment用法, 防止Fragment在旋转屏幕时不停的创建实例并重叠显示,
     * ViewPager+FragmentPagerAdapter的场合不需要这样处理, FragmentPagerAdapter会管理好Fragment.</p>
     *
     * <pre>{@code
     *      protected void onRelaunchFragments(Bundle savedInstanceState) {
     *          //根据TAG取回Fragment(这些Fragment是由系统自动重建的)
     *          this.aFragment = getSupportFragmentManager().findFragmentByTag(A_FRAGMENT_TAG);
     *          this.bFragment = getSupportFragmentManager().findFragmentByTag(B_FRAGMENT_TAG);
     *      }
     * }</pre>
     */
    protected void onRelaunchFragments(Bundle savedInstanceState){

    }

    /**
     * <p>[Can Override]</p>
     * 初始化View, 该方法在Activity初次创建时, 和重建(屏幕旋转/长时间后重开)时, 都会调用
     */
    protected abstract void onInitViews(Bundle savedInstanceState);

    /**
     * <p>[Can Override]</p>
     * 等同于原生的onCreate
     */
    protected void onCreateEnhanced(Bundle savedInstanceState){

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
    protected final void onDestroy() {
        beforeDestroy();
        super.onDestroy();
        provider.onDestroy(this);
        runtimePermissionManager.onDestroy();
        activityResultCallbackManager.onDestroy();
        onDestroyEnhanced();
        afterDestroy();
    }

    protected void beforeDestroy(){

    }

    /**
     * <p>[Can Override]</p>
     * 等同于onDestroy方法, 监听销毁的生命周期事件
     */
    protected void afterDestroy(){

    }

    /**
     * <p>[Can Override]</p>
     * 等同于onDestroy方法, 监听销毁的生命周期事件
     */
    protected void onDestroyEnhanced(){

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

    @Override
    public void onPresenterRefresh(int code) {

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
    @Override
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
    @Override
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
    @Override
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
    @Override
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

    /**********************************************
     * Public
     *
     * ActivityResult callback
     */

    /**
     * 注册一个回调并返回一个请求号, 将原先需要重写Activity.onActivityResult处理的事件转为回调形式
     *
     * <pre>{@code
     *      startActivityForResult(new Intent(SourceActivity.this, TargetActivity.class), registerActivityResultCallback(new ActivityResultCallbackManager.OnActivityResultCallback{
     *          public void onActivityResult(int requestCode, int resultCode, Intent data){
     *              ......
     *          }
     *      }));
     * }</pre>
     *
     * @param callback 回调
     * @return 回调对应的请求号
     */
    public int registerActivityResultCallback(ActivityResultCallbackManager.OnActivityResultCallback callback) {
        return activityResultCallbackManager.register(callback);
    }

    /**
     * onActivityResult先交由activityResultCallbackManager处理
     */
    @Override
    protected final void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!activityResultCallbackManager.handleActivityResult(requestCode, resultCode, data)){
            onActivityResultEnhanced(requestCode, resultCode, data);
        }
    }

    /**
     * <p>[Can Override]</p>
     * 等同于原生复写onActivityResult方法. 当内置的ActivityResultCallbackManager未能处理onActivityResult时回调此方法
     * @param requestCode requestCode
     * @param resultCode resultCode
     * @param data data
     */
    protected void onActivityResultEnhanced(int requestCode, int resultCode, Intent data){

    }

}
