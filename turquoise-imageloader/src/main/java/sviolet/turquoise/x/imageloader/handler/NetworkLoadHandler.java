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

import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.IndispensableState;
import sviolet.turquoise.x.imageloader.entity.LowNetworkSpeedStrategy;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.disk.DiskCacheServer;

/**
 * <p>Implement network load logic</p>
 *
 * <p>It is recommended to implement {@link sviolet.turquoise.x.imageloader.handler.common.AbstractNetworkLoadHandler},
 * this interface is for highly customized.</p>
 *
 * @author S.Violet
 */
public interface NetworkLoadHandler {

    /**
     * <p>Implement network load logic</p>
     *
     * <p>It is recommended to implement {@link sviolet.turquoise.x.imageloader.handler.common.AbstractNetworkLoadHandler},
     * this interface is for highly customized.</p>
     *
     * @param applicationContext application context
     * @param context context
     * @param writerProvider get OutputStream or RandomAccessFile, for write data to cache file
     * @param taskInfo task info
     * @param indispensableState indispensable state
     * @param lowNetworkSpeedConfig low network speed config
     * @param connectTimeout connect timeout
     * @param readTimeout read timeout
     * @param imageDataLengthLimit image data length limit
     * @param exceptionHandler exception handler
     * @param logger logger
     * @return handle result
     */
    HandleResult onHandle(
            Context applicationContext,
            Context context,
            DiskCacheServer.WriterProvider writerProvider,
            Task.Info taskInfo,
            IndispensableState indispensableState,
            LowNetworkSpeedStrategy.Configure lowNetworkSpeedConfig,
            long connectTimeout,
            long readTimeout,
            long imageDataLengthLimit,
            ExceptionHandler exceptionHandler,
            TLogger logger);

    /**
     * NetworkLoadHandler handle result
     */
    enum HandleResult {
        /**
         * load succeed (to file)
         */
        SUCCEED,
        /**
         * load failed
         */
        FAILED,
        /**
         * load canceled
         */
        CANCELED
    }

}
