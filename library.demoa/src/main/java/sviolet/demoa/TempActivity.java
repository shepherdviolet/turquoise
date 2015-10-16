package sviolet.demoa;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import sviolet.demoa.common.DemoDescription;
import sviolet.turquoise.enhanced.TActivity;
import sviolet.turquoise.enhanced.annotation.inject.ResourceId;
import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;
import sviolet.turquoise.view.drawable.TransitionBitmapDrawable;
import sviolet.turquoise.utils.bitmap.loader.enhanced.AsyncBitmapDrawableLoader;

/**
 * 临时调试用Activity
 */

@DemoDescription(
        title = "TempActivity",
        type = "Temp",
        info = "the activity for test"
)

@ResourceId(R.layout.temp_main)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class TempActivity extends TActivity {

    private AsyncBitmapDrawableLoader loader;

    @ResourceId(R.id.temp_imageview)
    ImageView imageView;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        try {
////            Bitmap bitmap = getCachedBitmapUtils().decodeFromResource(null, getResources(), R.mipmap.async_image_1, 500, 500);
//            loader = new AsyncBitmapDrawableLoader(getApplicationContext(), "tempActivity", null, new SimpleBitmapLoaderImplementor(60000))
//                .open();
//
//            imageView.setImageDrawable(loader.load("http://avatar.csdn.net/3/2/4/1_ameyume.jpg", 400, 400));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        final TransitionBitmapDrawable drawable = new TransitionBitmapDrawable();
        final Bitmap nullBitmap = getCachedBitmapUtils().decodeFromResource(null, getResources(), R.mipmap.async_image_null);
        final Bitmap bitmap1 = getCachedBitmapUtils().decodeFromResource(null, getResources(), R.mipmap.async_image_1, 500, 500);
        final TransitionBitmapDrawable drawable = new TransitionBitmapDrawable(null, null);
        imageView.setImageDrawable(drawable);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("1");
                drawable.setBitmap(getResources(), bitmap1, 500);
//                Bitmap bitmap = getCachedBitmapUtils().decodeFromResource(null, getResources(), R.mipmap.async_image_1);
//                drawable.addLayer(new BitmapDrawable(bitmap));
//                drawable.startTransition(500);
//                drawable.startTransition(500);
            }
        }, 1000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("2");
//                drawable.setBitmap(getResources(), getCachedBitmapUtils().decodeFromResource(null, getResources(), R.mipmap.async_image_2, 100, 100), 500);
//                Bitmap bitmap = getCachedBitmapUtils().decodeFromResource(null, getResources(), R.mipmap.async_image_1);
//                drawable.addLayer(new BitmapDrawable(bitmap));
//                drawable.startTransition(500);
//                drawable.startTransition(500);
                bitmap1.recycle();
                imageView.postInvalidate();
            }
        }, 2000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("3");
//                drawable.setBitmap(getResources(), getCachedBitmapUtils().decodeFromResource(null, getResources(), R.mipmap.async_image_3, 50, 50), 500);
//                Bitmap bitmap = getCachedBitmapUtils().decodeFromResource(null, getResources(), R.mipmap.async_image_1);
//                drawable.addLayer(new BitmapDrawable(bitmap));
//                drawable.startTransition(500);
//                drawable.startTransition(500);
                nullBitmap.recycle();
                imageView.postInvalidate();
            }
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
