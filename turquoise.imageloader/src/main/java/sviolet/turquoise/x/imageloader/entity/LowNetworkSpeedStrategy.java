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

import java.util.HashMap;
import java.util.Map;

import sviolet.turquoise.util.droid.NetStateUtils;

/**
 * TODO
 * Created by S.Violet on 2016/5/13.
 */
public class LowNetworkSpeedStrategy {

    private Map<Type, Configure> configures;

    private LowNetworkSpeedStrategy(Map<Type, Configure> configures){
        this.configures = configures;
    }

    public Configure getConfigure(Context context, IndispensableState indispensableState){
        if (indispensableState.isIndispensable()){
            return configures.get(Type.INDISPENSABLE_TASK);
        }
        NetStateUtils.NetworkType type = NetStateUtils.getNetworkType(context);
        switch (type){
            case MOBILE_2G:
                return configures.get(Type.LOW_SPEED_MOBILE_NETWORK);
            case WIFI:
                return configures.get(Type.WIFI_NETWORK);
            default:
                return configures.get(Type.HIGH_SPEED_MOBILE_NETWORK);
        }
    }

    /**
     * TODO
     */
    public enum Type{
        /**
         * 2G
         */
        LOW_SPEED_MOBILE_NETWORK,
        /**
         * 3G, 4G, unknown mobile
         */
        HIGH_SPEED_MOBILE_NETWORK,
        /**
         * WIFI
         */
        WIFI_NETWORK,
        /**
         * for indispensable task
         */
        INDISPENSABLE_TASK
    }

    public static class Configure{

        private Type type;
        private long windowPeriod;//ms
        private long deadline;//ms
        private int thresholdSpeed;//bytes/s

        private Configure(Type type, long windowPeriod, long deadline, int thresholdSpeed){
            this.type = type;
            this.windowPeriod = windowPeriod;
            this.deadline = deadline;
            this.thresholdSpeed = thresholdSpeed;
        }

        public Type getType() {
            return type;
        }

        public long getWindowPeriod() {
            return windowPeriod;
        }

        public long getDeadline() {
            return deadline;
        }

        public int getThresholdSpeed() {
            return thresholdSpeed;
        }
    }

    public static class Builder{

        private Map<Type, Configure> configures = new HashMap<>();

        public Builder(){
            configures.put(Type.LOW_SPEED_MOBILE_NETWORK, new Configure(Type.LOW_SPEED_MOBILE_NETWORK, 30000, 120000, 5 * 1024));
            configures.put(Type.HIGH_SPEED_MOBILE_NETWORK, new Configure(Type.HIGH_SPEED_MOBILE_NETWORK, 15000, 60000, 20 * 1024));
            configures.put(Type.WIFI_NETWORK, new Configure(Type.WIFI_NETWORK, 15000, 60000, 20 * 1024));
            configures.put(Type.INDISPENSABLE_TASK, new Configure(Type.INDISPENSABLE_TASK, 60000, 300000, 0));
        }

        /**
         * TODO
         * @param type
         * @param windowPeriod
         * @param deadline
         * @param thresholdSpeed
         * @return
         */
        public Builder setConfigure(Type type, long windowPeriod, long deadline, int thresholdSpeed){
            if (type == null){
                throw new RuntimeException("[LowNetworkSpeedStrategy]type must not be null");
            }
            if (windowPeriod < 0){
                throw new RuntimeException("[LowNetworkSpeedStrategy]windowPeriod must >= 0");
            }
            if (deadline <= windowPeriod){
                throw new RuntimeException("[LowNetworkSpeedStrategy]deadline must > windowPeriod");
            }
            if (thresholdSpeed < 0){
                throw new RuntimeException("[LowNetworkSpeedStrategy]thresholdSpeed must >= 0");
            }
            configures.put(type, new Configure(type, windowPeriod, deadline, thresholdSpeed));
            return this;
        }

        public LowNetworkSpeedStrategy build(){
            return new LowNetworkSpeedStrategy(configures);
        }

    }

}
