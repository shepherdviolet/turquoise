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
 */

package sviolet.turquoise.view.slide.view;

import sviolet.turquoise.view.listener.OnVelocityOverflowListener;
import sviolet.turquoise.view.slide.SlideView;
import sviolet.turquoise.view.slide.logic.LinearFlingEngine;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * 
 * [适配控件]ListView<p/>
 * 
 * 通过GestureDriver驱动的ListView<p/>
 * 
 * 用法::<br/>
 * 1.AdaptListView.getSlideEngine().bind(mGestureDriver);<br/>
 * 2.SlideEngineGroup.addSlideEngine(AdaptListView.getSlideEngine());<p/>
 * 
 * @author S.Violet
 *
 */

public class AdaptListView extends ListView implements SlideView{

	private OnVelocityOverflowListener mOnVelocityOverflowListener;//速度溢出监听器

	private boolean isVelocityOverflowCallbacked = false;//速度溢出事件已回调

	//内置滑动引擎
	private LinearFlingEngine mSlideEngine = new LinearFlingEngine(getContext(), this);
	
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
		mSlideEngine.setMaxRange(Integer.MAX_VALUE);//允许滑动距离
		mSlideEngine.setInitPosition(0);//初始位置
		mSlideEngine.setInfiniteRange(true);//无限滑动
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
		if(mSlideEngine != null) {
			int offset = mSlideEngine.getOffset();
			smoothScrollBy(offset, 0);
			if (!mSlideEngine.isStop()) {
				postInvalidate();
			}
			//速度溢出监听
			if (mOnVelocityOverflowListener != null){
				if (reachTop() || reachBottom()) {
					if(!isVelocityOverflowCallbacked) {
						isVelocityOverflowCallbacked = true;
						mOnVelocityOverflowListener.onVelocityOverflow(mSlideEngine.getCurrVelocity());
					}
				}else{
					isVelocityOverflowCallbacked = false;
				}
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
	 */
	public LinearFlingEngine getSlideEngine(){
		return mSlideEngine;
	}

	/**
	 * 列表是否拉到顶部
	 */
	public boolean reachTop(){
		//显示的第一个Item为第一个, 且该Item到达顶端的距离为0
		if(getFirstVisiblePosition() == 0 && getChildAt(0).getTop() == 0){
			return true;
		}
		return false;
	}

	/**
	 * 列表是否拉到底部
	 */
	public boolean reachBottom(){
		//显示的最后一个Item为最后一个, 且该Item到达底部的距离为0
		if(getLastVisiblePosition() == (getCount() - 1) &&
				getChildAt(getChildCount() - 1).getBottom() == getHeight()){
			return true;
		}
		return false;
	}

	/**
	 * 
	 * 设置速度溢出监听器<br/>
	 * 该实现方式与LinearFlingEngine不同, 必须使用此方法实现监听
	 * 
	 * @param listener
	 */
	public void setOnVelocityOverflowListener(OnVelocityOverflowListener listener){
		this.mOnVelocityOverflowListener = listener;
	}

	/****************************************************
	 * private
	 */
	
}
