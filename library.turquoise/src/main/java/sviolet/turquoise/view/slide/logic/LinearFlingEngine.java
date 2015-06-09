package sviolet.turquoise.view.slide.logic;

import sviolet.turquoise.view.slide.SlideView;
import android.content.Context;
import android.widget.Scroller;

/**
 * 线性滑动引擎(有惯性, 惯性滑动距离由松手时速度决定)<br>
 * <br>
 * @see sviolet.turquoise.view.slide.SlideView<br>
 **************************************************************************************<br>
 * 刷新UI/输出显示示例:<br>
 * SlideView::<br>
 * <br>
	//实现通知刷新UI接口
	@Override
	public void notifyRefresh() {
		postInvalidate();
	}
	
	//常用输出方法(0 -> range)
	@Override
	public void computeScroll() {
		if(mSlideEngine != null){
			scrollTo(mSlideEngine.getPosition(), 0);
			if(!mSlideEngine.isStop())
				postInvalidate();
		}
	}
	
	//常用输出方法2(-range -> 0)
	@Override
	public void computeScroll() {
		if(mSlideEngine != null){
			scrollTo(mSlideEngine.getPosition() - mSlideEngine.getRange(), 0);
			if(!mSlideEngine.isStop())
				postInvalidate();
		}
	}
	
	//其他输出方法
//	@Override
//	protected void onDraw(Canvas canvas) {
//		//绘制View
//		super.onDraw(canvas);
//		//滑动至engine所在位置
//		if(mSlideEngine != null){
//			scrollTo(mSlideEngine.getPosition(), 0);
//			//判断是否停止
//			if(!mSlideEngine.isStop())
//				postInvalidate();
//		}
//	}
 * <br>
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

	protected Scroller mScroller = null;
	
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
		mScroller = new Scroller(mContext);
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
		
		//速度为0无需滚动
		if(velocity == 0)
			return;
		//惯性滑动状态
		state = STATE_SLIDING;
		//初始化scroller
		abortScroller();
		if(mScroller != null){
			if(infiniteRange){
				mScroller.fling(position, 0, -velocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
			}else{
				mScroller.fling(position, 0, -velocity, 0, ORIGIN_POSITION, range, 0, 0);
			}
		}
		//通知刷新UI
		notifySlideView();
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
			}
		}
		return super.isStop();
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
	
}
