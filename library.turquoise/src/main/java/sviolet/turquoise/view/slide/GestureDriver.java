package sviolet.turquoise.view.slide;

import android.view.MotionEvent;

/**
 * 手势驱动<br>
 * <br>
 **************************************************************************************<br>
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
 **************************************************************************************<br>
 * 	永久触摸区域::<br>
 * 	若设置了永久触摸区域, 该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent
 * 会返回true以阻止事件向后方传递. 该区域内的触摸事件(down)强制拦截, 后方View无法
 * 捕获触摸事件. 可用于控件边界的把手设计.<br>
 * <br>
 **************************************************************************************<br>
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

public interface GestureDriver {
	
	/**
	 * 主动绑定, 绑定并触发对方的onBind被动绑定<br>
	 * 将这个手势驱动和滑动引擎互相绑定<br>
	 * 
	 * @param SlideEngine
	 */
	public void bind(SlideEngine slideEngine);
	
	/**
	 * 被动绑定<br>
	 * 将这个手势驱动和滑动引擎互相绑定<br>
	 * 
	 * @param slideEngine
	 */
	public void onBind(SlideEngine slideEngine);
	
	/**
	 * 处理是否拦截触摸事件
	 * 
	 * @param event
	 * @return
	 */
	public boolean onInterceptTouchEvent(MotionEvent event);
	
	/**
	 * 处理触摸事件
	 * 
	 * @param event
	 * @return
	 */
	public boolean onTouchEvent(MotionEvent event);
	
	/**
	 * 获得手势驱动器当前的手势状态
	 * @return
	 */
	public int getState();
	
	/**
	 * [特殊]第三者拦截<br>
	 * <br>
	 * 用于嵌套结构的SlideView, 内部的SlideView在拦截到事件后, 调用外部SlideView的
	 * GestureDriver.otherIntercepted()方法, 以阻断外部SlideView对本次事件的拦截,
	 * 防止内部SlideView在滑动时, 被外部拦截掉. <br>
	 * <br>
	 * 一个GestrueDriver被调用otherIntercepted()后, 不再拦截事件, 直到第二次ACTION_DOWN
	 * 事件来临, 拦截会被重置<br>
	 * <br>
	 * [实现提示]<br>
	 * 在onInterceptTouchEvent()中, ACTION_DOWN事件重置拦截标志, ACTION_MOVE/RELEASE
	 * 事件判断拦截标志, 若被拦截则直接返回false, 放弃拦截<br>
	 */
	public void otherIntercepted();
	
	/**
	 * 销毁
	 */
	public void destroy();
	
}
