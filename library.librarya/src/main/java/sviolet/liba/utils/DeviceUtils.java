package sviolet.liba.utils;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebSettings.ZoomDensity;

/**
 * @author SunPeng
 */
public class DeviceUtils {

	public static DisplayMetrics getDisplayMetrics(Context context) {
		return context.getResources().getDisplayMetrics();
	}

	/**
	 * 获取状态栏高度
	 * 
	 * @return
	 */
	public static int getStatusBarHeight(Activity activity) {

		int statusBarHeight = activity.getWindow()
				.findViewById(Window.ID_ANDROID_CONTENT).getTop();

		return statusBarHeight;

	}

	/**
	 * dp转化成px
	 */
	public static int dp2px(Context context, float dp) {
		final float densityDpi = getDensityDpi(context);
		return (int) (dp * (densityDpi / 160) + 0.5f);
	}

	public static int px2dip(Context context, float px) {
		float density = getDensity(context);
		int dip = (int) (px * (density / 1) + 0.5f);
		return dip;
	}

	/**
	 * px转化成dp
	 */
	public static int px2dp(Context context, float px) {
		// final float densityDpi = getDensityDpi(context);
		// return (int) ((px * 160) / densityDpi + 0.5f);
		return px2dip(context, px);
	}

	private static float getDensityDpi(Context context) {
		int densityDpi = getDisplayMetrics(context).densityDpi;
		return densityDpi;
	}

	private static float getDensity(Context context) {
		float density = getDisplayMetrics(context).density;// 密度
		return density;
	}

	/**
	 * 获取手机屏幕尺寸 高度
	 */
	public static int getDisplayMetricsHeight(Context context) {
		return getDisplayMetrics(context).heightPixels;
	}

	/**
	 * 获取手机屏幕尺寸 宽度
	 */
	public static int getDisplayMetricsWidth(Context context) {
		return getDisplayMetrics(context).widthPixels;
	}

	/**
	 * 获取指定Activity的截屏
	 * 
	 * @return Bitmap
	 * */
	@SuppressWarnings("deprecation")
	public static Bitmap getScreenShot(Activity activity) {
		// View是您需要截图的View
		View view = activity.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap b1 = view.getDrawingCache();
		// 获取状态栏高度
		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		// int statusBarHeight = frame.top;
		// 获取屏幕长和高
		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		int height = activity.getWindowManager().getDefaultDisplay()
				.getHeight();
		// 去掉标题栏
		// Bitmap b = Bitmap.createBitmap(b1, 0, 25, 320, 455);
		Bitmap b = Bitmap.createBitmap(b1, 0, 0, width, height);
		view.destroyDrawingCache();
		return b;
	}

	/**
	 * 判断系统是否root
	 * 
	 * @return boolean
	 * */
	public static boolean isRoot() {
		boolean isRoot = false;
		try {
			isRoot = Runtime.getRuntime().exec("su").getOutputStream() != null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isRoot;
	}

	/**
	 * 根据密码控件返回 页面移动高度
	 */
	public static int getMoveHight4View(Context context, View v) {

		int tempHight = v.getHeight();

		int[] location = new int[2];
		v.getLocationInWindow(location);
		int y = location[1];
		int pwKeyBoardHeight = ((getDisplayMetricsWidth(context) * 80 * 5 / 10) / 48); // 键盘高度
		int viewHight = getDisplayMetricsHeight(context) - y;// 输入控件高度

		int temp = viewHight - pwKeyBoardHeight;

		if (temp > tempHight) {
			return 0;
		} else {
			return tempHight - temp;
		}
	}

	/**
	 * 适配webview
	 */
	@SuppressWarnings("deprecation")
	public static void setWebViewZoom(Context context, WebSettings webSettings) {
		float density = getDensity(context);
		if (density == 0.75)
			webSettings.setDefaultZoom(ZoomDensity.CLOSE);
		else if (density == 1.0)
			webSettings.setDefaultZoom(ZoomDensity.MEDIUM);
		else
			webSettings.setDefaultZoom(ZoomDensity.FAR);
	}

}
