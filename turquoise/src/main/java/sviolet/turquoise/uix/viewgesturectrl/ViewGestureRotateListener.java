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
 * <p>ViewGestureController旋转监听器</p>
 *
 * Created by S.Violet on 2016/9/21.
 */

public interface ViewGestureRotateListener {

    /**
     * 移动开始
     */
    void holdRotate();

    /**
     * 旋转释放, 可用于做惯性滑动
     * @param angularVelocity 角速度, 顺时针为正, 逆时针为负
     */
    void releaseRotate(float angularVelocity);

    /**
     * 旋转
     * @param currentAngle 当前角度, 12点钟位置为0, 顺时针增加, 范围不限定在0~360之间, 可能出现负数或大于360的情况
     * @param angularOffset 角度位移量
     * @param angularVelocity 角速度, 顺时针为正, 逆时针为负
     */
    void rotate(float currentAngle, float angularOffset, float angularVelocity);

}
