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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.OverScroller;

import java.lang.ref.WeakReference;

import sviolet.turquoise.R;
import sviolet.turquoise.util.droid.MeasureUtils;

/**
 * <p>圆圈进度条下拉刷新指示器, 配合VerticalOverDragContainer使用</p>
 *
 * <p>具体用法参考demoa中的CircleDropIndicatorRefreshActivity.java</p>
 *
 * Created by S.Violet on 2016/11/11.
 */
public class CircleDropRefreshIndicator extends View implements VerticalOverDragContainer.RefreshIndicator {

    public static final int TYPE_TOP = 0;//指示器在顶部出现
    public static final int TYPE_BOTTOM = 1;//指示器在底部出现

    public static final int POSITION_LEFT = 0;//指示器位置在左侧
    public static final int POSITION_MIDDLE = 1;//指示器位置在中间
    public static final int POSITION_RIGHT = 2;//指示器位置在右侧

    private static final int STATE_DRAG = 0;//拖动状态
    private static final int STATE_REFRESHING = 1;//刷新状态

    ////////////////////////////////////////////////////////////////////////////

    private int type = TYPE_TOP;//类型
    private int position = POSITION_MIDDLE;//水平方向的位置

    private int shadowColor = 0x20000000;//阴影颜色
    private int shadowWidth = 10;//阴影宽度
    private boolean shadowEnabled = true;//true:开启阴影
    private int backgroundColor = 0xFFFFFFFF;//背景颜色
    private int backgroundRadius = 60;//背景半径
    private int outlineColor = 0xFFD0D0D0;//背景边线颜色
    private int outlineWidth = 1;//背景边线宽度
    private int progressBackgroundColor = 0xFFD0D0D0;//进度条背景颜色
    private int progressBackgroundWidth = 1;//进度条背景宽度
    private int progressColor = 0xFF209090;//进度条颜色
    private int progressRadius = 40;//进度条半径
    private int progressWidth = 5;//进度条宽度
    private int progressSweepAngle = 70;//进度条延伸角度
    private int progressStepAngle = 9;//刷新状态时, 进度条滚动速度(每一帧转动的角度)

    private int scrollDuration = 500;//进度条弹回时间

    private RefreshListener refreshListener;//刷新监听器

    ////////////////////////////////////////////////////////////////////////////

    private int state = STATE_DRAG;//当前状态
    private int scrollY = 0;//Y方向滚动位置
    private int rotateAngle = 0;//进度条旋转角度

    private float containerOverDragResistance;//暂存容器越界阻尼
    private int shadowAlpha;//暂存阴影透明度

    private WeakReference<VerticalOverDragContainer> container;//暂存容器

    private Paint shadowPaint;
    private Paint backgroundPaint;
    private Paint outlinePaint;
    private Paint progressBackgroundPaint;
    private Paint progressPaint;

    private OverScroller scroller;

    //性能优化
    private Rect canvasRect = new Rect();
    private RectF arcRect = new RectF();

    public CircleDropRefreshIndicator(Context context) {
        super(context);
        init();
    }

    public CircleDropRefreshIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSetting(context, attrs);
        init();
    }

    public CircleDropRefreshIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSetting(context, attrs);
        init();
    }

    private void initSetting(final Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleDropRefreshIndicator);
        setType(typedArray.getInt(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_type, TYPE_TOP));
        setPosition(typedArray.getInt(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_position, POSITION_MIDDLE));
        setShadowColor(typedArray.getColor(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_shadowColor, 0x20000000));
        setShadowWidth((int) typedArray.getDimension(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_shadowWidth, MeasureUtils.dp2px(getContext(), 3)));
        setShadowEnabled(typedArray.getBoolean(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_shadowEnabled, true));
        setBackgroundColor(typedArray.getColor(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_backgroundColor, 0xFFFFFFFF));
        setBackgroundRadius((int) typedArray.getDimension(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_backgroundRadius, MeasureUtils.dp2px(getContext(), 16)));
        setOutlineColor(typedArray.getColor(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_outlineColor, 0xFFD0D0D0));
        setOutlineWidth((int) typedArray.getDimension(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_outlineWidth, 1));
        setProgressBackgroundColor(typedArray.getColor(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_progressBackgroundColor, 0xFFD0D0D0));
        setProgressBackgroundWidth((int) typedArray.getDimension(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_progressBackgroundWidth, 1));
        setProgressColor(typedArray.getColor(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_progressColor, 0xFF209090));
        setProgressRadius((int) typedArray.getDimension(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_progressRadius, MeasureUtils.dp2px(getContext(), 11)));
        setProgressWidth((int) typedArray.getDimension(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_progressWidth, MeasureUtils.dp2px(getContext(), 1)));
        setProgressSweepAngle(typedArray.getInt(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_progressSweepAngle, 70));
        setProgressStepAngle(typedArray.getInt(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_progressStepAngle, 9));
        setScrollDuration(typedArray.getInt(R.styleable.CircleDropRefreshIndicator_CircleDropRefreshIndicator_scrollDuration, 500));
        typedArray.recycle();
    }

    private void init(){
        scroller = new OverScroller(getContext());

        shadowPaint = new Paint();
        shadowPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(shadowColor);
        shadowAlpha = shadowPaint.getAlpha();

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
        progressBackgroundPaint.setStrokeWidth(progressBackgroundWidth);

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
        //拖动状态才能进入
        if (state == STATE_DRAG) {
            //暂停动画
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
    public boolean onTopPark() {

        //顶部模式, 非刷新状态才能进入
        if (type == TYPE_TOP && state != STATE_REFRESHING){
            //置为刷新状态
            state = STATE_REFRESHING;
            //停止滚动
            scroller.abortAnimation();
            //滚动到刷新位置
            scroller.startScroll(0, scrollY, 0, getContainerOverDragThreshold() - scrollY, scrollDuration);
            VerticalOverDragContainer container = getContainer();
            if (container != null) {
                //在刷新状态时, 将容器的越界拖动阻尼置为0, 容器将无法越界拖动, 相当于冻结了容器
                container.setOverDragResistance(0);
                //直接重置容器的PARK状态, 但因为无法拖动, 也就不会再触发PARK事件了
                container.resetTopPark();
            }
            //回调监听
            if (refreshListener != null){
                refreshListener.onRefresh();
            }
            //刷新
            postInvalidate();
            //有效地处理了PARK事件时, 返回true, 使得容器进入PARK状态
            return true;
        }

        //未能有效处理PARK事件时, 返回false, 防止容器意外进入PARK状态
        return false;
    }

    @Override
    public boolean onBottomPark() {

        //底部模式, 非刷新状态才能进入
        if (type == TYPE_BOTTOM && state != STATE_REFRESHING){
            //置为刷新状态
            state = STATE_REFRESHING;
            //停止滚动
            scroller.abortAnimation();
            //滚动到刷新位置
            scroller.startScroll(0, scrollY, 0, -getContainerOverDragThreshold() - scrollY, scrollDuration);
            VerticalOverDragContainer container = getContainer();
            if (container != null) {
                //在刷新状态时, 将容器的越界拖动阻尼置为0, 容器将无法越界拖动, 相当于冻结了容器
                container.setOverDragResistance(0);
                //直接重置容器的PARK状态, 但因为无法拖动, 也就不会再触发PARK事件了
                container.resetBottomPark();
            }
            //回调监听
            if (refreshListener != null){
                refreshListener.onRefresh();
            }
            //刷新
            postInvalidate();
            //有效地处理了PARK事件时, 返回true, 使得容器进入PARK状态
            return true;
        }

        //未能有效处理PARK事件时, 返回false, 防止容器意外进入PARK状态
        return false;
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
            //滚动状态
            scroller.computeScrollOffset();
            this.scrollY = scroller.getCurrY();
            postInvalidate();
        } else if (state == STATE_REFRESHING){
            //刷新状态持续刷新
            postInvalidate();
        }

        //保存画布状态
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
        float centerX;
        switch (position){
            case POSITION_LEFT:
                centerX = canvasRect.left + (float)getContainerOverDragThreshold() / 2f;
                break;
            case POSITION_RIGHT:
                centerX = canvasRect.right - (float)getContainerOverDragThreshold() / 2f;
                break;
            case POSITION_MIDDLE:
            default:
                centerX = (float)(canvasRect.left + canvasRect.right) / 2f;
                break;
        }
        float centerY = (float)getContainerOverDragThreshold() / 2f;
        arcRect.left = centerX - progressRadius;
        arcRect.top = centerY - progressRadius;
        arcRect.right = centerX+ progressRadius;
        arcRect.bottom = centerY + progressRadius;

        //绘制阴影
        if (shadowEnabled) {
            shadowPaint.setAlpha(shadowAlpha / shadowWidth);
            for (int i = 1; i <= shadowWidth; i++) {
                canvas.drawCircle(centerX, centerY, backgroundRadius + i, shadowPaint);
            }
        }
        //绘制背景
        canvas.drawCircle(centerX, centerY, backgroundRadius, backgroundPaint);
        //绘制外线
        canvas.drawCircle(centerX, centerY, backgroundRadius, outlinePaint);
        //绘制进度条背景
        canvas.drawCircle(centerX, centerY, progressRadius, progressBackgroundPaint);
        //绘制进度条
        canvas.drawArc(arcRect, this.rotateAngle, progressSweepAngle, false, progressPaint);

        //回复画布状态
        canvas.restore();

    }

    /***************************************************************************
     * public
     */

    /**
     * @param refreshListener 设置刷新监听器
     */
    public void setRefreshListener(RefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    /**
     * [重要]重置刷新状态, 进度条弹回, 并回复容器的越界阻尼, 可以重新接收下拉事件触发刷新
     */
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

    /**
     * @param type 指示器类型
     */
    public void setType(int type) {
        if (type < TYPE_TOP || type > TYPE_BOTTOM){
            throw new RuntimeException("invalid type:" + type);
        }
        this.type = type;
    }

    /**
     * @param position 指示器位置
     */
    public void setPosition(int position) {
        if (position < POSITION_LEFT || position > POSITION_RIGHT){
            throw new RuntimeException("invalid position:" + position);
        }
        this.position = position;
    }

    /**
     * @param shadowColor 阴影颜色
     */
    public void setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
    }

    /**
     * @param shadowWidth 阴影宽度
     */
    public void setShadowWidth(int shadowWidth) {
        if (shadowWidth < 0){
            throw new RuntimeException("shadowWidth must >= 0");
        }
        this.shadowWidth = shadowWidth;
    }

    /**
     * @param shadowEnabled true:开启阴影
     */
    public void setShadowEnabled(boolean shadowEnabled) {
        this.shadowEnabled = shadowEnabled;
    }

    /**
     * @param backgroundColor 背景颜色
     */
    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * @param backgroundRadius 背景半径
     */
    public void setBackgroundRadius(int backgroundRadius) {
        if (backgroundRadius < 0){
            throw new RuntimeException("backgroundRadius must >= 0");
        }
        this.backgroundRadius = backgroundRadius;
    }

    /**
     * @param outlineColor 背景外线颜色
     */
    public void setOutlineColor(int outlineColor) {
        this.outlineColor = outlineColor;
    }

    /**
     * @param outlineWidth 背景外线宽度
     */
    public void setOutlineWidth(int outlineWidth) {
        if (outlineWidth < 0){
            throw new RuntimeException("outlineWidth must >= 0");
        }
        this.outlineWidth = outlineWidth;
    }

    /**
     * @param progressBackgroundColor 进度条背景颜色
     */
    public void setProgressBackgroundColor(int progressBackgroundColor) {
        this.progressBackgroundColor = progressBackgroundColor;
    }

    /**
     * @param progressBackgroundWidth 进度条背景宽度
     */
    public void setProgressBackgroundWidth(int progressBackgroundWidth) {
        if (progressBackgroundWidth < 0){
            throw new RuntimeException("progressBackgroundWidth must >= 0");
        }
        this.progressBackgroundWidth = progressBackgroundWidth;
    }

    /**
     * @param progressColor 进度条颜色
     */
    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
    }

    /**
     * @param progressRadius 进度条半径
     */
    public void setProgressRadius(int progressRadius) {
        if (progressRadius < 0){
            throw new RuntimeException("progressRadius must >= 0");
        }
        this.progressRadius = progressRadius;
    }

    /**
     * @param progressWidth 进度条宽度
     */
    public void setProgressWidth(int progressWidth) {
        if (progressWidth < 0){
            throw new RuntimeException("progressWidth must >= 0");
        }
        this.progressWidth = progressWidth;
    }

    /**
     * @param progressSweepAngle 进度条延伸角度
     */
    public void setProgressSweepAngle(int progressSweepAngle) {
        if (progressSweepAngle < 0 || progressSweepAngle > 360){
            throw new RuntimeException("progressSweepAngle must >= 0 and <= 360");
        }
        this.progressSweepAngle = progressSweepAngle;
    }

    /**
     * @param progressStepAngle 刷新状态时, 进度条滚动速度(每一帧转动的角度)
     */
    public void setProgressStepAngle(int progressStepAngle) {
        if (progressStepAngle <= 0 || progressStepAngle > 360){
            throw new RuntimeException("progressStepAngle must > 0 and <= 360");
        }
        this.progressStepAngle = progressStepAngle;
    }

    /**
     * @param scrollDuration 弹回时间
     */
    public void setScrollDuration(int scrollDuration) {
        if (scrollDuration < 0){
            throw new RuntimeException("scrollDuration must >= 0");
        }
        this.scrollDuration = scrollDuration;
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

    /**
     * 刷新事件监听器
     */
    public interface RefreshListener{

        void onRefresh();

    }

}
