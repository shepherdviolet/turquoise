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

import java.io.File;

import sviolet.turquoise.util.common.DateTimeUtilsForAndroid;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.TILoaderUtils;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>implement image decoding</p>
 *
 * Created by S.Violet on 2016/2/19.
 */
public abstract class DecodeHandler {

    /**
     * <p>To override reqWidth/reqHeight while decoding. DecodeHandler will use custom size to decode
     * and scale image.</p>
     *
     * <p>TYPE::Integer</p>
     *
     * <pre>{@code
     *         Map<String, Object> extras = new HashMap<>();
     *         extras.put(DecodeHandler.CUSTOM_REQ_WIDTH, 100);
     *         extras.put(DecodeHandler.CUSTOM_REQ_HEIGHT, 100);
     *         Params paramsDemo = new Params.Builder()
     *              .setExtras(extras)
     *              .build();
     * }</pre>
     */
    public static final String CUSTOM_REQ_WIDTH = "DecodeHandler_custom_req_width";
    public static final String CUSTOM_REQ_HEIGHT = "DecodeHandler_custom_req_height";

    //method//////////////////////////////////////////////////////////////////////////////////////////

    /**
     * decode image from bytes
     * @param applicationContext applicationContext
     * @param context activity context, might be null
     * @param taskInfo task info
     * @param data date of image
     * @param logger logger
     * @return ImageResource
     */
    public abstract ImageResource onDecode(Context applicationContext, Context context, Task.Info taskInfo, DecodeType decodeType, Object data, TLogger logger);

    public final ImageResource decode(Context applicationContext, Context context, Task task, DecodeType decodeType, Object data, TLogger logger){
        ImageResource imageResource = onDecode(applicationContext, context, task.getTaskInfo(), decodeType, data, logger);
        imageResource = intercept(applicationContext, context, task, logger, imageResource);
        return imageResource;
    }

    /**
     * intercept process
     */
    private ImageResource intercept(Context applicationContext, Context context, Task task, TLogger logger, ImageResource imageResource) {
        //interceptor
        if (task.getParams().getDecodeInterceptor() != null && TILoaderUtils.isImageResourceValid(imageResource)){
            long startTime = DateTimeUtilsForAndroid.getUptimeMillis();
            ImageResource imageResource2 = task.getParams().getDecodeInterceptor().intercept(applicationContext, context, task.getTaskInfo(), imageResource, logger);
            //recycle previous ImageResource
            if (!TILoaderUtils.isImageResourceEqual(imageResource, imageResource2)){
                TILoaderUtils.recycleImageResource(imageResource);
            }
            imageResource = imageResource2;
            logger.d("[DecodeHandler]decode interceptor elapse:" + (DateTimeUtilsForAndroid.getUptimeMillis() - startTime) + ", task:" + task);
        }
        return imageResource;
    }

    /**
     * process after DecodeHandler.onDecode()
     */
    public interface Interceptor{

        ImageResource intercept(Context applicationContext, Context context, Task.Info taskInfo, ImageResource imageResource, TLogger logger);

    }

    public enum DecodeType {
        /**
         * decode byte[]
         */
        BYTES,
        /**
         * decode File
         */
        FILE,
        /**
         * decode resources in apk (int resId)
         */
        RES,
        /**
         * decode assets in apk (String assetsPath)
         */
        ASSETS,
        /**
         * generate qr-code image by url value (String value)
         */
        QR_CODE
    }

    /**
     * <p>There are two steps in DecodeHandler: decode and scale.</p>
     *
     * <p>Step 1: Decode........................................................................</p>
     *
     * <p>Decode image from file/bytes, keep aspect ratio. The inSampleSize is calculated according
     * to the reqWidth/reqHeight, and you will get smaller image which take up less memory. In general,
     * we use InSampleQuality.MEDIUM option.</p>
     *
     * Params->decodeInSampleQuality:<br/>
     * BitmapUtils.InSampleQuality.ORIGINAL: decode into original size (highest quality), take up more memory.<br/>
     * BitmapUtils.InSampleQuality.HIGH: calculate appropriate inSampleSize (higher quality) and decode into smaller image, take up less memory.<br/>
     * BitmapUtils.InSampleQuality.MEDIUM: calculate appropriate inSampleSize (medium quality) and decode into smaller image, take up less memory, default option<br/>
     * BitmapUtils.InSampleQuality.LOW: calculate appropriate inSampleSize (low quality) and decode into smaller image, take up less memory.<br/>
     *
     * <p>Step 2: Scale........................................................................</p>
     *
     * <p>Scale image to specified size. In general, we use DecodeScaleStrategy.NO_SCALE.</p>
     *
     * NO_SCALE::do not scale, keep decoded size, keep aspect ratio, DEFAULT option.<br/>
     * SCALE_FIT_WIDTH_HEIGHT:scale image to reqWidth * reqHeight, ignore aspect ratio.<br/>
     * SCALE_FIT_WIDTH:scale image's width to reqWidth, keep aspect ratio<br/>
     * SCALE_FIT_HEIGHT:scale image's height to reqHeight, keep aspect ratio<br/>
     *
     */
    public enum DecodeScaleStrategy {
        NO_SCALE,
        SCALE_FIT_WIDTH_HEIGHT,
        SCALE_FIT_WIDTH,
        SCALE_FIT_HEIGHT
    }

}
