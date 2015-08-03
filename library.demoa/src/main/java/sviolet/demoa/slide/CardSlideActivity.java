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
import sviolet.turquoise.annotation.app.ActivitySettings;
import sviolet.turquoise.annotation.inject.ResourceId;
import sviolet.turquoise.app.TActivity;
import sviolet.turquoise.view.slide.view.AdaptListView;

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
        protected void onCreate(Bundle savedInstanceState) {
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
