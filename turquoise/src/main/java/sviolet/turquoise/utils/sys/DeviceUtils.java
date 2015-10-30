package sviolet.turquoise.utils.sys;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

/**
 * 设备级工具<br/>
 * Created by S.Violet on 2015/6/3.
 */
public class DeviceUtils {

    /**
     * 获取设备IMEI号
     */
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
    public static String getMacAddress(Context context) {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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
     * 获取系统SDK版本(例如22)
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

}
