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

package sviolet.turquoise.x.imageloader.server.module;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.thistle.model.cache.DiskLruCache;
import sviolet.thistle.model.thread.LazySingleThreadPool;
import sviolet.turquoise.util.droid.ApplicationUtils;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.Server;

/**
 * <p>disk cache</p>
 *
 * <p>disk cache will close if server is idle for a long time</p>
 *
 * Created by S.Violet on 2016/3/22.
 */
public class DiskCacheModule implements ComponentManager.Component, Server {

    public static final int BUFFER_SIZE = 8 * 1024;

    private static final int DEFAULT_APP_VERSION = 1;
    private static final long PAUSE_DELAY_NANOS = 20 * 1000000000L;//20s to pause diskCache
    private static final long FAILED_REOPEN_INTERVAL = 10 * 1000L;//10s, reopen if open failed before

    private ComponentManager manager;

    private int appVersion = DEFAULT_APP_VERSION;

    private DiskLruCache diskLruCache;
    private Status status = Status.UNINITIALIZED;
    private AtomicBoolean isHealthy = new AtomicBoolean(true);
    private int holdCounter = 0;
    private long lastOpenFailedTime = 0;//last open failed time

    private LazySingleThreadPool dispatchThreadPool;
    private ReentrantLock statusLock = new ReentrantLock();

    private boolean initialized = false;

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
        this.dispatchThreadPool = new LazySingleThreadPool("TLoader-DiskCacheModule-%d");
        status = Status.PAUSE;
    }

    private void initialize(){
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    if (manager.getServerSettings().isWipeDiskCacheWhenUpdate()){
                        appVersion = ApplicationUtils.getAppVersionCode(manager.getApplicationContextImage());
                    }
                    manager.getLogger().i("[TILoader:DiskCacheServer]initialized, diskCacheSize:" + (manager.getServerSettings().getDiskCacheSize() / 1024) + "K");
                    initialized = true;
                }
            }
        }
    }

    /**
     * try to open disk cache if is closed
     * @return true: disk cache ok
     */
    private boolean openCache(){
        initialize();
        Exception commonException = null;
        Exception openException = null;
        try{
            statusLock.lock();
            holdCounter++;
            switch (status){
                case UNINITIALIZED:
                    commonException = new RuntimeException("[TILoader:DiskCacheModule]can not use disk cache before initialize");
                    break;
                case PAUSE:
                    try {
                        diskLruCache = DiskLruCache.open(manager.getServerSettings().getDiskCachePath(), appVersion, 1, manager.getServerSettings().getDiskCacheSize());
                        status = Status.READY;
                        manager.getLogger().d("[DiskCacheServer]ready");
                        return true;
                    } catch (IOException e) {
                        status = Status.DISABLE;
                        lastOpenFailedTime = System.currentTimeMillis();//record time
                        openException = e;
                    }
                    break;
                case READY:
                    return true;
                case DISABLE:
                    if ((System.currentTimeMillis() - lastOpenFailedTime) < FAILED_REOPEN_INTERVAL) {
                        commonException = new RuntimeException("[TILoader:DiskCacheModule]can not use disk cache which has been disabled (open failed)");
                    }else{
                        status = Status.PAUSE;
                        manager.getLogger().d("[DiskCacheServer]re-open (open is failed before)");
                    }
                    break;
                default:
                    commonException = new RuntimeException("[TILoader:DiskCacheModule]illegal status");
                    break;
            }
        }finally {
            statusLock.unlock();
        }
        if (openException != null){
            manager.getServerSettings().getExceptionHandler().onDiskCacheOpenException(manager.getApplicationContextImage(), manager.getContextImage(), openException, manager.getLogger());
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
                manager.getLogger().d("[DiskCacheServer]pause");
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
        initialize();
        try {
            DiskLruCache diskLruCacheToFlush = this.diskLruCache;
            if (diskLruCacheToFlush != null) {
                diskLruCacheToFlush.flush();
            }
        } catch (IOException e) {
            manager.getServerSettings().getExceptionHandler().onDiskCacheCommonException(manager.getApplicationContextImage(), manager.getContextImage(), e, manager.getLogger());
        }
        try{
            statusLock.lock();
            holdCounter--;
            if (holdCounter < 0) {
                holdCounter = 0;
            }
        }finally {
            statusLock.unlock();
        }
        tryToClose();
    }

    private void tryToClose() {
        //try to pause cache
        dispatchThreadPool.execute(dispatchRunnable);
    }

    private Runnable dispatchRunnable = new Runnable() {
        @Override
        public void run() {
            LockSupport.parkNanos(PAUSE_DELAY_NANOS);
            closeCache();
        }
    };

    public void wipe(File path) throws IOException {
        DiskLruCache.deleteContents(path);
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
