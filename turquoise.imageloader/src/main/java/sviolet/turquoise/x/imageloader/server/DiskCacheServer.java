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

import sviolet.turquoise.model.cache.DiskLruCache;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.module.DiskCacheModule;

/**
 * <p>manage disk cache</p>
 *
 * Created by S.Violet on 2016/4/5.
 */
public class DiskCacheServer extends DiskCacheModule {

    /**
     * read Image from disk cache
     * @param task task
     * @param decodeHandler used to decode file
     * @return ImageResource, might be null
     */
    public ImageResource<?> read(Task task, DecodeHandler decodeHandler){
        //fetch cache file
        File targetFile = get(task);
        if (targetFile == null || !targetFile.exists()){
            return null;
        }
        //decode
        ImageResource<?> imageResource = null;
        try {
            imageResource = decodeHandler.decode(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                    task, targetFile, getComponentManager().getLogger());
            if (imageResource == null){
                getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                        new Exception("[TILoader:DiskEngine]decoding failed, return null ImageResource"), getComponentManager().getLogger());
            }
        }catch(Exception e){
            getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
        }
        //release
        release();
        return imageResource;
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
                writeToMemoryBuffer(task, inputStream, result, null);
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
                byte[] buffer = new byte[DiskCacheServer.BUFFER_SIZE];
                boolean hasWrite = false;
                int readLength;
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
                    try {
                        //try to write disk
                        outputStream.write(buffer, 0, readLength);
                        outputStream.flush();
                    }catch (Exception e){
                        //not healthy
                        setHealthy(false);
                        //if error while writing first buffer data, try to write to memory buffer, in order to ensure pictures display normally.
                        if (!hasWrite){
                            abortEditor(editor);
                            getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheWriteException(
                                    getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
                            writeToMemoryBuffer(task, inputStream, result, buffer);
                            return result;
                        }
                        throw e;
                    }
                    hasWrite = true;
                }
                if (!hasWrite){
                    throw new Exception("[TILoader]network load failed, null content received (1)");
                }
                //succeed
                editor.commit();
                result.setType(ResultType.SUCCEED);
                setHealthy(true);
            }else{
                //if disk is not healthy, data write to memory buffer, and return bytes, in order to ensure pictures display normally.
                writeToMemoryBuffer(task, inputStream, result, null);
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
                        getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheWriteException(
                                getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
                    }
                }else{
                    getComponentManager().getServerSettings().getExceptionHandler().onNetworkLoadException(
                            getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                            new Exception("[TILoader]network load failed, null content received (3)"), getComponentManager().getLogger());
                    editor.abort();
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
                getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheReadException(
                        getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                        new Exception("[TILoader]resources have been written to disk cache, but we can't find target File!!!"), getComponentManager().getLogger());
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
                    editor.abort();
                    getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheWriteException(
                            getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
                }
            } else {
                getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheWriteException(
                        getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                        new Exception("[TILoader]disk cache write failed, bytes is null"), getComponentManager().getLogger());
                editor.abort();
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
     * @return result of write task
     * @throws NetworkException exception of network
     * @throws IOException exception of io
     */
    private Result writeToMemoryBuffer(Task task, InputStream inputStream, Result result, byte[] buffer) throws NetworkException, IOException  {
        //full size buffer
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //fix buffer
        if (buffer != null) {
            outputStream.write(buffer);
        }else{
            buffer = new byte[DiskCacheServer.BUFFER_SIZE];
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
            outputStream.write(buffer, 0, readLength);
            outputStream.flush();
        }
        if (outputStream.size() <= 0){
            throw new NetworkException(new Exception("[TILoader]network load failed, null content received (2)"));
        }
        //return memory buffer
        result.setType(ResultType.RETURN_MEMORY_BUFFER);
        result.setMemoryBuffer(outputStream.toByteArray());
        return result;
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

    private static class NetworkException extends Exception{

        public NetworkException(Throwable throwable) {
            super(throwable);
        }

    }

}
