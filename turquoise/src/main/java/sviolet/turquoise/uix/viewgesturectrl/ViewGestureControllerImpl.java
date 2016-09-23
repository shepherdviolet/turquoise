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
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

import sviolet.turquoise.enhance.common.WeakHandler;

/**
 * <p>View触摸控制器实现类</p>
 * <p>
 * Created by S.Violet on 2016/9/22.
 */
public class ViewGestureControllerImpl implements ViewGestureController {

//    private static final int VERTICAL_ANGLE_TOLERANCE = 30;//判定为垂直角的允许容差
//
    //settings/////////////////////////////////////////////////

    private long longClickElapse = 1000;//长按时间

    private List<ViewGestureMoveListener> moveListeners = new ArrayList<>();//移动监听
    private List<ViewGestureZoomListener> zoomListeners = new ArrayList<>();//缩放监听
    private List<ViewGestureRotateListener> rotateListeners = new ArrayList<>();//旋转监听
    private List<ViewGestureClickListener> clickListeners = new ArrayList<>();//点击监听

    //state/////////////////////////////////////////////////

    private int mTouchSlop;
    private int rotateGesturePointDistanceThreshold;

    private ViewGestureTouchPointGroup touchPointGroup;//触点组

    private boolean longClicked = false;//长按触发标记

    private MotionState motionState = MotionState.RELEASE;//状态

    /**************************************************************************
     * public
     */

    public ViewGestureControllerImpl(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        rotateGesturePointDistanceThreshold = mTouchSlop * 10;
        touchPointGroup = new ViewGestureTouchPointGroup(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ViewGestureTouchPoint abandonedPoint = touchPointGroup.update(event);
        handleClickEvent(event, abandonedPoint);

        return true;
    }

    /**************************************************************************
     * settings
     */

    @Override
    public void addOutput(Object listener) {
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

    /*******************************************************************************
     * handle click
     */

    private void handleClickEvent(MotionEvent event, ViewGestureTouchPoint abandonedPoint) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
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
                    stateToRelease();
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
                    stateToRelease();
                } else if (touchPointGroup.getPointNum() >= 2) {
                    stateToMultiTouch();
                }
                break;
            case MULTI_TOUCH:
                if (touchPointGroup.getPointNum() <= 0) {
                    //无触点, 返回release状态
                    stateToRelease();
                } else if (touchPointGroup.getPointNum() == 1) {
                    stateToSingleTouch();
                }
                break;
            default:
                break;
        }
    }

//    private boolean isRotateGesture(ViewGestureTouchPoint pointSrc, ViewGestureTouchPoint pointDst){
//        //两个触点间距过小, 不判定为旋转手势
//        float pointDistance = calculatePointDistance(pointSrc, pointDst);
//        if (pointDistance < rotateGesturePointDistanceThreshold){
//            return false;
//        }
//        //两个触点之中有一个没有有效位移, 则不判定为旋转手势
//        if (!isPointMoving(pointSrc) || !isPointMoving(pointDst)){
//            return false;
//        }
//        float twoPointVectorAngle = calculatePointMotionVectorAngle(pointDst.currX - pointSrc.currX, pointDst.currY - pointDst.currY);
//        float srcVectorAngle = calculatePointMotionVectorAngle(pointSrc.stepX, pointSrc.stepY);
//        float dstVectorAngle = calculatePointMotionVectorAngle(pointDst.stepX, pointDst.stepY);
//
//        //触点位移矢量角度与触点连线垂直且反向时, 认定为旋转
//        int isSrcMovingVertical = calculateMovingVertical(twoPointVectorAngle, srcVectorAngle);
//        int isDstMovingVertical = calculateMovingVertical(twoPointVectorAngle, dstVectorAngle);
//        if (isSrcMovingVertical > 0 && isDstMovingVertical < 0){
//            return true;
//        }
//        if (isSrcMovingVertical < 0 && isDstMovingVertical > 0){
//            return true;
//        }
//
//        return false;
//    }

//    private float calculatePointDistance(ViewGestureTouchPoint pointSrc, ViewGestureTouchPoint pointDst) {
//        double _x = Math.abs(pointDst.currX - pointSrc.currX);
//        double _y = Math.abs(pointDst.currY - pointSrc.currY);
//        return (float) Math.sqrt(_x * _x + _y * _y);
//    }

//    private boolean isPointMoving(ViewGestureTouchPoint point){
//        if (Math.abs(point.stepX) > mTouchSlop || Math.abs(point.stepY) > mTouchSlop){
//            return true;
//        }
//        return false;
//    }

    /**
     * 计算点运动的矢量角度, 12点钟位置为0, 顺时针增加
     */
//    private float calculatePointMotionVectorAngle(float offsetX, float offsetY){
//        if (offsetX == 0 && offsetY == 0){
//            //点没有任何位移, 此处理论上不可能发生, 因为isRotateGesture方法中先判断了点是否有效位移
//            return 0;
//        }
//        if (offsetX == 0){
//            if (offsetY > 0){
//                return 180;
//            }else{
//                return 0;
//            }
//        }
//        if (offsetY == 0){
//            if (offsetX > 0){
//                return 90;
//            } else{
//                return 270;
//            }
//        }
//        if (offsetX > 0){
//            if (offsetY > 0){
//                return (float) (180 - MathUtils.atanAngle(Math.abs(offsetX / offsetY)));
//            } else {
//                return (float) (MathUtils.atanAngle(Math.abs(offsetX / offsetY)));
//            }
//        } else {
//            if (offsetY > 0){
//                return (float) (180 + MathUtils.atanAngle(Math.abs(offsetX / offsetY)));
//            } else {
//                return (float) (360 - MathUtils.atanAngle(Math.abs(offsetX / offsetY)));
//            }
//        }
//    }

    /**
     * 判断pointVectorAngle是否在lineVectorAngle垂直方向上, 允许一定的容差, 若在垂直方向上, 且角度大于
     * lineVectorAngle则返回1, 若在垂直方向上, 且角度小于lineVectorAngle, 则返回-1, 若不在垂直方向上则返回0
     */
//    private int calculateMovingVertical(float lineVectorAngle, float pointVectorAngle){
//        //逆时针方向的垂直角范围
//        float leftVerticalAngleMin = lineVectorAngle - 90 - VERTICAL_ANGLE_TOLERANCE;
//        float leftVerticalAngleMax = lineVectorAngle - 90 + VERTICAL_ANGLE_TOLERANCE;
//        if (leftVerticalAngleMin < 0 && leftVerticalAngleMax >= 0){
//            if (pointVectorAngle >= 0 && pointVectorAngle <= leftVerticalAngleMax){
//                return -1;//逆时针方向垂直
//            }
//            if (pointVectorAngle >= MathUtils.standardizeAngle(leftVerticalAngleMin) && pointVectorAngle <= 360){
//                return -1;//逆时针方向垂直
//            }
//        } else {
//            //min>=0&&max>=0, min<0&&max<0, 另外还有一种可能min>=0&&max<0, 在容差小于360时不可能出现, 故忽略
//            if (pointVectorAngle >= MathUtils.standardizeAngle(leftVerticalAngleMin) && pointVectorAngle <= MathUtils.standardizeAngle(leftVerticalAngleMax)){
//                return -1;//逆时针方向垂直
//            }
//        }
//
//        //顺时针方向的垂直角范围
//        float rightVerticalAngleMin = lineVectorAngle + 90 - VERTICAL_ANGLE_TOLERANCE;
//        float rightVerticalAngleMax = lineVectorAngle + 90 + VERTICAL_ANGLE_TOLERANCE;
//        if (rightVerticalAngleMin < 360 && rightVerticalAngleMax >= 360){
//            if (pointVectorAngle >= 0 && pointVectorAngle <= rightVerticalAngleMax){
//                return 1;//顺时针方向垂直
//            }
//            if (pointVectorAngle >= MathUtils.standardizeAngle(rightVerticalAngleMin) && pointVectorAngle <= 360){
//                return 1;//顺时针方向垂直
//            }
//        } else {
//            //min>=0&&max>=0, min<0&&max<0, 另外还有一种可能min>=0&&max<0, 在容差小于360时不可能出现, 故忽略
//            if (pointVectorAngle >= MathUtils.standardizeAngle(rightVerticalAngleMin) && pointVectorAngle <= MathUtils.standardizeAngle(rightVerticalAngleMax)){
//                return 1;//顺时针方向垂直
//            }
//        }
//
//        return 0;
//    }

    private void stateToRelease() {
        motionState = MotionState.RELEASE;
    }

    private void stateToHold() {
        motionState = MotionState.HOLD;
    }

    private void stateToSingleTouch() {
        motionState = MotionState.SINGLE_TOUCH;
    }

    private void stateToMultiTouch() {
        motionState = MotionState.MULTI_TOUCH;
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
