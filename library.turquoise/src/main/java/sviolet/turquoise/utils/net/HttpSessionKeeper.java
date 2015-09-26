package sviolet.turquoise.utils.net;

import java.net.HttpURLConnection;
import java.net.URLConnection;

/**
 * Http会话管理器<br>
 * <br>
 * 用于装入/获取 Http Session<br>
 *
 * @author S.Violet
 *
 */

@SuppressWarnings("deprecation")
public class HttpSessionKeeper {

	private String sessionId = "";

	public HttpSessionKeeper(){

	}

	public HttpSessionKeeper(URLConnection urlConnection){
		setSession(urlConnection);
	}

	/**
	 * 设置sessionId
	 * @param sessionId
	 */
	public void setSession(String sessionId){
		this.sessionId = sessionId;
	}

	/**
	 * 通过URLConnection获取session
	 *
	 * @param urlConnection
	 */
	public void setSession(URLConnection urlConnection){
		if(urlConnection != null){
			String cookies;
			cookies = ((HttpURLConnection)urlConnection).getHeaderField("Set-Cookie" );
			if(cookies != null && cookies.length() > 0)
				sessionId = cookies.substring(0, cookies.indexOf(";"));
		}
	}

	/**
	 * 获取sessionId
	 * @return
	 */
	public String getSessionId(){
		return sessionId;
	}

	/**
	 * 向URLConnection塞入sessionId
	 *
	 * @param urlConnection
	 */
	public void wrapSession(URLConnection urlConnection){
		if(urlConnection != null && !"".equals(sessionId)){
				urlConnection.setRequestProperty("Cookie", sessionId);
		}
	}

}
