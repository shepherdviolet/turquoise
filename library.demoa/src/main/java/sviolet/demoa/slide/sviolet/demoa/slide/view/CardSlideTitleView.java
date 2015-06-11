package sviolet.demoa.slide.sviolet.demoa.slide.view;

import sviolet.turquoise.utils.MeasureUtils;
import sviolet.turquoise.view.slide.SlideView;
import sviolet.turquoise.view.slide.logic.LinearFlingEngine;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 卡片控件伸缩标题
 * @author S.Violet
 */

public class CardSlideTitleView extends LinearLayout implements SlideView {

	private LinearFlingEngine mSlideEngine;
	
	public CardSlideTitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSlide();
	}

	public CardSlideTitleView(Context context) {
		super(context);
		initSlide();
	}
	
	private void initSlide() {
        int range = MeasureUtils.dp2px(getContext(), 120);//滑动范围
        //后续初始化操作, 创建配置手势驱动/滑动引擎实例
		mSlideEngine = new LinearFlingEngine(getContext(), this, range, range, LinearFlingEngine.DIRECTION_RIGHT_OR_BOTTOM);
		//允许拖动越界, 越界阻尼0.7
//		mSlideEngine.setOverScroll(true, 0.7f);
	}
	
	/**
	 * 输出
	 */
	@Override
	public void computeScroll() {
		//改变高度使用requestLayout()
		if(mSlideEngine != null && !mSlideEngine.isStop()){
			int value = mSlideEngine.getPosition();
			LayoutParams params = (LayoutParams) getLayoutParams();
			params.height = MeasureUtils.dp2px(getContext(), 60) + value;
			requestLayout();
		}
	}
	
	/**
	 * 通知刷新
	 */
	@Override
	public void notifyRefresh() {
		postInvalidate();
	}

	@Override
	public void destroy() {
		
	}

	public LinearFlingEngine getSlideEngine(){
		return mSlideEngine;
	}
	
}
