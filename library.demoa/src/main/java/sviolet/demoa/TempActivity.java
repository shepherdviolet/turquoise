package sviolet.demoa;

import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;

import sviolet.demoa.common.DemoDescription;
import sviolet.turquoise.enhance.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.TActivity;
import sviolet.turquoise.utils.bitmap.BitmapUtils;
import sviolet.turquoise.utils.bitmap.loader.AsyncBitmapDrawable;

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

    @ResourceId(R.id.temp_imageview)
    ImageView imageView;

    @Override
    protected void onResume() {
        super.onResume();
        final Bitmap bitmap = BitmapUtils.decodeFromResource(getResources(), R.mipmap.ic_launcher);
        final Bitmap defaultBitmap = BitmapUtils.decodeFromResource(getResources(), R.mipmap.async_image_null);
        AsyncBitmapDrawable asyncBitmapDrawable = new AsyncBitmapDrawable(bitmap, defaultBitmap);
        imageView.setImageDrawable(asyncBitmapDrawable);
//        imageView.setImageBitmap(bitmap);
//        imageView.setImageResource(R.mipmap.ic_launcher);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bitmap.recycle();
//                defaultBitmap.recycle();
                imageView.postInvalidate();
            }
        }, 1000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                bitmap.recycle();
                defaultBitmap.recycle();
                imageView.postInvalidate();
            }
        }, 3000);
    }

}
