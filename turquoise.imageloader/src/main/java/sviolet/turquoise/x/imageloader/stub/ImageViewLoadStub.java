/*
 * Copyright (C) 2015-2016 S.Violet
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

package sviolet.turquoise.x.imageloader.stub;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import sviolet.turquoise.x.imageloader.entity.Params;

/**
 * <p>loading image to ImageView</p>
 *
 * Created by S.Violet on 2016/2/25.
 */
class ImageViewLoadStub extends LoadStub<ImageView> {

    ImageViewLoadStub(String url, Params params, ImageView view){
        super(url, params, view);
    }

    @Override
    protected void setDrawableToView(Drawable drawable, ImageView view) {
        view.setImageDrawable(drawable);
    }

}
