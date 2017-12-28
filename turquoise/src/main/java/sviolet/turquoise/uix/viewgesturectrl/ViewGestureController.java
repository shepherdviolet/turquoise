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

package sviolet.turquoise.uix.viewgesturectrl;

import android.view.MotionEvent;

/**
 *
 * <p>View触摸控制器, 捕获View触摸事件, 转换为点击/移动/缩放/旋转事件</p>
 *
 * <p>
 *      #ViewGestureController常用于View自身的触摸控制.<br/>
 *      #SlideEngine常用于ViewGroup拦截触摸事件, 控制其子View滑动.<br/>
 * </p>
 *
 * <p>======================================================================</p>
 *
 * <pre>{@code
 *     //View的onTouchEvent方法中获取触摸事件
 *     public boolean onTouchEvent(MotionEvent event) {
 *          super.onTouchEvent(event);
 *          viewGestureController.onTouchEvent(event);
 *          return true;
 *     }
 * }</pre>
 *
 * Created by S.Violet on 2016/9/21.
 */

public interface ViewGestureController {

    /**
     * <pre>{@code
     *     //View的onTouchEvent方法中获取触摸事件
     *     public boolean onTouchEvent(MotionEvent event) {
     *          super.onTouchEvent(event);
     *          viewGestureController.onTouchEvent(event);
     *          return true;
     *     }
     * }</pre>
     */
    boolean onTouchEvent(MotionEvent event);

    /**
     * <p>添加输出监听器, 该方法不保证能绑定成功, 方法内部通过判断listener实现的接口来绑定事件监听</p>
     */
    void addOutput(ViewGestureOutput listener);

}
