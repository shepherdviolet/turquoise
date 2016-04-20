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

package sviolet.demoaimageloader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import sviolet.demoaimageloader.common.DemoDefault;
import sviolet.demoaimageloader.common.DemoList;
import sviolet.demoaimageloader.common.DemoListAdapter;
import sviolet.demoaimageloader.demos.BasicActivity;
import sviolet.demoaimageloader.demos.ListViewActivity;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;

/**************************************************************
 * Demo配置
 */
//默认Demo
//@DemoDefault(
//        BasicActivity.class
//)

// Demo列表
@DemoList({
        BasicActivity.class,
        ListViewActivity.class
})

/**************************************************************
 *  Activity
 */

@ResourceId(R.layout.guide_main)
@ActivitySettings(
        noTitle = false,
        translucentBar = false,
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class GuideActivity extends TActivity {

    @ResourceId(R.id.guide_main_listview)
    private ListView demoListView;
    private DemoListAdapter demoListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        injectDemoListView();
        injectDefaultDemo();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**************************************************************
     * private
     */

    /**
     * 进入指定的Activity
     *
     * @param activity 指定的Acitivity
     */
    private void go(Class<? extends Activity> activity) {
        if (activity == null)
            return;
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    /**
     * 注入并进入默认Demo
     */
    private void injectDefaultDemo() {
        if (this.getClass().isAnnotationPresent(DemoDefault.class)) {
            Class<? extends Activity> activity = this.getClass().getAnnotation(DemoDefault.class).value();
            go(activity);
        }
    }

    /**
     * 注入并显示Demo列表
     */
    private void injectDemoListView() {
        if (this.getClass().isAnnotationPresent(DemoList.class)) {
            Class<? extends Activity>[] activityList = this.getClass().getAnnotation(DemoList.class).value();
            demoListAdapter = new DemoListAdapter(this, R.layout.guide_main_item, activityList);
            demoListView.setAdapter(demoListAdapter);
            demoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (view != null)
                        go((Class<? extends Activity>) demoListAdapter.getItem(position));
                }
            });
        }
    }
}
