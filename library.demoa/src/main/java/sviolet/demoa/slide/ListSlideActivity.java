package sviolet.demoa.slide;

import android.os.Bundle;
import android.widget.ListView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.slide.sviolet.demoa.slide.view.SlideListAdapter;
import sviolet.turquoise.annotation.ActivitySettings;
import sviolet.turquoise.annotation.ResourceId;
import sviolet.turquoise.app.TActivity;

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

        mSlideListView.setAdapter(new SlideListAdapter(this, 30, "title", "type", "info.............."));

    }
}
