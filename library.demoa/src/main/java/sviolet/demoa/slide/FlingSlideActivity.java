package sviolet.demoa.slide;

import android.os.Bundle;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.slide.view.FlingView;
import sviolet.turquoise.enhance.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.TActivity;

@DemoDescription(
        title = "Fling",
        type = "View",
        info = "this is a Fling demo made by turquoise.view.slide"
)

/**
 *
 * Created by S.Violet on 2015/6/7.
 */

@ResourceId(R.layout.slide_fling)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class FlingSlideActivity extends TActivity {

    @ResourceId(R.id.slide_fling_flingview)
    private FlingView flingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
