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
import android.view.View;

/**
 * <p>[ViewPager专用]线条样式的TabView</p>
 *
 * <p>注意: LineIndicatorTabView内部有一个容器(LinearLayout), 所有的TabItem实际是由这个容器持有的,
 * 因此如果要获取TabItem, 请用{@link LineIndicatorTabView#getTabItemAt(int)}方法</p>
 *
 * <pre>{@code
 *
 *      //LineIndicatorTabView绑定ViewPager
 *      tabView.bindViewPager(viewPager, false);
 *
 *     <sviolet.turquoise.ui.viewgroup.tab.LineIndicatorTabViewForViewPager
 *          android:id="@+id/other_tab_view_tabview"
 *          android:layout_width="match_parent"
 *          android:layout_height="40dp"
 *          sviolet:LineIndicatorTabView_indicatorColor="#209090"
 *          sviolet:LineIndicatorTabView_indicatorWidth="3dp"
 *          sviolet:LineIndicatorTabView_indicatorBottomPadding="0dp"
 *          sviolet:LineIndicatorTabView_indicatorLeftPadding="10dp"
 *          sviolet:LineIndicatorTabView_indicatorRightPadding="10dp"/>
 *
 *     <android.support.v4.view.ViewPager
 *          android:id="@+id/other_tab_view_viewpager"
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent"/>
 *
 * }</pre>
 *
 * Created by S.Violet on 2016/12/2.
 */

public class LineIndicatorTabViewForViewPager extends LineIndicatorTabView implements ViewPager.OnPageChangeListener {

    private boolean scrolling = false;

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
        if (scrolling) {
            //通过这样来判断是点击TabView滚动的, 还是直接在ViewPager上手势滑动的
            moveToPage(position + positionOffset);
        }
    }

    @Override
    public void onPageSelected(int position) {
        //ViewPager翻页成功时, 也可以通知到onPageChangedListener监听器
        callbackPageChanged(position, false);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state > 0){
            //滚动中
            scrolling = true;
        } else {
            //非滚动中
            scrolling = false;
        }
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
        addOnPageChangedListener(new OnPageChangedListener() {
            @Override
            public void onPageChanged(int page, View child, boolean byClick) {
                if (byClick){
                    viewPager.setCurrentItem(page, smoothScroll);
                }
            }
        });
    }

}
