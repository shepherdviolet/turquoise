package sviolet.demoa.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sviolet.turquoise.utils.net.HttpURLConnectionClient;
import sviolet.turquoise.utils.net.HttpURLConnectionResponse;
import sviolet.turquoise.utils.net.HttpURLConnectionTask;

/**
 * [DEMO]
 * 收发JSON报文的HttpURLConnectionTask示例
 * 
 * @author S.Violet
 *
 */

public class JSONHttpURLConnectionTask extends HttpURLConnectionTask {

	public JSONHttpURLConnectionTask(HttpURLConnectionClient client, int type, String url, String request, long timeout, HttpURLConnectionResponse response) {
		super(client, type, url, request, timeout, response);
	}

	/**
	 * 打印日志
	 * @param msg
	 */
	public void printLog(String msg){
		if(msg != null)
			System.out.println(msg);
	}
	
	/******************************************************************
	 * 以下方法复写即可改变IO的方式, 默认为输入输出String
	 */
	
	@Override
	public Object initInput(InputStream inputStream) {
		return super.initInput(inputStream);
	}

	@Override
	public Object initOutput(OutputStream outputStream) {
		return super.initOutput(outputStream);
	}

	@Override
	public Object input(Object input) throws IOException {
		String result = (String) super.input(input);
		if(result != null)
			printLog("[响应报文]" + result);
		else
			printLog("[响应报文] {}" );
		return result;
	}

	@Override
	public void output(Object output, Object request) throws IOException {
		if(request != null)
			printLog("[请求报文]" + (String)request);
		else
			printLog("[请求报文] {}");
		super.output(output, request);
	}

	@Override
	public void closeInput(Object input) throws IOException {
		super.closeInput(input);
	}

	@Override
	public void closeOutput(Object output) throws IOException {
		super.closeOutput(output);
	}
	
}