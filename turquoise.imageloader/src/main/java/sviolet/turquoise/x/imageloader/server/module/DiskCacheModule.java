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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.model.cache.DiskLruCache;
import sviolet.turquoise.model.common.LazySingleThreadPool;
import sviolet.turquoise.util.droid.ApplicationUtils;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.Server;

/**
 *
 * Created by S.Violet on 2016/3/22.
 */
public class DiskCacheModule implements ComponentManager.Component, Server {

    public static final int BUFFER_SIZE = 1024;

    private static final int DEFAULT_APP_VERSION = 1;
    private static final long PAUSE_DELAY_NANOS = 60 * 1000000000L;//60s to pause diskcache

    private ComponentManager manager;

    private int appVersion = DEFAULT_APP_VERSION;

    private DiskLruCache diskLruCache;
    private Status status = Status.UNINITIALIZED;
    private AtomicBoolean isHealthy = new AtomicBoolean(true);
    private int holdCounter = 0;

    private LazySingleThreadPool dispatchThreadPool;
    private ReentrantLock statusLock = new ReentrantLock();

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
        this.dispatchThreadPool = new LazySingleThreadPool();
        if (manager.getServerSettings().isWipeDiskCacheWhenUpdate() && manager.getApplicationContextImage() != null){
            this.appVersion = ApplicationUtils.getAppVersion(manager.getApplicationContextImage());
        }
        status = Status.PAUSE;
    }

    /**
     * try to open disk cache if is closed
     * @return true: disk cache ok
     */
    private boolean openCache(){
        Exception commonException = null;
        Exception openException = null;
        try{
            statusLock.lock();
            switch (status){
                case UNINITIALIZED:
                    commonException = new RuntimeException("[TILoader:DiskCacheModule]can not use disk cache before initialize");
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
                    commonException = new RuntimeException("[TILoader:DiskCacheModule]can not use disk cache which has been disabled");
                    break;
            }
        }finally {
            statusLock.unlock();
        }
        if (openException != null){
            manager.getServerSettings().getExceptionHandler().onDiskCacheCommonException(manager.getApplicationContextImage(), manager.getContextImage(), openException, manager.getLogger());
        }
        if (commonException != null){
            manager.getServerSettings().getExceptionHandler().onDiskCacheCommonException(manager.getApplicationContextImage(), manager.getContextImage(), commonException, manager.getLogger());
        }
        return false;
    }

    /**
     * try to close disk cache, release resource
     */
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
                manager.getServerSettings().getExceptionHandler().onDiskCacheCommonException(manager.getApplicationContextImage(), manager.getContextImage(), e, manager.getLogger());
            }
        }
    }

    /**
     * @param task task
     * @return file of image disk cache
     */
    protected File get(Task task){
        if (openCache()){
            try{
                return diskLruCache.getFile(task.getResourceKey(), 0);
            } catch (IOException e) {
                manager.getServerSettings().getExceptionHandler().onDiskCacheReadException(manager.getApplicationContextImage(), manager.getContextImage(), task.getTaskInfo(), e, manager.getLogger());
            }
        }
        return null;
    }

    protected DiskLruCache.Editor edit(Task task){
        if (openCache()){
            try{
                return diskLruCache.edit(task.getResourceKey());
            } catch (IOException e) {
                manager.getServerSettings().getExceptionHandler().onDiskCacheReadException(manager.getApplicationContextImage(), manager.getContextImage(), task.getTaskInfo(), e, manager.getLogger());
            }
        }
        return null;
    }

    /**
     * release holding of disk cache, might have close disk cache
     */
    protected void release(){
        try {
            DiskLruCache diskLruCacheToFlush = this.diskLruCache;
            if (diskLruCacheToFlush != null)
                diskLruCacheToFlush.flush();
        } catch (IOException e) {
            manager.getServerSettings().getExceptionHandler().onDiskCacheCommonException(manager.getApplicationContextImage(), manager.getContextImage(), e, manager.getLogger());
        }
        try{
            statusLock.lock();
            holdCounter--;
            if (holdCounter < 0)
                holdCounter = 0;
        }finally {
            statusLock.unlock();
        }
        //try to pause cache
        dispatchThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                LockSupport.parkNanos(PAUSE_DELAY_NANOS);
                closeCache();
            }
        });
    }

    public boolean isHealthy(){
        return isHealthy.get();
    }

    protected void setHealthy(boolean isHealthy){
        this.isHealthy.set(isHealthy);
    }

    protected ComponentManager getComponentManager(){
        return manager;
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