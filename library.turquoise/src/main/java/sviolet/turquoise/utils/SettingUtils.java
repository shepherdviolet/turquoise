package sviolet.turquoise.utils;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * 参数设置工具
 * 
 * @author S.Violet
 *
 */

public class SettingUtils {

	/*******************************************************************
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
	
}
