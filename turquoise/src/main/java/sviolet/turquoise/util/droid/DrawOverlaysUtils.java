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

package sviolet.turquoise.util.droid;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * 覆盖层绘制工具(悬浮窗, 即允许在其他APP上方显示的窗口)
 *
 * Created by S.Violet on 2017/9/11.
 */
public class DrawOverlaysUtils {

    /**
     * 判断是否有权限绘制覆盖层, 6.0以上需要用户手动开启权限
     * @param context context
     * @return true:允许
     */
    @RequiresPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    public static boolean canDrawOverlays(@NonNull Context context) {
        //API23以上需要判断权限
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }

    /**
     * 6.0以上系统通过该方法引导用户开启覆盖层权限(打开系统设置界面)
     * @param context context
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void toEnableDrawOverlays(@NonNull Context context){
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * [重要]移除一个叠加层(悬浮窗)
     * @param context context
     * @param contentView 要移除的View
     */
    public static void removeOverlays(@NonNull Context context, View contentView){
        if (contentView == null){
            return;
        }
        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (mWindowManager == null){
            throw new RuntimeException("Can not get WindowManager from context");
        }
        mWindowManager.removeView(contentView);
    }

    /**
     * 添加一个透明的叠加层(悬浮窗)
     * @param context context
     * @param layoutId 要显示的布局ID
     * @param dragViewId 若为有效的资源ID, 则在该ID指向的View范围内, 可以拖动叠加层(这个View必须在viewGroup内, 且为clickable, 可以设置一个), 不需要拖动则设置-1
     * @param consumeTouchEvent true: dragView会消费掉触摸事件, 本身和子View不会再响应触摸事件
     * @param initX 初始位置X
     * @param initY 初始位置Y
     * @return ViewGroup
     */
    @RequiresPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    public static ViewGroup addOverlays(@NonNull Context context, int layoutId, int dragViewId, boolean consumeTouchEvent, int initX, int initY){
        ViewGroup layout = (ViewGroup) LayoutInflater.from(context).inflate(layoutId, null);
        View dragView = layout.findViewById(dragViewId);
        addOverlays(context, layout, dragView, consumeTouchEvent, initX, initY);
        return layout;
    }

    /**
     * 添加一个透明的叠加层(悬浮窗)
     * @param context context
     * @param viewGroup 要显示的ViewGroup, 通常由LayoutInflater.from(context).inflate(layoutId, null)创建
     * @param dragView 若不为空, 则在该View范围内, 可以拖动叠加层(这个View必须在viewGroup内)
     * @param consumeTouchEvent true: dragView会消费掉触摸事件, 本身和子View不会再响应触摸事件
     * @param initX 初始位置X
     * @param initY 初始位置Y
     */
    @RequiresPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    public static void addOverlays(@NonNull Context context, @NonNull View viewGroup, @Nullable final View dragView, boolean consumeTouchEvent, int initX, int initY){
        WindowManager.LayoutParams windowLayoutParams = new WindowManager.LayoutParams();
        windowLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        windowLayoutParams.format = PixelFormat.RGBA_8888;
        windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowLayoutParams.gravity = Gravity.START | Gravity.TOP;
        windowLayoutParams.x = initX;
        windowLayoutParams.y = initY;
        windowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        windowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        addOverlays(context, viewGroup, dragView, consumeTouchEvent, windowLayoutParams);
    }

    /**
     * 添加一个叠加层(悬浮窗)
     * @param context context
     * @param viewGroup 要显示的ViewGroup, 通常由LayoutInflater.from(context).inflate(layoutId, null)创建
     * @param dragView 若不为空, 则在该View范围内, 可以拖动叠加层(这个View必须在viewGroup内)
     * @param consumeTouchEvent true: dragView会消费掉触摸事件, 本身和子View不会再响应触摸事件
     * @param layoutParams 布局参数
     */
    @RequiresPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    public static void addOverlays(@NonNull Context context, @NonNull final View viewGroup, @Nullable final View dragView, final boolean consumeTouchEvent, @NonNull final WindowManager.LayoutParams layoutParams){
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null){
            throw new RuntimeException("Can not get WindowManager from context");
        }
        windowManager.addView(viewGroup, layoutParams);
        viewGroup.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        if (dragView != null){
            /*
             * 拖拽逻辑还可以优化
             */
            dragView.setOnTouchListener(new View.OnTouchListener() {
                private int lastX;
                private int lastY;
                @Override
                @RequiresPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getActionMasked();
                    if (action == MotionEvent.ACTION_DOWN) {
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                    } else if (action == MotionEvent.ACTION_MOVE) {
                        layoutParams.x += (int) event.getRawX() - lastX;
                        layoutParams.y += (int) event.getRawY() - lastY;
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        windowManager.updateViewLayout(viewGroup, layoutParams);
                    }
                    return consumeTouchEvent;
                }
            });
        }
    }

}
