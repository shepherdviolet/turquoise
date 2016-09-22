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

package sviolet.turquoise.uix.gesturectrl.view;

import android.view.MotionEvent;

/**
 * Created by S.Violet on 2016/9/22.
 */

public abstract class ViewGestureControllerAbs implements ViewGestureController {

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return true;
    }

    @Override
    public void addOutput(Object listener) {

    }

    public ViewGestureControllerAbs addMoveListener(ViewGestureMoveListener listener){

        return this;
    }

    public ViewGestureControllerAbs addRotateListener(ViewGestureRotateListener listener){

        return this;
    }

    public ViewGestureControllerAbs addZoomListener(ViewGestureZoomListener listener){

        return this;
    }

}
