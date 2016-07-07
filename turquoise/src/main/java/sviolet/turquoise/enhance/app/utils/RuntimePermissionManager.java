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

package sviolet.turquoise.enhance.app.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import sviolet.turquoise.util.common.CheckUtils;
import sviolet.turquoise.util.droid.MeasureUtils;
import sviolet.turquoise.common.entity.Destroyable;

/**
 * <p>运行时权限管理器, 兼容低版本, 占用Activity的requestCode 201-250</p>
 *
 * <pre>{@code
 *
 *      public class MyActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
 *
 *          private RuntimePermissionManager runtimePermissionManager = new RuntimePermissionManager(this);
 *
 *          private void test(){
 *              //请求权限
 *              executePermissionTask(Manifest.permission.WRITE_EXTERNAL_STORAGE, "Storage permission", "We need to write external storage", new RuntimePermissionManager.RequestPermissionTask() {
 *                  public void onResult(String[] permissions, int[] grantResults, boolean allGranted) {
 *                      if (allGranted) {
 *                          //权限请求成功, 处理事件
 *                      }else{
 *                          //权限请求失败, 终止
 *                      }
 *                  }
 *              });
 *          }
 *
 *          protected void onDestroy() {
 *              super.onDestroy();
 *              //销毁
 *              runtimePermissionManager.onDestroy();
 *          }
 *
 *          public final void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
 *              //拦截结果, 交由RuntimePermissionManager处理
 *              if(!runtimePermissionManager.handleRequestPermissionsResult(requestCode, permissions, grantResults)){
 *                  //未被RuntimePermissionManager处理的结果
 *              }
 *          }
 *
 *      }
 *
 * }</pre>
 *
 * Created by S.Violet on 2016/5/19.
 */
public class RuntimePermissionManager implements Destroyable {

    private WeakReference<Activity> activity;
    //权限请求请求码
    private AtomicInteger mPermissionRequestCode = new AtomicInteger(0);
    //权限请求任务池
    private SparseArray<RequestPermissionTask> mPermissionRequestTaskPool = new SparseArray<>();

    public RuntimePermissionManager(Activity activity){
        this.activity = new WeakReference<>(activity);
    }

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
        void onResult(String[] permissions, int[] grantResults, boolean allGranted);
    }

    /**
     * 销毁管理器
     */
    @Override
    public void onDestroy() {
        activity.clear();
        mPermissionRequestTaskPool.clear();
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
    public void executePermissionTask(final String[] permissions, String rationaleTitle, String rationaleContent, final RequestPermissionTask task){
        final Activity activity = this.activity.get();
        if (activity == null){
            onDestroy();
            return;
        }
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

    private void requestPermissions(Activity activity, final String[] permissions, RequestPermissionTask task) {
        //递减的请求号201-250
        final int requestCode = mPermissionRequestCode.getAndIncrement() % 50 + 201;
        //任务加入任务池
        mPermissionRequestTaskPool.put(requestCode, task);
        //请求权限
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * 处理权限请求结果(拦截{@link Activity#onRequestPermissionsResult(int, String[], int[])})
     * @return true:管理器处理了响应, false:管理器未处理响应, 需要自行处理
     */
    public boolean handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        final Activity activity = this.activity.get();
        if (activity == null){
            onDestroy();
            return false;
        }
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
            //从任务池中移除
            mPermissionRequestTaskPool.remove(requestCode);
            //回调任务
            task.onResult(permissions, grantResults, allGranted);
            return true;
        }
        return false;
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
                textView.setTextColor(0xFF606060);
                textView.setTextSize(16f);
            }
            final int dp15 = MeasureUtils.dp2px(getContext(), 15);
            final int dp10 = MeasureUtils.dp2px(getContext(), 10);
            textView.setPadding(dp15, dp15, dp10, dp10);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            addContentView(textView, params);

        }
    }

}
