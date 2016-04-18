package sviolet.demoaimageloader.demos;

import android.os.Bundle;
import android.widget.ImageView;

import java.util.concurrent.locks.LockSupport;

import sviolet.demoaimageloader.R;
import sviolet.demoaimageloader.common.DemoDescription;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.TILoader;
import sviolet.turquoise.x.imageloader.drawable.ContainerDrawable;
import sviolet.turquoise.x.imageloader.drawable.def.DefaultLoadingDrawableFactory;

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

        TILoader.node(this).load("https://avatars0.githubusercontent.com/u/12589661?v=3&s=460", imageView);

//        final DefaultLoadingDrawableFactory factory = new DefaultLoadingDrawableFactory();
//        factory.setBackgroundResId(R.mipmap.ic_launcher);
//        factory.setBackgroundColor(0xFF702020);
//        factory.setPointColor(0xFFFF0000);
//        factory.setAnimationEnabled(false);

//        imageView.setImageDrawable(new ContainerDrawable(factory.create(getApplicationContext(), this, null, TLogger.get(this))));
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                LockSupport.parkNanos(10 * 1000000000L);
//                factory.onDestroy();
//            }
//        }).start();

    }
}
