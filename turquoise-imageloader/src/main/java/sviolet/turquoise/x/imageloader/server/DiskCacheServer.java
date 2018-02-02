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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sviolet.thistle.model.cache.DiskLruCache;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.LowNetworkSpeedStrategy;
import sviolet.turquoise.x.imageloader.entity.ServerSettings;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.module.DiskCacheModule;

/**
 * <p>Manage disk cache (TILoader inner disk cache)</p>
 *
 * Created by S.Violet on 2016/4/5.
 */
public class DiskCacheServer extends DiskCacheModule {

    /************************************************************************
     * read
     */

    /**
     * read Image from disk cache
     * @param task task
     * @param decodeHandler used to decode file
     * @return ImageResource, might be null
     */
    public ImageResource read(Task task, DecodeHandler decodeHandler) {
        //fetch cache file
        try {
            File targetFile = get(task);
            if (targetFile == null || !targetFile.exists()) {
                return null;
            }
            //decode
            try {
                return decodeHandler.decode(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                        task, targetFile, getComponentManager().getLogger());
            } catch (Throwable t) {
                getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), t, getComponentManager().getLogger());
                return null;
            }
        } finally {
            //release
            release();
        }
    }

    /************************************************************************
     * write
     */

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
     * @param lowNetworkSpeedConfig lowNetworkSpeedConfig
     * @return Result
     */
    public Result write(Task task, InputStream inputStream, LowNetworkSpeedStrategy.Configure lowNetworkSpeedConfig) {
        Result result = new Result();//result of write task
        DiskLruCache.Editor editor = null;
        OutputStream outputStream = null;
        try {
            //edit cache file
            editor = edit(task);
            if (editor == null) {
                //if cache file fetch failed, write data to memory buffer, skip write to disk
                getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheWriteException(
                        getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                        new Exception("[TILoader]diskLruCache.edit(cacheKey) return null, write disk cache failed"), getComponentManager().getLogger());
                writeToMemoryBuffer(task, inputStream, result, null, 0, lowNetworkSpeedConfig);
                return result;
            }
            /*
             * Check disk cache healthy status.
             * If cache is healthy, data write to disk first, and return File, decoder will decode image from file,
             * in order to use less memory.
             * If cache is not healthy, data write to memory buffer, and return bytes, decoder will decode image from bytes,
             * in order to ensure pictures display normally.
             */
            if (isHealthy()) {
                //write to disk first, and return File
                outputStream = editor.newOutputStream(0);
                //buffer, to save memory
                long startTime = System.currentTimeMillis();
                long imageDataLengthLimit = getComponentManager().getServerSettings().getImageDataLengthLimit();
                byte[] buffer = new byte[DiskCacheServer.BUFFER_SIZE];
                int readLength;
                int loopCount = 0;
                //read and write
                while (true) {
                    try {
                        //read from inputStream
                        readLength = inputStream.read(buffer);
                    } catch (Exception e) {
                        throw new NetworkException(e);
                    }
                    if (readLength < 0) {
                        break;
                    }
                    //record progress
                    task.getLoadProgress().increaseLoaded(readLength);
                    //check if data out of limit
                    if (task.getLoadProgress().loaded() > imageDataLengthLimit){
                        abortEditor(editor);
                        getComponentManager().getServerSettings().getExceptionHandler().onImageDataLengthOutOfLimitException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                                task.getTaskInfo(), task.getLoadProgress().loaded(), getComponentManager().getServerSettings().getImageDataLengthLimit(), getComponentManager().getLogger());
                        result.setType(ResultType.CANCELED);
                        return result;
                    }
                    //check if speed is low
                    if (checkNetworkSpeed(startTime, loopCount, task, result, lowNetworkSpeedConfig)){
                        abortEditor(editor);
                        return result;
                    }
                    try {
                        //try to write disk
                        outputStream.write(buffer, 0, readLength);
                        outputStream.flush();
                    }catch (Exception e){
                        //not healthy
                        setHealthy(false);
                        //if error while writing first buffer data, try to write to memory buffer, in order to ensure pictures display normally.
                        if (loopCount == 0){
                            abortEditor(editor);
                            getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheWriteException(
                                    getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
                            writeToMemoryBuffer(task, inputStream, result, buffer, readLength, lowNetworkSpeedConfig);
                            return result;
                        }
                        throw e;
                    }
                    loopCount++;
                }
                if (loopCount == 0){
                    throw new NetworkException(new Exception("[TILoader]network load failed, null content received (1)"));
                }
                //succeed
                editor.commit();
                result.setType(ResultType.SUCCEED);
                setHealthy(true);
            }else{
                //if disk is not healthy, data write to memory buffer, and return bytes, in order to ensure pictures display normally.
                writeToMemoryBuffer(task, inputStream, result, null, 0, lowNetworkSpeedConfig);
                //trying to write to disk cache
                if (result.getType() == ResultType.RETURN_MEMORY_BUFFER && result.getMemoryBuffer().length > 0) {
                    try {
                        outputStream = editor.newOutputStream(0);
                        outputStream.write(result.getMemoryBuffer());
                        editor.commit();
                        setHealthy(true);
                    }catch(Exception e){
                        setHealthy(false);
                        abortEditor(editor);
                        getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheWriteException(
                                getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
                    }
                }else{
                    abortEditor(editor);
                    getComponentManager().getServerSettings().getExceptionHandler().onNetworkLoadException(
                            getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                            new Exception("[TILoader]network load failed, null content received (3)"), getComponentManager().getLogger());
                }
            }
        }catch(NetworkException e){
            abortEditor(editor);
            getComponentManager().getServerSettings().getExceptionHandler().onNetworkLoadException(
                    getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e.getCause(), getComponentManager().getLogger());
        }catch(Exception e){
            abortEditor(editor);
            getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheWriteException(
                    getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
        } finally {
            closeStream(inputStream);
            closeStream(outputStream);
            release();
        }
        //fetch target file while succeed
        fetchTargetFile(task, result);
        return result;
    }

    private void fetchTargetFile(Task task, Result result) {
        if (result.getType() == ResultType.SUCCEED){
            try {
                File targetFile = get(task);
                if (targetFile == null || !targetFile.exists()) {
                    setHealthy(false);
                    result.setType(ResultType.FAILED);
                    getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheReadException(
                            getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                            new Exception("[TILoader]resources have been written to disk cache, but we can't find target File!!!"), getComponentManager().getLogger());
                } else {
                    result.setTargetFile(targetFile);
                }
            } finally {
                release();
            }
        }
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
            //edit cache file
            editor = edit(task);
            if (editor == null) {
                getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheWriteException(
                        getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                        new Exception("[TILoader]diskLruCache.edit(cacheKey) return null, write disk cache failed (2)"), getComponentManager().getLogger());
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
                    abortEditor(editor);
                    getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheWriteException(
                            getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
                }
            } else {
                abortEditor(editor);
                getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheWriteException(
                        getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                        new Exception("[TILoader]disk cache write failed, bytes is null"), getComponentManager().getLogger());
            }
        }catch(Exception e){
            abortEditor(editor);
            getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheWriteException(
                    getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
        }finally {
            closeStream(outputStream);
            release();
        }
        return false;
    }

    /**
     * data load from network write to full size memory buffer, in order to ensure pictures display normally.
     * @param task task
     * @param inputStream inputStream
     * @param result result of write task
     * @param buffer fix buffer, might have data
     * @param bufferDataLength data length of fix buffer
     * @throws NetworkException exception of network
     * @throws IOException exception of io
     */
    private void writeToMemoryBuffer(Task task, InputStream inputStream, Result result, byte[] buffer, int bufferDataLength, LowNetworkSpeedStrategy.Configure lowNetworkSpeedConfig) throws NetworkException, IOException  {
        //cancel loading if memory buffer out of limit
        if (task.getLoadProgress().total() > getComponentManager().getServerSettings().getMemoryBufferLengthLimit()){
            getComponentManager().getServerSettings().getExceptionHandler().onMemoryBufferLengthOutOfLimitException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                    task.getTaskInfo(), task.getLoadProgress().total(), getComponentManager().getServerSettings().getMemoryBufferLengthLimit(), getComponentManager().getLogger());
            result.setType(ResultType.CANCELED);
            return;
        }
        //full size buffer
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //fix buffer
        if (buffer != null) {
            outputStream.write(buffer, 0, bufferDataLength);
        }else{
            buffer = new byte[DiskCacheServer.BUFFER_SIZE];
        }
        long startTime = System.currentTimeMillis();
        long memoryBufferLengthLimit = getComponentManager().getServerSettings().getMemoryBufferLengthLimit();
        int readLength;
        int loopCount = 0;
        while (true) {
            try {
                readLength = inputStream.read(buffer);
            } catch (Exception ne) {
                throw new NetworkException(ne);
            }
            if (readLength < 0) {
                break;
            }
            //record progress
            task.getLoadProgress().increaseLoaded(readLength);
            //check if data out of limit
            if (task.getLoadProgress().loaded() > memoryBufferLengthLimit){
                getComponentManager().getServerSettings().getExceptionHandler().onMemoryBufferLengthOutOfLimitException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                        task.getTaskInfo(), task.getLoadProgress().loaded(), getComponentManager().getServerSettings().getMemoryBufferLengthLimit(), getComponentManager().getLogger());
                result.setType(ResultType.CANCELED);
                return;
            }
            //check if speed is low
            if (checkNetworkSpeed(startTime, loopCount, task, result, lowNetworkSpeedConfig)){
                return;
            }
            outputStream.write(buffer, 0, readLength);
            outputStream.flush();
            loopCount++;
        }
        if (outputStream.size() <= 0){
            throw new NetworkException(new Exception("[TILoader]network load failed, null content received (2)"));
        }
        //return memory buffer
        result.setType(ResultType.RETURN_MEMORY_BUFFER);
        result.setMemoryBuffer(outputStream.toByteArray());
    }

    private boolean checkNetworkSpeed(long startTime, int loopCount, Task task, Result result, LowNetworkSpeedStrategy.Configure lowNetworkSpeedConfig){
        //decrease check frequency
        if (loopCount << 30 != 0){
            return false;
        }

        final long elapseTime = System.currentTimeMillis() - startTime + 1;
        final long loadedData = task.getLoadProgress().loaded();
        final long totalData = task.getLoadProgress().total();

        ServerSettings serverSettings = getComponentManager().getServerSettings();
        final long deadline = lowNetworkSpeedConfig.getDeadline();
        final long windowPeriod = lowNetworkSpeedConfig.getWindowPeriod();
        final int thresholdSpeed = lowNetworkSpeedConfig.getThresholdSpeed();

        //dead line
        if (elapseTime > deadline){
            int speed = (int) ((float)loadedData / (elapseTime >> 10));
            serverSettings.getExceptionHandler().handleLowNetworkSpeedEvent(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                    task.getTaskInfo(), elapseTime, speed, getComponentManager().getLogger());
            result.setType(ResultType.CANCELED);
            return true;
        }
        //if in window period, skip speed check
        if (elapseTime < windowPeriod){
            return false;
        }
        //check speed
        int speed = (int) ((float)loadedData / (elapseTime >> 10));
        //check by thresholdSpeed
        if (speed > thresholdSpeed) {
            //check by progress
            if (totalData > 0) {
                //calculate min speed by total data length
                int minSpeed = (int)((float)totalData / (deadline >> 10));
                //80% off
                if (speed > minSpeed * 0.8f){
                    return false;
                }
            } else {
                return false;
            }
        }
        serverSettings.getExceptionHandler().handleLowNetworkSpeedEvent(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                task.getTaskInfo(), elapseTime, speed, getComponentManager().getLogger());
        result.setType(ResultType.CANCELED);
        return true;
    }

    /************************************************************************
     * function
     */

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

    /************************************************************************
     * inner
     */

    public enum ResultType{
        SUCCEED,
        FAILED,
        RETURN_MEMORY_BUFFER,
        CANCELED
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

    private static class NetworkException extends Exception{

        public NetworkException(Throwable throwable) {
            super(throwable);
        }

    }

}
