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
import sviolet.turquoise.x.imageloader.drawable.def.DefaultBackgroundDrawableFactory;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;

/**
 *
 * Created by S.Violet on 2016/2/16.
 */
public class NodeSettings implements Destroyable {


    private static class Values{

        //settings////////////////////////////////////////////////////////////////////////////

        private int diskQueueSize = DEFAULT_DISK_QUEUE_SIZE;
        private int netQueueSize = DEFAULT_NET_QUEUE_SIZE;
        private int reloadTimes = DEFAULT_RELOAD_TIMES;
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

        public Builder setDiskQueueSize(int diskQueueSize){
            if (diskQueueSize < 1){
                throw new RuntimeException("[NodeSettings]diskQueueSize must >= 1");
            }
            values.diskQueueSize = diskQueueSize;
            return this;
        }

        public Builder setNetQueueSize(int netQueueSize){
            if (netQueueSize < 1){
                throw new RuntimeException("[NodeSettings]netQueueSize must >= 1");
            }
            values.netQueueSize = netQueueSize;
            return this;
        }

        //handler////////////////////////////////////////////////////////////////////////////

        //configurable factory////////////////////////////////////////////////////////////////////////////

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
            if (values.backgroundDrawableFactory == null){
                values.backgroundDrawableFactory = new DefaultBackgroundDrawableFactory();
            }
            values.backgroundDrawableFactory.setBackgroundImageResId(backgroundImageResId);
            return this;
        }

        public Builder setBackgroundColor(int backgroundColor){
            if (values.backgroundDrawableFactory == null){
                values.backgroundDrawableFactory = new DefaultBackgroundDrawableFactory();
            }
            values.backgroundDrawableFactory.setBackgroundColor(backgroundColor);
            return this;
        }

    }

    //DEFAULT/////////////////////////////////////////////////////////////////////////////

    private static final int DEFAULT_DISK_QUEUE_SIZE = 10;
    private static final int DEFAULT_NET_QUEUE_SIZE = 10;
    private static final int DEFAULT_RELOAD_TIMES = 2;
    private static final int DEFAULT_IMAGE_APPEAR_DURATION = 400;

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

    public int getDiskQueueSize(){
        return values.diskQueueSize;
    }

    public int getNetQueueSize(){
        return values.netQueueSize;
    }

    public int getReloadTimes(){
        return values.reloadTimes;
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
