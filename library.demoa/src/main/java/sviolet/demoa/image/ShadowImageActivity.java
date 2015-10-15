package sviolet.demoa.image;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.turquoise.enhance.TActivity;
import sviolet.turquoise.enhance.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.annotation.setting.ActivitySettings;

@DemoDescription(
        title = "Shadow Demo",
        type = "Image",
        info = "add shadow to a view"
)

/**
 * 给控件加上阴影<br/>
 *
 * Created by S.Violet on 2015/7/7.
 */
@ResourceId(R.layout.image_shadow)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class ShadowImageActivity extends TActivity {

}
