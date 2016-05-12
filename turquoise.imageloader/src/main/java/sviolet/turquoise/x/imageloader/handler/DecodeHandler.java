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

import java.io.File;

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
     * <p>TYPE::Integer</p>
     * TODO
     */
    public static final String CUSTOM_REQ_WIDTH = "DecodeHandler_custom_req_width";
    public static final String CUSTOM_REQ_HEIGHT = "DecodeHandler_custom_req_height";

    /**
     * decode image from bytes
     * @param applicationContext applicationContext
     * @param context activity context, might be null
     * @param taskInfo task info
     * @param data date of image
     * @param logger logger
     * @return ImageResource
     */
    public abstract ImageResource<?> onDecode(Context applicationContext, Context context, Task.Info taskInfo, byte[] data, TLogger logger);

    /**
     * decode image from file
     * @param applicationContext applicationContext
     * @param context activity context, might be null
     * @param taskInfo task info
     * @param file file of image
     * @param logger logger
     * @return ImageResource
     */
    public abstract ImageResource<?> onDecode(Context applicationContext, Context context, Task.Info taskInfo, File file, TLogger logger);

    public final ImageResource<?> decode(Context applicationContext, Context context, Task task, byte[] data, TLogger logger){
        ImageResource<?> imageResource = onDecode(applicationContext, context, task.getTaskInfo(), data, logger);
        imageResource = intercept(applicationContext, context, task, logger, imageResource);
        return imageResource;
    }

    public final ImageResource<?> decode(Context applicationContext, Context context, Task task, File file, TLogger logger){
        ImageResource<?> imageResource = onDecode(applicationContext, context, task.getTaskInfo(), file, logger);
        imageResource = intercept(applicationContext, context, task, logger, imageResource);
        return imageResource;
    }

    /**
     * intercept process
     */
    private ImageResource<?> intercept(Context applicationContext, Context context, Task task, TLogger logger, ImageResource<?> imageResource) {
        //interceptor
        if (task.getParams().getDecodeInterceptor() != null && TILoaderUtils.isImageResourceValid(imageResource)){
            ImageResource<?> imageResource2 = task.getParams().getDecodeInterceptor().intercept(applicationContext, context, task.getTaskInfo(), imageResource, logger);
            //recycle previous ImageResource
            if (!TILoaderUtils.isImageResourceEqual(imageResource, imageResource2)){
                TILoaderUtils.recycleImageResource(imageResource);
            }
            imageResource = imageResource2;
        }
        return imageResource;
    }

    /**
     * process after DecodeHandler.onDecode()
     */
    public interface Interceptor{

        ImageResource<?> intercept(Context applicationContext, Context context, Task.Info taskInfo, ImageResource<?> imageResource, TLogger logger);

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
