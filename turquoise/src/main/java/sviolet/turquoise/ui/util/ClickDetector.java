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

package sviolet.turquoise.ui.util;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

/**
 * <p>MotionEvent用, 点击事件侦测器</p>
 *
 * <pre>{@code
 *      view.setOnTouchListener(new View.OnTouchListener() {
 *
 *          private ClickDetector clickDetector = new ClickDetector(getApplicationContext());
 *
 *          public boolean onTouch(View v, MotionEvent event) {
 *              if (clickDetector.detect(event)){
 *                  //此处触发点击事件
 *              }
 *              return true;
 *          }
 *      });
 * }</pre>
 *
 * Created by S.Violet on 2016/11/24.
 */

public class ClickDetector {

    private float mTouchSlop;
    private float downX, downY;
    private boolean moved = false;

    public ClickDetector(Context context) {
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * 持续收集MotionEvent, 并在ACTION_UP事件时判断是否发生位移, 若没有发生位移, 判断为点击事件时, 返回true
     */
    public boolean detect(MotionEvent event){
        switch (event.getActionMasked()){
            case ACTION_DOWN:
                //按下事件记录按下的坐标
                moved = false;
                downX = event.getX();
                downY = event.getY();
                break;
            case ACTION_MOVE:
                if (moved){
                    break;
                }
                //发生有效位移时, 取消点击事件
                if (Math.abs(event.getX() - downX) > mTouchSlop || Math.abs(event.getY() - downY) > mTouchSlop){
                    moved = true;
                }
                break;
            case ACTION_UP:
                //没有发生有效位移时
                if (!moved) {
                    //此次事件为点击事件
                    return true;
                }
                break;
        }
        return false;
    }

}
