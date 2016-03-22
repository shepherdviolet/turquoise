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

import android.content.Context;
import android.graphics.Bitmap;

import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.server.module.BitmapMemoryCacheModule;

/**
 * <p>manage all memory caches</p>
 *
 * Created by S.Violet on 2016/3/15.
 */
public class MemoryCacheServer implements ComponentManager.Component, Server {

    private static final int MIN_MEMORY_CACHE_SIZE = 1024 * 1024 * 5;//minimum of memory Cache Size
    private static final float DEFAULT_MEMORY_CACHE_PERCENT = 0.25f;//default percent

    private ComponentManager manager;

    private BitmapMemoryCacheModule bitmapMemoryCacheModule;
//    private OtherCache otherCache;

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
        initBitmapCache();
    }

    private void initBitmapCache() {
        int memoryCacheSize = manager.getServerSettings().getMemoryCacheSize();
        //try to use default value if memoryCacheSize <= 0
        if (memoryCacheSize <= 0){
            final Context contextImage = manager.getApplicationContextImage();
            if (contextImage != null){
                final int memoryClass = DeviceUtils.getMemoryClass(contextImage);
                memoryCacheSize = (int) (1024 * 1024 * memoryClass * DEFAULT_MEMORY_CACHE_PERCENT);
            }
        }
        //limit
        if (memoryCacheSize < MIN_MEMORY_CACHE_SIZE){
            memoryCacheSize = MIN_MEMORY_CACHE_SIZE;
        }
        bitmapMemoryCacheModule = new BitmapMemoryCacheModule(memoryCacheSize, manager.getLogger());
    }

    public void put(String key, ImageResource<?> resource){
        if (key == null){
            manager.getLogger().e("MemoryCacheServer can't put with null key");
            return;
        }
        if (resource == null || resource.getType() == null || resource.getResource() == null){
            manager.getLogger().e("MemoryCacheServer can't put with null or illegal resource");
            return;
        }
        switch (resource.getType()){
            case BITMAP:
                if (resource.getResource() instanceof Bitmap) {
                    bitmapMemoryCacheModule.put(key, (Bitmap) resource.getResource());
                    //otherCache.remove(key);//remove from other cache
                }else{
                    throw new RuntimeException("[TILoader:MemoryCacheServer]illegal ImageResource, type:<" + resource.getType().toString() + ">, resource:<" + resource.getResource().getClass().getName() + ">");
                }
                break;
//            case OTHER:
//                otherCache.put(key, (Bitmap) resource.getResource());
//                bitmapMemoryCacheModule.remove(key);//remove from other cache
//                break;
            default:
                manager.getLogger().e("MemoryCacheServer can't put this kind of ImageResource, type:<" + resource.getType().toString() + ">");
                break;
        }
    }

    public ImageResource<?> get(String key){
        if (key == null){
            manager.getLogger().e("MemoryCacheServer can't get with null key");
            return null;
        }
        Object resource = null;
        resource = bitmapMemoryCacheModule.get(key);
        if (resource != null){
            return new ImageResource<>(ImageResource.Type.BITMAP, resource);
        }
//        resource = otherCache.get(key);
//        if (resource != null){
//            return new ImageResource<>(ImageResource.Type.OTHER, resource);
//        }
        return null;
    }

    public void remove(String key){
        if (key == null){
            manager.getLogger().e("MemoryCacheServer can't remove with null key");
            return;
        }
        bitmapMemoryCacheModule.remove(key);
//        otherCache.remove(key);
    }

    public void removeAll(){
        bitmapMemoryCacheModule.removeAll();
//        otherCache.removeAll();
    }

    @Override
    public Type getServerType() {
        return Type.MEMORY_CACHE;
    }
}
