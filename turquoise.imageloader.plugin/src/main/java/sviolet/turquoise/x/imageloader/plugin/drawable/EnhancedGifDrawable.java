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

package sviolet.turquoise.x.imageloader.plugin.drawable;

import android.graphics.Canvas;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;

/**
 *
 *
 * Created by S.Violet on 2016/5/4.
 */
public class EnhancedGifDrawable extends GifDrawable {

    private boolean bytesDataMode = false;
    private boolean skipException = false;
    private int fixedWidth = Integer.MIN_VALUE;
    private int fixedHeight = Integer.MIN_VALUE;

    public EnhancedGifDrawable(@NonNull File file) throws IOException {
        super(file);
    }

    public EnhancedGifDrawable(@NonNull byte[] bytes) throws IOException {
        super(bytes);
        bytesDataMode = true;
    }

    @Override
    public void draw(Canvas canvas) {
        try {
            super.draw(canvas);
            //throw exception manually when GifDrawable is recycled
            if (isRecycled()) {
                throw new RuntimeException("[EnhancedGifDrawable]draw: GifDrawable is recycled");
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
    public EnhancedGifDrawable setMatchParent(boolean matchParent){
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
    public EnhancedGifDrawable setSkipException(boolean skipException){
        this.skipException = skipException;
        return this;
    }

    /**
     * set fixed size
     * @param width width
     * @param height height
     */
    public EnhancedGifDrawable setFixedSize(int width, int height){
        this.fixedWidth = width;
        this.fixedHeight = height;
        return this;
    }

    public long getByteCount(){
        if (bytesDataMode) {
            //include input source byte count in bytesDataMode
            return getInputSourceByteCount() + getAllocationByteCount();
        }else{
            return getAllocationByteCount();
        }
    }

}
