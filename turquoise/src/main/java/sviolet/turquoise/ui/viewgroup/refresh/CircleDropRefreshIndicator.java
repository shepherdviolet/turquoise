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

package sviolet.turquoise.ui.viewgroup.refresh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.OverScroller;

import java.lang.ref.WeakReference;

/**
 * <p>圆圈进度条下拉刷新指示器, 配合VerticalOverDragContainer使用</p>
 *
 * Created by S.Violet on 2016/11/11.
 */
public class CircleDropRefreshIndicator extends View implements VerticalOverDragContainer.RefreshIndicator {

    public static final int TYPE_TOP = 0;
    public static final int TYPE_BOTTOM = 1;

    private static final int STATE_DRAG = 0;
    private static final int STATE_REFRESHING = 1;

    ////////////////////////////////////////////////////////////////////////////

    private int backgroundColor = 0xFFFFFFFF;
    private int backgroundRadius = 70;
    private int outlineColor = 0xFFE0E0E0;
    private int outlineWidth = 1;
    private int progressColor = 0xFF209090;
    private int progressBackgroundColor = 0xFFF0F0F0;
    private int progressRadius = 50;
    private int progressWidth = 5;
    private int progressSweepAngle = 120;
    private int progressStepAngle = 9;

    private int scrollDuration = 500;

    private RefreshListener refreshListener;

    ////////////////////////////////////////////////////////////////////////////

    private int type = TYPE_TOP;
    private int state = STATE_DRAG;
    private int scrollY = 0;
    private int rotateAngle = 0;

    private float containerOverDragResistance;

    private WeakReference<VerticalOverDragContainer> container;

    private Paint backgroundPaint;
    private Paint outlinePaint;
    private Paint progressBackgroundPaint;
    private Paint progressPaint;

    private OverScroller scroller;

    private Rect canvasRect = new Rect();
    private RectF arcRect = new RectF();

    public CircleDropRefreshIndicator(Context context) {
        super(context);
        init();
    }

    public CircleDropRefreshIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleDropRefreshIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        scroller = new OverScroller(getContext());

        backgroundPaint = new Paint();
        backgroundPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(backgroundColor);

        outlinePaint = new Paint();
        outlinePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(outlineColor);
        outlinePaint.setStrokeWidth(outlineWidth);

        progressBackgroundPaint = new Paint();
        progressBackgroundPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        progressBackgroundPaint.setStyle(Paint.Style.STROKE);
        progressBackgroundPaint.setColor(progressBackgroundColor);
        progressBackgroundPaint.setStrokeWidth(progressWidth);

        progressPaint = new Paint();
        progressPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeWidth(progressWidth);

    }

    @Override
    public void onStateChanged(int state) {

    }

    @Override
    public void onScroll(int scrollY) {
        if (state == STATE_DRAG) {
            if (!scroller.isFinished()){
                scroller.abortAnimation();
            }
            //更新位置
            this.scrollY = scrollY;
            //刷新
            postInvalidate();
        }
    }

    @Override
    public void onTopPark() {

        if (type == TYPE_TOP && state != STATE_REFRESHING){
            state = STATE_REFRESHING;
            scroller.abortAnimation();
            scroller.startScroll(0, scrollY, 0, getContainerOverDragThreshold() - scrollY, scrollDuration);
            VerticalOverDragContainer container = getContainer();
            if (container != null) {
                //在刷新状态时, 将容器的越界拖动阻尼置为0, 容器将无法越界拖动, 相当于冻结了容器
                container.setOverDragResistance(0);
                //直接重置容器的PARK状态, 但因为无法拖动, 也就不会再触发PARK事件了
                container.resetTopPark();
            }
            if (refreshListener != null){
                refreshListener.onRefresh();
            }
            postInvalidate();
        }

    }

    @Override
    public void onBottomPark() {

        if (type == TYPE_BOTTOM && state != STATE_REFRESHING){
            state = STATE_REFRESHING;
            scroller.abortAnimation();
            scroller.startScroll(0, scrollY, 0, -getContainerOverDragThreshold() - scrollY, scrollDuration);
            VerticalOverDragContainer container = getContainer();
            if (container != null) {
                //在刷新状态时, 将容器的越界拖动阻尼置为0, 容器将无法越界拖动, 相当于冻结了容器
                container.setOverDragResistance(0);
                //直接重置容器的PARK状态, 但因为无法拖动, 也就不会再触发PARK事件了
                container.resetBottomPark();
            }
            if (refreshListener != null){
                refreshListener.onRefresh();
            }
            postInvalidate();
        }

    }

    @Override
    public void setContainer(VerticalOverDragContainer container) {
        this.container = new WeakReference<>(container);
        if (container != null){
            //记录容器的越界拖动阻尼
            containerOverDragResistance = container.getOverDragResistance();
            //禁用容器的自身滚动
            container.setDisableContainerScroll(true);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (!scroller.isFinished()){
            scroller.computeScrollOffset();
            this.scrollY = scroller.getCurrY();
            postInvalidate();
        } else if (state == STATE_REFRESHING){
            postInvalidate();
        }

        canvas.save();
        //滚动画布
        switch (type) {
            case TYPE_TOP://顶部进入模式
                canvas.translate(0, scrollY - getContainerOverDragThreshold());
                break;
            case TYPE_BOTTOM://底部进入模式
                canvas.translate(0, scrollY + getContainerOverDragThreshold());
                break;
        }

        if(state == STATE_DRAG){
            int threshold = getContainerOverDragThreshold() != 0 ? getContainerOverDragThreshold() : 100;
            this.rotateAngle = 360 * (this.scrollY % threshold) / threshold;
        }else{
            this.rotateAngle -= progressStepAngle;
        }

        //取画布绘制区域矩形
        canvas.getClipBounds(canvasRect);

        //计算中点
        float centerX = (float)(canvasRect.left + canvasRect.right) / 2f;
        float centerY = (float)getContainerOverDragThreshold() / 2f;
        arcRect.left = centerX - progressRadius;
        arcRect.top = centerY - progressRadius;
        arcRect.right = centerX+ progressRadius;
        arcRect.bottom = centerY + progressRadius;

        //绘制背景
        canvas.drawCircle(centerX, centerY, backgroundRadius, backgroundPaint);
        //绘制外线
        canvas.drawCircle(centerX, centerY, backgroundRadius, outlinePaint);
        //绘制进度条背景
        canvas.drawCircle(centerX, centerY, progressRadius, progressBackgroundPaint);
        //绘制进度条
        canvas.drawArc(arcRect, this.rotateAngle, progressSweepAngle, false, progressPaint);

        canvas.restore();

    }

    /***************************************************************************
     * public
     */

    public void setRefreshListener(RefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    public void reset(){
        if (state == STATE_REFRESHING) {
            state = STATE_DRAG;
            scroller.abortAnimation();
            scroller.startScroll(0, scrollY, 0, -scrollY, scrollDuration);
            VerticalOverDragContainer container = getContainer();
            if (container != null) {
                //恢复越界阻尼
                container.setOverDragResistance(containerOverDragResistance);
            }
            postInvalidate();
        }
    }

    /****************************************************************
     * container
     */

    /**
     * @return 获得弱引用持有的VerticalOverDragContainer
     */
    protected VerticalOverDragContainer getContainer() {
        if (this.container != null) {
            return this.container.get();
        }
        return null;
    }

    /**
     * @return 获得VerticalOverDragContainer的OverDragThreshold
     */
    protected int getContainerOverDragThreshold() {
        VerticalOverDragContainer container = getContainer();
        if (container != null) {
            return container.getOverDragThreshold();
        }
        return 0;
    }

    protected int getContainerScrollDuration(){
        VerticalOverDragContainer container = getContainer();
        if (container != null) {
            return container.getScrollDuration();
        }
        return 0;
    }

    /*******************************************************************************
     * class
     */

    public interface RefreshListener{

        void onRefresh();

    }

}
