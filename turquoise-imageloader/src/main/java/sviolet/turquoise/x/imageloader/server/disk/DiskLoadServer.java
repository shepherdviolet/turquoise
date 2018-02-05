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

package sviolet.turquoise.x.imageloader.server.disk;

import android.content.res.AssetFileDescriptor;

import java.io.File;

import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.Server;

/**
 * <p>Load image from device local disk (for SourceType.LOCAL_DISK)</p>
 *
 * @author S.Violet
 */
public class DiskLoadServer implements ComponentManager.Component, Server {

    private ComponentManager manager;

    private boolean initialized = false;

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
    }

    private void initialize(){
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {

                    //do nothing

                    //finish
                    getComponentManager().getLogger().i("[DiskLoadServer]initialized");
                    initialized = true;
                }
            }
        }
    }

    @Override
    public Type getServerType() {
        return Type.DISK_LOAD;
    }

    /**
     * Read Image from device local disk
     * @param task task
     * @param decodeHandler used to decode file
     * @return ImageResource, might be null
     */
    public ImageResource readFromLocalDisk(Task task, DecodeHandler decodeHandler){
        initialize();
        //fetch cache file
        File targetFile = new File(task.getUrl());
        if (!targetFile.exists()){
            getComponentManager().getServerSettings().getExceptionHandler().onLocalDiskLoadNotExistsException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                    new Exception("[TILoader]Loading from local disk failed, file not found:" + task.getUrl()), getComponentManager().getLogger());
            return null;
        }
        //decode
        try {
            ImageResource imageResource = decodeHandler.decode(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(),
                    task, targetFile, getComponentManager().getLogger());
            if (imageResource == null) {
                getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                        new Exception("[TILoader]Decoding failed, return null or invalid ImageResource"), getComponentManager().getLogger());
                return null;
            }
            return imageResource;
        } catch (Throwable t) {
            getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), t, getComponentManager().getLogger());
            return null;
        }
    }

    /**
     * Read Image from apk assets
     * @param task task
     * @param decodeHandler used to decode file
     * @return ImageResource, might be null
     */
    public ImageResource readFromAssets(Task task, DecodeHandler decodeHandler){
        initialize();
        //check if exists
        AssetFileDescriptor descriptor = null;
        try {
            descriptor = getComponentManager().getApplicationContextImage().getAssets().openFd(task.getUrl());
        } catch (Exception e) {
            getComponentManager().getServerSettings().getExceptionHandler().onApkLoadNotExistsException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                    new Exception("[TILoader]Loading from apk assets failed, file not found (or error):" + task.getUrl(), e), getComponentManager().getLogger());
            return null;
        } finally {
            if (descriptor != null){
                try {
                    descriptor.close();
                } catch (Exception ignore){
                }
            }
        }
        //decode
        try {
            ImageResource imageResource = decodeHandler.decodeAssets(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task, task.getUrl(), getComponentManager().getLogger());
            if (imageResource == null) {
                getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                        new Exception("[TILoader]Decoding failed, return null or invalid ImageResource"), getComponentManager().getLogger());
                return null;
            }
            return imageResource;
        } catch (Throwable t) {
            getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), t, getComponentManager().getLogger());
            return null;
        }
    }

    /**
     * Read Image from apk resources
     * @param task task
     * @param decodeHandler used to decode file
     * @return ImageResource, might be null
     */
    public ImageResource readFromRes(Task task, DecodeHandler decodeHandler){
        initialize();
        //parse res id
        int resId = getComponentManager().getApplicationContextImage().getResources().getIdentifier(
                getComponentManager().getApplicationContextImage().getPackageName() + ":" + task.getUrl(), null, null);
        if (resId <= 0) {
            getComponentManager().getServerSettings().getExceptionHandler().onApkLoadNotExistsException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                    new Exception("[TILoader]Loading from apk res failed, res not found:" + task.getUrl()), getComponentManager().getLogger());
            return null;
        }
        //decode
        try {
            ImageResource imageResource = decodeHandler.decodeRes(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task, resId, getComponentManager().getLogger());
            if (imageResource == null) {
                getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(),
                        new Exception("[TILoader]Decoding failed, return null or invalid ImageResource"), getComponentManager().getLogger());
                return null;
            }
            return imageResource;
        } catch (Throwable t) {
            getComponentManager().getServerSettings().getExceptionHandler().onDecodeException(getComponentManager().getApplicationContextImage(), getComponentManager().getContextImage(), task.getTaskInfo(), t, getComponentManager().getLogger());
            return null;
        }
    }

    private ComponentManager getComponentManager(){
        return manager;
    }

}
