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

package sviolet.turquoise.x.imageloader.drawable;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * <p>create background of Loaded Image</p>
 *
 * Created by S.Violet on 2016/3/16.
 */
public interface BackgroundDrawableFactory {

    /**
     * <p>create background of Loaded Image</p>
     *
     * <p>it will show after Image loaded, and then gradually disappear. The drawable is used for
     * TransitionDrawable's layer, and it will continue to use color/bitmap of loading state.</p>
     *
     * @param loadingBackgroundColor background color of loading state
     * @param loadingBitmap bitmap of loading state
     * @return Drawable
     */
    Drawable create(int loadingBackgroundColor, Bitmap loadingBitmap);

}
