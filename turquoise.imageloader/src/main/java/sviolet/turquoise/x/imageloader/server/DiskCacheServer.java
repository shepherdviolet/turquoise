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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.model.cache.DiskLruCache;
import sviolet.turquoise.model.common.LazySingleThreadPool;
import sviolet.turquoise.util.droid.ApplicationUtils;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 *
 * Created by S.Violet on 2016/3/22.
 */
public class DiskCacheServer implements ComponentManager.Component, Server {

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
     * ResultType.SUCCEED :<br/>
     * use {@link Result#getTargetFile()} to get File of resource, and than decode from this file<br/>
     * <br/>
     * ResultType.FAILED :<br/>
     * write failed and can't restore data, you have to return failed state<br/>
     * <br/>
     * ResultType.RETURN_MEMORY_BUFFER :<br/>
     * use {@link Result#getMemoryBuffer()} to get bytes of resource, and than decode from bytes<br/>
     *
     * @param task task
     * @param inputStream InputStream
     * @return Result
     */
    public Result write(Task task, InputStream inputStream) {
        Result result = new Result();
        DiskLruCache.Editor editor = null;
        OutputStream outputStream = null;
        try {
            editor = edit(task);
            if (editor == null) {
                manager.getServerSettings().getExceptionHandler().onDiskCacheWriteException(manager.getApplicationContextImage(), manager.getContextImage(), task.getTaskInfo(),
                        new Exception("[TILoader]diskLruCache.edit(cacheKey) return null, multiple edit one file, write disk cache failed"), manager.getLogger());
                writeToMemoryCache(task, inputStream, result, null);
                return result;
            }
            if (isHealthy()) {
                outputStream = editor.newOutputStream(0);
                byte[] buffer = new byte[DiskCacheServer.BUFFER_SIZE];
                boolean hasWrite = false;
                int readLength;
                while (true) {
                    try {
                        readLength = inputStream.read(buffer);
                    } catch (Exception e) {
                        throw new NetworkException(e);
                    }
                    if (readLength < 0) {
                        break;
                    }
                    try {
                        outputStream.write(buffer, 0, readLength);
                        outputStream.flush();
                    }catch (Exception e){
                        setHealthy(false);
                        if (!hasWrite){
                            abortEditor(editor);
                            manager.getServerSettings().getExceptionHandler().onDiskCacheWriteException(manager.getApplicationContextImage(), manager.getContextImage(), task.getTaskInfo(), e, manager.getLogger());
                            writeToMemoryCache(task, inputStream, result, buffer);
                            return result;
                        }
                        throw e;
                    }
                    hasWrite = true;
                }
                if (!hasWrite){
                    throw new Exception("[TILoader]network load failed, null content received (1)");
                }
                editor.commit();
                result.setType(ResultType.SUCCEED);
                setHealthy(true);
            }else{
                writeToMemoryCache(task, inputStream, result, null);
                //trying to write to disk cache
                if (result.getType() == ResultType.RETURN_MEMORY_BUFFER && result.getMemoryBuffer().length > 0) {
                    try {
                        outputStream = editor.newOutputStream(0);
                        outputStream.write(result.getMemoryBuffer());
                        editor.commit();
                        setHealthy(true);
                    }catch(Exception e){
                        setHealthy(false);
                        editor.abort();
                        manager.getServerSettings().getExceptionHandler().onDiskCacheWriteException(manager.getApplicationContextImage(), manager.getContextImage(), task.getTaskInfo(), e, manager.getLogger());
                    }
                }else{
                    manager.getServerSettings().getExceptionHandler().onNetworkLoadException(manager.getApplicationContextImage(), manager.getContextImage(), task.getTaskInfo(),
                            new Exception("[TILoader]network load failed, null content received (3)"), manager.getLogger());
                    editor.abort();
                }
            }
        }catch(NetworkException e){
            abortEditor(editor);
            manager.getServerSettings().getExceptionHandler().onNetworkLoadException(manager.getApplicationContextImage(), manager.getContextImage(), task.getTaskInfo(), e.getCause(), manager.getLogger());
        }catch(Exception e){
            abortEditor(editor);
            manager.getServerSettings().getExceptionHandler().onDiskCacheWriteException(manager.getApplicationContextImage(), manager.getContextImage(), task.getTaskInfo(), e, manager.getLogger());
        }finally {
            closeStream(inputStream);
            closeStream(outputStream);
            release();
        }
        //fetch target file while succeed
        if (result.getType() == ResultType.SUCCEED){
            File targetFile = get(task);
            if (targetFile == null || !targetFile.exists()){
                setHealthy(false);
                result.setType(ResultType.FAILED);
                manager.getServerSettings().getExceptionHandler().onDiskCacheReadException(manager.getApplicationContextImage(), manager.getContextImage(), task.getTaskInfo(),
                    new Exception("[TILoader]resources have been written to disk cache, but we can't find target File!!!"), manager.getLogger());
            }else{
                result.setTargetFile(targetFile);
            }
        }
        return result;
    }

    /**
     * ResultType.SUCCEED :<br/>
     * write succeed, please decode from origin bytes<br/>
     * <br/>
     * ResultType.FAILED :<br/>
     * write failed, please decode from origin bytes<br/>
     * <br/>
     *
     * @param task task
     * @param bytes bytes
     * @return true: write succeed
     */
    public boolean write(Task task, byte[] bytes){
        DiskLruCache.Editor editor = null;
        OutputStream outputStream = null;
        try {
            editor = edit(task);
            if (editor == null) {
                manager.getServerSettings().getExceptionHandler().onDiskCacheWriteException(manager.getApplicationContextImage(), manager.getContextImage(), task.getTaskInfo(),
                        new Exception("[TILoader]diskLruCache.edit(cacheKey) return null, multiple edit one file, write disk cache failed (2)"), manager.getLogger());
                return false;
            }
            //trying to write to disk cache
            if (bytes != null && bytes.length > 0) {
                try {
                    outputStream = editor.newOutputStream(0);
                    outputStream.write(bytes);
                    editor.commit();
                    setHealthy(true);
                    return true;
                } catch (Exception e) {
                    setHealthy(false);
                    editor.abort();
                    manager.getServerSettings().getExceptionHandler().onDiskCacheWriteException(manager.getApplicationContextImage(), manager.getContextImage(), task.getTaskInfo(), e, manager.getLogger());
                }
            } else {
                manager.getServerSettings().getExceptionHandler().onDiskCacheWriteException(manager.getApplicationContextImage(), manager.getContextImage(), task.getTaskInfo(),
                        new Exception("[TILoader]disk cache write failed, bytes is null"), manager.getLogger());
                editor.abort();
            }
        }catch(Exception e){
            abortEditor(editor);
            manager.getServerSettings().getExceptionHandler().onDiskCacheWriteException(manager.getApplicationContextImage(), manager.getContextImage(), task.getTaskInfo(), e, manager.getLogger());
        }finally {
            closeStream(outputStream);
            release();
        }
        return false;
    }

    private Result writeToMemoryCache(Task task, InputStream inputStream, Result result, byte[] buffer) throws NetworkException, IOException  {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (buffer != null) {
            outputStream.write(buffer);
        }
        int readLength;
        while (true) {
            try {
                readLength = inputStream.read(buffer);
            } catch (Exception ne) {
                throw new NetworkException(ne);
            }
            if (readLength < 0) {
                break;
            }
            outputStream.write(buffer);
            outputStream.flush();
        }
        if (outputStream.size() <= 0){
            throw new NetworkException(new Exception("[TILoader]network load failed, null content received (2)"));
        }
        result.setType(ResultType.RETURN_MEMORY_BUFFER);
        result.setMemoryBuffer(outputStream.toByteArray());
        return result;
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
            manager.getServerSettings().getExceptionHandler().onDiskCacheCommonException(manager.getApplicationContextImage(), manager.getContextImage(), openException, manager.getLogger());
        }
        if (commonException != null){
            manager.getServerSettings().getExceptionHandler().onDiskCacheCommonException(manager.getApplicationContextImage(), manager.getContextImage(), commonException, manager.getLogger());
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
                manager.getServerSettings().getExceptionHandler().onDiskCacheCommonException(manager.getApplicationContextImage(), manager.getContextImage(), e, manager.getLogger());
            }
        }
    }

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

    private void closeStream(InputStream stream){
        if (stream != null){
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void closeStream(OutputStream stream){
        if (stream != null){
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void abortEditor(DiskLruCache.Editor editor){
        if (editor != null){
            try {
                editor.abort();
            } catch (IOException ignored) {
            }
        }
    }

    private void setHealthy(boolean isHealthy){
        this.isHealthy.set(isHealthy);
    }

    public boolean isHealthy(){
        return isHealthy.get();
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

    public enum ResultType{
        SUCCEED,
        FAILED,
        RETURN_MEMORY_BUFFER
    }

    public static class Result{

        private ResultType type;
        private byte[] memoryBuffer;
        private File targetFile;

        public Result(){
            type = ResultType.FAILED;
        }

        public ResultType getType() {
            return type;
        }

        public byte[] getMemoryBuffer() {
            return memoryBuffer;
        }

        public void setType(ResultType type) {
            this.type = type;
        }

        public void setMemoryBuffer(byte[] memoryBuffer) {
            this.memoryBuffer = memoryBuffer;
        }

        public File getTargetFile() {
            return targetFile;
        }

        public void setTargetFile(File targetFile) {
            this.targetFile = targetFile;
        }
    }

    public static class NetworkException extends Exception{

        public NetworkException(Throwable throwable) {
            super(throwable);
        }

    }

}
