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

package sviolet.turquoise.x.imageloader.drawable.def;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import sviolet.turquoise.x.imageloader.drawable.BackgroundDrawableFactory;

/**
 * Created by S.Violet on 2016/3/17.
 */
public class DefaultBackgroundDrawableFactory implements BackgroundDrawableFactory {

    public static final int DEFAULT_BACKGROUND_COLOR = 0x00000000;

    private int backgroundImageResId = -1;
    private int backgroundColor = DEFAULT_BACKGROUND_COLOR;

    @Override
    public Drawable create(int loadingBackgroundColor, Bitmap loadingBitmap) {
        return null;
    }

    @Override
    public void onDestroy() {

    }

    public void setBackgroundImageResId(int backgroundImageResId) {
        this.backgroundImageResId = backgroundImageResId;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
