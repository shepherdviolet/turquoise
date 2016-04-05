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
 *
 * Created by S.Violet on 2016/2/19.
 */
public abstract class DecodeHandler {

    public abstract ImageResource<?> onDecode(Context applicationContext, Context context, Task.Info taskInfo, byte[] data, TLogger logger);

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

    private ImageResource<?> intercept(Context applicationContext, Context context, Task task, TLogger logger, ImageResource<?> imageResource) {
        //interceptor
        if (imageResource != null && task.getParams().getDecodeInterceptor() != null){
            ImageResource<?> imageResource2 = task.getParams().getDecodeInterceptor().intercept(applicationContext, context, task.getTaskInfo(), imageResource, logger);
            //recycle previous ImageResource
            if (!TILoaderUtils.isImageResourceEqual(imageResource, imageResource2)){
                TILoaderUtils.recycleImageResource(imageResource);
            }
            imageResource = imageResource2;
        }
        return imageResource;
    }

    public static interface Interceptor{

        ImageResource<?> intercept(Context applicationContext, Context context, Task.Info taskInfo, ImageResource<?> imageResource, TLogger logger);

    }

}
