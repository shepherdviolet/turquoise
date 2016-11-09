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
 * Created by S.Violet on 2016/11/8.
 */

public class SimpleVerticalOverDragRefreshView extends RelativeLayout implements VerticalOverDragContainer.RefreshView {

    public static final int TYPE_TOP_STATIC = 0;
    public static final int TYPE_BOTTOM_STATIC = 1;
    public static final int TYPE_IN_FROM_TOP = 2;
    public static final int TYPE_IN_FROM_BOTTOM = 3;

    private static final int STATE_INIT = 0;
    private static final int STATE_READY = 1;
    private static final int STATE_REFRESHING = 2;
    private static final int STATE_SUCCEED = 3;
    private static final int STATE_FAILED = 4;

    //////////////////////////////////////////////////////////////////

    private int type = TYPE_TOP_STATIC;
    private long resultDuration = 500;

    private int initViewIndex;
    private int readyViewIndex;
    private int refreshingViewIndex;
    private int succeedViewIndex;
    private int failedViewIndex;

    private RefreshListener refreshListener;

    //////////////////////////////////////////////////////////////////

    private int scrollY = 0;
    private int state = STATE_INIT;

    private View viewInit;
    private View viewReady;
    private View viewRefreshing;
    private View viewSucceed;
    private View viewFailed;

    private WeakReference<VerticalOverDragContainer> container;

    public SimpleVerticalOverDragRefreshView(Context context) {
        super(context);
        init();
    }

    public SimpleVerticalOverDragRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initSetting(context, attrs);
    }

    public SimpleVerticalOverDragRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initSetting(context, attrs);
    }

    private void init(){
        ViewCommonUtils.setInitListener(this, new ViewCommonUtils.InitListener() {
            @Override
            public void onInit() {
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
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SimpleVerticalOverDragRefreshView);
        type = typedArray.getInt(R.styleable.SimpleVerticalOverDragRefreshView_SimpleVerticalOverDragRefreshView_type, TYPE_TOP_STATIC);
        initViewIndex = typedArray.getInt(R.styleable.SimpleVerticalOverDragRefreshView_SimpleVerticalOverDragRefreshView_initViewIndex, -1);
        readyViewIndex = typedArray.getInt(R.styleable.SimpleVerticalOverDragRefreshView_SimpleVerticalOverDragRefreshView_readyViewIndex, -1);
        refreshingViewIndex = typedArray.getInt(R.styleable.SimpleVerticalOverDragRefreshView_SimpleVerticalOverDragRefreshView_refreshingViewIndex, -1);
        succeedViewIndex = typedArray.getInt(R.styleable.SimpleVerticalOverDragRefreshView_SimpleVerticalOverDragRefreshView_succeedViewIndex, -1);
        failedViewIndex = typedArray.getInt(R.styleable.SimpleVerticalOverDragRefreshView_SimpleVerticalOverDragRefreshView_failedViewIndex, -1);
        typedArray.recycle();
    }

    @Override
    public void setContainer(VerticalOverDragContainer container) {
        this.container = new WeakReference<>(container);
    }

    @Override
    public void onTopPark() {
        if (type == TYPE_TOP_STATIC || type == TYPE_IN_FROM_TOP){
            if (state == STATE_INIT || state == STATE_READY) {
                stateToRefreshing();
                if(refreshListener != null){
                    refreshListener.onRefresh();
                }
            }
        }
    }

    @Override
    public void onBottomPark() {
        if (type == TYPE_BOTTOM_STATIC || type == TYPE_IN_FROM_BOTTOM){
            if (state == STATE_INIT || state == STATE_READY) {
                stateToRefreshing();
                if(refreshListener != null){
                    refreshListener.onRefresh();
                }
            }
        }
    }

    @Override
    public void onScroll(int state, int scrollY) {
        this.scrollY = scrollY;
        System.out.println(scrollY);

        if(this.state == STATE_INIT){
            if(Math.abs(scrollY) >= getOverDragThreshold()){
                stateToReady();
            }
        }else if(this.state == STATE_READY){
            if(Math.abs(scrollY) < getOverDragThreshold()){
                stateToInit();
            }
        }

        postInvalidate();
    }

    @Override
    public void onStateChanged(int state) {

    }

    @Override
    public void computeScroll() {
        switch (type){
            case TYPE_IN_FROM_TOP:
                //手势坐标系与scroll反方向
                scrollTo(0, - scrollY + getOverDragThreshold());
                break;
            case TYPE_IN_FROM_BOTTOM:
                //手势坐标系与scroll反方向
                scrollTo(0, - scrollY - getOverDragThreshold());
                break;
            default:
                scrollTo(0, 0);
                break;
        }
    }

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

    protected VerticalOverDragContainer getContainer() {
        if (this.container != null) {
            return this.container.get();
        }
        return null;
    }

    public void reset(boolean succeed) {
        if (state == STATE_REFRESHING){
            if (succeed){
                stateToSucceed();
            }else{
                stateToFailed();
            }
            myHandler.sendEmptyMessageDelayed(MyHandler.HANDLER_RESET, resultDuration);
        }
    }

    protected void resetContainer() {
        VerticalOverDragContainer container = getContainer();
        if (container != null) {
            if (type == TYPE_TOP_STATIC || type == TYPE_IN_FROM_TOP){
                container.resetTopPark();
            } else {
                container.resetBottomPark();
            }
        }
    }

    protected int getOverDragThreshold() {
        VerticalOverDragContainer container = getContainer();
        if (container != null) {
            return container.getOverDragThreshold();
        }
        return 0;
    }

    public interface RefreshListener{
        void onRefresh();
    }

    public void setRefreshListener(RefreshListener listener){
        this.refreshListener = listener;
    }

    /***********************************************************************************
     * handler
     */

    private MyHandler myHandler = new MyHandler(Looper.getMainLooper(), this);

    private static class MyHandler extends WeakHandler<SimpleVerticalOverDragRefreshView>{

        private static final int HANDLER_RESET = 0;

        public MyHandler(Looper looper, SimpleVerticalOverDragRefreshView host) {
            super(looper, host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, SimpleVerticalOverDragRefreshView host) {
            switch (msg.what){
                case HANDLER_RESET:
                    host.stateToInit();
                    host.resetContainer();
                    break;
                default:
                    break;
            }
        }

    }

}
