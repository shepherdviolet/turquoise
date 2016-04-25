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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import java.io.InputStream;

import sviolet.turquoise.x.imageloader.TILoader;

/**
 * <p>special BitmapDrawable for TILoader</p>
 *
 * <p>you must use this {@link TIBitmapDrawable} instead of {@link BitmapDrawable}
 * to implements {@link LoadingDrawableFactory}/{@link BackgroundDrawableFactory}/{@link FailedDrawableFactory}</p>
 *
 * <p>{@link TIBitmapDrawable} will throw exceptions if Bitmap has bean recycled anyway. I found that some Device will
 * not throw exceptions in this case when using {@link BitmapDrawable}, such as MIUI. {@link TILoader} will reload
 * image when catch an exception while drawable drawing, so you must use this {@link TIBitmapDrawable} instead of
 * {@link BitmapDrawable}.</p>
 *
 * Created by S.Violet on 2016/4/13.
 */
public class TIBitmapDrawable extends BitmapDrawable {

    private boolean skipException = false;
    private int fixedWidth = Integer.MIN_VALUE;
    private int fixedHeight = Integer.MIN_VALUE;

//    public TIBitmapDrawable() {
//    }

//    public TIBitmapDrawable(Resources res) {
//        super(res);
//    }

//    public TIBitmapDrawable(Bitmap bitmap) {
//        super(bitmap);
//    }

    public TIBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

//    public TIBitmapDrawable(String filepath) {
//        super(filepath);
//    }

    public TIBitmapDrawable(Resources res, String filepath) {
        super(res, filepath);
    }

//    public TIBitmapDrawable(InputStream is) {
//        super(is);
//    }

    public TIBitmapDrawable(Resources res, InputStream is) {
        super(res, is);
    }

    @Override
    public void draw(Canvas canvas) {
        try {
            super.draw(canvas);
            //throw exception manually when bitmap is recycled
            if (getBitmap() != null && getBitmap().isRecycled()) {
                throw new RuntimeException("[TIBitmapDrawable]draw: bitmap is recycled");
            }
        }catch (Exception e){
            if (!skipException){
                throw e;
            }
        }
    }

    @Override
    public int getIntrinsicWidth() {
        if (fixedWidth > Integer.MIN_VALUE)
            return fixedWidth;
        return super.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        if (fixedHeight > Integer.MIN_VALUE)
            return fixedHeight;
        return super.getIntrinsicHeight();
    }

    /**
     * set if match parent
     */
    public TIBitmapDrawable setMatchParent(boolean matchParent){
        if (matchParent) {
            setFixedSize(-1, -1);
        }else{
            setFixedSize(Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
        return this;
    }

    /**
     * set if skip drawing exception
     * @param skipException true:skip drawing exception
     */
    public TIBitmapDrawable setSkipException(boolean skipException){
        this.skipException = skipException;
        return this;
    }

    /**
     * set fixed size
     * @param width width
     * @param height height
     */
    public TIBitmapDrawable setFixedSize(int width, int height){
        this.fixedWidth = width;
        this.fixedHeight = height;
        return this;
    }

}
