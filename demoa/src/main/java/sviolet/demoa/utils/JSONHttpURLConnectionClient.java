/*
 * Copyright (C) 2015 S.Violet
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

package sviolet.demoa.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import sviolet.turquoise.util.net.HttpURLConnectionClient;
import sviolet.turquoise.util.net.HttpURLConnectionResponse;
import sviolet.turquoise.util.net.HttpURLConnectionTask;

/**
 * [DEMO]
 * 收发JSON报文的HttpURLConnectionClient示例
 * 
 * @author S.Violet
 *
 */
public class JSONHttpURLConnectionClient extends HttpURLConnectionClient{

	public JSONHttpURLConnectionClient(boolean reverse, int concurrencyVolumeMax) {
		super(reverse, concurrencyVolumeMax);
	}

	public JSONHttpURLConnectionClient(boolean reverse, int concurrencyVolumeMax, int timeout) {
		super(reverse, concurrencyVolumeMax, timeout);
	}
	
	/**
	 * POST请求(JSON报文), 优先级0
	 * 
	 * @param url
	 * @param request
	 * @param response
	 */
	public void postJSON(String url, JSONObject request, HttpURLConnectionResponse response) {
		post(url, handleJSONRequest(request), response);
	}
	
	/**
	 * POST请求(JSON报文)
	 * 
	 * @param url
	 * @param key
	 * @param request
	 * @param response
	 */
	public void postJSON(String url, String key, JSONObject request, HttpURLConnectionResponse response) {
		post(url, key, handleJSONRequest(request), response);
	}
	
	/**
	 * 把JSON转换为String报文
	 * 
	 * @param request
	 * @return
	 */
	private String handleJSONRequest(JSONObject request) {
		String requestText = null;
		if(request != null){
			try {
				request.put("_locale", "zh_CN");//_locale字段
				requestText = request.toString(2);//转为String
				requestText = requestText.replaceAll("\\\\/", "/");//处理密码控件加密密文
			} catch (JSONException e) {
			}
		}else{
			
		}
		return requestText;
	}

	/**
	 * 默认请求报文
	 */
	@Override
	public String getDefaultRequest() {
		return "{}";
	}

	/**
	 * 报文头设置
	 */
	@Override
	public void onConnectionSetting(HttpURLConnection urlConnection, int type) throws ProtocolException, IOException {
		super.onConnectionSetting(urlConnection, type);
		urlConnection.setUseCaches(false);
		urlConnection.setRequestProperty("HTTP-Version", "HTTP/1.1");
		urlConnection.setRequestProperty("User-Agent",  System.getProperty("http.agent"));
		urlConnection.setRequestProperty("Accept", "application/json, text/plain, */*");
		urlConnection.setRequestProperty("Accept-Language", Locale.getDefault().toString());
		urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
		urlConnection.setRequestProperty("Connection", "close");
		urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		urlConnection.setRequestProperty("Cache-Control", "no-cache");
		urlConnection.setInstanceFollowRedirects(false);
	}

	/**
	 * 自定义task
	 */
	@Override
	public HttpURLConnectionTask initConnectionTask(HttpURLConnectionClient client, int type, String url, String request, long timeout, HttpURLConnectionResponse response) {
		return new JSONHttpURLConnectionTask(client, type, url, request, timeout, response);
	}
	
}
