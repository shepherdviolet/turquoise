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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;

import sviolet.turquoise.util.common.BitmapUtils;
import sviolet.turquoise.util.droid.DeviceUtils;
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
    public boolean isValid(ImageResource resource) {
        if (resource == null || resource.getResource() == null){
            return false;
        }
        switch (resource.getType()){
            case BITMAP:
                Object res = resource.getResource();
                if (res instanceof Bitmap){
                    return !((Bitmap) res).isRecycled();
                }
                break;
            default:
                return isValidExtension(resource);
        }
        return false;
    }

    protected boolean isValidExtension(ImageResource resource){
        return false;
    }

    @Override
    public boolean isEqual(ImageResource src, ImageResource dst) {
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
    public boolean recycle(ImageResource resource) {
        if (resource == null || resource.getResource() == null){
            return false;
        }
        switch (resource.getType()){
            case BITMAP:
                Object res = resource.getResource();
                if ((res instanceof Bitmap) && (!((Bitmap) res).isRecycled())){
                    ((Bitmap) res).recycle();
                    return true;
                }
                break;
            default:
                return recycleExtension(resource);
        }
        return false;
    }

    public boolean recycleExtension(ImageResource resource) {
        return false;
    }

    @Override
    public Drawable toDrawable(Context applicationContext, ImageResource resource, boolean skipDrawingException) {
        if (resource == null || resource.getResource() == null){
            return null;
        }
        switch (resource.getType()){
            case BITMAP:
                Object res = resource.getResource();
                if ((res instanceof Bitmap) && (!((Bitmap) res).isRecycled())){
                    return new TIBitmapDrawable(applicationContext.getResources(), (Bitmap) res).setSkipException(skipDrawingException);
                }
                break;
            default:
                return toDrawableExtension(applicationContext, resource, skipDrawingException);
        }
        return null;
    }

    public Drawable toDrawableExtension(Context applicationContext, ImageResource resource, boolean skipDrawingException){
        return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public int byteCountOf(ImageResource resource){
        if (resource == null || resource.getResource() == null){
            return 0;
        }
        switch (resource.getType()){
            case BITMAP:
                Object res = resource.getResource();
                if ((res instanceof Bitmap) && (!((Bitmap) res).isRecycled())){
                    //calculate
                    if (DeviceUtils.getVersionSDK() >= 12) {
                        return ((Bitmap) res).getByteCount();
                    }
                    return ((Bitmap) res).getRowBytes() * ((Bitmap) res).getHeight();
                }
                break;
            default:
                return byteCountOfExtension(resource);
        }
        return 0;
    }

    public int byteCountOfExtension(ImageResource resource){
        return 0;
    }

}
