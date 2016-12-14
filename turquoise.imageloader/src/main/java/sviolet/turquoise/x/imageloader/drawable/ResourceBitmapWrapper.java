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

import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.util.bitmap.BitmapUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * <p>a wrapper of bitmap decode from resource</p>
 *
 * Created by S.Violet on 2016/4/15.
 */
public class ResourceBitmapWrapper {

    private static final int NORMAL = 0;
    private static final int UNDEFINED = -1;
    private static final int DESTROYED = -2;

    private int state = UNDEFINED;
    private int resId = UNDEFINED;
    private Bitmap bitmap;

    private ReentrantLock lock = new ReentrantLock();

    /**
     * @param resId set bitmap's resId
     */
    public void setResId(int resId) {
        //check input
        if ((resId >>> 24) < 2) {
            throw new RuntimeException("[ResourceBitmapWrapper]The resId must be an application-specific resource id.");
        }
        try {
            lock.lock();
            if (this.state == UNDEFINED){
                this.state = NORMAL;
                this.resId = resId;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param bitmap set bitmap
     */
    public void setBitmap(Bitmap bitmap){
        try {
            lock.lock();
            if (this.state == UNDEFINED){
                this.state = NORMAL;
                this.bitmap = bitmap;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * get bitmap if resId is valid, and the bitmap has not destroyed
     * @param resources Resources
     * @param logger TLogger
     * @return bitmap, null if resId is invalid or bitmap is destroyed
     */
    public Bitmap getBitmap(Resources resources, TLogger logger){
        if (bitmap != null){
            return bitmap;
        }
        try {
            lock.lock();
            if (bitmap != null){
                return bitmap;
            }
            if (state == NORMAL && resId > 0){
                try{
                    //decode from resources
                    bitmap = BitmapUtils.decodeFromResource(resources, resId);
                } catch (Exception e){
                    logger.e("[ResourceBitmapWrapper]decode bitmap from resources failed", e);
                }
                if (bitmap != null){
                    return bitmap;
                }
                state = DESTROYED;
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * destroy bitmap
     */
    public void destroy(){
        Bitmap bitmap = null;
        try {
            lock.lock();
            state = DESTROYED;
            bitmap = this.bitmap;
            this.bitmap = null;
        } finally {
            lock.unlock();
        }
        if (bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
        }
    }

}
