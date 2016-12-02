/*
 * Copyright (C) 2015-2016 S.Violet
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

package sviolet.turquoise.ui.viewgroup.tab;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * <p>[ViewPager专用]线条样式的TabView</p>
 *
 * Created by S.Violet on 2016/12/2.
 */

public class LineIndicatorTabViewForViewPager extends LineIndicatorTabView implements ViewPager.OnPageChangeListener {

    public LineIndicatorTabViewForViewPager(Context context) {
        super(context);
    }

    public LineIndicatorTabViewForViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineIndicatorTabViewForViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //将ViewPager的位置同步给TabView
        moveToPage(position + positionOffset);
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /*************************************************************************
     * public
     */

    /**
     * 绑定一个ViewPager
     * @param viewPager viewPager
     * @param smoothScroll 点击TabView换页时, 是否有动画
     */
    public void bindViewPager(final ViewPager viewPager, final boolean smoothScroll){
        if (viewPager == null){
            return;
        }

        //将ViewPager的位置同步给TabView
        viewPager.addOnPageChangeListener(this);
        //将TabView的点击事件, 同步给ViewPager
        setOnPageChangedListener(new OnPageChangedListener() {
            @Override
            public void onPageChanged(int page, boolean byClick) {
                if (byClick){
                    viewPager.setCurrentItem(page, smoothScroll);
                }
            }
        });
    }

}
