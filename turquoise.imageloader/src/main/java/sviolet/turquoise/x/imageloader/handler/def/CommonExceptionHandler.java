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

package sviolet.turquoise.x.imageloader.handler.def;

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
 *
 * Created by S.Violet on 2016/3/22.
 */
public class CommonExceptionHandler implements ExceptionHandler {

    private static final String DISK_CACHE_EXCEPTION_TOAST_CN = "图片磁盘缓存访问失败,请检查您的手机内存是否已满";
    private static final String DISK_CACHE_EXCEPTION_TOAST_EN = "Image disk cache access fails, check your phone memory is full";
    private static final long DISK_CACHE_EXCEPTION_NOTICE_INTERVAL = 5 * 60 * 1000L;//5min

    private AtomicLong diskCacheExceptionNoticeTime = new AtomicLong(0);

    @Override
    public void onDiskCacheOpenException(Context applicationContext, Context context, Throwable throwable, TLogger logger) {
        long time = DateTimeUtils.getUptimeMillis();
        long previousTime = diskCacheExceptionNoticeTime.getAndSet(time);
        if ((time - previousTime) > DISK_CACHE_EXCEPTION_NOTICE_INTERVAL) {
            Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_DISK_CACHE_OPEN_EXCEPTION);
            msg.obj = new Info(applicationContext, throwable);
            msg.sendToTarget();
        }
        logger.e("DiskCacheOpenException", throwable);
    }

    @Override
    public void onDiskCacheReadException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        long time = DateTimeUtils.getUptimeMillis();
        long previousTime = diskCacheExceptionNoticeTime.getAndSet(time);
        if ((time - previousTime) > DISK_CACHE_EXCEPTION_NOTICE_INTERVAL) {
            Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_DISK_CACHE_OPEN_EXCEPTION);
            msg.obj = new Info(applicationContext, throwable);
            msg.sendToTarget();
        }
        logger.e("DiskCacheReadException:" + taskInfo, throwable);
    }

    @Override
    public void onDiskCacheWriteException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        long time = DateTimeUtils.getUptimeMillis();
        long previousTime = diskCacheExceptionNoticeTime.getAndSet(time);
        if ((time - previousTime) > DISK_CACHE_EXCEPTION_NOTICE_INTERVAL) {
            Message msg = myHandler.obtainMessage(MyHandler.HANDLER_ON_DISK_CACHE_OPEN_EXCEPTION);
            msg.obj = new Info(applicationContext, throwable);
            msg.sendToTarget();
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
    public void onDecodeException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        logger.e("DecodeException:" + taskInfo, throwable);
    }

    /*******************************************************************************
     * protected
     */

    protected void onDiskCacheOpenExceptionInner(final Context context, Throwable throwable) {
        if (context != null){
            if (DeviceUtils.isLocaleZhCn(context)){
                Toast.makeText(context, DISK_CACHE_EXCEPTION_TOAST_CN, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, DISK_CACHE_EXCEPTION_TOAST_EN, Toast.LENGTH_LONG).show();
            }
        }
    }

    /*******************************************************************************
     * handler
     */

    private final MyHandler myHandler = new MyHandler(Looper.getMainLooper(), this);

    private static class MyHandler extends WeakHandler<CommonExceptionHandler>{

        private static final int HANDLER_ON_DISK_CACHE_OPEN_EXCEPTION = 1;

        public MyHandler(Looper looper, CommonExceptionHandler host) {
            super(looper, host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, CommonExceptionHandler host) {
            switch (msg.what){
                case HANDLER_ON_DISK_CACHE_OPEN_EXCEPTION:
                    if (msg.obj instanceof Info) {
                        host.onDiskCacheOpenExceptionInner(((Info) msg.obj).getApplicationContext(), ((Info) msg.obj).getThrowable());
                    }
                    break;
                default:
                    break;
            }
        }

    }

    private static class Info{
        private WeakReference<Context> applicationContext;
        private Throwable throwable;

        private Info(Context context, Throwable throwable){
            setApplicationContext(context);
            setThrowable(throwable);
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
