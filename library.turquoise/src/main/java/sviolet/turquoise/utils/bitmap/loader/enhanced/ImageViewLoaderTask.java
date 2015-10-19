package sviolet.turquoise.utils.bitmap.loader.enhanced;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by S.Violet on 2015/10/19.
 */
public class ImageViewLoaderTask extends SimpleBitmapLoaderTask<ImageView> {


    public ImageViewLoaderTask(String url, int reqWidth, int reqHeight, SimpleBitmapLoader loader, ImageView view) {
        super(url, reqWidth, reqHeight, loader, view);
    }

    @Override
    protected void setDrawable(ImageView view, Drawable drawable) {
        view.setImageDrawable(drawable);
    }

}
