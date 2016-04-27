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

package sviolet.turquoise.x.imageloader.handler.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import sviolet.turquoise.util.common.BitmapUtils;
import sviolet.turquoise.x.imageloader.drawable.TIBitmapDrawable;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.ImageResourceHandler;

/**
 * <p>common implementation of ImageResourceHandler</p>
 *
 * Created by S.Violet on 2016/3/15.
 */
public class CommonImageResourceHandler implements ImageResourceHandler {

    @Override
    public boolean isValid(ImageResource<?> resource) {
        if (resource == null || resource.getResource() == null){
            return false;
        }
        switch (resource.getType()){
            case BITMAP:
                if (resource.getResource() instanceof Bitmap){
                    return !((Bitmap) resource.getResource()).isRecycled();
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean isEqual(ImageResource<?> src, ImageResource<?> dst) {
        if (src == null && dst == null){
            return true;
        }else if (src == null || dst == null){
            return false;
        }
        Object srcRes = src.getResource();
        Object dstRes = dst.getResource();
        if (srcRes == null && dstRes == null){
            return true;
        }
        return srcRes != null && srcRes.equals(dstRes);
    }

    @Override
    public boolean recycle(ImageResource<?> resource) {
        if (resource == null || resource.getResource() == null){
            return false;
        }
        switch (resource.getType()){
            case BITMAP:
                if ((resource.getResource() instanceof Bitmap) && (!((Bitmap) resource.getResource()).isRecycled())){
                    ((Bitmap) resource.getResource()).recycle();
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public Drawable toDrawable(Context applicationContext, ImageResource<?> resource, boolean skipDrawingException) {
        if (resource == null || resource.getResource() == null){
            return null;
        }
        switch (resource.getType()){
            case BITMAP:
                if ((resource.getResource() instanceof Bitmap) && (!((Bitmap) resource.getResource()).isRecycled())){
                    return new TIBitmapDrawable(applicationContext.getResources(), (Bitmap) resource.getResource()).setSkipException(skipDrawingException);
                }
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public ImageResource<?> copy(ImageResource<?> resource) {
        if (resource == null || resource.getResource() == null){
            return null;
        }
        switch (resource.getType()){
            case BITMAP:
                if ((resource.getResource() instanceof Bitmap) && (!((Bitmap) resource.getResource()).isRecycled())){
                    Bitmap copy = BitmapUtils.copy((Bitmap)resource.getResource(), false);
                    if (copy != null) {
                        return new ImageResource<>(ImageResource.Type.BITMAP, copy);
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }
}
