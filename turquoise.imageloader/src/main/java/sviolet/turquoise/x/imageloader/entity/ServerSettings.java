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

package sviolet.turquoise.x.imageloader.entity;

import android.content.Context;

import java.io.File;

import sviolet.turquoise.util.common.CheckUtils;
import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.util.droid.DirectoryUtils;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.drawable.BackgroundDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.FailedDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.LoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.def.DefaultBackgroundDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.def.DefaultFailedDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.def.DefaultLoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.handler.ExceptionHandler;
import sviolet.turquoise.x.imageloader.handler.ImageResourceHandler;
import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;
import sviolet.turquoise.x.imageloader.handler.def.DefaultDecodeHandler;
import sviolet.turquoise.x.imageloader.handler.def.DefaultExceptionHandler;
import sviolet.turquoise.x.imageloader.handler.def.DefaultImageResourceHandler;
import sviolet.turquoise.x.imageloader.handler.def.DefaultNetworkLoadHandler;
import sviolet.turquoise.x.imageloader.node.TaskFactory;
import sviolet.turquoise.x.imageloader.node.TaskFactoryImpl;
import sviolet.turquoise.x.imageloader.stub.StubFactory;
import sviolet.turquoise.x.imageloader.stub.StubFactoryImpl;

/**
 *
 * Created by S.Violet on 2016/2/16.
 */
public class ServerSettings implements ComponentManager.Component{

    private static class Values{

        //settings////////////////////////////////////////////////////////////////////////////

        private boolean logEnabled = DEFAULT_LOG_ENABLED;
        private boolean wipeDiskCacheWhenUpdate = DEFAULT_WIPE_DISK_CACHE_WHEN_UPDATE;
        private int memoryCacheSize = DEFAULT_MEMORY_CACHE_SIZE;
        private int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        private int networkLoadMaxThread = DEFAULT_NETWORK_LOAD_MAX_THREAD;
        private int diskLoadMaxThread = DEFAULT_DISK_LOAD_MAX_THREAD;
        private File diskCachePath = null;

        //handler////////////////////////////////////////////////////////////////////////////

        private ImageResourceHandler imageResourceHandler = new DefaultImageResourceHandler();
        private NetworkLoadHandler networkLoadHandler = new DefaultNetworkLoadHandler();
        private DecodeHandler decodeHandler = new DefaultDecodeHandler();
        private ExceptionHandler exceptionHandler = new DefaultExceptionHandler();

        //configurable factory////////////////////////////////////////////////////////////////////////////

        private final StubFactoryImpl stubFactory = new StubFactoryImpl();
        private LoadingDrawableFactory loadingDrawableFactory;
        private FailedDrawableFactory failedDrawableFactory;
        private BackgroundDrawableFactory backgroundDrawableFactory = new DefaultBackgroundDrawableFactory();

        //static factory////////////////////////////////////////////////////////////////////////////

        private final TaskFactory taskFactory = new TaskFactoryImpl();
    }

    public static class Builder{

        private Values values;

        public Builder(){
            values = new Values();
        }

        public ServerSettings build(){
            return new ServerSettings(values);
        }

        //settings////////////////////////////////////////////////////////////////////////////

        /**
         * set the max thread of network loading
         * @param maxThread max thread num, >=1
         */
        public Builder setNetworkLoadMaxThread(int maxThread){
            if (maxThread < 1){
                throw new RuntimeException("[ServerSettings]networkLoadMaxThread must >= 1");
            }
            values.networkLoadMaxThread = maxThread;
            return this;
        }

        /**
         * set the max thread of disk loading
         * @param maxThread max thread num, >=1
         */
        public Builder setDiskLoadMaxThread(int maxThread){
            if (maxThread < 1){
                throw new RuntimeException("[ServerSettings]diskLoadMaxThread must >= 1");
            }
            values.diskLoadMaxThread = maxThread;
            return this;
        }

        /**
         * set the memory cache size by percent of app's MemoryClass
         * @param context context
         * @param percent percent of app's MemoryClass (0f-0.5f)
         */
        public Builder setMemoryCachePercent(Context context, float percent){
            if (context == null){
                throw new RuntimeException("[ServerSettings]setMemoryCachePercent:　context is null!");
            }
            //控制上下限
            if (percent < 0){
                percent = 0;
            }else if (percent > 0.5f){
                percent = 0.5f;
            }
            //应用可用内存级别
            final int memoryClass = DeviceUtils.getMemoryClass(context);
            //计算缓存大小
            values.memoryCacheSize = (int) (1024 * 1024 * memoryClass * percent);
            return this;
        }

        /**
         * set the disk cache size
         * @param sizeMb mb, > 0
         */
        public Builder setDiskCacheSize(float sizeMb){
            //控制上下限
            if (sizeMb <= 0){
                throw new RuntimeException("[ServerSettings]setDiskCacheSize: size must be >0");
            }
            values.diskCacheSize = (int) (sizeMb * 1024 * 1024);
            return this;
        }

        public Builder setDiskCachePath(Context context, DiskCachePath diskCachePath, String subPath){
            if (context == null){
                throw new RuntimeException("[ServerSettings]setDiskCachePath:　context is null!");
            }
            values.diskCachePath = fetchDiskCachePath(context, diskCachePath, subPath);
            return this;
        }

        //handler////////////////////////////////////////////////////////////////////////////

        //configurable factory////////////////////////////////////////////////////////////////////////////

        public Builder setCustomStubFactory(StubFactory customStubFactory){
            values.stubFactory.setCustomStubFactory(customStubFactory);
            return this;
        }

        public Builder setLoadingDrawableFactory(LoadingDrawableFactory factory){
            if (factory != null){
                values.loadingDrawableFactory = factory;
            }
            return this;
        }

        public Builder setFailedDrawableFactory(FailedDrawableFactory factory){
            if (factory != null){
                values.failedDrawableFactory = factory;
            }
            return this;
        }

        public Builder setBackgroundImageResId(int backgroundImageResId){
            values.backgroundDrawableFactory.setBackgroundImageResId(backgroundImageResId);
            return this;
        }

        public Builder setBackgroundColor(int backgroundColor){
            values.backgroundDrawableFactory.setBackgroundColor(backgroundColor);
            return this;
        }

    }

    //DEFAULT/////////////////////////////////////////////////////////////////////////////

    public static final boolean DEFAULT_LOG_ENABLED = true;
    public static final boolean DEFAULT_WIPE_DISK_CACHE_WHEN_UPDATE = false;
    public static final int DEFAULT_MEMORY_CACHE_SIZE = 0;
    public static final int DEFAULT_DISK_CACHE_SIZE = 10 * 1024 * 1024;
    public static final int DEFAULT_NETWORK_LOAD_MAX_THREAD = 3;
    public static final int DEFAULT_DISK_LOAD_MAX_THREAD = 10;

    public static final DiskCachePath DEFAULT_DISK_CACHE_PATH = DiskCachePath.INNER_STORAGE;
    public static final String DEFAULT_DISK_CACHE_SUB_PATH = "TILoader";

    //Var/////////////////////////////////////////////////////////////////////////////////

    private ComponentManager manager;
    private Values values;

    private ServerSettings(Values values) {
        this.values = values;
    }

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
        values.taskFactory.init(manager);
        if (values.loadingDrawableFactory == null) {
            values.loadingDrawableFactory = new DefaultLoadingDrawableFactory();
        }
        if(values.failedDrawableFactory == null) {
            values.failedDrawableFactory = new DefaultFailedDrawableFactory();
        }
    }

    //settings////////////////////////////////////////////////////////////////////////////

    public boolean isLogEnabled(){
        return values.logEnabled;
    }

    public boolean isWipeDiskCacheWhenUpdate(){
        return values.wipeDiskCacheWhenUpdate;
    }

    public int getMemoryCacheSize(){
        return values.memoryCacheSize;
    }

    public int getDiskCacheSize(){
        return values.diskCacheSize;
    }

    public int getNetworkLoadMaxThread(){
        return values.networkLoadMaxThread;
    }

    public int getDiskLoadMaxThread(){
        return values.diskLoadMaxThread;
    }

    public File getDiskCachePath(){
        if (values.diskCachePath == null){
            values.diskCachePath = fetchDiskCachePath(manager.getApplicationContextImage(), DEFAULT_DISK_CACHE_PATH, null);
        }
        return values.diskCachePath;
    }

    //handler////////////////////////////////////////////////////////////////////////////

    public ImageResourceHandler getImageResourceHandler(){
        return values.imageResourceHandler;
    }

    public NetworkLoadHandler getNetworkLoadHandler(){
        return values.networkLoadHandler;
    }

    public DecodeHandler getDecodeHandler(){
        return values.decodeHandler;
    }

    public ExceptionHandler getExceptionHandler(){
        return values.exceptionHandler;
    }

    //configurable factory////////////////////////////////////////////////////////////////////////////

    public StubFactory getStubFactory(){
        return values.stubFactory;
    }

    public LoadingDrawableFactory getLoadingDrawableFactory(){
        return values.loadingDrawableFactory;
    }

    public FailedDrawableFactory getFailedDrawableFactory(){
        return values.failedDrawableFactory;
    }

    public BackgroundDrawableFactory getBackgroundDrawableFactory(){
        return values.backgroundDrawableFactory;
    }

    //static factory////////////////////////////////////////////////////////////////////////////

    public TaskFactory getTaskFactory(){
        return values.taskFactory;
    }

    /*************************************************************
     * enum
     */

    public enum DiskCachePath{
        INNER_STORAGE,
        EXTERNAL_STORAGE
    }

    /**************************************************************
     * private
     */

    private static File fetchDiskCachePath(Context context, DiskCachePath diskCachePath, String subPath){
        if (CheckUtils.isEmpty(subPath)){
            subPath = DEFAULT_DISK_CACHE_SUB_PATH;
        }
        switch (diskCachePath){
            case EXTERNAL_STORAGE:
                return DirectoryUtils.getCacheDir(context, subPath);
            case INNER_STORAGE:
            default:
                return new File(DirectoryUtils.getInnerCacheDir(context).getAbsolutePath() + File.separator + subPath);
        }
    }

}
