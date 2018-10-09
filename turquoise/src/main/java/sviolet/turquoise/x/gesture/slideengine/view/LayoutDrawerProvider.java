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

package sviolet.turquoise.x.gesture.slideengine.view;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import sviolet.turquoise.x.gesture.slideengine.abs.SlideException;
import sviolet.turquoise.x.gesture.slideengine.abs.SlideView;
import sviolet.turquoise.x.gesture.slideengine.impl.LinearGestureDriver;
import sviolet.turquoise.x.gesture.slideengine.impl.LinearScrollEngine;
import sviolet.turquoise.x.gesture.slideengine.listener.OnInitCompleteListener;
import sviolet.turquoise.x.gesture.slideengine.listener.OnSlideStopListener;
import sviolet.turquoise.util.droid.MeasureUtils;

/**
 * LayoutDrawer控件逻辑实现
 * 
 * @author S.Violet
 *
 */

public class LayoutDrawerProvider {

	/*******************************************************
	 * var
	 */
	
	private LinearGestureDriver mGestureDriver;
	private LinearScrollEngine mSlideEngine;
	private SlideView mSlideView;
	
	public static final int DRAWER_WIDTH_MATCH_PARENT = -1;//抽屉宽度=控件宽/高
	public static final int FEEDBACK_RANGE_HALF_HANDLE_WIDTH = -1;//把手触摸反馈=把手宽度/2
	
	public static final boolean STAGE_PUSH_IN = false;//初始位置: 收起
	public static final boolean STAGE_PULL_OUT = true;//初始位置: 拉出
	
	public static final int DEF_HANDLE_WIDTH = 0;
	public static final int DEF_SCROLL_DURATION = 500;
	public static final boolean DEF_OVER_SCROLL_ENABLED = false;
	public static final float DEF_OVER_SCROLL_DAMP = 0.7f;
	
	public static final int DIRECTION_TOP = 0;//抽屉从顶部拉出
	public static final int DIRECTION_BOTTOM = 1;//抽屉从底部拉出
	public static final int DIRECTION_LEFT = 2;//抽屉从左边拉出
	public static final int DIRECTION_RIGHT = 3;//抽屉从右边拉出
	
	private int scrollDirection = DIRECTION_LEFT;//抽屉拉出方向
	private int drawerWidth = DRAWER_WIDTH_MATCH_PARENT;//抽屉宽度(dp), 包含把手宽度
	private int handleWidth = DEF_HANDLE_WIDTH;//把手宽度(dp), 即抽屉未拉出时露出部分
	private int scrollDuration = DEF_SCROLL_DURATION;//惯性滑动全程耗时
	private boolean initStage = STAGE_PUSH_IN;//初始状态是否拉出
	private boolean overScrollEnabled = DEF_OVER_SCROLL_ENABLED;//是否允许越界拖动
	private float overScrollDamp = DEF_OVER_SCROLL_DAMP;//越界阻尼
	
	private int scrollRange = 0;//抽屉滑动距离
	private int pullOutStage;//拉出抽屉状态对应的stage
	private int pushInStage;//关闭抽屉状态对应的stage
	
	protected boolean handleFeedbackEnabled = false;//把手触摸反馈效果开关
	private int handleFeedbackRange = FEEDBACK_RANGE_HALF_HANDLE_WIDTH;//把手触摸反馈幅度
	private OnClickListener mOnHandleTouchListener;//把手触摸监听器
	private OnClickListener mOnHandleClickListener;//把手点击监听器
	private OnClickListener mOnHandleLongPressListener;//把手长按监听器
	private OnClickListener mOnGestureHoldListener;//持有监听器
	private OnSlideStopListener mOnSlideStopListener;//滑动停止监听器
	private OnInitCompleteListener mOnInitCompleteListener;//初始化完成监听器

	/**
	 *
	 * @param view 必须为继承View实现SlideView的类
	 */
	public LayoutDrawerProvider(SlideView view){
		this.mSlideView = view;
		try {
			mSlideEngine = new LinearScrollEngine(((View) view).getContext(), view);
			mGestureDriver = new LinearGestureDriver(((View) view).getContext());
		}catch(ClassCastException e){
			throw new SlideException("[DrawerProvider]SlideView is not a View instance", e);
		}
	}

	/*******************************************************
	 * setting/init
	 */
	
	/**
	 * 
	 * 设置滑动抽屉方向<p/>
	 * 
	 * 默认{@link #DIRECTION_LEFT}<p/>
	 * 
	 * {@link #DIRECTION_TOP} 抽屉从顶部拉出<br/>
	 * {@link #DIRECTION_BOTTOM} 抽屉从底部拉出<br/>
	 * {@link #DIRECTION_LEFT} 抽屉从左边拉出<br/>
	 * {@link #DIRECTION_RIGHT} 抽屉从右边拉出<br/>
	 * 
	 * 
	 * @param scrollDirection 抽屉方向
	 */
	public void setSlideScrollDirection(int scrollDirection){
		this.scrollDirection = scrollDirection;
	}
	
	/**
	 * 
	 * 设置抽屉宽度(单位 dp), 即可滑动距离<p/>
	 * 
	 * 默认{@link #DRAWER_WIDTH_MATCH_PARENT} = {@value #DRAWER_WIDTH_MATCH_PARENT}<p/>
	 * 
	 * 默认抽屉宽度 = 控件的宽度或高度<p/>
	 * 
	 * 
	 * @param drawerWidth
	 * @return
	 */
	public void setSlideDrawerWidth(int drawerWidth){
		this.drawerWidth = drawerWidth;
	}
	
	/**
	 * 
	 * 设置把手宽度(单位 dp)<p/>
	 * 
	 * 默认{@value #DEF_HANDLE_WIDTH}<p/>
	 * 
	 * 把手是抽屉收起来后用于拉出抽屉的一块特殊范围, 由GestureDriver的永久触摸区域实现, 
	 * 即控件边界处宽handleWidth的区域, 触摸起点在这个区域内, 可拉出抽屉.<p/>
	 *
	 * 例如DIRECTION_RIGHT的抽屉, handleWidth=30, 则该控件右边界宽30dp的范围内开始
	 * 触摸, 向左滑动即可拉出抽屉
	 * 
	 * 
	 * @param handleWidth
	 * @return
	 */
	public void setSlideHandleWidth(int handleWidth){
		this.handleWidth = handleWidth;
	}
	
	/**
	 * 
	 * 设置惯性滑动时间(全程) 单位ms<p/>
	 * 
	 * 默认{@value #DEF_SCROLL_DURATION}<p/>
	 * 
	 * 抽屉从收起状态到拉出状态惯性滑动所需的时间<p/>
	 * 
	 * 
	 * @param scrollDuration
	 * @return
	 */
	public void setSlideScrollDuration(int scrollDuration){
		this.scrollDuration = scrollDuration;
	}
	
	/**
	 * 
	 * 设置抽屉初始状态:收起/拉出<p/>
	 * 
	 * 默认{@link #STAGE_PUSH_IN}<p/>
	 * 
	 * {@link #STAGE_PUSH_IN}:抽屉初始状态:收起<br/>
	 * {@link #STAGE_PULL_OUT}:抽屉初始状态:拉出<Br/>
	 * 
	 * 
	 * @param initStage
	 * @return
	 */
	public void setSlideInitStage(boolean initStage){
		this.initStage = initStage;
	}
	
	/**
	 * 
	 * 设置抽屉是否允许越界拖动<p/>
	 * 
	 * 默认{@value #DEF_OVER_SCROLL_ENABLED}
	 * 
	 * 
	 * @param overScrollEnabled
	 * @return
	 */
	public void setSlideOverScrollEnabled(boolean overScrollEnabled){
		this.overScrollEnabled = overScrollEnabled;
	}
	
	/**
	 * 
	 * 设置抽屉越界拖动阻尼[0,1)<p/>
	 * 
	 * 默认{@value #DEF_OVER_SCROLL_DAMP}<p/>
	 * 
	 * 越界阻尼越大, 越界时拖动越慢
	 * 
	 * 
	 * @param overScrollDamp
	 * @return
	 */
	public void setSlideOverScrollDamp(float overScrollDamp){
		this.overScrollDamp = overScrollDamp;
	}
	
	/**
	 * 设置把手触摸反馈效果(按在把手上抽屉弹出一部分)
	 * 
	 * @param enabled
	 * @return
	 */
	public void setHandleFeedback(boolean enabled){
		this.handleFeedbackEnabled = enabled;
	}
	
	/**
	 * 
	 * 设置把手触摸反馈效果幅度, 单位dp(按在把手上抽屉弹出一部分)<p/>
	 * 
	 * 默认{@link #FEEDBACK_RANGE_HALF_HANDLE_WIDTH}: 幅度 = 把手宽度(handleWidth) / 2 <p/>
	 * 
	 * 此处的幅度不同于LinearSlideEngine的永久触摸区域反馈幅度. 此处数值为正数, 单位为dp, 
	 * 无需考虑方向(方向由抽屉方向决定), 而LinearSlideEngine的反馈幅度需要区别正负方向, 
	 * 单位为像素px
	 * 
	 * 
	 * @param range 反馈效果幅度 >=0
	 * @return
	 */
	public void setHandleFeedbackRange(int range){
		this.handleFeedbackRange = range;
	}

	/**
	 * 
	 * 设置把手触摸事件监听器<p/>
	 * 
	 * 由于该监听事件回调后, 还会触发SlideEngine手势释放事件, 导致滚动目标重定向,
	 * 该监听事件回调中若需要pullOut/pushIn, 必须使用强制执行方式
	 * 
	 *
	 * @param listener
	 */
	public void setOnHandleTouchListener(OnClickListener listener){
		this.mOnHandleTouchListener = listener;
	}

	/**
	 * 
	 * 设置把手点击事件监听器<p/>
	 * 
	 * 由于该监听事件回调后, 还会触发SlideEngine手势释放事件, 导致滚动目标重定向, 
	 * 该监听事件回调中若需要pullOut/pushIn, 必须使用强制执行方式
	 * 
	 * 
	 * @param listener
	 */
	public void setOnHandleClickListener(OnClickListener listener){
		this.mOnHandleClickListener = listener;
	}
	
	/**
	 * 
	 * 设置把手长按事件监听器<p/>
	 * 
	 * 由于该监听事件回调后, 还会触发SlideEngine手势释放事件, 导致滚动目标重定向, 
	 * 该监听事件回调中若需要pullOut/pushIn, 必须使用强制执行方式
	 * 
	 * 
	 * @param listener
	 */
	public void setOnHandleLongPressListener(OnClickListener listener){
		this.mOnHandleLongPressListener = listener;
	}

	/**
	 * 设置滑动停止监听器
	 * @param listener
	 */
	public void setOnSlideStopListener(OnSlideStopListener listener){
		this.mOnSlideStopListener = listener;
	}

	/**
	 * 
	 * 设置持有事件监听器<Br/>
	 * 当手势滑动有效距离, 触发Engine拖动时触发
	 * 
	 *
	 * @param listener
	 */
	public void setOnGestureHoldListener(OnClickListener listener){
		this.mOnGestureHoldListener = listener;
	}

	/**
	 * 
	 * 设置初始化完成监听器
	 * 
	 * @param mOnInitCompleteListener
	 */
	public void setOnInitCompleteListener(OnInitCompleteListener mOnInitCompleteListener){
		this.mOnInitCompleteListener = mOnInitCompleteListener;
	}
	
	/*********************************************************
	 * init
	 */
	
	/**
	 * 初始化滑动
	 */
	protected void initSlide() {
		try{
			Context context = ((View) mSlideView).getContext();
			int viewWidth = ((View)mSlideView).getWidth();//控件宽度
			int viewHeight = ((View)mSlideView).getHeight();//控件高度
			int initPosition = 0;//初始位置
			int orientation = LinearGestureDriver.ORIENTATION_HORIZONTAL;//手势捕获方向
			int xScrollRange;//x轴方向滚动距离
			int yScrollRange;//y轴方向滚动距离
			if(drawerWidth > DRAWER_WIDTH_MATCH_PARENT){
				xScrollRange = MeasureUtils.dp2px(context, drawerWidth); //滑动范围
				yScrollRange = MeasureUtils.dp2px(context, drawerWidth); //滑动范围
			}else{
				xScrollRange = viewWidth;//滑动范围
				yScrollRange = viewHeight;//滑动范围
			}
			
			//永久触摸区域
			int staticTouchAreaLeft = 0;
			int staticTouchAreaRight = 0;
			int staticTouchAreaTop = 0;
			int staticTouchAreaBottom = 0;
			boolean staticTouchAreaEnabled = false;
			if(handleWidth > 0) {
                staticTouchAreaEnabled = true;
            }
			
			//把手触摸反馈
			int _handleFeedbackRange = 0;
			if(handleFeedbackRange > FEEDBACK_RANGE_HALF_HANDLE_WIDTH) {
                _handleFeedbackRange = MeasureUtils.dp2px(context, handleFeedbackRange);
            } else {
                _handleFeedbackRange = MeasureUtils.dp2px(context, handleWidth / 2);
            }
			
			switch(scrollDirection){
			case DIRECTION_TOP:
				scrollRange = yScrollRange;//滑动范围
				orientation = LinearGestureDriver.ORIENTATION_VERTICAL;//捕获垂直手势
				pullOutStage = 0;
				pushInStage = 1;
				//永久触摸区域
				staticTouchAreaLeft = 0;
				staticTouchAreaRight = viewWidth;
				staticTouchAreaTop = 0;
				staticTouchAreaBottom = MeasureUtils.dp2px(context, handleWidth);
				//把手触摸反馈
				_handleFeedbackRange = - _handleFeedbackRange;//设定方向
				//初始位置
				if(initStage == STAGE_PULL_OUT) {
                    initPosition = 0;//初始位置:原位
                } else {
                    initPosition = scrollRange;//初始位置:上面
                }
				break;
			case DIRECTION_BOTTOM:
				scrollRange = yScrollRange;//滑动范围
				orientation = LinearGestureDriver.ORIENTATION_VERTICAL;//捕获垂直手势
				pullOutStage = 1;
				pushInStage = 0;
				//永久触摸区域
				staticTouchAreaLeft = 0;
				staticTouchAreaRight = viewWidth;
				staticTouchAreaTop = viewHeight - MeasureUtils.dp2px(context, handleWidth);
				staticTouchAreaBottom = viewHeight;
				//把手触摸反馈
//				_handleFeedbackRange = _handleFeedbackRange;//设定方向
				//初始位置
				if(initStage == STAGE_PULL_OUT) {
                    initPosition = scrollRange;//初始位置:原位
                } else {
                    initPosition = 0;//初始位置:下面
                }
				break;
			case DIRECTION_LEFT:
				scrollRange = xScrollRange;//滑动范围
				orientation = LinearGestureDriver.ORIENTATION_HORIZONTAL;//捕获水平手势
				pullOutStage = 0;
				pushInStage = 1;
				//永久触摸区域
				staticTouchAreaLeft = 0;
				staticTouchAreaRight = MeasureUtils.dp2px(context, handleWidth);
				staticTouchAreaTop = 0;
				staticTouchAreaBottom = viewHeight;
				//把手触摸反馈
				_handleFeedbackRange = - _handleFeedbackRange;//设定方向
				//初始位置
				if(initStage == STAGE_PULL_OUT) {
                    initPosition = 0;//初始位置:原位
                } else {
                    initPosition = scrollRange;//初始位置:左边
                }
				break;
			case DIRECTION_RIGHT:
				scrollRange = xScrollRange;//滑动范围
				orientation = LinearGestureDriver.ORIENTATION_HORIZONTAL;//捕获水平手势
				pullOutStage = 1;
				pushInStage = 0;
				//永久触摸区域
				staticTouchAreaLeft = viewWidth - MeasureUtils.dp2px(context, handleWidth);
				staticTouchAreaRight = viewWidth;
				staticTouchAreaTop = 0;
				staticTouchAreaBottom = viewHeight;
				//把手触摸反馈
//				_handleFeedbackRange = _handleFeedbackRange;//设定方向
				//初始位置
				if(initStage == STAGE_PULL_OUT) {
                    initPosition = scrollRange;//初始位置:原位
                } else {
                    initPosition = 0;//初始位置:右边
                }
				break;
			}

			mGestureDriver.setOrientation(orientation);//设置手势捕获方向
			mGestureDriver.setStaticTouchArea(staticTouchAreaEnabled, staticTouchAreaLeft, staticTouchAreaRight, staticTouchAreaTop, staticTouchAreaBottom);
			mSlideEngine.setMaxRange(scrollRange);//设置最大可滑动距离
			mSlideEngine.setInitPosition(initPosition);//设置初始位置
			mSlideEngine.setStageDuration(scrollDuration);//设置阶段滑动时间

			mSlideEngine.bind(mGestureDriver);
			mSlideEngine.setOverScroll(overScrollEnabled, overScrollDamp);
			mSlideEngine.setStaticTouchAreaFeedback(handleFeedbackEnabled, _handleFeedbackRange);
			mSlideEngine.setOnStaticTouchAreaClickListener(mOnHandleClickListener);
			mSlideEngine.setOnStaticTouchAreaLongPressListener(mOnHandleLongPressListener);
			mSlideEngine.setOnSlideStopListener(mOnSlideStopListener);
			mSlideEngine.setOnGestureHoldListener(mOnGestureHoldListener);
			mSlideEngine.setOnStaticTouchAreaTouchListener(mOnHandleTouchListener);
		}catch(ClassCastException e){
			throw new SlideException("[DrawerProvider]SlideView is not a View instance", e);
		}

		if (mOnInitCompleteListener != null) {
            mOnInitCompleteListener.onInitComplete((View) mSlideView);
        }
	}
	
	/****************************************************
	 * func
	 */
	
	/**
	 * 拉出抽屉
	 */
	protected void pullOut(){
		if(mSlideEngine != null) {
            mSlideEngine.scrollToStage(pullOutStage, false);
        }
	}
	
	/**
	 * 关闭抽屉
	 */
	protected void pushIn(){
		if(mSlideEngine != null) {
            mSlideEngine.scrollToStage(pushInStage, false);
        }
	}
	
	/**
	 * 
	 * 拉出抽屉<p/>
	 * 
	 * 设定强制执行后, 抽屉拉出完成前触摸无效, 滚动强制完成目标无法改变
	 * 
	 * @param force 是否强制执行(锁定目标)
	 */
	protected void pullOut(boolean force){
		if(mSlideEngine != null) {
            mSlideEngine.scrollToStage(pullOutStage, force);
        }
	}
	
	/**
	 * 
	 * 关闭抽屉<p/>
	 * 
	 * 	设定强制执行后, 抽屉拉出完成前触摸无效, 滚动强制完成目标无法改变
	 * 
	 * @param force 是否强制执行(锁定目标)
	 */
	protected void pushIn(boolean force){
		if(mSlideEngine != null) {
            mSlideEngine.scrollToStage(pushInStage, force);
        }
	}

    /**
     * 打开抽屉(立即, 无动画)
     */
	protected void pullOutImmediately(){
		if (mSlideEngine != null) {
            mSlideEngine.scrollToPosition(mSlideEngine.getPositionOfStage(pullOutStage), 0, false);
        }
	}

    /**
     * 关闭抽屉(立即, 无动画)
     */
	protected void pushInImmediately(){
		if (mSlideEngine != null) {
            mSlideEngine.scrollToPosition(mSlideEngine.getPositionOfStage(pushInStage), 0, false);
        }
	}
	
	/**
	 * 销毁
	 */
	protected void destroy(){
		if(mGestureDriver != null){
			mGestureDriver.destroy();
			mGestureDriver = null;
		}
		if(mSlideEngine != null){
			mSlideEngine.destroy();
			mSlideEngine = null;
		}
	}

	/*******************************************************
	 * getter
	 */
	
	/**
	 * 获得抽屉滚动范围(距离)
	 * @return
	 */
	public int getScrollRange(){
		return scrollRange;
	}
	
	protected LinearGestureDriver getGestureDriver() {
		return mGestureDriver;
	}

	protected LinearScrollEngine getSlideEngine() {
		return mSlideEngine;
	}

	protected int getScrollDirection() {
		return scrollDirection;
	}

	protected int getDrawerWidth() {
		return drawerWidth;
	}

	protected int getHandleWidth() {
		return handleWidth;
	}

	protected int getScrollDuration() {
		return scrollDuration;
	}

	protected boolean getInitStage() {
		return initStage;
	}

	protected boolean isOverScrollEnabled() {
		return overScrollEnabled;
	}

	protected float getOverScrollDamp() {
		return overScrollDamp;
	}

	protected float getPullOutStage() {
		return pullOutStage;
	}

	protected float getPushInStage() {
		return pushInStage;
	}

	protected boolean isHandleFeedbackEnabled() {
		return handleFeedbackEnabled;
	}

	protected int getHandleFeedbackRange() {
		return handleFeedbackRange;
	}

	protected float getCurrentStage(){
		if (mSlideEngine != null) {
            return mSlideEngine.getCurrentStage();
        } else {
            return 0;
        }
	}
	
}
