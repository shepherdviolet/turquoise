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
 * Created by S.Violet on 2016/2/19.
 */
public interface ExceptionHandler {

    /*************************************************************
     * disk
     */

    /**
     * exception while disk cache open
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param throwable throwable
     * @param logger logger
     */
    void onDiskCacheOpenException(Context applicationContext, Context context, Throwable throwable, TLogger logger);

    /**
     * exception while disk cache reading
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onDiskCacheReadException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

    /**
     * exception while disk cache writing
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onDiskCacheWriteException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

    /**
     * exception while disk cache close/flush..., for unimportance exception
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
     * exception while network loading
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onNetworkLoadException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

    /*************************************************************
     * decode
     */

    /**
     * exception while image decoding
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param taskInfo taskInfo
     * @param throwable throwable
     * @param logger logger
     */
    void onDecodeException(Context applicationContext, Context context, Task.Info taskInfo, Throwable throwable, TLogger logger);

}
