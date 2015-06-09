package sviolet.demoa.net;

import org.json.JSONObject;

import sviolet.turquoise.io.net.HttpURLConnectionResponse;

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
	public void onNetException(int errorCode, Exception e) {
		
	}

	@Override
	public void onLogicException(int errorCode, Exception e) {
		super.onLogicException(errorCode, e);
	}

	@Override
	public void onCancel() {
		//取消进度条
		//任务取消后处理
	}
	
}
