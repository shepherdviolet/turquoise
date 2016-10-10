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
import android.graphics.RectF;
import android.view.ViewConfiguration;

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

    public static final double AUTO_MAGNIFICATION_LIMIT = -1;

    private static final float DEFAULT_FLING_FRICTION = 0.007f;

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

    //初始显示状态
    private InitScaleType initScaleType;

    //归位滑动时间
    private int scrollDuration = 250;

    //平移越界开关
    private boolean overMoveEnabled = true;
    //平移越界阻尼
    private double overMoveResistance = 5;

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

    //惯性滑动, 因为有可能需要一个方向scroll到边界, 一个惯性fling, 因此分为两个
    private CompatScroller23 flingScrollerX;
    private CompatScroller23 flingScrollerY;

    //临时参数, 优化性能
    private Point actualTouchPoint = new Point();
    private Point leftTopDisplayPoint = new Point();
    private Point rightBottomDisplayPoint = new Point();
    private RectF tempSrcRectF = new RectF();
    private RectF tempDstRectF = new RectF();

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
     * @param magnificationLimit 最大放大倍数, 可设置自适应放大倍数:{@link SimpleRectangleOutput#AUTO_MAGNIFICATION_LIMIT}
     */
    public SimpleRectangleOutput(Context context, double actualWidth, double actualHeight, double displayWidth, double displayHeight, double magnificationLimit, InitScaleType initScaleType) {
        if (context == null){
            throw new RuntimeException("context is null");
        }

        this.flingScrollerX = new CompatScroller23(context);
        this.flingScrollerY = new CompatScroller23(context);

        this.flingScrollerX.setFriction(DEFAULT_FLING_FRICTION);
        this.flingScrollerY.setFriction(DEFAULT_FLING_FRICTION);

        reset(actualWidth, actualHeight, displayWidth, displayHeight, magnificationLimit, initScaleType);
    }

    /**
     * 实际矩形和显示矩形长宽均为0, 放大倍数限制为1, 的默认输出实例, 需要后续调用
     * {@link SimpleRectangleOutput#reset(double, double, double, double, double, InitScaleType)}方法方可正常输出.
     * @param context context
     */
    public SimpleRectangleOutput(Context context){
        this(context, 0, 0, 0, 0, 1, InitScaleType.FIT_CENTER);
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
        if (initScaleType.getScaleFactor() == ScaleFactor.FIT || actualWidth >= displayWidth || actualHeight >= displayHeight) {
            //FIT模式 或 实际尺寸大于显示尺寸时
            double actualAspectRatio = actualWidth / actualHeight;
            double displayAspectRatio = displayWidth / displayHeight;
            if (actualAspectRatio > displayAspectRatio) {
                double a = (actualWidth / displayAspectRatio - actualHeight) / 2;
                maxLeft = 0;
                maxTop = -a + initScaleType.getVerticalFactor() * a;
                maxRight = actualWidth;
                maxBottom = a + initScaleType.getVerticalFactor() * a + actualHeight;
                if (magnificationLimit == AUTO_MAGNIFICATION_LIMIT) {
                    magnificationLimit = actualWidth / displayWidth;
                    if (magnificationLimit < 1){
                        magnificationLimit = 1;
                    }
                }
            } else if (actualAspectRatio < displayAspectRatio) {
                double a = (actualHeight * displayAspectRatio - actualWidth) / 2;
                maxLeft = -a + initScaleType.getHorizontalFactor() * a;
                maxTop = 0;
                maxRight = a + initScaleType.getHorizontalFactor() * a + actualWidth;
                maxBottom = actualHeight;
                if (magnificationLimit == AUTO_MAGNIFICATION_LIMIT) {
                    magnificationLimit = actualHeight / displayHeight;
                    if (magnificationLimit < 1){
                        magnificationLimit = 1;
                    }
                }
            } else {
                maxLeft = 0;
                maxTop = 0;
                maxRight = actualWidth;
                maxBottom = actualHeight;
                if (magnificationLimit == AUTO_MAGNIFICATION_LIMIT) {
                    magnificationLimit = actualWidth / displayWidth;
                    if (magnificationLimit < 1){
                        magnificationLimit = 1;
                    }
                }
            }
        } else {
            //NORMAL模式 且 实际尺寸小于显示尺寸时
            double xOffset = (displayWidth - actualWidth) / 2;
            double yOffset = (displayHeight - actualHeight) / 2;
            maxLeft = -xOffset + initScaleType.getHorizontalFactor() * xOffset;
            maxTop = -yOffset + initScaleType.getVerticalFactor() * yOffset;
            maxRight = xOffset + initScaleType.getHorizontalFactor() * xOffset + actualWidth;
            maxBottom = yOffset + initScaleType.getVerticalFactor() * yOffset + actualHeight;
            if (magnificationLimit == AUTO_MAGNIFICATION_LIMIT) {
                magnificationLimit = 1;
            }
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
     * [慎用]重置实际矩形尺寸, 请在UI线程调用, 该方法线程不安全
     *
     * @param actualWidth 实际矩形宽度
     * @param actualHeight 实际矩形高度
     */
    public void resetActualDimension(double actualWidth, double actualHeight) {
        this.actualWidth = actualWidth;
        this.actualHeight = actualHeight;
        init();
    }

    /**
     * [慎用]重置最大放大倍数, 请在UI线程调用, 该方法线程不安全
     *
     * @param magnificationLimit 最大放大倍数, 可设置自适应放大倍数:{@link SimpleRectangleOutput#AUTO_MAGNIFICATION_LIMIT}
     */
    public void resetMagnificationLimit(double magnificationLimit){
        if (magnificationLimit < 1 && magnificationLimit != AUTO_MAGNIFICATION_LIMIT) {
            throw new RuntimeException("magnificationLimit must >= 1 or SimpleRectangleOutput.AUTO_MAGNIFICATION_LIMIT");
        }

        this.magnificationLimit = magnificationLimit;

        init();
    }

    /**
     * [慎用]重置初始显示方式, 请在UI线程调用, 该方法线程不安全
     *
     * @param initScaleType 初始显示方式
     */
    public void resetInitScaleType(InitScaleType initScaleType){
        if (initScaleType == null){
            throw new RuntimeException("initScaleType is null");
        }

        this.initScaleType = initScaleType;

        init();
    }

    /**
     * [慎用]重置参数, 请在UI线程调用, 该方法线程不安全
     *
     * @param actualWidth 实际宽度, 相当于dstRect的宽度
     * @param actualHeight 实际高度, 相当于dstRect的高度
     * @param displayWidth 显示宽度, 相当于srcRect的宽度
     * @param displayHeight 显示高度, 相当于srcRect的高度
     * @param magnificationLimit 最大放大倍数, 可设置自适应放大倍数:{@link SimpleRectangleOutput#AUTO_MAGNIFICATION_LIMIT}
     * @param initScaleType 初始显示方式
     */
    public void reset(double actualWidth, double actualHeight, double displayWidth, double displayHeight, double magnificationLimit, InitScaleType initScaleType) {
        if (magnificationLimit < 1 && magnificationLimit != AUTO_MAGNIFICATION_LIMIT) {
            throw new RuntimeException("magnificationLimit must >= 1 or SimpleRectangleOutput.AUTO_MAGNIFICATION_LIMIT");
        }
        if (initScaleType == null){
            throw new RuntimeException("initScaleType is null");
        }

        this.actualWidth = actualWidth;
        this.actualHeight = actualHeight;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.magnificationLimit = magnificationLimit;
        this.initScaleType = initScaleType;

        init();
    }

    /**
     * 是否允许多触点移动
     * @param enabled true:允许
     */
    public void setMultiTouchMoveEnabled(boolean enabled){
        this.multiTouchMoveEnabled = enabled;
    }

    /**
     * 设置归位滚动时间
     */
    public void setScrollDuration(int scrollDuration){
        if (scrollDuration < 0){
            throw new RuntimeException("scroll duration must >= 0");
        }
        this.scrollDuration = scrollDuration;
    }

    /**
     * 惯性滑动的减速率, 值越小滑动距离越长, 默认值为{@value SimpleRectangleOutput#DEFAULT_FLING_FRICTION}
     * ({@link SimpleRectangleOutput#DEFAULT_FLING_FRICTION}),
     * 系统推荐值为{@link ViewConfiguration#getScrollFriction}.
     *
     * @param flingFriction > 0
     */
    public void setFlingFriction(int flingFriction){
        if (flingFriction <= 0){
            throw new RuntimeException("flingFriction must > 0");
        }
        this.flingScrollerX.setFriction(flingFriction);
        this.flingScrollerY.setFriction(flingFriction);
    }

    /**
     * @param overMoveEnabled true:允许越界移动
     */
    public void setOverMoveEnabled(boolean overMoveEnabled){
        this.overMoveEnabled = overMoveEnabled;
    }

    /**
     * 设置越界移动阻尼
     * @param overMoveResistance >=1, 值越大移动越慢
     */
    public void setOverMoveResistance(double overMoveResistance){
        if (overMoveResistance < 1){
            throw new RuntimeException("overMoveResistance must >= 1");
        }
        this.overMoveResistance= overMoveResistance;
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
            mappingDisplayPointToActual(x, y, actualTouchPoint);
            clickListener.onClick((float)actualTouchPoint.getX(), (float)actualTouchPoint.getY(), x, y);
        }
    }

    @Override
    public void onLongClick(float x, float y) {
        if (invalidWidthOrHeight) {
            return;
        }
        if (longClickListener != null){
            mappingDisplayPointToActual(x, y, actualTouchPoint);
            longClickListener.onLongClick((float)actualTouchPoint.getX(), (float)actualTouchPoint.getY(), x, y);
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

        flingScrollerX.abortAnimation();
        flingScrollerY.abortAnimation();

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

        //显示矩形在实际矩形中的宽高
        double width = maxWidth / currMagnification;
        double height = maxHeight / currMagnification;

        //显示矩形在实际矩形中的位置
        double left = currX;
        double right = left + width;
        double top = currY;
        double bottom = top + height;

        //是否需要越界弹回
        boolean xScrollToDst = false;
        boolean yScrollToDst = false;
        //越界弹回目标
        double dstX = currX;
        double dstY = currY;

        if (width > actualWidth) {
            double a = (width - actualWidth) / 2;
            dstX = -a + initScaleType.getHorizontalFactor() * a;
            xScrollToDst = true;
        } else if (left < 0) {
            dstX = 0;
            xScrollToDst = true;
        } else if (right > actualWidth){
            dstX = currX - right + actualWidth;
            xScrollToDst = true;
        }

        if (height > actualHeight) {
            double a = (height - actualHeight) / 2;
            dstY = -a + initScaleType.getVerticalFactor() * a;
            yScrollToDst = true;
        } else if (top < 0) {
            dstY = 0;
            yScrollToDst = true;
        } else if (bottom > actualHeight){
            dstY = currY - bottom + actualHeight;
            yScrollToDst = true;
        }

        if (xScrollToDst){
            double dxDouble = dstX - currX;
            int dx;
            //消除回弹时和边界的缝隙
            if (dxDouble > -1 && dxDouble < 1){
                dx = 0;
            }else if (dstX > currX){
                dx = (int) Math.ceil(dxDouble);
            } else {
                dx = (int) Math.floor(dxDouble);
            }
            flingScrollerX.startScroll((int)currX, 0, dx, 0, scrollDuration);
        } else {
            flingScrollerX.fling((int) currX, 0, (int) -velocityX, 0, 0, (int) (actualWidth - width), 0, 0);
        }

        if (yScrollToDst){
            double dyDouble = dstY - currY;
            int dy;
            //消除回弹时和边界的缝隙
            if (dyDouble > -1 && dyDouble < 1) {
                dy = 0;
            }else if (dstY > currY){
                dy = (int) Math.ceil(dyDouble);
            } else {
                dy = (int) Math.floor(dyDouble);
            }
            flingScrollerY.startScroll(0, (int) currY, 0, dy, scrollDuration);
        } else {
            flingScrollerY.fling(0, (int) currY, 0, (int) -velocityY, 0, 0, 0, (int) (actualHeight - height));
        }

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

    private void moveTo(double x, double y){
        moveBy(x - currX, y - currY);
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
                if (overMoveEnabled){
                    x = currX + offsetX / overMoveResistance;
                }else {
                    x = currX;
                }
            }
        } else if (offsetX > 0) {
            if ((x + (maxWidth / currMagnification)) > actualWidth) {
                if (overMoveEnabled){
                    x = currX + offsetX / overMoveResistance;
                }else {
                    x = currX;
                }
            }
        }

        if (offsetY < 0) {
            if (y < 0) {
                if (overMoveEnabled){
                    y = currY + offsetY / overMoveResistance;
                }else {
                    y = currY;
                }
            }
        } else if (offsetY > 0) {
            if ((y + (maxHeight / currMagnification)) > actualHeight) {
                if (overMoveEnabled){
                    y = currY + offsetY / overMoveResistance;
                }else {
                    y = currY;
                }
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
     * @param displayX 显示矩形(显示/触摸坐标系)中的坐标
     * @param displayY 显示矩形(显示/触摸坐标系)中的坐标
     * @param actualPoint 输出参数, 实际矩形(默认坐标系)上的点
     */
    public void mappingDisplayPointToActual(double displayX, double displayY, Point actualPoint) {

        if (invalidWidthOrHeight) {
            return;
        }

        actualPoint.setX(currX + (displayX / displayWidth) * (maxWidth / currMagnification));
        actualPoint.setY(currY + (displayY / displayHeight) * (maxHeight / currMagnification));

    }

    /**
     * 将实际矩阵(默认坐标系)上的点坐标映射到显示矩形(显示/触摸坐标系)中
     * @param actualX 实际矩阵(默认坐标系)上的点坐标
     * @param actualY 实际矩阵(默认坐标系)上的点坐标
     * @param displayPoint 输出参数, 显示矩形(显示/触摸坐标系)中的点
     */
    public void mappingActualPointToDisplay(double actualX, double actualY, Point displayPoint) {

        if (invalidWidthOrHeight) {
            return;
        }

        displayPoint.setX((actualX - currX) * displayWidth / (maxWidth / currMagnification));
        displayPoint.setY((actualY - currY) * displayHeight / (maxHeight / currMagnification));

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
        return isHold || !flingScrollerX.isFinished() || !flingScrollerY.isFinished();
    }

    /**
     * [必须在UI线程调用]主要的数据输出方法, 在传入的两个Rect中赋值
     * @param srcRect 源矩形, 即为实际矩形, 例如:图片(Bitmap)的矩形
     * @param dstRect 目标矩形, 即为显示矩形, 例如:控件矩形
     */
    public void getSrcDstRect(Rect srcRect, Rect dstRect){
        getSrcDstRectF(tempSrcRectF, tempDstRectF);
        srcRect.left = (int) tempSrcRectF.left;
        srcRect.top = (int) tempSrcRectF.top;
        srcRect.right = (int) tempSrcRectF.right;
        srcRect.bottom = (int) tempSrcRectF.bottom;
        dstRect.left = (int) tempDstRectF.left;
        dstRect.top = (int) tempDstRectF.top;
        dstRect.right = (int) Math.ceil(tempDstRectF.right);
        dstRect.bottom = (int) Math.ceil(tempDstRectF.bottom);
    }

    /**
     * [必须在UI线程调用]主要的数据输出方法, 在传入的两个Rect中赋值
     * @param srcRect 源矩形, 即为实际矩形, 例如:图片(Bitmap)的矩形
     * @param dstRect 目标矩形, 即为显示矩形, 例如:控件矩形
     */
    public void getSrcDstRect(Rect srcRect, RectF dstRect){
        getSrcDstRectF(tempSrcRectF, dstRect);
        srcRect.left = (int) tempSrcRectF.left;
        srcRect.top = (int) tempSrcRectF.top;
        srcRect.right = (int) tempSrcRectF.right;
        srcRect.bottom = (int) tempSrcRectF.bottom;
    }

    /**
     * 主要的数据输出方法, 在传入的两个RectF中赋值
     * @param srcRect 源矩形, 即为实际矩形, 例如:图片(Bitmap)的矩形
     * @param dstRect 目标矩形, 即为显示矩形, 例如:控件矩形
     */
    public void getSrcDstRectF(RectF srcRect, RectF dstRect) {
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
            srcRect.left = (float) left;
            srcRect.right = (float) right;
            srcRect.top = (float) top;
            srcRect.bottom = (float) bottom;
        }

        //目标矩形
        if (dstRect != null) {
            //将实际矩形的点映射到显示矩形中
            mappingActualPointToDisplay(left, top, leftTopDisplayPoint);
            mappingActualPointToDisplay(right, bottom, rightBottomDisplayPoint);

            dstRect.left = (float) leftTopDisplayPoint.getX();
            dstRect.top = (float) leftTopDisplayPoint.getY();
            dstRect.right = (float) rightBottomDisplayPoint.getX();
            dstRect.bottom = (float) rightBottomDisplayPoint.getY();
        }

    }

    private void calculateFlingPosition(){
        if (isHold){
            return;
        }
        double offsetX = 0;
        double offsetY = 0;
        if (!flingScrollerX.isFinished()){
            flingScrollerX.computeScrollOffset();
            offsetX = flingScrollerX.getCurrX() - currX;
        }
        if (!flingScrollerY.isFinished()){
            flingScrollerY.computeScrollOffset();
            offsetY = flingScrollerY.getCurrY() - currY;
        }
        moveBy(offsetX, offsetY);
    }

    /*****************************************************************************
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

    public static class Point {
        private double x;
        private double y;

        public Point() {
        }

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }
    }

    public enum ScaleFactor{
        NORMAL,
        FIT
    }

    /**
     * 初始显示方式, 即未放大时的显示方式
     */
    public enum InitScaleType{
        CENTER(ScaleFactor.NORMAL, 0, 0),//若实际矩形小于显示矩形, 居中显示, 四边留空
        LEFT(ScaleFactor.NORMAL, 1, 0),//若实际矩形小于显示矩形, 靠左显示, 其他三边留空
        TOP(ScaleFactor.NORMAL, 0, 1),//若实际矩形小于显示矩形, 靠上显示, 其他三边留空
        RIGHT(ScaleFactor.NORMAL, -1, 0),//若实际矩形小于显示矩形, 靠右显示, 其他三边留空
        BOTTOM(ScaleFactor.NORMAL, 0, -1),//若实际矩形小于显示矩形, 靠下显示, 其他三边留空
        LEFT_TOP(ScaleFactor.NORMAL, 1, 1),//若实际矩形小于显示矩形, 靠左上显示, 其他两边留空
        RIGHT_TOP(ScaleFactor.NORMAL, -1, 1),//若实际矩形小于显示矩形, 靠右上显示, 其他两边留空
        LEFT_BOTTOM(ScaleFactor.NORMAL, 1, -1),//若实际矩形小于显示矩形, 靠左下显示, 其他两边留空
        RIGHT_BOTTOM(ScaleFactor.NORMAL, -1, -1),//若实际矩形小于显示矩形, 靠右下显示, 其他两边留空
        FIT_CENTER(ScaleFactor.FIT, 0, 0),//若实际矩形小于显示矩形, 等比例拉伸, 使得长或宽之一填充满显示矩形, 其他两边留空, 居中显示
        FIT_LEFT(ScaleFactor.FIT, 1, 0),//若实际矩形小于显示矩形, 等比例拉伸, 使得长或宽之一填充满显示矩形, 靠左显示, 垂直方向居中
        FIT_TOP(ScaleFactor.FIT, 0, 1),//若实际矩形小于显示矩形, 等比例拉伸, 使得长或宽之一填充满显示矩形, 靠上显示, 水平方向居中
        FIT_RIGHT(ScaleFactor.FIT, -1, 0),//若实际矩形小于显示矩形, 等比例拉伸, 使得长或宽之一填充满显示矩形, 靠右显示, 垂直方向居中
        FIT_BOTTOM(ScaleFactor.FIT, 0, -1),//若实际矩形小于显示矩形, 等比例拉伸, 使得长或宽之一填充满显示矩形, 靠下显示, 水平方向居中
        FIT_LEFT_TOP(ScaleFactor.FIT, 1, 1),//若实际矩形小于显示矩形, 等比例拉伸, 使得长或宽之一填充满显示矩形, 靠左上显示
        FIT_RIGHT_TOP(ScaleFactor.FIT, -1, 1),//若实际矩形小于显示矩形, 等比例拉伸, 使得长或宽之一填充满显示矩形, 靠右上显示
        FIT_LEFT_BOTTOM(ScaleFactor.FIT, 1, -1),//若实际矩形小于显示矩形, 等比例拉伸, 使得长或宽之一填充满显示矩形, 靠左下显示
        FIT_RIGHT_BOTTOM(ScaleFactor.FIT, -1, -1);//若实际矩形小于显示矩形, 等比例拉伸, 使得长或宽之一填充满显示矩形, 靠右下显示

        private ScaleFactor scaleFactor;
        private int horizontalFactor;
        private int verticalFactor;

        /**
         * @param horizontalFactor +1靠左 0居中 -1靠右
         * @param verticalFactor +1靠上 0居中 -1靠下
         */
        InitScaleType(ScaleFactor scaleFactor, int horizontalFactor, int verticalFactor){
            this.scaleFactor = scaleFactor;
            this.horizontalFactor = horizontalFactor;
            this.verticalFactor = verticalFactor;
        }

        public ScaleFactor getScaleFactor() {
            return scaleFactor;
        }

        public int getHorizontalFactor() {
            return horizontalFactor;
        }

        public int getVerticalFactor() {
            return verticalFactor;
        }
    }

}
