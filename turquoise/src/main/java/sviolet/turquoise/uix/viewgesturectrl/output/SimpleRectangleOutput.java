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

import android.graphics.Rect;

import sviolet.turquoise.uix.viewgesturectrl.ViewGestureClickListener;
import sviolet.turquoise.uix.viewgesturectrl.ViewGestureMoveListener;
import sviolet.turquoise.uix.viewgesturectrl.ViewGestureZoomListener;

/**
 * <p>简易的矩形输出</p>
 *
 * Created by S.Violet on 2016/9/27.
 */

public class SimpleRectangleOutput implements ViewGestureClickListener, ViewGestureMoveListener, ViewGestureZoomListener {

    private static final Rect INVALID_RECT = new Rect(0, 0, 0, 0);

    //setting///////////////////////////////////

    //实际宽高
    private int actualWidth;
    private int actualHeight;

    //显示宽高
    private float displayWidth;
    private float displayHeight;

    //放大倍数上限
    private float magnificationLimit;

    //variable//////////////////////////////////

    private boolean invalidWidthOrHeight = false;

    //显示区域最大界限
    private float maxLeft;
    private float maxTop;
    private float maxRight;
    private float maxBottom;
    private float maxWidth;
    private float maxHeight;

    //当前显示矩形坐标, 相对于实际矩形左上角的位置
    private float currX;
    private float currY;

    //当前放大率
    private float currMagnification;

    /*******************************************************************
     * init
     */

    public SimpleRectangleOutput(int actualWidth, int actualHeight, float displayWidth, float displayHeight, float magnificationLimit){
        if (magnificationLimit < 1){
            throw new RuntimeException("magnificationLimit must >= 1");
        }

        this.actualWidth = actualWidth;
        this.actualHeight = actualHeight;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.magnificationLimit = magnificationLimit;

        init();
    }

    private void init(){
        initMaxBounds();
        currX = maxLeft;
        currY = maxTop;
        currMagnification = 1;
    }

    private void initMaxBounds() {
        if (actualWidth <= 0 || actualHeight <= 0 || displayWidth <= 0 || displayHeight <= 0){
            invalidWidthOrHeight = true;
            return;
        }else{
            invalidWidthOrHeight = false;
        }

        //计算显示界限
        float actualAspectRatio = actualWidth / actualHeight;
        float displayAspectRatio = displayWidth / displayHeight;
        if (actualAspectRatio > displayAspectRatio){
            float a = (actualWidth / displayAspectRatio - actualHeight) / 2;
            maxLeft = 0;
            maxTop = -a;
            maxRight = actualWidth;
            maxBottom = a + actualHeight;
        } else if (actualAspectRatio < displayAspectRatio){
            float a = (actualHeight * displayAspectRatio - actualWidth) / 2;
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
     * @param displayWidth 显示矩形宽度
     * @param displayHeight 显示矩形高度
     */
    public void resetDisplayDimension(float displayWidth, float displayHeight){
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        init();
    }

    /*******************************************************************
     * click
     */

    @Override
    public void onClick(float x, float y) {
        if (invalidWidthOrHeight){
            return;
        }
    }

    @Override
    public void onLongClick(float x, float y) {
        if (invalidWidthOrHeight){
            return;
        }
    }

    /*******************************************************************
     * move
     */

    @Override
    public void holdMove() {
        if (invalidWidthOrHeight){
            return;
        }
    }

    @Override
    public void releaseMove(float velocityX, float velocityY) {
        if (invalidWidthOrHeight){
            return;
        }
    }

    @Override
    public void move(float currentX, float offsetX, float velocityX, float currentY, float offsetY, float velocityY) {
        if (invalidWidthOrHeight){
            return;
        }

        float actualOffsetX = -offsetX * (maxWidth / currMagnification) / displayWidth;
        float actualOffsetY = -offsetY * (maxHeight / currMagnification) / displayHeight;

        moveBy(actualOffsetX, actualOffsetY);
    }

    /**
     * @param offsetX DisplayRect在X方向的偏移量, 与手势方向相反
     * @param offsetY DisplayRect在Y方向的偏移量, 与手势方向相反
     */
    private void moveBy(float offsetX, float offsetY){

        float x = currX + offsetX;
        float y = currY + offsetY;

        if (offsetX < 0){
            if (x < 0){
                x = currX;
            }
        } else if (offsetX > 0){
            if ((x + (maxWidth / currMagnification)) > actualWidth){
                x = currX;
            }
        }

        if (offsetY < 0){
            if (y < 0){
                y = currY;
            }
        } else if (offsetY > 0){
            if ((y + (maxHeight / currMagnification)) > actualHeight){
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

    }

    @Override
    public void releaseZoom() {
        if (invalidWidthOrHeight){
            return;
        }

    }

    @Override
    public void zoom(float basicPointX, float basicPointY, float current, float offset) {
        if (invalidWidthOrHeight){
            return;
        }
        //过滤偏移量很小的情况
        if (((int)(offset * 100)) == 0){
            return;
        }

        float[] actualBasicPoint = mappingDisplayPointToActual(basicPointX, basicPointY);
        float zoomRateOffset = offset / (current - offset);

        zoomBy(actualBasicPoint[0], actualBasicPoint[1], zoomRateOffset);
    }

    private void zoomBy(float basicPointX, float basicPointY, float zoomRateOffset){
        //计算新的放大率
        float magnification = currMagnification + zoomRateOffset * currMagnification;
        //限制放大率
        if (magnification < 1){
            magnification = 1;
        } else if (magnification > magnificationLimit){
            magnification = magnificationLimit;
        }
        //如果放大率不变, 则跳过后续步骤
        if (magnification == currMagnification){
            return;
        }

        //计算因缩放引起的坐标移动

        float xMoveRate = basicPointX / displayWidth;
        if (xMoveRate < 0){
            xMoveRate = 0;
        } else if (xMoveRate > 1){
            xMoveRate = 1;
        }

        float yMoveRate = basicPointY / displayHeight;
        if (yMoveRate < 0){
            yMoveRate = 0;
        } else if (yMoveRate > 1){
            yMoveRate = 1;
        }

        float offsetX = xMoveRate * maxWidth * (magnification - currMagnification);
        float offsetY = yMoveRate * maxHeight * (magnification - currMagnification);

        float x = currX + offsetX;
        float y = currY + offsetY;

        float actualDisplayWidth = maxWidth / magnification;
        if (actualDisplayWidth > actualWidth){
            x = -(actualDisplayWidth - actualWidth) / 2;
        } else {
            if (x < 0){
                x = 0;
            } else if ((x + actualDisplayWidth) > actualWidth){
                x = actualWidth - actualDisplayWidth;
            }
        }

        float actualDisplayHeight = maxHeight / magnification;
        if (actualDisplayHeight > actualHeight){
            y = -(actualDisplayHeight - actualHeight) / 2;
        } else if (offsetY > 0){
            if (y < 0){
                y = 0;
            } else if ((y + actualDisplayHeight) > actualHeight){
                y = actualHeight - actualDisplayHeight;
            }
        }

        //更新坐标
        currX = x;
        currY = y;
        //更新当前放大率
        currMagnification = magnification;

    }

    /*******************************************************************
     * mapping
     */

    /**
     * 将显示矩形(显示/触摸坐标系)中的触点坐标映射到实际矩形(默认坐标系)上
     */
    private float[] mappingDisplayPointToActual(float x, float y){
        float[] actual = new float[2];

        if (invalidWidthOrHeight){
            return actual;
        }

        actual[0] = currX + (x / displayWidth) * (maxWidth / currMagnification);
        actual[1] = currY + (y / displayHeight) * (maxHeight / currMagnification);

        return actual;
    }

    /**
     * 将实际矩阵(默认坐标系)上的点坐标映射到显示矩形(显示/触摸坐标系)中
     */
    private float[] mappingActualPointToDisplay(float x, float y){
        float[] display = new float[2];

        if (invalidWidthOrHeight){
            return display;
        }

        display[0] = (x - currX) * displayWidth / (maxWidth / currMagnification);
        display[1] = (y - currY) * displayHeight / (maxHeight / currMagnification);

        return display;
    }

    /*******************************************************************
     * output
     */

    public void getSrcDstRect(Rect srcRect, Rect dstRect){
        if (invalidWidthOrHeight){
            if (srcRect != null) {
                srcRect.left = 0;
                srcRect.top = 0;
                srcRect.right = 0;
                srcRect.bottom = 0;
            }
            if (dstRect != null){
                dstRect.left = 0;
                dstRect.top = 0;
                dstRect.right = 0;
                dstRect.bottom = 0;
            }
            return;
        }

        int left = currX < 0 ? 0 : (int) currX;
        int right = (currX + (maxWidth / currMagnification)) > actualWidth ? actualWidth : (int) (currX + (maxWidth / currMagnification));
        int top = currY < 0 ? 0 : (int) currY;
        int bottom = (currY + (maxHeight / currMagnification)) > actualHeight ? actualHeight : (int) (currY + (maxHeight / currMagnification));

        if (srcRect != null) {
            srcRect.left = left;
            srcRect.right = right;
            srcRect.top = top;
            srcRect.bottom = bottom;
        }

        if (dstRect != null){
            float[] leftTopPoint = mappingActualPointToDisplay(left, top);
            float[] rightBottomPoint = mappingActualPointToDisplay(right, bottom);
            dstRect.left = (int) leftTopPoint[0];
            dstRect.top = (int) leftTopPoint[1];
            dstRect.right = (int) rightBottomPoint[0];
            dstRect.bottom = (int) rightBottomPoint[1];
            //TODO 建议做贴边处理, 在边界很接近Rect边缘的时候, 将数值设置为边界值可防止抖动
        }

    }

}
