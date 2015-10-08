package sviolet.demoa.image;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.turquoise.enhance.TActivity;
import sviolet.turquoise.enhance.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.annotation.setting.ActivitySettings;

@DemoDescription(
        title = "Common Image Demo",
        type = "Image",
        info = "an Async. Image Demo"
)

/**
 * 简易的图片动态加载Demo<br/>
 * 1.SafeBitmapDrawable示例
 * 2.AbstractBitmapLoader,setDuplicateLoadEnable示例
 * 3.CachedBitmapUtils示例
 *
 * Created by S.Violet on 2015/7/7.
 */
@ResourceId(R.layout.image_async)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class CommonImageActivity extends TActivity {
}
