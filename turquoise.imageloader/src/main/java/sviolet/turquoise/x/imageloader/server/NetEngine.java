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
import java.io.InputStream;

import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>Net Load Engine</p>
 *
 * Created by S.Violet on 2016/2/19.
 */
public class NetEngine extends Engine {

    @Override
    protected void executeNewTask(Task task) {
        //reset progress
        task.getLoadProgress().reset();
        //timeout
        long connectTimeout = getNetworkConnectTimeout(task);
        long readTimeout = getNetworkReadTimeout(task);
        //network loading, callback's timeout is triple of network timeout
        EngineCallback<NetworkLoadHandler.Result> callback = new EngineCallback<>((connectTimeout + readTimeout) * 3, getComponentManager().getLogger());
        try {
            getNetworkLoadHandler(task).onHandle(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), callback, connectTimeout, readTimeout, getComponentManager().getLogger());
        }catch(Exception e){
            getComponentManager().getServerSettings().getExceptionHandler().onNetworkLoadException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
            responseFailed(task);
            return;
        }
        //waiting for result
        int result = callback.getResult();
        if (!getComponentManager().getLogger().isNullLogger()) {
            getComponentManager().getLogger().d("[NetEngine]get result from handler, task:" + task);
        }
        switch(result){
            //load succeed
            case EngineCallback.RESULT_SUCCEED:
                NetworkLoadHandler.Result data = callback.getData();
                //dispatch by type
                if (data.getType() == NetworkLoadHandler.ResultType.NULL){
                    getComponentManager().getServerSettings().getExceptionHandler().onNetworkLoadException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                            new Exception("[TILoader:NetworkLoadHandler]callback return null result!"), getComponentManager().getLogger());
                    responseFailed(task);
                }else if (data.getType() == NetworkLoadHandler.ResultType.BYTES){
                    //set progress
                    task.getLoadProgress().setTotal(data.getBytes().length);
                    task.getLoadProgress().setLoaded(data.getBytes().length);
                    //handle
                    handleBytesResult(task, data.getBytes());
                }else if (data.getType() == NetworkLoadHandler.ResultType.INPUTSTREAM){
                    //set progress
                    task.getLoadProgress().setTotal(data.getLength());
                    //handle
                    handleInputStreamResult(task, data.getInputStream());
                }
                return;
            //load failed
            case EngineCallback.RESULT_FAILED:
                Exception exception = callback.getException();
                if (exception != null){
                    getComponentManager().getServerSettings().getExceptionHandler().onNetworkLoadException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), exception, getComponentManager().getLogger());
                }
                responseFailed(task);
                return;
            //load canceled
            case EngineCallback.RESULT_CANCELED:
            default:
                responseCanceled(task);
                break;
        }

    }

    @Override
    protected int getMaxThread() {
        return getComponentManager().getServerSettings().getNetworkLoadMaxThread();
    }

    @Override
    public Type getServerType() {
        return Type.NETWORK_ENGINE;
    }

    /**
     * @param task task
     * @param bytes image bytes data
     */
    private void handleBytesResult(Task task, byte[] bytes){
        //try to write disk cache
        getComponentManager().getDiskCacheServer().write(task, bytes);
        //decode
        ImageResource<?> imageResource = decode(task, bytes, null);
        if (imageResource == null){
            responseFailed(task);
            return;
        }
        //cache by memory
        getComponentManager().getMemoryCacheServer().put(task.getKey(), imageResource);
        responseSucceed(task);
    }

    /**
     * @param task task
     * @param inputStream image input stream
     */
    private void handleInputStreamResult(Task task, InputStream inputStream){
        //cancel loading if image data out of limit
        if (task.getLoadProgress().total() > getComponentManager().getServerSettings().getImageDataLengthLimit()){
            getComponentManager().getServerSettings().getExceptionHandler().onImageDataLengthOutOfLimitException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                    task.getTaskInfo(), task.getLoadProgress().total(), getComponentManager().getServerSettings().getImageDataLengthLimit(), getComponentManager().getLogger());
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
            responseCanceled(task);
            return;
        }
        //try to write disk cache
        DiskCacheServer.Result result = getComponentManager().getDiskCacheServer().write(task, inputStream);
        //decode
        ImageResource<?> imageResource = null;
        switch (result.getType()){
            case SUCCEED:
                imageResource = decode(task, null, result.getTargetFile());
                break;
            case RETURN_MEMORY_BUFFER:
                imageResource = decode(task, result.getMemoryBuffer(), null);
                break;
            case CANCELED:
                responseCanceled(task);
                return;
            case FAILED:
            default:
                responseFailed(task);
                return;
        }
        if (imageResource == null){
            responseFailed(task);
            return;
        }
        //cache by memory
        getComponentManager().getMemoryCacheServer().put(task.getKey(), imageResource);
        responseSucceed(task);
    }

    private ImageResource<?> decode(Task task, byte[] bytes, File file){
        ImageResource<?> imageResource = null;
        try {
            //dispatch type
            if (bytes != null) {
                imageResource = getDecodeHandler(task).decode(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                        task, bytes, getComponentManager().getLogger());
            }else if(file != null){
                imageResource = getDecodeHandler(task).decode(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                        task, file, getComponentManager().getLogger());
            }else{
                throw new Exception("[TILoader:NetEngine]can't decode neither byte[] nor file");
            }
        }catch(Exception e){
            getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
            return null;
        }
        //check valid
        if (!getComponentManager().getServerSettings().getImageResourceHandler().isValid(imageResource)){
            getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                    new Exception("[TILoader:NetEngine]decoding failed, return null or invalid ImageResource"), getComponentManager().getLogger());
            return null;
        }
        return imageResource;
    }

    private void responseSucceed(Task task){
        task.setState(Task.State.SUCCEED);
        response(task);
    }

    private void responseFailed(Task task){
        task.setState(Task.State.FAILED);
        response(task);
    }

    private void responseCanceled(Task task){
        task.setState(Task.State.CANCELED);
        response(task);
    }

}
