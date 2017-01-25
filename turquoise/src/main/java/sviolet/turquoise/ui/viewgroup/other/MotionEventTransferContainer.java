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

package sviolet.turquoise.ui.viewgroup.other;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

import sviolet.turquoise.ui.util.motion.UnidirectionalMoveDetector;
import sviolet.turquoise.util.droid.MotionEventUtils;

/**
 * <p>触摸事件传递容器</p>
 *
 * <p>用于将垂直方向或水平方向的触摸事件, 直接传递给其他控件处理.
 * 例如: 实现ListView + 顶部控件伸缩悬停效果, 给顶部控件套上这个容器, 然后设置
 * motionEventTransferContainer.setYReceiver(listView), 这样即使顶部控件包含
 * ViewPager/HorizontalScrollView等横向滑动的控件, 在顶部控件上垂直滑动, 一样
 * 可以将事件分发到listView做上下滚动, 同时不影响顶部控件的横向滑动.</p>
 *
 * Created by S.Violet on 2016/12/13.
 */

public class MotionEventTransferContainer extends FrameLayout {

    private UnidirectionalMoveDetector unidirectionalMoveDetector;

    private WeakReference<View> xReceiver;
    private WeakReference<View> yReceiver;

    public MotionEventTransferContainer(Context context) {
        super(context);
        init();
    }

    public MotionEventTransferContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MotionEventTransferContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        unidirectionalMoveDetector = new UnidirectionalMoveDetector(getContext());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        int state = unidirectionalMoveDetector.detect(ev);
        View receiver;

        switch (state){
            case UnidirectionalMoveDetector.STATE_TO_X_MOVE:
                receiver = getXReceiver();
                if (receiver == null){
                    break;
                }
                intercept(ev, receiver);
                return true;
            case UnidirectionalMoveDetector.STATE_X_MOVEING:
                receiver = getXReceiver();
                if (receiver == null){
                    break;
                }
                pass(ev, receiver);
                return true;
            case UnidirectionalMoveDetector.STATE_TO_Y_MOVE:
                receiver = getYReceiver();
                if (receiver == null){
                    break;
                }
                intercept(ev, receiver);
                return true;
            case UnidirectionalMoveDetector.STATE_Y_MOVEING:
                receiver = getYReceiver();
                if (receiver == null){
                    break;
                }
                pass(ev, receiver);
                return true;
        }

        return super.dispatchTouchEvent(ev);
    }

    private void intercept(MotionEvent ev, View receiver) {
        //取消子控件的事件
        MotionEventUtils.emulateCancelEvent(ev, emulateMotionEventExecutor);
        //模拟接收器down事件
        MotionEventUtils.emulateDownEvent(ev, true, receiver);
        //分发时间给接收器
        pass(ev, receiver);
    }

    private void pass(MotionEvent ev, View receiver) {
        //把事件的坐标修正为接收器坐标系
        MotionEventUtils.offsetLocation(ev, receiver);
        //手动把事件交给接收器处理
        receiver.dispatchTouchEvent(ev);
        //恢复事件的坐标为本控件坐标系
        MotionEventUtils.offsetLocation(ev, this);
    }

    public void setXReceiver(View view){
        this.xReceiver = new WeakReference<>(view);
    }

    public void setYReceiver(View view){
        this.yReceiver = new WeakReference<>(view);
    }

    private View getXReceiver(){
        if (xReceiver != null){
            return xReceiver.get();
        }
        return null;
    }

    private View getYReceiver(){
        if (yReceiver != null){
            return yReceiver.get();
        }
        return null;
    }

    private MotionEventUtils.EmulateMotionEventExecutor emulateMotionEventExecutor = new MotionEventUtils.EmulateMotionEventExecutor() {
        @Override
        public void dispatchTouchEvent(MotionEvent emulateMotionEvent) {
            //模拟出来的事件是屏幕坐标系的, 需要根据当前ViewGroup修正坐标
            MotionEventUtils.offsetLocation(emulateMotionEvent, MotionEventTransferContainer.this);
            //分发事件
            MotionEventTransferContainer.super.dispatchTouchEvent(emulateMotionEvent);
        }
    };

}
