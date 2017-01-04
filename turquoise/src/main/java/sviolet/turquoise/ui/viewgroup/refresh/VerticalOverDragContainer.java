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
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

import sviolet.turquoise.R;
import sviolet.turquoise.common.compat.CompatOverScroller;
import sviolet.turquoise.enhance.common.WeakHandler;
import sviolet.turquoise.ui.util.ListViewUtils;
import sviolet.turquoise.ui.util.ScrollViewUtils;
import sviolet.turquoise.util.common.DateTimeUtils;
import sviolet.turquoise.util.droid.MeasureUtils;
import sviolet.turquoise.util.droid.MotionEventUtils;

/**
 * <p>垂直方向越界拖动容器(RelativeLayout), 可用于实现下拉刷新上拉加载</p>
 *
 * <p>PARK: 即越界拖动超过设定值(overDragThreshold)后, 停止在设定位置, 用于实现下拉刷新/上拉加载,
 * PARK状态即下拉刷新中的状态.</p>
 *
 * <p>注意: 当发生PARK事件后, 若监听器{@link RefreshIndicator#onTopPark()}/{@link RefreshIndicator#onBottomPark()}
 * 返回true, VerticalOverDragContainer会保持PARK状态, 不会再发生相同的PARK事件,必须调用resetTopPark/resetBottomPark方法,
 * 重置状态, 才会再次发生PARK事件. 在实际使用中, 接收到PARK事件时, 开始进行数据刷新, 回调方法返回true, 当数据刷新完成后,
 * 调用{@link VerticalOverDragContainer#resetTopPark()}/{@link VerticalOverDragContainer#resetBottomPark()}方法重置状态.
 * 当你使用{@link SimpleVerticalRefreshIndicatorGroup}配合实现下拉刷新时, 调用
 * {@link SimpleVerticalRefreshIndicatorGroup#reset(boolean)}方法可以起到相同的作用.
 * 当你使用{@link CircleDropRefreshIndicator}实现下拉刷新时, 调用{@link CircleDropRefreshIndicator#reset()}
 * 方法可以起到相同作用.</p>
 *
 * <p>
 *     支持的子控件:<br/>
 *     1.ScrollView<br/>
 *     2.ListView<br/>
 * </p>
 *
 * <p>
 *     将VerticalOverDragContainer作为父控件, 将需要越界拖动的ScrollView/ListView等作为子控件放置在其内部,
 *     原则上, VerticalOverDragContainer只能容纳一个子控件, 如果必须要容纳多个控件, 请务必将ScrollView/ListView等
 *     自身会滚动的控件放置在最后一个, VerticalOverDragContainer会根据最后一个子控件是否到达顶部/底部来判断
 *     是否要发生越界滚动.
 * </p>
 *
 * <pre>{@code
 *      <sviolet.turquoise.ui.viewgroup.refresh.VerticalOverDragContainer
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent"
 *          sviolet:VerticalOverDragContainer_overDragThreshold="70dp"
 *          sviolet:VerticalOverDragContainer_overDragResistance="0.4"
 *          sviolet:VerticalOverDragContainer_scrollDuration="300"
 *          sviolet:VerticalOverDragContainer_topParkEnabled="true"
 *          sviolet:VerticalOverDragContainer_bottomParkEnabled="false"
 *          sviolet:VerticalOverDragContainer_disableIfHorizontalDrag="false">
 *
 *          ...
 *
 *          <ScrollView
 *              android:layout_width="match_parent"
 *              android:layout_height="match_parent">
 *
 *              ...
 *
 *          </ScrollView>
 *      </sviolet.turquoise.ui.viewgroup.refresh.VerticalOverDragContainer>
 * }</pre>
 *
 * <p>具体用法参考demoa中的OverDragRefreshActivity.java</p>
 *
 * Created by S.Violet on 2016/11/3.
 */
public class VerticalOverDragContainer extends RelativeLayout {

    //状态
    private static final int STATE_RELEASE = 0;
    private static final int STATE_HOLD = 1;
    private static final int STATE_TOP_OVER_DRAG = 2;
    private static final int STATE_BOTTOM_OVER_DRAG = 3;
    private static final int STATE_HORIZONTAL_DRAG = 4;

    //手势方向
    private static final int DRAG_DIRECTION_HORIZONTAL = -1;
    private static final int DRAG_DIRECTION_UNKNOWN = 0;
    private static final int DRAG_DIRECTION_VERTICAL = 1;

    //////////////////////////////////////////////////////

    //越界拖动界限, 超过该界限则进入PARK状态(停止在界限上, 用于实现下拉刷新上拉加载)
    private int overDragThreshold = 300;
    //越界拖动阻尼, 0-1, 值越小拖动越慢
    private float overDragResistance = 0.4f;

    //scroller的回弹时间
    private int scrollDuration = 300;

    //顶部PARK允许
    private boolean topParkEnabled = false;
    //底部PARK允许
    private boolean bottomParkEnabled = false;

    //true:当出现水平方向的手势时, 禁用越界拖动
    private boolean disableIfHorizontalDrag = false;
    //true:禁止容器自身的滚动(用于实现本身不越界滚动, 刷新指示器滚动的场合)
    private boolean disableContainerScroll = false;
    //true:精确模拟ACTION_DOWN事件(分发给子控件)
    private boolean preciseTouchEmulate = true;

    private long autoResetDelay = 120000;//当长时间没有归位时, 自动归位
    private long parkInterval = 0;//最小PARK间隔

    //监听器
    private List<RefreshIndicator> refreshIndicatorList;

    //////////////////////////////////////////////////////

    private int mTouchSlop;

    private int state = STATE_RELEASE;
    private int dragDirection = DRAG_DIRECTION_UNKNOWN;

    //当前位置
    private float scrollY;

    //按下时的坐标
    private float downX;
    private float downY;
    //上一次的Y值
    private float lastY;
    //上一次触点ID
    private int lastPointId = -1;

    private boolean topParked = false;
    private boolean bottomParked = false;

    //记录上次PARK的时间
    private long lastTopParkTime;
    private long lastBottomParkTime;

    private CompatOverScroller scroller;

    private MotionEventUtils.TouchPoints touchPoints = new MotionEventUtils.TouchPoints();

    public VerticalOverDragContainer(Context context) {
        super(context);
        //初始化
        init();
    }

    public VerticalOverDragContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        //初始化
        init();
        initSetting(context, attrs);
    }

    public VerticalOverDragContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化
        init();
        initSetting(context, attrs);
    }

    /**
     * 初始化
     */
    private void init() {
        this.mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.scroller = new CompatOverScroller(getContext());
    }

    /**
     * 初始化配置
     */
    private void initSetting(final Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerticalOverDragContainer);
        setOverDragThreshold(typedArray.getDimensionPixelOffset(R.styleable.VerticalOverDragContainer_VerticalOverDragContainer_overDragThreshold, MeasureUtils.dp2px(getContext(), 70)));
        setOverDragResistance(typedArray.getFloat(R.styleable.VerticalOverDragContainer_VerticalOverDragContainer_overDragResistance, 0.4f));
        setScrollDuration(typedArray.getInteger(R.styleable.VerticalOverDragContainer_VerticalOverDragContainer_scrollDuration, 300));
        setTopParkEnabled(typedArray.getBoolean(R.styleable.VerticalOverDragContainer_VerticalOverDragContainer_topParkEnabled, false));
        setBottomParkEnabled(typedArray.getBoolean(R.styleable.VerticalOverDragContainer_VerticalOverDragContainer_bottomParkEnabled, false));
        setDisableIfHorizontalDrag(typedArray.getBoolean(R.styleable.VerticalOverDragContainer_VerticalOverDragContainer_disableIfHorizontalDrag, false));
        setDisableContainerScroll(typedArray.getBoolean(R.styleable.VerticalOverDragContainer_VerticalOverDragContainer_disableContainerScroll, false));
        typedArray.recycle();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //没有子控件时直接分发事件
        int childCount = getChildCount();
        if (childCount < 1) {
            return super.dispatchTouchEvent(ev);
        }

        //取滚动的子控件
        View child = getScrollChild();
        //子控件是否在顶部或底部
        ReachState reachState = checkReachState(child);

        //当前坐标
        float currX = ev.getX();
        float currY = ev.getY();

        //Y方向位移量
        float distanceY = calculateMoveDistance(ev);

        //ACTION_CANCEL
        boolean isCancel = false;

        switch (ev.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                //重置状态
                resetState();
                //记录坐标
                this.downX = currX;
                this.downY = currY;
                this.lastY = currY;
                //持有状态
                stateToHold();
                //事件分发给子控件处理
                return super.dispatchTouchEvent(ev);
            case MotionEvent.ACTION_POINTER_DOWN:
                if (this.state == STATE_HOLD || this.state == STATE_HORIZONTAL_DRAG){
                    return super.dispatchTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                switch (this.state){
                    case STATE_HOLD:
                        /**
                         * 判定为横向滑动时, 禁止拦截事件, 可配置
                         */
                        if (disableIfHorizontalDrag && dragDirection == DRAG_DIRECTION_UNKNOWN){
                            if (Math.abs(currY - downY) > mTouchSlop){
                                dragDirection = DRAG_DIRECTION_VERTICAL;
                            } else if (Math.abs(currX - downX) > mTouchSlop) {
                                dragDirection = DRAG_DIRECTION_HORIZONTAL;
                                stateToHorizontalDrag();
                                return super.dispatchTouchEvent(ev);
                            }
                        }
                        //判断状态
                        if ((currY - downY > mTouchSlop && distanceY > 0 && reachState.reachTop())
                                || (currY - downY < -mTouchSlop && distanceY < 0 && scrollY > 0)){
                            /**
                             * 子控件到达顶部, 且继续向下拉动
                             */
                            //上边越界
                            stateToTopOverDrag();
                            //越界阻尼
                            if (scrollY >= 0 && distanceY > 0){
                                distanceY = distanceY * overDragResistance;
                            }
                            //越界滚动
                            scrollByOffset(distanceY);
                            //模拟取消事件给子控件
                            MotionEventUtils.emulateCancelEvent(ev, emulateMotionEventExecutor);
                            return true;
                        } else if ((currY - downY < -mTouchSlop && distanceY < 0 && reachState.reachBottom())
                                || (currY - downY > mTouchSlop && distanceY > 0 && scrollY < 0)){
                            /**
                             * 子控件到达底部, 且继续向上拉动
                             */
                            //下边越界
                            stateToBottomOverDrag();
                            //越界阻尼
                            if (scrollY <= 0 && distanceY < 0){
                                distanceY = distanceY * overDragResistance;
                            }
                            //越界滚动
                            scrollByOffset(distanceY);
                            //模拟取消事件给子控件
                            MotionEventUtils.emulateCancelEvent(ev, emulateMotionEventExecutor);
                            return true;
                        } else {
                            /**
                             * 子控件未到达顶部或底部
                             */
                            //无越界, 直接分发事件给子控件
                            return super.dispatchTouchEvent(ev);
                        }
                    case STATE_TOP_OVER_DRAG:
                        if (scrollY + distanceY <= 0){
                            //上边越界结束
                            if (reachState.reachBottom()){
                                //如果滚动子控件同时也到达底部的话, 直接进入下边越界状态
                                stateToBottomOverDrag();
                                //越界滚动
                                scrollByOffset(distanceY);
                                return true;
                            }
                            //归位
                            scrollToTarget(0);
                            //HOLD模式
                            stateToHold();
                            //模拟DOWN事件给子控件
                            MotionEventUtils.emulateDownEvent(ev, preciseTouchEmulate, emulateMotionEventExecutor);
                            //分发事件给子控件
                            return super.dispatchTouchEvent(ev);
                        }
                        //越界阻尼
                        if (!topParked && scrollY >= 0 && distanceY > 0){
                            distanceY = distanceY * overDragResistance;
                        } else if (topParked && scrollY >= overDragThreshold && distanceY > 0){
                            distanceY = distanceY * overDragResistance;
                        }
                        //越界滚动
                        scrollByOffset(distanceY);
                        return true;
                    case STATE_BOTTOM_OVER_DRAG:
                        if (scrollY + distanceY >= 0){
                            //下边越界结束
                            if (reachState.reachTop()){
                                //如果滚动子控件同时也到达顶部的话, 直接进入上边越界状态
                                stateToTopOverDrag();
                                //越界滚动
                                scrollByOffset(distanceY);
                                return true;
                            }
                            //归位
                            scrollToTarget(0);
                            //HOLD模式
                            stateToHold();
                            //模拟DOWN事件给子控件
                            MotionEventUtils.emulateDownEvent(ev, preciseTouchEmulate, emulateMotionEventExecutor);
                            //分发事件给子控件
                            return super.dispatchTouchEvent(ev);
                        }
                        //越界阻尼
                        if (!bottomParked && scrollY <= 0 && distanceY < 0){
                            distanceY = distanceY * overDragResistance;
                        } else if (bottomParked && scrollY <= -overDragThreshold && distanceY < 0){
                            distanceY = distanceY * overDragResistance;
                        }
                        //越界滚动
                        scrollByOffset(distanceY);
                        return true;
                    case STATE_HORIZONTAL_DRAG:
                        //横向拖动模式直接分发给子控件
                        return super.dispatchTouchEvent(ev);
                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                if (this.state == STATE_HOLD || this.state == STATE_HORIZONTAL_DRAG){
                    return super.dispatchTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                //事件被上层拦截
                isCancel = true;
            case MotionEvent.ACTION_UP:
                int lastState = this.state;
                stateToRelease();
                switch (lastState){
                    case STATE_TOP_OVER_DRAG:
                        if (!isCancel) {
                            if (topParkEnabled && scrollY >= overDragThreshold) {
                                if (!topParked) {
                                    //过滤频繁的PARK
                                    if (DateTimeUtils.getCurrentTimeMillis() - lastTopParkTime < parkInterval){
                                        callbackTopParkIgnore();
                                        //弹回原始位置
                                        free(true);
                                        return true;
                                    }
                                    /**
                                     * (1) 返回true时, 表明接受了该事件, 容器进入顶部PARK状态, 并阻断后续触发的顶部PARK事件(不管怎么
                                     * 拖动都不会再触发顶部PARK事件), 直到调用{@link VerticalOverDragContainer#resetTopPark()}
                                     * 方法解除PARK状态并弹回. 例如:监听器中开始刷新流程, 返回true, 等待刷新流程结束后, 调用
                                     * {@link VerticalOverDragContainer#resetTopPark()}方法, 容器会弹回初始状态, 并开始接受
                                     * 下一个PARK事件.
                                     * (2) 返回false时, 表明监听器不处理该事件, 容器不进入PARK状态, 无需调用
                                     * {@link VerticalOverDragContainer#resetTopPark()}方法重置, 容器会继续响应接下来的顶部
                                     * PARK事件.
                                     */
                                    //先置为PARK状态
                                    topParked = true;
                                    if (callbackTopPark()){
                                        //记录当前时间
                                        lastTopParkTime = DateTimeUtils.getCurrentTimeMillis();
                                        //长时间没归位时, 自动归位
                                        myHandler.sendEmptyMessageDelayed(MyHandler.HANDLER_RESET_TOP_PARK_AUTO, autoResetDelay);
                                    } else {
                                        //若回调返回false, 则解除PARK状态
                                        //之所以不在回调返回true时, 将PARK置为true, 是防止监听器执行时, 状态被重置为false, 回到这里被重置掉
                                        topParked = false;
                                    }
                                }
                            }
                        }
                        //弹回PARK位置
                        free(false);
                        return true;
                    case STATE_BOTTOM_OVER_DRAG:
                        if (!isCancel) {
                            if (bottomParkEnabled && scrollY <= -overDragThreshold) {
                                if (!bottomParked) {
                                    //过滤频繁的PARK
                                    if (DateTimeUtils.getCurrentTimeMillis() - lastBottomParkTime < parkInterval){
                                        callbackBottomParkIgnore();
                                        //弹回原始位置
                                        free(true);
                                        return true;
                                    }
                                    /**
                                     * (1) 返回true时, 表明接受了该事件, 容器进入底部PARK状态, 并阻断后续触发的底部PARK事件(不管怎么
                                     * 拖动都不会再触发底部PARK事件), 直到调用{@link VerticalOverDragContainer#resetBottomPark()}
                                     * 方法解除PARK状态并弹回. 例如:监听器中开始加载流程, 返回true, 等待加载流程结束后, 调用
                                     * {@link VerticalOverDragContainer#resetBottomPark()}方法, 容器会弹回初始状态, 并开始接受
                                     * 下一个PARK事件.
                                     * (2) 返回false时, 表明监听器不处理该事件, 容器不进入PARK状态, 无需调用
                                     * {@link VerticalOverDragContainer#resetBottomPark()}方法重置, 容器会继续响应接下来的底部
                                     * PARK事件.
                                     */
                                    //先置为PARK状态
                                    bottomParked = true;
                                    if (callbackBottomPark()){
                                        //记录当前时间
                                        lastBottomParkTime = DateTimeUtils.getCurrentTimeMillis();
                                        //长时间没归位时, 自动归位
                                        myHandler.sendEmptyMessageDelayed(MyHandler.HANDLER_RESET_BOTTOM_PARK_AUTO, autoResetDelay);
                                    } else {
                                        //若回调返回false, 则解除PARK状态
                                        //之所以不在回调返回true时, 将PARK置为true, 是防止监听器执行时, 状态被重置为false, 回到这里被重置掉
                                        bottomParked = false;
                                    }
                                }
                            }
                        }
                        //弹回PARK位置
                        free(false);
                        return true;
                    case STATE_HOLD:
                    case STATE_HORIZONTAL_DRAG:
                    case STATE_RELEASE:
                    default:
                        if (scrollY != 0){
                            //弹回PARK位置
                            free(false);
                        }
                        return super.dispatchTouchEvent(ev);
                }
        }
        return true;
    }

    @Override
    public void computeScroll() {
        //计算scroller
        if (this.state == STATE_RELEASE && !scroller.isFinished()){
            scroller.computeScrollOffset();
            scrollToTarget(scroller.getCurrY());
        }

        //计算当前位置
        int currScrollY;
        if (Math.abs(scrollY) < 1){
            currScrollY = 0;
        } else {
            currScrollY = (int) scrollY;
        }

        //滚动控件(手势坐标系与scroll反方向)
        if (!disableContainerScroll) {
            scrollTo(0, -currScrollY);
        }

        //必须实现自刷新
        if (this.state == STATE_RELEASE && !scroller.isFinished())
            postInvalidate();
    }

    /*****************************************************************************88
     * 状态变化
     */

    /**
     * 重置状态
     */
    private void resetState() {
        //停止滚动
        scroller.abortAnimation();
        //拖动方向重置
        dragDirection = DRAG_DIRECTION_UNKNOWN;
    }

    private void stateToRelease() {
        this.state = STATE_RELEASE;
        callbackStateChanged();
    }

    private void stateToHorizontalDrag() {
        this.state = STATE_HORIZONTAL_DRAG;
        callbackStateChanged();
    }

    private void stateToBottomOverDrag() {
        this.state = STATE_BOTTOM_OVER_DRAG;
        callbackStateChanged();
    }

    private void stateToTopOverDrag() {
        this.state = STATE_TOP_OVER_DRAG;
        callbackStateChanged();
    }

    private void stateToHold() {
        this.state = STATE_HOLD;
        callbackStateChanged();
    }

    private void callbackStateChanged() {
        if (refreshIndicatorList != null){
            for (RefreshIndicator refreshIndicator : refreshIndicatorList){
                refreshIndicator.onStateChanged(this.state);
            }
        }
    }

    /**********************************************************************
     * 滚动
     */

    private void scrollByOffset(float offset){
        float result = scrollY + offset;
        if ((result > 0 && scrollY < 0) || (result < 0 && scrollY > 0)){
            //正反方向变换时, 必须先变为0
            scrollY = 0;
        }else{
            scrollY = result;
        }
        postInvalidate();
        callbackScroll();
    }

    private void scrollToTarget(float target){
        scrollY = target;
        postInvalidate();
        callbackScroll();
    }

    private void callbackScroll() {
        int currScrollY;
        if (Math.abs(scrollY) < 1){
            currScrollY = 0;
        } else {
            currScrollY = (int) scrollY;
        }

        if (refreshIndicatorList != null){
            for (RefreshIndicator refreshIndicator : refreshIndicatorList){
                refreshIndicator.onScroll(currScrollY);
            }
        }
    }

    /************************************************************************
     * PARK
     */

    private boolean callbackTopPark() {
        //当其中一个监听器返回true时, 返回true
        boolean result = false;
        if (refreshIndicatorList != null){
            for (RefreshIndicator refreshIndicator : refreshIndicatorList){
                if (refreshIndicator.onTopPark()){
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean callbackBottomPark() {
        //当其中一个监听器返回true时, 返回true
        boolean result = false;
        if (refreshIndicatorList != null){
            for (RefreshIndicator refreshIndicator : refreshIndicatorList){
                if (refreshIndicator.onBottomPark()){
                    result = true;
                }
            }
        }
        return result;
    }

    private void callbackTopParkAutoReset() {
        if (refreshIndicatorList != null){
            for (RefreshIndicator refreshIndicator : refreshIndicatorList){
                refreshIndicator.onTopParkAutoReset();
            }
        }
    }

    private void callbackBottomParkAutoReset() {
        if (refreshIndicatorList != null){
            for (RefreshIndicator refreshIndicator : refreshIndicatorList){
                refreshIndicator.onBottomParkAutoReset();
            }
        }
    }

    private void callbackTopParkIgnore() {
        if (refreshIndicatorList != null){
            for (RefreshIndicator refreshIndicator : refreshIndicatorList){
                refreshIndicator.onTopParkIgnore();
            }
        }
    }

    private void callbackBottomParkIgnore() {
        if (refreshIndicatorList != null){
            for (RefreshIndicator refreshIndicator : refreshIndicatorList){
                refreshIndicator.onBottomParkIgnore();
            }
        }
    }

    /*************************************************************************
     * public
     */

    /**
     * [重要]重置顶部PARK状态, 并将越界的容器控件弹回初始位置.
     * 在发生PARK事件后, 进行刷新流程, 刷新结束后, 必须调用该方法, 重置状态, 在重置状态前, 容器将不会再触发PARK事件.
     */
    public void resetTopPark(){
        //清除自动归位消息
        myHandler.removeMessages(MyHandler.HANDLER_RESET_TOP_PARK_AUTO);
        //归位
        myHandler.sendEmptyMessage(MyHandler.HANDLER_RESET_TOP_PARK);
    }

    /**
     * [重要]重置底部PARK状态, 并将越界的容器控件弹回初始位置.
     * 在发生PARK事件后, 进行刷新流程, 刷新结束后, 必须调用该方法, 重置状态, 在重置状态前, 容器将不会再触发PARK事件.
     */
    public void resetBottomPark(){
        //清除自动归位消息
        myHandler.removeMessages(MyHandler.HANDLER_RESET_BOTTOM_PARK_AUTO);
        //归位
        myHandler.sendEmptyMessage(MyHandler.HANDLER_RESET_BOTTOM_PARK);
    }

    /**
     * 越界拖动归位(不重置PARK状态)
     * @param forceToZero true:强制回到初始位置
     */
    void resetScrollOnly(boolean forceToZero){
        Message message = myHandler.obtainMessage(MyHandler.HANDLER_RESET_SCROLL_ONLY);
        message.obj = forceToZero;
        myHandler.sendMessage(message);
    }

    /**
     * 获取滚动子控件
     */
    protected View getScrollChild(){
        return getChildAt(getChildCount() - 1);
    }

    /**
     * 判断子控件是否达到顶部/底部(增加对View的支持, 可复写此方法实现)
     */
    protected ReachState checkReachState(View child) {
        boolean reachTop;
        boolean reachBottom;

        if (child instanceof ScrollView) {
            reachTop = ScrollViewUtils.reachTop((ScrollView) child);
            reachBottom = ScrollViewUtils.reachBottom((ScrollView) child);
        } else if (child instanceof ListView) {
            reachTop = ListViewUtils.reachTop((ListView) child);
            reachBottom = ListViewUtils.reachBottom((ListView) child);
        } else {
            throw new RuntimeException("[VerticalOverDragContainer]child view is not supported, view:" + child);
        }

        if (reachTop && reachBottom) {
            return ReachState.REACH_BOTH;
        } else if (reachTop) {
            return ReachState.REACH_TOP;
        } else if (reachBottom) {
            return ReachState.REACH_BOTTOM;
        }

        return ReachState.HALFWAY;
    }

    /**
     * 添加刷新效果指示器, 能够监听状态变化/越界滚动/PARK事件.
     * 用于下拉刷新/上拉加载效果, 先实现RefreshIndicator接口, 然后通过该方法添加监听.
     * @param refreshIndicator 刷新效果指示器
     */
    public void addRefreshIndicator(RefreshIndicator refreshIndicator){
        if (refreshIndicator == null){
            return;
        }
        if (refreshIndicatorList == null){
            refreshIndicatorList = new ArrayList<>();
        }
        refreshIndicator.setContainer(this);
        refreshIndicatorList.add(refreshIndicator);
    }

    /**
     * @param disableIfHorizontalDrag true:当发生横向手势, 禁止越界拖动, 常用与嵌套横向滑动的控件, 例如ViewPager
     */
    public void setDisableIfHorizontalDrag(boolean disableIfHorizontalDrag) {
        this.disableIfHorizontalDrag = disableIfHorizontalDrag;
    }

    /**
     * @param disableContainerScroll true:禁止容器自身的滚动(用于实现本身不越界滚动, 刷新指示器滚动的场合)
     */
    public void setDisableContainerScroll(boolean disableContainerScroll) {
        this.disableContainerScroll = disableContainerScroll;
    }

    /**
     * P.S.这个方法还可以被用来临时冻结容器的越界滚动(设置为0)
     * @param overDragResistance 0-1, 越界拖动阻尼, 值约小越界拖动越慢
     */
    public void setOverDragResistance(float overDragResistance) {
        if (overDragResistance < 0 || overDragResistance > 1){
            throw new RuntimeException("overDragResistance must >= 0 and <= 1");
        }
        this.overDragResistance = overDragResistance;
    }

    /**
     * @param scrollDuration ms, 越界弹回的时间
     */
    public void setScrollDuration(int scrollDuration) {
        if (scrollDuration < 0){
            throw new RuntimeException("scrollDuration must >= 0");
        }
        this.scrollDuration = scrollDuration;
    }

    /**
     * @param overDragThreshold >=0, 当越界拖动超过该设定值时, 会发生PARK时间, 滚动位置会停滞在设定值上, 用于实现下拉刷新上拉加载
     */
    public void setOverDragThreshold(int overDragThreshold) {
        if (overDragThreshold < 0){
            throw new RuntimeException("overDragThreshold must >= 0");
        }
        this.overDragThreshold = overDragThreshold;
    }

    /**
     * @param topParkEnabled true:允许顶部PARK
     */
    public void setTopParkEnabled(boolean topParkEnabled) {
        this.topParkEnabled = topParkEnabled;
    }

    /**
     * @param bottomParkEnabled true:允许底部PARK
     */
    public void setBottomParkEnabled(boolean bottomParkEnabled) {
        this.bottomParkEnabled = bottomParkEnabled;
    }

    public int getOverDragThreshold(){
        return overDragThreshold;
    }

    public int getScrollDuration(){
        return scrollDuration;
    }

    public float getOverDragResistance() {
        return overDragResistance;
    }

    public void setAutoResetDelay(long delay){
        if (delay <= 0){
            throw new RuntimeException("auto reset delay must > 0");
        }
        this.autoResetDelay = delay;
    }

    /**
     * @param interval 刷新最短间隔, 小于间隔时间触发多次PARK会被视为过于频繁, 会回调RefreshIndicator.onParkIgnore方法
     */
    public void setParkInterval(long interval){
        this.parkInterval = interval;
    }

    /*************************************************************************
     * private
     */

    /**
     * 回弹
     */
    private void free(boolean forceToZero){
        //当前位置
        int currScrollY = (int) scrollY;
        //计算弹回目标
        int target = 0;
        if (forceToZero || disableContainerScroll){
            //do nothing
        } else if (topParkEnabled && currScrollY > 0){
            if (topParked) {
                //当前是PARK状态
                if (currScrollY > overDragThreshold){
                    //弹回
                    target = overDragThreshold;
                }else{
                    //不动
                    target = currScrollY;
                }
            } else {
                //当前不是PARK状态
                if (currScrollY > overDragThreshold) {
                    //当前位置超过设定值
                    target = overDragThreshold;
                }
            }
        }else if (bottomParkEnabled && currScrollY < 0){
            if (bottomParked) {
                //当前是PARK状态
                if(currScrollY < -overDragThreshold){
                    //弹回
                    target = -overDragThreshold;
                }else{
                    //不动
                    target = currScrollY;
                }
            } else {
                //当前不是PARK状态
                if (currScrollY < -overDragThreshold) {
                    //当前位置超过设定值
                    target = -overDragThreshold;
                }
            }
        }

        //过滤
        if (currScrollY == target){
            return;
        }
        //回弹
        scroller.startScroll(0, currScrollY, 0, target - currScrollY, scrollDuration);
        //刷新
        postInvalidate();
    }

    /**
     * 计算Y方向上的位移
     */
    protected float calculateMoveDistance(MotionEvent event) {
        //触摸事件结束后, 重置状态
        if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            lastPointId = -1;
            return 0;
        }
        //获得第一个触点的ID
        int pointId = event.getPointerId(0);
        if (pointId == lastPointId) {
            //若第一个触点ID未变, 则计算点位移
            float distance = event.getY() - lastY;
            this.lastY = event.getY();
            return distance;
        } else {
            //若第一个触点ID变化, 则记录新的ID, 本次视为没有移动
            this.lastPointId = pointId;
            this.lastY = event.getY();
            return 0;
        }
    }

    /*********************************************************************************
     * interface / enum
     */

    protected enum ReachState {

        HALFWAY(false, false),
        REACH_TOP(true, false),
        REACH_BOTTOM(false, true),
        REACH_BOTH(true, true);

        private boolean reachTop;
        private boolean reachBottom;

        ReachState(boolean reachTop, boolean reachBottom) {
            this.reachTop = reachTop;
            this.reachBottom = reachBottom;
        }

        protected boolean reachTop() {
            return reachTop;
        }

        protected boolean reachBottom() {
            return reachBottom;
        }

    }

    /**
     * 下拉刷新上拉加载效果控件接口
     */
    public interface RefreshIndicator {

        /**
         * private static final int STATE_RELEASE = 0;
         * private static final int STATE_HOLD = 1;
         * private static final int STATE_TOP_OVER_DRAG = 2;
         * private static final int STATE_BOTTOM_OVER_DRAG = 3;
         * private static final int STATE_HORIZONTAL_DRAG = 4;
         */
        void onStateChanged(int state);

        /**
         * @param scrollY Y方向越界拖动位置, +:顶部越界拖动, -:底部越界拖动
         */
        void onScroll(int scrollY);

        /**
         * 顶部PARK事件
         *
         * @return (1) 返回true时, 表明接受了该事件, 容器进入顶部PARK状态, 并阻断后续触发的顶部PARK事件(不管怎么
         *          拖动都不会再触发顶部PARK事件), 直到调用{@link VerticalOverDragContainer#resetTopPark()}
         *          方法解除PARK状态并弹回. 例如:监听器中开始刷新流程, 返回true, 等待刷新流程结束后, 调用
         *          {@link VerticalOverDragContainer#resetTopPark()}方法, 容器会弹回初始状态, 并开始接受
         *          下一个PARK事件.
         *          (2) 返回false时, 表明监听器不处理该事件, 容器不进入PARK状态, 无需调用
         *          {@link VerticalOverDragContainer#resetTopPark()}方法重置, 容器会继续响应接下来的顶部
         *          PARK事件.
         */
        boolean onTopPark();

        /**
         * 底部PARK事件
         *
         * @return (1) 返回true时, 表明接受了该事件, 容器进入底部PARK状态, 并阻断后续触发的底部PARK事件(不管怎么
         *          拖动都不会再触发底部PARK事件), 直到调用{@link VerticalOverDragContainer#resetBottomPark()}
         *          方法解除PARK状态并弹回. 例如:监听器中开始加载流程, 返回true, 等待加载流程结束后, 调用
         *          {@link VerticalOverDragContainer#resetBottomPark()}方法, 容器会弹回初始状态, 并开始接受
         *          下一个PARK事件.
         *          (2) 返回false时, 表明监听器不处理该事件, 容器不进入PARK状态, 无需调用
         *          {@link VerticalOverDragContainer#resetBottomPark()}方法重置, 容器会继续响应接下来的底部
         *          PARK事件.
         */
        boolean onBottomPark();

        /**
         * 若触发顶部PARK事件后, 长时间没有重置状态, 容器会自动重置, 并回调指示器的这个方法通知
         */
        void onTopParkAutoReset();

        /**
         * 若触发底部PARK事件后, 长时间没有重置状态, 容器会自动重置, 并回调指示器的这个方法通知
         */
        void onBottomParkAutoReset();

        /**
         * 在设定的最小间隔时间内, 触发多次顶部PARK时, 会回调此方法
         */
        void onTopParkIgnore();

        /**
         * 在设定的最小间隔时间内, 触发多次底部PARK时, 会回调此方法
         */
        void onBottomParkIgnore();

        void setContainer(VerticalOverDragContainer container);

    }

    /***********************************************************************************
     * handler
     */

    private MyHandler myHandler = new MyHandler(Looper.getMainLooper(), this);

    private static class MyHandler extends WeakHandler<VerticalOverDragContainer> {

        private static final int HANDLER_RESET_TOP_PARK = 0;
        private static final int HANDLER_RESET_BOTTOM_PARK = 1;
        private static final int HANDLER_RESET_TOP_PARK_AUTO = 2;
        private static final int HANDLER_RESET_BOTTOM_PARK_AUTO = 3;
        private static final int HANDLER_RESET_SCROLL_ONLY = 4;

        public MyHandler(Looper looper, VerticalOverDragContainer host) {
            super(looper, host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, VerticalOverDragContainer host) {
            switch (msg.what){
                case HANDLER_RESET_TOP_PARK_AUTO://顶部长时间未归位, 自动归位
                    host.callbackTopParkAutoReset();
                case HANDLER_RESET_TOP_PARK://顶部PARK归位
                    host.topParked = false;
                    host.free(true);
                    break;
                case HANDLER_RESET_BOTTOM_PARK_AUTO://底部长时间未归位, 自动归位
                    host.callbackBottomParkAutoReset();
                case HANDLER_RESET_BOTTOM_PARK://底部PARK归位
                    host.bottomParked = false;
                    host.free(true);
                    break;
                case HANDLER_RESET_SCROLL_ONLY:
                    host.free(msg.obj == (Boolean)true);
                    break;
                default:
                    break;
            }
        }

    }

    /*****************************************************************************
     * Emulate motion event executor
     */

    private MotionEventUtils.EmulateMotionEventExecutor emulateMotionEventExecutor = new MotionEventUtils.EmulateMotionEventExecutor() {
        @Override
        public void dispatchTouchEvent(MotionEvent emulateMotionEvent) {
            //模拟出来的事件是屏幕坐标系的, 需要根据当前ViewGroup修正坐标
            MotionEventUtils.offsetLocation(emulateMotionEvent, VerticalOverDragContainer.this);
            //分发事件
            VerticalOverDragContainer.super.dispatchTouchEvent(emulateMotionEvent);
        }
    };

}
