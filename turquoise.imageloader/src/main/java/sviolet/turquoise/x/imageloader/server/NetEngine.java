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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.IndispensableState;
import sviolet.turquoise.x.imageloader.entity.LowNetworkSpeedStrategy;
import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>Net Load Engine</p>
 *
 * Created by S.Violet on 2016/2/19.
 */
public class NetEngine extends Engine {

    private Map<String, TaskGroup> taskGroups = new ConcurrentHashMap<>();
    private ReentrantLock lock = new ReentrantLock();

    @Override
    protected void executeNewTask(Task task) {

        boolean executable = false;
        String resourceKey = task.getResourceKey();
        TaskGroup group;
        try{
            lock.lock();
            group = taskGroups.get(resourceKey);
            if (group == null){
                group = new TaskGroup();
                taskGroups.put(resourceKey, group);
                executable = true;
            }
        } finally {
            lock.unlock();
        }
        group.add(task);

        if (executable) {
            loadByHandler(task, group.getIndispensableState());
        }
    }

    private void loadByHandler(Task task, IndispensableState indispensableState) {
        //reset progress
        task.getLoadProgress().reset();
        //timeout, indispensable task has double timeout
        long connectTimeout = indispensableState.isIndispensable() ? getNetworkConnectTimeout(task) << 1 : getNetworkConnectTimeout(task);
        long readTimeout = indispensableState.isIndispensable() ? getNetworkReadTimeout(task) << 1 : getNetworkReadTimeout(task);
        //network loading, callback's timeout is triple of network timeout
        EngineCallback<NetworkLoadHandler.Result> callback = new EngineCallback<>((connectTimeout + readTimeout) * 3, getComponentManager().getLogger());
        try {
            getNetworkLoadHandler(task).onHandle(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), callback, connectTimeout, readTimeout, getComponentManager().getLogger());
        }catch(Exception e){
            getComponentManager().getServerSettings().getExceptionHandler().onNetworkLoadException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
            handleFailed(task);
            return;
        }
        //waiting for result
        int result = callback.getResult();
        if (!getComponentManager().getLogger().isNullLogger()) {
            getComponentManager().getLogger().d("[NetEngine]get result from networkHandler, result:" + result + ", task:" + task);
        }
        switch(result){
            //load succeed
            case EngineCallback.RESULT_SUCCEED:
                onResultSucceed(task, callback.getData(), indispensableState);
                return;
            //load failed
            case EngineCallback.RESULT_FAILED:
                onResultFailed(task, callback.getException(), indispensableState);
                return;
            //load canceled
            case EngineCallback.RESULT_CANCELED:
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
                    new Exception("[TILoader:NetworkLoadHandler]callback return null result!"), getComponentManager().getLogger());
            handleFailed(task);
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
        handleFailed(task);
    }

    private void onResultCanceled(Task task, IndispensableState indispensableState) {
        handleCanceled(task);
    }

    /**
     * @param task task
     * @param bytes image bytes data
     */
    private void onBytesResult(Task task, byte[] bytes, IndispensableState indispensableState){
        //try to write disk cache
        getComponentManager().getDiskCacheServer().write(task, bytes);
        //handle data
        handleImageData(task, bytes, null);
    }

    /**
     * @param task task
     * @param inputStream image input stream
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
            handleCanceled(task);
            return;
        }
        //try to write disk cache
        LowNetworkSpeedStrategy.Configure lowNetworkSpeedConfig = getComponentManager().getServerSettings().getLowNetworkSpeedStrategy().getConfigure(getComponentManager().getApplicationContextImage(), indispensableState);
        if (!getComponentManager().getLogger().isNullLogger()) {
            getComponentManager().getLogger().d("[NetEngine]LowNetworkSpeedStrategy:" + lowNetworkSpeedConfig.getType() + ", task:" + task);
        }
        DiskCacheServer.Result result = getComponentManager().getDiskCacheServer().write(task, inputStream, lowNetworkSpeedConfig);
        switch (result.getType()){
            case SUCCEED:
                handleImageData(task, null, result.getTargetFile());
                break;
            case RETURN_MEMORY_BUFFER:
                handleImageData(task, result.getMemoryBuffer(), null);
                break;
            case CANCELED:
                handleCanceled(task);
                break;
            case FAILED:
            default:
                handleFailed(task);
                break;
        }
    }

    /*********************************************************************
     * handle data
     */

    private void handleImageData(Task task, byte[] bytes, File file){
        TaskGroup group = taskGroups.remove(task.getResourceKey());
        if (group == null){
            return;
        }
        for (Task t : group.getSet()) {
            //decode
            ImageResource imageResource = decode(t, bytes, file);
            if (imageResource == null) {
                responseFailed(t);
                continue;
            }
            //cache by memory
            getComponentManager().getMemoryCacheServer().put(t.getKey(), imageResource);
            responseSucceed(t);
        }
        group.getSet().clear();
    }

    private void handleFailed(Task task){
        TaskGroup group = taskGroups.remove(task.getResourceKey());
        if (group == null){
            return;
        }
        for (Task t : group.getSet()) {
            responseFailed(t);
        }
        group.getSet().clear();
    }

    private void handleCanceled(Task task){
        TaskGroup group = taskGroups.remove(task.getResourceKey());
        if (group == null){
            return;
        }
        for (Task t : group.getSet()) {
            responseCanceled(t);
        }
        group.getSet().clear();
    }

    private ImageResource decode(Task task, byte[] bytes, File file){
        ImageResource imageResource = null;
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

    /*********************************************************************
     * response
     */

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

    /*********************************************************************
     * override
     */

    @Override
    protected int getMaxThread() {
        return getComponentManager().getServerSettings().getNetworkLoadMaxThread();
    }

    @Override
    public Type getServerType() {
        return Type.NETWORK_ENGINE;
    }

    /*********************************************************************
     * inner class
     */

    private static class TaskGroup{

        private Set<Task> stubSet = Collections.newSetFromMap(new ConcurrentHashMap<Task, Boolean>());
        private IndispensableState indispensableState = new IndispensableState();

        /**
         * @param task add the task into group, non-repetitive(Set)
         */
        public void add(Task task){
            if (task == null)
                return;
            stubSet.add(task);
            //check and set indispensable state
            if (task.isIndispensable()){
                indispensableState.setIndispensable();
            }
        }

        public Set<Task> getSet(){
            return stubSet;
        }

        public IndispensableState getIndispensableState(){
            return indispensableState;
        }

    }

}
