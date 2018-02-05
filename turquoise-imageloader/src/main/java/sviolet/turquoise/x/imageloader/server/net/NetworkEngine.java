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

package sviolet.turquoise.x.imageloader.server.net;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.IndispensableState;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.Engine;
import sviolet.turquoise.x.imageloader.server.Server;

/**
 * <p>Net Load Engine</p>
 *
 * @author S.Violet
 */
public class NetworkEngine extends Engine {

    private static final int HISTORY_CAPACITY = 30;

    private Map<String, TaskGroup> taskGroups = new ConcurrentHashMap<>();
    private NetworkLoadingHistory history = new NetworkLoadingHistory(HISTORY_CAPACITY);
    private ReentrantLock lock = new ReentrantLock();

    @Override
    protected boolean preCheck(Task task) {
        switch (task.getParams().getSourceType()) {
            case HTTP_GET:
            case GEN_QR:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void executeNewTask(Task task) {

        //return if resource has loaded recently
        if (history.contains(task.getResourceKey())){
            if (!task.hasReturnedFromNetEngine()){
                getComponentManager().getLogger().d("[NetworkEngine]task return to DiskEngine phase, the resource has loaded recently, task:" + task);
                task.setServerType(Server.Type.DISK_ENGINE);
                task.setState(Task.State.STAND_BY);
                task.setHasReturnedFromNetEngine(true);
                response(task);
                return;
            }else{
                //task can only return to DiskEngine once
                getComponentManager().getLogger().d("[NetworkEngine]task has returned to DiskEngine phase once, loading by NetworkEngine this time, task:" + task);
            }
        }

        //merge if tasks have same resource key
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
            try {
                load(task, group.getIndispensableState());
            } catch (Exception e) {
                try {
                    getComponentManager().getServerSettings().getExceptionHandler().onNetworkLoadException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
                } catch (Exception e2) {
                    getComponentManager().getLogger().e("exception in ExceptionHandler", e2);
                }
                handleFailed(task);
            }
        }
    }

    /**
     * load by servers
     */
    private void load(Task task, IndispensableState indispensableState){
        switch (task.getParams().getSourceType()) {
            case HTTP_GET:
                getComponentManager().getHttpGetServer().load(task, indispensableState);
                break;
            case GEN_QR:
                //TODO
                break;
            default:
                getComponentManager().getLogger().e("[NetworkEngine]load: Unsupported sourceType:" + task.getParams().getSourceType());
                responseCanceled(task);
                break;
        }
    }

    /*********************************************************************
     * handle data
     */

    void handleImageData(Task task, DecodeHandler.DecodeType decodeType, Object data){
        //add resource key to history if loaded succeed
        history.put(task.getResourceKey());
        //get group
        TaskGroup group = taskGroups.remove(task.getResourceKey());
        if (group == null){
            return;
        }
        for (Task t : group.getSet()) {
            //decode
            ImageResource imageResource = decode(t, decodeType, data);
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

    void handleFailed(Task task){
        TaskGroup group = taskGroups.remove(task.getResourceKey());
        if (group == null){
            return;
        }
        for (Task t : group.getSet()) {
            responseFailed(t);
        }
        group.getSet().clear();
    }

    void handleCanceled(Task task){
        TaskGroup group = taskGroups.remove(task.getResourceKey());
        if (group == null){
            return;
        }
        for (Task t : group.getSet()) {
            responseCanceled(t);
        }
        group.getSet().clear();
    }

    private ImageResource decode(Task task, DecodeHandler.DecodeType decodeType, Object data){
        ImageResource imageResource = null;
        try {
            //decode
            imageResource = getDecodeHandler(task).decode(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                    task, decodeType, data, getComponentManager().getLogger());
        }catch(Exception e){
            getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
            return null;
        }
        //check valid
        if (!getComponentManager().getServerSettings().getImageResourceHandler().isValid(imageResource)){
            getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                    new Exception("[NetworkEngine]decoding failed, return null or invalid ImageResource"), getComponentManager().getLogger());
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
            if (task == null) {
                return;
            }
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
