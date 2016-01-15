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

import org.json.JSONObject;

import sviolet.turquoise.util.net.HttpURLConnectionResponse;

/**
 * [DEMO]
 * 收发JSON报文的HttpURLConnectionResponse示例
 * 
 * @author S.Violet
 *
 */

public abstract class JSONHttpURLConnectionResponse extends HttpURLConnectionResponse {
	
	public abstract void onSuccess(JSONObject result);
	
	/****************************************************
	 * 实现
	 */
	
	@Override
	public void onPre(Object request) {
		//显示进度条
	}

	@Override
	public void onPost() {
		//取消进度条
	}

	@Override
	public void onReceived(Object result) {
		try{
			JSONObject json = new JSONObject((String)result);
			String _RejCode = json.optString("_RejCode");
			if(_RejCode != null && _RejCode.equals("000000"))
				onSuccess(json);
			
		}catch (Exception e){
			
		}
	}

	@Override
	public void onResponseCodeError(int responseCode) {
		
	}

	@Override
	public void onException(int errorCode, Exception e) {
		
	}

	@Override
	public void onCancel() {
		//取消进度条
		//任务取消后处理
	}
	
}
