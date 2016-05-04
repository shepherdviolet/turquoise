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

package sviolet.turquoise.x.imageloader.plugin.handler;

import android.content.Context;
import android.graphics.drawable.Drawable;

import pl.droidsonroids.gif.GifDrawable;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.common.CommonImageResourceHandler;
import sviolet.turquoise.x.imageloader.plugin.drawable.EnhancedGifDrawable;

/**
 * <p>Enhanced ImageResourceHandler</p>
 *
 * <p>1.Add support for GIF</p>
 *
 * Created by S.Violet on 2016/5/4.
 */
public class EnhancedImageResourceHandler extends CommonImageResourceHandler {

    @Override
    protected boolean isValidExtension(ImageResource<?> resource) {
        switch (resource.getType()){
            case GIF:
                Object res = resource.getResource();
                if (res instanceof GifDrawable){
                    return !((GifDrawable) res).isRecycled();
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean recycleExtension(ImageResource<?> resource) {
        switch (resource.getType()){
            case GIF:
                Object res = resource.getResource();
                if ((res instanceof GifDrawable) && (!((GifDrawable) res).isRecycled())){
                    ((GifDrawable) res).recycle();
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public Drawable toDrawableExtension(Context applicationContext, ImageResource<?> resource, boolean skipDrawingException) {
        switch (resource.getType()){
            case GIF:
                Object res = resource.getResource();
                if ((res instanceof EnhancedGifDrawable) && (!((EnhancedGifDrawable) res).isRecycled())){
                    return ((EnhancedGifDrawable) res).setSkipException(skipDrawingException);
                }
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public int byteCountOfExtension(ImageResource<?> resource) {
        switch (resource.getType()){
            case GIF:
                Object res = resource.getResource();
                if ((res instanceof EnhancedGifDrawable) && (!((EnhancedGifDrawable) res).isRecycled())){
                    return (int) ((EnhancedGifDrawable) res).getByteCount();
                }
                break;
            default:
                break;
        }
        return 0;
    }
}
