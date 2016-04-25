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

package sviolet.turquoise.x.imageloader.drawable.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.drawable.BackgroundDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.FailedDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.LoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.ResourceBitmapWrapper;
import sviolet.turquoise.x.imageloader.drawable.TIBitmapDrawable;
import sviolet.turquoise.x.imageloader.drawable.TIColorDrawable;
import sviolet.turquoise.x.imageloader.entity.Params;

/**
 *
 * <p>you must use this {@link TIBitmapDrawable} instead of {@link BitmapDrawable}
 * to implements {@link LoadingDrawableFactory}/{@link BackgroundDrawableFactory}/{@link FailedDrawableFactory}</p>
 *
 * Created by S.Violet on 2016/3/17.
 */
public class CommonBackgroundDrawableFactory implements BackgroundDrawableFactory {

    public static final int DEFAULT_BACKGROUND_COLOR = 0x00000000;

    private ResourceBitmapWrapper backgroundBitmap = new ResourceBitmapWrapper();
    private int backgroundColor = DEFAULT_BACKGROUND_COLOR;

    @Override
    public Drawable create(Context applicationContext, Context context, Params params, TLogger logger) {
        //size
        int drawableWidth = -1;
        int drawableHeight = -1;
        if (!params.isSizeMatchView()){
            drawableWidth = params.getReqWidth();
            drawableHeight = params.getReqHeight();
        }

        Bitmap bitmap = backgroundBitmap.getBitmap(applicationContext.getResources(), logger);
        if (bitmap != null && !bitmap.isRecycled()){
            //use TIBitmapDrawable instead of BitmapDrawable
            return new TIBitmapDrawable(applicationContext.getResources(), bitmap).setFixedSize(drawableWidth, drawableHeight);
        }
        return new TIColorDrawable(backgroundColor).setFixedSize(drawableWidth, drawableHeight);
    }

    @Override
    public void onDestroy() {
        backgroundBitmap.destroy();
    }

    @Override
    public void setBackgroundImageResId(int backgroundImageResId) {
        this.backgroundBitmap.setResId(backgroundImageResId);
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
