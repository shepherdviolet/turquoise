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

import java.io.InputStream;

import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 *
 *
 * Created by S.Violet on 2016/2/19.
 */
public class NetEngine extends Engine {

    @Override
    protected void executeNewTask(Task task) {
        EngineCallback<NetworkLoadHandler.Result> callback = new EngineCallback<>();
        try {
            getComponentManager().getServerSettings().getNetworkLoadHandler().onHandle(task, callback);
        }catch(Exception e){
            responseFailed(task, e);
            return;
        }
        int result = callback.getResult();
        switch(result){
            case EngineCallback.RESULT_SUCCEED:
                NetworkLoadHandler.Result data = callback.getData();
                if (data.getType() == NetworkLoadHandler.ResultType.NULL){
                    responseFailed(task, new NullPointerException("[TILoader:NetworkLoadHandler]callback return null result!"));
                }else if (data.getType() == NetworkLoadHandler.ResultType.BYTES){
                    handleBytesResult(task, data.getBytes(), data.getLength());
                }else if (data.getType() == NetworkLoadHandler.ResultType.INPUTSTREAM){
                    handleInputStreamResult(task, data.getInputStream(), data.getLength());
                }
                return;
            case EngineCallback.RESULT_FAILED:
                responseFailed(task, callback.getException());
                return;
            default:
                break;
        }
        responseCanceled(task);
    }

    @Override
    protected int getMaxThread() {
        return getComponentManager().getServerSettings().getNetworkLoadMaxThread();
    }

    @Override
    public Type getServerType() {
        return Type.NETWORK_ENGINE;
    }

    private void handleBytesResult(Task task, byte[] bytes, int length){
        if (length == NetworkLoadHandler.Result.UNKNOW_LENGTH){
            length = bytes.length;
        }

    }

    private void handleInputStreamResult(Task task, InputStream inputStream, int length){

    }

    private void responseSucceed(Task task){
        task.setState(Task.State.SUCCEED);
        response(task);
    }

    private void responseFailed(Task task, Exception exception){
        if (exception != null){
            getComponentManager().getServerSettings().getExceptionHandler().onNetworkLoadException(getComponentManager().getContextImage(), task, exception);
        }
        task.setState(Task.State.FAILED);
        response(task);
    }

    private void responseCanceled(Task task){
        task.setState(Task.State.CANCELED);
        response(task);
    }

}
