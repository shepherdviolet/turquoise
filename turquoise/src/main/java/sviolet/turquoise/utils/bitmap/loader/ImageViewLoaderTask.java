/*
 * Copyright (C) 2015 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.turquoise.utils.bitmap.loader;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * ImageView异步加载任务<br/>
 * Created by S.Violet on 2015/10/19.
 */
public class ImageViewLoaderTask extends SimpleBitmapLoaderTask<ImageView> {


    ImageViewLoaderTask(String url, int reqWidth, int reqHeight, SimpleBitmapLoader loader, ImageView view) {
        super(url, reqWidth, reqHeight, loader, view);
    }

    @Override
    protected void setDrawable(ImageView view, Drawable drawable) {
        view.setImageDrawable(drawable);
    }

    @Override
    protected Drawable getDrawable(ImageView view) {
        return view.getDrawable();
    }
}
