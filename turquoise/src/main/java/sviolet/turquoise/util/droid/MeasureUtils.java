package sviolet.turquoise.util.droid;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;

import java.lang.reflect.Method;
import java.math.BigDecimal;

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
	 * 获取DisplayMetrics, 不含底部导航栏
	 */
	public static DisplayMetrics getDisplayMetrics(Context context) {
		return context.getResources().getDisplayMetrics();
	}

	/**
	 * 获取真实的DisplayMetrics, 包含底部导航栏, API17以上有效, API17以下不含底部导航栏
     */
	public static DisplayMetrics getRealDisplayMetrics(Activity activity) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return getDisplayMetrics(activity);
		}
		Display display = activity.getWindowManager().getDefaultDisplay();
		DisplayMetrics realDisplayMetrics = new DisplayMetrics();
		display.getRealMetrics(realDisplayMetrics);
		return realDisplayMetrics;
	}

	/**
	 * 获取屏幕densityDpi(dpi)
	 */
	public static float getDensityDpi(Context context) {
		return getDisplayMetrics(context).densityDpi;
	}

	/**
	 * 获取屏幕density(dpi/160)
	 */
	public static float getDensity(Context context) {
		return getDisplayMetrics(context).density;
	}

	/**
	 * 获取屏幕高度(pixel像素), 不含底部导航栏
	 */
	public static int getScreenHeight(Context context) {
		return getDisplayMetrics(context).heightPixels;
	}

	/**
	 * 获取屏幕宽度(pixel像素), 不含底部导航栏
	 */
	public static int getScreenWidth(Context context) {
		return getDisplayMetrics(context).widthPixels;
	}

    /**
     * 获取屏幕高度(pixel像素), 包含底部导航栏, API17以上有效, API17以下不含底部导航栏
     */
    public static int getRealScreenHeight(Activity activity) {
        return getRealDisplayMetrics(activity).heightPixels;
    }

    /**
     * 获取屏幕宽度(pixel像素), 包含底部导航栏, API17以上有效, API17以下不含底部导航栏
     */
    public static int getRealScreenWidth(Activity activity) {
        return getRealDisplayMetrics(activity).widthPixels;
    }
	
	/**
	 * 获取屏幕高度(dp), 不含底部导航栏
	 */
	public static int getScreenHeightDp(Context context) {
		return px2dp(context, getDisplayMetrics(context).heightPixels);
	}

	/**
	 * 获取屏幕宽度(dp), 不含底部导航栏
	 */
	public static int getScreenWidthDp(Context context) {
		return px2dp(context, getDisplayMetrics(context).widthPixels);
	}
	
	/**
	 * 获取通知栏/状态栏高度(pixel 像素)
	 */
	public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
	}

    /**
     * 获取底部导航栏高度(pixel 像素)
     */
    public static int getNavigationBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0 && checkNavigationBar(context)) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * 检查是否有底部导航栏
     * @return true:存在底部导航栏 false:不存在底部导航栏
     */
    public static boolean checkNavigationBar(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && context instanceof Activity) {
            return checkNavigationBarBySize((Activity) context);
        }
        return checkNavigationBarByConfig(context);
    }

    private static boolean checkNavigationBarBySize(Activity activity) {
        DisplayMetrics realDisplayMetrics = getRealDisplayMetrics(activity);
        DisplayMetrics displayMetrics = getDisplayMetrics(activity);
        return realDisplayMetrics.widthPixels > displayMetrics.widthPixels || realDisplayMetrics.heightPixels > displayMetrics.heightPixels;
    }

    private static boolean checkNavigationBarByConfig(Context context) {
        boolean hasNavigationBar = false;
        Resources resources = context.getResources();
        //先判断config_showNavigationBar配置
        int resourceId = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId > 0) {
            hasNavigationBar = resources.getBoolean(resourceId);
        }
        //若存在qemu.hw.mainkeys配置, 则采用qemu.hw.mainkeys配置覆盖结果
        try {
            Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method getMethod = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) getMethod.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception ignored) {
        }
        return hasNavigationBar;
    }

    /**
     * 获得物理屏幕对角线尺寸(即几英寸屏)
     * @return 英寸
     */
	public static float getPhysicalScreenSize(Activity activity){
        DisplayMetrics displayMetrics = getRealDisplayMetrics(activity);
        //屏幕宽度(英寸) = 宽度(像素) / xdpi, xdpi是屏幕真实的物理dpi, 与densityDpi不同
        float widthInch = (float)displayMetrics.widthPixels / displayMetrics.xdpi;
        //屏幕高度(英寸) = 高度(像素) / ydpi, ydpi是屏幕真实的物理dpi, 与densityDpi不同
        float heightInch = (float)displayMetrics.heightPixels / displayMetrics.ydpi;
        return new BigDecimal(Math.sqrt(widthInch * widthInch + heightInch * heightInch)).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
	}

	/**
	 * dp转px
	 */
	public static int dp2px(Context context, float dp) {
		if(dp == 0) {
            return 0;
        }
		return (int) (dp * getDensity(context) + 0.5f);
	}

	/**
	 * px转dp
	 */
	public static int px2dp(Context context, float px) {
		if(px == 0) {
            return 0;
        }
		return (int) (px / getDensity(context) + 0.5f);
	}

}
