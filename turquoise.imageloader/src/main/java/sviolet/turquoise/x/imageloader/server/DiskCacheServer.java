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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.model.cache.DiskLruCache;
import sviolet.turquoise.util.droid.ApplicationUtils;
import sviolet.turquoise.x.imageloader.ComponentManager;

/**
 *
 * Created by S.Violet on 2016/3/22.
 */
public class DiskCacheServer implements ComponentManager.Component, Server {

    private static final int DEFAULT_APP_VERSION = 1;

    private ComponentManager manager;

    private int appVersion = DEFAULT_APP_VERSION;

    private DiskLruCache diskLruCache;
    private Status status = Status.UNINITIALIZED;
    private boolean isHealthy = true;
    private int holdCounter = 0;

    private ReentrantLock statusLock = new ReentrantLock();

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
        if (manager.getServerSettings().isWipeDiskCacheWhenUpdate() && manager.getApplicationContextImage() != null){
            this.appVersion = ApplicationUtils.getAppVersion(manager.getApplicationContextImage());
        }
        status = Status.PAUSE;
    }

    private boolean openCache(){
        Exception commonException = null;
        Exception openException = null;
        try{
            statusLock.lock();
            switch (status){
                case UNINITIALIZED:
                    commonException = new RuntimeException("[TILoader:DiskCacheServer]can not use disk cache before initialize");
                    break;
                case PAUSE:
                    holdCounter++;
                    try {
                        diskLruCache = DiskLruCache.open(manager.getServerSettings().getDiskCachePath(), appVersion, 1, manager.getServerSettings().getDiskCacheSize());
                        status = Status.READY;
                        return true;
                    } catch (IOException e) {
                        status = Status.DISABLE;
                        openException = e;
                    }
                    break;
                case READY:
                    holdCounter++;
                    return true;
                case DISABLE:
                default:
                    commonException = new RuntimeException("[TILoader:DiskCacheServer]can not use disk cache which has been disabled");
                    break;
            }
        }finally {
            statusLock.unlock();
        }
        if (openException != null){
            manager.getServerSettings().getExceptionHandler().onDiskCacheCommonException(manager.getContextImage(), openException);
        }
        if (commonException != null){
            manager.getServerSettings().getExceptionHandler().onDiskCacheCommonException(manager.getContextImage(), commonException);
        }
        return false;
    }

    private void closeCache(){
        DiskLruCache diskLruCacheToClose = null;
        try{
            statusLock.lock();
            if (status == Status.READY && holdCounter <= 0){
                diskLruCacheToClose = this.diskLruCache;
                this.diskLruCache = null;
                status = Status.PAUSE;
                holdCounter = 0;
            }
        }finally {
            statusLock.unlock();
        }
        if (diskLruCacheToClose != null) {
            try {
                diskLruCacheToClose.close();
            } catch (IOException e) {
                manager.getServerSettings().getExceptionHandler().onDiskCacheCommonException(manager.getContextImage(), e);
            }
        }
    }

    public File get(String sourceKey){
        if (openCache()){
            try{
                return diskLruCache.getFile(sourceKey, 0);
            } catch (IOException e) {
                manager.getServerSettings().getExceptionHandler().onDiskCacheLoadException(manager.getContextImage(), e);
            }
        }
        return null;
    }

    public DiskLruCache.Editor edit(String sourceKey){
        if (openCache()){
            try{
                return diskLruCache.edit(sourceKey);
            } catch (IOException e) {
                manager.getServerSettings().getExceptionHandler().onDiskCacheLoadException(manager.getContextImage(), e);
            }
        }
        return null;
    }

    public void release(){
        try {
            DiskLruCache diskLruCacheToFlush = this.diskLruCache;
            if (diskLruCacheToFlush != null)
                diskLruCacheToFlush.flush();
        } catch (IOException e) {
            manager.getServerSettings().getExceptionHandler().onDiskCacheCommonException(manager.getContextImage(), e);
        }
        try{
            statusLock.lock();
            holdCounter--;
            if (holdCounter < 0)
                holdCounter = 0;
        }finally {
            statusLock.unlock();
        }
    }

    @Override
    public Type getServerType() {
        return Type.DISK_CACHE;
    }

    public enum Status{
        UNINITIALIZED,
        PAUSE,
        READY,
        DISABLE
    }

}
