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

package sviolet.turquoise.utils.bitmap.loader.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import sviolet.turquoise.utils.bitmap.BitmapUtils;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoaderMessenger;

/**
 * 网络加载处理器默认实现<p/>
 *
 * 实现根据url地址, 通过HTTP/GET方式, 从网络下载图片的过程, 实现任务取消时, 强制终止网络加载的功能(可选)<p/>
 *
 * 设置网络超时时间:<Br/>
 * <pre>{@code
 *      //连接超时5s, 读取超时20s
 *      bitmapLoader.setNetLoadHandler(new DefaultNetLoadHandler(5000, 20000, true))
 * }</pre>
 * <br/>
 * 加载任务取消时, 不强制终止网络加载:<br/>
 * <pre>{@code
 *      //加载任务取消时, 不强制终止网络加载
 *      bitmapLoader.setNetLoadHandler(new DefaultNetLoadHandler(10000, 30000, false))
 * }</pre>
 *
 * Created by S.Violet on 2015/10/12.
 */
public class DefaultNetLoadHandler implements NetLoadHandler {

    private int connectTimeout;
    private int readTimeout;
    private boolean forceCancel;

    /**
     * 连接超时10s, 读取超时30s, 取消任务时,强制终止网络加载
     */
    public DefaultNetLoadHandler(){
        this(10000, 30000, true);
    }

    /**
     * @param connectTimeout 网络连接超时ms
     * @param readTimeout 网络读取超时ms
     * @param forceCancel 取消任务时,强制终止网络加载(默认true)
     */
    public DefaultNetLoadHandler(int connectTimeout, int readTimeout, boolean forceCancel){
        if (connectTimeout <= 0)
            throw new NullPointerException("[DefaultNetLoadHandler] connectTimeout <= 0");
        if (readTimeout <= 0)
            throw new NullPointerException("[DefaultNetLoadHandler] readTimeout <= 0");
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.forceCancel = forceCancel;
    }

    /**
     * 实现网络加载过程
     */
    @Override
    public void loadFromNet(String url, int reqWidth, int reqHeight, final BitmapLoaderMessenger messenger) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        HttpURLConnection conn = null;
        try {
            URL httpUrl = new URL(url);
            conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            conn.setRequestMethod("GET");
            //连接
            inputStream = conn.getInputStream();

            //连接后判断取消状态
            if (messenger.isCancelling()){
                messenger.setResultCanceled();
                return;
            }

            final InputStream finalInputStream = inputStream;

            if (forceCancel) {
                //设置取消监听
                //任务取消时,网络加载强制终止
                messenger.setOnCancelListener(new Runnable() {
                    @Override
                    public void run() {
                        //强制关闭输入流
                        try {
                            finalInputStream.close();
                        } catch (Exception ignored) {
                        }
                        //强制设置结果为取消, 后续将无法改变
                        messenger.setResultCanceled();
                    }
                });
            }

            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while((len = inputStream.read(buffer)) != -1){
                    outputStream.write(buffer, 0, len);
                }
                byte[] data = outputStream.toByteArray();
                if (data == null || data.length <= 0){
                    //设置结果返回[重要]
                    messenger.setResultFailed(new Exception("[DefaultNetLoadHandler]data is null"));
                    return;
                }
                //设置结果返回[重要]
                messenger.setResultSucceed(BitmapUtils.decodeFromByteArray(data, reqWidth, reqHeight));
                return;
            }
        } catch (IOException e) {
            if (messenger.isCancelling()){
                //设置结果返回[重要]
                messenger.setResultCanceled();
            }else {
                //设置结果返回[重要]
                messenger.setResultFailed(e);
            }
            return;
        }finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            if (conn != null)
                conn.disconnect();
        }
        //设置结果返回[重要]
        messenger.setResultFailed(null);
    }

    /**
     * 销毁成员变量
     */
    @Override
    public void onDestroy() {

    }
}
