package sviolet.liba.utils;

import android.content.Context;
import android.location.LocationManager;

/**
 * @author SunPeng
 * @create 2014-7-7 下午3:08:20
 */
public class MapUtils {

	private static final double X_PI = 3.14159265358979324 * 3000.0 / 180.0;

	/**
	 * 百度经度转成高德经度
	 * 
	 * @param bd_lat
	 * @param bd_lon
	 * @return
	 */
	public static double bdLat2gdLat(double bd_lat, double bd_lon) {
		double x = bd_lon - 0.0065;
		double y = bd_lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
		return z * Math.sin(theta);
	}

	/**
	 * 百度维度转成高德维度
	 * 
	 * @param bd_lat
	 * @param bd_lon
	 * @return
	 */
	public static double bdLon2gdLon(double bd_lat, double bd_lon) {
		double x = bd_lon - 0.0065;
		double y = bd_lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
		return z * Math.cos(theta);
	}

	/**
	 * 判断GPS是否打开
	 * 
	 * @param context
	 * @return
	 * 
	 * @author S.Violet (zhuqinchao)
	 */
	public static boolean isGpsOpen(Context context){
		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		boolean isGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean isAgps = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if(isGps || isAgps)
			return true;
		
		return false;
	}
}
