package sviolet.turquoise.view.slide.view;

import sviolet.turquoise.view.listener.OnInitCompleteListener;
import sviolet.turquoise.view.listener.OnSlideStopListener;
import sviolet.turquoise.view.slide.SlideView;
import sviolet.turquoise.view.slide.logic.LinearGestureDriver;
import sviolet.turquoise.view.slide.logic.LinearScrollEngine;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.LinearLayout;

/**
 * 滑动抽屉(LinearLayout)<br>
 * <br>
 * 从屏幕侧边滑出的控件<br>
 * <br>
 * Provider : @see sviolet.turquoise.view.slide.view.DrawerProvider<br>
 *********************************************************************<br>
 * 设置滑动参数::<br>
 * Activity.onCreate中::<br>
 * <br>
		layout = (LinearLayoutDrawer) findViewById(R.id.layout);
		layout
			.setSlideScrollDirection(LinearLayoutDrawer.DIRECTION_LEFT)//设置抽屉方向
			.setSlideScrollDuration(500)//设置惯性滑动事件
			.setSlideHandleWidth(30)//设置把手宽度(dp)
			.applySlideSetting();//应用设置
 * <br>
 *********************************************************************<br>
 * XML配置::<br>
 * <br>
    <!-- 抽屉控件 -->
    <!-- scrollX/scrollY设置一个很大的值, 让子控件消失, 由engine的初始位置决定控件初始位置 -->
    <!-- [必须]background 设置透明背景, 使得ViewGroup得以绘制, 否则无法滑动 -->
    <!-- [必须]若子控件不处理触摸事件的(例TextView), 则必须设置子控件clickable="true", 否则无法捕获触摸事件, 无法滑动 -->
    <sviolet.turquoise.view.slide.view.LinearLayoutDrawer
        android:id="@+id/layout"
        android:layout_width="300dp"
        android:layout_height="match_parent"
		android:background="#00000000"
        android:scrollX="10000dp"
        android:scrollY="10000dp"
        android:orientation="horizontal">

        <!-- 内容 -->
        <ListView 
		    android:id="@+id/listview"
		    android:layout_width="match_parent"
		    android:layout_height="match_parent"
			android:background="#209090"/>

    </sviolet.turquoise.view.slide.view.LinearLayoutDrawer>
 * <br>
 * <br>
 * 类似如下情况必须设置clickable="true"::<br>
        <TextView 
		    android:id="@+id/textview"
		    android:layout_width="match_parent"
		    android:layout_height="match_parent"
			android:clickable="true"/>
 * 
 * @author S.Violet
 *
 */

public class LinearLayoutDrawer extends LinearLayout implements SlideView{
	
	public LinearLayoutDrawer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LinearLayoutDrawer(Context context) {
		super(context);
	}
	
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
	
	private LayoutDrawerProvider mDrawerProvider = new LayoutDrawerProvider(this);
	
	/***********************************************************
	 * setting / init
	 */
	
	/**
	 * 设置滑动抽屉方向<br>
	 * <br>
	 * 默认{@link #DIRECTION_LEFT}<br>
	 * <br>
	 * {@link #DIRECTION_TOP} 抽屉从顶部拉出<br>
	 * 	{@link #DIRECTION_BOTTOM} 抽屉从底部拉出<br>
	 * 	{@link #DIRECTION_LEFT} 抽屉从左边拉出<br>
	 * 	{@link #DIRECTION_RIGHT} 抽屉从右边拉出<br>
	 * 
	 * @param scrollDirection 抽屉方向
	 */
	public LinearLayoutDrawer setSlideScrollDirection(int scrollDirection){
		mDrawerProvider.setSlideScrollDirection(scrollDirection);
		return this;
	}
	
	/**
	 * 设置抽屉宽度(单位 dp), 即可滑动距离<br>
	 * <br>
	 * 默认{@link #DRAWER_WIDTH_MATCH_PARENT} = {@value #DRAWER_WIDTH_MATCH_PARENT}<br>
	 * <br>
	 * 默认抽屉宽度 = 控件的宽度或高度<br>
	 * 
	 * @param drawerWidth
	 * @return
	 */
	public LinearLayoutDrawer setSlideDrawerWidth(int drawerWidth){
		mDrawerProvider.setSlideDrawerWidth(drawerWidth);
		return this;
	}
	
	/**
	 * 设置把手宽度(单位 dp)<br>
	 * <br>
	 * 默认{@value #DEF_HANDLE_WIDTH}<br>
	 * <br>
	 * 把手是抽屉收起来后用于拉出抽屉的一块特殊范围, 由GestureDriver的永久触摸区域实现, 
	 * 即控件边界处宽handleWidth的区域, 触摸起点在这个区域内, 可拉出抽屉.<br>
	 * 例如DIRECTION_RIGHT的抽屉, handleWidth=30, 则该控件右边界宽30dp的范围内开始
	 * 触摸, 向左滑动即可拉出抽屉<br>
	 * 
	 * @param handleWidth
	 * @return
	 */
	public LinearLayoutDrawer setSlideHandleWidth(int handleWidth){
		mDrawerProvider.setSlideHandleWidth(handleWidth);
		return this;
	}
	
	/**
	 * 设置惯性滑动时间(全程) 单位ms<br>
	 * <br>
	 * 默认{@value #DEF_SCROLL_DURATION}<Br>
	 * <br>
	 * 抽屉从收起状态到拉出状态惯性滑动所需的时间<Br>
	 * 
	 * @param scrollDuration
	 * @return
	 */
	public LinearLayoutDrawer setSlideScrollDuration(int scrollDuration){
		mDrawerProvider.setSlideScrollDuration(scrollDuration);
		return this;
	}
	
	/**
	 * 设置抽屉初始状态:收起/拉出<br>
	 * <br>
	 * 默认{@link #STAGE_PUSH_IN}<br>
	 * <br>
	 * {@link #STAGE_PUSH_IN}:抽屉初始状态:收起<br>
	 * {@link #STAGE_PULL_OUT}:抽屉初始状态:拉出<br>
	 * 
	 * @param initStage
	 * @return
	 */
	public LinearLayoutDrawer setSlideInitStage(boolean initStage){
		mDrawerProvider.setSlideInitStage(initStage);
		return this;
	}
	
	/**
	 * 设置抽屉是否允许越界拖动<br>
	 * <br>
	 * 默认{@value #DEF_OVER_SCROLL_ENABLED}<br>
	 * 
	 * @param overScrollEnabled
	 * @return
	 */
	public LinearLayoutDrawer setSlideOverScrollEnabled(boolean overScrollEnabled){
		mDrawerProvider.setSlideOverScrollEnabled(overScrollEnabled);
		return this;
	}
	
	/**
	 * 设置抽屉越界拖动阻尼[0,1)<br>
	 * <br>
	 * 默认{@value #DEF_OVER_SCROLL_DAMP}<br>
	 * <br>
	 * 越界阻尼越大, 越界时拖动越慢<br>
	 * 
	 * @param overScrollDamp
	 * @return
	 */
	public LinearLayoutDrawer setSlideOverScrollDamp(float overScrollDamp){
		mDrawerProvider.setSlideOverScrollDamp(overScrollDamp);
		return this;
	}
	
	/**
	 * 设置把手触摸反馈效果(按在把手上抽屉弹出一部分)<br>
	 * 
	 * @param enabled
	 * @return
	 */
	public LinearLayoutDrawer setHandleFeedback(boolean enabled){
		mDrawerProvider.setHandleFeedback(enabled);
		return this;
	}
	
	/**
	 * 设置把手触摸反馈效果幅度, 单位dp(按在把手上抽屉弹出一部分)<br>
	 * <br>
	 * 默认{@link #FEEDBACK_RANGE_HALF_HANDLE_WIDTH}: 幅度 = 把手宽度(handleWidth) / 2<br>
	 * <br>
	 * 此处的幅度不同于LinearSlideEngine的永久触摸区域反馈幅度. 此处数值为正数, 单位为dp, 
	 * 无需考虑方向(方向由抽屉方向决定), 而LinearSlideEngine的反馈幅度需要区别正负方向, 
	 * 单位为像素px<br>
	 * 
	 * @param range 反馈效果幅度 >=0
	 * @return
	 */
	public LinearLayoutDrawer setHandleFeedbackRange(int range){
		mDrawerProvider.setHandleFeedbackRange(range);
		return this;
	}

	/**
	 * 设置把手触摸事件监听器<br>
	 * <br>
	 * 由于该监听事件回调后, 还会触发SlideEngine手势释放事件, 导致滚动目标重定向,
	 * 该监听事件回调中若需要pullOut/pushIn, 必须使用强制执行方式<br>
	 *
	 * @param listener
	 */
	public LinearLayoutDrawer setOnHandleTouchListener(OnClickListener listener){
		mDrawerProvider.setOnHandleTouchListener(listener);
		return this;
	}

	/**
	 * 设置把手点击事件监听器<br>
	 * <br>
	 * 由于该监听事件回调后, 还会触发SlideEngine手势释放事件, 导致滚动目标重定向, 
	 * 该监听事件回调中若需要pullOut/pushIn, 必须使用强制执行方式<br>
	 * 
	 * @param listener
	 */
	public LinearLayoutDrawer setOnHandleClickListener(OnClickListener listener){
		mDrawerProvider.setOnHandleClickListener(listener);
		return this;
	}
	
	/**
	 * 设置把手长按事件监听器<br>
	 * <br>
	 * 由于该监听事件回调后, 还会触发SlideEngine手势释放事件, 导致滚动目标重定向, 
	 * 该监听事件回调中若需要pullOut/pushIn, 必须使用强制执行方式<br>
	 * 
	 * @param listener
	 */
	public LinearLayoutDrawer setOnHandleLongPressListener(OnClickListener listener){
		mDrawerProvider.setOnHandleLongPressListener(listener);
		return this;
	}

	/**
	 * 设置滑动停止监听器
	 *
	 * @param listener
	 * @return
	 */
	public LinearLayoutDrawer setOnSlideStopListener(OnSlideStopListener listener){
		mDrawerProvider.setOnSlideStopListener(listener);
		return this;
	}

	/**
	 * 设置持有监听器<br>
	 * 当手势滑动有效距离, 触发Engine拖动时触发
	 * @param listener
	 * @return
	 */
	public LinearLayoutDrawer setOnGestureHoldListener(OnClickListener listener){
		mDrawerProvider.setOnGestureHoldListener(listener);
		return this;
	}

	/**
	 * 设置初始化完成监听器
	 * @param mOnInitCompleteListener
	 * @return
	 */
	public LinearLayoutDrawer setOnInitCompleteListener(OnInitCompleteListener mOnInitCompleteListener){
		mDrawerProvider.setOnInitCompleteListener(mOnInitCompleteListener);
		return this;
	}

	/**
	 * 应用滑动设置(使setSlide...生效)<br>
	 * 非立即生效<br/>
	 */
	public void applySlideSetting(){
		postInitSlide();
	}

	/*******************************************************
	 * override
	 */
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean original = super.onInterceptTouchEvent(ev);
		if(mDrawerProvider.getGestureDriver() != null && mDrawerProvider.getGestureDriver().onInterceptTouchEvent(ev))
			return true;
		return original;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean original = super.onTouchEvent(event);
		if(mDrawerProvider.getGestureDriver() != null && mDrawerProvider.getGestureDriver().onTouchEvent(event))
			return true;
		return original;
	}
	
	@Override
	public void computeScroll() {
		if(mDrawerProvider.getSlideEngine() != null){
			switch(mDrawerProvider.getScrollDirection()){
			case LayoutDrawerProvider.DIRECTION_TOP:
				scrollTo(0, mDrawerProvider.getSlideEngine().getPosition());
				break;
			case LayoutDrawerProvider.DIRECTION_BOTTOM:
				scrollTo(0, mDrawerProvider.getSlideEngine().getPosition() - mDrawerProvider.getScrollRange());
				break;
			case LayoutDrawerProvider.DIRECTION_LEFT:
				scrollTo(mDrawerProvider.getSlideEngine().getPosition(), 0);
				break;
			case LayoutDrawerProvider.DIRECTION_RIGHT:
				scrollTo(mDrawerProvider.getSlideEngine().getPosition() - mDrawerProvider.getScrollRange(), 0);
				break;
			}
			if(!mDrawerProvider.getSlideEngine().isStop()){
				postInvalidate();
			}
		}
	}
	
	@Override
	public void notifyRefresh() {
		postInvalidate();
	}
	
	@Override
	public void destroy() {
		
	}
	
	/********************************************************
	 * public
	 */
	
	/**
	 * 拉出抽屉
	 */
	public void pullOut(){
		mDrawerProvider.pullOut();
	}
	
	/**
	 * 关闭抽屉
	 */
	public void pushIn(){
		mDrawerProvider.pushIn();
	}
	
	/**
	 * 拉出抽屉<br>
	 * <br>
	 * 设定强制执行后, 抽屉拉出完成前触摸无效, 滚动强制完成目标无法改变<br>
	 * 
	 * @param force 是否强制执行(锁定目标)
	 */
	public void pullOut(boolean force){
		mDrawerProvider.pullOut(force);
	}
	
	/**
	 * 关闭抽屉<br>
	 * <br>
	 * 	设定强制执行后, 抽屉拉出完成前触摸无效, 滚动强制完成目标无法改变<br>
	 * 
	 * @param force 是否强制执行(锁定目标)
	 */
	public void pushIn(boolean force){
		mDrawerProvider.pushIn(force);
	}

	/**
	 * 拉出抽屉(立即, 无动画)<br>
	 * <br>
	 *
	 */
	public void pullOutImmidiatly(){
		mDrawerProvider.pullOutImmidiatly();
	}

	/**
	 * 关闭抽屉(立即, 无动画)<br>
	 * <br>
	 *
	 */
	public void pushInImmidiatly(){
		mDrawerProvider.pushInImmidiatly();
	}

	/**
	 * 销毁
	 */
	public void destory(){
		mDrawerProvider.destory();
	}
	
	/***********************************************************
	 * private
	 */
	
	/**
	 * 初始化滑动(通知)<br>
	 * <br>
	 * 由于View渲染之前无法获取自身的宽高, 需要设置监听器, 在宽高计算出来后获取,
	 * 获取到宽高后方可进行初始化操作<br>
	 */
	private void postInitSlide() {
		//设置绘制监听器, 为了获得View自身宽高
		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				//移除监听, 不重复初始化
				getViewTreeObserver().removeOnPreDrawListener(this);
				//初始化滑动
				mDrawerProvider.initSlide();
				//重绘UI, 触发OnPreDrawListener
				postInvalidate();
				return true;
			} 
        });
	}
	
	/*************************************************************
	 * getter
	 */

	public LinearGestureDriver getGestureDriver() {
		return mDrawerProvider.getGestureDriver();
	}

	public LinearScrollEngine getSlideEngine() {
		return mDrawerProvider.getSlideEngine();
	}

	public int getScrollDirection() {
		return mDrawerProvider.getScrollDirection();
	}

	public int getDrawerWidth() {
		return mDrawerProvider.getDrawerWidth();
	}

	public int getHandleWidth() {
		return mDrawerProvider.getHandleWidth();
	}

	public int getScrollDuration() {
		return mDrawerProvider.getScrollDuration();
	}

	public boolean isOverScrollEnabled() {
		return mDrawerProvider.isOverScrollEnabled();
	}

	public float getOverScrollDamp() {
		return mDrawerProvider.getOverScrollDamp();
	}

	public float getCurrentStage(){
		return mDrawerProvider.getCurrentStage();
	}

	public float getPullOutStage(){
		return mDrawerProvider.getPullOutStage();
	}

	public float getPushInStage(){
		return mDrawerProvider.getPushInStage();
	}
	
}
