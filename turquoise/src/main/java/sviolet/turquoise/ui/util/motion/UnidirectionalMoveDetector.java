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

package sviolet.turquoise.ui.util.motion;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;

/**
 * <p>MotionEvent用, 单向有效移动侦测器, 当一个方向发生有效位移后, 会保持这个状态直到触摸流程结束, 用于
 * 拆分XY两个方向的触摸事件.</p>
 *
 * Created by S.Violet on 2016/11/24.
 */

public class UnidirectionalMoveDetector {

    public static final int STATE_X_MOVEING = -2;//X方向移动状态
    public static final int STATE_TO_X_MOVE = -1;//刚进入X方向移动状态
    public static final int STATE_STATIC = 0;//静止状态
    public static final int STATE_TO_Y_MOVE = 1;//刚进入Y方向移动状态
    public static final int STATE_Y_MOVEING = 2;//Y方向移动状态

    private float mTouchSlop;
    private float downX, downY;
    private int state = STATE_STATIC;

    public UnidirectionalMoveDetector(Context context) {
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * 持续收集MotionEvent, 并在ACTION_UP事件时判断是否发生位移, 若没有发生位移, 判断为点击事件时, 返回true
     */
    public int detect(MotionEvent event){
        switch (event.getActionMasked()){
            case ACTION_DOWN:
                //按下事件记录按下的坐标
                state = STATE_STATIC;
                downX = event.getX();
                downY = event.getY();
                break;
            case ACTION_MOVE:
                switch (state){
                    case STATE_STATIC:
                        //发生有效位移时, 取消点击事件
                        if (Math.abs(event.getX() - downX) > mTouchSlop){
                            state = STATE_TO_X_MOVE;
                        }
                        if (Math.abs(event.getY() - downY) > mTouchSlop) {
                            state = STATE_TO_Y_MOVE;
                        }
                        break;
                    case STATE_TO_X_MOVE:
                        state = STATE_X_MOVEING;
                        break;
                    case STATE_TO_Y_MOVE:
                        state = STATE_Y_MOVEING;
                        break;
                }
                break;
        }
        return state;
    }

}
