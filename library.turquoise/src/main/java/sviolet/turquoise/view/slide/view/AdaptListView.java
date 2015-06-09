package sviolet.turquoise.view.slide.view;

import sviolet.turquoise.view.slide.SlideView;
import sviolet.turquoise.view.slide.logic.LinearFlingEngine;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * [适配控件]ListView<br>
 * <br>
 * 通过GestureDriver驱动的ListView<br>
 * <br>
 * 用法::<br>
 * 1.AdaptListView.getSlideEngine().bind(mGestureDriver);<br>
 * 2.SlideEngineGroup.addSlideEngine(AdaptListView.getSlideEngine());<br>
 * 
 * @author S.Violet
 *
 */

public class AdaptListView extends ListView implements SlideView{

	//内置滑动引擎
	private LinearFlingEngine mSlideEngine;
	
	public AdaptListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AdaptListView(Context context) {
		super(context);
		init();
	}

	//初始化引擎
	private void init(){
		mSlideEngine = new LinearFlingEngine(getContext(), this, Integer.MAX_VALUE, 0);
		mSlideEngine.setInfiniteRange(true);
	}
	
	/******************************************************
	 * override
	 */
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}
	
	/**
	 * 引擎驱动List滚动
	 */
	@Override
	public void computeScroll() {
		if(mSlideEngine != null){
			int offset = mSlideEngine.getOffset();
			smoothScrollBy(offset, 0);
			if(!mSlideEngine.isStop()){
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
	
	/**************************************************
	 * FUNC
	 */
	
	/**
	 * 获得控件内置滑动引擎
	 * @return
	 */
	public LinearFlingEngine getSlideEngine(){
		return mSlideEngine;
	}
	
	/****************************************************
	 * private
	 */
	
}
