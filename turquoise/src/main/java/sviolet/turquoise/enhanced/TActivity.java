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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.*;
import android.os.Process;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;
import sviolet.turquoise.utils.Logger;
import sviolet.turquoise.utils.CheckUtils;
import sviolet.turquoise.utils.sys.DeviceUtils;
import sviolet.turquoise.enhanced.utils.InjectUtils;
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
 * @author S.Violet
 */

public abstract class TActivity extends Activity {

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

    /**********************************************
     * Public
     *
     * Runtime Permission
     */

    //权限请求请求码
    private volatile int mOnRequestPermissionListenerRequestCode = Integer.MAX_VALUE;
    //权限请求结果监听器池
    private Map<Integer, OnRequestPermissionListener> mOnRequestPermissionListenerPool = new HashMap<Integer, OnRequestPermissionListener>();

    /**
     * 权限请求结果监听器
     */
    public interface OnRequestPermissionListener{
        /**
         * @param requestCode 请求码
         * @param permissions 权限列表 android.Manifest.permission....
         * @param grantResults 结果列表
         * @param allGranted 是否所有请求的权限都被允许
         */
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, boolean allGranted);
    }

    /**
     * 检查权限<br/>
     * 支持低版本<Br/>
     *
     * @param permission 权限名 android.Manifest.permission....
     * @return PackageManager.PERMISSION_...
     */
    @Override
    public int checkSelfPermission(String permission) {
        if(permission == null) {
            throw new IllegalArgumentException("permission is null");
        } else {
            return super.checkPermission(permission, android.os.Process.myPid(), Process.myUid());
        }
    }

    /**
     * 是否需要显示权限描述<br/>
     * 支持低版本<br/>
     *
     * @param permission android.Manifest.permission....
     * @return
     */
    @Override
    public boolean shouldShowRequestPermissionRationale(String permission) {
        return DeviceUtils.getVersionSDK() >= 23 && super.shouldShowRequestPermissionRationale(permission);
    }

    /**
     * 请求权限<br/>
     * 支持低版本<br/>
     * 监听器方式,无需复写onRequestPermissionsResult(...)处理结果事件<br/>
     * <br/>
     * 若使用原有requestPermissions(@NonNull String[] permissions, int requestCode)方法,需要注意检查
     * SDK版本,且需要复写onRequestPermissionsResult(...)方法处理结果事件<br/>
     *
     * @param permission 权限 android.Manifest.permission....
     * @param listener 监听器
     */
    public void requestPermissions(String permission, OnRequestPermissionListener listener) {
        requestPermissions(new String[]{permission}, listener);
    }

    /**
     * 请求权限<br/>
     * 支持低版本<br/>
     * 监听器方式,无需复写onRequestPermissionsResult(...)处理结果事件<br/>
     * <br/>
     * 若使用原有requestPermissions(@NonNull String[] permissions, int requestCode)方法,需要注意检查
     * SDK版本,且需要复写onRequestPermissionsResult(...)方法处理结果事件<br/>
     *
     * @param permissions 权限列表 android.Manifest.permission....
     * @param listener 监听器
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(final String[] permissions, OnRequestPermissionListener listener) {

        final int requestCode;
        synchronized (TActivity.class){
            mOnRequestPermissionListenerRequestCode--;
            requestCode = mOnRequestPermissionListenerRequestCode;
        }

        mOnRequestPermissionListenerPool.put(requestCode, listener);

        if(DeviceUtils.getVersionSDK() >= 23) {
            super.requestPermissions(permissions, requestCode);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    int[] grantResults = new int[permissions.length];
                    PackageManager packageManager = getPackageManager();
                    String packageName = getPackageName();
                    int permissionCount = permissions.length;

                    for (int i = 0; i < permissionCount; ++i) {
                        grantResults[i] = packageManager.checkPermission(permissions[i], packageName);
                    }

                    onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            });
        }
    }

    /**
     * 权限请求结果回调方法<br/>
     *
     * @param requestCode 请求码
     * @param permissions 权限列表 android.Manifest.permission....
     * @param grantResults 结果列表
     */
    @Override
    public final void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean allGranted = true;
        if(grantResults == null || grantResults.length <= 0){
            allGranted = false;
        }
        for(int result : grantResults){
            if(result != PackageManager.PERMISSION_GRANTED){
                allGranted = false;
                break;
            }
        }
        OnRequestPermissionListener listener = mOnRequestPermissionListenerPool.get(requestCode);
        if(listener != null) {
            listener.onRequestPermissionsResult(requestCode, permissions, grantResults, allGranted);
            mOnRequestPermissionListenerPool.remove(requestCode);
        }else{
            onRequestPermissionsResult(requestCode, permissions, grantResults, allGranted);
        }
    }

    /**
     * 权限请求结果回调方法<br/>
     * 采用requestPermissions(final String[] permissions, OnRequestPermissionListener listener)请求权限的情况下,
     * 无需复写此方法,onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)会回调监听器.<br/>
     * 采用原生requestPermissions(@NonNull String[] permissions, int requestCode)请求权限的情况下,
     * 需要复写此方法,处理返回结果.<br/>
     *
     * @param requestCode 请求码
     * @param permissions 权限列表 android.Manifest.permission....
     * @param grantResults 结果列表
     * @param allGranted 是否所有请求的权限都被允许
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, boolean allGranted) {

    }

    /**
     * 执行一个需要权限的任务<br/>
     * 检查权限->请求权限->回调监听器<br/>
     * 目的任务在监听器中实现, 需要判断权限是否被授予<br/>
     *
     * @param permission 任务需要的权限
     * @param listener 在监听器中判断权限是否授予, 并执行目的任务
     */
    public void executePermissionTask(final String permission, final OnRequestPermissionListener listener){
        executePermissionTask(permission, null, null, listener);
    }

    /**
     * 执行一个需要权限的任务<br/>
     * 检查权限->显示说明->请求权限->回调监听器<br/>
     * 目的任务在监听器中实现, 需要判断权限是否被授予<br/>
     *
     * @param permission 任务需要的权限
     * @param rationaleTitle 权限说明标题(标题和内容都送空, 则不提示)
     * @param rationaleContent 权限说明内容(标题和内容都送空, 则不提示)
     * @param listener 在监听器中判断权限是否授予, 并执行目的任务
     */
    public void executePermissionTask(final String permission, String rationaleTitle, String rationaleContent, final OnRequestPermissionListener listener){
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            if ((!CheckUtils.isEmpty(rationaleTitle) || !CheckUtils.isEmpty(rationaleContent)) && shouldShowRequestPermissionRationale(permission)) {
                new PermissionRationaleDialog(this, rationaleTitle, rationaleContent, new DialogInterface.OnCancelListener(){
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        requestPermissions(permission, listener);
                    }
                }).show();
            }else{
                requestPermissions(permission, listener);
            }
        }else{
            listener.onRequestPermissionsResult(0, new String[]{permission}, new int[]{PackageManager.PERMISSION_GRANTED}, true);
        }
    }

    /**
     * 权限说明窗口
     */
    private final class PermissionRationaleDialog extends Dialog {
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
