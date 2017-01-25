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
import sviolet.turquoise.ui.util.motion.ClickDetector;
import sviolet.turquoise.ui.util.ViewCommonUtils;
import sviolet.turquoise.util.droid.MeasureUtils;

/**
 *
 * <p>线条样式的TabView</p>
 *
 * <p>注意: LineIndicatorTabView内部有一个容器(LinearLayout), 所有的TabItem实际是由这个容器持有的,
 * 因此如果要获取TabItem, 请用{@link LineIndicatorTabView#getTabItemAt(int)}方法</p>
 *
 * <pre>{@code
 *
 *     <sviolet.turquoise.ui.viewgroup.tab.LineIndicatorTabViewForViewPager
 *          android:id="@+id/other_tab_view_tabview"
 *          android:layout_width="match_parent"
 *          android:layout_height="40dp"
 *          sviolet:LineIndicatorTabView_indicatorColor="#30C0C0"
 *          sviolet:LineIndicatorTabView_indicatorWidth="3dp"
 *          sviolet:LineIndicatorTabView_indicatorBottomMargin="0dp"
 *          sviolet:LineIndicatorTabView_indicatorLeftMargin="10dp"
 *          sviolet:LineIndicatorTabView_indicatorRightMargin="10dp"/>
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

    private int indicatorColor = 0xFF30C0C0;
    private int indicatorWidth = 7;
    private int indicatorBottomMargin = 0;
    private int indicatorLeftMargin = 0;
    private int indicatorRightMargin = 0;

    private List<OnPageChangedListener> onPageChangedListeners;//页面切换监听器

    //VAR///////////////////////////////////////

    private float currPage = 0f;//当前指示器页码
    private boolean init = true;//初始状态

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
        setIndicatorColor(typedArray.getColor(R.styleable.LineIndicatorTabView_LineIndicatorTabView_indicatorColor, 0xFF30C0C0));
        setIndicatorWidth((int) typedArray.getDimension(R.styleable.LineIndicatorTabView_LineIndicatorTabView_indicatorWidth, MeasureUtils.dp2px(getContext(), 3)));
        setIndicatorBottomMargin((int) typedArray.getDimension(R.styleable.LineIndicatorTabView_LineIndicatorTabView_indicatorBottomMargin, 0));
        setIndicatorLeftMargin((int) typedArray.getDimension(R.styleable.LineIndicatorTabView_LineIndicatorTabView_indicatorLeftMargin, 0));
        setIndicatorRightMargin((int) typedArray.getDimension(R.styleable.LineIndicatorTabView_LineIndicatorTabView_indicatorRightMargin, 0));
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
        super.removeView(view);
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
        super.removeAllViews();
    }

    /**
     * 获得TabView的Item容器
     */
    public LinearLayout getTabItemContainer(){
        return container;
    }

    /**
     * 获得TabView的ItemView, 用来代替getChildAt方法
     * @param index 位置
     */
    public View getTabItemAt(int index) {
        //由容器代为接收
        if (container == null){
            return null;
        }
        return container.getChildAt(index);
    }

    public void removeAllTabItems(){
        //由容器代为接收
        container.removeAllViews();
    }

    @Override
    public void draw(Canvas canvas) {
        //第一次绘制
        if (init){
            init = false;
            callbackPageChanged((int) Math.floor(currPage), false);
        }

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
     * 直接设置到指定页码(无动画, 无回调)
     */
    public void setToPage(int page){
        moveToPage(page);
    }

    /**
     * 直接设置到指定页码(无动画, 有回调)
     * @param page
     */
    public void setToPageWithCallback(int page){
        moveToPage(page);
        //回调
        callbackPageChanged((int) this.currPage, false);
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
        View currentView = container.getChildAt(pageInt);
        View nextView = container.getChildAt(pageInt + 1);//右边一个Item

        if (nextView == null){
            nextView = currentView;
        }

        if (currentView != null) {
            scrollBy(currentView.getLeft() - getScrollX() - getIndicatorLeftPadding(currentView, nextView) + (int)((this.currPage - pageInt) * currentView.getWidth()), 0);
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
            View child = getTabItemAt(page);
            for (OnPageChangedListener listener : onPageChangedListeners) {
                listener.onPageChanged(page, child, byClick);
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
        page = (int) limitPage(page);

        //滚动Items
        View currentView = container.getChildAt(page);
        View nextView = container.getChildAt(page + 1);//右边一个Item

        if (nextView == null){
            nextView = currentView;
        }

        if (currentView != null) {
            smoothScrollBy(currentView.getLeft() - getScrollX() - getIndicatorLeftPadding(currentView, nextView), 0);
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

        //调整指示器的长度
        indicatorPosition += indicatorLeftMargin;
        indicatorRightPosition -= indicatorRightMargin;

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
        drawIndicatorLine(canvas, indicatorPosition, indicatorRightPosition, getIndicatorTopMargin(canvas, leftView, rightView));

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
     * @param currentView 当前ITEM
     * @param nextView 下一个Item
     */
    protected int getIndicatorLeftPadding(View currentView, View nextView) {
        //默认指示器居中
        float pageOffset = this.currPage - (float)Math.floor(this.currPage);//当前Item偏移量
        return (int) ((getWidth() - (currentView.getWidth() * (1 - pageOffset) + nextView.getWidth() * pageOffset)) / 2);
    }

    /**
     * 指示器的下边距
     * @param canvas 当前画布
     * @param currentView 当前Item
     * @param nextView 下一个Item
     */
    protected int getIndicatorTopMargin(Canvas canvas, View currentView, View nextView){
        return getHeight() - indicatorBottomMargin - indicatorWidth / 2;
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

    public void setIndicatorBottomMargin(int indicatorBottomMargin) {
        if (indicatorBottomMargin < 0){
            throw new RuntimeException("indicatorBottomMargin must >= 0");
        }
        this.indicatorBottomMargin = indicatorBottomMargin;
    }

    public void setIndicatorLeftMargin(int indicatorLeftMargin) {
        if (indicatorLeftMargin < 0){
            throw new RuntimeException("indicatorLeftMargin must >= 0");
        }
        this.indicatorLeftMargin = indicatorLeftMargin;
    }

    public void setIndicatorRightMargin(int indicatorRightMargin) {
        if (indicatorRightMargin < 0){
            throw new RuntimeException("indicatorRightMargin must >= 0");
        }
        this.indicatorRightMargin = indicatorRightMargin;
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
         * @param child 当前页的TabItem(可能为空)
         * @param byClick true:此次事件是由在Tab上点击产生的
         */
        void onPageChanged(int page, View child, boolean byClick);

    }

}
