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

package sviolet.turquoise.ui.viewgroup.gesture;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import sviolet.turquoise.common.compat.CompatOverScroller;
import sviolet.turquoise.ui.util.ListViewUtils;
import sviolet.turquoise.ui.util.ScrollViewUtils;
import sviolet.turquoise.ui.util.ViewCommonUtils;
import sviolet.turquoise.util.droid.MotionEventUtils;

/**
 * Created by S.Violet on 2016/11/3.
 */

public class VerticalOverDragContainer extends RelativeLayout {

    private static final int STATE_RELEASE = 0;
    private static final int STATE_HOLD = 1;
    private static final int STATE_TOP_OVER_DRAG = 2;
    private static final int STATE_BOTTOM_OVER_DRAG = 3;
    private static final int STATE_HORIZONTAL_DRAG = 4;

    private static final int DRAG_DIRECTION_HORIZONTAL = -1;
    private static final int DRAG_DIRECTION_UNKNOWN = 0;
    private static final int DRAG_DIRECTION_VERTICAL = 1;

    //////////////////////////////////////////////////////

    private boolean disableIfHorizontalDrag = true;

    private float overDragResistance = 0.4f;
    private int scrollDuration = 300;

    private int overDragThreshold = 300;

    private boolean topParkEnabled = true;
    private boolean bottomParkEnabled = true;

    //////////////////////////////////////////////////////

    private int mTouchSlop;

    private int state = STATE_RELEASE;
    private int dragDirection = DRAG_DIRECTION_UNKNOWN;

    private float scrollY;

    private float downX;
    private float downY;
    private float lastY;
    private int lastPointId = -1;

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
    }

    public VerticalOverDragContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        this.mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.scroller = new CompatOverScroller(getContext());

        ViewCommonUtils.setInitListener(this, new ViewCommonUtils.InitListener() {
            @Override
            public void onInit() {

            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        System.out.println("container ev:" + ev);//TODO
        System.out.println("container last scroll y:" + scrollY);//TODO

        //没有子控件时直接分发事件
        int childCount = getChildCount();
        if (childCount < 1) {
            return super.dispatchTouchEvent(ev);
        }

        //取滚动的子控件
        View child = getScrollChild();
        //子控件是否在顶部或底部
        ReachState reachState = checkReachState(child);

        System.out.println("container reach state:" + reachState);//TODO

        //当前坐标
        float currX = ev.getX();
        float currY = ev.getY();

        //Y方向位移量
        float distanceY = calculateMoveDistance(ev);

        System.out.println("container distanceY:" + distanceY);//TODO

        switch (ev.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                //重置状态
                resetState();
                //记录坐标
                this.downX = currX;
                this.downY = currY;
                this.lastY = currY;

                if (scrollY == 0) {
                    System.out.println("container down_hold");//TODO
                    //没有越界, 普通持有模式
                    stateToHold();
                    //事件分发给子控件处理
                    return super.dispatchTouchEvent(ev);
                } else if (scrollY > 0){
                    System.out.println("container down_to_top");//TODO
                    //上方越界
                    stateToTopOverDrag();
                    return true;
                } else {
                    System.out.println("container down_to_bottom");//TODO
                    //下方越界
                    stateToBottomOverDrag();
                    return true;
                }
            case MotionEvent.ACTION_POINTER_DOWN:
                System.out.println("container point_down");//TODO
                if (this.state == STATE_HOLD || this.state == STATE_HORIZONTAL_DRAG){
                    return super.dispatchTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                switch (this.state){
                    case STATE_HOLD:
                        System.out.println("container move_hold");//TODO
                        /**
                         * 判定为横向滑动时, 禁止拦截事件, 可配置
                         */
                        if (disableIfHorizontalDrag && dragDirection == DRAG_DIRECTION_UNKNOWN){
                            if (Math.abs(currY - downY) > mTouchSlop){
                                dragDirection = DRAG_DIRECTION_VERTICAL;
                            } else if (Math.abs(currX - downX) > mTouchSlop) {
                                System.out.println("container move_hold horizontal!!");//TODO
                                dragDirection = DRAG_DIRECTION_HORIZONTAL;
                                stateToHorizontalDrag();
                                return super.dispatchTouchEvent(ev);
                            }
                        }
                        //判断状态
                        if (reachState.reachTop() && distanceY > mTouchSlop){
                            /**
                             * 子控件到达顶部, 且继续向下拉动
                             */
                            System.out.println("container move_hold upup");//TODO
                            //上边越界
                            stateToTopOverDrag();
                            //模拟取消事件给子控件
                            emulateCancelEvent(ev);
                            //越界阻尼
                            if (scrollY >= 0 && distanceY > 0){
                                distanceY = distanceY * overDragResistance;
                            }
                            //越界滚动
                            scrollByOffset(distanceY);
                            System.out.println("container move_hold scroll to:" + scrollY);//TODO
                            return true;
                        } else if (reachState.reachBottom() && distanceY < -mTouchSlop){
                            /**
                             * 子控件到达底部, 且继续向上拉动
                             */
                            System.out.println("container move_hold downdown");//TODO
                            //下边越界
                            stateToBottomOverDrag();
                            //模拟取消事件给子控件
                            emulateCancelEvent(ev);
                            //越界阻尼
                            if (scrollY <= 0 && distanceY < 0){
                                distanceY = distanceY * overDragResistance;
                            }
                            //越界滚动
                            scrollByOffset(distanceY);
                            System.out.println("container move_hold scroll to:" + scrollY);//TODO
                            return true;
                        } else {
                            /**
                             * 子控件未到达顶部或底部
                             */
                            System.out.println("container move_hold nono");//TODO
                            //无越界, 直接分发事件给子控件
                            return super.dispatchTouchEvent(ev);
                        }
                    case STATE_TOP_OVER_DRAG:
                        System.out.println("container move_top_drag");//TODO
                        if (scrollY + distanceY <= 0){
                            //上边越界结束
                            if (reachState.reachBottom()){
                                System.out.println("container move_top_drag top to bottom");//TODO
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
                            emulateDownEvent(ev);
                            //分发事件给子控件
                            return super.dispatchTouchEvent(ev);
                        }
                        //越界阻尼
                        if (scrollY >= 0 && distanceY > 0){
                            distanceY = distanceY * overDragResistance;
                        }
                        //越界滚动
                        scrollByOffset(distanceY);
                        System.out.println("container move_top_drag scroll to:" + scrollY);//TODO
                        return true;
                    case STATE_BOTTOM_OVER_DRAG:
                        System.out.println("container move_bottom_drag");//TODO
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
                            emulateDownEvent(ev);
                            //分发事件给子控件
                            return super.dispatchTouchEvent(ev);
                        }
                        //越界阻尼
                        if (scrollY <= 0 && distanceY < 0){
                            distanceY = distanceY * overDragResistance;
                        }
                        //越界滚动
                        scrollByOffset(distanceY);
                        return true;
                    case STATE_HORIZONTAL_DRAG:
                        System.out.println("container move_HORIZONTAL_drag");//TODO
                        //横向拖动模式直接分发给子控件
                        return super.dispatchTouchEvent(ev);
                }
                System.out.println("container move_other!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");//TODO
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                System.out.println("container point_up");//TODO
                if (this.state == STATE_HOLD || this.state == STATE_HORIZONTAL_DRAG){
                    return super.dispatchTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                System.out.println("container cancel");//TODO
                //TODO 被外层拦截事件时, 将各种状态归位
            case MotionEvent.ACTION_UP:
                int lastState = this.state;
                stateToRelease();

                switch (lastState){
                    case STATE_TOP_OVER_DRAG:
                        if (topParkEnabled && scrollY > overDragThreshold){
                            //TODO 刷新
                            System.out.println("refresh");//TODO
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    free();
                                }
                            }, 1000);
                        }
                        //弹回
                        free();
                        return true;
                    case STATE_BOTTOM_OVER_DRAG:
                        if (bottomParkEnabled && scrollY < -overDragThreshold){
                            //TODO 加载
                            System.out.println("load");//TODO
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    free();
                                }
                            }, 1000);
                        }
                        free();
                        return true;
                    case STATE_HOLD:
                    case STATE_HORIZONTAL_DRAG:
                    case STATE_RELEASE:
                    default:
                        System.out.println("container up hold");//TODO
                        return super.dispatchTouchEvent(ev);
                }
        }
        System.out.println("container other!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");//TODO
        return true;
    }

    private void stateToRelease() {
        this.state = STATE_RELEASE;
    }

    private void stateToHorizontalDrag() {
        this.state = STATE_HORIZONTAL_DRAG;
    }

    private void stateToBottomOverDrag() {
        this.state = STATE_BOTTOM_OVER_DRAG;
    }

    private void stateToTopOverDrag() {
        this.state = STATE_TOP_OVER_DRAG;
    }

    private void stateToHold() {
        this.state = STATE_HOLD;
    }

    private void scrollByOffset(float offset){
        scrollY += offset;
        postInvalidate();
    }

    private void scrollToTarget(float target){
        scrollY = target;
        postInvalidate();
    }

    /**
     * 重置状态
     */
    private void resetState() {
        //停止滚动
        scroller.abortAnimation();
        //拖动方向重置
        dragDirection = DRAG_DIRECTION_UNKNOWN;
    }

    private void free(){
        //当前位置
        int currScrollY = (int) scrollY;
        //计算弹回目标
        int target = 0;
        if (topParkEnabled && currScrollY > overDragThreshold){
            target = overDragThreshold;
        }else if (bottomParkEnabled && currScrollY < -overDragThreshold){
            target = -overDragThreshold;
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

    private void emulateCancelEvent(MotionEvent ev){
        touchPoints.setCapacity(ev.getPointerCount());
        for (int i = 0 ; i < touchPoints.getCapacity() ; i++){
            touchPoints.setX(i, ev.getX(i));
            touchPoints.setY(i, ev.getY(i));
            touchPoints.setId(i, ev.getPointerId(i));
        }
        MotionEvent emuEvent = MotionEventUtils.obtain(MotionEvent.ACTION_CANCEL, touchPoints, ev.getDownTime());
        super.dispatchTouchEvent(emuEvent);
    }

    private void emulateDownEvent(MotionEvent ev){
        //TODO 简易处理, 后续改成吧多个点变成多个POINTER_DOWN时间, 模拟的更逼真
        touchPoints.setCapacity(ev.getPointerCount());
        for (int i = 0 ; i < touchPoints.getCapacity() ; i++){
            touchPoints.setX(i, ev.getX(i));
            touchPoints.setY(i, ev.getY(i));
            touchPoints.setId(i, ev.getPointerId(i));
        }
        MotionEvent emuEvent = MotionEventUtils.obtain(MotionEvent.ACTION_DOWN, touchPoints, ev.getDownTime());
        super.dispatchTouchEvent(emuEvent);
    }

    @Override
    public void computeScroll() {
        //计算scroller
        if (this.state == STATE_RELEASE && !scroller.isFinished()){
            scroller.computeScrollOffset();
            scrollToTarget(scroller.getCurrY());
        }

        //计算当前位置
        int _scrollY;
        if (Math.abs(scrollY) < 1){
            _scrollY = 0;
        } else {
            _scrollY = (int) -scrollY;
        }

        //滚动控件
        scrollTo(0, _scrollY);

        //必须实现自刷新
        if (this.state == STATE_RELEASE && !scroller.isFinished())
            postInvalidate();
    }

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

    protected View getScrollChild(){
        return getChildAt(getChildCount() - 1);
    }

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

}
