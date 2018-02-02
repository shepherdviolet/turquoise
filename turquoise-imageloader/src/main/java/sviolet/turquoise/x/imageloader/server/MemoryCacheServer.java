/*
 * Copyright (C) 2015-2017 S.Violet
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

import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.server.module.ImageResourceCacheModule;

/**
 * <p>Manage memory caches (TILoader inner memory cache)</p>
 *
 * Created by S.Violet on 2016/3/15.
 */
public class MemoryCacheServer implements ComponentManager.Component, Server {

    public static final int MIN_MEMORY_CACHE_SIZE = 1024 * 1024 * 2;//minimum of memory Cache Size
    public static final float DEFAULT_MEMORY_CACHE_PERCENT = 0.10f;//default percent

    private ComponentManager manager;

    private ImageResourceCacheModule imageResourceCacheModule;

    private boolean initialized = false;

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
    }

    private void initialize() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized){
                    int memoryCacheSize = manager.getServerSettings().getMemoryCacheSize();
                    //try to use default value if memoryCacheSize <= 0
                    if (memoryCacheSize <= 0){
                        final int memoryClass = DeviceUtils.getMemoryClass(manager.getApplicationContextImage());
                        memoryCacheSize = (int) (1024 * 1024 * memoryClass * DEFAULT_MEMORY_CACHE_PERCENT);
                        manager.getLogger().i("[TILoader:MemoryCacheServer]initialize, calculate default memoryCacheSize");
                    }
                    //limit
                    if (DeviceUtils.getVersionSDK() < 11){
                        manager.getLogger().i("[TILoader:MemoryCacheServer]initialize, API < 11, reset to minimumSize:" + (MIN_MEMORY_CACHE_SIZE / 1024) + "K");
                        memoryCacheSize = MIN_MEMORY_CACHE_SIZE;
                    } else if (memoryCacheSize < MIN_MEMORY_CACHE_SIZE){
                        manager.getLogger().i("[TILoader:MemoryCacheServer]initialize, setting memoryCacheSize:" + (memoryCacheSize / 1024) + "K < minimumSize, reset to minimumSize:" + (MIN_MEMORY_CACHE_SIZE / 1024) + "K");
                        memoryCacheSize = MIN_MEMORY_CACHE_SIZE;
                    }
                    imageResourceCacheModule = new ImageResourceCacheModule(memoryCacheSize, manager.getServerSettings().getImageResourceHandler(), manager.getLogger());
                    manager.getLogger().i("[TILoader:MemoryCacheServer]initialized, memoryCacheSize:" + (memoryCacheSize / 1024) + "K");
                    initialized = true;
                }
            }
        }
    }

    public void put(String key, ImageResource resource){
        initialize();
        if (key == null){
            manager.getLogger().e("MemoryCacheServer can't put with null key");
            return;
        }
        imageResourceCacheModule.put(key, resource);
    }

    public ImageResource get(String key){
        initialize();
        if (key == null){
            manager.getLogger().e("MemoryCacheServer can't get with null key");
            return null;
        }
        return imageResourceCacheModule.get(key);
    }

    public ImageResource extract(String key){
        initialize();
        if (key == null){
            manager.getLogger().e("MemoryCacheServer can't extract with null key");
            return null;
        }
        return imageResourceCacheModule.extract(key);
    }

    public void remove(String key){
        initialize();
        if (key == null){
            manager.getLogger().e("MemoryCacheServer can't remove with null key");
            return;
        }
        imageResourceCacheModule.remove(key);
    }

    public void removeAll(){
        initialize();
        imageResourceCacheModule.removeAll();
    }

    public String getMemoryReport() {
        initialize();
        return imageResourceCacheModule.getMemoryReport();
    }

    @Override
    public Type getServerType() {
        return Type.MEMORY_CACHE;
    }
}
