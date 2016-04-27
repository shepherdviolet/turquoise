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

import sviolet.turquoise.utilx.lifecycle.listener.Destroyable;
import sviolet.turquoise.x.imageloader.drawable.BackgroundDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.FailedDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.LoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.common.CommonBackgroundDrawableFactory;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;

/**
 * <p>Settings for Node only</p>
 *
 * Created by S.Violet on 2016/2/16.
 */
public class NodeSettings implements Destroyable {

    private static class Values{

        //settings////////////////////////////////////////////////////////////////////////////

        private int memoryQueueSize = DEFAULT_MEMORY_QUEUE_SIZE;
        private int diskQueueSize = DEFAULT_DISK_QUEUE_SIZE;
        private int netQueueSize = DEFAULT_NET_QUEUE_SIZE;
        private int reloadTimes = DEFAULT_RELOAD_TIMES;
        private long networkConnectTimeout = DEFAULT_NETWORK_CONNECT_TIMEOUT;
        private long networkReadTimeout = DEFAULT_NETWORK_READ_TIMEOUT;
        private int imageAppearDuration = DEFAULT_IMAGE_APPEAR_DURATION;

        //handler////////////////////////////////////////////////////////////////////////////

        private NetworkLoadHandler networkLoadHandler;
        private DecodeHandler decodeHandler;

        //configurable factory////////////////////////////////////////////////////////////////////////////

        private LoadingDrawableFactory loadingDrawableFactory;
        private FailedDrawableFactory failedDrawableFactory;
        private BackgroundDrawableFactory backgroundDrawableFactory;
    }

    public static class Builder{

        private Values values;

        public Builder(){
            values = new Values();
        }

        public NodeSettings build(){
            return new NodeSettings(values);
        }

        //settings////////////////////////////////////////////////////////////////////////////

        /**
         * set memory load queue size, increase if you want to make screen accommodate more pictures
         * @param memoryQueueSize {@value DEFAULT_MEMORY_QUEUE_SIZE} by default
         */
        public Builder setMemoryQueueSize(int memoryQueueSize){
            if (memoryQueueSize < 1){
                throw new RuntimeException("[NodeSettings]memoryQueueSize must >= 1");
            }
            values.memoryQueueSize = memoryQueueSize;
            return this;
        }

        /**
         * set disk load queue size, increase if you want to make screen accommodate more pictures
         * @param diskQueueSize {@value DEFAULT_DISK_QUEUE_SIZE} by default
         */
        public Builder setDiskQueueSize(int diskQueueSize){
            if (diskQueueSize < 1){
                throw new RuntimeException("[NodeSettings]diskQueueSize must >= 1");
            }
            values.diskQueueSize = diskQueueSize;
            return this;
        }

        /**
         * set network load queue size, increase if you want to make screen accommodate more pictures
         * @param netQueueSize {@value DEFAULT_NET_QUEUE_SIZE} by default
         */
        public Builder setNetQueueSize(int netQueueSize){
            if (netQueueSize < 1){
                throw new RuntimeException("[NodeSettings]netQueueSize must >= 1");
            }
            values.netQueueSize = netQueueSize;
            return this;
        }

        /**
         * @param reloadTimes reload times (reload when load failed)
         */
        public Builder setReloadTimes(int reloadTimes){
            if (reloadTimes < 0){
                throw new RuntimeException("[NodeSettings]reloadTimes must >= 0");
            }
            values.reloadTimes = reloadTimes;
            return this;
        }

        /**
         * set timeout of network connect
         * @param timeout timeout milli second
         */
        public Builder setNetworkConnectTimeout(long timeout){
            if (timeout <= 0){
                throw new RuntimeException("[ServerSettings]connect timeout must > 0");
            }
            values.networkConnectTimeout = timeout;
            return this;
        }

        /**
         * set timeout of network read
         * @param timeout timeout milli second
         */
        public Builder setNetworkReadTimeout(long timeout){
            if (timeout <= 0){
                throw new RuntimeException("[ServerSettings]read timeout must > 0");
            }
            values.networkReadTimeout = timeout;
            return this;
        }

        /**
         * @param imageAppearDuration duration of image appear animation
         */
        public Builder setImageAppearDuration(int imageAppearDuration){
            if (imageAppearDuration < 0){
                throw new RuntimeException("[NodeSettings]imageAppearDuration must >= 0");
            }
            values.imageAppearDuration = imageAppearDuration;
            return this;
        }

        //handler////////////////////////////////////////////////////////////////////////////

        /**
         * @param networkLoadHandler custom network load implementation, for node only
         */
        public Builder setNetworkLoadHandler(NetworkLoadHandler networkLoadHandler){
            values.networkLoadHandler = networkLoadHandler;
            return this;
        }

        /**
         * @param decodeHandler custom decode implementation, for node only
         */
        public Builder setDecodeHandler(DecodeHandler decodeHandler){
            values.decodeHandler = decodeHandler;
            return this;
        }

        //configurable factory////////////////////////////////////////////////////////////////////////////

        /**
         * show when image loading, for node only
         * @param factory loadingDrawable factory
         */
        public Builder setLoadingDrawableFactory(LoadingDrawableFactory factory){
            values.loadingDrawableFactory = factory;
            return this;
        }

        /**
         * show when image load failed, for node only
         * @param factory failedDrawable factory
         */
        public Builder setFailedDrawableFactory(FailedDrawableFactory factory){
            values.failedDrawableFactory = factory;
            return this;
        }

        /**
         * for image's background, for node only
         * @param backgroundImageResId image resource id
         */
        public Builder setBackgroundImageResId(int backgroundImageResId){
            if (values.backgroundDrawableFactory == null){
                values.backgroundDrawableFactory = new CommonBackgroundDrawableFactory();
            }
            values.backgroundDrawableFactory.setBackgroundImageResId(backgroundImageResId);
            return this;
        }

        /**
         * for image's background, for node only
         * @param backgroundColor color
         */
        public Builder setBackgroundColor(int backgroundColor){
            if (values.backgroundDrawableFactory == null){
                values.backgroundDrawableFactory = new CommonBackgroundDrawableFactory();
            }
            values.backgroundDrawableFactory.setBackgroundColor(backgroundColor);
            return this;
        }

    }

    //DEFAULT/////////////////////////////////////////////////////////////////////////////

    private static final int DEFAULT_MEMORY_QUEUE_SIZE = 10;
    private static final int DEFAULT_DISK_QUEUE_SIZE = 15;
    private static final int DEFAULT_NET_QUEUE_SIZE = 20;
    private static final int DEFAULT_RELOAD_TIMES = 2;
    private static final int DEFAULT_IMAGE_APPEAR_DURATION = 400;
    public static final long DEFAULT_NETWORK_CONNECT_TIMEOUT = -1;//ms
    public static final long DEFAULT_NETWORK_READ_TIMEOUT = -1;//ms

    private Values values;

    private NodeSettings(Values values) {
        this.values = values;
    }

    @Override
    public void onDestroy() {
        if (values.loadingDrawableFactory != null){
            values.loadingDrawableFactory.onDestroy();
        }
        if (values.failedDrawableFactory != null){
            values.failedDrawableFactory.onDestroy();
        }
        if (values.backgroundDrawableFactory != null){
            values.backgroundDrawableFactory.onDestroy();
        }
    }

    //settings////////////////////////////////////////////////////////////////////////////

    public int getMemoryQueueSize(){
        return values.memoryQueueSize;
    }

    public int getDiskQueueSize(){
        return values.diskQueueSize;
    }

    public int getNetQueueSize(){
        return values.netQueueSize;
    }

    public int getReloadTimes(){
        return values.reloadTimes;
    }

    /**
     * @return milli second
     */
    public long getNetworkConnectTimeout(){
        return values.networkConnectTimeout;
    }

    /**
     * @return milli second
     */
    public long getNetworkReadTimeout(){
        return values.networkReadTimeout;
    }

    public int getImageAppearDuration() {
        return values.imageAppearDuration;
    }

    //handler////////////////////////////////////////////////////////////////////////////

    public NetworkLoadHandler getNetworkLoadHandler(){
        return values.networkLoadHandler;
    }

    public DecodeHandler getDecodeHandler(){
        return values.decodeHandler;
    }

    //configurable factory////////////////////////////////////////////////////////////////////////////

    public LoadingDrawableFactory getLoadingDrawableFactory(){
        return values.loadingDrawableFactory;
    }

    public FailedDrawableFactory getFailedDrawableFactory(){
        return values.failedDrawableFactory;
    }

    public BackgroundDrawableFactory getBackgroundDrawableFactory(){
        return values.backgroundDrawableFactory;
    }

}
