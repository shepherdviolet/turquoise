/*
 * Copyright (C) 2015-2016 S.Violet
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

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicLong;

import sviolet.turquoise.enhance.common.WeakHandler;
import sviolet.turquoise.util.common.DateTimeUtils;
import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.handler.ExceptionHandler;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>common implementation of ExceptionHandler</p>
 *
 * Created by S.Violet on 2016/3/22.
 */
public class CommonExceptionHandler implements ExceptionHandler {

    private static final String DISK_CACHE_EXCEPTION_TOAST_CN = "图片磁盘缓存访问失败,请检查您的手机内存是否已满,图片将通过网络下载.";
    private static final String DISK_CACHE_EXCEPTION_TOAST_EN = "Image disk cache access fails, check your phone memory, image will loading from network always.";
    private static final long DISK_CACHE_EXCEPTION_NOTICE_INTERVAL = 20 * 1000L;//20s

    private static final String IMAGE_DATA_LENGTH_OUT_OF_LIMIT_TOAST_CN = "部分图片过大, 加载取消.";
    private static final String IMAGE_DATA_LENGTH_OUT_OF_LIMIT_TOAST_EN = "Some images are too large, load tasks ware canceled.";
    private static final String MEMORY_BUFFER_LENGTH_OUT_OF_LIMIT_TOAST_CN = "由于磁盘访问失败, 大图的加载取消.";
    private static final String MEMORY_BUFFER_LENGTH_OUT_OF_LIMIT_TOAST_EN = "Owing to failure of disk cache access, load tasks of large image canceled.";
    private static final String TASK_ABORT_ON_LOW_SPEED_NETWORK_TOAST_CN = "由于网速过慢, 部分图片取消加载.";
    private static final String TASK_ABORT_ON_LOW_SPEED_NETWORK_TOAST_EN = "Network speed is too slow, some of the load tasks were canceled.";

    private AtomicLong diskCacheExceptionNoticeTime = new AtomicLong(0);

    @Override
    public void onDiskCacheOpenException(Context applicationContext, Context context, Throwable throwable, TLogger logger) {
        long time = DateTimeUtils.getUptimeMillis();
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
        long time = DateTimeUtils.getUptimeMillis();
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
        long time = DateTimeUtils.getUptimeMillis();
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
    public void onNetworkLoadException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
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
    public void onMemoryBufferLengthOutOfLimitException(Context applicationContext, Context context, Task.Info taskInfo, long dataLength, long lengthLimit, TLogger logger) {
        Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_MEMORY_BUFFER_LENGTH_OUT_OF_LIMIT);
        msg.obj = new Info(applicationContext);
        msg.sendToTarget();
        logger.e("MemoryBufferLengthOutOfLimit: diskCache is unhealthy, we have to write data into memory buffer, but the image is too large, so we cancel loading, ServerSettings->setMemoryBufferLengthLimitPercent() can adjust limit, dataLength:" + dataLength + ", limit:" + lengthLimit + ", task:" + taskInfo);

    }

    @Override
    public void onTaskAbortOnLowSpeedNetwork(Context applicationContext, Context context, Task.Info taskInfo, long elapseTime, int speed, float progress, TLogger logger) {
        Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_TASK_ABORT_ON_LOW_SPEED_NETWORK);
        msg.obj = new Info(applicationContext);
        msg.sendToTarget();

        String progressStr;
        if (progress >= 0){
            progressStr = Float.toString(progress * 100);
        }else{
            progressStr = "?";
        }
        logger.e("TaskAbortOnLowSpeedNetwork: network speed is too slow, cancel loading, " + elapseTime + "ms elapse, " + speed / 1024 + "k/s, " + progressStr + "%loaded, ServerSettings->setAbortOnLowNetworkSpeed() can adjust configuration, task:" + taskInfo);
    }

    @Override
    public void onDecodeException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        logger.e("DecodeException:" + taskInfo, throwable);
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
        private static final int HANDLER_ON_IMAGE_DATA_LENGTH_OUT_OF_LIMIT = 2;
        private static final int HANDLER_ON_MEMORY_BUFFER_LENGTH_OUT_OF_LIMIT = 3;
        private static final int HANDLER_ON_TASK_ABORT_ON_LOW_SPEED_NETWORK = 4;

        public MyHandler(Looper looper, CommonExceptionHandler host) {
            super(looper, host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, CommonExceptionHandler host) {
            switch (msg.what){
                case HANDLER_ON_DISK_CACHE_OPEN_EXCEPTION:
                    host.showToast(((Info) msg.obj).getApplicationContext(), DISK_CACHE_EXCEPTION_TOAST_CN, DISK_CACHE_EXCEPTION_TOAST_EN);
                    break;
                case HANDLER_ON_IMAGE_DATA_LENGTH_OUT_OF_LIMIT:
                    host.showToast(((Info) msg.obj).getApplicationContext(), IMAGE_DATA_LENGTH_OUT_OF_LIMIT_TOAST_CN, IMAGE_DATA_LENGTH_OUT_OF_LIMIT_TOAST_EN);
                    break;
                case HANDLER_ON_MEMORY_BUFFER_LENGTH_OUT_OF_LIMIT:
                    host.showToast(((Info) msg.obj).getApplicationContext(), MEMORY_BUFFER_LENGTH_OUT_OF_LIMIT_TOAST_CN, MEMORY_BUFFER_LENGTH_OUT_OF_LIMIT_TOAST_EN);
                    break;
                case HANDLER_ON_TASK_ABORT_ON_LOW_SPEED_NETWORK:
                    host.showToast(((Info) msg.obj).getApplicationContext(), TASK_ABORT_ON_LOW_SPEED_NETWORK_TOAST_CN, TASK_ABORT_ON_LOW_SPEED_NETWORK_TOAST_EN);
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
