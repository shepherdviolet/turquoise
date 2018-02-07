/*
 * Copyright (C) 2015-2018 S.Violet
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

package sviolet.turquoise.x.imageloader.server.net;

import java.io.IOException;
import java.io.InputStream;

import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.entity.IndispensableState;
import sviolet.turquoise.x.imageloader.entity.LowNetworkSpeedStrategy;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.Server;
import sviolet.turquoise.x.imageloader.server.disk.DiskCacheServer;

/**
 * Get image from http
 *
 * @author S.Violet
 */
public class HttpGetServer implements ComponentManager.Component, Server {

    private ComponentManager manager;
    private NetworkEngine networkEngine;

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
        this.networkEngine = (NetworkEngine) manager.getNetworkEngine();
    }

    @Override
    public Type getServerType() {
        return Type.NETWORK_HTTP_GET;
    }

    private ComponentManager getComponentManager(){
        return manager;
    }

    private NetworkEngine getNetworkEngine() {
        return networkEngine;
    }

    //load//////////////////////////////////////////////////////////////////////////////////////

    void load(Task task, IndispensableState indispensableState) {
        //reset progress
        task.getLoadProgress().reset();
        //timeout, indispensable task has double timeout
        long connectTimeout = indispensableState.isIndispensable() ?
                getNetworkEngine().getNetworkConnectTimeout(task) << 1 :
                getNetworkEngine().getNetworkConnectTimeout(task);
        long readTimeout = indispensableState.isIndispensable() ?
                getNetworkEngine().getNetworkReadTimeout(task) << 1 :
                getNetworkEngine().getNetworkReadTimeout(task);
        //network loading, callback's timeout is triple of network timeout
        NetworkCallback<NetworkLoadHandler.Result> callback = new NetworkCallback<>((connectTimeout + readTimeout) * 3, getComponentManager().getLogger());
        try {
            getNetworkEngine().getNetworkLoadHandler(task).onHandle(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), callback, connectTimeout, readTimeout, getComponentManager().getLogger());
        }catch(Exception e){
            getComponentManager().getServerSettings().getExceptionHandler().onNetworkLoadException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
            getNetworkEngine().handleFailed(task);
            return;
        }
        //waiting for result
        int result = callback.getResult();
        if (getComponentManager().getLogger().checkEnable(TLogger.DEBUG)) {
            getComponentManager().getLogger().d("[NetworkEngine]get result from networkHandler, result:" + result + ", task:" + task);
        }
        switch(result){
            //load succeed
            case NetworkCallback.RESULT_SUCCEED:
                onResultSucceed(task, callback.getData(), indispensableState);
                return;
            //load failed
            case NetworkCallback.RESULT_FAILED:
                onResultFailed(task, callback.getException(), indispensableState);
                return;
            //load canceled
            case NetworkCallback.RESULT_CANCELED:
            default:
                onResultCanceled(task, indispensableState);
                break;
        }
    }

    /*********************************************************************
     * handle network result
     */

    private void onResultSucceed(Task task, NetworkLoadHandler.Result data, IndispensableState indispensableState) {
        //dispatch by type
        if (data.getType() == NetworkLoadHandler.ResultType.NULL){
            getComponentManager().getServerSettings().getExceptionHandler().onNetworkLoadException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                    new Exception("[NetworkLoadHandler]callback return null result!"), getComponentManager().getLogger());
            getNetworkEngine().handleFailed(task);
        }else if (data.getType() == NetworkLoadHandler.ResultType.BYTES){
            //set progress
            task.getLoadProgress().setTotal(data.getBytes().length);
            task.getLoadProgress().setLoaded(data.getBytes().length);
            //handle
            onBytesResult(task, data.getBytes(), indispensableState);
        }else if (data.getType() == NetworkLoadHandler.ResultType.INPUTSTREAM){
            //set progress
            task.getLoadProgress().setTotal(data.getLength());
            //handle
            onInputStreamResult(task, data.getInputStream(), indispensableState);
        }
    }

    private void onResultFailed(Task task, Exception exception, IndispensableState indispensableState) {
        if (exception != null){
            getComponentManager().getServerSettings().getExceptionHandler().onNetworkLoadException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), exception, getComponentManager().getLogger());
        }
        getNetworkEngine().handleFailed(task);
    }

    private void onResultCanceled(Task task, IndispensableState indispensableState) {
        getNetworkEngine().handleCanceled(task);
    }

    /**
     * handle byte[] result
     */
    private void onBytesResult(Task task, byte[] bytes, IndispensableState indispensableState){
        //try to write disk cache
        getComponentManager().getDiskCacheServer().write(task, bytes);
        //handle data
        getNetworkEngine().handleImageData(task, DecodeHandler.DecodeType.IMAGE_BYTES, bytes);
    }

    /**
     * handle InputStream result
     */
    private void onInputStreamResult(Task task, InputStream inputStream, IndispensableState indispensableState){
        //cancel loading if image data out of limit
        if (task.getLoadProgress().total() > getComponentManager().getServerSettings().getImageDataLengthLimit()){
            getComponentManager().getServerSettings().getExceptionHandler().onImageDataLengthOutOfLimitException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                    task.getTaskInfo(), task.getLoadProgress().total(), getComponentManager().getServerSettings().getImageDataLengthLimit(), getComponentManager().getLogger());
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
            getNetworkEngine().handleCanceled(task);
            return;
        }
        //try to write disk cache
        LowNetworkSpeedStrategy.Configure lowNetworkSpeedConfig = getComponentManager().getServerSettings().getLowNetworkSpeedStrategy().getConfigure(getComponentManager().getApplicationContextImage(), indispensableState);
        if (getComponentManager().getLogger().checkEnable(TLogger.DEBUG)) {
            getComponentManager().getLogger().d("[NetworkEngine]LowNetworkSpeedStrategy:" + lowNetworkSpeedConfig.getType() + ", task:" + task);
        }
        DiskCacheServer.Result result = getComponentManager().getDiskCacheServer().write(task, inputStream, lowNetworkSpeedConfig);
        switch (result.getType()){
            case SUCCEED:
                getNetworkEngine().handleImageData(task, DecodeHandler.DecodeType.IMAGE_FILE, result.getTargetFile());
                break;
            case RETURN_MEMORY_BUFFER:
                getNetworkEngine().handleImageData(task, DecodeHandler.DecodeType.IMAGE_BYTES, result.getMemoryBuffer());
                break;
            case CANCELED:
                getNetworkEngine().handleCanceled(task);
                break;
            case FAILED:
            default:
                getNetworkEngine().handleFailed(task);
                break;
        }
    }

}
