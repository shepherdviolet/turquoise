/*
 * Copyright (C) 2015 S.Violet
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
 */

package sviolet.turquoise.view.slide.logic;

import sviolet.turquoise.view.listener.OnSlideStopListener;
import sviolet.turquoise.view.slide.GestureDriver;
import sviolet.turquoise.view.slide.SlideEngine;
import sviolet.turquoise.view.slide.SlideView;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 
 * 线性拖动引擎(无惯性)<br/>
 * <Br/>
 * 基本设置:<br/>
 * setMaxRange()<br/>
 * setInitPosition()<br/>
 * setSlidingDirection()<br/>
 * <br>
 * {@link SlideView}<br/>
 * *************************************************************************************<br>
 * 输出定义::<br>
 * <br>
 * position方向:<br>
 * DIRECTION_LEFT_OR_TOP				:	手势上/左::递增  手势下/右::递减<br>
 * DIRECTION_RIGHT_OR_BOTTOM	: 	手势上/左::递减  手势下/右::递增<br>
 * <br>
 * stage方向:<br>
 * DIRECTION_LEFT_OR_TOP				:	手势上/左::递增  手势下/右::递减<br>
 * DIRECTION_RIGHT_OR_BOTTOM	: 	手势上/左::递减  手势下/右::递增<br>
 * 
 * @author S.Violet
 *
 */

public class LinearDragEngine implements SlideEngine {
	
	private static final float DEF_OVER_SCROLL_DAMP = 0.7f;
	
	//位置状态
	protected static final int POSITION_OUT_OF_MIN = -2;//小于最小值
	protected static final int POSITION_ON_MIN = -1;//在最小值处
	protected static final int POSITION_IN_RANGE = 0;//在范围内
	protected static final int POSITION_ON_MAX = 1;//在最大值处
	protected static final int POSITION_OUT_OF_MAX = 2;//大于最大值
	
	public static final int DIRECTION_LEFT_OR_TOP = 0;//由原位置(position=0, scrollX/Y=0)向左边或上方滑动
	public static final int DIRECTION_RIGHT_OR_BOTTOM = 1;//由原位置(position=0, scrollX/Y=0)向右边或下方滑动
	protected int slidingDirection = DIRECTION_LEFT_OR_TOP;//滑动方向(仅影响输出值)
	
	protected static final int ORIGIN_POSITION = 0;//原点位置
	
	public static final int STATE_STOP = 0;//运动停止状态
	public static final int STATE_HOLDING = 1;//手势持有运动状态
	public static final int STATE_SLIDING = 2;//惯性滑动状态
	
	protected SlideView mSlideView = null;
	protected GestureDriver mGestureDriver = null;
	protected Context mContext = null;
	private SlideEngine parentSlideEngine = null;//外部引擎（嵌套时）
	
	private OnClickListener mOnGestureHoldListener;//GestureDriver HOLD事件监听器
	private OnSlideStopListener mOnSlideStopListener;//滑动停止监听器
	
	protected boolean infiniteRange = false;//无边界(无限滚动距离)
	protected boolean overScrollEnable = false;//允许拖动越界
	protected float overScrollDamp = DEF_OVER_SCROLL_DAMP;//越界拖动阻尼(阻尼越大越界移动越慢)
	
	protected int state = STATE_STOP;//运动状态
	protected int position = ORIGIN_POSITION;//当前位置
	protected int lastPosition = ORIGIN_POSITION;//上次位置, 用于计算偏移量
	protected int range = ORIGIN_POSITION;//可滑动最大距离

	protected boolean isSlideStopCallbacked = false;//滑动停止事件已回调

	/**
	 * @param context ViewGroup上下文
	 * @param slideView 通知刷新的View
	 */
	public LinearDragEngine(Context context, SlideView slideView){
		this.mContext = context;
		this.mSlideView = slideView;
	}

	/*********************************************************
	 * settings
	 */

	/**
     * [基本设置]<br/>
	 * 设置允许滑动的最大距离(默认0)
	 * @param maxRange 允许滑动最大距离(全程) >=0
	 */
	public void setMaxRange(int maxRange){
		if(maxRange  >= 0)
			this.range = maxRange;
		else
			this.range = 0;
	}

	/**
     * [基本设置]<br/>
	 * 设置初始位置(默认0)
	 * @param initPosition 初始位置
	 */
	public void setInitPosition(int initPosition){
		this.position = initPosition;
		this.lastPosition = ORIGIN_POSITION;
	}

	/**
     * [基本设置]<br/>
	 * 设置滑动输出方向(默认DIRECTION_LEFT_OR_TOP)
	 * @param slidingDirection 滑动输出方向
	 */
	public void setSlidingDirection(int slidingDirection){
		this.slidingDirection = slidingDirection;
	}

	/**
	 * 设置越界拖动
	 *
	 * @param overScrollEnable 是否允许越界拖动
	 * @param overScrollDamp 越界拖动阻尼[0~1), 阻尼越大越界拖动越慢
	 */
	public void setOverScroll(boolean overScrollEnable, float overScrollDamp){
		this.overScrollEnable = overScrollEnable;
		if(overScrollDamp >=0.0f && overScrollDamp < 1.0f)
			this.overScrollDamp = overScrollDamp;
	}

	/**
	 * 设置hold事件监听器<Br>
	 * 当手势滑动有效距离, 触发Engine拖动时触发
	 *
	 * @param listener
	 */
	public void setOnGestureHoldListener(OnClickListener listener){
		this.mOnGestureHoldListener = listener;
	}

	/**
	 * 设置滑动停止监听器
	 *
	 * @param mOnSlideStopListener
	 */
	public void setOnSlideStopListener(OnSlideStopListener mOnSlideStopListener){
		this.mOnSlideStopListener = mOnSlideStopListener;
	}

	/**
	 * 设置是否无限滑动距离(无边界)<br>
	 * <br>
	 * 默认:false<br>
	 *
	 * @param value true:无限距离(无边界)
	 */
	public void setInfiniteRange(boolean value){
		this.infiniteRange = value;
	}

	/**
	 * 添加一个内部引擎<br/>
	 * 作用:<br/>
	 * 1.内部引擎拦截到事件后, 会调用外部引擎对应驱动的skipIntercept()方法,
	 *      阻断外部引擎的本次事件拦截, 防止内部控件滑动时被外部拦截<br/>
	 *
	 * @param innerSlideEngine 内部引擎
	 */
	@Override
	public void addInnerEngine(SlideEngine innerSlideEngine) {
		if (innerSlideEngine != null){
			innerSlideEngine.setParentEngine(this);
		}
	}

	/**
	 * 设置外部引擎<br/>
	 * 对应addInnerEngine()<br/>
	 *
	 * @param parentSlideEngine 外部引擎
	 */
	@Override
	public void setParentEngine(SlideEngine parentSlideEngine) {
		this.parentSlideEngine = parentSlideEngine;
	}

	/*********************************************************
	 * 实现
	 */
	
	/**
	 * 绑定GestureDriver
	 */
	@Override
	public void bind(GestureDriver gestureDriver) {
		onBind(gestureDriver);
		if(mGestureDriver != null)
			mGestureDriver.onBind(this);
	}
	
	@Override
	public void onBind(GestureDriver gestureDriver) {
		mGestureDriver = gestureDriver;
	}

	/**
	 * 输入方式 : 一维
	 */
	@Override
	public int inputMode() {
		return SlideEngine.INPUT_MODE_1D;
	}

	/**
	 * [手势通知引擎]输入触摸状态(二维)<br>
	 * <br>
	 * 输入手势运动状态<br>
	 * 
	 */
	@Override
	public void onGestureDrive(int x, int y, int offsetX, int offsetY, int velocityX, int velocityY) {
	}
	
	/**
	 * [手势通知引擎]输入触摸状态(一维)<br>
	 * <br>
	 * 输入手势运动状态<br>
	 * 
	 */
	@Override
	public void onGestureDrive(int curr, int offset, int velocity) {
		if(slidingDirection == DIRECTION_LEFT_OR_TOP)
			handleGestureDrive(curr, offset, velocity);
		else
			handleGestureDrive(curr, -offset, -velocity);
	}

	/**
	 * [手势通知引擎]开始触摸<br>
	 * <br>
	 * 通知SlideView刷新UI
	 */
	@Override
	public void onGestureHold() {
		handleGestureHold();
	}
	
	/**
	 * [手势通知引擎]已释放触摸<br>
	 * <br>
	 */
	@Override
	public void onGestureRelease(int velocity) {
		if(slidingDirection == DIRECTION_LEFT_OR_TOP)
			handleGestureRelease(velocity);
		else
			handleGestureRelease(-velocity);
	}
	
	/**
	 * [手势通知引擎]永久触摸区域拦截到未处理事件<br>
	 */
	@Override
	public void onStaticTouchAreaCaptureEscapedTouch() {
		
	}
	
	/**
	 * [手势通知引擎]永久触摸区域点击事件<br>
	 */
	@Override
	public void onStaticTouchAreaClick() {
		
	}
	
	/**
	 * [手势通知引擎]永久触摸区域长按事件<br>
	 */
	@Override
	public void onStaticTouchAreaLongPress() {
		
	}
	
	/**
	 * 是否在惯性滑动
	 */
	@Override
	public boolean isSliding() {
		return state == STATE_SLIDING;
	}

	/**
	 * 获得外部引擎
	 * @return
	 */
	@Override
	public SlideEngine getParentEngine() {
		return parentSlideEngine;
	}

	/**
	 * 获得对应手势驱动器
	 *
	 * @return
	 */
	@Override
	public GestureDriver getGestureDriver() {
		return mGestureDriver;
	}

	/**
	 * 跳过本次拦截, 并上发到外部引擎
	 */
	@Override
	public void skipIntercepted() {
		//手势驱动器跳过本次拦截
		if (getGestureDriver() != null)
			getGestureDriver().skipIntercepted();
		//外部驱动跳过本次拦截
		if (parentSlideEngine != null)
			parentSlideEngine.skipIntercepted();
	}

	/**
	 * 销毁
	 */
	@Override
	public void destroy() {
		mSlideView = null;
		mGestureDriver = null;
		mContext = null;
	}

	/***************************************************
	 * 处理手势输入
	 */
	
	/**
	 * 处理手势输入:移动
	 * 
	 * @param curr
	 * @param offset
	 * @param velocity
	 */
	protected void handleGestureDrive(int curr, int offset, int velocity) {
		if(overScrollEnable){//允许越界拖动
			switch(checkPositionState(position)){
			case POSITION_OUT_OF_MIN:
			case POSITION_OUT_OF_MAX:
				//越界减速
				position = (int) ((float)position - (float)offset * (1.0f - overScrollDamp));
				break;
			default:
				position = position - offset;
				break;
			}
		}else{//禁止越界拖动
			//计算位置
			int _position = position - offset;
			//越界处理
			switch(checkPositionState(_position)){
			case POSITION_OUT_OF_MIN:
				position = ORIGIN_POSITION;
				break;
			case POSITION_OUT_OF_MAX:
				position = range;
				break;
			default:
				position = _position;
				break;
			}
		}
		//若引擎被驱动时, 处于STOP状态, 则置为HOLDING状态, 并通知刷新
		if (state == STATE_STOP){
			handleGestureHold();
		}
	}
	
	/**
	 * 处理手势输入:持有
	 */
	protected void handleGestureHold() {
		//持有状态
		state = STATE_HOLDING;
		//通知刷新UI
		notifySlideView();

		//跳过外部引擎本次拦截
		if (parentSlideEngine != null){
			parentSlideEngine.skipIntercepted();
		}

		//调用监听器
		if(mOnGestureHoldListener != null){
			try{
				mOnGestureHoldListener.onClick((View) mSlideView);
			}catch (ClassCastException e){
				mOnGestureHoldListener.onClick(null);
			}
		}
	}

	/**
	 * 处理手势输入:释放
	 */
	protected void handleGestureRelease(int velocity) {
		//释放后置为停止状态
		state = STATE_STOP;
	}
	
	/**************************************************
	 * public/protected
	 */
	
	/**
	 * 获得当前运动位置
	 * @return
	 */
	public int getPosition(){
		//记录当前位置为前次位置
		lastPosition = position;
		return position;
	}
	
	/**
	 * 获得当前的阶段(页数)<br>
	 * <br>
	 * position = 0 时 stage = 0;<br>
	 * position = range 时 stage = 1(最大值)<br>
	 */
	public float getCurrentStage() {
		int positionState = checkPositionState(position);
		switch(positionState){
		case POSITION_OUT_OF_MIN:
		case POSITION_ON_MIN:
			return 0;
		case POSITION_ON_MAX:
		case POSITION_OUT_OF_MAX:
			return 1;
		default:
			break;
		}
		return (float)position / (float)range;
	}
	
	/**
	 * 获得阶段对应的位置
	 * 
	 * @param stage [0, 1]
	 * @return
	 */
	public int getPositionOfStage(int stage){
		return limit(stage, ORIGIN_POSITION, 1) * range;
	}
	
	/**
	 * 检查运动是否已停止, 若停止则置为停止状态, 并返回true
	 * @return
	 */
	public boolean isStop(){

		if (state == STATE_STOP){
			if (!isSlideStopCallbacked && mOnSlideStopListener != null){
				isSlideStopCallbacked = true;
				mOnSlideStopListener.onStop();
			}
			return true;
		}else{
			//清除已回调状态
			isSlideStopCallbacked = false;
			return false;
		}
	}
	
	/**
	 * 获得与引擎绑定的SlideView
	 * @return
	 */
	public SlideView getSlideView(){
		return mSlideView;
	}
	
	/**
	 * 获得引擎的最大滑动范围(距离)
	 * @return
	 */
	public int getRange(){
		return range;
	}
	
	/**
	 * 获得引擎的阶段数<br>
	 * @return 2
	 */
	public int getStageNum(){
		return 2;
	}
	
	/***************************************************
	 * private/protected
	 */
	
	/**
	 * 当前位置状态
	 * 
	 * @param position
	 * @return
	 */
	protected int checkPositionState(int position){
		//无限滑动范围
		if(infiniteRange)
			return POSITION_IN_RANGE;
		
		if(position < ORIGIN_POSITION)
			return POSITION_OUT_OF_MIN;
		else if(position == ORIGIN_POSITION)
			return POSITION_ON_MIN;
		else if(position == range)
			return POSITION_ON_MAX;
		else if(position > range)
			return POSITION_OUT_OF_MAX;
		else
			return POSITION_IN_RANGE;
	}
	
	/**
	 * 判断当前位置是否在停止点(由checkSlideStop调用)
	 * @return
	 */
	protected boolean isOnArrestPoint(){
		int positionState = checkPositionState(position);
		return positionState == POSITION_ON_MIN || positionState == POSITION_ON_MAX;
	}
	
	/**
	 * 获得上次运动位置<br>
	 * @return
	 */
	protected int getLastPosition(){
		return lastPosition;//由原位置向左上滑动情况的输出
	}
	
	/**
	 * 通知SlideView刷新显示
	 */
	protected void notifySlideView(){
		handler.sendEmptyMessage(HANDLER_NOTIFY_SLIDE);
	}
	
	/**
	 * 限制num在lowerLimit和higherLimit之间 [lowerLimit, higherLimit]
	 * 
	 * @param num
	 * @param lowerLimit 下限
	 * @param higherLimit 上限
	 * @return
	 */
	protected int limit(int num, int lowerLimit, int higherLimit){
		if(num < lowerLimit)
			return lowerLimit;
		else if(num > higherLimit)
			return higherLimit;
		else
			return num;
	}
	
	/****************************************************
	 * Handler
	 */
	
	private static final int HANDLER_NOTIFY_SLIDE = 0;//通知SlideView刷新
	
	private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
        	
			switch (msg.what) {
			case HANDLER_NOTIFY_SLIDE://通知SlideView刷新
				if(mSlideView != null)
					mSlideView.notifyRefresh();
				break;
			default:
				break;
			}
			
			return true;
        }
    });
}
