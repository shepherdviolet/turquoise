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

import sviolet.turquoise.x.imageloader.drawable.BackgroundDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.FailedDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.LoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.handler.def.DefaultDecodeHandler;

/**
 *
 * Created by S.Violet on 2016/2/16.
 */
public class NodeSettings {

    private static final int DEFAULT_DISK_QUEUE_SIZE = 10;
    private static final int DEFAULT_NET_QUEUE_SIZE = 10;
    private static final int DEFAULT_RELOAD_TIMES = 2;

    private Values values;

    private NodeSettings(Values values) {
        this.values = values;
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

    //handler////////////////////////////////////////////////////////////////////////////

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

    private static class Values{

        //settings////////////////////////////////////////////////////////////////////////////

        private int diskQueueSize = DEFAULT_DISK_QUEUE_SIZE;
        private int netQueueSize = DEFAULT_NET_QUEUE_SIZE;
        private int reloadTimes = DEFAULT_RELOAD_TIMES;

        //handler////////////////////////////////////////////////////////////////////////////

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

        //handler////////////////////////////////////////////////////////////////////////////

        //configurable factory////////////////////////////////////////////////////////////////////////////

    }

    public void destroy(){

    }

}
