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

package sviolet.turquoise.x.common.lifecycle;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.thistle.util.concurrent.ConcurrentUtils;

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

    private boolean destroyed = false;

    private ReentrantLock weakListenersLock = new ReentrantLock();

    LifeCycleManagerImpl() {
    }

    @Override
    public void addComponent(String componentName, LifeCycle component) {
        if (destroyed){
            component.onDestroy();
            return;
        }

        components.put(componentName, component);
    }

    @Override
    public LifeCycle getComponent(String componentName) {
        if (destroyed){
            return null;
        }

        return components.get(componentName);
    }

    @Override
    public void removeComponent(String componentName) {
        if (destroyed){
            return;
        }

        components.remove(componentName);
    }

    @Override
    public void removeComponent(LifeCycle component){
        if (destroyed){
            return;
        }

        components.values().remove(component);
    }

    @Override
    public void addWeakListener(LifeCycle listener) {
        if (destroyed){
            listener.onDestroy();
            return;
        }

        try {
            weakListenersLock.lock();
            weakListeners.add(listener);
        } finally {
            weakListenersLock.unlock();
        }

    }

    @Override
    public void removeWeakListener(LifeCycle listener) {
        if (destroyed){
            return;
        }

        try {
            weakListenersLock.lock();
            weakListeners.remove(listener);
        } finally {
            weakListenersLock.unlock();
        }
    }

    @Override
    public void onCreate() {
        if (destroyed){
            return;
        }

        for (LifeCycle listener : ConcurrentUtils.getSnapShot(components.values())){
            listener.onCreate();
        }

        List<LifeCycle> weakListeners;
        try {
            weakListenersLock.lock();
            weakListeners = ConcurrentUtils.getSnapShot(this.weakListeners);
        } finally {
            weakListenersLock.unlock();
        }
        for (LifeCycle listener : weakListeners){
            listener.onCreate();
        }
    }

    @Override
    public void onStart() {
        if (destroyed){
            return;
        }

        for (LifeCycle listener : ConcurrentUtils.getSnapShot(components.values())){
            listener.onStart();
        }

        List<LifeCycle> weakListeners;
        try {
            weakListenersLock.lock();
            weakListeners = ConcurrentUtils.getSnapShot(this.weakListeners);
        } finally {
            weakListenersLock.unlock();
        }
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(weakListeners)){
            listener.onStart();
        }
    }

    @Override
    public void onResume() {
        if (destroyed){
            return;
        }

        for (LifeCycle listener : ConcurrentUtils.getSnapShot(components.values())){
            listener.onResume();
        }

        List<LifeCycle> weakListeners;
        try {
            weakListenersLock.lock();
            weakListeners = ConcurrentUtils.getSnapShot(this.weakListeners);
        } finally {
            weakListenersLock.unlock();
        }
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(weakListeners)){
            listener.onResume();
        }
    }

    @Override
    public void onPause() {
        if (destroyed){
            return;
        }

        for (LifeCycle listener : ConcurrentUtils.getSnapShot(components.values())){
            listener.onPause();
        }

        List<LifeCycle> weakListeners;
        try {
            weakListenersLock.lock();
            weakListeners = ConcurrentUtils.getSnapShot(this.weakListeners);
        } finally {
            weakListenersLock.unlock();
        }
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(weakListeners)){
            listener.onPause();
        }
    }

    @Override
    public void onStop() {
        if (destroyed){
            return;
        }

        for (LifeCycle listener : ConcurrentUtils.getSnapShot(components.values())){
            listener.onStop();
        }

        List<LifeCycle> weakListeners;
        try {
            weakListenersLock.lock();
            weakListeners = ConcurrentUtils.getSnapShot(this.weakListeners);
        } finally {
            weakListenersLock.unlock();
        }
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(weakListeners)){
            listener.onStop();
        }
    }

    @Override
    public void onDestroy() {
        this.destroyed = true;

        for (LifeCycle listener : ConcurrentUtils.getSnapShot(components.values())){
            listener.onDestroy();
        }

        List<LifeCycle> weakListeners;
        try {
            weakListenersLock.lock();
            weakListeners = ConcurrentUtils.getSnapShot(this.weakListeners);
        } finally {
            weakListenersLock.unlock();
        }
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(weakListeners)){
            listener.onDestroy();
        }

        //销毁时移除所有监听器
        components.clear();

        try {
            weakListenersLock.lock();
            weakListeners.clear();
        } finally {
            weakListenersLock.unlock();
        }
    }

}
