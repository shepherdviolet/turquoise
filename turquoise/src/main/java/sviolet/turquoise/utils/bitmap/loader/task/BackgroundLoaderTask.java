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

package sviolet.turquoise.utils.bitmap.loader.task;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import sviolet.turquoise.utils.bitmap.loader.SimpleBitmapLoader;
import sviolet.turquoise.utils.bitmap.loader.SimpleBitmapLoaderTask;
import sviolet.turquoise.utils.sys.DeviceUtils;

/**
 * View背景异步加载任务<br/>
 * Created by S.Violet on 2015/10/19.
 */
class BackgroundLoaderTask extends SimpleBitmapLoaderTask<View> {

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

    @Override
    protected Drawable getDrawable(View view) {
        return view.getBackground();
    }
}
