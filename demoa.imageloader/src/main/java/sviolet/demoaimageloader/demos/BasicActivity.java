package sviolet.demoaimageloader.demos;

import android.os.Bundle;
import android.widget.ImageView;

import java.io.IOException;

import sviolet.demoaimageloader.R;
import sviolet.demoaimageloader.common.DemoDescription;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.x.imageloader.TILoader;
import sviolet.turquoise.x.imageloader.TILoaderUtils;
import sviolet.turquoise.x.imageloader.entity.ServerSettings;

/**
 * Basic Usage of TurquoiseImageLoader
 * <p/>
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

//    private Params params = new Params.Builder()
//            .setBitmapConfig(Bitmap.Config.ARGB_8888)
//            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TILoader.setting(new ServerSettings.Builder().setMemoryCachePercent(this, 0.001f).build());

        try {
            TILoaderUtils.wipeDiskCache(this, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        TILoader.node(this).load("https://avatars0.githubusercontent.com/u/12589661?v=3&s=460", imageView);

    }
}
