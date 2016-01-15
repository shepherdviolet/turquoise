/*
 * Copyright (C) 2015 S.Violet
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

package sviolet.turquoise.util.lifecycle.manager;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import sviolet.turquoise.util.concurrent.ConcurrentUtils;
import sviolet.turquoise.util.lifecycle.LifeCycleUtils;
import sviolet.turquoise.util.lifecycle.listener.LifeCycle;

/**
 * 声明周期管理器实现<p/>
 *
 * use{@link LifeCycleUtils}<p/>
 *
 * Created by S.Violet on 2015/11/24.
 */
public class LifeCycleManagerImpl implements LifeCycleManager {

    //生命周期监听器(弱引用)
    private final Set<LifeCycle> listeners = Collections.newSetFromMap(new WeakHashMap<LifeCycle, Boolean>());

    @Override
    public void addListener(LifeCycle listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(LifeCycle listener) {
        listeners.remove(listener);
    }

    @Override
    public void onCreate() {
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(listeners)){
            listener.onCreate();
        }
    }

    @Override
    public void onStart() {
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(listeners)){
            listener.onStart();
        }
    }

    @Override
    public void onResume() {
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(listeners)){
            listener.onResume();
        }
    }

    @Override
    public void onPause() {
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(listeners)){
            listener.onPause();
        }
    }

    @Override
    public void onStop() {
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(listeners)){
            listener.onStop();
        }
    }

    @Override
    public void onDestroy() {
        for (LifeCycle listener : ConcurrentUtils.getSnapShot(listeners)){
            listener.onDestroy();
        }
        //销毁时移除所有监听器
        listeners.clear();
    }

}
