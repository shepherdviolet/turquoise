/*
 * Copyright (C) 2015-2017 S.Violet
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

package sviolet.turquoise.x.imageloader.handler.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicLong;

import sviolet.turquoise.enhance.async.WeakHandler;
import sviolet.turquoise.util.common.DateTimeUtilsForAndroid;
import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.util.droid.NetStateUtils;
import sviolet.turquoise.x.common.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.handler.ExceptionHandler;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>common implementation of ExceptionHandler</p>
 *
 * @author S.Violet
 */
public class CommonExceptionHandler implements ExceptionHandler {

    private static final String DISK_CACHE_EXCEPTION_TOAST_CN = "手机存储已满(存储访问错误), 图片无法加载";
    private static final String DISK_CACHE_EXCEPTION_TOAST_EN = "The storage is full(storage access error), image loading failed";
    private static final long DISK_CACHE_EXCEPTION_NOTICE_INTERVAL = 20 * 1000L;//20s

    private static final String DISK_LOAD_EXCEPTION_TOAST_CN = "磁盘加载图片失败";
    private static final String DISK_LOAD_EXCEPTION_TOAST_EN = "Image loading failed from local disk.";
    private static final String DISK_LOAD_NOT_EXISTS_TOAST_CN = "磁盘加载图片失败, 不存在";
    private static final String DISK_LOAD_NOT_EXISTS_TOAST_EN = "Image loading failed from local disk, not exists.";

    private static final String APK_LOAD_EXCEPTION_TOAST_CN = "应用加载图片失败";
    private static final String APK_LOAD_EXCEPTION_TOAST_EN = "Image loading failed from app.";
    private static final String APK_LOAD_NOT_EXISTS_TOAST_CN = "应用加载图片失败, 不存在";
    private static final String APK_LOAD_NOT_EXISTS_TOAST_EN = "Image loading failed from app, not exists.";

    private static final String IMAGE_DATA_LENGTH_OUT_OF_LIMIT_TOAST_CN = "部分图片过大, 加载失败.";
    private static final String IMAGE_DATA_LENGTH_OUT_OF_LIMIT_TOAST_EN = "Some images are too large, loading failed.";
    private static final String HTTP_CONTENT_RANGE_PARSE_TOAST_CN = "服务端资源异常, 加载效率低";
    private static final String HTTP_CONTENT_RANGE_PARSE_TOAST_EN = "Abnormal server resources, low loading efficiency";
    private static final String TASK_ABORT_ON_LOW_SPEED_NETWORK_TOAST_CN = "网速过慢, 图片加载失败.";
    private static final String TASK_ABORT_ON_LOW_SPEED_NETWORK_TOAST_EN = "Network speed is too slow, loading failed.";

    private static final String NETWORK_NOT_CONNECTED_CN = "图片加载失败, 网络未连接.";
    private static final String NETWORK_NOT_CONNECTED_EN = "Image loading failed, network bad.";
    private static final long NETWORK_EXCEPTION_NOTICE_INTERVAL = 20 * 1000L;//20s

    private AtomicLong diskCacheExceptionNoticeTime = new AtomicLong(0);
    private AtomicLong networkExceptionNoticeTime = new AtomicLong(0);

    @Override
    public void onDiskCacheOpenException(Context applicationContext, Context context, Throwable throwable, TLogger logger) {
        long time = DateTimeUtilsForAndroid.getUptimeMillis();
        long previousTime = diskCacheExceptionNoticeTime.get();
        if ((time - previousTime) > DISK_CACHE_EXCEPTION_NOTICE_INTERVAL) {
            if (diskCacheExceptionNoticeTime.compareAndSet(previousTime, time)) {
                Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_DISK_CACHE_OPEN_EXCEPTION);
                msg.obj = new Info(applicationContext);
                msg.sendToTarget();
            }
        }
        logger.e("DiskCacheOpenException", throwable);
    }

    @Override
    public void onDiskCacheReadException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        long time = DateTimeUtilsForAndroid.getUptimeMillis();
        long previousTime = diskCacheExceptionNoticeTime.get();
        if ((time - previousTime) > DISK_CACHE_EXCEPTION_NOTICE_INTERVAL) {
            if (diskCacheExceptionNoticeTime.compareAndSet(previousTime, time)) {
                Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_DISK_CACHE_OPEN_EXCEPTION);
                msg.obj = new Info(applicationContext);
                msg.sendToTarget();
            }
        }
        logger.e("DiskCacheReadException:" + taskInfo, throwable);
    }

    @Override
    public void onDiskCacheWriteException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        long time = DateTimeUtilsForAndroid.getUptimeMillis();
        long previousTime = diskCacheExceptionNoticeTime.get();
        if ((time - previousTime) > DISK_CACHE_EXCEPTION_NOTICE_INTERVAL) {
            if (diskCacheExceptionNoticeTime.compareAndSet(previousTime, time)) {
                Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_DISK_CACHE_OPEN_EXCEPTION);
                msg.obj = new Info(applicationContext);
                msg.sendToTarget();
            }
        }
        logger.e("DiskCacheWriteException:" + taskInfo, throwable);
    }

    @Override
    public void onDiskCacheCommonException(Context applicationContext, Context context, Throwable throwable, TLogger logger) {
        logger.w("DiskCacheCommonException", throwable);
    }

    @Override
    public void onLocalDiskLoadCommonException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_DISK_LOAD_COMMON_EXCEPTION);
        msg.obj = new Info(applicationContext);
        msg.sendToTarget();
        logger.e("LocalDiskLoadCommonException:" + taskInfo, throwable);
    }

    @Override
    public void onLocalDiskLoadNotExistsException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_DISK_LOAD_NOT_EXISTS_EXCEPTION);
        msg.obj = new Info(applicationContext);
        msg.sendToTarget();
        logger.e("LocalDiskLoadNotExistsException:" + taskInfo, throwable);
    }

    @Override
    public void onApkLoadCommonException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_APK_LOAD_COMMON_EXCEPTION);
        msg.obj = new Info(applicationContext);
        msg.sendToTarget();
        logger.e("ApkLoadCommonException:" + taskInfo, throwable);
    }

    @Override
    public void onApkLoadNotExistsException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_APK_LOAD_NOT_EXISTS_EXCEPTION);
        msg.obj = new Info(applicationContext);
        msg.sendToTarget();
        logger.e("ApkLoadNotExistsException:" + taskInfo, throwable);
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onNetworkLoadException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        if (!NetStateUtils.isNetworkConnected(applicationContext)){
            long time = DateTimeUtilsForAndroid.getUptimeMillis();
            long previousTime = networkExceptionNoticeTime.get();
            if ((time - previousTime) > NETWORK_EXCEPTION_NOTICE_INTERVAL) {
                if (networkExceptionNoticeTime.compareAndSet(previousTime, time)) {
                    Message msg = myHandler.obtainMessage(MyHandler.HANDLER_NETWORK_NOT_CONNECTED);
                    msg.obj = new Info(applicationContext);
                    msg.sendToTarget();
                }
            }
        }
        //print unknownHostException
        if(throwable instanceof UnknownHostException){
            logger.e("NetworkLoadException:" + taskInfo + "\n" + throwable.getMessage(), throwable);
            return;
        }
        logger.e("NetworkLoadException:" + taskInfo, throwable);
    }

    @Override
    public void onImageDataLengthOutOfLimitException(Context applicationContext, Context context, Task.Info taskInfo, long dataLength, long lengthLimit, TLogger logger) {
        Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_IMAGE_DATA_LENGTH_OUT_OF_LIMIT);
        msg.obj = new Info(applicationContext);
        msg.sendToTarget();
        logger.e("ImageDataLengthOutOfLimit: image is too large, cancel loading, ServerSettings->setImageDataLengthLimitPercent() can adjust limit, dataLength:" + dataLength + ", limit:" + lengthLimit + ", task:" + taskInfo);
    }

    @Override
    public void onHttpContentRangeParseException(Context applicationContext, Context context, Task.Info taskInfo, TLogger logger) {
        Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_HTTP_CONTENT_RANGE_PARSE);
        msg.obj = new Info(applicationContext);
        msg.sendToTarget();
        logger.e("HttpContentRangeParseException: CAUTION!!! Http ranges is not accepted, first connection is accepted, but the others did not, task:" + taskInfo);
    }

    @Override
    public void handleLowNetworkSpeedEvent(Context applicationContext, Context context, Task.Info taskInfo, long elapseTime, int speed, TLogger logger) {
        Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_TASK_ABORT_ON_LOW_SPEED_NETWORK);
        msg.obj = new Info(applicationContext);
        msg.sendToTarget();
        logger.e("handleLowNetworkSpeedEvent: network speed is too slow, cancel loading, " + elapseTime + "ms elapse, " + speed / 1024 + "k/s, ServerSettings->setLowNetworkSpeedStrategy() can adjust configuration, task:" + taskInfo);
    }

    @Override
    public void onDecodeException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        logger.e("DecodeException:" + taskInfo, throwable);
    }

    @Override
    public void onMemoryCacheCommonException(Context applicationContext, Context context, Throwable throwable, TLogger logger) {
        logger.w("MemoryCacheCommonException", throwable);
    }

    /*******************************************************************************
     * protected
     */

    protected void showToast(final Context context, String msgCn, String msgEn) {
        if (context != null){
            if (DeviceUtils.isLocaleZhCn(context)){
                Toast.makeText(context, msgCn, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, msgEn, Toast.LENGTH_LONG).show();
            }
        }
    }

    /*******************************************************************************
     * handler
     */

    private final MyHandler myHandler = new MyHandler(Looper.getMainLooper(), this);

    private static class MyHandler extends WeakHandler<CommonExceptionHandler>{

        private static final int HANDLER_ON_DISK_CACHE_OPEN_EXCEPTION = 1;
        private static final int HANDLER_ON_DISK_LOAD_COMMON_EXCEPTION = 2;
        private static final int HANDLER_ON_DISK_LOAD_NOT_EXISTS_EXCEPTION = 3;
        private static final int HANDLER_ON_APK_LOAD_COMMON_EXCEPTION = 4;
        private static final int HANDLER_ON_APK_LOAD_NOT_EXISTS_EXCEPTION = 5;
        private static final int HANDLER_ON_IMAGE_DATA_LENGTH_OUT_OF_LIMIT = 6;
        private static final int HANDLER_ON_HTTP_CONTENT_RANGE_PARSE = 7;
        private static final int HANDLER_ON_TASK_ABORT_ON_LOW_SPEED_NETWORK = 8;
        private static final int HANDLER_NETWORK_NOT_CONNECTED = 9;

        public MyHandler(Looper looper, CommonExceptionHandler host) {
            super(looper, host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, CommonExceptionHandler host) {
            switch (msg.what){
                case HANDLER_ON_DISK_CACHE_OPEN_EXCEPTION:
                    host.showToast(((Info) msg.obj).getApplicationContext(), DISK_CACHE_EXCEPTION_TOAST_CN, DISK_CACHE_EXCEPTION_TOAST_EN);
                    break;
                case HANDLER_ON_DISK_LOAD_COMMON_EXCEPTION:
                    host.showToast(((Info) msg.obj).getApplicationContext(), DISK_LOAD_EXCEPTION_TOAST_CN, DISK_LOAD_EXCEPTION_TOAST_EN);
                    break;
                case HANDLER_ON_DISK_LOAD_NOT_EXISTS_EXCEPTION:
                    host.showToast(((Info) msg.obj).getApplicationContext(), DISK_LOAD_NOT_EXISTS_TOAST_CN, DISK_LOAD_NOT_EXISTS_TOAST_EN);
                    break;
                case HANDLER_ON_APK_LOAD_COMMON_EXCEPTION:
                    host.showToast(((Info) msg.obj).getApplicationContext(), APK_LOAD_EXCEPTION_TOAST_CN, APK_LOAD_EXCEPTION_TOAST_EN);
                case HANDLER_ON_APK_LOAD_NOT_EXISTS_EXCEPTION:
                    host.showToast(((Info) msg.obj).getApplicationContext(), APK_LOAD_NOT_EXISTS_TOAST_CN, APK_LOAD_NOT_EXISTS_TOAST_EN);
                    break;
                case HANDLER_ON_IMAGE_DATA_LENGTH_OUT_OF_LIMIT:
                    host.showToast(((Info) msg.obj).getApplicationContext(), IMAGE_DATA_LENGTH_OUT_OF_LIMIT_TOAST_CN, IMAGE_DATA_LENGTH_OUT_OF_LIMIT_TOAST_EN);
                    break;
                case HANDLER_ON_HTTP_CONTENT_RANGE_PARSE:
                    host.showToast(((Info) msg.obj).getApplicationContext(), HTTP_CONTENT_RANGE_PARSE_TOAST_CN, HTTP_CONTENT_RANGE_PARSE_TOAST_EN);
                    break;
                case HANDLER_ON_TASK_ABORT_ON_LOW_SPEED_NETWORK:
                    host.showToast(((Info) msg.obj).getApplicationContext(), TASK_ABORT_ON_LOW_SPEED_NETWORK_TOAST_CN, TASK_ABORT_ON_LOW_SPEED_NETWORK_TOAST_EN);
                    break;
                case HANDLER_NETWORK_NOT_CONNECTED:
                    host.showToast(((Info) msg.obj).getApplicationContext(), NETWORK_NOT_CONNECTED_CN, NETWORK_NOT_CONNECTED_EN);
                    break;
                default:
                    break;
            }
        }

    }

    private static class Info{
        private WeakReference<Context> applicationContext;
        private Throwable throwable;

        private Info(Context context){
            setApplicationContext(context);
        }

        public Context getApplicationContext() {
            if (applicationContext != null){
                return applicationContext.get();
            }
            return null;
        }

        public void setApplicationContext(final Context context) {
            if (context != null){
                applicationContext = new WeakReference<>(context.getApplicationContext());
            }
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
        }
    }

}
