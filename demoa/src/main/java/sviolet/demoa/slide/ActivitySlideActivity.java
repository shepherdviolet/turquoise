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

package sviolet.demoa.slide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.EmulateListAdapter;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.TActivity;

/**
 * 可侧滑关闭Activity的demo(主界面)
 *
 * Created by S.Violet on 2015/6/30.
 */

@DemoDescription(
        title = "SlideActivity",
        type = "View",
        info = "slide to finish an Activity"
)

@ResourceId(R.layout.slide_activity_main)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class ActivitySlideActivity extends TActivity {

    @ResourceId(R.id.slide_activity_main_listview)
    private ListView listView;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        listView.setAdapter(new EmulateListAdapter(this, 30, "title", "type", "content.............................................."));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //右侧滑入动画
                startActivity(new Intent(ActivitySlideActivity.this, SubActivitySlideActivity.class));
                overridePendingTransition(R.anim.in_from_right, 0);
            }
        });
    }
}
