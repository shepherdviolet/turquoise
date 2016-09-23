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

package sviolet.turquoise.uix.viewgesturectrl;

/**
 * <p>触点信息</p>
 *
 * Created by S.Violet on 2016/9/22.
 */

class ViewGestureTouchPoint {

    //按下时的坐标
    int downX = 0;
    int downY = 0;

    //当前坐标
    int currX = 0;
    int currY = 0;

    //单次位移
    int stepX = 0;
    int stepY = 0;

    //是否产生了有效移动
    boolean isEffectiveMoved = false;

    @Override
    public String toString() {
        return "down:[" + downX + "," + downY + "] curr:[" + currX + "," + currY + "] step[" + stepX + "," + stepY + "] moved:" + isEffectiveMoved;
    }
}
