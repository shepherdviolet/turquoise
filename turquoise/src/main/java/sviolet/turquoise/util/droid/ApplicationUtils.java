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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * 应用级工具
 * Created by S.Violet on 2015/7/2.
 */
public class ApplicationUtils {

    /**
     * 获得应用版本(versionCode)<br/>
     * 默认返回1
     *
     * @param context context
     */
    public static int getAppVersionCode(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return 1;
    }

    /**
     * 获得应用版本(versionName)<br/>
     * 默认返回"1.0"
     *
     * @param context context
     */
    public static String getAppVersionName(Context context){
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return "1.0";
    }

    /*******************************************************************
     * SETTINGS
     */

    /**
     * 硬件加速<br>
     * <br>
     * Manifest设置硬件加速:<br>
     * <application android:hardwareAccelerated="true">
     * <activity android:hardwareAccelerated="false">
     */

    /**
     * [Window级]启用硬件加速 API11
     *
     * @param window
     */
    @SuppressLint("InlinedApi")
    public static void enableHardwareAccelerated(Window window){
        if (DeviceUtils.getVersionSDK() >= 11) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }
    }

    /**
     * [View级]禁用硬件加速 API11
     *
     * @param view
     */
    @SuppressLint("NewApi")
    public static void disableHardwareAccelerated(View view){
        if(android.os.Build.VERSION.SDK_INT >= 11) {
            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    /**
     * 启用策略检测(调试用)
     */
    @SuppressLint("NewApi")
    public static void enableStrictMode() {
        if (DeviceUtils.getVersionSDK() >= 9) {
            // 线程策略
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
                    .detectAll() // 发现所有策略的违反行为
                    .penaltyLog(); // 发现违反策略，打印log
            // VM策略
            StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
                    .detectAll() // 发现所有策略的违反行为
                    .penaltyLog(); // 发现违反策略，打印log
            //应用策略
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    /****************************************************************
     * 5.0 Service Intent  must be explitict 异常解决
     * 5.0 Service必须显式调用
     * 通常采用:
     * Intent mIntent = new Intent();
     * mIntent.setAction("XXX.XXX.XXX");//service的action
     * mIntent.setPackage(getPackageName());//service所在应用的包名
     * context.startService(mIntent);
     */

}
