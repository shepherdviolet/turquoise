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
 */

package sviolet.demoa.slide;

import android.os.Bundle;
import android.widget.ListView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.slide.view.MySlideListAdapter;
import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhanced.annotation.inject.ResourceId;
import sviolet.turquoise.enhanced.TActivity;

/**
 * Item可滑动的ListView Demo
 * Created by S.Violet on 2015/6/17.
 */

@DemoDescription(
        title = "SlideListView",
        type = "View",
        info = "a listview contain slideview"
)

@ResourceId(R.layout.slide_list)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class ListSlideActivity extends TActivity{

    @ResourceId(R.id.slide_list_listview)
    private ListView mSlideListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSlideListView.setAdapter(new MySlideListAdapter(this, 30, "title", "type", "info.............."));

    }
}
