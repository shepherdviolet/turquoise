package sviolet.turquoise.util.sys;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

/**
 * 网络状态工具
 */
public class NetStateUtils {

	public static NetworkInfo getNetworkInfo(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return manager.getActiveNetworkInfo();
	}

	/**
	 * 判断当前是否有网络连接
	 */
	public static boolean checkNetwork(Context context) {
		NetworkInfo networkInfo = getNetworkInfo(context);
		return networkInfo != null && networkInfo.isConnected();
	}

	/**
	 * 判断当前网络模式是否为CMWAP
	 */
	public static boolean isCmwap(Context context) {
		NetworkInfo networkInfo = getNetworkInfo(context);
		if (networkInfo != null && networkInfo.isConnected()) {
			return networkInfo.getType() == ConnectivityManager.TYPE_MOBILE && "cmwap".equals(networkInfo.getExtraInfo());
		}
		return false;
	}

	/**
	 * 判断当前网络是否为WIFI
	 */
	public static boolean isWifi(Context context){
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State state = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		return state != null && state == State.CONNECTED;
	}
}
