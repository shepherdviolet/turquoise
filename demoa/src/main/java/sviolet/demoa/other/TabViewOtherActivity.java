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
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.other.utils.TabViewPageFragment;
import sviolet.demoa.other.utils.TabViewPageFragmentAdapter;
import sviolet.turquoise.enhance.app.TFragmentActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
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

    }

    private void createTabView(LineIndicatorTabViewForViewPager tabView){
        tabView.addView(createTabItem("新闻1"));
        tabView.addView(createTabItem("新闻2"));
        tabView.addView(createTabItem("新闻3"));
        tabView.addView(createTabItem("新闻4"));
        tabView.addView(createTabItem("新闻5"));
        tabView.addView(createTabItem("新闻6"));
        tabView.addView(createTabItem("新闻7"));
        tabView.addView(createTabItem("新闻8"));
        tabView.addView(createTabItem("新闻9"));
        tabView.addView(createTabItem("新闻10"));
    }

    private View createTabItem(String title){
        View view = LayoutInflater.from(this).inflate(R.layout.other_tab_view_tabitem, null);
        ((TextView)view.findViewById(R.id.other_tab_view_tabitem_text)).setText(title);
        return view;
    }

    private List<Fragment> createFragments() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(createFragment(0xFF10A0F0, "1"));
        fragments.add(createFragment(0xFF2090D0, "2"));
        fragments.add(createFragment(0xFF3080B0, "3"));
        fragments.add(createFragment(0xFF407090, "4"));
        fragments.add(createFragment(0xFF506070, "5"));
        fragments.add(createFragment(0xFF605050, "6"));
        fragments.add(createFragment(0xFF704030, "7"));
        fragments.add(createFragment(0xFF803010, "8"));
        fragments.add(createFragment(0xFF9020F0, "9"));
        fragments.add(createFragment(0xFFA010D0, "10"));
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
