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

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.EmulateListAdapter;
import sviolet.demoa.slide.view.CardSlideTitleView;
import sviolet.demoa.slide.view.CardSlideView;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.uix.slideengine.view.AdaptListView;

/**
 * 卡片Demo
 * Created by S.Violet on 2015/6/9.
 */

@DemoDescription(
        title = "CardSlide",
        type = "View",
        info = "Card slide view made by sviolet.turquoise.slide"
)

@ResourceId(R.layout.slide_card)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class CardSlideActivity extends TActivity{

        @ResourceId(R.id.slide_card_cardview)
        private CardSlideView cardSlideView;//卡片控件
        @ResourceId(R.id.slide_card_cardview_listview)
        private AdaptListView cardListView;//卡片中的ListView
        @ResourceId(R.id.slide_card_cardview_title)
        private CardSlideTitleView cardTitleView;//卡片中的标题
        @ResourceId(R.id.slide_card_listview)
        private ListView listView;

        @Override
        protected void onInitViews(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                cardListView.setAdapter(new EmulateListAdapter(this, 30, "卡片内容", "no.","The card content"));

                cardSlideView.bindListView(cardListView);
                cardSlideView.bindTitleView(cardTitleView);

                listView.setAdapter(new EmulateListAdapter(this, 30, "目录标题", "no.","The menu content"));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                cardSlideView.show();
                        }
                });

        }

}
