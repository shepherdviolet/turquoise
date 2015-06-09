package sviolet.liba.view.slidedriver.logic;

import java.util.ArrayList;

import android.view.View;

/**
 * 
 * SlideDriver专用点击/长按事件分发器<p>
 * 
 * (用于将SlideDriver自身触发的点击/长按事件分发给多个View,
 * 判断触点位置是否在View上,并以此选择是否触发该View的onClick事件)
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

/*
 * private SlideClickDispatcher slideClickDispatcher = new SlideClickDispatcher(this);
 * 
		slidedriver.setOnShortClickListener(new OnShortClickListener(){
			@Override
			public boolean onClick(float x, float y) {
				//手动分发点击事件
				if(slideClickDispatcher.dispatchClick(x, y)){
					//有view响应点击
				}else{
					//没有view响应点击
				}
			}
		});
		slidedriver.setOnLongPressListener(new OnLongPressListener() {
			@Override
			public void OnLongPress(float x, float y) {
				//手动分发长按事件
				if(slideClickDispatcher.dispatchLongPress(x, y)){
					//有view相应长按
				}else{
					//没有view相应长按
				}
			}
		});
		driver.setOnActionListener(new OnActionListener() {
			@Override
			public void onUp(float x, float y) {
				//重置动画
				slideClickDispatcher.resetAnimation(SlideListItem.this);
			}
			@Override
			public void onMove(float x, float y) {
				//将自己设置为捕获事件的Item
				adapter.slidingItem = SlideListItem.this;
			}
			@Override
			public void onDown(float x, float y) {
				//重置上一个item的动画
				if(getAdapter().touchedSlideItem != null)
					getAdapter().touchedSlideItem.resetAnimation();
				//手动分发按下动画
				slideClickDispatcher.dispatchAnimation(x, y, true);
				//设置本Item为最后触摸的Item
				getAdapter().touchedSlideItem = SlideListItem.this;
			}
		});
 */

public class SlideClickDispatcher {

	//根View
	private View root;
	//点击监听器表
	private ArrayList<SlideClickableView> ClickableViewList = new ArrayList<SlideClickableView>();
	
	/**
	 * @param root 根View(用于root.findViewById(viewId))
	 */
	public SlideClickDispatcher(View root){
		this.root = root;
	}
	
	/**
	 * 调用此方法分发点击事件
	 * 
	 * @return true:有View响应点击 false:无View相应点击
	 *
	 */
	public boolean dispatchClick(float x, float y){
		for(SlideClickableView clickableView : ClickableViewList){
			if(!clickableView.isLongPress && clickableView.dispatchClick(root, x, y)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 调用此方法分发长按事件
	 *
	 * @return true:有View响应点击 false:无View相应点击
	 *
	 */
	public boolean dispatchLongPress(float x, float y){
		for(SlideClickableView clickableView : ClickableViewList){
			if(clickableView.isLongPress && clickableView.dispatchClick(root, x, y)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 调用此方法分发动画事件
	 *
	 * @return true:有View响应点击 false:无View相应点击
	 *
	 */
	public boolean dispatchAnimation(float x, float y, boolean isPressed){
		for(SlideClickableView clickableView : ClickableViewList){
			if(clickableView.dispatchAnimation(root, x, y, isPressed)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 重置所有动画
	 */
	public void resetAnimation(View root){
		for(SlideClickableView clickableView : ClickableViewList){
			clickableView.resetAnimation(root);
		}
	}
	
	/**
	 * 合理的时候清理,避免重复添加可点击对象(),
	 * 清除所有可点击对象ClickableViewList & LongPressableViewList
	 */
	public void wipeClickableView(){
		ClickableViewList.clear();
	}
	
	/**
	 * 添加一个可点击对象
	 * 先添加的对象优先级高,优先级高的对象消费掉onclick事件后,优先级低的对象将不会调用onclick,
	 * 建议先添加上层对象,后添加下层对象
	 * 
	 * @param SlideClickableView
	 */
	public void addClickableView(SlideClickableView clickableView){
		ClickableViewList.add(clickableView);
	}
	
}
