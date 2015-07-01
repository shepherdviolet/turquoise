package sviolet.turquoise.utils;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * Http工具
 * 
 * @author S.Violet
 *
 */
public class HttpUtils {
	
//	/**
//	 * 打印返回报文头
//	 * @param http
//	 * @throws UnsupportedEncodingException
//	 */
//	public static void printResponseHeader(HttpURLConnection httpConnection) throws UnsupportedEncodingException {
//		Map<String, String> header = getHttpResponseHeader(httpConnection);
//		for (Map.Entry<String, String> entry : header.entrySet()) {
//			String key = entry.getKey() != null ? entry.getKey() + ":" : "";
//			//打印
//		}
//	}

	/**
	 * 根据HttpURLConnection获取返回报文头
	 * @throws UnsupportedEncodingException
	 */
	public static Map<String, String> getHttpResponseHeader(HttpURLConnection httpConnection) throws UnsupportedEncodingException {
		Map<String, String> header = new LinkedHashMap<String, String>();
		for (int i = 0;; i++) {
			String value = httpConnection.getHeaderField(i);
			if (value == null)
				break;
			header.put(httpConnection.getHeaderFieldKey(i), value);
		}
		return header;
	}
}
