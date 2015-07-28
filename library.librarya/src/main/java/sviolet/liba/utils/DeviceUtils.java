package sviolet.liba.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;
import android.webkit.WebSettings.ZoomDensity;

import sviolet.turquoise.utils.MeasureUtils;

/**
 * @author SunPeng
 */
public class DeviceUtils {

	public static DisplayMetrics getDisplayMetrics(Context context) {
		return context.getResources().getDisplayMetrics();
	}

	/**
	 * 适配webview
	 */
	@SuppressWarnings("deprecation")
	public static void setWebViewZoom(Context context, WebSettings webSettings) {
		float density = MeasureUtils.getScreenDensity(context);
		if (density == 0.75)
			webSettings.setDefaultZoom(ZoomDensity.CLOSE);
		else if (density == 1.0)
			webSettings.setDefaultZoom(ZoomDensity.MEDIUM);
		else
			webSettings.setDefaultZoom(ZoomDensity.FAR);
	}

}
