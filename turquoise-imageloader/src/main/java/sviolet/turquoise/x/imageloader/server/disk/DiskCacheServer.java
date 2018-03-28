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

package sviolet.turquoise.x.imageloader.server.disk;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import sviolet.thistle.model.cache.DiskLruCache;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>Manage disk cache (TILoader inner disk cache)</p>
 *
 * @author S.Violet
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
            if (targetFile == null || !targetFile.exists()|| targetFile.isDirectory()) {
                return null;
            }
            //decode
            try {
                return decodeHandler.decode(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                        task, DecodeHandler.DecodeType.IMAGE_FILE, targetFile, getComponentManager().getLogger());
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
     * Start write process
     * @param task task
     * @param writeProcess write logic
     */
    public WriteResult startWrite(Task task, WriteProcess writeProcess){
        WriteResult result = null;
        DiskLruCache.Editor editor = null;

        try {
            editor = edit(task);
            if (editor == null){
                //if cache file fetch failed, write data to memory buffer, skip write to disk
                throw new Exception("[TILoader]diskLruCache.edit(cacheKey) return null, write disk cache failed");
            }
            result = writeProcess.onWrite(task, new WriterProvider(editor));
            if (result == null || result.getType() != ResultType.SUCCEED){
                abortEditor(editor);
            } else {
                editor.commit();
            }
        }catch(Exception e){
            abortEditor(editor);
            getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheWriteException(
                    getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
        } finally {
            release();
        }

        if (result == null) {
            result = WriteResult.failedResult();
        }

        //fetch target file from cache
        if (result.getType() == ResultType.SUCCEED){
            try {
                File targetFile = get(task);
                if (targetFile == null || !targetFile.exists()) {
                    result.setType(ResultType.FAILED);
                    getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheReadException(
                            getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                            new Exception("[TILoader]Resources have been written to disk cache, but we can't find target File!!!"), getComponentManager().getLogger());
                } else {
                    result.setTargetFile(targetFile);
                }
            } finally {
                release();
            }
        }
        return result;
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

    /**
     * DiskCache write result
     */
    public final static class WriteResult {

        private ResultType type;
        private File targetFile;

        private WriteResult(){
        }

        public static WriteResult succeedResult() {
            WriteResult result = new WriteResult();
            result.setType(ResultType.SUCCEED);
            return result;
        }

        public static WriteResult failedResult() {
            WriteResult result = new WriteResult();
            result.setType(ResultType.FAILED);
            return result;
        }

        public static WriteResult canceledResult(){
            WriteResult result = new WriteResult();
            result.setType(ResultType.CANCELED);
            return result;
        }

        public ResultType getType() {
            return type;
        }

        public File getTargetFile() {
            return targetFile;
        }

        private void setType(ResultType type) {
            this.type = type;
        }

        private void setTargetFile(File targetFile) {
            this.targetFile = targetFile;
        }
    }

    /**
     * Writer provider, to open OutputStream or RandomAccessFile
     */
    public static class WriterProvider {

        private DiskLruCache.Editor editor;

        private WriterProvider(DiskLruCache.Editor editor) {
            this.editor = editor;
        }

        /**
         * Open OutputStream to target file
         */
        public OutputStream newOutputStream() throws IOException {
            return editor.newOutputStream(0);
        }

        /**
         * Open RandomAccessFile to target file
         */
        public RandomAccessFile newRandomAccessFileForWrite() throws IOException {
            return editor.newRandomAccessFileForWrite(0);
        }

    }

    /**
     * Implement write process
     */
    public interface WriteProcess {

        /**
         * Write data to writerProvider
         * @param task task
         * @param writerProvider to open OutputStream or RandomAccessFile
         * @return write result
         */
        WriteResult onWrite(Task task, WriterProvider writerProvider);
    }

}
