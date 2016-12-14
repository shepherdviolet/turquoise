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
import android.graphics.Bitmap;

import java.util.concurrent.atomic.AtomicInteger;

import sviolet.turquoise.util.bitmap.BitmapUtils;
import sviolet.turquoise.util.droid.MeasureUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.EngineCallback;

/**
 * <p>used to emulate net loading</p>
 *
 * Created by S.Violet on 2016/3/22.
 */
public class EmulateNetworkLoadHandler extends CommonNetworkLoadHandler {

    private int[] resIds;
    private long delay;
    private AtomicInteger index = new AtomicInteger(0);

    /**
     * @param resIds picture resource id
     * @param delay load delay, >=100ms
     */
    public EmulateNetworkLoadHandler(int[] resIds, long delay) {
        if (resIds == null || resIds.length <= 0){
            throw new RuntimeException("[EmulateNetworkLoadHandler]resIds is null or empty");
        }
        if (delay < 100){
            throw new RuntimeException("[EmulateNetworkLoadHandler]delay must >= 100ms");
        }

        this.resIds = resIds;
        this.delay = delay;
    }

    @Override
    public void onHandle(Context applicationContext, Context context, Task.Info taskInfo, EngineCallback<Result> callback, long connectTimeout, long readTimeout, TLogger logger) {

        try {
            int currIndex = index.getAndAdd(1) % resIds.length;
            //emulate delay
            Thread.sleep(delay);
            //emulate load
            Bitmap bitmap = BitmapUtils.decodeFromResource(applicationContext.getResources(), resIds[currIndex], MeasureUtils.getScreenWidth(applicationContext), MeasureUtils.getScreenHeight(applicationContext));
            byte[] data = BitmapUtils.bitmapToByteArray(bitmap, Bitmap.CompressFormat.JPEG, 70, true);
            //callback
            callback.setResultSucceed(new Result(data));
        } catch (Exception e) {
            //callback
            callback.setResultFailed(e);
        }

    }
}
