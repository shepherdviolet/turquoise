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

package sviolet.turquoise.x.imageloader.entity;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import sviolet.turquoise.util.droid.NetStateUtils;
import sviolet.turquoise.x.imageloader.handler.ExceptionHandler;

/**
 * <p>[Senior Setting]</p>
 *
 * <p>Some times, network speed is very slow, but uninterruptedly, it will hardly to cause read-timeout exception,
 * In order to avoid this situation, TILoader will cancel load task with slow speed.</p>
 *
 * <p>At the beginning of loading, task will keep loading in any case, even if the speed is very slow,
 * we called it "windowPeriod". After "windowPeriod", it start to check loading speed.
 * If the speed is slower than "thresholdSpeed", task will be canceled. (you can override
 * {@link ExceptionHandler#handleLowNetworkSpeedEvent} method to handle this event).
 * If the speed is faster than "thresholdSpeed", we will try to get data length from http-header,
 * in order to calculate progress of task. If we found that the speed is too slow to finish task
 * before "deadline", we will cancel task in advance.(override {@link ExceptionHandler#handleLowNetworkSpeedEvent}
 * method to handle this event).</p>
 *
 * <p>Finally, task will be canceled when reach the "deadline".</p>
 *
 * <p>***************************************************************************************************</p>
 *
 * <p>You can adjust configure by ServerSettings->setLowNetworkSpeedStrategy(). There are several strategies
 * to cope with different network environments. See {@link LowNetworkSpeedStrategy.Type}.</p>
 *
 * <p>"Indispensable" task ({@link Params.Builder#setIndispensable}) has double connection-timeout & read-timeout,
 * and loading with {@link LowNetworkSpeedStrategy.Type#INDISPENSABLE_TASK} strategy. Generally, it is used for
 * loading large image and gif.</p></p>
 *
 * <p>***************************************************************************************************</p>
 *
 * <p>Example & Default value::</p>
 *
 * <pre>
 *      LowNetworkSpeedStrategy lowNetworkSpeedStrategy = new LowNetworkSpeedStrategy.Builder()
 *          .setConfigure(LowNetworkSpeedStrategy.Type.LOW_SPEED_MOBILE_NETWORK, {@value WINDOW_PERIOD_LOW_SPEED}, {@value DEADLINE_LOW_SPEED}, {@value THRESHOLD_SPEED_LOW_SPEED})
 *          .setConfigure(LowNetworkSpeedStrategy.Type.HIGH_SPEED_MOBILE_NETWORK, {@value WINDOW_PERIOD_HIGH_SPEED}, {@value DEADLINE_HIGH_SPEED}, {@value THRESHOLD_SPEED_HIGH_SPEED})
 *          .setConfigure(LowNetworkSpeedStrategy.Type.WIFI_NETWORK, {@value WINDOW_PERIOD_WIFI}, {@value DEADLINE_WIFI}, {@value THRESHOLD_SPEED_WIFI})
 *          .setConfigure(LowNetworkSpeedStrategy.Type.INDISPENSABLE_TASK, {@value WINDOW_PERIOD_INDISPENSABLE}, {@value DEADLINE_HIGH_INDISPENSABLE}, {@value THRESHOLD_SPEED_INDISPENSABLE})
 *          .build();
 *      TILoader.setting(new ServerSettings.Builder()
 *          .setLowNetworkSpeedStrategy(lowNetworkSpeedStrategy)
 *          .build());
 * </pre>
 *
 * Created by S.Violet on 2016/5/13.
 */
public class LowNetworkSpeedStrategy {

    public static final long WINDOW_PERIOD_LOW_SPEED = 20 * 1000;//20s
    public static final long DEADLINE_LOW_SPEED = 60 * 1000;//60s
    public static final int THRESHOLD_SPEED_LOW_SPEED = 5 * 1024;//5k/s
    public static final long WINDOW_PERIOD_HIGH_SPEED = 10 * 1000;//10s
    public static final long DEADLINE_HIGH_SPEED = 30 * 1000;//30s
    public static final int THRESHOLD_SPEED_HIGH_SPEED = 20 * 1024;//20k/s
    public static final long WINDOW_PERIOD_WIFI = 10 * 1000;//10s
    public static final long DEADLINE_WIFI = 30 * 1000;//30s
    public static final int THRESHOLD_SPEED_WIFI = 20 * 1024;//20k/s
    public static final long WINDOW_PERIOD_INDISPENSABLE = 40 * 1000;//40s
    public static final long DEADLINE_HIGH_INDISPENSABLE = 120 * 1000;//120s
    public static final int THRESHOLD_SPEED_INDISPENSABLE = 256;//256byte/s

    private Map<Type, Configure> configures;

    private LowNetworkSpeedStrategy(Map<Type, Configure> configures){
        this.configures = configures;
    }

    @SuppressLint("MissingPermission")
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
     * <p>Strategy Type::</p>
     * <p>LOW_SPEED_MOBILE_NETWORK:: Strategy for 2G network.</p>
     * <p>HIGH_SPEED_MOBILE_NETWORK:: Strategy for 3G, 4G, and unknown network.</p>
     * <p>WIFI_NETWORK:: Strategy for WIFI network. </p>
     * <p>INDISPENSABLE_TASK:: Strategy for indispensable task. (As far as possible to complete the task)</p>
     */
    public enum Type{
        LOW_SPEED_MOBILE_NETWORK,
        HIGH_SPEED_MOBILE_NETWORK,
        WIFI_NETWORK,
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
            configures.put(Type.LOW_SPEED_MOBILE_NETWORK, new Configure(Type.LOW_SPEED_MOBILE_NETWORK, WINDOW_PERIOD_LOW_SPEED, DEADLINE_LOW_SPEED, THRESHOLD_SPEED_LOW_SPEED));
            configures.put(Type.HIGH_SPEED_MOBILE_NETWORK, new Configure(Type.HIGH_SPEED_MOBILE_NETWORK, WINDOW_PERIOD_HIGH_SPEED, DEADLINE_HIGH_SPEED, THRESHOLD_SPEED_HIGH_SPEED));
            configures.put(Type.WIFI_NETWORK, new Configure(Type.WIFI_NETWORK, WINDOW_PERIOD_WIFI, DEADLINE_WIFI, THRESHOLD_SPEED_WIFI));
            configures.put(Type.INDISPENSABLE_TASK, new Configure(Type.INDISPENSABLE_TASK, WINDOW_PERIOD_INDISPENSABLE, DEADLINE_HIGH_INDISPENSABLE, THRESHOLD_SPEED_INDISPENSABLE));
        }

        /**
         * <p>[Senior Setting]</p>
         *
         * <p>Some times, network speed is very slow, but uninterruptedly, it will hardly to cause read-timeout exception,
         * In order to avoid this situation, TILoader will cancel load task with slow speed.</p>
         *
         * <p>At the beginning of loading, task will keep loading in any case, even if the speed is very slow,
         * we called it "windowPeriod". After "windowPeriod", it start to check loading speed.
         * If the speed is slower than "thresholdSpeed", task will be canceled. (you can override
         * {@link ExceptionHandler#handleLowNetworkSpeedEvent} method to handle this event).
         * If the speed is faster than "thresholdSpeed", we will try to get data length from http-header,
         * in order to calculate progress of task. If we found that the speed is too slow to finish task
         * before "deadline", we will cancel task in advance.(override {@link ExceptionHandler#handleLowNetworkSpeedEvent}
         * method to handle this event).</p>
         *
         * <p>Finally, task will be canceled when reach the "deadline".</p>
         *
         * <p>***************************************************************************************************</p>
         *
         * <p>You can adjust configure by ServerSettings->setLowNetworkSpeedStrategy(). There are several strategies
         * to cope with different network environments. See {@link LowNetworkSpeedStrategy.Type}.</p>
         *
         * <p>"Indispensable" task ({@link Params.Builder#setIndispensable}) has double connection-timeout & read-timeout,
         * and loading with {@link LowNetworkSpeedStrategy.Type#INDISPENSABLE_TASK} strategy.</p>
         *
         * @param type Strategy Type, nonnull
         * @param windowPeriod ms, >=0
         * @param deadline ms, >windowPeriod
         * @param thresholdSpeed bytes/s, >=0
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
