package sviolet.liba.view.slidelist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * 
 * SlideList专用View
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

public class SlideListView extends ListView {
	
	public SlideListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SlideListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SlideListView(Context context) {
		super(context);
	}
	
	/**
	 * 复写方法:
	 * 截获触摸事件
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
	
		SlideListItem slidingItem = ((SlideListAdapter)getAdapter()).slidingItem;
		
		if(slidingItem != null){
			if(slidingItem.dispatchTouchEvent(ev)){
				
			}else{
				slidingItem.slideBack();
			}
			return true;
		}
		
		return super.dispatchTouchEvent(ev);
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		
		return super.onTouchEvent(ev);
	}
	
	/**
	 * 复写方法:
	 * 滚动时复位点击效果
	 */
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		
		SlideListItem touchedSlideItem = ((SlideListAdapter)getAdapter()).touchedSlideItem;
		
		if(touchedSlideItem != null){
			touchedSlideItem.resetAnimation();
		}
		
		super.onScrollChanged(l, t, oldl, oldt);
		
	}
}
