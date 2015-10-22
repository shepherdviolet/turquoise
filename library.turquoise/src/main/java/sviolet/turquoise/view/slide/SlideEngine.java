package sviolet.turquoise.view.slide;


/**
 * <pre>
 * 屏幕滑动引擎(接口)
 * 
 **************************************************************************************
 * 输出定义::
 * 
 * position方向:
 * DIRECTION_LEFT_OR_TOP				:	手势上/左::递增  手势下/右::递减
 * DIRECTION_RIGHT_OR_BOTTOM	: 	手势上/左::递减  手势下/右::递增
 * 
 * stage方向:
 * DIRECTION_LEFT_OR_TOP				:	手势上/左::递增  手势下/右::递减
 * DIRECTION_RIGHT_OR_BOTTOM	: 	手势上/左::递减  手势下/右::递增
 * </pre>
 * @author S.Violet
 *
 */

public interface SlideEngine {

	//输入模式
	public static final int INPUT_MODE_1D = 1;//一维输入
	public static final int INPUT_MODE_2D = 2;//二维输入
//	public static final int INPUT_MODE_3D = 3;//三维输入

	/**********************************************
	 * 待实现
	 */
	
	/**
     * <pre>
	 * 主动绑定, 绑定并触发对方的onBind被动绑定
	 * 将这个滑动引擎和手势驱动绑定
	 * </pre>
	 * @param gestureDriver
	 */
	public void bind(GestureDriver gestureDriver);
	
	/**
     * <pre>
	 * 被动绑定
	 * 将这个滑动引擎和手势驱动绑定
     * </pre>
	 */
	public void onBind(GestureDriver gestureDriver);

	/**
     * <pre>
	 * 输入模式
	 * INPUT_MODE_1D = 1;//一维输入
	 * INPUT_MODE_2D = 2;//二维输入
	 * 
	 * 通过该方法设置引擎所用的输入模式, 根据输入模式, 选择实现
	 * 两个onGestureDrive中的一个.
     * </pre>
	 */
	public int inputMode();

	/**
     * <pre>
	 * [手势通知引擎]输入触摸状态(二维输入)
	 * 选择二维输入时实现
	 * 
	 * 输入手势运动状态
	 * </pre>
	 * @param x
	 * @param y
	 * @param offsetX
	 * @param offsetY
	 * @param velocityX
	 * @param velocityY
	 */
	public void onGestureDrive(int x, int y, int offsetX, int offsetY, int velocityX, int velocityY);
	
	/**
     * <pre>
	 * [手势通知引擎]输入触摸状态(一维输入)
	 * 选择一维输入时实现
	 * 
	 * 输入手势运动状态
	 * </pre>
	 * @param curr
	 * @param offset
	 * @param velocity
	 */
	public void onGestureDrive(int curr, int offset, int velocity);
	
	/**
     * <pre>
	 * [手势通知引擎]开始触摸
	 * 
	 * 停止惯性滑动, 被手势持有
     * </pre>
	 */
	public void onGestureHold();
	
	/**
     * <pre>
	 * [手势通知引擎]已释放触摸
	 * 
	 * 释放手势持有, View开始惯性滑动
     * </pre>
	 */
	public void onGestureRelease(int velocity);
	
	/**
     * <pre>
	 * [手势通知引擎]永久触摸区域拦截到未处理事件
	 * 
	 * ACTION_DOWN捕获到未处理事件
	 * 
	 * 永久触摸区域::
	 * 若设置了永久触摸区域, 该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent
	 * 会返回true以阻止事件向后方传递. 该区域内的触摸事件(down)强制拦截, 后方View无法
	 * 捕获触摸事件. 可用于控件边界的把手设计.
     * </pre>
	 */
	public void onStaticTouchAreaCaptureEscapedTouch();
	
	/**
     * <pre>
	 * [手势通知引擎]永久触摸区域点击事件
	 * 
	 * ACTION_UP时触发, 永久触摸区域的触摸事件未发生有效位移
	 * 
	 * 永久触摸区域::
	 * 若设置了永久触摸区域, 该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent
	 * 会返回true以阻止事件向后方传递. 该区域内的触摸事件(down)强制拦截, 后方View无法
	 * 捕获触摸事件. 可用于控件边界的把手设计.
     * </pre>
	 */
	public void onStaticTouchAreaClick();
	
	/**
     * <pre>
	 * [手势通知引擎]永久触摸区域长按事件
	 * 
	 * 长按计时器触发, 且永久触摸区域的触摸事件未发生有效位移
	 * 
	 * 永久触摸区域::
	 * 若设置了永久触摸区域, 该区域内, 若没有子View捕获事件, ViewGroup.onTouchEvent
	 * 会返回true以阻止事件向后方传递. 该区域内的触摸事件(down)强制拦截, 后方View无法
	 * 捕获触摸事件. 可用于控件边界的把手设计.
     * </pre>
	 */
	public void onStaticTouchAreaLongPress();
	
	/**
	 * 是否在滑动状态
	 * @return
	 */
	public boolean isSliding();

	/**
     * <pre>
	 * 添加一个内部引擎
     * 对应setParentEngine()
     * 作用:
     * 1.内部引擎拦截到事件后, 会调用外部引擎对应驱动的skipIntercept()方法,
     *      阻断外部引擎的本次事件拦截, 防止内部控件滑动时被外部拦截
     * </pre>
     *
	 * @param slideEngine 内部引擎
	 */
	public void addInnerEngine(SlideEngine slideEngine);

    /**
     * <pre>
     * 设置外部引擎
     * 对应addInnerEngine()
     * </pre>
     * @param slideEngine 外部引擎
     */
    public void setParentEngine(SlideEngine slideEngine);

    /**
     * 获得外部引擎
     *
     * @return 外部引擎
     */
    public SlideEngine getParentEngine();

    /**
     * 获得对应的手势驱动器
     * @return
     */
    public GestureDriver getGestureDriver();

    /**
     * <pre>
     * [特殊]跳过本次拦截
     * 
     * 用于嵌套结构的SlideView, 内部的SlideView在拦截到事件后, 调用外部SlideView的
     * GestureDriver.skipIntercepted()方法, 以阻断外部SlideView对本次事件的拦截,
     * 防止内部SlideView在滑动时, 被外部拦截掉. 
     * 
     * 一个GestrueDriver被调用skipIntercepted()后, 不再拦截事件, 直到第二次ACTION_DOWN
     * 事件来临, 状态会被重置
     * 
     * [实现提示]
     * 调用自身对应GestureDriver.skipIntercepted()方法, 并调用外部引擎的skipIntercepted()
     * 方法.
     * </pre>
     */
    public void skipIntercepted();

	/**
	 * 销毁
	 */
	public void destroy();
	
}
