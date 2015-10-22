package sviolet.turquoise.utils.bitmap.loader.enhanced;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import sviolet.turquoise.utils.sys.DeviceUtils;

/**
 * Created by S.Violet on 2015/10/19.
 */
public class BackgroundLoaderTask extends SimpleBitmapLoaderTask<View> {

    BackgroundLoaderTask(String url, int reqWidth, int reqHeight, SimpleBitmapLoader loader, View view) {
        super(url, reqWidth, reqHeight, loader, view);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void setDrawable(View view, Drawable drawable) {
        if (DeviceUtils.getVersionSDK() >= 16) {
            view.setBackground(drawable);
        }else{
            view.setBackgroundDrawable(drawable);
        }
    }
}
