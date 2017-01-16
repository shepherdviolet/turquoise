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
import android.os.Looper;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import sviolet.turquoise.util.common.ConcurrentUtils;
import sviolet.turquoise.utilx.lifecycle.LifeCycle;
import sviolet.turquoise.utilx.lifecycle.LifeCycleUtils;

/**
 * 事件处理中心
 *
 * Created by S.Violet on 2017/1/16.
 */
class EvCenter {

    private static final String COMPONENT_ID = "TURQUOISE_EV_BUS_STATION_COMPONENT";

    static final EvCenter INSTANCE = new EvCenter();

    private final Set<EvStation> stations = Collections.newSetFromMap(new WeakHashMap<EvStation, Boolean>());

    void post(String id, Object message){
        for (EvStation station : ConcurrentUtils.getSnapShot(stations)){
            station.post(id, message);
        }
    }

    void register(Activity activity, String id, EvBus.Type type, EvReceiver receiver){
        LifeCycle component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            component = new EvStation(activity);
            LifeCycleUtils.addComponent(activity, COMPONENT_ID, component);
            stations.add((EvStation) component);
        }
        EvStation station = (EvStation) component;
        station.register(id, type, receiver);
    }

    void register(Fragment fragment, String id, EvBus.Type type, EvReceiver receiver){
        LifeCycle component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            component = new EvStation(fragment.getActivity());
            LifeCycleUtils.addComponent(fragment.getActivity(), COMPONENT_ID, component);
            stations.add((EvStation) component);
        }
        EvStation station = (EvStation) component;
        station.register(id, type, receiver);
    }

    void register(android.support.v4.app.FragmentActivity activity, String id, EvBus.Type type, EvReceiver receiver){
        LifeCycle component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            component = new EvStation(activity);
            LifeCycleUtils.addComponent(activity, COMPONENT_ID, component);
            stations.add((EvStation) component);
        }
        EvStation station = (EvStation) component;
        station.register(id, type, receiver);
    }

    void register(android.support.v4.app.Fragment fragment, String id, EvBus.Type type, EvReceiver receiver){
        LifeCycle component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            component = new EvStation(fragment.getActivity());
            LifeCycleUtils.addComponent(fragment.getActivity(), COMPONENT_ID, component);
            stations.add((EvStation) component);
        }
        EvStation station = (EvStation) component;
        station.register(id, type, receiver);
    }

}
