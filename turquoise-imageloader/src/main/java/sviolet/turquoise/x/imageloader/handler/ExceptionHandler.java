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

package sviolet.turquoise.x.imageloader.handler;

import android.content.Context;

import sviolet.turquoise.x.common.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.LowNetworkSpeedStrategy;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>implement exception handling</p>
 *
 * Created by S.Violet on 2016/2/19.
 */
public interface ExceptionHandler {

    /*************************************************************
     * disk engine
     */

    /**
     * exception while inner disk cache open, notify user generally.
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param throwable throwable
     * @param logger logger
     */
    void onDiskCacheOpenException(Context applicationContext, Context context, Throwable throwable, TLogger logger);

    /**
     * exception while inner disk cache reading, notify user generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onDiskCacheReadException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

    /**
     * exception while inner disk cache writing, notify user generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onDiskCacheWriteException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

    /**
     * exception while inner disk cache close/flush..., for unimportance exception, print log generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param throwable throwable
     * @param logger logger
     */
    void onDiskCacheCommonException(Context applicationContext, Context context, Throwable throwable, TLogger logger);

    /**
     * exception while loading image from device local disk (SourceType.LOCAL_DISK), notify user generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onLocalDiskLoadCommonException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

    /**
     * Image is not exists in device local disk (SourceType.LOCAL_DISK), notify user generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onLocalDiskLoadNotExistsException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

    /**
     * exception while loading image from apk assets / resources (SourceType.APK_ASSETS/APK_RES), notify user generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onApkLoadCommonException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

    /**
     * Image is not exists in apk assets / resources (SourceType.APK_ASSETS/APK_RES), notify user generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onApkLoadNotExistsException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

    /*************************************************************
     * net engine
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
     * <p>exception while first connection accept ranges but the others are not accepted (cancel loading), notify user generally</p>
     *
     * <p>This event occurs during multithreaded loading. When the first connection indicates that it
     * supports ACCEPT_RANGES, but other connections indicate that they do not support ACCEPT_RANGES (
     * Or the returned parameters do not match expectations).</p>
     *
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param logger logger
     */
    void onHttpContentRangeParseException(Context applicationContext, Context context, Task.Info taskInfo, TLogger logger);

    /**
     * <p>Some times, network speed is very slow, but uninterruptedly, it will hardly to cause read-timeout exception,
     * In order to avoid this situation, TILoader will cancel load task with slow speed.</p>
     *
     * <p>At the beginning of loading, task will keep loading in any case, even if the speed is very slow,
     * we called it "windowPeriod". After "windowPeriod", it start to check loading speed.
     * If the speed is slower than "thresholdSpeed", task will be canceled. (you can override
     * {@link ExceptionHandler#handleLowNetworkSpeedEvent} method to handle this event).
     * If the speed is faster than "thresholdSpeed", we will try to get data length from http-header,
     * in order to calculate progress of task. If we found that the speed is too slow to finish task
     * before "deadline", we will cancel task in advance.(override {@link ExceptionHandler#handleLowNetworkSpeedEvent}
     * method to handle this event).</p>
     *
     * <p>Finally, task will be canceled when reach the "deadline".</p>
     *
     * <p>***************************************************************************************************</p>
     *
     * <p>You can adjust configure by ServerSettings->setLowNetworkSpeedStrategy(). There are several strategies
     * to cope with different network environments. See {@link LowNetworkSpeedStrategy.Type}.</p>
     *
     * <p>"Indispensable" task ({@link Params.Builder#setIndispensable}) has double connection-timeout & read-timeout,
     * and loading with {@link LowNetworkSpeedStrategy.Type#INDISPENSABLE_TASK} strategy.</p>
     *
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param elapseTime elapse time of loading
     * @param speed speed of loading
     * @param logger logger
     */
    void handleLowNetworkSpeedEvent(Context applicationContext, Context context, Task.Info taskInfo, long elapseTime, int speed, TLogger logger);

    /*************************************************************
     * memory engine
     */

    /**
     * exception while memory cache execute..., for unimportance exception, print log generally
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param throwable throwable
     * @param logger logger
     */
    void onMemoryCacheCommonException(Context applicationContext, Context context, Throwable throwable, TLogger logger);

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
