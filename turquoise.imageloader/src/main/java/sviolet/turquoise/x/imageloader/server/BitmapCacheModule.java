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

package sviolet.turquoise.x.imageloader.server;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import java.util.Map;

import sviolet.turquoise.common.compat.CompatLruCache;
import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * <p>bitmap cache for TILoader</p>
 *
 * Created by S.Violet on 2016/3/15.
 */
class BitmapCacheModule extends CompatLruCache<String, Bitmap> {

    private TLogger logger;

    /**
     * @param cacheSize cache size
     * @param logger logger
     */
    BitmapCacheModule(int cacheSize, TLogger logger) {
        super(cacheSize);
    }

    /*****************************************************************************
     * function
     */

    @Override
    public Bitmap get(String key) {
        if (key == null){
            throw new NullPointerException("[TILoader:BitmapCacheModule]key must not be null");
        }
        //return bitmap
        Bitmap bitmap = super.get(key);
        //exclude recycled bitmap
        if (bitmap != null && bitmap.isRecycled()){
            remove(key);
            return null;
        }
        return bitmap;
    }

    @Override
    public Bitmap put(String key, Bitmap value) {
        if (key == null){
            throw new NullPointerException("[TILoader:BitmapCacheModule]key must not be null");
        }
        //exclude null or recycled bitmap
        if (value == null || value.isRecycled()) {
            logger.e("BitmapCacheModule trying to put a null or recycled bitmap, key:" + key);
            return null;
        }
        //remove previous bitmap with same key
        remove(key);
        return super.put(key, value);
    }

    @Override
    public Bitmap remove(String key) {
        if (key == null){
            throw new NullPointerException("[TILoader:BitmapCacheModule]key must not be null");
        }
        Bitmap bitmap = super.remove(key);
        //recycle
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        //return null
        return null;
    }

    public void removeAll() {
        int counter = 0;

        //recycle all
        for (Map.Entry<String, Bitmap> entry : getMap().entrySet()) {
            Bitmap bitmap = entry.getValue();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                counter++;
            }
        }

        //clean
        getMap().clear();
        setSize(0);

        //打印日志
        logger.d("BitmapCacheModule removeAll recycled:" + counter);
        logger.d(getMemoryReport());
    }

    public String getMemoryReport() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("MemoryReport: ");
        stringBuilder.append("[Cache]: ");
        stringBuilder.append(size() / 1024);
        stringBuilder.append("K/");
        stringBuilder.append(maxSize() / 1024);
        stringBuilder.append("K ");
        stringBuilder.append(quantity());
        stringBuilder.append("pcs ");
        return stringBuilder.toString();
    }

    /******************************************************
     * override
     */

    @Override
    protected void trimToSize(int maxSize) {
        while (true) {
            String key;
            Bitmap value;

            synchronized (this) {
                if (size() < 0 || (getMap().isEmpty() && size() != 0)) {
                    throw new IllegalStateException("[TILoader:BitmapCacheModule]sizeOf: is reporting inconsistent results!");
                }

                if (size() <= maxSize) {
                    break;
                }

                Map.Entry<String, Bitmap> toEvict = null;

                //get earliest item
                for (Map.Entry<String, Bitmap> entry : getMap().entrySet()) {
                    toEvict = entry;
                    break;
                }

                if (toEvict == null) {
                    break;
                }

                key = toEvict.getKey();
                value = toEvict.getValue();

                setSize(size() - safeSizeOf(key, value));
                getMap().remove(key);
                setEvictionCount(getEvictionCount() + 1);
            }

            entryRemoved(true, key, value, null);

            //recycle
            if (value != null && !value.isRecycled()) {
                value.recycle();
            }
        }
        //打印内存使用情况
        logger.d(getMemoryReport());
    }

    @SuppressLint("NewApi")
    @Override
    protected int sizeOf(String key, Bitmap value) {
        if (value == null || value.isRecycled())
            return 0;
        //calculate
        if (DeviceUtils.getVersionSDK() >= 12) {
            return value.getByteCount();
        }
        return value.getRowBytes() * value.getHeight();
    }

}
