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

package sviolet.turquoise.modelx.bitmaploader.handler;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import sviolet.turquoise.util.common.BitmapUtils;
import sviolet.turquoise.modelx.bitmaploader.BitmapLoader;
import sviolet.turquoise.modelx.bitmaploader.BitmapLoaderMessenger;
import sviolet.turquoise.modelx.bitmaploader.entity.BitmapRequest;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * 网络加载处理器默认实现<p/>
 *
 * 实现根据url地址, 通过HTTP/GET方式, 从网络下载图片数据的过程, 实现任务取消时, 强制终止网络加载的功能(可选)<p/>
 *
 * 设置网络超时时间:<Br/>
 * <pre>{@code
 *      //连接超时5s, 读取超时20s
 *      BitmapLoader.Builder.setNetLoadHandler(new DefaultNetLoadHandler(5000, 20000))
 * }</pre>
 * <br/>
 * 加载任务取消时, 不强制终止网络加载:<br/>
 * <pre>{@code
 *      //加载任务取消时, 不强制终止网络加载
 *      BitmapLoader.Builder.setNetLoadHandler(new DefaultNetLoadHandler().setForceCancel(false))
 * }</pre
 * <br/>
 * 设置原图缩放(用于节省磁盘空间, 或固定原片尺寸):<br/>
 * <pre>{@code
 *      //设置原图缩放, 指定宽度100px, 高度自适应, 保存格式JPEG, 质量70
 *      BitmapLoader.Builder.setNetLoadHandler(new DefaultNetLoadHandler().setScale(Bitmap.CompressFormat.JPEG, 70, 100, 0))
 * }</pre><p/>
 *
 * 注意: 在该"网络加载处理器"中特殊处理图片数据, 磁盘缓存将保存改变后的数据, 而非原始数据. 这点与在
 * {@link BitmapDecodeHandler}中进行图片特殊处理不同, NetLoadHandler适合进行较为复杂的图片处理,
 * 因为仅影响网络加载时的效率, 磁盘缓存加载时直接加载处理后的数据, 效率较高<p/>
 *
 * Created by S.Violet on 2015/10/12.
 */
public class DefaultNetLoadHandler implements NetLoadHandler {

    private TLogger logger = TLogger.get(this);

    private int connectTimeout;
    private int readTimeout;
    private boolean forceCancel = true;
    private boolean scale = false;
    private int scaleWidth = 0;
    private int scaleHeight = 0;
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
     * 设置原图缩放, 默认不缩放<p/>
     *
     * 将图片缩放为指定尺寸, 并转码为指定格式, 用于减少磁盘缓存占用, 或固定原图尺寸(格式).
     * 注意:磁盘缓存将保存缩放后的图片数据.<Br/>
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
     * @param scaleWidth 指定图片宽度
     * @param scaleHeight 指定图片高度
     */
    public DefaultNetLoadHandler setScale(Bitmap.CompressFormat compressFormat, int compressQuality, int scaleWidth, int scaleHeight){
        this.scale = true;
        this.compressFormat = compressFormat;
        this.compressQuality = compressQuality;
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;
        return this;
    }

    /**
     * 实现网络加载过程
     */
    @Override
    public void loadFromNet(Context context, BitmapLoader loader, BitmapRequest request, final BitmapLoaderMessenger messenger) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        HttpURLConnection conn = null;
        try {
            URL httpUrl = new URL(request.getUrl());
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
                messenger.setResultSucceed(onScale(outputStream.toByteArray(), request, loader, messenger));
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

    /**
     * 缩放处理
     */
    protected byte[] onScale(byte[] data, BitmapRequest request, BitmapLoader loader, BitmapLoaderMessenger messenger){
        //缩放原图片
        if (scale && data != null && data.length > 0){
            int decodeWidth = 0;
            int decodeHeight = 0;
            if (scaleWidth <= 0 && scaleHeight <= 0){
                //判断需求尺寸是否生效
                if (request.hasReqDimension()) {
                    decodeWidth = request.getReqWidth();
                    decodeHeight = request.getReqHeight();
                }
            } else if (scaleWidth > 0 && scaleHeight <= 0){
                decodeWidth = scaleWidth;
                decodeHeight = scaleWidth;
            } else if (scaleWidth <= 0 && scaleHeight > 0) {
                decodeWidth = scaleHeight;
                decodeHeight = scaleHeight;
            } else {
                decodeWidth = scaleWidth;
                decodeHeight = scaleHeight;
            }

            logger.d("scale: start, url<" + request.getUrl() + "> decodeReqWidth:" + decodeWidth + " decodeReqHeight:" + decodeHeight);

            Bitmap bitmap = BitmapUtils.decodeFromByteArray(data, decodeWidth, decodeHeight);
            if (bitmap == null){
                messenger.setResultFailed(new Exception("[DefaultNetLoadHandler]scale: data decode to Bitmap failed"));
                return null;
            }

            logger.d("scale: decoded, url<" + request.getUrl() + "> bitmapWidth:" + bitmap.getWidth() + " bitmapHeight:" + bitmap.getHeight());

            if (scaleWidth > 0 || scaleHeight > 0){
                bitmap = BitmapUtils.scaleTo(bitmap, scaleWidth, scaleHeight, true);
                if (bitmap == null){
                    messenger.setResultFailed(new Exception("[DefaultNetLoadHandler]scale: bitmap scale failed"));
                    return null;
                }
                logger.d("scale: scaled, url<" + request.getUrl() + "> bitmapWidth:" + bitmap.getWidth() + " bitmapHeight:" + bitmap.getHeight());
            }

            //图片特殊处理
            bitmap = onSpecialProcessing(bitmap, request, loader, messenger);
            if (bitmap == null || bitmap.isRecycled()){
                messenger.setResultFailed(new Exception("[DefaultNetLoadHandler]scale: Bitmap onSpecialProcessing() failed"));
                return null;
            }

            //转码
            try {
                data = BitmapUtils.bitmapToByteArray(bitmap, compressFormat, compressQuality, true);
            } catch (IOException e) {
                messenger.setResultFailed(new Exception("[DefaultNetLoadHandler]scale: bitmap encode to byteArray failed", e));
                return null;
            }
        }
        return data;
    }

    /**
     * 图片特殊处理, 仅在设置原图缩放的情况下生效<br/>
     * 对bitmap进行特殊处理并返回<br/>
     */
    protected Bitmap onSpecialProcessing(Bitmap bitmap, BitmapRequest request, BitmapLoader loader, BitmapLoaderMessenger messenger){
        return bitmap;
    }

}
