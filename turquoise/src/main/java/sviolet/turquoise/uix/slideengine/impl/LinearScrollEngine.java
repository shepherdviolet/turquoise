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

package sviolet.turquoise.uix.slideengine.impl;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import sviolet.turquoise.common.compat.CompatScroller;
import sviolet.turquoise.uix.slideengine.abs.SlideView;

/**
 * 
 * 线性滑动引擎(有惯性, 惯性滑动至停止点)<br>
 * <Br/>
 * 基本设置:<br/>
 * setMaxRange()<br/>
 * setInitPosition()<br/>
 * setSlidingDirection()<br/>
 * setStageDuration()<br/>
 * <br>
 * {@link SlideView} <br/>
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

public class LinearScrollEngine extends LinearDragEngine {
	
	private static final int DEF_STAGE_DURATION = 700;
	
	protected int stageDuration = DEF_STAGE_DURATION;//一个阶段的全程惯性滑动时间
	
	protected CompatScroller mScroller = null;

	private OnClickListener mOnStaticTouchAreaTouchListener;//永久触摸区域触摸事件监听器
	private OnClickListener mOnStaticTouchAreaClickListener;//永久触摸区域点击事件监听器
	private OnClickListener mOnStaticTouchAreaLongPressListener;//永久触摸区域长按事件监听器
	
	protected boolean staticTouchAreaFeedbackEnabled = false;//永久触摸区域触摸反馈效果开关
	protected int staticTouchAreaFeedbackRange = 0;//永久触摸区域触摸反馈效果幅度(区分正负方向)
	protected boolean isStaticTouchAreaFeedbackRunning = false;//永久触摸区域触摸反馈效果是否正在进行中
	
	protected boolean scrollTargetLock = false;//滚动目标锁
	
	/**
	 * @param context ViewGroup上下文
	 * @param slideView 通知刷新的View
	 */
	public LinearScrollEngine(Context context, SlideView slideView){
		super(context, slideView);
		mScroller = new CompatScroller(mContext);
	}

	/******************************************************
	 * settings
	 */

	/**
     * [基本设置]<br/>
	 * 设置一个阶段的全程滑动时间(ms)(默认DEF_STAGE_DURATION)
	 *
	 * @param stageDuration 一个阶段的全程滑动时间(ms) [0, INTEGER_MAX)
	 */
	public void setStageDuration(int stageDuration) {
        if (stageDuration >= 0) {
            this.stageDuration = stageDuration;
        } else {
            this.stageDuration = 0;
        }
	}

    /**
     * 设置永久触摸区域触摸反馈效果<br>
     * <br>
     * 永久触摸区域::<br>
     * 若设置了永久触摸区域, 该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent
     * 会返回true以阻止事件向后方传递. 该区域内的触摸事件(down)强制拦截, 后方View无法
     * 捕获触摸事件. 可用于控件边界的把手设计.<br>
     * <br>
     * range反馈幅度::<br>
     * 永久触摸区域捕获到未被处理的事件时, 向一个方向滑动一小段距离(通常是滑出)的效果.
     * range需要指定方向(正负值), 和幅度大小.<br>
     *
     * @param enabled 开关
     * @param range 反馈幅度(带正负方向) 单位px
     */
    public void setStaticTouchAreaFeedback(boolean enabled, int range){
        this.staticTouchAreaFeedbackEnabled = enabled;
        this.staticTouchAreaFeedbackRange = range;
    }

    /**
     * 设置永久触摸区域触摸事件监听器<br>
     * <br>
     * ACTION_Down时触发, 永久触摸区域触摸触发<br>
     * <br>
     * 永久触摸区域::<br>
     * 若设置了永久触摸区域, 该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent
     * 会返回true以阻止事件向后方传递. 该区域内的触摸事件(down)强制拦截, 后方View无法
     * 捕获触摸事件. 可用于控件边界的把手设计.<br>
     *
     * @param listener
     */
    public void setOnStaticTouchAreaTouchListener(OnClickListener listener){
        this.mOnStaticTouchAreaTouchListener = listener;
    }

    /**
     * 设置永久触摸区域单击事件监听器<br>
     * <br>
     * ACTION_UP时触发, 永久触摸区域的触摸事件未发生有效位移<br>
     * <br>
     * 永久触摸区域::<br>
     * 若设置了永久触摸区域, 该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent
     * 会返回true以阻止事件向后方传递. 该区域内的触摸事件(down)强制拦截, 后方View无法
     * 捕获触摸事件. 可用于控件边界的把手设计.<br>
     *
     * @param listener
     */
    public void setOnStaticTouchAreaClickListener(OnClickListener listener){
        this.mOnStaticTouchAreaClickListener = listener;
    }

    /**
     * 设置永久触摸区域长按事件监听器<br>
     * <br>
     * 长按计时器触发, 且永久触摸区域的触摸事件未发生有效位移<br>
     * <br>
     * 永久触摸区域::<br>
     * 若设置了永久触摸区域, 该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent
     * 会返回true以阻止事件向后方传递. 该区域内的触摸事件(down)强制拦截, 后方View无法
     * 捕获触摸事件. 可用于控件边界的把手设计.<br>
     *
     * @param listener
     */
    public void setOnStaticTouchAreaLongPressListener(OnClickListener listener){
        this.mOnStaticTouchAreaLongPressListener = listener;
    }

	/******************************************************
	 * override SlideEngine
	 */
	
	/**
	 * [手势通知引擎]输入触摸状态(一维)<br>
	 * <br>
	 * 输入手势运动状态<br>
	 * 
	 */
	@Override
	protected void handleGestureDrive(int curr, int offset, int velocity) {
		//结束永久触摸区域反馈效果状态
		isStaticTouchAreaFeedbackRunning = false;
		
		//滚动目标锁定后, 无视手势驱动
		if(scrollTargetLock) {
            return;
        }
		
		super.handleGestureDrive(curr, offset, velocity);
	}
	
	/**
	 * [手势通知引擎]开始触摸<br>
	 * <br>
	 * 通知SlideView刷新UI, 停止惯性滑动
	 */
	@Override
	protected void handleGestureHold() {
		//结束永久触摸区域反馈效果状态
		isStaticTouchAreaFeedbackRunning = false;
		
		//滚动目标锁定后, 无视手势驱动
		if(scrollTargetLock) {
            return;
        }
		
		super.handleGestureHold();
		
		//中止scroller
		abortScroller();
	}
	
	/**
	 * [手势通知引擎]已释放触摸<br>
	 * <br>
	 * 释放后进入惯性滑动<br>
	 * 
	 */
	@Override
	protected void handleGestureRelease(int velocity) {
		//结束永久触摸区域反馈效果状态
		isStaticTouchAreaFeedbackRunning = false;
		
		//滚动目标锁定后, 无视手势驱动
		if(scrollTargetLock) {
            return;
        }
		
		//先置为stop状态
		super.handleGestureRelease(velocity);
		
		//计算惯性滑动目标位置
		int target = calculateSlideTarget(velocity);
		//滚动至目标位置
		scrollToPosition(target);
	}
	
	/**
	 * [手势通知引擎]永久触摸区域拦截到未处理事件<br>
	 */
	@Override
	public void onStaticTouchAreaCaptureEscapedTouch() {
		//触摸监听
		if(mOnStaticTouchAreaTouchListener != null) {
            try {
                mOnStaticTouchAreaTouchListener.onClick((View) getSlideView());
            } catch (ClassCastException e) {
                mOnStaticTouchAreaTouchListener.onClick(null);
            }
        }
		//允许反馈效果, 且当前运动已停止
		if(staticTouchAreaFeedbackEnabled && isStop()){
			//激活永久触摸区域反馈效果状态
			isStaticTouchAreaFeedbackRunning = true;
			//计算惯性滑动目标位置
			int target = position + staticTouchAreaFeedbackRange;
			//滚动至目标位置(不锁定)
			scrollToPosition(target, stageDuration, false);
		}
	}
	
	/**
	 * [手势通知引擎]永久触摸区域点击事件<br>
	 */
	@Override
	public void onStaticTouchAreaClick() {
		if(mOnStaticTouchAreaClickListener != null){
			try{
				mOnStaticTouchAreaClickListener.onClick((View)getSlideView());
			}catch (ClassCastException e){
				mOnStaticTouchAreaClickListener.onClick(null);
			}
		}
	}
	
	/**
	 * [手势通知引擎]永久触摸区域长按事件<br>
	 */
	@Override
	public void onStaticTouchAreaLongPress() {
		if(mOnStaticTouchAreaLongPressListener != null){
			try{
				mOnStaticTouchAreaLongPressListener.onClick((View)getSlideView());
			}catch (ClassCastException e){
				mOnStaticTouchAreaLongPressListener.onClick(null);
			}
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		isStaticTouchAreaFeedbackRunning = false;
		mScroller = null;
		scrollTargetLock = false;
	}
	
	/********************************************************
	 * override LinearDragEngine
	 */
	
	/**
	 * 获得当前运动位置
	 * @return
	 */
	@Override
	public int getPosition() {
		//惯性滑动状态, 计算当前位置
		if(state == STATE_SLIDING && mScroller != null){
			mScroller.computeScrollOffset();
			position = mScroller.getCurrX();
		}
		return super.getPosition();
	}
	
	/**
	 * 检查运动是否已停止, 若停止则置为停止状态, 并返回true
	 * @return
	 */
	@Override
	public boolean isStop(){
		if(state != STATE_HOLDING){
			//若滚动目标锁定, 到达停止点不停止运动
			if(!scrollTargetLock && isOnArrestPoint()){//到达边界为止, 判断为结束, 目标锁定时跳过
				abortScroller();
				state = STATE_STOP;
				return super.isStop();
			}else if(mScroller != null && mScroller.isFinished()){//Scroll停止
				//若滚动目标锁定状态
				if(scrollTargetLock){
					//滚动停止后解除[滚动目标锁]
					scrollTargetLock = false;
					//判断手势驱动器状态
					if(mGestureDriver != null && mGestureDriver.getState() > LinearGestureDriver.STATE_DOWN){
						//手势驱动器down/move状态则置为拖动状态, 不停止
						state = STATE_HOLDING;
						return super.isStop();
					}else{
						//手势驱动器release状态则置为停止状态
						state = STATE_STOP;
						return super.isStop();
					}
				}
				//若在进行永久触摸区域反馈效果则停止运动
				if(isStaticTouchAreaFeedbackRunning){
					state = STATE_STOP;
					return super.isStop();
				}
				//重新计算惯性滑动目标
				int target = calculateSlideTarget(0);
				//滚动至目标位置
				scrollToPosition(target);
			}
		}
		return super.isStop();
	}
	
	/**
	 * 设置是否无限滑动距离(无边界)<br>
	 * <br>
	 * LinearScrollEngine禁用该方法<Br>
	 * 
	 * @param value true:无限距离(无边界)
	 */
	@Deprecated
	@Override
	public void setInfiniteRange(boolean value) {
		
	}
	
	/***********************************************
	 * public
	 */
	
	/**
	 * 滚动到指定阶段, 不锁定目标
	 * 
	 * @param targetStage 目标阶段
	 */
	public void scrollToStage(int targetStage){
		scrollToPosition(getPositionOfStage(targetStage));
	}
	
	/**
	 * 滚动到指定阶段
	 * 
	 * @param targetStage 目标阶段
	 * @param targetLock 目标锁定(锁定后, 滚动到目标之前控件无法被拖动/改变目标)
	 */
	public void scrollToStage(int targetStage, boolean targetLock){
		scrollToPosition(getPositionOfStage(targetStage), targetLock);
	}
	
	/**
	 * 滚动到指定位置(滚动时间由滚动距离计算得到), 不锁定目标
	 * 
	 * @param targetPosition
	 */
	public void scrollToPosition(int targetPosition){
		scrollToPosition(targetPosition, false);
	}
	
	/**
	 * 滚动到指定位置(滚动时间由滚动距离计算得到)
	 * 
	 * @param targetPosition
	 * @param targetLock 目标锁定(锁定后, 滚动到目标之前控件无法被拖动/改变目标)
	 */
	public void scrollToPosition(int targetPosition, boolean targetLock){
		//通过滑动距离计算滚动时间
		int duration = calculateSlideDuration(targetPosition);
		scrollToPosition(targetPosition, duration, targetLock);
	}
	
	/**
	 * 滚动到指定位置(指定滚动时间)
	 * 
	 * @param targetPosition 目标位置
	 * @param duration 滚动时间
	 * @param targetLock 目标锁定(锁定后, 滚动到目标之前控件无法被拖动/改变目标)
	 */
	public void scrollToPosition(int targetPosition, int duration, boolean targetLock){
		//无需滚动
		if(targetPosition == position) {
            return;
        }
		//目标已被锁定, 取消滚动
		if(scrollTargetLock) {
            return;
        }
		
		//惯性滑动状态
		state = STATE_SLIDING;
		//初始化scroller
		abortScroller();
		//提前位移(防止由于getPosition过快, 当前位置尚在停止点, 判断为stop停止刷新UI)
		if(targetPosition > position){
			position++;
		}else{
			position--;
		}
		//目标锁定
		if(targetLock) {
            scrollTargetLock = true;
        }
		//开始滚动
		if(mScroller != null) {
            mScroller.startScroll(position, 0, targetPosition - position, 0, duration);
        }
		//通知刷新UI
		notifySlideView();
	}
	
	/***********************************************
	 * private/protected
	 */
	
	/**
	 * 强制停止scroller
	 */
	protected void abortScroller(){
		if(mScroller != null && !mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
	}
	
	/**
	 * 计算惯性滑动目标位置
	 * 
	 * @param velocity
	 * @return
	 */
	protected int calculateSlideTarget(int velocity) {
		int positionState = checkPositionState(position);
		int target = ORIGIN_POSITION;
		
		switch(positionState){
		case POSITION_OUT_OF_MIN:
			target = ORIGIN_POSITION;
			break;
		case POSITION_OUT_OF_MAX:
			target = range;
			break;
		default:
			//根据速度方向判断目标
			target = calculateSlideTargetByVelocity(velocity);
			break;
		}
		return target;
	}
	
	/**
	 * 根据速度计算惯性滑动目标位置(被calculateSlideTarget调用)
	 * 
	 * @param velocity
	 * @return
	 */
	protected int calculateSlideTargetByVelocity(int velocity){
		int target = ORIGIN_POSITION;
		if(velocity == 0){
			//速度为0根据距离判断目标
			if(Math.abs(position - range) < Math.abs(position - 0)) {
                target = range;
            } else {
                target = ORIGIN_POSITION;
            }
		}else if(velocity < 0){
			target = range;
		}else{
			target = ORIGIN_POSITION;
		}
		return target;
	}
	
	/**
	 * 计算惯性滑动时间
	 * 
	 * @param target
	 * @return
	 */
	protected int calculateSlideDuration(int target) {
		if(range > 0) {
            return (int) ((float) stageDuration * (float) Math.abs(position - target) / (float) range);
        } else {
            return stageDuration;
        }
	}
	
}
