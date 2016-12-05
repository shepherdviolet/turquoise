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

package sviolet.demoa.other;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.other.utils.TabViewPageFragment;
import sviolet.demoa.other.utils.TabViewPageFragmentAdapter;
import sviolet.turquoise.enhance.app.TFragmentActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.ui.viewgroup.tab.LineIndicatorTabView;
import sviolet.turquoise.ui.viewgroup.tab.LineIndicatorTabViewForViewPager;

@DemoDescription(
        title = "TabView Demo",
        type = "Other",
        info = "TabView Demo"
)

/**
 * TabView<br/>
 *
 * Created by S.Violet on 2016/11/23.
 */
@ResourceId(R.layout.other_tab_view)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class TabViewOtherActivity extends TFragmentActivity {

    @ResourceId(R.id.other_tab_view_viewpager)
    private ViewPager viewPager;
    @ResourceId(R.id.other_tab_view_tabview)
    private LineIndicatorTabViewForViewPager tabView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //ViewPager填充数据
        List<Fragment> fragments = createFragments();
        TabViewPageFragmentAdapter adapter = new TabViewPageFragmentAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);

        //tabView填充数据
        createTabView(tabView);

        //绑定
        tabView.bindViewPager(viewPager, false);

        //自定义:翻页时, TabItem的颜色变化
        tabView.addOnPageChangedListener(new LineIndicatorTabView.OnPageChangedListener() {

            private WeakReference<TextView> lastTextView;//持有上一个变色的Item

            @Override
            public void onPageChanged(int page, View child, boolean byClick) {
                //前提是知道TabItem外层是LinearLayout
                if (child instanceof LinearLayout){
                    //获取TextView, 前提是知道LinearLayout中第一个是TextView
                    View textView = ((LinearLayout) child).getChildAt(0);
                    if (textView instanceof TextView){
                        //将前一个Item颜色置为黑色
                        if (lastTextView != null){
                            TextView lastTextViewInstance = lastTextView.get();
                            if (lastTextViewInstance != null){
                                lastTextViewInstance.setTextColor(0xFF808080);
                            }
                        }
                        //设置当前Item的颜色
                        ((TextView) textView).setTextColor(0xFF209090);
                        //记录当前Item
                        this.lastTextView = new WeakReference<>((TextView) textView);
                    }
                }
            }
        });

    }

    private void createTabView(LineIndicatorTabViewForViewPager tabView){
        createTabItem(tabView, "FirstTab1");
        createTabItem(tabView, "Tab2");
        createTabItem(tabView, "Tab3");
        createTabItem(tabView, "Tab4");
        createTabItem(tabView, "Tab5");
        createTabItem(tabView, "Tab6");
        createTabItem(tabView, "Tab7");
        createTabItem(tabView, "Tab8");
        createTabItem(tabView, "Tab9");
        createTabItem(tabView, "Tab10");
    }

    private void createTabItem(LineIndicatorTabViewForViewPager tabView, String title){
        View view = LayoutInflater.from(this).inflate(R.layout.other_tab_view_tabitem, null);
        ((TextView)view.findViewById(R.id.other_tab_view_tabitem_text)).setText(title);
        tabView.addView(view);
    }

    private List<Fragment> createFragments() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(createFragment(0xFF10A0F0, "Page 1 Title-"));
        fragments.add(createFragment(0xFF2090D0, "Page 2 Title-"));
        fragments.add(createFragment(0xFF3080B0, "Page 3 Title-"));
        fragments.add(createFragment(0xFF407090, "Page 4 Title-"));
        fragments.add(createFragment(0xFF506070, "Page 5 Title-"));
        fragments.add(createFragment(0xFF605050, "Page 6 Title-"));
        fragments.add(createFragment(0xFF704030, "Page 7 Title-"));
        fragments.add(createFragment(0xFF803010, "Page 8 Title-"));
        fragments.add(createFragment(0xFF9020F0, "Page 9 Title-"));
        fragments.add(createFragment(0xFFA010D0, "Page 10 Title-"));
        return fragments;
    }

    private Fragment createFragment(int color, String text){
        TabViewPageFragment fragment = new TabViewPageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("color", color);
        bundle.putString("text", text);
        fragment.setArguments(bundle);
        return fragment;
    }

}
