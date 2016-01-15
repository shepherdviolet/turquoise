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

package sviolet.turquoise.model.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import sviolet.turquoise.model.queue.taskqueue.TQueue;

/**
 * 简易Http/HttpsURLConnection请求会话实例<br>
 * <br>
 * 通过get、post方法发起网络请求。<br>
 * 队列方式执行网络请求, 允许并发, 设置优先级等。<br>
 * 一个client维持一个session会话, 所有由此Client发起的请求, 会保持上一个session。<br>
 * 继承并复写本类, 可以实现统一的connection配置。<br>
 * 
 * @author S.Violet
 *
 */

public class HttpURLConnectionClient {
	
	//static//////////////////////////////////

	private static final String DEFAULT_KEY_PREFIX = "DEFAULT_KEY_PREFIX";//默认标签前缀
	private static final int DEFAULT_TIMEOUT = 30000;//网络超时时间(默认)
	
	//var//////////////////////////////////
	
	private TQueue queue;//请求队列
	private HttpSessionKeeper httpSessionKeeper = new HttpSessionKeeper();//会话管理器

	private int defaultKeyIndex = 0;//默认标签编号
	
	//setting//////////////////////////////////
	
	private String urlPrefix = "";//URL前缀
	private int timeout = DEFAULT_TIMEOUT;//网络超时时间
	private Proxy proxy;//设置的代理
	
	/***************************************************************************
	 * 构造
	 */
	
	/**
     * @param reverse false:顺序队列,先进先执行  true:逆序队列,后进先执行
	 * @param concurrencyVolumeMax 最大并发量
	 */
	public HttpURLConnectionClient(boolean reverse, int concurrencyVolumeMax){
		queue = new TQueue(reverse, concurrencyVolumeMax);
	}
	
	/**
     * @param reverse false:顺序队列,先进先执行  true:逆序队列,后进先执行
	 * @param concurrencyVolumeMax 最大并发量
	 * @param timeout 网络超时
	 */
	public HttpURLConnectionClient(boolean reverse, int concurrencyVolumeMax, int timeout){
		queue = new TQueue(reverse, concurrencyVolumeMax);
		this.timeout = timeout;
	}
	
	/***************************************************************************
	 *  FUNC
	 */
	
	/**
	 * 发起GET请求(优先级0)
	 * @param url
	 * @param response
	 */
	public HttpURLConnectionTask get(String url, HttpURLConnectionResponse response){
		return get(url, DEFAULT_KEY_PREFIX + defaultKeyIndex++, response);
	}
	
	/**
	 * 发起POST请求(优先级0)
	 * @param url
	 * @param request
	 * @param response
	 */
	public HttpURLConnectionTask post(String url, String request, HttpURLConnectionResponse response){
		return post(url, DEFAULT_KEY_PREFIX + defaultKeyIndex++, request, response);
	}
	
	/**
	 * 发起GET请求
	 * @param url
	 * @param key 任务标签
	 * @param response
	 */
	public HttpURLConnectionTask get(String url, String key, HttpURLConnectionResponse response){
		HttpURLConnectionTask task = initConnectionTask(this, HttpURLConnectionTask.TYPE_GET, url, null, timeout, response);
		queue.put(key, task);
		return task;
	}

	/**
	 * 发起POST请求
	 * @param url
	 * @param key 任务标签
	 * @param request
	 * @param response
	 */
	public HttpURLConnectionTask post(String url, String key, String request, HttpURLConnectionResponse response){
		HttpURLConnectionTask task = initConnectionTask(this, HttpURLConnectionTask.TYPE_POST, url, request, timeout, response);
		queue.put(key, task);
		return task;
	}
	
	/**
	 * 取消全部任务
	 */
	public void cancelAll(){
		if(queue != null)
			queue.cancelAll();
	}
	
	/***************************************************************************
	 * [复写]
	 */
	
	/**
	 * 初始化URL实例
	 * 
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 */
	public URL initURL(String url) throws MalformedURLException{
		return new URL(urlPrefix + url);
	}
	
	/**
	 * 初始化SimpleHttpURLConnectionTask实例<br>
	 * <br>
	 * 使用自定义的task(继承自SimpleHttpURLConnectionTask), 复写此方法<br>
	 * 
	 * @param client
	 * @param type
	 * @param url
	 * @param request
	 * @param timeout
	 * @param response
	 * @return
	 */
	public HttpURLConnectionTask initConnectionTask(HttpURLConnectionClient client, int type, String url, String request, long timeout, HttpURLConnectionResponse response){
		return new HttpURLConnectionTask(client, type, url, request, timeout, response);
	}
	
	/**
	 * 创建并初始化一个UrlConnection(通常由队列中的任务自动调用)
	 * 
	 * @param url
	 * @param type
	 * @return
	 * @throws MalformedURLException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	public URLConnection initURLConnection(String url, int type) throws MalformedURLException, ProtocolException, IOException{
		URL urlInstance = initURL(url);
		Proxy proxy = initProxy();
		
		URLConnection urlConnection;
		if(proxy == null)
			urlConnection = urlInstance.openConnection();
		else
			urlConnection = urlInstance.openConnection(proxy);
		
		httpSessionKeeper.wrapSession(urlConnection);
		onConnectionSetting((HttpURLConnection) urlConnection, type);
		
		return urlConnection;
	}
	
	/**
	 * UrlConnection配置[复写]<br>
	 * <br>
	 * 复写该方法进行connection配置<br>
	 * <br>
	 * 设置参考<br>
	 * urlConnection.setUseCaches(false);//禁用缓存<br>
	 * urlConnection.setRequestProperty("HTTP-Version", "HTTP/1.1");//HTTP版本<br>
	 * urlConnection.setRequestProperty("User-Agent",  System.getProperty("http.agent"));//UserAgent<br>
	 * urlConnection.setRequestProperty("Accept", "text/xml, text/plain");//XML返回报文<br>
	 * urlConnection.setRequestProperty("Accept", "application/json, text/plain");//JSON返回报文<br>
	 * urlConnection.setRequestProperty("Accept-Language", Locale.getDefault().toString());//返回报文语言<br>
	 * urlConnection.setRequestProperty("Accept-Charset", "UTF-8");//返回报文编码<br>
	 * urlConnection.setRequestProperty("Connection", "close");//短连接<br>
	 * urlConnection.setRequestProperty("Connection", "Keep-Alive");//长连接<br>
	 * urlConnection.setRequestProperty("Content-Length", length);//内容长度(可不定义)<br>
	 * urlConnection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");//请求报文格式XML<br>
	 * urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");//请求报文格式JSON<br>
	 * urlConnection.setRequestProperty("Cache-Control", "no-cache");//禁用缓存(和setUseCaches同理?)<br>
	 * urlConnection.setInstanceFollowRedirects(true);//是否重定向<br>
	 * 
	 * @param urlConnection
	 * @param type
	 * @throws ProtocolException
	 * @throws IOException
	 */
	public void onConnectionSetting(HttpURLConnection urlConnection, int type) throws ProtocolException, IOException{
		if(isPostType(type)){
			urlConnection.setRequestMethod("POST");//POST请求
			urlConnection.setDoOutput(true);//允许输出
		}else{
			urlConnection.setRequestMethod("GET");//GET请求
		}
		
//		urlConnection.setConnectTimeout(timeout);//连接超时
//		urlConnection.setReadTimeout(timeout);//传输超时
//		
//		urlConnection.setUseCaches(false);//禁用缓存
//		
//		urlConnection.setRequestProperty("HTTP-Version", "HTTP/1.1");//HTTP版本
//		
//		urlConnection.setRequestProperty("User-Agent",  System.getProperty("http.agent"));//UserAgent
//		
//		urlConnection.setRequestProperty("Accept", "text/xml, text/plain, */*");//XML返回报文
//		urlConnection.setRequestProperty("Accept", "application/json, text/plain, */*");//JSON返回报文
//		urlConnection.setRequestProperty("Accept-Language", Locale.getDefault().toString());//返回报文语言
//		urlConnection.setRequestProperty("Accept-Charset", "UTF-8");//返回报文编码
//		
//		urlConnection.setRequestProperty("Connection", "close");//短连接
//		urlConnection.setRequestProperty("Connection", "Keep-Alive");//长连接
//		
//		urlConnection.setRequestProperty("Content-Length", length);//内容长度(可不定义)
//		urlConnection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");//请求报文格式XML
//		urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");//请求报文格式JSON
//		
//		urlConnection.setRequestProperty("Cache-Control", "no-cache");//禁用缓存(和setUseCaches同理?)
//		
//		urlConnection.setInstanceFollowRedirects(true);//是否重定向
	}
	
	/**
	 * 创建并初始化Proxy[复写]<br>
	 * <br>
	 * 复写此方法设置proxy或调用setProxy设置(不调用父类方法)<br>
	 * @return
	 */
	public Proxy initProxy(){
		if(proxy != null)
			return proxy;
		return null;
	}
	
	/**
	 * Post默认请求报文, 当request==null时使用该报文, 默认""[复写]<br>
	 * <br>
	 * 通常情况下, Task会调用此方法获得默认报文, 复写此方法设置默认报文<br>
	 * 
	 * @return
	 */
	public Object getDefaultRequest(){
		return "";
	}
	
	/*****************************************************************************
	 * GET/SET
	 */
	
	/**
	 * 设置Proxy
	 * @param proxy
	 */
	public void setProxy(Proxy proxy){
		this.proxy = proxy;
	}
	
	/**
	 * 设置URL通用前缀
	 * @param urlPrefix
	 */
	public void setUrlPrefix(String urlPrefix){
		if(urlPrefix != null)
			this.urlPrefix = urlPrefix;
	}
	
	/**
	 * 获取URL通用前缀
	 * @return
	 */
	public String getUrlPrefix(){
		return urlPrefix;
	}
	
	/**
	 * 会话管理器中存入新session
	 * @param urlConnection
	 */
	public void setSession(URLConnection urlConnection){
		httpSessionKeeper.setSession(urlConnection);
	}
	
	/**
	 * 获得队列queue实例
	 * @return
	 */
	public TQueue getQueue(){
		return queue;
	}
	
	/************************************************************
	 * private
	 */
	
	/**
	 * 判断任务是否为Post任务
	 * @param type
	 * @return
	 */
	private boolean isPostType(int type){
		return type == (type | 0x0001);
	}
	
}
