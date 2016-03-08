package sviolet.demoaimageloader.demos;

import android.os.Bundle;
import android.widget.ImageView;

import sviolet.demoaimageloader.R;
import sviolet.demoaimageloader.common.DemoDescription;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;

/**
 * Basic Usage of TurquoiseImageLoader
 *
 * Created by S.Violet on 2016/3/8.
 */

@DemoDescription(
        title = "Basic Usage",
        type = "",
        info = "Basic Usage of TurquoiseImageLoader"
)

@ResourceId(R.layout.basic_main)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class BasicActivity extends TActivity {

    @ResourceId(R.id.basic_main_imageview)
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        TILoader.node(this).load("url", imageView);

    }
}
