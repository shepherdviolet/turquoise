/*
 * Copyright (C) 2015-2016 S.Violet
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

package sviolet.turquoise.modelx.thttp;

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
