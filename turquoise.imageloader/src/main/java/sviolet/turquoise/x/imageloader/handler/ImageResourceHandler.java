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
import android.graphics.drawable.Drawable;

import sviolet.turquoise.x.imageloader.entity.ImageResource;

/**
 * <p>For processing ImageResource</p>
 *
 * Created by S.Violet on 2016/3/15.
 */
public interface ImageResourceHandler {

    /**
     * if the resource is valid (not null or recycled)
     * @param resource imageResource
     * @return true:valid
     */
    boolean isValid(ImageResource<?> resource);

    /**
     * judge whether the two resource are equal.
     * @param src resource
     * @param dst resource
     * @return true:equal
     */
    boolean isEqual(ImageResource<?> src, ImageResource<?> dst);

    /**
     * recycle imageResource
     * @param resource imageResource which will be recycled
     * @return true:recycled
     */
    boolean recycle(ImageResource<?> resource);

    /**
     * convert ImageResource to Drawable
     * @param applicationContext context
     * @param resource ImageResource
     * @param skipDrawingException true:skip drawing exception of drawable, if true, it will not throw exceptions even when bitmap has recycled
     * @return drawable
     */
    Drawable toDrawable(Context applicationContext, ImageResource<?> resource, boolean skipDrawingException);

    /**
     * copy ImageResource
     * @param resource ImageResource
     * @return new ImageResource with new resource(new Bitmap/...)
     */
    ImageResource<?> copy(ImageResource<?> resource);

    /**
     * get the byte count of ImageResource (memory)
     * @param resource ImageResource
     * @return byte count of ImageResource
     */
    int byteCountOf(ImageResource<?> resource);

}
