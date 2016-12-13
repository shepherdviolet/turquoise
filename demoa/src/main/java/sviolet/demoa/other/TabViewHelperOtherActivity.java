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
import sviolet.turquoise.ui.viewgroup.tab.LineIndicatorTabViewHelper;

@DemoDescription(
        title = "TabViewHelper Demo",
        type = "Other",
        info = "TabViewHelper Demo"
)

/**
 * TabView inflate by helper<br/>
 *
 * Created by S.Violet on 2016/11/23.
 */
@ResourceId(R.layout.other_tab_view)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class TabViewHelperOtherActivity extends TFragmentActivity {

    @ResourceId(R.id.other_tab_view_viewpager)
    private ViewPager viewPager;
    @ResourceId(R.id.other_tab_view_tabview)
    private LineIndicatorTabViewForViewPager lineIndicatorTabViewForViewPager;

    private LineIndicatorTabViewHelper lineIndicatorTabViewHelper;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        //ViewPager填充数据
        List<Fragment> fragments = createFragments();
        TabViewPageFragmentAdapter adapter = new TabViewPageFragmentAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);

        //初始化tabView
        initTabView(lineIndicatorTabViewForViewPager);

        //绑定
        lineIndicatorTabViewForViewPager.bindViewPager(viewPager, false);
    }

    private void initTabView(LineIndicatorTabViewForViewPager tabView){

        //帮助类实例化
        lineIndicatorTabViewHelper = new LineIndicatorTabViewHelper(
                getApplicationContext(),
                lineIndicatorTabViewForViewPager,
                R.layout.other_tab_view_tabitem,//Item布局的Id
                R.id.other_tab_view_tabitem_text,//布局中TextView的Id
                0xFF808080,//未选中时字体颜色
                0xFF209090);//选中时字体颜色

        //数据
        List<String> tagStringList = new ArrayList<>();
        tagStringList.add("Tab1");
        tagStringList.add("Tab2");
        tagStringList.add("Tab3");
        tagStringList.add("Tab4");
        tagStringList.add("Tab5");
        tagStringList.add("Tab6");
        tagStringList.add("Tab7");
        tagStringList.add("Tab8");
        tagStringList.add("Tab9");
        tagStringList.add("Tab10");

        //设置数据. 并指定初始页面为2
        lineIndicatorTabViewHelper.setData(tagStringList, 2);
        //设置ViewPager的页面也为2
        viewPager.setCurrentItem(2);
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
