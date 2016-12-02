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

        Bundle bundle = new Bundle();
        bundle.putInt("color", 0xFFFF00FF);
        List<Fragment> fragments = new ArrayList<>();
        TabViewPageFragment page = new TabViewPageFragment();
        page.setArguments(bundle);
        fragments.add(page);
        bundle = new Bundle();
        bundle.putInt("color", 0xFFFFFF00);
        page = new TabViewPageFragment();
        page.setArguments(bundle);
        fragments.add(page);

        TabViewPageFragmentAdapter adapter = new TabViewPageFragmentAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);

        View view = LayoutInflater.from(this).inflate(R.layout.other_tab_view_tabitem, null);
        ((TextView)view.findViewById(R.id.other_tab_view_tabitem_text)).setText("111");
        tabView.addView(view);

        view = LayoutInflater.from(this).inflate(R.layout.other_tab_view_tabitem, null);
        ((TextView)view.findViewById(R.id.other_tab_view_tabitem_text)).setText("222");
        tabView.addView(view);

        tabView.bindViewPager(viewPager, false);

    }
}
