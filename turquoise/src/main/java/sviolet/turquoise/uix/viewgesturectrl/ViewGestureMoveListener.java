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
 * <p>ViewGestureController移动事件监听器</p>
 *
 * Created by S.Violet on 2016/9/21.
 */

public interface ViewGestureMoveListener {

    /**
     * 移动开始
     */
    void holdMove();

    /**
     * 移动释放, 可用于做惯性滑动
     * @param velocityX X轴方向速度
     * @param velocityY Y轴方向速度
     */
    void releaseMove(float velocityX, float velocityY);

    /**
     * 移动
     * @param currentX x轴方向当前位置(触点位置)
     * @param offsetX x轴方向位移量
     * @param velocityX x轴方向速度
     * @param currentY y轴方向当前位置(触点位置)
     * @param offsetY y轴方向位移量
     * @param velocityY y轴方向速度
     */
    void move(float currentX, float offsetX, float velocityX, float currentY, float offsetY, float velocityY);

}
