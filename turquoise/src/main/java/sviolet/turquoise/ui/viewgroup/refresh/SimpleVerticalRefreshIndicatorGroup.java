/*
 * Copyright (C) 2015-2017 S.Violet
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
import android.view.View;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;

import sviolet.turquoise.R;
import sviolet.turquoise.enhance.common.WeakHandler;
import sviolet.turquoise.ui.util.ViewCommonUtils;

/**
 * <p>配合{@link VerticalOverDragContainer}实现下拉刷新/上拉加载</p>
 *
 * <p>具体用法参考demoa中的RefreshIndicatorRefreshActivity.java</p>
 *
 * Created by S.Violet on 2016/11/8.
 */

public class SimpleVerticalRefreshIndicatorGroup extends RelativeLayout implements VerticalOverDragContainer.RefreshIndicator {

    public static final int TYPE_TOP_STATIC = 0;//顶部固定位置
    public static final int TYPE_BOTTOM_STATIC = 1;//底部固定位置
    public static final int TYPE_IN_FROM_TOP = 2;//从顶部滚动出现
    public static final int TYPE_IN_FROM_BOTTOM = 3;//从底部滚动出现

    private static final int STATE_INIT = 0;//初始状态
    private static final int STATE_READY = 1;//松开刷新状态
    private static final int STATE_REFRESHING = 2;//刷新状态
    private static final int STATE_SUCCEED = 3;//刷新成功
    private static final int STATE_FAILED = 4;//刷新失败

    //////////////////////////////////////////////////////////////////

    private int type = TYPE_TOP_STATIC;//类型
    private long resultDuration = 500;//显示刷新结果(成功/失败)时间, ms

    private int initViewIndex;//初始状态布局序号(在父控件中的序号, 0~childCount)
    private int readyViewIndex;//松开刷新状态布局序号(在父控件中的序号, 0~childCount)
    private int refreshingViewIndex;//刷新状态布局序号(在父控件中的序号, 0~childCount)
    private int succeedViewIndex;//成功状态布局序号(在父控件中的序号, 0~childCount)
    private int failedViewIndex;//失败状态布局序号(在父控件中的序号, 0~childCount)

    private RefreshListener refreshListener;//刷新监听器

    //////////////////////////////////////////////////////////////////

    private int scrollY = 0;
    private int state = STATE_INIT;

    private View viewInit;
    private View viewReady;
    private View viewRefreshing;
    private View viewSucceed;
    private View viewFailed;

    private WeakReference<VerticalOverDragContainer> container;

    public SimpleVerticalRefreshIndicatorGroup(Context context) {
        super(context);
        init();
    }

    public SimpleVerticalRefreshIndicatorGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initSetting(context, attrs);
    }

    public SimpleVerticalRefreshIndicatorGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initSetting(context, attrs);
    }

    private void init(){
        ViewCommonUtils.setInitListener(this, new ViewCommonUtils.InitListener() {
            @Override
            public void onInit() {
                //根据各种效果的序号, 从子控件中获取
                if (initViewIndex >= 0) {
                    viewInit = getChildAt(initViewIndex);
                    viewInit.setVisibility(View.VISIBLE);
                }
                if (readyViewIndex >= 0) {
                    viewReady = getChildAt(readyViewIndex);
                    viewReady.setVisibility(View.GONE);
                }
                if (refreshingViewIndex >= 0) {
                    viewRefreshing = getChildAt(refreshingViewIndex);
                    viewRefreshing.setVisibility(View.GONE);
                }
                if (succeedViewIndex >= 0) {
                    viewSucceed = getChildAt(succeedViewIndex);
                    viewSucceed.setVisibility(View.GONE);
                }
                if (failedViewIndex >= 0) {
                    viewFailed = getChildAt(failedViewIndex);
                    viewFailed.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 初始化配置
     */
    private void initSetting(final Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleVerticalRefreshIndicatorGroup);
        setType(typedArray.getInt(R.styleable.SimpleVerticalRefreshIndicatorGroup_SimpleVerticalRefreshIndicatorGroup_type, TYPE_TOP_STATIC));
        setResultDuration(typedArray.getInt(R.styleable.SimpleVerticalRefreshIndicatorGroup_SimpleVerticalRefreshIndicatorGroup_resultDuration, 500));
        initViewIndex = typedArray.getInt(R.styleable.SimpleVerticalRefreshIndicatorGroup_SimpleVerticalRefreshIndicatorGroup_initViewIndex, -1);
        readyViewIndex = typedArray.getInt(R.styleable.SimpleVerticalRefreshIndicatorGroup_SimpleVerticalRefreshIndicatorGroup_readyViewIndex, -1);
        refreshingViewIndex = typedArray.getInt(R.styleable.SimpleVerticalRefreshIndicatorGroup_SimpleVerticalRefreshIndicatorGroup_refreshingViewIndex, -1);
        succeedViewIndex = typedArray.getInt(R.styleable.SimpleVerticalRefreshIndicatorGroup_SimpleVerticalRefreshIndicatorGroup_succeedViewIndex, -1);
        failedViewIndex = typedArray.getInt(R.styleable.SimpleVerticalRefreshIndicatorGroup_SimpleVerticalRefreshIndicatorGroup_failedViewIndex, -1);
        typedArray.recycle();
    }

    @Override
    public void setContainer(VerticalOverDragContainer container) {
        this.container = new WeakReference<>(container);
    }

    @Override
    public void onTopParkAutoReset() {
        if (type == TYPE_TOP_STATIC || type == TYPE_IN_FROM_TOP){
            //容器自动重置时, 重置指示器状态
            reset(false);
        }
    }

    @Override
    public void onBottomParkAutoReset() {
        if (type == TYPE_BOTTOM_STATIC || type == TYPE_IN_FROM_BOTTOM){
            //容器自动重置时, 重置指示器状态
            reset(false);
        }
    }

    @Override
    public void onTopParkIgnore() {
        if (type == TYPE_TOP_STATIC || type == TYPE_IN_FROM_TOP){
            if(refreshListener != null){
                refreshListener.onIgnore();
            }
        }
    }

    @Override
    public void onBottomParkIgnore() {
        if (type == TYPE_BOTTOM_STATIC || type == TYPE_IN_FROM_BOTTOM){
            if(refreshListener != null){
                refreshListener.onIgnore();
            }
        }
    }

    @Override
    public void onStateChanged(int state) {

    }

    @Override
    public void onScroll(int scrollY) {
        //更新位置
        this.scrollY = scrollY;

        if(this.state == STATE_INIT){
            //init状态变ready
            if(Math.abs(scrollY) >= getContainerOverDragThreshold()){
                stateToReady();
            }
        }else if(this.state == STATE_READY){
            //ready状态变init
            if(Math.abs(scrollY) < getContainerOverDragThreshold()){
                stateToInit();
            }
        }

        postInvalidate();
    }

    @Override
    public boolean onTopPark() {
        //顶部固定或顶部进入模式时接受顶部PARK事件
        if (type == TYPE_TOP_STATIC || type == TYPE_IN_FROM_TOP){
            //非refreshing状态才能进入refreshing状态
            if (state != STATE_REFRESHING) {
                stateToRefreshing();
                if(refreshListener != null){
                    refreshListener.onRefresh();
                }
                //有效地处理了PARK事件时, 返回true, 使得容器进入PARK状态
                return true;
            }
        }
        //未能有效处理PARK事件时, 返回false, 防止容器意外进入PARK状态
        return false;
    }

    @Override
    public boolean onBottomPark() {
        //底部固定或底部进入模式时接受底部PARK事件
        if (type == TYPE_BOTTOM_STATIC || type == TYPE_IN_FROM_BOTTOM){
            //非refreshing状态才能进入refreshing状态
            if (state != STATE_REFRESHING) {
                stateToRefreshing();
                if(refreshListener != null){
                    refreshListener.onRefresh();
                }
                //有效地处理了PARK事件时, 返回true, 使得容器进入PARK状态
                return true;
            }
        }
        //未能有效处理PARK事件时, 返回false, 防止容器意外进入PARK状态
        return false;
    }

    @Override
    public void computeScroll() {
        switch (type){
            case TYPE_IN_FROM_TOP://顶部进入模式
                //手势坐标系与scroll反方向
                scrollTo(0, - scrollY + getContainerOverDragThreshold());
                break;
            case TYPE_IN_FROM_BOTTOM://底部进入模式
                //手势坐标系与scroll反方向
                scrollTo(0, - scrollY - getContainerOverDragThreshold());
                break;
            default://固定模式不滚动
                scrollTo(0, 0);
                break;
        }
    }

    /*******************************************************************************
     * state change
     */

    protected void stateToInit(){
        state = STATE_INIT;
        //隐藏
        if (viewReady != null) {
            viewReady.setVisibility(View.GONE);
        }
        if (viewRefreshing != null) {
            viewRefreshing.setVisibility(View.GONE);
        }
        if (viewSucceed != null) {
            viewSucceed.setVisibility(View.GONE);
        }
        if (viewFailed != null) {
            viewFailed.setVisibility(View.GONE);
        }
        //显示
        if (viewInit != null){
            viewInit.setVisibility(View.VISIBLE);
        }

        postInvalidate();
    }

    protected void stateToReady(){
        state = STATE_READY;
        //隐藏
        if (viewInit != null) {
            viewInit.setVisibility(View.GONE);
        }
        if (viewRefreshing != null) {
            viewRefreshing.setVisibility(View.GONE);
        }
        if (viewSucceed != null) {
            viewSucceed.setVisibility(View.GONE);
        }
        if (viewFailed != null) {
            viewFailed.setVisibility(View.GONE);
        }
        //显示
        if (viewReady != null){
            viewReady.setVisibility(View.VISIBLE);
        } else if (viewInit != null){
            viewInit.setVisibility(View.VISIBLE);
        }

        postInvalidate();
    }

    protected void stateToRefreshing(){
        state = STATE_REFRESHING;
        //隐藏
        if (viewInit != null) {
            viewInit.setVisibility(View.GONE);
        }
        if (viewReady != null) {
            viewReady.setVisibility(View.GONE);
        }
        if (viewSucceed != null) {
            viewSucceed.setVisibility(View.GONE);
        }
        if (viewFailed != null) {
            viewFailed.setVisibility(View.GONE);
        }
        //显示
        if (viewRefreshing != null){
            viewRefreshing.setVisibility(View.VISIBLE);
        } else if (viewInit != null){
            viewInit.setVisibility(View.VISIBLE);
        }

        postInvalidate();
    }

    protected void stateToSucceed(){
        state = STATE_SUCCEED;
        //隐藏
        if (viewInit != null) {
            viewInit.setVisibility(View.GONE);
        }
        if (viewReady != null) {
            viewReady.setVisibility(View.GONE);
        }
        if (viewRefreshing != null) {
            viewRefreshing.setVisibility(View.GONE);
        }
        if (viewFailed != null) {
            viewFailed.setVisibility(View.GONE);
        }
        //显示
        if (viewSucceed != null){
            viewSucceed.setVisibility(View.VISIBLE);
        } else if (viewInit != null){
            viewInit.setVisibility(View.VISIBLE);
        }

        postInvalidate();
    }

    protected void stateToFailed(){
        state = STATE_FAILED;
        //隐藏
        if (viewInit != null) {
            viewInit.setVisibility(View.GONE);
        }
        if (viewReady != null) {
            viewReady.setVisibility(View.GONE);
        }
        if (viewRefreshing != null) {
            viewRefreshing.setVisibility(View.GONE);
        }
        if (viewSucceed != null) {
            viewSucceed.setVisibility(View.GONE);
        }
        //显示
        if (viewFailed != null){
            viewFailed.setVisibility(View.VISIBLE);
        } else if (viewInit != null){
            viewInit.setVisibility(View.VISIBLE);
        }

        postInvalidate();
    }

    /***********************************************************88
     * protected
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
     * 重置VerticalOverDragContainer的PARK状态
     */
    protected void resetContainerPark() {
        VerticalOverDragContainer container = getContainer();
        if (container != null) {
            if (type == TYPE_TOP_STATIC || type == TYPE_IN_FROM_TOP){
                container.resetTopPark();
            } else {
                container.resetBottomPark();
            }
        }
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

    /***********************************************************************
     * public
     */

    /**
     * 重置刷新状态, 在刷新流程结束后务必调用该方法. 本控件在触发刷新事件后, 会保持刷新中的状态, 直到
     * 调用该方法返回刷新结果, 并结束刷新状态.
     * @param succeed true:刷新成功 false:刷新失败
     */
    public void reset(boolean succeed) {
        if (state == STATE_REFRESHING){
            if (succeed){
                stateToSucceed();
            }else{
                stateToFailed();
            }
            myHandler.sendEmptyMessageDelayed(MyHandler.HANDLER_RESET_PARK, resultDuration);
        }
    }

    /**
     * @param type 设置类型
     */
    public void setType(int type){
        if (type < TYPE_TOP_STATIC || type > TYPE_IN_FROM_BOTTOM){
            throw new RuntimeException("invalid type:" + type);
        }
        this.type = type;
    }

    /**
     * @param resultDuration 设置结果状态显示时间
     */
    public void setResultDuration(long resultDuration){
        if (resultDuration < 0){
            throw new RuntimeException("resultDuration >= 0");
        }
        this.resultDuration = resultDuration;
    }

    /**
     * @param listener 设置刷新监听器
     */
    public void setRefreshListener(RefreshListener listener){
        this.refreshListener = listener;
    }

    /**********************************************************************************
     * class
     */

    public interface RefreshListener{

        void onRefresh();

        /**
         * 刷新过于频繁
         */
        void onIgnore();

    }

    /***********************************************************************************
     * handler
     */

    private MyHandler myHandler = new MyHandler(Looper.getMainLooper(), this);

    private static class MyHandler extends WeakHandler<SimpleVerticalRefreshIndicatorGroup>{

        private static final int HANDLER_RESET_PARK = 0;
        private static final int HANDLER_STATE_TO_INIT = 1;

        public MyHandler(Looper looper, SimpleVerticalRefreshIndicatorGroup host) {
            super(looper, host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, SimpleVerticalRefreshIndicatorGroup host) {
            switch (msg.what){
                case HANDLER_RESET_PARK:
                    host.resetContainerPark();
                    //等弹回后再改为INIT状态
                    sendEmptyMessageDelayed(HANDLER_STATE_TO_INIT, host.getContainerScrollDuration());
                    break;
                case HANDLER_STATE_TO_INIT:
                    //只能从成功失败状态变为初始状态
                    if(host.state == STATE_SUCCEED || host.state == STATE_FAILED) {
                        host.stateToInit();
                    }
                    break;
                default:
                    break;
            }
        }

    }

}
