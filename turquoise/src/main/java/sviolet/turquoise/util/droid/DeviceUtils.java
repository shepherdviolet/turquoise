package sviolet.turquoise.util.droid;

import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;

import java.util.Locale;

/**
 * 设备级工具<br/>
 * Created by S.Violet on 2015/6/3.
 */
public class DeviceUtils {

    /**
     * 获取设备IMEI号
     * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
     */
    @RequiresPermission("android.permission.READ_PHONE_STATE")
    public static String getIMEI(Context context) {
        final TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = manager.getDeviceId();
        if (imei != null)
            return imei;
        return "";
    }

    /**
     * 获取设备MAC地址<br>
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
     * 获取设备类型
     */
    public static String getModel() {
        String model = android.os.Build.MODEL;
        if (model != null)
            return model;
        return "";
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

}
