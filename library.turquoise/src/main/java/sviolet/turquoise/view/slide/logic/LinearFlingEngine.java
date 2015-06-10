package sviolet.turquoise.view.slide.logic;

import android.annotation.SuppressLint;
import android.content.Context;

import sviolet.turquoise.compat.CompatScroller;
import sviolet.turquoise.view.listener.OnVelocityOverflowListener;
import sviolet.turquoise.view.slide.SlideView;

/**
 * 线性滑动引擎(有惯性, 惯性滑动距离由松手时速度决定)<br>
 * <br>
 * @see sviolet.turquoise.view.slide.SlideView
 **************************************************************************************<br>
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

public class LinearFlingEngine extends LinearDragEngine {

	protected CompatScroller mScroller = null;

	protected OnVelocityOverflowListener mOnVelocityOverflowListener;//速度溢出监听器

	protected boolean isVelocityOverflowCallbacked = false;//速度溢出事件已回调

	/**
	 *
	 * @param context ViewGroup上下文
	 * @param slideView 通知刷新的View
	 * @param maxRange 允许滑动最大距离(全程) >=0
	 * @param initPosition 初始位置
	 */
	public LinearFlingEngine(Context context, SlideView slideView, int maxRange, int initPosition) {
		this(context, slideView, maxRange, initPosition, DIRECTION_LEFT_OR_TOP);
	}

	/**
	 * @param context ViewGroup上下文
	 * @param slideView 通知刷新的View
	 * @param maxRange 允许滑动最大距离(全程) >=0
	 * @param initPosition 初始位置
	 * 	@param slidingDirection 滑动输出方向
	 */
	public LinearFlingEngine(Context context, SlideView slideView, int maxRange, int initPosition, int slidingDirection) {
		super(context, slideView, maxRange, initPosition, slidingDirection);
		mScroller = new CompatScroller(mContext);
	}

	/************************************************************
	 * override SlideEngine
	 */

	/**
	 * [手势通知引擎]开始触摸<br>
	 * <br>
	 * 通知SlideView刷新UI, 停止惯性滑动
	 */
	@Override
	protected void handleGestureHold() {
		//清除已回调状态
		isVelocityOverflowCallbacked = false;

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
		//先置为stop状态
		super.handleGestureRelease(velocity);
		//惯性滑动
		fling(-velocity);
	}

	public void destroy() {
		super.destroy();

		mScroller = null;
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
		//滑动范围限制
		switch (checkPositionState(position)) {
			case POSITION_OUT_OF_MIN:
				position = ORIGIN_POSITION;
				break;
			case POSITION_OUT_OF_MAX:
				position = range;
				break;
			default:
				break;
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
			//scroll停止
			if(mScroller == null || mScroller.isFinished()){
				state = STATE_STOP;
			}else if (checkPositionState(position) != POSITION_IN_RANGE){
				state = STATE_STOP;
				callVelocityOverflow();
			}
		}
		return super.isStop();
	}

	/**
	 * [禁用]设置越界拖动
	 */
	@Override
	@Deprecated
	public void setOverScroll(boolean overScrollEnable, float overScrollDamp){
	}

	/***********************************************
	 * private/protected
	 */

	/**
	 * 强制停止scroller
	 */
	protected void abortScroller(){
		if(mScroller != null && !mScroller.isFinished())
			mScroller.abortAnimation();
	}

	/**
	 * 计算速度溢出量
	 */
	@SuppressLint("NewApi")
	protected void callVelocityOverflow(){
		if (!isVelocityOverflowCallbacked && mOnVelocityOverflowListener != null && mScroller != null) {
			isVelocityOverflowCallbacked = true;
			mOnVelocityOverflowListener.onVelocityOverflow(getCurrVelocity());
		}
	}

	/************************************************
	 * public
	 */

	/**
	 * 获得运动位移量<br>
	 * @return
	 */
	public int getOffset(){
		//获取上次位置
		int _lastPosition = getLastPosition();
		//获取当前位置(并记录当前位置为前次位置)
		int _position = getPosition();
		//计算位移量
		return _position - _lastPosition;
	}

	/**
	 * 开始惯性滑动
	 * @param velocity 初始速度
	 */
	public void fling(int velocity){
		//速度为0无需滚动
		if(velocity == 0)
			return;
		//惯性滑动状态
		state = STATE_SLIDING;
		//清除已回调状态
		isVelocityOverflowCallbacked = false;
		//初始化scroller
		abortScroller();
		if(mScroller != null){
			//先行位移, 防止在停止点
			if(velocity > 0) {
				position++;
			}else {
				position--;
			}
			mScroller.fling(position, 0, velocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
		}
		//通知刷新UI
		notifySlideView();
	}

	/**
	 * 速度溢出监听器<br>
	 * <br>
	 * 注意: 无限滑动模式中该监听器无效<Br>
	 * 当惯性滑动到底时, 若速度未降至0, 则回调该监听器, 并传入剩余速度参数<br>
	 *
	 * @param mOnVelocityOverflowListener 监听器
	 */
	public void setOnVelocityOverflowListener(OnVelocityOverflowListener mOnVelocityOverflowListener){
		this.mOnVelocityOverflowListener = mOnVelocityOverflowListener;
	}

	/**
	 * 获得当前惯性滑动速度
	 * @return
	 */
	public int getCurrVelocity(){
		if (mScroller != null) {
			float velocity;
			if (mScroller.getStartX() < mScroller.getCurrX()){
				velocity = mScroller.getCurrVelocity();
			}else{
				velocity = - mScroller.getCurrVelocity();
			}
			return (int) velocity;
		}else {
			return 0;
		}
	}

}
