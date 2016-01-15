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
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.turquoise.uix.slideengine.logic;

import java.util.Timer;
import java.util.TimerTask;

import sviolet.turquoise.enhance.common.WeakHandler;
import sviolet.turquoise.uix.slideengine.abs.GestureDriver;
import sviolet.turquoise.uix.slideengine.abs.SlideEngine;
import sviolet.turquoise.uix.slideengine.abs.SlideView;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * 
 * 线性手势驱动<br/>
 * <br/>
 * 基本设置:<br/>
 * setOrientation()<br/>
 * <br>
 * {@link SlideView}<br/>
 * *************************************************************************************<br>
 * 手势捕获示例:<br>
 * 需要捕获触摸手势的控件复写如下方法:<br>
 * ViewGroup::<br>
 * <br>
	//复写事件拦截
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean original = super.onInterceptTouchEvent(ev);
		if(mGestureDriver != null && mGestureDriver.onInterceptTouchEvent(ev))
			return true;
		return original;
	}
	
	//复写触摸事件处理
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean original = super.onTouchEvent(event);
		if(mGestureDriver != null && mGestureDriver.onTouchEvent(event))
			return true;
		return original;
	}
 * <br>
 * *************************************************************************************<br>
 * 永久触摸区域::<br>
 * 若设置了永久触摸区域, 该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent
 * 会返回true以阻止事件向后方传递. 该区域内的触摸事件(down)强制拦截, 后方View无法
 * 捕获触摸事件. 可用于控件边界的把手设计.<br>
 * <br>
 * *************************************************************************************<br>
 * 输出定义::<br>
 * <br>
 * velocity方向:<br>
 * 手势上/左::负  手势下/右::正<br>
 * <br>
 * curr方向:<br>
 * 手势上/左::递减  手势下/右::递增
 * <br>
 * step方向:<br>
 * 手势上/左::负  手势下/右::正<br>
 * 
 * @author S.Violet
 *
 */

public class LinearGestureDriver implements GestureDriver {
	
	private static final long STATIC_TOUCH_AREA_LONG_PRESS_DELAY = 2000L;//永久触摸区域长按事件触发时间
	private static final long RELEASE_CHECK_DELAY = 5000L;//[ACTION超时释放]检测间隔
	
	//速度捕获器
	private VelocityTracker mVelocityTracker;
	//绑定的引擎
	private SlideEngine mSlideEngine;
	//Context
	private Context mContext;
	//是否被销毁
	private boolean isDestroyed = false;
	//[特殊状态]在控件惯性滑动时触摸停住(hold)滑动
	private boolean holdSliding = false;
	//[特殊状态]跳过拦截状态::被其他GestureDriver拦截事件, 不再拦截本次事件(本次down的后续事件), 下次down会重置标志位
	private boolean skipIntercepted = false;
	
	//参数/////////////////////////////////
	//捕获手势方向
	public static final int ORIENTATION_ALL = 0;//双向支持
	public static final int ORIENTATION_VERTICAL = 1;//垂直手势捕获
	public static final int ORIENTATION_HORIZONTAL = 2;//水平手势捕获
	private int orientation = ORIENTATION_ALL;//捕获手势方向
	
	//滑动有效距离
	private int mTouchSlop;
	
	//有效触摸区域(down时的区域)
	public static final int TOUCH_AREA_MODE_NULL = 0;//全都无效
	public static final int TOUCH_AREA_MODE_ALL = 1;//全部有效
	public static final int TOUCH_AREA_MODE_VALID = 2;//指定范围有效(矩形)
	public static final int TOUCH_AREA_MODE_INVALID = 3;//指定范围无效(矩形)
	private int touchAreaMode = TOUCH_AREA_MODE_ALL;//有效区域模式
	private int touchAreaLeft = Integer.MIN_VALUE;//区域左边界
	private int touchAreaRight = Integer.MAX_VALUE;//区域右边界
	private int touchAreaTop = Integer.MIN_VALUE;//区域上边界
	private int touchAreaBottom = Integer.MAX_VALUE;//区域下边界
	
	//永久触摸区域::
	//该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent会返回true以阻止事件向后方传递
	private boolean staticTouchAreaEnabled = false;//是否允许永久触摸区域
	private int staticTouchAreaLeft = 0;//区域左边界
	private int staticTouchAreaRight = 0;//区域右边界
	private int staticTouchAreaTop = 0;//区域上边界
	private int staticTouchAreaBottom = 0;//区域下边界
	//永久触摸区域在ViewGroup.onTouchEvent拦截到未被处理的事件, 需要在onTouch中判断是否有效滑动
	private boolean captureEscapedTouch = false;
	//永久触摸区域长按计时器
	private Timer staticTouchAreaLongPressTimer;
	private boolean staticTouchAreaLongPressHandled = false;//永久触摸区域长按事件已处理, 不再处理click
	
	//变量/////////////////////////////////
	//按下时的坐标
	private int downX = 0;
	private int downY = 0;
	
	//当前坐标
	private int currX = 0;
	private int currY = 0;
	
	//前次坐标
	private int lastX = 0;
	private int lastY = 0;
	
	//单次位移
	private int stepX = 0;
	private int stepY = 0;
	
	//状态
	public static final int STATE_RELEASE = 0;//释放
	public static final int STATE_DOWN = 1;//按下但并未有效移动
	public static final int STATE_MOVING_X = 2;//X轴移动
	public static final int STATE_MOVING_Y = 3;//Y轴移动
	private int state = STATE_RELEASE;
	
	//[ACTION超时释放]检测时间内ACTION事件计数器
	private int releaseCheckCounter = 0;
	
	/**
	 * @param context ViewGroup上下文

	 */
	public LinearGestureDriver(Context context){
		this.mContext = context;
		this.mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
	}

	/*****************************************************
	 * 					settings
	 */

	/**
	 * [基本设置]<br/>
	 * 设置手势获取方向(默认ORIENTATION_ALL)<Br/>
	 * 	ORIENTATION_ALL 双向支持<Br/>
	 * 	ORIENTATION_VERTICAL 垂直手势捕获<Br/>
	 * 	ORIENTATION_HORIZONTAL 水平手势捕获<Br/>
	 *
	 * @param orientation 手势捕获方向
	 */
	public void setOrientation(int orientation){
		this.orientation = orientation;
	}

	/**
	 * 设置有效/无效触摸区域(单位px)<br>
	 * <br>
	 * touchAreaMode::<br>
	 * TOUCH_AREA_MODE_NULL 触摸全部无效<br>
	 * TOUCH_AREA_MODE_ALL 触摸全部有效<br>
	 * TOUCH_AREA_MODE_VALID 指定范围内有效(矩形)<br>
	 * TOUCH_AREA_MODE_INVALID 指定范围内无效(矩形)<br>
	 * <br>
	 *
	 * @param touchAreaMode
	 * @param left 区域左边界
	 * @param right 区域右边界
	 * @param top 区域上边界
	 * @param bottom 区域下边界
	 */
	public void setTouchArea(int touchAreaMode, int left, int right, int top, int bottom){
		this.touchAreaMode = touchAreaMode;//有效区域模式
		this.touchAreaLeft = left;//区域左边界
		this.touchAreaRight = right;//区域右边界
		this.touchAreaTop = top;//区域上边界
		this.touchAreaBottom = bottom;//区域下边界
	}

	/**
	 * 设置有效/无效触摸区域<br>
	 * <br>
	 * touchAreaMode::<br>
	 * TOUCH_AREA_MODE_NULL 触摸全部无效<br>
	 * TOUCH_AREA_MODE_ALL 触摸全部有效<br>
	 * TOUCH_AREA_MODE_VALID 指定范围内有效(矩形)<br>
	 * TOUCH_AREA_MODE_INVALID 指定范围内无效(矩形)<br>
	 * <br>
	 * @param touchAreaMode
	 */
	public void setTouchAreaMode(int touchAreaMode){
		this.touchAreaMode = touchAreaMode;//有效区域模式
	}

	/**
	 * 设置永久触摸区域(单位px)<br>
	 * 该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent会返回true以阻止事件向后方传递<Br>
	 *
	 * @param staticTouchAreaEnabled 是否开启永久触摸区域
	 * @param left 区域左边界
	 * @param right 区域右边界
	 * @param top 区域上边界
	 * @param bottom 区域下边界
	 */
	public void setStaticTouchArea(boolean staticTouchAreaEnabled, int left, int right, int top, int bottom){
		this.staticTouchAreaEnabled = staticTouchAreaEnabled;//是否允许永久触摸区域
		this.staticTouchAreaLeft = left;//区域左边界
		this.staticTouchAreaRight = right;//区域右边界
		this.staticTouchAreaTop = top;//区域上边界
		this.staticTouchAreaBottom = bottom;//区域下边界
	}
	
	/***********************************************************************************************************
	 * 						calculate
	 */
	
	//坐标过滤//////////////////////////////////////////////
	
	public int getX(MotionEvent event){
		return (int) event.getX();//相对于容器的坐标
	}
	
	public int getY(MotionEvent event){
		return (int) event.getY();//相对于容器的坐标
	}
	
	//计算判断/////////////////////////////////////////////
	
	/**
	 * 判断坐标是否在有效触摸区域内(触摸是否有效)
	 * @param x
	 * @param y
	 * @return true 触摸有效 false 无效
	 */
	private boolean isValidTouch(int x, int y){
		
		//是否在区域内
		boolean isInArea = (x >= touchAreaLeft && x <= touchAreaRight && y >= touchAreaTop && y <= touchAreaBottom);
		
		//区分触摸区域模式
		switch(touchAreaMode){
		case TOUCH_AREA_MODE_NULL://全部无效
			return false;
		case TOUCH_AREA_MODE_ALL://全部有效
			return true;
		case TOUCH_AREA_MODE_VALID://区域内有效
			if(isInArea)
				return true;
			else
				return false;
		case TOUCH_AREA_MODE_INVALID://区域内无效
			if(isInArea)
				return false;
			else
				return true;
		default:
		}
		return false;
	}
	
	/**
	 * 判断坐标是否在永久触摸区域内
	 * @param x
	 * @param y
	 * @return true 触摸有效 false 无效
	 */
	private boolean isStaticTouch(int x, int y){
		//是否启用永久触摸区域
		if(!staticTouchAreaEnabled)
			return false;
		
		//判断是否在区域内
		return x > staticTouchAreaLeft && x < staticTouchAreaRight && y > staticTouchAreaTop && y < staticTouchAreaBottom;
	}
	
	/**
	 * 判断X轴方向是否有效移动
	 * @return
	 */
	private boolean isValidMoveX() {
		return orientation != ORIENTATION_VERTICAL && Math.abs(currX - downX) > mTouchSlop;
	}
	
	/**
	 * 判断Y轴方向是否有效移动
	 * @return
	 */
	private boolean isValidMoveY() {
		return orientation != ORIENTATION_HORIZONTAL && Math.abs(currY - downY) > mTouchSlop;
	}
	
	/**
	 * 重置坐标<br>
	 * 1.记录当前坐标<br>
	 * 2.重置按下时坐标/上次坐标<br>
	 * 
	 * @param event
	 */
	private void resetCoordinate(MotionEvent event) {
		currX = getX(event);
		currY = getY(event);
		downX = getX(event);
		downY = getY(event);
		lastX  = getX(event);
		lastY = getY(event);
	}
	
	/**
	 * 计算坐标<br>
	 * 1.记录当前坐标<br>
	 * 2.计算位移<br>
	 * 
	 * @param event
	 */
	private void calculateCoordinate(MotionEvent event) {
		currX = getX(event);
		currY = getY(event);
		stepX = currX - lastX;
		stepY = currY - lastY;
		lastX = currX;
		lastY = currY;
	}
	
	/**
	 * 检查滑动是否有效, 若有效则记录滑动方向, 并通知SlideEngine
	 * 
	 * @return true:有效滑动 false:无效滑动
	 */
	private boolean checkValidMove() {
		if(isValidMoveY()){
			state = STATE_MOVING_Y;
			holdEngine();
			return true;
		} else if(isValidMoveX()){
			state = STATE_MOVING_X;
			holdEngine();
			return true;
		}
		return false;
	}
	
	/***********************************************************************************************************
	 * 						M
	 */
	
	//输出/////////////////////////////////////////////////////////////////
	
	/**
	 * 向引擎输出
	 * 
	 */
	private void driveEngine() {
		if (mSlideEngine != null) {
			//当前加速度
			int velocity[] = getVelocity();

            switch(mSlideEngine.inputMode()){
                case SlideEngine.INPUT_MODE_1D:
                    //一维
                    switch (state) {
                        case STATE_MOVING_X://X轴方向移动
                            mSlideEngine.onGestureDrive(currX, stepX, velocity[0]);
                            break;
                        case STATE_MOVING_Y://Y轴方向移动
                            mSlideEngine.onGestureDrive(currY, stepY, velocity[1]);
                            break;
                    }
                    break;
                case SlideEngine.INPUT_MODE_2D:
                    //二维
                    switch (state) {
                        case STATE_MOVING_X://X轴方向移动
                            mSlideEngine.onGestureDrive(currX, 0, stepX, 0, velocity[0], 0);
                            break;
                        case STATE_MOVING_Y://Y轴方向移动
                            mSlideEngine.onGestureDrive(0, currY, 0, stepY, 0, velocity[1]);
                            break;
                    }
                    break;
            }
		}
	}
	
	/**
	 * 通知引擎持有
	 */
	private void holdEngine(){
		if(mSlideEngine != null){
			mSlideEngine.onGestureHold();
			postReleaseCheck();
		}
	}
	
	/**
	 * 通知引擎释放
	 */
	private void releaseEngine(){
		if(mSlideEngine != null){
			//当前加速度
			int velocity[] = getVelocity();
			//释放时给予加速度
			switch(state){
			case STATE_MOVING_X:
				mSlideEngine.onGestureRelease(velocity[0]);
				break;
			case STATE_MOVING_Y:
				mSlideEngine.onGestureRelease(velocity[1]);
				break;
			default:
				mSlideEngine.onGestureRelease(0);
				break;
			}
		}
	}
	
	/**
	 * 手势释放<br/>
	 * 1.通知引擎释放<br/>
	 * 2.设置手势驱动释放状态<br/>
	 * 3.重置其他特殊状态<br/>
	 */
	private void release() {
		//通知engine释放
		releaseEngine();
		//释放状态
		state = STATE_RELEASE;
		//重置永久触摸区域状态
		resetStaticTouchAreaState();
		//重置惯性滑动停住(hold)状态
		holdSliding = false;
		//[ACTION超时释放]停止检测
		handler.removeMessages(MyHandler.HANDLER_RELEASE_CHECK);
	}
	
	/**
	 * [ACTION超时释放]<br>
	 * 开始延时检测
	 */
	private void postReleaseCheck(){
		//[ACTION超时释放]停止检测
		handler.removeMessages(MyHandler.HANDLER_RELEASE_CHECK);
		//重置计数
		releaseCheckCounter = 0;
		//延时执行检测
		handler.sendEmptyMessageDelayed(MyHandler.HANDLER_RELEASE_CHECK, RELEASE_CHECK_DELAY);
	}
	
	/**
	 * [ACTION超时释放]<br>
	 * <br>
	 * 在通知SlideEngine hold后, 开始定时检测GestureDriver是否收到触摸ACTION事件,
	 * 若规定时间内没有触摸事件, 且SlideEngine没有在惯性滑动, 则通知SlideEngine释放,
	 * 防止偶尔出现的ACTION_UP/ACTION_CANCEL事件丢失, 但引擎保持HOLD状态, 无
	 * 限刷新UI的情况.
	 */
	private void releaseCheck(){
		if(mSlideEngine == null)
			return;
		if(!mSlideEngine.isSliding() && releaseCheckCounter <= 0){
			//超时释放引擎
			release();
		}else{
			//继续检测
			postReleaseCheck();
		}
	}
	
	//速度/////////////////////////////////////////////////////////////////
	
	/**
	 * 获得速度捕获器
	 * @return
	 */
	private VelocityTracker getVelocityTracker(){
		if(mVelocityTracker == null){
			mVelocityTracker = VelocityTracker.obtain();
		}
		return mVelocityTracker;
	}
	
	/**
	 * 重置/回收速度捕获器
	 */
	private void resetVelocityTracker(){
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}
	
	/**
	 * 速度捕获器添加数据
	 * @param event
	 */
	private void updateVelocity(MotionEvent event){
		getVelocityTracker().addMovement(event);
	}
	
	/**
	 * 获得速度
	 * @return [0]X轴速度 [1]Y轴速度
	 */
	private int[] getVelocity(){
		int[] velocity = new int[2];
		getVelocityTracker().computeCurrentVelocity(1000);
		velocity[0] = (int)getVelocityTracker().getXVelocity();
		velocity[1] = (int)getVelocityTracker().getYVelocity();
		return velocity;
	}
	
	//永久触摸区域长按计时//////////////////////////////////////////////
	
	private void staticTouchAreaLongPressTimerStart(){
		staticTouchAreaLongPressTimerReset();
		staticTouchAreaLongPressTimer = new Timer();
		staticTouchAreaLongPressTimer.schedule(new TimerTask(){
			@Override
			public void run() {
				staticTouchAreaLongPressHandled = true;
				staticTouchAreaLongPressTimer = null;
				if(mSlideEngine != null)
					mSlideEngine.onStaticTouchAreaLongPress();
			}
		}, STATIC_TOUCH_AREA_LONG_PRESS_DELAY); 
	}
	
	private void staticTouchAreaLongPressTimerReset(){
		//重置计时器
		if(staticTouchAreaLongPressTimer != null){
			staticTouchAreaLongPressTimer.cancel();
			staticTouchAreaLongPressTimer = null;
		}
		//重置标志位
		staticTouchAreaLongPressHandled = false;
	}
	
	/**
	 * 重置永久触摸区域状态
	 */
	private void resetStaticTouchAreaState(){
		staticTouchAreaLongPressTimerReset();
		captureEscapedTouch = false;
	}
	
	//onInterceptTouchEvent//////////////////////////////////////////////
	
	private boolean onInterceptDown(MotionEvent event){
		//[特殊状态]重置跳过拦截状态
		skipIntercepted = false;
		//重置永久触摸区域状态
		resetStaticTouchAreaState();
		//重置坐标
		resetCoordinate(event);
		//重置速度计
		resetVelocityTracker();
		//判断是否有效触摸
		if(isValidTouch(downX, downY))
			state = STATE_DOWN;
		//engine正在惯性滑动时, 触摸并停住控件, 通知engine持有
		if(mSlideEngine != null && mSlideEngine.isSliding()){
			holdEngine();
			holdSliding = true;//[特殊状态]在控件惯性滑动时触摸停住(hold)滑动
		}
		return false;
	}
	
	private boolean onInterceptMove(MotionEvent event) {
		//是否被跳过本次拦截
		if(skipIntercepted)
			return false;
		//计算坐标
		calculateCoordinate(event);
		//判断是否有效滑动, 有效滑动通知engine持有
		if(state == STATE_DOWN){
			checkValidMove();
		}
		//有效滑动后拦截事件
		if(state > STATE_DOWN)
			return true;
		//非有效滑动不拦截事件
		return false;
	}

	private boolean onInterceptUP(MotionEvent event) {
		//是否被跳过本次拦截
		if(skipIntercepted)
			return false;
		//有效滑动状态拦截事件
		if(state > STATE_DOWN){
			return true;
		}
		release();//释放手势
		return false;
	}

	//onTouchEvent//////////////////////////////////////////////
	
	private boolean onTouchDown(MotionEvent event){
		//不处理down事件, 可交由后方的View分发处理, 子View在ViewGroup内部, 而后面的View和ViewGroup平级但是在后方
		//[this.onInterceptTouchEvent] false--> [this.onTouchEvent] false--> [后方的View.onInterceptTouchEvent]...
		
		//在控件惯性滑动时触摸停住(hold)滑动, 若没有子控件处理事件, 则视为未触摸在控件上, 释放引擎hold状态
		if(holdSliding){
			release();//释放手势
		}
		
		//判断是否在永久触摸区域
		if(state == STATE_DOWN && isStaticTouch(getX(event), getY(event))){
			captureEscapedTouch = true;//永久触摸区域在ViewGroup.onTouchEvent拦截到未被处理的事件
			if(mSlideEngine != null){
				staticTouchAreaLongPressTimerStart();//永久触摸区域长按计时开始
				mSlideEngine.onStaticTouchAreaCaptureEscapedTouch();//通知Engine捕获到未处理的事件
			}
			return true;
		}
			
		return false;
	}
	
	private boolean onTouchMove(MotionEvent event){
		//在没有子View处理事件时, 事件会交由本ViewGroup.onTouchEvent处理, 若此处也返回false, 则会交由后方View分发处理
		
		//永久触摸区域在ViewGroup.onTouchEvent拦截到未被处理的事件
		if(captureEscapedTouch){
			//是否按下状态
			if(state == STATE_DOWN){
				//计算坐标
				calculateCoordinate(event);
				//判断是否有效滑动, 若有效滑动则通知持有
				if(checkValidMove()){
					//重置永久触摸区域状态
					resetStaticTouchAreaState();
					//添加坐标点到速度计
					updateVelocity(event);
					//驱动engine
					driveEngine();
				}
				return true;
			}else{
				//重置永久触摸区域状态
				resetStaticTouchAreaState();
			}
		}
		
		//普通处理流程
		if(state > STATE_DOWN){
			//计算坐标
			calculateCoordinate(event);
			//添加坐标点到速度计
			updateVelocity(event);
			//驱动engine
			driveEngine();
			return true;
		}
		return false;
	}
	
	private boolean onTouchUp(MotionEvent event){
		//永久触摸区域点击事件
		if(captureEscapedTouch && !staticTouchAreaLongPressHandled && mSlideEngine != null)
			mSlideEngine.onStaticTouchAreaClick();
			
		release();//释放手势
		return true;
	}
	
	/***********************************************************************************************************
	 * 						public
	 */
	
	/**
	 * 将这个手势驱动和滑动引擎互相绑定<br>
	 * 只需要任意一方调用即可完成双向绑定<br>
	 * 
	 */
	@Override
	public void bind(SlideEngine slideEngine) {
		onBind(slideEngine);
		if(mSlideEngine != null)
			mSlideEngine.onBind(this);
	}

	@Override
	public void onBind(SlideEngine slideEngine) {
		mSlideEngine = slideEngine;
	}

	/**
	 * 判断是否拦截事件<br>
	 * <br>
	 * 此处返回true则后续事件交由本ViewGroup.onTouchEvent处理, 之前处理事件的子View会收到CANCEL事件.<br>
	 * 因此这里通常不截获down事件(仅在惯性滑动时截获), 直到move事件手势滑动距离超过阈值, 才开始拦截事件,
	 * 交由ViewGroup.onTouchEvent处理, 拦截前子View可收到事件, 滑动触发后子View收到CANCEL事件,放弃处理.
	 * 若没有子View处理事件, 则会返回到本ViewGroup.onTouchEvent处理, 若再返回false, 则会交由ViewGroup平级
	 * 但在后方的View分发并处理事件<br>
	 * <br>
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			boolean original = super.onInterceptTouchEvent(ev);
			if(mGestureDriver != null && mGestureDriver.onInterceptTouchEvent(ev))
				return true;
			return original;
		}
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if(isDestroyed)
			return false;
		
		//[ACTION超时释放]计数
		releaseCheckCounter++;
		
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			return onInterceptDown(event);
		case MotionEvent.ACTION_MOVE:
			return onInterceptMove(event);
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			return onInterceptUP(event);
		default:
			break;
		}
		
		return false;
	}

	/**
	 * 处理事件<br>
	 * <br>
	 * onInterceptTouchEvent拦截下事件或没有子View处理事件, 则会执行该onTouchEvent.
	 * 若没有子View处理事件, 此处再返回false, 则会交由和ViewGroup平级的后方的View分发处理<br>
	 * 这里处理滑动触发后(滑动超过阈值)的事件, 进行相应的速度计算, 并向engine输出数据.<br>
	 * <br>
		public boolean onTouchEvent(MotionEvent event) {
			boolean original = super.onTouchEvent(event);
			if(mGestureDriver != null && mGestureDriver.onTouchEvent(event))
				return true;
			return original;
		}
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(isDestroyed)
			return false;
		
		//[ACTION超时释放]计数
		releaseCheckCounter++;
		
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			return onTouchDown(event);
		case MotionEvent.ACTION_MOVE:
			return onTouchMove(event);
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			return onTouchUp(event);
		default:
			break;
		}
		
		return false;
	}

	/**
	 * 获得手势驱动器当前的手势状态
	 * @return
	 */
	@Override
	public int getState(){
		return state;
	}
	
	/**
	 * [特殊]跳过本次拦截<br>
	 * <br>
	 * 用于嵌套结构的SlideView, 内部的SlideView在拦截到事件后, 调用外部SlideView的
	 * GestureDriver.skipIntercepted()方法, 以阻断外部SlideView对本次事件的拦截,
	 * 防止内部SlideView在滑动时, 被外部拦截掉. <br>
	 * <br/>
	 * 用法示例1:<br/>
	 * 外部的SlideEngine.setInnerEngine(SlideEngine)，设置一个内部引擎，内部引擎
	 * 拦截到事件后，会阻断外部引擎的本次拦截<br/>
	 * 用法示例2:<br>
	 * 给里层SlideView的SlideEngine设置一个onGestureHoldListener监听器, 触发时,
	 * 执行外层SlideView的GestureDriver.skipIntercepted();<br>
	 */
	@Override
	public void skipIntercepted(){
		this.skipIntercepted = true;
	}
	
	/**
	 * 销毁
	 */
	@Override
	public void destroy() {
		resetVelocityTracker();
		mSlideEngine = null;
		mContext = null;
		isDestroyed = true;
	}
	
	/*********************************************************
	 * handler
	 */
	
    private final MyHandler handler = new MyHandler(Looper.getMainLooper(), this);

	private static class MyHandler extends WeakHandler<LinearGestureDriver>{

		private static final int HANDLER_RELEASE_CHECK = 1;

		public MyHandler(Looper looper, LinearGestureDriver host) {
			super(looper, host);
		}

		@Override
		protected void handleMessageWithHost(Message msg, LinearGestureDriver host) {
			switch(msg.what){
				case HANDLER_RELEASE_CHECK:
					host.releaseCheck();
					break;
				default:
					break;
			}
		}
	}
	
}
