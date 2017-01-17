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

package sviolet.turquoise.utilx.eventbus;

import android.app.Activity;
import android.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.util.common.ConcurrentUtils;
import sviolet.turquoise.utilx.lifecycle.LifeCycle;
import sviolet.turquoise.utilx.lifecycle.LifeCycleUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * 事件处理中心
 *
 * Created by S.Violet on 2017/1/16.
 */
class EvCenter {

    private static final String COMPONENT_ID = "TURQUOISE_EV_BUS_STATION_COMPONENT";

    static final EvCenter INSTANCE = new EvCenter();

    private TLogger logger = TLogger.get(this);
    private ReentrantLock lock = new ReentrantLock();

    private final Set<EvStation> stations = Collections.newSetFromMap(new WeakHashMap<EvStation, Boolean>());

    void post(Object message){
        boolean result = false;
        //遍历所有station并推送消息
        for (EvStation station : ConcurrentUtils.getSnapShot(stations)){
            if (station.post(message)){
                result = true;
            }
        }
        if (!result){
            logger.d("no receiver handle this message, messageClass:" + message.getClass() + ", message:" + message);
        }
    }

    void register(Activity activity, EvBus.Type type, EvReceiver receiver){
        //获得station
        LifeCycle component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            try{
                lock.lock();
                component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(activity);
                    LifeCycleUtils.addComponent(activity, COMPONENT_ID, component);
                    stations.add((EvStation) component);
                }
            } finally {
                lock.unlock();
            }
        }
        ((EvStation)component).register(type, receiver);
    }

    void register(Fragment fragment, EvBus.Type type, EvReceiver receiver){
        //获得station
        LifeCycle component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            try{
                lock.lock();
                component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(fragment.getActivity());
                    LifeCycleUtils.addComponent(fragment, COMPONENT_ID, component);
                    stations.add((EvStation) component);
                }
            } finally {
                lock.unlock();
            }
        }
        ((EvStation)component).register(type, receiver);
    }

    void register(android.support.v4.app.FragmentActivity activity, EvBus.Type type, EvReceiver receiver){
        //获得station
        LifeCycle component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            try{
                lock.lock();
                component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(activity);
                    LifeCycleUtils.addComponent(activity, COMPONENT_ID, component);
                    stations.add((EvStation) component);
                }
            } finally {
                lock.unlock();
            }
        }
        ((EvStation)component).register(type, receiver);
    }

    void register(android.support.v4.app.Fragment fragment, EvBus.Type type, EvReceiver receiver){
        //获得station
        LifeCycle component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            try{
                lock.lock();
                component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(fragment.getActivity());
                    LifeCycleUtils.addComponent(fragment, COMPONENT_ID, component);
                    stations.add((EvStation) component);
                }
            } finally {
                lock.unlock();
            }
        }
        ((EvStation)component).register(type, receiver);
    }

    /********************************************************************************
     *
     */

    List<Object> withdraw(Class messageType){

        //根据时间倒叙排列， 第一个为最新的消息
        TreeMap<Long, Object> map = new TreeMap<>(comparator);
        //遍历所有station拉取消息
        for (EvStation station : ConcurrentUtils.getSnapShot(stations)){
            EvStation.StoredMessage message = station.withdraw(messageType);
            if (message != null) {
                map.put(message.time, message.message);
            }
        }

        return new ArrayList<>(map.values());
    }

    void store(Activity activity, Object message){
        //获得station
        LifeCycle component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            try{
                lock.lock();
                component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(activity);
                    LifeCycleUtils.addComponent(activity, COMPONENT_ID, component);
                    stations.add((EvStation) component);
                }
            } finally {
                lock.unlock();
            }
        }
        ((EvStation)component).store(message);
    }

    void store(Fragment fragment, Object message){
        //获得station
        LifeCycle component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            try{
                lock.lock();
                component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(fragment.getActivity());
                    LifeCycleUtils.addComponent(fragment, COMPONENT_ID, component);
                    stations.add((EvStation) component);
                }
            } finally {
                lock.unlock();
            }
        }
        ((EvStation)component).store(message);
    }

    void store(android.support.v4.app.FragmentActivity activity, Object message){
        //获得station
        LifeCycle component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            try{
                lock.lock();
                component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(activity);
                    LifeCycleUtils.addComponent(activity, COMPONENT_ID, component);
                    stations.add((EvStation) component);
                }
            } finally {
                lock.unlock();
            }
        }
        ((EvStation)component).store(message);
    }

    void store(android.support.v4.app.Fragment fragment, Object message){
        //获得station
        LifeCycle component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            try{
                lock.lock();
                component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(fragment.getActivity());
                    LifeCycleUtils.addComponent(fragment, COMPONENT_ID, component);
                    stations.add((EvStation) component);
                }
            } finally {
                lock.unlock();
            }
        }
        ((EvStation)component).store(message);
    }

    private Comparator<Long> comparator = new Comparator<Long>() {
        @Override
        public int compare(Long o1, Long o2) {
            return o2.compareTo(o1);
        }
    };

}
