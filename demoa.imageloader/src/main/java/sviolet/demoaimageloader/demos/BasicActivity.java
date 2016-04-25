package sviolet.demoaimageloader.demos;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

import sviolet.demoaimageloader.R;
import sviolet.demoaimageloader.common.DemoDescription;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.TILoader;
import sviolet.turquoise.x.imageloader.TILoaderUtils;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;
import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.entity.Params;

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

    @ResourceId(R.id.basic_main_imageview1)
    private ImageView imageView1;
    @ResourceId(R.id.basic_main_imageview2)
    private ImageView imageView2;
    @ResourceId(R.id.basic_main_imageview3)
    private ImageView imageView3;

    //loading params
//    private Params params = new Params.Builder()
//            .setBitmapConfig(Bitmap.Config.ARGB_8888)
//            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //wipe disk cache
        try {
            TILoaderUtils.wipeDiskCache(this, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //node setting, you should setting before load !
        TILoader.node(this).setting(new NodeSettings.Builder().build());

        //load into imageView
        TILoader.node(this).load("https://raw.githubusercontent.com/shepherdviolet/static-resources/master/image/logo/slate.jpg", imageView1);
        TILoader.node(this).load("https://raw.githubusercontent.com/shepherdviolet/static-resources/master/image/logo/turquoise.jpg", imageView2);

        //extract mode, get a bitmap
        TILoader.extract(this, "https://raw.githubusercontent.com/shepherdviolet/static-resources/master/image/logo/violet.jpg", null, new OnLoadedListener<ImageView>() {
            @Override
            public void onLoadSucceed(String url, Params params, ImageResource<?> resource) {
                ImageView imageView = getWeakRegister();
                if (imageView != null) {
                    Toast.makeText(BasicActivity.this, "load succeed", Toast.LENGTH_SHORT).show();
                    imageView.setImageDrawable(TILoaderUtils.imageResourceToDrawable(BasicActivity.this, resource, true));
                }else{
                    TLogger.get(this).e("imageView is recycled, can't show image");
                }
            }
            @Override
            public void onLoadCanceled(String url, Params params) {
                Toast.makeText(BasicActivity.this, "load failed", Toast.LENGTH_SHORT).show();
            }
        }.setWeakRegister(imageView3));

        //reload test
        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TILoaderUtils.wipeMemoryCache();
                imageView1.invalidate();
                imageView2.invalidate();
                imageView3.invalidate();
            }
        });

    }
}
