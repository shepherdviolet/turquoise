package sviolet.turquoise.view.slide;



/**
 * 滑动效果控件<br>
 * <br>
 * 通常情况下::<br>
 * [View] TouchEvent--> [GestrueDrive] <--bind--> [SlideEngine] notify--> [SlideView]<br>
 * <br>
 **************************************************************************************<br>
 * 注意::<br>
 * <br>
 * 若子View无法截获事件, 会导致无法滑动, 可以设置android:clickable="true"解决<br>
 * 由于没有View处理事件, ViewGroup也无法截获事件, 导致触摸事件无效.<br>
 * <br>
 * 绑定的SlideView至少设置一个背景android:background="#00000000", 否则无法滑动<br>
 * 由于ViewGroup没有内容, 不会调用draw/onDraw方法, 导致postInvalidate无效, 因此设置background即可解决<br>
 * <br>
 **************************************************************************************<br>
 * 设置示例:<br>
 * ViewGroup(LinearLayout/RelativeLayout等) 实现SlideView接口, 并按如下复写方法:<br>
 * SlideView::<br>
 * <br>
	//构造方法(有三个)
	public MyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//初始化
		init();
	}
	
	//初始化
	private void init(){
		//View获取自身宽高等参数方法
		//绘制监听器, 也可以使用addOnGlobalLayoutListener监听layout事件
		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				//移除监听器, 以免重复调用
				getViewTreeObserver().removeOnPreDrawListener(this); 
                initLinearSlide();//LinearSlideEngine初始化示例
                return true;
            }
        });
	}
	
	//LinearSlideEngine初始化示例
	private void initLinearSlide() {
		//此处可调用getWidth()/getHeight()等方法获得自身宽高, 因为View已完成measure
        int range = getWidth() - MeasureUtils.dp2px(getContext(), 100);//滑动范围 = 控件宽 - 100dp
        int position = range;//初始位置 = 滑动范围
        //后续初始化操作, 创建配置手势驱动/滑动引擎实例
		mGestureDriver = new LinearGestureDriver(getContext(), LinearGestureDriver.ORIENTATION_HORIZONTAL);
		mSlideEngine = new LinearSlideEngine(getContext(), this, range, position, 500);
		mGestureDriver.bind(mSlideEngine);
		//允许拖动越界, 越界阻尼0.7
		mSlideEngine.setOverScroll(true, 0.7f);
	}
	
	//LinearStageSlideEngine初始化示例
	private void initLinearStageSlide() {
		//此处可调用getWidth()/getHeight()等方法获得自身宽高, 因为View已完成measure
        int range = getWidth() - MeasureUtils.dp2px(getContext(), 100);//滑动范围 = 控件宽 - 100dp
        int position = range;//初始位置 = 滑动范围
        //后续初始化操作, 创建配置手势驱动/滑动引擎实例
		mGestureDriver = new LinearGestureDriver(getContext(), LinearGestureDriver.ORIENTATION_HORIZONTAL);
		mSlideEngine = new LinearStageSlideEngine(getContext(), this, range / 2, 3, position, 500);
		mGestureDriver.bind(mSlideEngine);
		//允许拖动越界, 越界阻尼0.7
		mSlideEngine.setOverScroll(true, 0.7f);
	}
 * 
 * 
 **************************************************************************************<br>
 *	output<br>
 * 刷新UI/输出显示示例:<br>
 * SlideView::<br>
 * <br>
	//实现通知刷新UI接口
	@Override
	public void notifyRefresh() {
		postInvalidate();
	}
	
	//常用输出方法:滚动(0 -> range)
	@Override
	public void computeScroll() {
		if(mSlideEngine != null){
			scrollTo(mSlideEngine.getPosition(), 0);
			if(!mSlideEngine.isStop())
				postInvalidate();
		}
	}
	
	//常用输出方法2:滚动(-range -> 0)
	@Override
	public void computeScroll() {
		if(mSlideEngine != null){
			scrollTo(mSlideEngine.getPosition() - mSlideEngine.getRange(), 0);
			if(!mSlideEngine.isStop())
				postInvalidate();
		}
	}
	
	//常用输出方法3:改变宽高
	@Override
	public void computeScroll() {
		if(mSlideEngine != null && !mSlideEngine.isStop()){
			LinearLayout.LayoutParams params = (LayoutParams) getLayoutParams();
			params.height = mSlideEngine.getPosition();
			requestLayout();//改用requestLayout();
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
 * <br>
 **************************************************************************************<br>
 *	input<br>
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
 * XML示例:<br>
 * android:scrollX/scrollY 处可以设置很大的值, 目的是让控件在onCreate时消失, 
 * 随后SlideView会调用onDraw方法, 从SlideEngine.getPosition(), 取得
 * SlideEngine构建时设置的初始position, 从而让控件出现在SlideEngine
 * 设置的初始位置. 若使用getOffset()方式滚动, 请勿这样设置.<br>
 * 绑定的SlideView至少设置一个背景android:background="#00000000", 
 * 否则无法滑动, 由于ViewGroup没有内容, 不会调用draw/onDraw方法, 导
 * 致postInvalidate无效, 因此设置background即可解决<br>
 * <br>
    <x.x.MySlideView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
		android:background="#00000000"
        android:scrollX="10000dp"
        android:scrollY="10000dp"><!-- 设置很大的scroll值 -->
		<ListView 
		    android:id="@+id/listview"
		    android:layout_width="match_parent"
		    android:layout_height="match_parent"
			android:background="#209090"/>
    </x.x.MySlideView>
    
    <x.x.MyView 
        android:layout_width="match_parent"
        android:layout_height="match_parent"
		android:background="#00000000"
        android:scrollX="100dp"><!-- 可以设置View初始位置 -->

		<TextView 
		    android:id="@+id/textView"
		    android:layout_width="match_parent"
		    android:layout_height="match_parent"
			android:background="#209090"
			android:clickable="true"/><!-- 若子View无法截获事件, 会导致无法滑动, 可以设置android:clickable="true"解决 -->

    </x.x.MyView>
 * <br>
 * <br>
 * **************************************************************************************<br>
 * View事件监听者:<br>
 * <br>
 * 用于获取控件宽高, 建议使用OnPreDrawListener, 若使用OnGlobalLayoutListener, 
 * 可能会在ListView中随机性得未被调用<br>
 * <br>
	interface          ViewTreeObserver.OnPreDrawListener
	当一个视图树将要绘制时，所要调用的回调函数的接口类 返回true继续绘制 false取消绘制
	
	interface  ViewTreeObserver.OnDrawListener 
	挡在一个视图树绘制时，所要调用的回调函数的接口类（level 16)
	
	interface          ViewTreeObserver.OnGlobalFocusChangeListener   
	当在一个视图树中的焦点状态发生改变时，所要调用的回调函数的接口类
	
	interface          ViewTreeObserver.OnGlobalLayoutListener
	当在一个视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变时，所要调用的回调函数的接口类
	
	interface          ViewTreeObserver.OnScrollChangedListener
	当一个视图树中的一些组件发生滚动时，所要调用的回调函数的接口类
	
	interface          ViewTreeObserver.OnTouchModeChangeListener
	当一个视图树的触摸模式发生改变时，所要调用的回调函数的接口类
 * <br>
 * *****************************************************************************************<br>
 * 其他<br>
 * <br>
 * 1.里层SlideView滑动后阻断外层SlideView拦截:<br>
		//里层设置hold事件监听
		insideSlideView.getSlideEngine()..setOnGestureHoldListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//阻断外层手势拦截
				outsideSlideView.getSlideEngine().otherIntercepted();
			}
		});
 * <br>
 * @author S.Violet
 */

public interface SlideView {
	
	/**
	 * 通知UI刷新
	 */
	public void notifyRefresh();
	
	/**
	 * 销毁<br>
	 */
	public void destroy();
	
}
