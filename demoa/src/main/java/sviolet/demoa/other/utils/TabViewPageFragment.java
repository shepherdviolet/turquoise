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

package sviolet.demoa.other.utils;

import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import sviolet.demoa.R;
import sviolet.demoa.common.EmulateListAdapter;
import sviolet.turquoise.enhance.app.TFragmentV4;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;

/**
 * TabView的ViewPagerFragment
 *
 * Created by S.Violet on 2016/12/2.
 */
@ResourceId(R.layout.other_tab_view_page)
public class TabViewPageFragment extends TFragmentV4 {

    @ResourceId(R.id.other_tab_view_page_listview)
    private ListView listView;

    //适配器(数据)
    private BaseAdapter adapter;

    @Override
    protected void onInitView(View fragmentView, Bundle savedInstanceState) {
        super.afterCreateView(fragmentView, savedInstanceState);

        //获取传入参数
        Bundle bundle = getArguments();
        String text = bundle.getString("text");

        //创建适配器
        adapter = new EmulateListAdapter(getContext(), 100, text, "12 hours ago", "content content content content content content content");
        //设置适配器
        listView.setAdapter(adapter);
    }

    @Override
    protected void onRefreshView(View fragmentView, Bundle savedInstanceState) {
        super.onRefreshView(fragmentView, savedInstanceState);

        //可以在这里填充数据/刷新数据

        //刷新数据
        adapter.notifyDataSetChanged();
    }

    @Override
    protected boolean fragmentViewCacheEnabled() {
        return true;//开启View复用模式
    }
}
