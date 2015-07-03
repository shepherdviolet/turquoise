package sviolet.turquoise.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;

/**
 * 应用级工具
 * Created by S.Violet on 2015/7/2.
 */
public class ApplicationUtils {

    /**
     * 应用对应的磁盘缓存路径<br/>
     * 扩展储存存在时, 返回/sdcard/Android/data/<application package>/cache/uniqueName
     * 扩展储存不存在, 返回/data/data/<application package>/cache/uniqueName
     *
     * @param context
     * @param uniqueName 缓存路径下的子目录
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            try {
                cachePath = context.getExternalCacheDir().getPath();
            }catch(NullPointerException ignored){
            }
        }
        if (cachePath == null){
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 获得应用版本<br/>
     * 默认返回1
     *
     * @param context
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return 1;
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
