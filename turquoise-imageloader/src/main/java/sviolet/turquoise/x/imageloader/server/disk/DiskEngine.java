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

import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.Engine;

/**
 * <p>Disk Load Engine</p>
 *
 * @author S.Violet
 */
public class DiskEngine extends Engine {

    @Override
    protected boolean preCheck(Task task) {
        return true;
    }

    @Override
    protected void executeNewTask(Task task) {
        switch (task.getParams().getSourceType()) {
            case LOCAL_DISK:
                loadFromLocalDisk(task);
                break;
            case APK_ASSETS:
                loadFromAssets(task);
                break;
            case APK_RES:
                loadFromRes(task);
                break;
            case URL_TO_QR_CODE:
                //to network engine
                task.setState(Task.State.FAILED);
                response(task);
                break;
            default:
                //default way
                loadFromInnerDiskCache(task);
                break;
        }
    }

    /**
     * Load from device local disk
     */
    private void loadFromLocalDisk(Task task){
        ImageResource imageResource;
        try{
            imageResource = getComponentManager().getDiskLoadServer().readFromLocalDisk(task, getDecodeHandler(task));
        } catch (Exception e){
            getComponentManager().getServerSettings().getExceptionHandler().onLocalDiskLoadCommonException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
            task.setState(Task.State.CANCELED);
            response(task);
            return;
        }
        if (!getComponentManager().getServerSettings().getImageResourceHandler().isValid(imageResource)){
            task.setState(Task.State.CANCELED);
            response(task);
            return;
        }
        getComponentManager().getMemoryCacheServer().put(task.getKey(), imageResource);
        task.setState(Task.State.SUCCEED);
        response(task);
    }

    /**
     * Load from device local disk
     */
    private void loadFromAssets(Task task){
        ImageResource imageResource;
        try{
            imageResource = getComponentManager().getDiskLoadServer().readFromAssets(task, getDecodeHandler(task));
        } catch (Exception e){
            getComponentManager().getServerSettings().getExceptionHandler().onApkLoadCommonException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
            task.setState(Task.State.CANCELED);
            response(task);
            return;
        }
        if (!getComponentManager().getServerSettings().getImageResourceHandler().isValid(imageResource)){
            task.setState(Task.State.CANCELED);
            response(task);
            return;
        }
        getComponentManager().getMemoryCacheServer().put(task.getKey(), imageResource);
        task.setState(Task.State.SUCCEED);
        response(task);
    }

    private void loadFromRes(Task task){
        ImageResource imageResource;
        try{
            imageResource = getComponentManager().getDiskLoadServer().readFromRes(task, getDecodeHandler(task));
        } catch (Exception e){
            getComponentManager().getServerSettings().getExceptionHandler().onApkLoadCommonException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), e, getComponentManager().getLogger());
            task.setState(Task.State.CANCELED);
            response(task);
            return;
        }
        if (!getComponentManager().getServerSettings().getImageResourceHandler().isValid(imageResource)){
            task.setState(Task.State.CANCELED);
            response(task);
            return;
        }
        getComponentManager().getMemoryCacheServer().put(task.getKey(), imageResource);
        task.setState(Task.State.SUCCEED);
        response(task);
    }

    /**
     * Load from disk cache of TILoader
     */
    private void loadFromInnerDiskCache(Task task) {
        ImageResource imageResource;
        try{
            imageResource = getComponentManager().getDiskCacheServer().read(task, getDecodeHandler(task));
        } catch (Exception e){
            getComponentManager().getServerSettings().getExceptionHandler().onDiskCacheCommonException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), e, getComponentManager().getLogger());
            task.setState(Task.State.FAILED);
            response(task);
            return;
        }
        if (!getComponentManager().getServerSettings().getImageResourceHandler().isValid(imageResource)){
            task.setState(Task.State.FAILED);
            response(task);
            return;
        }
        getComponentManager().getMemoryCacheServer().put(task.getKey(), imageResource);
        task.setState(Task.State.SUCCEED);
        response(task);
    }

    @Override
    protected int getMaxThread() {
        return getComponentManager().getServerSettings().getDiskLoadMaxThread();
    }

    @Override
    public Type getServerType() {
        return Type.DISK_ENGINE;
    }
}
