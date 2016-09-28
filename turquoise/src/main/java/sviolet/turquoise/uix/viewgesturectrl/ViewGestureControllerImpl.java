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

import android.content.Context;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import java.util.ArrayList;
import java.util.List;

import sviolet.turquoise.enhance.common.WeakHandler;
import sviolet.turquoise.util.common.MathUtils;

/**
 * <p>View触摸控制器实现类</p>
 * <p>
 * Created by S.Violet on 2016/9/22.
 */
public class ViewGestureControllerImpl implements ViewGestureController {

    private static final int VELOCITY_UNITS = 1000;//速度采样周期

    //settings/////////////////////////////////////////////////

    private long longClickElapse = 1000;//长按时间

    private List<ViewGestureMoveListener> moveListeners = new ArrayList<>();//移动监听
    private List<ViewGestureZoomListener> zoomListeners = new ArrayList<>();//缩放监听
    private List<ViewGestureRotateListener> rotateListeners = new ArrayList<>();//旋转监听
    private List<ViewGestureClickListener> clickListeners = new ArrayList<>();//点击监听

    //state/////////////////////////////////////////////////

    private ViewGestureTouchPointGroup touchPointGroup;//触点组
    private VelocityTracker mVelocityTracker;//速度捕获器

    private boolean longClicked = false;//长按触发标记

    private MotionState motionState = MotionState.RELEASE;//状态

    private boolean hasSingleTouchHold = false;//单点触摸触发标记
    private boolean hasMultiTouchHold = false;//多点触摸触发标记

    private float currentPointsDistance = -1;//[缩放]当前两点间距, -1表示未定义
    private float currentRotateAngle = -1;//[缩放]当前旋转角度, -1表示未定义

    /**************************************************************************
     * public
     */

    public ViewGestureControllerImpl(Context context) {
        touchPointGroup = new ViewGestureTouchPointGroup(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ViewGestureTouchPoint abandonedPoint = touchPointGroup.update(event);
        handleClickEvent(event, abandonedPoint);
        handleState(event, abandonedPoint);
        handleMove(event);
        handleZoom(event);
        handleRotate(event);
        return true;
    }

    /**************************************************************************
     * settings
     */

    @Override
    public void addOutput(ViewGestureOutput listener) {
        if (listener instanceof ViewGestureMoveListener) {
            addMoveListener((ViewGestureMoveListener) listener);
        }
        if (listener instanceof ViewGestureRotateListener) {
            addRotateListener((ViewGestureRotateListener) listener);
        }
        if (listener instanceof ViewGestureZoomListener) {
            addZoomListener((ViewGestureZoomListener) listener);
        }
        if (listener instanceof ViewGestureClickListener) {
            addClickListener((ViewGestureClickListener) listener);
        }
    }

    public ViewGestureController addMoveListener(ViewGestureMoveListener listener) {
        if (listener != null) {
            moveListeners.add(listener);
        }
        return this;
    }

    public ViewGestureController addRotateListener(ViewGestureRotateListener listener) {
        if (listener != null) {
            rotateListeners.add(listener);
        }
        return this;
    }

    public ViewGestureController addZoomListener(ViewGestureZoomListener listener) {
        if (listener != null) {
            zoomListeners.add(listener);
        }
        return this;
    }

    public ViewGestureController addClickListener(ViewGestureClickListener listener) {
        if (listener != null) {
            clickListeners.add(listener);
        }
        return this;
    }

    /**************************************************************************
     * velocity
     */

    /**
     * 获得速度捕获器
     */
    private VelocityTracker getVelocityTracker(){
        if(mVelocityTracker == null){
            mVelocityTracker = VelocityTracker.obtain();
        }
        return mVelocityTracker;
    }

    /**
     * 重置/回收速度捕获器
     */
    private void resetVelocityTracker(){
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 速度捕获器更新数据
     */
    private void updateVelocity(MotionEvent event){
        getVelocityTracker().addMovement(event);
    }

    /*******************************************************************************
     * handle click
     */

    private void handleClickEvent(MotionEvent event, ViewGestureTouchPoint abandonedPoint) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                hasSingleTouchHold = false;
                hasMultiTouchHold = false;
                resetVelocityTracker();
                startLongClickCounter();
                break;
            case MotionEvent.ACTION_MOVE:

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                cancelLongClickCounter();
                break;
            case MotionEvent.ACTION_POINTER_UP:

                break;
            case MotionEvent.ACTION_UP:
                if (touchPointGroup.getMaxPointNum() == 1 && abandonedPoint != null && !abandonedPoint.isEffectiveMoved && !longClicked) {
                    for (ViewGestureClickListener listener : clickListeners) {
                        listener.onClick(abandonedPoint.downX, abandonedPoint.downY);
                    }
                }
            case MotionEvent.ACTION_CANCEL:
                //以下代码UP/CANCEL都执行
                cancelLongClickCounter();
                break;
            default:
                break;
        }
    }

    private void startLongClickCounter() {
        cancelLongClickCounter();
        myHandler.sendEmptyMessageDelayed(MyHandler.WHAT_LONG_CLICK, longClickElapse);
    }

    private void cancelLongClickCounter() {
        myHandler.removeMessages(MyHandler.WHAT_LONG_CLICK);
        longClicked = false;
    }

    private void onLongClick() {
        ViewGestureTouchPoint point = touchPointGroup.getPoint(0);
        if (touchPointGroup.getMaxPointNum() != 1 || point == null || point.isEffectiveMoved) {
            return;
        }
        longClicked = true;
        for (ViewGestureClickListener listener : clickListeners) {
            listener.onLongClick(point.downX, point.downY);
        }
    }

    /*******************************************************************************
     * handle state
     */

    private void handleState(MotionEvent event, ViewGestureTouchPoint abandonedPoint) {
        switch (motionState) {
            case RELEASE:
                if (touchPointGroup.getPointNum() > 0) {
                    stateToHold();
                }
                break;
            case HOLD:
                if (touchPointGroup.getPointNum() <= 0) {
                    //无触点, 返回release状态
                    stateToRelease(abandonedPoint);
                } else if (touchPointGroup.getPointNum() >= 2) {
                    stateToMultiTouch();
                } else {
                    //单触点, 判断平移距离
                    ViewGestureTouchPoint point = touchPointGroup.getPoint(0);
                    if (point != null && point.isEffectiveMoved) {
                        stateToSingleTouch();
                    }
                }
                break;
            case SINGLE_TOUCH:
                if (touchPointGroup.getPointNum() <= 0) {
                    //无触点, 返回release状态
                    stateToRelease(abandonedPoint);
                } else if (touchPointGroup.getPointNum() >= 2) {
                    stateToMultiTouch();
                }
                break;
            case MULTI_TOUCH:
                if (touchPointGroup.getPointNum() <= 0) {
                    //无触点, 返回release状态
                    stateToRelease(abandonedPoint);
                } else if (touchPointGroup.getPointNum() == 1) {
                    stateToSingleTouch();
                }
                break;
            default:
                break;
        }
    }

    private void stateToRelease(ViewGestureTouchPoint abandonedPoint) {
        singleTouchReleaseCallback(abandonedPoint);
        multiTouchReleaseCallback();
        motionState = MotionState.RELEASE;
        resetVelocityTracker();
    }

    private void stateToHold() {
        motionState = MotionState.HOLD;
    }

    private void stateToSingleTouch() {
        switch (motionState) {
            case HOLD:
                singleTouchHoldCallback();
                break;
            case MULTI_TOUCH:
                multiTouchReleaseCallback();
                break;
        }
        motionState = MotionState.SINGLE_TOUCH;
    }

    private void stateToMultiTouch() {
        switch (motionState) {
            case HOLD:
                singleTouchHoldCallback();
            case SINGLE_TOUCH:
                multiTouchHoldCallback();
                break;
        }
        motionState = MotionState.MULTI_TOUCH;
        //重置触点间距和旋转角度值, 保证offset输出正常
        currentPointsDistance = -1;
        currentRotateAngle = -1;
    }

    private void singleTouchHoldCallback() {
        if (!hasSingleTouchHold) {
            hasSingleTouchHold = true;
            for (ViewGestureMoveListener listener : moveListeners) {
                listener.holdMove();
            }
        }
    }

    private void multiTouchHoldCallback() {
        if (!hasMultiTouchHold) {
            hasMultiTouchHold = true;
            for (ViewGestureZoomListener listener : zoomListeners) {
                listener.holdZoom();
            }
            for (ViewGestureRotateListener listener : rotateListeners) {
                listener.holdRotate();
            }
        }
    }

    private void singleTouchReleaseCallback(ViewGestureTouchPoint abandonedPoint) {
        if (hasSingleTouchHold) {
            //计算速度
            getVelocityTracker().computeCurrentVelocity(VELOCITY_UNITS);
            float velocityX = 0;
            float velocityY = 0;
            if (abandonedPoint != null){
                velocityX = getVelocityTracker().getXVelocity(abandonedPoint.id);
                velocityY = getVelocityTracker().getYVelocity(abandonedPoint.id);
            }
            for (ViewGestureMoveListener listener : moveListeners) {
                listener.releaseMove(velocityX, velocityY);
            }
            hasSingleTouchHold = false;
        }
    }

    private void multiTouchReleaseCallback() {
        if (hasMultiTouchHold) {
            for (ViewGestureZoomListener listener : zoomListeners) {
                listener.releaseZoom();
            }
            for (ViewGestureRotateListener listener : rotateListeners) {
                listener.releaseRotate();
            }
            hasMultiTouchHold = false;
        }
    }

    /*******************************************************************************
     * handle move
     */

    private void handleMove(MotionEvent event){
        handleSingleTouchMove(event);
        handleMultiTouchMove(event);
    }

    private void handleSingleTouchMove(MotionEvent event){
        //只处理SINGLE_TOUCH状态
        if (motionState != MotionState.SINGLE_TOUCH){
            return;
        }
        //更新移动速度
        updateVelocity(event);

        //取第一个点
        ViewGestureTouchPoint point = touchPointGroup.getPoint(0);

        //计算速度
        getVelocityTracker().computeCurrentVelocity(VELOCITY_UNITS);
        float velocityX = getVelocityTracker().getXVelocity(point.id);
        float velocityY = getVelocityTracker().getYVelocity(point.id);

        //输出
        for (ViewGestureMoveListener listener : moveListeners) {
            listener.move(point.currX, point.stepX, velocityX, point.currY, point.stepY, velocityY);
        }
    }

    private void handleMultiTouchMove(MotionEvent event){
        //只处理MULTI_TOUCH状态
        if (motionState != MotionState.MULTI_TOUCH){
            return;
        }
        //更新移动速度
        updateVelocity(event);

        //取前两个点
        ViewGestureTouchPoint point0 = touchPointGroup.getPoint(0);
        ViewGestureTouchPoint point1 = touchPointGroup.getPoint(1);

        float currX = (point0.currX + point1.currX) / 2;
        float currY = (point0.currY + point1.currY) / 2;
        float stepX = (point0.stepX + point1.stepX) / 2;
        float stepY = (point0.stepY + point1.stepY) / 2;

        System.out.println("x:" + currX + " y:" + currY + " sX:" + stepX + " sY:" + stepY);

        //计算速度
        getVelocityTracker().computeCurrentVelocity(VELOCITY_UNITS);
        float velocityX = (getVelocityTracker().getXVelocity(point0.id) + getVelocityTracker().getXVelocity(point1.id)) / 2;
        float velocityY = (getVelocityTracker().getYVelocity(point0.id) + getVelocityTracker().getYVelocity(point1.id)) / 2;

        //输出
        for (ViewGestureMoveListener listener : moveListeners) {
            listener.move(currX, stepX, velocityX, currY, stepY, velocityY);
        }
    }

    /*******************************************************************************
     * handle zoom
     */

    private void handleZoom(MotionEvent event){
        //只处理MULTI_TOUCH状态
        if (motionState != MotionState.MULTI_TOUCH){
            return;
        }

        //取前两个点
        ViewGestureTouchPoint point0 = touchPointGroup.getPoint(0);
        ViewGestureTouchPoint point1 = touchPointGroup.getPoint(1);

        //计算两点之间距离
        float distance = calculatePointDistance(point0, point1);
        //计算偏移量
        float offset = currentPointsDistance < 0 ? 0 : distance - currentPointsDistance;
        currentPointsDistance = distance;

        //计算中点
        float[] midpoint = calculateMidpoint(point0, point1);

        //输出
        for (ViewGestureZoomListener listener : zoomListeners) {
            listener.zoom(midpoint[0], midpoint[1], distance, offset);
        }
    }

    private float calculatePointDistance(ViewGestureTouchPoint pointSrc, ViewGestureTouchPoint pointDst) {
        double _x = Math.abs(pointDst.currX - pointSrc.currX);
        double _y = Math.abs(pointDst.currY - pointSrc.currY);
        return (float) Math.sqrt(_x * _x + _y * _y);
    }

    private float[] calculateMidpoint(ViewGestureTouchPoint pointSrc, ViewGestureTouchPoint pointDst){
        float[] midpoint = new float[2];
        midpoint[0] = (pointSrc.currX + pointDst.currX) / 2;
        midpoint[1] = (pointSrc.currY + pointDst.currY) / 2;
        return midpoint;
    }

    /*******************************************************************************
     * handle rotate
     */

    private void handleRotate(MotionEvent event){
        //只处理MULTI_TOUCH状态
        if (motionState != MotionState.MULTI_TOUCH){
            return;
        }

        //取前两个点
        ViewGestureTouchPoint point0 = touchPointGroup.getPoint(0);
        ViewGestureTouchPoint point1 = touchPointGroup.getPoint(1);

        //计算当前角度
        float angle = calculateRotateAngle(point0, point1);
        //计算偏移量
        float offset = currentRotateAngle < 0 ? 0 : angle - currentRotateAngle;
        currentRotateAngle = angle;

        //解决当角度变化越过0度时的偏移量异常
        if (offset > 270){
            offset -= 360;
        } else if (offset < -270){
            offset += 360;
        }

        //输出
        for (ViewGestureRotateListener listener : rotateListeners) {
            listener.rotate(angle, offset);
        }

    }

    /**
     * 根据两个触点坐标计算旋转角度
     */
    private float calculateRotateAngle(ViewGestureTouchPoint point0, ViewGestureTouchPoint point1){
        //计算两个触点的偏移量, 用于三角函数计算
        float offsetX = point1.currX - point0.currX;
        float offsetY = point1.currY - point0.currY;

        if (offsetX == 0 && offsetY == 0){
            //点没有任何位移, 此处理论上不可能发生, 两个触点不可能没有间距
            return -1;
        }
        if (offsetX == 0){
            if (offsetY > 0){
                return 180;
            }else{
                return 0;
            }
        }
        if (offsetY == 0){
            if (offsetX > 0){
                return 90;
            } else{
                return 270;
            }
        }
        if (offsetX > 0){
            if (offsetY > 0){
                return (float) (180 - MathUtils.atanAngle(Math.abs(offsetX / offsetY)));
            } else {
                return (float) (MathUtils.atanAngle(Math.abs(offsetX / offsetY)));
            }
        } else {
            if (offsetY > 0){
                return (float) (180 + MathUtils.atanAngle(Math.abs(offsetX / offsetY)));
            } else {
                return (float) (360 - MathUtils.atanAngle(Math.abs(offsetX / offsetY)));
            }
        }
    }

    /***************************************************************
     * handler
     */

    private MyHandler myHandler = new MyHandler(this);

    private static class MyHandler extends WeakHandler<ViewGestureControllerImpl> {

        private static final int WHAT_LONG_CLICK = 1;

        public MyHandler(ViewGestureControllerImpl host) {
            super(host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, ViewGestureControllerImpl host) {
            switch (msg.what) {
                case WHAT_LONG_CLICK:
                    host.onLongClick();
                    break;
                default:
                    break;
            }
        }
    }

    /***************************************************************
     * inner class
     */

    private enum MotionState {
        RELEASE,//释放
        HOLD,//持有
        SINGLE_TOUCH,//单点触摸
        MULTI_TOUCH//多点触摸
    }

}
