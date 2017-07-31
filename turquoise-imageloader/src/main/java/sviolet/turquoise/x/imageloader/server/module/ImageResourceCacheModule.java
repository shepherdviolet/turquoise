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

package sviolet.turquoise.x.imageloader.server.module;

import android.annotation.SuppressLint;

import java.util.Map;

import sviolet.thistle.common.compat.CompatLruCache;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.ImageResourceHandler;

/**
 * <p>ImageResource cache for TILoader</p>
 *
 * Created by S.Violet on 2016/3/15.
 */
public class ImageResourceCacheModule extends CompatLruCache<String, ImageResource> {

    private ImageResourceHandler imageResourceHandler;
    private TLogger logger;

    /**
     * @param cacheSize cache size
     * @param logger logger
     */
    public ImageResourceCacheModule(int cacheSize, ImageResourceHandler imageResourceHandler, TLogger logger) {
        super(cacheSize);
        this.imageResourceHandler = imageResourceHandler;
        this.logger = logger;
    }

    /*****************************************************************************
     * function
     */

    @Override
    public ImageResource get(String key) {
        if (key == null){
            throw new NullPointerException("[TILoader:ImageResourceCacheModule]key must not be null");
        }
        //return ImageResource
        ImageResource imageResource = super.get(key);
        //exclude invalid ImageResource
        if (!imageResourceHandler.isValid(imageResource)){
            remove(key);
            return null;
        }
        return imageResource;
    }

    @Override
    public ImageResource put(String key, ImageResource value) {
        if (key == null){
            throw new NullPointerException("[TILoader:ImageResourceCacheModule]key must not be null");
        }
        //exclude invalid ImageResource
        if (!imageResourceHandler.isValid(value)) {
            logger.e("ImageResourceCacheModule trying to put an invalid ImageResource, key:" + key);
            return null;
        }
        //remove previous ImageResource with same key
        remove(key);
        return super.put(key, value);
    }

    public ImageResource extract(String key){
        if (key == null){
            throw new NullPointerException("[TILoader:ImageResourceCacheModule]key must not be null");
        }
        return super.remove(key);
    }

    @Override
    public ImageResource remove(String key) {
        if (key == null){
            throw new NullPointerException("[TILoader:ImageResourceCacheModule]key must not be null");
        }
        ImageResource imageResource = super.remove(key);
        //recycle
        imageResourceHandler.recycle(imageResource);
        //return null
        return null;
    }

    public void removeAll() {
        int counter = 0;

        //recycle all
        for (Map.Entry<String, ImageResource> entry : getMap().entrySet()) {
            ImageResource imageResource = entry.getValue();
            if (imageResourceHandler.recycle(imageResource)) {
                counter++;
            }
        }

        //clean
        getMap().clear();
        setSize(0);

        //打印日志
        logger.d("[ImageResourceCacheModule]removeAll recycled:" + counter);
        logger.d(getMemoryReport());
    }

    public String getMemoryReport() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[ImageResourceCacheModule]MemoryReport: ");
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
            ImageResource value;

            synchronized (this) {
                if (size() < 0 || (getMap().isEmpty() && size() != 0)) {
                    throw new IllegalStateException("[TILoader:ImageResourceCacheModule]byteCountOf: is reporting inconsistent results!");
                }

                if (size() <= maxSize) {
                    break;
                }

                Map.Entry<String, ImageResource> toEvict = null;

                //get earliest item
                for (Map.Entry<String, ImageResource> entry : getMap().entrySet()) {
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
            imageResourceHandler.recycle(value);
        }
        //打印内存使用情况
        logger.d(getMemoryReport());
    }

    @SuppressLint("NewApi")
    @Override
    protected int sizeOf(String key, ImageResource value) {
        return imageResourceHandler.byteCountOf(value);
    }

}
