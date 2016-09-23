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
 * <p>ViewGestureController缩放监听器</p>
 *
 * Created by S.Violet on 2016/9/21.
 */

public interface ViewGestureZoomListener {

    /**
     * 缩放开始
     */
    void holdZoom();

    /**
     * 缩放释放
     * @param basicPointX 基点坐标X
     * @param basicPointY 基点坐标Y
     * @param velocity 缩放速度(放大为正, 缩小为负)
     */
    void releaseZoom(int basicPointX, int basicPointY, int velocity);

    /**
     * 缩放
     * @param basicPointX 基点坐标X
     * @param basicPointY 基点坐标Y
     * @param current 当前缩放值(即两点间距)
     * @param offset 缩放偏移量(两点间距变化量)
     * @param velocity 缩放速度(放大为正, 缩小为负)
     */
    void zoom(int basicPointX, int basicPointY, int current, int offset, int velocity);

}
