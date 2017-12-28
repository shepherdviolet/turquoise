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

package sviolet.turquoise.x.imageloader.drawable.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
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
 * <p>create drawable for failed status</p>
 *
 * <p>you must use this {@link TIBitmapDrawable} instead of {@link BitmapDrawable}
 * to implements {@link LoadingDrawableFactory}/{@link BackgroundDrawableFactory}/{@link FailedDrawableFactory}</p>
 *
 * <p>implement notes::</p>
 *
 * <p>1.failedDrawable's size match Params->reqWidth/reqHeight, in any case</p>
 *
 * Created by S.Violet on 2016/3/17.
 */
public class CommonFailedDrawableFactory implements FailedDrawableFactory {

    public static final int DEFAULT_COLOR = 0x20000000;

    private ResourceBitmapWrapper bitmap = new ResourceBitmapWrapper();
    private int color = DEFAULT_COLOR;

    @Override
    public Drawable create(Context applicationContext, Context context, Params params, TLogger logger) {
        //size, match Params->reqWidth/reqHeight, in any case
        int drawableWidth = params.getReqWidth();
        int drawableHeight = params.getReqHeight();

        Bitmap bitmap = this.bitmap.getBitmap(applicationContext.getResources(), logger);
        if (bitmap != null && !bitmap.isRecycled()){
            //use TIBitmapDrawable instead of BitmapDrawable
            return new TIBitmapDrawable(applicationContext.getResources(), bitmap).setFixedSize(drawableWidth, drawableHeight);
        }
        return new TIColorDrawable(color).setFixedSize(drawableWidth, drawableHeight);
    }

    @Override
    public void onDestroy() {
        bitmap.destroy();
    }

    public CommonFailedDrawableFactory setImageResId(int resId) {
        this.bitmap.setResId(resId);
        return this;
    }

    /**
     * set loading image
     * @param bitmap bitmap, TILoader will recycle it automatically
     */
    public CommonFailedDrawableFactory setImageBitmap(Bitmap bitmap){
        this.bitmap.setBitmap(bitmap);
        return this;
    }

    public CommonFailedDrawableFactory setColor(int color) {
        this.color = color;
        return this;
    }

}
