package sviolet.turquoise.io.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URLConnection;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import sviolet.turquoise.app.CommonException;
import sviolet.turquoise.io.TTask;
import sviolet.turquoise.utils.conversion.BinaryUtils;

/**
 * 简易Http/HttpsURLConnection请求任务(通常由SimpleHttpURLConnectionClient创建并管理)<br>
 * <br>
 * http/https由URL中的前缀决定<br>
 * 
 * @author S.Violet
 *
 */

public class HttpURLConnectionTask extends TTask {
	
	//请求类型////////////////////////////////////////
	
	//第0位:post/get
	public static final int TYPE_GET = 0x00000000;
	public static final int TYPE_POST = 0x00000001;
	
	//报错信息////////////////////////////////////////
	
	//无错误
	public static final int ERROR_CODE_NULL = 0;
	//网络错误
	public static final int ERROR_CODE_UNKNOWN_HOST_EXCEPTION = 101;//域名未找到(可能断网)
	public static final int ERROR_CODE_MALFORMED_URL_EXCEPTION = 102;//URL格式错误
	public static final int ERROR_CODE_SSL_HANDSHAKE_EXCEPTION = 103;//SSL握手失败(https证书错误)
	public static final int ERROR_CODE_PROTOCOL_EXCEPTION = 104;//协议错误
	public static final int ERROR_CODE_SSL_EXCEPTION = 105;//SSL其他错误
	public static final int ERROR_CODE_SOCKET_TIMEOUT_EXCEPTION = 106;//网络超时
	public static final int ERROR_CODE_OTHER_EXCEPTION = 199;//其他IO错误
	
	//变量////////////////////////////////////////////
	
	//绑定的client, 用于回调
	private HttpURLConnectionClient client;
	
	private int type;//网络请求类型
	private String url;//网络请求链接
	private Object request;//请求数据
	private HttpURLConnectionResponse response;//结果回调
	
	private URLConnection connection;
	private OutputStream outputStream;
	private InputStream inputStream;
	
	private int errorCode = ERROR_CODE_NULL;//错误码
	private Exception exception = null;//异常实例
	private int responseCode = 0;//Http返回码
	
	/**
	 * @param client 绑定client对象
	 * @param type 网络请求类型
	 * @param url 网络请求链接
	 * @param request 请求数据
	 * @param timeout 超时时间
	 * @param response 响应回调
	 */
	public HttpURLConnectionTask(HttpURLConnectionClient client, int type, String url, Object request, long timeout, HttpURLConnectionResponse response){
		this.type = type;
		this.url = url;
		this.request = request;
		this.response = response;
		setClient(client);
		if(timeout > 0)
			setTimeOut(timeout + 5);
	}
	
	/******************************************************************
	 *  实现
	 */
	
	/**
	 * 网络请求前的UI处理
	 * @param params
	 */
	@Override
	public void onPreExecute(Object params) {
		if(response != null)
			response.onPre(request);
	}

	/**
	 * 网络请求过程(子线程)
	 * @param params
	 * @return
	 */
	@Override
	public Object doInBackground(Object params) {
		//未绑定client
		if(getClient() == null){
			throw new CommonException("[HttpURLConnectionTask]HttpUrlConnectionClient can't be null");
		}
		//网络请求
		try {
			return connect();
		} catch (SocketTimeoutException e) {
			errorCode = ERROR_CODE_SOCKET_TIMEOUT_EXCEPTION;
			exception = e;
		} catch (MalformedURLException e) {//URL异常
			errorCode = ERROR_CODE_MALFORMED_URL_EXCEPTION;
			exception = e;
		} catch (ProtocolException e) {//协议错误
			errorCode = ERROR_CODE_PROTOCOL_EXCEPTION;
			exception = e;
		} catch (UnknownHostException e) {//域名找不到(可能断网)
			errorCode = ERROR_CODE_UNKNOWN_HOST_EXCEPTION;
			exception = e;
		} catch (SSLHandshakeException e){//SSL握手失败(证书错误)
			errorCode = ERROR_CODE_SSL_HANDSHAKE_EXCEPTION;
			exception = e;
		} catch (SSLException e){//其他SSL错误
			errorCode = ERROR_CODE_SSL_EXCEPTION;
			exception = e;
		} catch (IOException e){//其他IO错误
			errorCode = ERROR_CODE_OTHER_EXCEPTION;
			exception = e;
		}
		return null;
	}

	/**
	 * 网络请求后的 数据/UI处理
	 * @param result
	 */
	@Override
	public void onPostExecute(Object result) {
		//无response实例, 不回调
		if(response == null)
			return;
		
		//回调结果
		if(getState() >= TTask.STATE_CANCELING){//任务中止
			response.onCancel();
		}else if(errorCode == ERROR_CODE_NULL){//无错误
			if(responseCode == HttpURLConnection.HTTP_OK)//网络请求成功
				response.onReceived(result);
			else//网络返回码异常
				response.onResponseCodeError(responseCode);
		}else{//网络错误
			response.onException(errorCode, exception);
		}
		
		//回调onPost
		response.onPost();
	}

	/**
	 * 网络请求中断处理[复写]<br>
	 * <br>
	 * 复写此方法实现网络中断<br>
	 */
	@Override
	public void onCancel() {
		//父类中断
		super.onCancel();
		//中止网络请求
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(connection != null)
					((HttpURLConnection)connection).disconnect();
				try {
					if (outputStream != null)
						outputStream.close();
				} catch (IOException e) {}
				try {
					if (inputStream != null)
						inputStream.close();
				} catch (IOException e) {}
			}
		}).start();
	}
	
	/******************************************************************
	 *  input/output [复写]<br>
	 *  <br>
	 *  默认为String类型的输入输出(BufferedReader/OutputStreamWriter)<br>
	 *  <br>
	 *  复写下列方法, 即可修改输入输出的方法<br>
	 */
	
	/**
	 * [复写]实现把InputStream封装为需要的输入流<br>
	 * <br>
	 * 父类代码:<br>
	 * return new BufferedReader(new InputStreamReader(inputStream));//获取输入流<br>
	 * 
	 * @param inputStream
	 * @return
	 */
	public Object initInput(InputStream inputStream){
		return new BufferedReader(new InputStreamReader(inputStream));//获取输入流
	}
	
	/**
	 * [复写]实现把OutputStream封装为需要的输出流<br>
	 * <br>
	 * 父类代码:<br>
	 * return new OutputStreamWriter(outputStream);//获取输出流<br>
	 * 
	 * @param outputStream
	 * @return
	 */
	public Object initOutput(OutputStream outputStream){
		return new OutputStreamWriter(outputStream);//获取输出流
	}
	
	/**
	 * [复写]实现从input中读取数据并返回<br>
	 * <br>
	 * 只需要实现读取流程, 无需判断返回状态, 无需关闭流<br>
	 * <br>
	 * 父类代码:<br>
	 * BufferedReader inputReader = (BufferedReader)input;<br>
	 * String result = null;<br>
	 * StringBuilder stringBuilder = new StringBuilder();<br>
	 * String temp = inputReader.readLine();<br>
	 * while (temp != null) { <br>
	 *     stringBuilder.append(temp);<br>
	 *     stringBuilder.append("\n");<br>
	 *     temp = inputReader.readLine();<br>
	 * }<br>
	 * result =  stringBuilder.toString();<br>
	 * stringBuilder.setLength(0);<br>
	 * return result;<br>
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public Object input(Object input) throws IOException{
		BufferedReader inputReader = (BufferedReader)input;
		
		String result = null;
		StringBuilder stringBuilder = new StringBuilder();
		
        String temp = inputReader.readLine();
        while (temp != null) { 
            stringBuilder.append(temp);
            stringBuilder.append("\n");
            temp = inputReader.readLine();
        }
        
        result =  stringBuilder.toString();
        stringBuilder.setLength(0);
        
        return result;
	}
	
	/**
	 * [复写]实现把request通过output写出的过程<br>
	 * <br>
	 * 只需要实现写出流程, 无需判断返回状态, 无需关闭流<br>
	 * <br>
	 * 父类代码:<br>
	 * OutputStreamWriter outputWriter = (OutputStreamWriter)output;<br>
	 * String requestString = (String)request;<br>
	 * outputWriter.write(requestString);<br>
	 * 
	 * @param output
	 * @param request
	 * @throws IOException
	 */
	public void output(Object output, Object request) throws IOException{
		OutputStreamWriter outputWriter = (OutputStreamWriter)output;
		String requestString = (String)request;
		
		outputWriter.write(requestString); 
	}
	
	/**
	 * [复写]实现把input(输入流)关闭的过程<br>
	 * <br>
	 * 父类代码:<br>
	 * ((BufferedReader)input).close();<br>
	 * 
	 * @param input
	 * @throws IOException
	 */
	public void closeInput(Object input) throws IOException{
		((BufferedReader)input).close();
	}
	
	/**
	 * [复写]实现把output(输出流)flush并关闭的过程<br>
	 * <br>
	 * 父类代码:<br>
	 * ((OutputStreamWriter)output).flush();<br>
	 * ((OutputStreamWriter)output).close();<br>
	 * 
	 * @param output 输出流
	 * @throws IOException
	 */
	public void closeOutput(Object output) throws IOException{
		((OutputStreamWriter)output).flush(); 
		((OutputStreamWriter)output).close();
	}
	
	/******************************************************************
	 *  public
	 */
	
	/**
	 * Post默认请求报文, 当request==null时使用该报文, 默认""(建议复写SimpleHttpURLConnectionClient中的同名方法)<br>
	 * <br>
	 * 若client != null则该方法会调用client中的同名方法获取默认请求报文<br>
	 * 
	 * @return
	 */
	public Object getDefaultRequest(){
		if(client != null)
			return client.getDefaultRequest();
		else
			return "";
	}
	
	/**
	 * 是否为POST
	 * @return
	 */
	public boolean isPost(){
		return BinaryUtils.getFlag(type, 0);
	}
	
	/**
	 * 绑定SimpleHttpURLConnectionClient
	 * @param client
	 */
	public void setClient(HttpURLConnectionClient client){
		this.client = client;
	}
	
	/**
	 * 取出绑定的SimpleHttpURLConnectionClient
	 * @return
	 */
	public HttpURLConnectionClient getClient(){
		return client;
	}
	
	/**********************************************************
	 * private
	 */
	
	/**
	 * 请求<br>
	 * <br>
	 * 分发GET/POST<br>
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws UnknownHostException
	 * @throws SSLHandshakeException
	 * @throws SSLException
	 * @throws IOException
	 */
	private Object connect() 
			throws MalformedURLException, ProtocolException, UnknownHostException, SSLHandshakeException, SSLException, IOException{
		
		switch (type) {
		case TYPE_GET:
			return get(url, response);
		case TYPE_POST:
			return post(url, request, response);
		default:
			break;
		}
		
		throw new CommonException("[HttpURLConnectionTask]undefined http type (get/post)");
	}
	
	/**
	 * GET请求<br>
	 * 
	 * @param url
	 * @param response
	 * @return
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws UnknownHostException
	 * @throws SSLHandshakeException
	 * @throws SSLException
	 * @throws IOException
	 */
	private Object get(String url, HttpURLConnectionResponse response) 
			throws MalformedURLException, ProtocolException, UnknownHostException, SSLHandshakeException, SSLException, IOException{
		
		connection = getClient().initURLConnection(url, type);//初始化connection
		connection.connect();//启动链接(可不调用, getInputStream会调用)
		inputStream = connection.getInputStream();//获取输入流
		responseCode = ((HttpURLConnection)connection).getResponseCode();//获取返回码
		return receiveData(inputStream, connection);//读取数据
	}
	
	/**
	 * POST请求
	 * 
	 * @param url
	 * @param request
	 * @param response
	 * @return
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws UnknownHostException
	 * @throws SSLHandshakeException
	 * @throws SSLException
	 * @throws IOException
	 */
	private Object post(String url, Object request, HttpURLConnectionResponse response) 
			throws MalformedURLException, ProtocolException, UnknownHostException, SSLHandshakeException, SSLException, IOException{
		
		connection = getClient().initURLConnection(url, type);//初始化connection
		connection.connect();//启动链接(可不调用, getInputStream会调用)
		outputStream = connection.getOutputStream();
		sendData(outputStream, request);//发送请求数据
		inputStream = connection.getInputStream();//获取输入流
		responseCode = ((HttpURLConnection)connection).getResponseCode();//获取返回码
		return receiveData(inputStream, connection);//读取数据
	}
	
	/**
	 * 发送请求数据
	 * 
	 * @param outputStream
	 * @param request
	 * @throws IOException
	 */
	private void sendData(OutputStream outputStream, Object request) throws IOException{
		Object output = initOutput(outputStream);
		if(request == null)
			request = getDefaultRequest();
		if (getState() < TTask.STATE_CANCELING && request != null)
			output(output, request);
		closeOutput(output);
	}
	
	/**
	 * 读取数据
	 * 
	 * @param inputStream 输入流
	 * @param connection
	 * @return
	 * @throws IOException
	 */
	private Object receiveData(InputStream inputStream, URLConnection connection) throws IOException{
		Object input = initInput(inputStream);
		Object result = null;
		if (getState() < TTask.STATE_CANCELING && HttpURLConnection.HTTP_OK == responseCode){
			//持有会话
			client.setSession(connection);
			//读取数据
			result = input(input);
		}
		closeInput(input);
		return result;
	}

}
