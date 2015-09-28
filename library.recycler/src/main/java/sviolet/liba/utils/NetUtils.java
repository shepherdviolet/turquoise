package sviolet.liba.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

/**
 * @author SunPeng
 */
public class NetUtils {

	public static NetworkInfo getNetworkInfo(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return manager.getActiveNetworkInfo();
	}

	/**
	 * 判断当前是否有网络连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkNetwork(Context context) {
		NetworkInfo networkInfo = getNetworkInfo(context);
		if (networkInfo != null && networkInfo.isConnected())
			return true;
		else
			return false;
	}

	/**
	 * 判断当前网络模式是否为CMWAP
	 */
	public static boolean isCmwapNet(Context context) {
		NetworkInfo networkInfo = getNetworkInfo(context);
		if (networkInfo != null && networkInfo.isConnected()) {
			if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE
					&& "cmwap".equals(networkInfo.getExtraInfo()))
				return true;
			else
				return false;
		}
		return false;
	}

	/**
	 * 判断当前网络是否为WIFI
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifi(Context context){
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		State state = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if(state != null && state == State.CONNECTED)
			return true;
		else
			return false;
	}
}
