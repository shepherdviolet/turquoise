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

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import pl.droidsonroids.gif.GifDrawable;

/**
 *
 *
 * Created by S.Violet on 2016/5/4.
 */
public class EnhancedGifDrawable extends GifDrawable {

    private boolean skipException = false;
    private int fixedWidth = Integer.MIN_VALUE;
    private int fixedHeight = Integer.MIN_VALUE;

    public EnhancedGifDrawable(@NonNull Resources res, @DrawableRes @RawRes int id) throws Resources.NotFoundException, IOException {
        super(res, id);
    }

    public EnhancedGifDrawable(@NonNull AssetManager assets, @NonNull String assetName) throws IOException {
        super(assets, assetName);
    }

    public EnhancedGifDrawable(@NonNull String filePath) throws IOException {
        super(filePath);
    }

    public EnhancedGifDrawable(@NonNull File file) throws IOException {
        super(file);
    }

    public EnhancedGifDrawable(@NonNull InputStream stream) throws IOException {
        super(stream);
    }

    public EnhancedGifDrawable(@NonNull AssetFileDescriptor afd) throws IOException {
        super(afd);
    }

    public EnhancedGifDrawable(@NonNull FileDescriptor fd) throws IOException {
        super(fd);
    }

    public EnhancedGifDrawable(@NonNull byte[] bytes) throws IOException {
        super(bytes);
    }

    public EnhancedGifDrawable(@NonNull ByteBuffer buffer) throws IOException {
        super(buffer);
    }

    public EnhancedGifDrawable(@Nullable ContentResolver resolver, @NonNull Uri uri) throws IOException {
        super(resolver, uri);
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

}
