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

package sviolet.turquoise.utils.net;

/**
 * HttpURLConnectionClient专用响应
 * 
 * @author S.Violet
 *
 */
public abstract class HttpURLConnectionResponse {
	
	/**
	 * 任务执行前(主线程)
	 */
	public abstract void onPre(Object request);
	
	/**
	 * 任务完成后(主线程)
	 */
	public abstract void onPost();
	
	/**
	 * 成功接收到数据
	 * 
	 * @param result
	 */
	public abstract void onReceived(Object result);
	
	/**
	 * HTTP返回码异常
	 * 
	 * @param responseCode
	 */
	public abstract void onResponseCodeError(int responseCode);
	
	/**
	 * 网络异常<br>
	 * <br>
		//网络错误<br>
		public static final int ERROR_CODE_UNKNOWN_HOST_EXCEPTION = 101;//域名未找到(可能断网)<br>
		public static final int ERROR_CODE_MALFORMED_URL_EXCEPTION = 102;//URL格式错误<br>
		public static final int ERROR_CODE_SSL_HANDSHAKE_EXCEPTION = 103;//SSL握手失败(https证书错误)<br>
		public static final int ERROR_CODE_PROTOCOL_EXCEPTION = 104;//协议错误<br>
		public static final int ERROR_CODE_SSL_EXCEPTION = 105;//SSL其他错误<br>
		public static final int ERROR_CODE_OTHER_EXCEPTION = 199;//其他IO错误<br>
	 * 
	 * @param errorCode 异常码
	 * @param e Exception
	 */
	public abstract void onException(int errorCode, Exception e);
	
	/**
	 * 网络请求被取消后调用(超时/手动)
	 */
	public void onCancel(){
		
	}
	
}
