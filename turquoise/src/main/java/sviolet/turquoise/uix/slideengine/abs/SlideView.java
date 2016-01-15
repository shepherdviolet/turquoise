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

package sviolet.turquoise.uix.slideengine.abs;

/**
 * 滑动效果控件<p/>
 *
 * 通常情况下::<br/>
 * [View] TouchEvent--> {@link GestureDriver} <--bind--> {@link SlideEngine} notify--> {@link SlideView}<p/>
 * 
 * *************************************************************************************<br/>
 * 注意::<p/>
 * 
 * 若子View无法截获事件, 会导致无法滑动, 可以设置android:clickable="true"解决
 * 由于没有View处理事件, ViewGroup也无法截获事件, 导致触摸事件无效.<p/>
 * 
 * 绑定的{@link SlideView}至少设置一个背景android:background="#00000000", 否则无法滑动
 * 由于ViewGroup没有内容, 不会调用draw/onDraw方法, 导致postInvalidate无效, 因此设置background即可解决<p/>
 * 
 * *************************************************************************************<br/>
 * 设置示例:<br/>
 * ViewGroup(LinearLayout/RelativeLayout等) 实现{@link SlideView}接口, 并按如下复写方法:<br/>
 * SlideView::<p/>
 *
 * <pre>{@code
 *      //成员变量, 实例化
 *      private LinearGestureDriver mGestureDriver = new LinearGestureDriver(getContext());//手势驱动
 *      private LinearScrollEngine mSlideEngine = new LinearScrollEngine(getContext(), this);//滑动引擎1
 *      private LinearStageScrollEngine mStageSlideEngine = new LinearStageScrollEngine(getContext(), this);//滑动引擎2
 *
 *      //构造方法(有三个)
 *      public MyView(Context context, AttributeSet attrs) {
 *          super(context, attrs);
 *          //初始化
 *          init();
 *      }
 *
 *      //初始化
 *      private void init(){
 *          //View获取自身宽高等参数方法
 *          //绘制监听器, 也可以使用addOnGlobalLayoutListener监听layout事件
 *          getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
 *              public boolean onPreDraw() {
 *                  //移除监听器, 以免重复调用
 *                  getViewTreeObserver().removeOnPreDrawListener(this);
 *                  initLinearSlide();//LinearSlideEngine初始化示例
 *                  return true;
 *              }
 *          });
 *      }
 *
 *      //LinearSlideEngine初始化示例
 *      private void initLinearSlide() {
 *          //此处可调用getWidth()/getHeight()等方法获得自身宽高, 因为View已完成measure
 *          int range = getHeight() - MeasureUtils.dp2px(getContext(), 60);//滑动范围 = 控件高 - 30dp
 *          int position = range;//初始位置
 *          //后续初始化操作, 创建配置手势驱动/滑动引擎实例
 *          mGestureDriver.setOrientation(LinearGestureDriver.ORIENTATION_VERTICAL);
 *          mSlideEngine.setMaxRange(range);//最大可滑动距离
 *          mSlideEngine.setInitPosition(position);//初始位置
 *          mSlideEngine.setStageDuration(1000);//阶段滑动时间
 *          mGestureDriver.bind(mSlideEngine);
 *          //允许拖动越界, 越界阻尼0.7
 *          mSlideEngine.setOverScroll(true, 0.7f);
 *          mSlideEngine.setOnGestureHoldListener(mOnGestureHoldListener);
 *      }
 *
 *      //LinearStageSlideEngine初始化示例
 *      private void initLinearStageSlide() {
 *          //此处可调用getWidth()/getHeight()等方法获得自身宽高, 因为View已完成measure
 *          int range = getHeight() - MeasureUtils.dp2px(getContext(), 60);//滑动范围 = 控件高 - 30dp
 *          int position = range;//初始位置
 *          //后续初始化操作, 创建配置手势驱动/滑动引擎实例
 *          mGestureDriver.setOrientation(LinearGestureDriver.ORIENTATION_VERTICAL);
 *          mStageSlideEngine.setStageRange(range / 2);//一个阶段的滑动距离
 *          mStageSlideEngine.setStageNum(3);//阶段数
 *          mStageSlideEngine.setInitPosition(position);//初始位置
 *          mStageSlideEngine.setStageDuration(1000);//阶段滑动时间
 *          mGestureDriver.bind(mSlideEngine);
 *          //允许拖动越界, 越界阻尼0.7
 *          //mStageSlideEngine.setOverScroll(true, 0.7f);
 *          mStageSlideEngine.setOnGestureHoldListener(mOnGestureHoldListener);
 *      }
 *
 * }</pre>
 * *************************************************************************************<br/>
 *  output<br/>
 *  刷新UI/输出显示示例:<br/>
 *  {@link SlideView}::<p/>
 *
 * <pre>{@code
 *	//实现通知刷新UI接口
 *	public void notifyRefresh() {
 *		postInvalidate();
 *	}
 *
 *	//常用输出方法:滚动(0 -> range)
 *	public void computeScroll() {
 *		if(mSlideEngine != null){
 *			scrollTo(mSlideEngine.getPosition(), 0);
 *			if(!mSlideEngine.isStop())
 *				postInvalidate();
 *		}
 *	}
 *
 *	//常用输出方法2:滚动(-range -> 0)
 *	public void computeScroll() {
 *		if(mSlideEngine != null){
 *			scrollTo(mSlideEngine.getPosition() - mSlideEngine.getRange(), 0);
 *			if(!mSlideEngine.isStop())
 *				postInvalidate();
 *		}
 *	}
 *
 *	//常用输出方法3:改变宽高
 *	public void computeScroll() {
 *		if(mSlideEngine != null && !mSlideEngine.isStop()){
 *			LinearLayout.LayoutParams params = (LayoutParams) getLayoutParams();
 *			params.height = mSlideEngine.getPosition();
 *			requestLayout();//改用requestLayout();
 *		}
 *	}
 *
 *	//其他输出方法
 *	protected void onDraw(Canvas canvas) {
 *		//绘制View
 *		super.onDraw(canvas);
 *		//滑动至engine所在位置
 *		if(mSlideEngine != null){
 *			scrollTo(mSlideEngine.getPosition(), 0);
 *			//判断是否停止
 *			if(!mSlideEngine.isStop())
 *				postInvalidate();
 *		}
 *	}
 * 
 * }</pre>
 * *************************************************************************************<br/>
 *  input<br/>
 *  手势捕获示例:<br/>
 *  需要捕获触摸手势的控件复写如下方法:<p/>
 *
 *  ViewGroup::<p/>
 *
 *  <pre>{@code
 *  //复写事件拦截
 *  public boolean onInterceptTouchEvent(MotionEvent ev) {
 *      boolean original = super.onInterceptTouchEvent(ev);
 *      if(mGestureDriver != null && mGestureDriver.onInterceptTouchEvent(ev))
 *          return true;
 *      return original;
 *  }
 *
 *  //复写触摸事件处理
 *  public boolean onTouchEvent(MotionEvent event) {
 *      boolean original = super.onTouchEvent(event);
 *      if(mGestureDriver != null && mGestureDriver.onTouchEvent(event))
 *          return true;
 *      return original;
 *  }
 * }</pre>
 * *************************************************************************************<br/>
 * XML示例:<p/>
 *
 * android:scrollX/scrollY 处可以设置很大的值, 目的是让控件在onCreate时消失, 
 * 随后SlideView会调用onDraw方法, 从SlideEngine.getPosition(), 取得
 * {@link SlideEngine}构建时设置的初始position, 从而让控件出现在{@link SlideEngine}
 * 设置的初始位置. 若使用getOffset()方式滚动, 请勿这样设置.
 * 绑定的{@link SlideView}至少设置一个背景android:background="#00000000",
 * 否则无法滑动, 由于ViewGroup没有内容, 不会调用draw/onDraw方法, 导
 * 致postInvalidate无效, 因此设置background即可解决<p/>
 *
 * <pre>{@code
 *  <x.x.MySlideView
 *      android:layout_width="match_parent"
 *      android:layout_height="match_parent"
 *      android:background="#00000000"
 *      android:scrollX="10000dp"
 *      android:scrollY="10000dp"><!-- 设置很大的scroll值 -->
 *      <ListView
 *          android:id="@+id/listview"
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent"
 *          android:background="#209090"/>
 *  </x.x.MySlideView>
 *
 *  <x.x.MyView
 *      android:layout_width="match_parent"
 *      android:layout_height="match_parent"
 *      android:background="#00000000"
 *      android:scrollX="100dp"><!-- 可以设置View初始位置 -->
 *
 *      <TextView
 *          android:id="@+id/textView"
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent"
 *          android:background="#209090"
 *          android:clickable="true"/><!-- 若子View无法截获事件, 会导致无法滑动, 可以设置android:clickable="true"解决 -->
 *
 *  </x.x.MyView>
 * }</pre>
 * 
 * **************************************************************************************<br/>
 * View事件监听者:<p/>
 * 
 * 用于获取控件宽高, 建议使用OnPreDrawListener, 若使用OnGlobalLayoutListener, 
 * 可能会在ListView中随机性得未被调用<p/>
 *
 *	interface          ViewTreeObserver.OnPreDrawListener<br/>
 *	当一个视图树将要绘制时，所要调用的回调函数的接口类 返回true继续绘制 false取消绘制<p/>
 *
 *	interface  ViewTreeObserver.OnDrawListener<br/>
 *	挡在一个视图树绘制时，所要调用的回调函数的接口类（level 16)<p/>
 *
 *	interface          ViewTreeObserver.OnGlobalFocusChangeListener<br/>
 *	当在一个视图树中的焦点状态发生改变时，所要调用的回调函数的接口类<p/>
 *
 *	interface          ViewTreeObserver.OnGlobalLayoutListener<br/>
 *	当在一个视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变时，所要调用的回调函数的接口类<p/>
 *
 *	interface          ViewTreeObserver.OnScrollChangedListener<br/>
 *	当一个视图树中的一些组件发生滚动时，所要调用的回调函数的接口类<p/>
 *
 *	interface          ViewTreeObserver.OnTouchModeChangeListener<br/>
 *	当一个视图树的触摸模式发生改变时，所要调用的回调函数的接口类<p/>
 * 
 * *****************************************************************************************<br/>
 * 其他<p/>
 * 
 * 1.里层SlideView滑动后阻断外层SlideView拦截:<br/>
 *		outside.getSlideEngine().addInnerEngine(inside.getSlideEngine());<p/>
 *
 * @author S.Violet
 */

public interface SlideView {
	
	/**
	 * 通知UI刷新
	 */
	public void notifyRefresh();
	
	/**
	 * 销毁
	 */
	public void destroy();
	
}
