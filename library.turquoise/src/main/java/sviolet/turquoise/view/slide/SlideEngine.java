package sviolet.turquoise.view.slide;


/**
 * 屏幕滑动引擎(接口)<br>
 * <br>
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

public interface SlideEngine {
	
	/**********************************************
	 * 待实现
	 */
	
	/**
	 * 主动绑定, 绑定并触发对方的onBind被动绑定<br>
	 * 将这个滑动引擎和手势驱动绑定<br>
	 * 
	 * @param gestureDriver
	 */
	public void bind(GestureDriver gestureDriver);
	
	/**
	 * 被动绑定<br>
	 * 将这个滑动引擎和手势驱动绑定<br>
	 */
	public void onBind(GestureDriver gestureDriver);
	
	/**
	 * [手势通知引擎]输入触摸状态(二维)<br>
	 * <br>
	 * 输入手势运动状态<br>
	 * 
	 * @param x
	 * @param y
	 * @param offsetX
	 * @param offsetY
	 * @param velocityX
	 * @param velocityY
	 */
	public void onGestureDrive(int x, int y, int offsetX, int offsetY, int velocityX, int velocityY);
	
	/**
	 * [手势通知引擎]输入触摸状态(一维)<br>
	 * <br>
	 * 输入手势运动状态<br>
	 * 
	 * @param x
	 * @param y
	 * @param offsetX
	 * @param offsetY
	 * @param velocityX
	 * @param velocityY
	 */
	public void onGestureDrive(int curr, int offset, int velocity);
	
	/**
	 * [手势通知引擎]开始触摸<br>
	 * <br>
	 * 停止惯性滑动, 被手势持有<br>
	 */
	public void onGestureHold();
	
	/**
	 * [手势通知引擎]已释放触摸<br>
	 * <br>
	 * 释放手势持有, View开始惯性滑动<br>
	 */
	public void onGestureRelease(int velocity);
	
	/**
	 * [手势通知引擎]永久触摸区域拦截到未处理事件<br>
	 * <br>
	 * ACTION_DOWN捕获到未处理事件<br>
	 * <br>
	 * 	永久触摸区域::<br>
	 * 	若设置了永久触摸区域, 该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent
	 * 会返回true以阻止事件向后方传递. 该区域内的触摸事件(down)强制拦截, 后方View无法
	 * 捕获触摸事件. 可用于控件边界的把手设计.<br>
	 */
	public void onStaticTouchAreaCaptureEscapedTouch();
	
	/**
	 * [手势通知引擎]永久触摸区域点击事件<br>
	 * <br>
	 * ACTION_UP时触发, 永久触摸区域的触摸事件未发生有效位移<br>
	 * <br>
	 * 	永久触摸区域::<br>
	 * 	若设置了永久触摸区域, 该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent
	 * 会返回true以阻止事件向后方传递. 该区域内的触摸事件(down)强制拦截, 后方View无法
	 * 捕获触摸事件. 可用于控件边界的把手设计.<br>
	 */
	public void onStaticTouchAreaClick();
	
	/**
	 * [手势通知引擎]永久触摸区域长按事件<br>
	 * <br>
	 * 长按计时器触发, 且永久触摸区域的触摸事件未发生有效位移<br>
	 * <br>
	 * 	永久触摸区域::<br>
	 * 	若设置了永久触摸区域, 该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent
	 * 会返回true以阻止事件向后方传递. 该区域内的触摸事件(down)强制拦截, 后方View无法
	 * 捕获触摸事件. 可用于控件边界的把手设计.<br>
	 */
	public void onStaticTouchAreaLongPress();
	
	/**
	 * 是否在滑动状态
	 * @return
	 */
	public boolean isSliding();
	
	/**
	 * [特殊]第三者拦截<br>
	 * <br>
	 * 	用于嵌套结构的SlideView, 内部的SlideView在拦截到事件后, 调用外部SlideView的
	 * 此方法, 以阻断外部SlideView对本次事件的拦截,防止内部SlideView在滑动时, 被外部
	 * 拦截掉. <br>
	 * <br>
	 * 代码实现见sviolet.turquoise.view.slide.GestureDriver.otherIntercepted()<br>
	 * <br>
	 */
	public void otherIntercepted();
	
	/**
	 * 销毁
	 */
	public void destroy();
	
}
