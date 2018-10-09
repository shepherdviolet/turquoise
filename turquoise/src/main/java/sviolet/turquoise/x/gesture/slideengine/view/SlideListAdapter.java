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

package sviolet.turquoise.x.gesture.slideengine.view;

/**
 * 
 * 可左右滑动的ListView配套适配器(接口)
 * 
 * Created by S.Violet on 2015/6/25.
 */
public interface SlideListAdapter {

    /**
     * 
     * [功能]:上下滑动时复位被滑动过的单元项]<br/>
     * 返回false不启用功能<br/>
     * [实现提示]:<br/>
     * 判断所有的子View是否被滑动, 若有一个被滑动过则返回true, 否则返回false<br/>
     * 
     */
    public abstract boolean hasSliddenItem();

    /**
     * 
     * [功能]:上下滑动时复位被滑动过的单元项]<br/>
     * [实现提示]:<br/>
     * 将所有的子View重置为未滑动过的状态<br/>
     * 
     */
    public abstract void resetSliddenItem();

}
