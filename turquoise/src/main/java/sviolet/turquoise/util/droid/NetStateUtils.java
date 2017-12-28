/*
 * Copyright (C) 2015-2017 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.turquoise.util.droid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;

/**
 * 网络状态工具
 */
public class NetStateUtils {

	@RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
	public static NetworkInfo getNetworkInfo(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return manager.getActiveNetworkInfo();
	}

	/**
	 * 判断当前是否有网络连接
	 */
    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
	public static boolean isNetworkConnected(Context context) {
		NetworkInfo networkInfo = getNetworkInfo(context);
		return networkInfo != null && networkInfo.isConnected();
	}

	/**
	 * 判断当前网络模式是否为CMWAP
	 */
    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
	public static boolean isCmwap(Context context) {
		NetworkInfo networkInfo = getNetworkInfo(context);
		if (networkInfo != null && networkInfo.isConnected()) {
			return networkInfo.getType() == ConnectivityManager.TYPE_MOBILE && "cmwap".equals(networkInfo.getExtraInfo());
		}
		return false;
	}

	/**
	 * 判断网络类型
     */
    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
	public static NetworkType getNetworkType(Context context) {
		NetworkInfo networkInfo = getNetworkInfo(context);

		if (networkInfo == null || !networkInfo.isConnected()) {
			return NetworkType.NULL;
		}

		if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return NetworkType.WIFI;
		} else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			int networkType = networkInfo.getSubtype();
			switch (networkType) {
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_IDEN:
					return NetworkType.MOBILE_2G;
				case TelephonyManager.NETWORK_TYPE_UMTS:
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
				case TelephonyManager.NETWORK_TYPE_EHRPD:
				case TelephonyManager.NETWORK_TYPE_HSPAP:
				case 17: //NETWORK_TYPE_TD_SCDMA
					return NetworkType.MOBILE_3G;
				case TelephonyManager.NETWORK_TYPE_LTE:
					return NetworkType.MOBILE_4G;
				default:
					String subtypeName = networkInfo.getSubtypeName();
					if ("TD-SCDMA".equalsIgnoreCase(subtypeName) || "WCDMA".equalsIgnoreCase(subtypeName) || "CDMA2000".equalsIgnoreCase(subtypeName)) {
						return NetworkType.MOBILE_3G;
					}
					return NetworkType.UNKNOWN;
			}
		}
		return NetworkType.UNKNOWN;
	}

	public enum NetworkType{
		NULL,
		WIFI,
		MOBILE_2G,
		MOBILE_3G,
		MOBILE_4G,
		UNKNOWN
	}

}
