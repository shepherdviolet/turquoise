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

package sviolet.turquoise.x.imageloader.handler;

import android.content.Context;

import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>implement exception handling</p>
 *
 * Created by S.Violet on 2016/2/19.
 */
public interface ExceptionHandler {

    /*************************************************************
     * disk
     */

    /**
     * exception while disk cache open, notify user generally.
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param throwable throwable
     * @param logger logger
     */
    void onDiskCacheOpenException(Context applicationContext, Context context, Throwable throwable, TLogger logger);

    /**
     * exception while disk cache reading, notify user generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onDiskCacheReadException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

    /**
     * exception while disk cache writing, notify user generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onDiskCacheWriteException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

    /**
     * exception while disk cache close/flush..., for unimportance exception, print log generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param throwable throwable
     * @param logger logger
     */
    void onDiskCacheCommonException(Context applicationContext, Context context, Throwable throwable, TLogger logger);

    /*************************************************************
     * net
     */

    /**
     * exception while network loading, print log generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onNetworkLoadException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

    /**
     * exception while image data length out of limit (cancel loading), notify user generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param dataLength length of data
     * @param lengthLimit limit of image data
     * @param logger logger
     */
    void onImageDataLengthOutOfLimitException(Context applicationContext, Context context, Task.Info taskInfo, long dataLength, long lengthLimit, TLogger logger);

    /**
     * exception while memory buffer length out of limit (cancel loading), notify user generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param dataLength length of data
     * @param lengthLimit limit of memory buffer
     * @param logger logger
     */
    void onMemoryBufferLengthOutOfLimitException(Context applicationContext, Context context, Task.Info taskInfo, long dataLength, long lengthLimit, TLogger logger);

    /**
     * call while task is aborted by low speed network (cancel loading), notify user generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param elapseTime elapse time of loading
     * @param speed speed of loading
     * @param progress progress of loading (-1 or 0~1)
     * @param logger logger
     */
    void onTaskAbortOnLowSpeedNetwork(Context applicationContext, Context context, Task.Info taskInfo, long elapseTime, int speed, float progress, TLogger logger);

    /*************************************************************
     * decode
     */

    /**
     * exception while image decoding, print log generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onDecodeException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

}
