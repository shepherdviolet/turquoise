package sviolet.turquoise.utils;

import android.annotation.SuppressLint;
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
	 * [Window级]启用硬件加速
	 * 
	 * @param window
	 */
	@SuppressLint("InlinedApi")
	public static void enableHardwareAccelerated(Window window){
		window.setFlags(
				WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, 
				WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
	}
	
	/**
	 * [View级]禁用硬件加速
	 * 
	 * @param view
	 */
	@SuppressLint("NewApi")
	public static void disableHardwareAccelerated(View view){
		if(android.os.Build.VERSION.SDK_INT >= 11)
			view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}
	
}
