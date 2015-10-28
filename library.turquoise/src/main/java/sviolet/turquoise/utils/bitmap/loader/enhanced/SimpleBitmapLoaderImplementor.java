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

package sviolet.turquoise.utils.bitmap.loader.enhanced;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import sviolet.turquoise.utils.bitmap.BitmapUtils;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoaderImplementor;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoaderMessenger;
import sviolet.turquoise.utils.conversion.ByteUtils;
import sviolet.turquoise.utils.crypt.DigestCipher;

/**
 * 简易BitmapLoaderImplementor实现<br/>
 * 1.实现HTTP加载图片<br/>
 * 2.实现sha1缓存key<br/>
 * 3.日志打印方式处理异常<br/>
 *
 * Created by S.Violet on 2015/10/12.
 */
public class SimpleBitmapLoaderImplementor implements BitmapLoaderImplementor {

    private int connectTimeout;
    private int readTimeout;

    /**
     * 连接超时10s, 读取超时30s
     */
    public SimpleBitmapLoaderImplementor(){
        this(10000, 30000);
    }

    /**
     * @param connectTimeout 连接超时ms
     * @param readTimeout 读取超时ms
     */
    public SimpleBitmapLoaderImplementor(int connectTimeout, int readTimeout){
        if (connectTimeout <= 0)
            throw new NullPointerException("[SimpleBitmapLoaderImplementor] connectTimeout <= 0");
        if (readTimeout <= 0)
            throw new NullPointerException("[SimpleBitmapLoaderImplementor] readTimeout <= 0");
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public String getCacheKey(String url) {
        //url->SHA1->hex->key
        return ByteUtils.byteToHex(DigestCipher.digest(url, DigestCipher.TYPE_SHA1));
    }

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

            //设置取消监听
            final InputStream finalInputStream = inputStream;
            messenger.setOnCancelListener(new Runnable() {
                @Override
                public void run() {
                    //强制关闭输入流
                    try {
                        finalInputStream.close();
                    }catch (Exception ignored){}
                    //强制设置结果为取消, 后续将无法改变
                    messenger.setResultCanceled();
                }
            });

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
                    messenger.setResultFailed(new Exception("[SimpleBitmapLoaderImplementor]data is null"));
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

    @Override
    public void onException(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onCacheWriteException(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onDestroy() {

    }
}
