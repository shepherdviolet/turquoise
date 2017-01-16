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

package sviolet.turquoise.utilx.lifecycle;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import sviolet.turquoise.util.common.ConcurrentUtils;

/**
 * 生命周期管理器实现<p/>
 *
 * use{@link LifeCycleUtils}<p/>
 *
 * Created by S.Violet on 2015/11/24.
 */
class LifeCycleManagerImpl implements LifeCycleManager {

    private final Map<String, LifeCycle> components = new ConcurrentHashMap<>();//生命周期监听器
    private final Set<LifeCycle> weakListeners = Collections.newSetFromMap(new WeakHashMap<LifeCycle, Boolean>());//生命周期监听器(弱引用)

    LifeCycleManagerImpl() {
    }

    @Override
    public void addComponent(String componentName, LifeCycle component) {
        components.put(componentName, component);
    }

    @Override
    public LifeCycle getComponent(String componentName) {
        return components.get(componentName);
    }

    @Override
    public void removeComponent(String componentName) {
        components.remove(componentName);
    }

    @Override
    public void removeComponent(LifeCycle component){
        components.values().remove(component);
    }

    @Override
    public void addWeakListener(LifeCycle listener) {
        weakListeners.add(listener);
    }

    @Override
    public void removeWeakListener(LifeCycle listener) {
        weakListeners.remove(listener);
    }

    @Override
    public void onCreate() {
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(components.values())){
            listener.onCreate();
        }
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(weakListeners)){
            listener.onCreate();
        }
    }

    @Override
    public void onStart() {
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(components.values())){
            listener.onStart();
        }
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(weakListeners)){
            listener.onStart();
        }
    }

    @Override
    public void onResume() {
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(components.values())){
            listener.onResume();
        }
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(weakListeners)){
            listener.onResume();
        }
    }

    @Override
    public void onPause() {
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(components.values())){
            listener.onPause();
        }
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(weakListeners)){
            listener.onPause();
        }
    }

    @Override
    public void onStop() {
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(components.values())){
            listener.onStop();
        }
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(weakListeners)){
            listener.onStop();
        }
    }

    @Override
    public void onDestroy() {
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(components.values())){
            listener.onDestroy();
        }
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(weakListeners)){
            listener.onDestroy();
        }
        //销毁时移除所有监听器
        components.clear();
        weakListeners.clear();
    }

}
