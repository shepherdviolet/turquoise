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

package sviolet.turquoise.uix.viewgesturectrl.output;

import android.content.Context;
import android.graphics.Rect;

import sviolet.turquoise.common.compat.CompatScroller23;
import sviolet.turquoise.uix.viewgesturectrl.ViewGestureClickListener;
import sviolet.turquoise.uix.viewgesturectrl.ViewGestureMoveListener;
import sviolet.turquoise.uix.viewgesturectrl.ViewGestureZoomListener;

/**
 * <p>简易的矩形输出</p>
 *
 * Created by S.Violet on 2016/9/27.
 */

public class SimpleRectangleOutput implements ViewGestureClickListener, ViewGestureMoveListener, ViewGestureZoomListener {

    //setting///////////////////////////////////

    //实际宽高
    private double actualWidth;
    private double actualHeight;

    //显示宽高
    private double displayWidth;
    private double displayHeight;

    //放大倍数上限
    private double magnificationLimit;

    //刷新回调
    private RefreshListener refreshListener;

    //点击长按回调
    private ClickListener clickListener;
    private LongClickListener longClickListener;

    //允许多触点移动
    private boolean multiTouchMoveEnabled = true;

    //variable//////////////////////////////////

    private boolean isHold = false;//是否被持有(有触点)
    private boolean invalidWidthOrHeight = false;//无效的宽高

    //显示区域最大界限
    private double maxLeft;
    private double maxTop;
    private double maxRight;
    private double maxBottom;
    private double maxWidth;
    private double maxHeight;

    //当前显示矩形坐标, 相对于实际矩形左上角的位置
    private double currX;
    private double currY;

    //当前放大率
    private double currMagnification;

    //当前是否是多触点状态
    private boolean isMultiTouch = false;

    //惯性滑动
    private CompatScroller23 flingScroller;

    /*******************************************************************
     * init
     */

    /**
     * 例如做一个图片缩放控件, actualWidth/actualHeight为图片(Bitmap)尺寸,
     * displayWidth/displayHeight为控件尺寸
     *
     * @param context context
     * @param actualWidth 实际宽度, 相当于dstRect的宽度
     * @param actualHeight 实际高度, 相当于dstRect的高度
     * @param displayWidth 显示宽度, 相当于srcRect的宽度
     * @param displayHeight 显示高度, 相当于srcRect的高度
     * @param magnificationLimit 最大放大倍数
     */
    public SimpleRectangleOutput(Context context, double actualWidth, double actualHeight, double displayWidth, double displayHeight, double magnificationLimit) {
        if (context == null){
            throw new RuntimeException("context is null");
        }
        if (magnificationLimit < 1) {
            throw new RuntimeException("magnificationLimit must >= 1");
        }

        this.flingScroller = new CompatScroller23(context);
        this.actualWidth = actualWidth;
        this.actualHeight = actualHeight;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.magnificationLimit = magnificationLimit;

        init();
    }

    private void init() {
        initMaxBounds();
        currX = maxLeft;
        currY = maxTop;
        currMagnification = 1;
    }

    private void initMaxBounds() {
        if (actualWidth <= 0 || actualHeight <= 0 || displayWidth <= 0 || displayHeight <= 0) {
            invalidWidthOrHeight = true;
            return;
        } else {
            invalidWidthOrHeight = false;
        }

        //计算显示界限
        double actualAspectRatio = actualWidth / actualHeight;
        double displayAspectRatio = displayWidth / displayHeight;
        if (actualAspectRatio > displayAspectRatio) {
            double a = (actualWidth / displayAspectRatio - actualHeight) / 2;
            maxLeft = 0;
            maxTop = -a;
            maxRight = actualWidth;
            maxBottom = a + actualHeight;
        } else if (actualAspectRatio < displayAspectRatio) {
            double a = (actualHeight * displayAspectRatio - actualWidth) / 2;
            maxLeft = -a;
            maxTop = 0;
            maxRight = a + actualWidth;
            maxBottom = actualHeight;
        } else {
            maxLeft = 0;
            maxTop = 0;
            maxRight = actualWidth;
            maxBottom = actualHeight;
        }

        maxWidth = maxRight - maxLeft;
        maxHeight = maxBottom - maxTop;
    }

    /**
     * [慎用]重置显示矩形尺寸, 请在UI线程调用, 该方法线程不安全
     *
     * @param displayWidth  显示矩形宽度
     * @param displayHeight 显示矩形高度
     */
    public void resetDisplayDimension(double displayWidth, double displayHeight) {
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        init();
    }

    /**
     * 是否允许多触点移动
     * @param enabled true:允许
     */
    public void setMultiTouchMoveEnabled(boolean enabled){
        this.multiTouchMoveEnabled = enabled;
    }

    /*******************************************************************
     * click
     */

    @Override
    public void onClick(float x, float y) {
        if (invalidWidthOrHeight) {
            return;
        }
        if (clickListener != null){
            double[] actualPoint = mappingDisplayPointToActual(x, y);
            clickListener.onClick((float)actualPoint[0], (float)actualPoint[1], x, y);
        }
    }

    @Override
    public void onLongClick(float x, float y) {
        if (invalidWidthOrHeight) {
            return;
        }
        if (longClickListener != null){
            double[] actualPoint = mappingDisplayPointToActual(x, y);
            longClickListener.onLongClick((float)actualPoint[0], (float)actualPoint[1], x, y);
        }
    }

    /*******************************************************************
     * move
     */

    @Override
    public void holdMove() {
        if (invalidWidthOrHeight) {
            return;
        }

        isHold = true;

        flingScroller.abortAnimation();

        if (refreshListener != null) {
            refreshListener.onRefresh();
        }
    }

    @Override
    public void releaseMove(float velocityX, float velocityY) {
        if (invalidWidthOrHeight) {
            return;
        }

        isHold = false;

        //惯性滑动
        fling(velocityX * (maxWidth / currMagnification) / displayWidth, velocityY * (maxHeight / currMagnification) / displayHeight);

        if (refreshListener != null) {
            refreshListener.onRefresh();
        }
    }

    private void fling(double velocityX, double velocityY) {
        flingScroller.fling((int)currX, (int)currY, (int)-velocityX, (int)-velocityY, 0, (int)(actualWidth - maxWidth / currMagnification), 0, (int)(actualHeight - maxHeight / currMagnification));
    }

    @Override
    public void move(float currentX, float offsetX, float velocityX, float currentY, float offsetY, float velocityY) {
        if (invalidWidthOrHeight) {
            return;
        }

        if (!multiTouchMoveEnabled && isMultiTouch){
            return;
        }

        //offsetX/offsetY为显示矩形坐标系中的数据, 需要变换为实际矩形中的数据
        double actualOffsetX = -offsetX * (maxWidth / currMagnification) / displayWidth;
        double actualOffsetY = -offsetY * (maxHeight / currMagnification) / displayHeight;

        moveBy(actualOffsetX, actualOffsetY);
    }

    /**
     * @param offsetX DisplayRect在X方向的偏移量, 与手势方向相反
     * @param offsetY DisplayRect在Y方向的偏移量, 与手势方向相反
     */
    private void moveBy(double offsetX, double offsetY) {

        double x = currX + offsetX;
        double y = currY + offsetY;

        //越界控制
        if (offsetX < 0) {
            if (x < 0) {
                x = currX;
            }
        } else if (offsetX > 0) {
            if ((x + (maxWidth / currMagnification)) > actualWidth) {
                x = currX;
            }
        }

        if (offsetY < 0) {
            if (y < 0) {
                y = currY;
            }
        } else if (offsetY > 0) {
            if ((y + (maxHeight / currMagnification)) > actualHeight) {
                y = currY;
            }
        }

        //更新坐标
        currX = x;
        currY = y;

    }

    /*******************************************************************
     * zoom
     */

    @Override
    public void holdZoom() {
        if (invalidWidthOrHeight){
            return;
        }
        isMultiTouch = true;
    }

    @Override
    public void releaseZoom() {
        if (invalidWidthOrHeight){
            return;
        }
        isMultiTouch = false;
    }

    @Override
    public void zoom(float basicPointX, float basicPointY, float current, float offset) {
        if (invalidWidthOrHeight) {
            return;
        }
        //过滤偏移量很小的情况
        if (((int) (offset * 100)) == 0) {
            return;
        }

        zoomBy(basicPointX, basicPointY, current, current - offset);
    }

    /**
     * 根据手势变化量缩放
     * @param basicPointX 基点X, 显示坐标系
     * @param basicPointY 基点Y, 显示坐标系
     * @param currDistance 当前的两个触点距离
     * @param lastDistance 之前的两个触点距离
     */
    private void zoomBy(double basicPointX, double basicPointY, double currDistance, double lastDistance) {
        //计算新的放大率
        double magnification = currDistance * currMagnification / lastDistance;
        zoomTo(basicPointX, basicPointY, magnification);
    }

    /**
     * 缩放到指定放大率
     * @param basicPointX 基点X, 显示坐标系
     * @param basicPointY 基点Y, 显示坐标系
     * @param newMagnification 指定放大率
     */
    private void zoomTo(double basicPointX, double basicPointY, double newMagnification){
        //限制放大率
        if (newMagnification < 1) {
            newMagnification = 1;
        } else if (newMagnification > magnificationLimit) {
            newMagnification = magnificationLimit;
        }
        //如果放大率不变, 则跳过后续步骤
        if (newMagnification == currMagnification) {
            return;
        }

        //根据基点位置计算因缩放引起的坐标移动的比率

        double xMoveRate = basicPointX / displayWidth;
        if (xMoveRate < 0) {
            xMoveRate = 0;
        } else if (xMoveRate > 1) {
            xMoveRate = 1;
        }

        double yMoveRate = basicPointY / displayHeight;
        if (yMoveRate < 0) {
            yMoveRate = 0;
        } else if (yMoveRate > 1) {
            yMoveRate = 1;
        }

        //计算因缩放引起的坐标移动

        double offsetX = xMoveRate * (maxWidth / currMagnification - maxWidth / newMagnification);
        double offsetY = yMoveRate * (maxHeight / currMagnification - maxHeight / newMagnification);

        //计算当前坐标

        double x = currX + offsetX;
        double y = currY + offsetY;

        //控制越界

        double actualDisplayWidth = maxWidth / newMagnification;
        if (x < maxLeft) {
            x = maxLeft;
        } else if ((x + actualDisplayWidth) > maxRight) {
            x = maxRight - actualDisplayWidth;
        }

        double actualDisplayHeight = maxHeight / newMagnification;
        if (y < maxTop) {
            y = maxTop;
        } else if ((y + actualDisplayHeight) > maxBottom) {
            y = maxBottom - actualDisplayHeight;
        }

        //更新坐标
        currX = x;
        currY = y;

        //更新当前放大率
        currMagnification = newMagnification;
    }

    /*******************************************************************
     * mapping
     */

    /**
     * 将显示矩形(显示/触摸坐标系)中的触点坐标映射到实际矩形(默认坐标系)上
     */
    private double[] mappingDisplayPointToActual(double x, double y) {
        double[] actual = new double[2];

        if (invalidWidthOrHeight) {
            return actual;
        }

        actual[0] = currX + (x / displayWidth) * (maxWidth / currMagnification);
        actual[1] = currY + (y / displayHeight) * (maxHeight / currMagnification);

        return actual;
    }

    /**
     * 将实际矩阵(默认坐标系)上的点坐标映射到显示矩形(显示/触摸坐标系)中
     */
    private double[] mappingActualPointToDisplay(double x, double y) {
        double[] display = new double[2];

        if (invalidWidthOrHeight) {
            return display;
        }

        display[0] = (x - currX) * displayWidth / (maxWidth / currMagnification);
        display[1] = (y - currY) * displayHeight / (maxHeight / currMagnification);

        return display;
    }

    /*******************************************************************
     * output
     */

    /**
     * 设置刷新监听器, 用于Output告知View需要刷新显示
     */
    public SimpleRectangleOutput setRefreshListener(RefreshListener listener){
        this.refreshListener = listener;
        return this;
    }

    public SimpleRectangleOutput setClickListener(ClickListener listener){
        this.clickListener = listener;
        return this;
    }

    public SimpleRectangleOutput setLongClickListener(LongClickListener listener){
        this.longClickListener = listener;
        return this;
    }

    /**
     * 是否需要继续刷新, 用于View判断是否继续刷新
     */
    public boolean isActive() {
        calculateFlingPosition();
        return isHold || !flingScroller.isFinished();
    }

    /**
     * 主要的数据输出方法, 在传入的两个Rect中赋值
     * @param srcRect 源矩形, 即为实际矩形, 例如:图片(Bitmap)的矩形
     * @param dstRect 目标矩形, 即为显示矩形, 例如:控件矩形
     */
    public void getSrcDstRect(Rect srcRect, Rect dstRect) {
        if (invalidWidthOrHeight) {
            if (srcRect != null) {
                srcRect.left = 0;
                srcRect.top = 0;
                srcRect.right = 0;
                srcRect.bottom = 0;
            }
            if (dstRect != null) {
                dstRect.left = 0;
                dstRect.top = 0;
                dstRect.right = 0;
                dstRect.bottom = 0;
            }
            return;
        }

        calculateFlingPosition();

        //计算显示范围(实际坐标系)
        double left = currX < 0 ? 0 : currX;
        double right = currX + (maxWidth / currMagnification);
        right = right > actualWidth ? actualWidth : right;
        double top = currY < 0 ? 0 : currY;
        double bottom = currY + (maxHeight / currMagnification);
        bottom = bottom > actualHeight ? actualHeight : bottom;

        //原矩形
        if (srcRect != null) {
            srcRect.left = (int) left;
            srcRect.right = (int) right;
            srcRect.top = (int) top;
            srcRect.bottom = (int) bottom;
        }

        //目标矩形
        if (dstRect != null) {
            //将实际矩形的点映射到显示矩形中
            double[] leftTopPoint = mappingActualPointToDisplay(left, top);
            double[] rightBottomPoint = mappingActualPointToDisplay(right, bottom);

            dstRect.left = (int) leftTopPoint[0];
            dstRect.top = (int) leftTopPoint[1];
            dstRect.right = (int) Math.ceil(rightBottomPoint[0]);
            dstRect.bottom = (int) Math.ceil(rightBottomPoint[1]);
        }

    }

    private void calculateFlingPosition(){
        if (!flingScroller.isFinished() && !isHold){
            flingScroller.computeScrollOffset();
            moveBy(flingScroller.getCurrX() - currX, flingScroller.getCurrY() - currY);
        }
    }

    /*****************************************************************************8
     * inner
     */

    /**
     * <p>用于Output通知View刷新(通常实现为postInvalidate())</p>
     */
    public interface RefreshListener {

        void onRefresh();

    }

    public interface ClickListener {

        /**
         * @param actualX 即srcRect坐标系中的点, 即图片坐标系
         * @param actualY 即srcRect坐标系中的点, 即图片坐标系
         * @param displayX 即dstRect坐标系中的点, 即控件坐标系
         * @param displayY 即dstRect坐标系中的点, 即控件坐标系
         */
        void onClick(float actualX, float actualY, float displayX, float displayY);

    }

    public interface LongClickListener {

        /**
         * @param actualX 即srcRect坐标系中的点, 即图片坐标系
         * @param actualY 即srcRect坐标系中的点, 即图片坐标系
         * @param displayX 即dstRect坐标系中的点, 即控件坐标系
         * @param displayY 即dstRect坐标系中的点, 即控件坐标系
         */
        void onLongClick(float actualX, float actualY, float displayX, float displayY);

    }

}
