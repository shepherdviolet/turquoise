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

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;

import java.util.Locale;

/**
 * 设备级工具<br/>
 * Created by S.Violet on 2015/6/3.
 */
public class DeviceUtils {

    /**
     * 获取安卓ID, 出厂化/刷机等操作后会变, Android 8 (API 26) 以上不同APP获得的也不同(详见官方文档)
     */
    public static String getAndroidId(Context context){
        return Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取设备IMEI号, Android 10 (API 29) 以上就获取不到了
     * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
     */
    @RequiresPermission("android.permission.READ_PHONE_STATE")
    public static String getIMEI(Context context) {
        if (getVersionSDK() >= 29) {
            // API 29 以上即使通过了权限, 也会抛出异常
            return "";
        }
        final TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = manager.getDeviceId();
        if (imei != null) {
            return imei;
        }
        return "";
    }

    /**
     * 获取设备MAC地址, Android 6 (API 23) 以上就获取不到了(变成02:00:00:00:00:00)<br>
     * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
     */
    @RequiresPermission("android.permission.ACCESS_WIFI_STATE")
    public static String getMacAddress(Context context) {
        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null) {
            return info.getMacAddress();
        }
        return "";
    }

    /**
     * 获取设备型号(机型)
     */
    public static String getModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取系统SDK版本(例如22) <br>
     * Build.VERSION_CODES.XXX <br>
     */
    public static int getVersionSDK() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 获取系统版本(例如5.1.1)
     */
    public static String getVersionRelease() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获得系统地区信息
     */
    public static Locale getLocale(Context context){
        return context.getResources().getConfiguration().locale;
    }

    /**
     * 获得系统语言<Br/>
     * zh：汉语<br/>
     * en：英语<br/>
     */
    public static String getLanguage(Context context){
        return getLocale(context).getLanguage();
    }

    /**
     * 获得系统国家<br/>
     * CN：中国<br/>
     * US：美国<br/>
     */
    public static String getCountry(Context context){
        return getLocale(context).getCountry();
    }

    /**
     * 判断系统语言环境是否为中国汉语(zh-CN)
     */
    public static boolean isLocaleZhCn(Context context){
        return "CN".equals(getCountry(context)) && "zh".equals(getLanguage(context));
    }

    /**
     * 获得该设备应用最大可用内存
     *
     * @return 最大可用内存 byte
     */
    public static int getMemoryClass(Context context){
        return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    }

    /**
     * 判断该设备是否是低内存设备
     *
     * Android 8.1 (API level 27) adds two new hardware-feature constants, FEATURE_RAM_LOW and FEATURE_RAM_NORMAL,
     * to Package Manager. These constants allow you target the distribution of your apps and APK splits to
     * normal- or low-RAM devices.
     * These constants enable the Play store to promote a better user experience by highlighting apps especially
     * well-suited to the capabilities of a given device.
     *
     * NotificationListenerService and ConditionProviderService are not supported on low-ram Android-powered
     * devices that return true whenActivityManager.isLowRamDevice() is called.
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean isLowRamDevice(@NonNull Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return mActivityManager != null && mActivityManager.isLowRamDevice();
    }

    /**
     * @return 获得CPU架构(arm,x86)和参数(32位,64位,v7a,v8a...), API21以上会返回多个值, 第一个是首选值
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String[] getCpuAbis(){
        if (getVersionSDK() >= Build.VERSION_CODES.LOLLIPOP) {
            return Build.SUPPORTED_ABIS;
        } else {
            return new String[]{Build.CPU_ABI};
        }
    }

    /**
     * @return 获得硬件设备制造商
     */
    public static String getProductManufacturer(){
        return Build.MANUFACTURER;
    }

    /**
     * @return 获得设备品牌
     */
    public static String getProductBrand(){
        return Build.BRAND;
    }

    /**
     * @return 获得操作系统版本(编译版本)
     */
    public static String getBuildDisplayId(){
        return Build.DISPLAY;
    }

}
