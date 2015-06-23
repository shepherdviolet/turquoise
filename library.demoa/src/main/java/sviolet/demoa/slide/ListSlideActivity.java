package sviolet.demoa.slide;

import android.os.Bundle;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
