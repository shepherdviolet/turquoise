package sviolet.liba.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * @author SunPeng
 */
public class ToastUtils {

	public static void showShortToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static void showLongToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
}
