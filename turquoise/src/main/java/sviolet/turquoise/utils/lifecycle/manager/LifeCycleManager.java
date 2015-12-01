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

package sviolet.turquoise.utils.lifecycle.manager;

import sviolet.turquoise.utils.lifecycle.LifeCycleUtils;
import sviolet.turquoise.utils.lifecycle.listener.LifeCycle;

/**
 * 生命周期管理器<p/>
 *
 * use{@link LifeCycleUtils}<p/>
 *
 * Created by S.Violet on 2015/11/24.
 */
public interface LifeCycleManager {

    public static final String FRAGMENT_TAG = "TURQUOISE_LIFE_CYCLE_FRAGMENT";

    public static final String MANAGER_TAG = "TURQUOISE_LIFE_CYCLE_MANAGER";

    /**
     * @param listener 添加生命周期监听器
     */
    void addListener(LifeCycle listener);

    /**
     * @param listener 移除生命周期监听器
     */
    void removeListener(LifeCycle listener);

    void onCreate();

    void onStart();

    void onResume();

    void onPause();

    void onStop();

    void onDestroy();

}
