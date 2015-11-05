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

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import sviolet.turquoise.utils.bitmap.BitmapUtils;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoader;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoaderMessenger;

/**
 * 网络加载处理器默认实现<p/>
 *
 * 实现根据url地址, 通过HTTP/GET方式, 从网络下载图片数据的过程, 实现任务取消时, 强制终止网络加载的功能(可选)<p/>
 *
 * 设置网络超时时间:<Br/>
 * <pre>{@code
 *      //连接超时5s, 读取超时20s
 *      bitmapLoader.setNetLoadHandler(new DefaultNetLoadHandler(5000, 20000))
 * }</pre>
 * <br/>
 * 加载任务取消时, 不强制终止网络加载:<br/>
 * <pre>{@code
 *      //加载任务取消时, 不强制终止网络加载
 *      bitmapLoader.setNetLoadHandler(new DefaultNetLoadHandler().setForceCancel(false))
 * }</pre
 * <br/>
 * 设置原图压缩(节省磁盘空间):<br/>
 * <pre>{@code
 *      //加载任务取消时, 不强制终止网络加载
 *      bitmapLoader.setNetLoadHandler(new DefaultNetLoadHandler().setCompress(Bitmap.CompressFormat.JPEG, 70))
 * }</pre><p/>
 *
 * 注意: 在该"网络加载处理器"中特殊处理图片数据, 磁盘缓存将保存改变后的数据, 而非原始数据. 这点与在
 * {@link BitmapDecodeHandler}中进行图片特殊处理不同, NetLoadHandler适合进行较为复杂的图片处理,
 * 因为仅影响网络加载时的效率, 磁盘缓存加载时直接加载处理后的数据, 效率较高<p/>
 *
 * Created by S.Violet on 2015/10/12.
 */
public class DefaultNetLoadHandler implements NetLoadHandler {

    private int connectTimeout;
    private int readTimeout;
    private boolean forceCancel = true;
    private boolean compress = false;
    private int compressWidth = 0;
    private int compressHeight = 0;
    private Bitmap.CompressFormat compressFormat;
    private int compressQuality;

    /**
     * 连接超时10s, 读取超时30s
     */
    public DefaultNetLoadHandler(){
        this(10000, 30000);
    }

    /**
     * @param connectTimeout 网络连接超时ms
     * @param readTimeout 网络读取超时ms
     */
    public DefaultNetLoadHandler(int connectTimeout, int readTimeout){
        if (connectTimeout <= 0)
            throw new NullPointerException("[DefaultNetLoadHandler] connectTimeout <= 0");
        if (readTimeout <= 0)
            throw new NullPointerException("[DefaultNetLoadHandler] readTimeout <= 0");
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * 加载任务被取消时, 网络加载是否强制终止
     * @param forceCancel 默认true, 强制终止
     */
    public DefaultNetLoadHandler setForceCancel(boolean forceCancel){
        this.forceCancel = forceCancel;
        return this;
    }

    /**
     * 设置原图压缩, 减少磁盘缓存占用, 默认不压缩<p/>
     *
     *
     * 宽高根据需求尺寸(reqWidth/reqHeight)适当缩小<br/>
     *
     * @param compressFormat 图片压缩格式
     * @param compressQuality 图片压缩质量
     */
    public DefaultNetLoadHandler setCompress(Bitmap.CompressFormat compressFormat, int compressQuality) {
        return setCompress(compressFormat, compressQuality, 0, 0);
    }

    /**
     * 设置原图压缩, 减少磁盘缓存占用, 默认不压缩<p/>
     *
     * <pre>{@code
     * width>0 & height>0   : 宽高分别缩放到指定值<br/>
     * width>0 & height<=0  : 宽缩放到指定值,高同比例缩放,保持宽高比<br/>
     * width<=0 & height>0  : 高缩放到指定值,宽同比例缩放,保持宽高比<br/>
     * width<=0 & height<=0 : 宽高根据需求尺寸(reqWidth/reqHeight)适当缩小<br/>
     * }</pre>
     *
     * @param compressFormat 图片压缩格式
     * @param compressQuality 图片压缩质量
     * @param compressWidth 指定图片宽度
     * @param compressHeight 指定图片高度
     */
    public DefaultNetLoadHandler setCompress(Bitmap.CompressFormat compressFormat, int compressQuality, int compressWidth, int compressHeight){
        this.compressFormat = compressFormat;
        this.compressQuality = compressQuality;
        this.compressWidth = compressWidth;
        this.compressHeight = compressHeight;
        return this;
    }

    /**
     * 实现网络加载过程
     */
    @Override
    public void loadFromNet(String url, int reqWidth, int reqHeight, BitmapLoader loader, final BitmapLoaderMessenger messenger) {
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
                //设置结果返回[重要]
                messenger.setResultSucceed(compress(outputStream.toByteArray(), url, reqWidth, reqHeight, loader, messenger));
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

    private byte[] compress(byte[] data, String url, int reqWidth, int reqHeight, BitmapLoader loader, BitmapLoaderMessenger messenger){
        //压缩原图片
        if (compress && data != null && data.length > 0){
            int decodeWidth;
            int decodeHeight;
            if (compressWidth <= 0 && compressHeight <= 0){
                decodeWidth = reqWidth;
                decodeHeight = reqHeight;
            } else if (compressWidth > 0 && compressHeight <= 0){
                decodeWidth = compressWidth;
                decodeHeight = compressWidth;
            } else if (compressWidth <= 0 && compressHeight > 0) {
                decodeWidth = compressHeight;
                decodeHeight = compressHeight;
            } else {
                decodeWidth = compressWidth;
                decodeHeight = compressHeight;
            }

            if (loader.getLogger() != null){
                loader.getLogger().d("[DefaultNetLoadHandler]compress start, url<" + url + "> decodeReqWidth:" + decodeWidth + " decodeReqHeight:" + decodeHeight);
            }

            Bitmap bitmap = BitmapUtils.decodeFromByteArray(data, decodeWidth, decodeHeight);
            if (bitmap == null){
                messenger.setResultFailed(new Exception("[DefaultNetLoadHandler]compress: data decode to Bitmap failed"));
                return null;
            }

            if (loader.getLogger() != null){
                loader.getLogger().d("[DefaultNetLoadHandler]compress decoded, url<" + url + "> bitmapWidth:" + bitmap.getWidth() + " bitmapHeight:" + bitmap.getHeight());
            }

            if (compressWidth > 0 || compressHeight > 0){
                bitmap = BitmapUtils.scaleTo(bitmap, compressWidth, compressHeight, true);
                if (bitmap == null){
                    messenger.setResultFailed(new Exception("[DefaultNetLoadHandler]compress: bitmap scale failed"));
                    return null;
                }
                if (loader.getLogger() != null){
                    loader.getLogger().d("[DefaultNetLoadHandler]compress scaled, url<" + url + "> bitmapWidth:" + bitmap.getWidth() + " bitmapHeight:" + bitmap.getHeight());
                }
            }

            try {
                data = BitmapUtils.bitmapToByteArray(bitmap, compressFormat, compressQuality, true);
            } catch (IOException e) {
                messenger.setResultFailed(new Exception("[DefaultNetLoadHandler]compress: bitmap encode to byteArray failed", e));
                return null;
            }
        }
        return data;
    }
}
