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

package sviolet.demoaimageloader.custom;

import android.content.Context;
import android.util.Log;

import sviolet.turquoise.x.common.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.handler.ExceptionHandler;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * 自定义实现异常处理: 改为用Log打印
 *
 * Created by S.Violet on 2016/4/27.
 */
public class MyExceptionHandler implements ExceptionHandler {

    @Override
    public void onDiskCacheOpenException(Context applicationContext, Context context, Throwable throwable, TLogger logger) {
        Log.e("MyExceptionHandler", "onDiskCacheOpenException", throwable);
    }

    @Override
    public void onDiskCacheReadException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        Log.e("MyExceptionHandler", "onDiskCacheReadException", throwable);
    }

    @Override
    public void onDiskCacheWriteException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        Log.e("MyExceptionHandler", "onDiskCacheWriteException", throwable);
    }

    @Override
    public void onDiskCacheCommonException(Context applicationContext, Context context, Throwable throwable, TLogger logger) {
        Log.e("MyExceptionHandler", "onDiskCacheCommonException", throwable);
    }

    @Override
    public void onLocalDiskLoadCommonException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        Log.e("MyExceptionHandler", "onLocalDiskLoadCommonException", throwable);
    }

    @Override
    public void onLocalDiskLoadNotExistsException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        Log.e("MyExceptionHandler", "onLocalDiskLoadNotExistsException", throwable);
    }

    @Override
    public void onApkLoadCommonException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        Log.e("MyExceptionHandler", "onApkLoadCommonException", throwable);
    }

    @Override
    public void onApkLoadNotExistsException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        Log.e("MyExceptionHandler", "onApkLoadNotExistsException", throwable);
    }

    @Override
    public void onNetworkLoadException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        Log.e("MyExceptionHandler", "onNetworkLoadException", throwable);
    }

    @Override
    public void onImageDataLengthOutOfLimitException(Context applicationContext, Context context, Task.Info taskInfo, long dataLength, long lengthLimit, TLogger logger) {
        Log.e("MyExceptionHandler", "onImageDataLengthOutOfLimitException", null);
    }

    @Override
    public void onHttpContentRangeParseException(Context applicationContext, Context context, Task.Info taskInfo, TLogger logger) {
        Log.e("MyExceptionHandler", "onHttpContentRangeParseException", null);
    }

    @Override
    public void handleLowNetworkSpeedEvent(Context applicationContext, Context context, Task.Info taskInfo, long elapseTime, int speed, TLogger logger) {
        Log.e("MyExceptionHandler", "handleLowNetworkSpeedEvent", null);

    }

    @Override
    public void onDecodeException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger) {
        Log.e("MyExceptionHandler", "onDecodeException", throwable);
    }

    @Override
    public void onMemoryCacheCommonException(Context applicationContext, Context context, Throwable throwable, TLogger logger) {
        Log.e("MyExceptionHandler", "onMemoryCacheCommonException", throwable);
    }
}
