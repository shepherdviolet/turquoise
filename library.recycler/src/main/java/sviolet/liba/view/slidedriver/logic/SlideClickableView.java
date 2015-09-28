package sviolet.liba.view.slidedriver.logic;

import android.view.View;

public abstract class SlideClickableView {
	
	private int viewId;
	public boolean isLongPress;
	
	private View view;
	
	/**
	 * 可点击对象(SlideDriver/SlideClickDispatcher专用)
	 * 
	 * @param viewId
	 */
	public SlideClickableView(int viewId, boolean isLongPress){
		this.viewId = viewId;
		this.isLongPress = isLongPress;
	}
	
	/**
	 * 点击事件回调(复写)
	 * 
	 * @param root 分发事件的根View
	 * @param view 相应的子View
	 */
	public abstract void onClick(View root, View view);
	
	/**
	 * 动画效果回调
	 * 
	 * @param root 分发事件的根View
	 * @param view 相应的子View
	 * @param isPressed true:按下 false:复位
	 */
	public abstract void onAnimation(View root, View view, boolean isPressed);
	
	//////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 分发点击(若点在View范围内则触发onClick)
	 * 
	 * @param root View的根View(用于root.findViewById(viewId))
	 * @param x 触点X坐标
	 * @param y 触点Y坐标
	 * @return 是否触发onClick
	 */
	protected boolean dispatchClick(View root, float x, float y){
		initView(root);
		if(isPointOnView(view, x, y)){
			onClick(root, view);
			return true;
		}
		return false;
	}
	
	protected boolean dispatchAnimation(View root, float x, float y, boolean isPressed){
		initView(root);
		if(isPointOnView(view, x, y)){
			onAnimation(root, view, isPressed);
			return true;
		}
		return false;
	}
	
	protected void resetAnimation(View root){
		initView(root);
		onAnimation(root, view, false);
	}
	
	private void initView(View root){
		if(this.view == null){
			this.view = root.findViewById(viewId);
		}
	}
	
	/**
	 * 判断一个点是否落在View的范围内
	 * 
	 * @param view
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isPointOnView(View view, float x, float y){

		if(view == null)
			return false;
		
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		int left = location[0];
		int right = (view.getRight() - view.getLeft()) + left;
		int top = location[1];
		int bottom = (view.getBottom() - view.getTop()) + top;
		
		if(x >= left && x <= right && y >= top && y <= bottom){
			return true;
		}
		
		return false;
	}
}
