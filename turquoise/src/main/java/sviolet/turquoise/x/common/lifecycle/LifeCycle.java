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

import sviolet.thistle.entity.common.Destroyable;

/**
 * 生命周期监听器(Android Activity生命周期监听)
 *
 * Created by S.Violet on 2015/11/24.
 */
public interface LifeCycle extends Destroyable {

    void onCreate();

    void onStart();

    void onResume();

    void onPause();

    void onStop();

}
