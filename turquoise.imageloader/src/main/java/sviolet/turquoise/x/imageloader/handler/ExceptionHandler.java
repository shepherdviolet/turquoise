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
     * <p>exception while image data length out of limit (cancel loading), notify user generally</p>
     *
     * <p>To avoid OOM, TILoader will cancel load task if data length of source image is out of limit,
     * then call ExceptionHandler->onImageDataLengthOutOfLimitException() to handle this event. You can
     * adjust the limit value by ServerSettings->setImageDataLengthLimitPercent();</p>
     *
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param dataLength length of data
     * @param lengthLimit limit of image data
     * @param logger logger
     */
    void onImageDataLengthOutOfLimitException(Context applicationContext, Context context, Task.Info taskInfo, long dataLength, long lengthLimit, TLogger logger);

    /**
     * <p>exception while memory buffer length out of limit (cancel loading), notify user generally</p>
     *
     * <p>When disk cache of TILoader is not healthy (memory full or access failed), to show image as usual,
     * we have to write all data into memory buffer, and decoding image from memory buffer.
     * To avoid OOM, we will cancel load task if data length of source image is out of buffer limit,
     * then call ExceptionHandler->onMemoryBufferLengthOutOfLimitException() to handle this event.
     * You can adjust the limit value by ServerSettings->setMemoryBufferLengthLimitPercent();</p>
     *
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param dataLength length of data
     * @param lengthLimit limit of memory buffer
     * @param logger logger
     */
    void onMemoryBufferLengthOutOfLimitException(Context applicationContext, Context context, Task.Info taskInfo, long dataLength, long lengthLimit, TLogger logger);

    /**
     * <p>call while task is aborted by low speed network (cancel loading), notify user generally</p>
     *
     * <p>Some times, network speed is very slow, but uninterruptedly, it will hardly to cause read-timeout exception,
     * In order to avoid this situation, TILoader will cancel load task with slow speed.</p>
     *
     * <p>At the beginning of loading, task will keep loading in any case, even if the speed is very slow,
     * we called it windowPeriod. After windowPeriod, it start to check loading speed:</p>
     *
     * <p>Situation 1:
     * If we can get image data length from http-header, we will calculate progress of task,
     * if we found that the speed is too slow to finish loading before deadline, we will cancel
     * task in advance.(override ExceptionHandler->onTaskAbortOnLowSpeedNetwork() method to handle this event)</p>
     *
     * <p>Situation 2:
     * If we can not get image data length from http-header, we will make reference to boundarySpeed,
     * if task is faster than boundarySpeed, task will keep loading. if not, task will be canceled.
     * (override ExceptionHandler->onTaskAbortOnLowSpeedNetwork() method to handle this event)</p>
     *
     * <p>Finally, task will be canceled when reach the deadline.</p>
     *
     * <p>You can adjust params by ServerSettings->setAbortOnLowNetworkSpeed().</p>
     *
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param elapseTime elapse time of loading
     * @param speed speed of loading
     * @param logger logger
     */
    void onTaskAbortOnLowSpeedNetwork(Context applicationContext, Context context, Task.Info taskInfo, long elapseTime, int speed, TLogger logger);

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
