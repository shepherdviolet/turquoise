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

package sviolet.turquoise.ui.viewgroup.tab;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import sviolet.turquoise.R;
import sviolet.turquoise.common.compat.CompatOverScroller;
import sviolet.turquoise.ui.util.ClickDetector;
import sviolet.turquoise.ui.util.ViewCommonUtils;
import sviolet.turquoise.util.droid.MeasureUtils;

/**
 *
 * <p>线条样式的TabView</p>
 *
 * <pre>{@code
 *
 *     <sviolet.turquoise.ui.viewgroup.tab.LineIndicatorTabViewForViewPager
 *          android:id="@+id/other_tab_view_tabview"
 *          android:layout_width="match_parent"
 *          android:layout_height="40dp"
 *          sviolet:LineIndicatorTabView_indicatorColor="#209090"
 *          sviolet:LineIndicatorTabView_indicatorWidth="3dp"
 *          sviolet:LineIndicatorTabView_indicatorBottomPadding="0dp"/>
 *
 * }</pre>
 *
 * Created by S.Violet on 2016/11/15.
 */

public class LineIndicatorTabView extends HorizontalScrollView {

    private LinearLayout container;//子控件由此layout持有
    private ClickDetector detector;//点击检测器
    private CompatOverScroller scroller;//scroller

    private Paint indicatorPaint;//指示器画笔

    //setting///////////////////////////////////

    private int indicatorColor = 0xFF209090;
    private int indicatorWidth = 7;
    private int indicatorBottomPadding = 0;

    private List<OnPageChangedListener> onPageChangedListeners;//页面切换监听器

    //VAR///////////////////////////////////////

    private float currPage = 0f;//当前指示器页码

    //性能优化////////////////////////////////////

    private int[] viewLocationCache = new int[2];//复用

    public LineIndicatorTabView(Context context) {
        super(context);
        init();
    }

    public LineIndicatorTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context, attrs);
        init();
    }

    public LineIndicatorTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData(context, attrs);
        init();
    }

    private void initData(Context context, AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LineIndicatorTabView);
        setIndicatorColor(typedArray.getColor(R.styleable.LineIndicatorTabView_LineIndicatorTabView_indicatorColor, 0xFF209090));
        setIndicatorWidth((int) typedArray.getDimension(R.styleable.LineIndicatorTabView_LineIndicatorTabView_indicatorWidth, MeasureUtils.dp2px(getContext(), 3)));
        setIndicatorBottomPadding((int) typedArray.getDimension(R.styleable.LineIndicatorTabView_LineIndicatorTabView_indicatorBottomPadding, 0));
        typedArray.recycle();
    }

    private void init(){

        //初始化
        detector = new ClickDetector(getContext());
        scroller = new CompatOverScroller(getContext());

        //初始化容器, 并将容器作为子控件
        container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        super.addView(container, -1, params);

        //禁用越界效果
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        //初始化指示器画笔
        indicatorPaint = new Paint();
        indicatorPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        indicatorPaint.setColor(indicatorColor);
        indicatorPaint.setStrokeWidth(indicatorWidth);

    }

    @Override
    public void addView(View child) {
        //由容器代为接收
        container.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        //由容器代为接收
        container.addView(child, index);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        //由容器代为接收
        container.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        //由容器代为接收
        container.addView(child, index, params);
    }

    @Override
    public void removeView(View view) {
        //由容器代为接收
        container.removeView(view);
    }

    @Override
    public void removeViewAt(int index) {
        //由容器代为接收
        container.removeViewAt(index);
    }

    @Override
    public void removeAllViews() {
        //由容器代为接收
        container.removeAllViews();
    }

    @Override
    public void draw(Canvas canvas) {
        //计算scroller
        if (!scroller.isFinished()){
            scroller.computeScrollOffset();
            currPage = (float)scroller.getCurrX() / 256f;
            postInvalidate();
        }

        //绘制子控件
        super.draw(canvas);
        //绘制指示器
        drawIndicator(canvas, currPage);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //实现点击Item跳页
        if (detector.detect(ev)){
            float rawX = ev.getRawX();
            float rawY = ev.getRawY();
            for (int i = 0 ; i < container.getChildCount() ; i++){
                if (ViewCommonUtils.isPointOnView(container.getChildAt(i), rawX, rawY, viewLocationCache)){
                    scrollToPageInner(i, true);
                    break;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * ViewPager等通过此方法来控制TabView滚动
     * @param page 当前页码数
     */
    protected void moveToPage(float page){
        //滚动指示器
        this.currPage = limitPage(page);

        //滚动Items
        int pageInt = (int) Math.floor(this.currPage);
        View targetView = container.getChildAt(pageInt);
        if (targetView != null) {
            scrollBy(targetView.getLeft() - getScrollX() - getIndicatorLeftPadding(targetView) + (int)((this.currPage - pageInt) * targetView.getWidth()), 0);
        }

        postInvalidate();
    }

    /**
     * ViewPager等通过此方法设定当前页面, 会回调onPageChangedListener监听器
     * @param page 页码数
     * @param byClick true:TabView上的点击事件引起的滚动
     */
    protected void callbackPageChanged(int page, boolean byClick){
        //回调
        if (onPageChangedListeners != null){
            for (OnPageChangedListener listener : onPageChangedListeners) {
                listener.onPageChanged(page, byClick);
            }
        }
    }

    /**
     * 直接滚动到指定页
     * @param page 指定页码
     */
    protected void scrollToPage(int page){
        scrollToPageInner(page, false);
    }

    /**
     * 直接滚动到指定页
     * @param page 指定页码
     * @param byClick true:TabView上的点击事件引起的滚动
     */
    private void scrollToPageInner(int page, boolean byClick) {
        //滚动Items
        View targetView = container.getChildAt(page);
        if (targetView != null) {
            smoothScrollBy(targetView.getLeft() - getScrollX() - getIndicatorLeftPadding(targetView), 0);
        }

        //滚动指示器
        int curr = (int)currPage << 8;
        scroller.abortAnimation();
        scroller.startScroll(curr, 0, (page << 8) - curr, 0);
        postInvalidate();

        //回调
        callbackPageChanged(page, byClick);
    }

    /**
     * 根据当前标记的页码绘制标记
     * @param canvas 画布
     * @param page 页码
     */
    protected void drawIndicator(Canvas canvas, float page){
        int childCount = container.getChildCount();
        if (childCount <= 0){
            return;
        }
        float limitedPage = limitPage(page);
        int leftPage = (int) Math.floor(limitedPage);//当前Item页码
        float pageOffset = limitedPage - leftPage;//当前Item偏移量

        View leftView = container.getChildAt(leftPage);//当前Item
        View rightView = container.getChildAt(leftPage + 1);//右边一个Item

        if (leftView == null){
            return;
        }
        if (rightView == null){
            rightView = leftView;
        }

        int leftViewPosition = leftView.getLeft();//当前View的位置
        int rightViewPosition = rightView.getLeft();//右边View的位置
        int leftViewWidth = leftView.getWidth();////当前View的宽度
        int rightViewWidth = rightView.getWidth();//右边View的宽度

        //计算指示器位置
        float indicatorPosition = leftViewPosition + (float)(rightViewPosition - leftViewPosition) * pageOffset;
        //计算指示器宽度
        float indicatorWidth = leftViewWidth - (float)(leftViewWidth - rightViewWidth) * pageOffset;
        //计算指示器右边位置
        float indicatorRightPosition = indicatorPosition + indicatorWidth;

        int parentScrollX = getScrollX();

        //超出屏幕的部分不绘制
        if (indicatorPosition < parentScrollX){
            indicatorPosition = 0;
        }
        if (indicatorRightPosition > parentScrollX + getWidth()){
            indicatorRightPosition = parentScrollX + getWidth();
        }

        //指示器长度为0不绘制
        if (indicatorRightPosition <= indicatorPosition){
            return;
        }

        //根据标记的左右位置绘制
        drawIndicatorLine(canvas, indicatorPosition, indicatorRightPosition, getIndicatorTopPadding(canvas, leftView));

    }

    /**
     * 根据标记的左右位置, 绘制标记
     * @param canvas 画布
     * @param indicatorPosition 标记左位置
     * @param indicatorRightPosition 标记右位置
     */
    protected void drawIndicatorLine(Canvas canvas, float indicatorPosition, float indicatorRightPosition, float topPadding) {
        //默认绘制一个普通的线
        canvas.drawLine(indicatorPosition, topPadding, indicatorRightPosition, topPadding, indicatorPaint);
    }

    /**
     * 限制当前标记的页码, 防止超出限制
     * @param page 当前标记页码
     */
    protected float limitPage(float page){
        int childCount = container.getChildCount();
        if (childCount <= 0){
            return 0;
        }
        if (page < 0){
            return 0f;
        }else if (page > childCount - 1){
            return childCount - 1f;
        }
        return page;
    }

    /**
     * 指示器的左边距
     * @param targetView 当前ITEM
     */
    protected int getIndicatorLeftPadding(View targetView) {
        //默认指示器居中
        return (getWidth() - targetView.getWidth()) / 2;
    }

    /**
     * 指示器的下边距
     * @param canvas 当前画布
     * @param targetView 目标Item
     */
    protected int getIndicatorTopPadding(Canvas canvas, View targetView){
        return getHeight() - indicatorBottomPadding - indicatorWidth / 2;
    }

    /******************************************************************************
     * public
     */

    public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
    }

    public void setIndicatorWidth(int indicatorWidth) {
        if (indicatorWidth <= 0){
            throw new RuntimeException("indicatorWidth must > 0");
        }
        this.indicatorWidth = indicatorWidth;
    }

    public void setIndicatorBottomPadding(int indicatorBottomPadding) {
        if (indicatorBottomPadding < 0){
            throw new RuntimeException("indicatorBottomPadding must >= 0");
        }
        this.indicatorBottomPadding = indicatorBottomPadding;
    }

    public void addOnPageChangedListener(OnPageChangedListener listener){
        if (this.onPageChangedListeners == null){
            this.onPageChangedListeners = new ArrayList<>();
        }
        this.onPageChangedListeners.add(listener);
    }

    /******************************************************************************
     * class
     */

    public interface OnPageChangedListener{

        /**
         * @param page 页码数
         * @param byClick true:此次事件是由在Tab上点击产生的
         */
        void onPageChanged(int page, boolean byClick);

    }

}
