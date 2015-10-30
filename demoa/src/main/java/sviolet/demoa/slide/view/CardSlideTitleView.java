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

package sviolet.demoa.slide.view;

import sviolet.turquoise.utils.sys.MeasureUtils;
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

	private LinearFlingEngine mSlideEngine = new LinearFlingEngine(getContext(), this);
	
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
		mSlideEngine.setMaxRange(range);//设置最大可滑动距离
		mSlideEngine.setInitPosition(range);//设置初始位置
		mSlideEngine.setSlidingDirection(LinearFlingEngine.DIRECTION_RIGHT_OR_BOTTOM);//设置滑动输出方向
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
