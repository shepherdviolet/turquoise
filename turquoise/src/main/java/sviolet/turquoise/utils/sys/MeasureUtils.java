package sviolet.turquoise.utils.sys;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Window;

/**
 * 尺寸度量工具<br>
 * <br>
 * px (pixels)像素 – 屏幕上实际的像素点单位<br>
 * dip/dp (device independent pixels) 设备独立像素 - 与设备屏幕有关<br>
 * dpi(dot per inch) 屏幕像素密度 - 每英寸多少像素<br>
 * <br>
 * px = dip * (dpi / 160)<br>
 * DisplayMetrics.density = dpi / 160 <br>
 * DisplayMetrics.densityDpi = dpi<br>
 * 
 * @author S.Violet
 *
 */

public class MeasureUtils {

	/**
	 * 获取DisplayMetrics
	 * @param context
	 * @return
	 */
	public static DisplayMetrics getDisplayMetrics(Context context) {
		return context.getResources().getDisplayMetrics();
	}

	/**
	 * 获取设备屏幕densityDpi( dpi )
	 * @param context
	 * @return
	 */
	public static float getScreenDensityDpi(Context context) {
		return getDisplayMetrics(context).densityDpi;
	}

	/**
	 * 获取设备屏幕density( dpi / 160)
	 * @param context
	 * @return
	 */
	public static float getScreenDensity(Context context) {
		return getDisplayMetrics(context).density;
	}

	/**
	 * 获取设备屏幕高度(pixel像素)
	 */
	public static int getScreenHeight(Context context) {
		return getDisplayMetrics(context).heightPixels;
	}

	/**
	 * 获取手机屏幕宽度(pixel像素)
	 */
	public static int getScreenWidth(Context context) {
		return getDisplayMetrics(context).widthPixels;
	}
	
	/**
	 * 获取设备屏幕高度(dp)
	 */
	public static int getScreenHeightDp(Context context) {
		return px2dp(context, getDisplayMetrics(context).heightPixels);
	}

	/**
	 * 获取手机屏幕宽度(dp)
	 */
	public static int getScreenWidthDp(Context context) {
		return px2dp(context, getDisplayMetrics(context).widthPixels);
	}
	
	/**
	 * 获取通知栏/状态栏高度(pixel 像素)
	 * @return
	 */
	public static int getStatusBarHeight(Activity activity) {
		return activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
	}

	/**
	 * dp转px
	 */
	public static int dp2px(Context context, float dp) {
		if(dp == 0)
			return 0;
		return (int) (dp * getScreenDensity(context) + 0.5f);
	}

	/**
	 * px转dp
	 */
	public static int px2dp(Context context, float px) {
		if(px == 0)
			return 0;
		return (int) (px / getScreenDensity(context) + 0.5f);
	}

}
