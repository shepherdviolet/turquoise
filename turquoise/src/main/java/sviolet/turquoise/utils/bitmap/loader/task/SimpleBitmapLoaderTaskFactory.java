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

import android.view.View;
import android.widget.ImageView;

import sviolet.turquoise.utils.bitmap.loader.SimpleBitmapLoader;
import sviolet.turquoise.utils.bitmap.loader.SimpleBitmapLoaderTask;
import sviolet.turquoise.utils.bitmap.loader.entity.BitmapRequest;

/**
 * SimpleBitmapLoader加载任务工厂<p/>
 *
 * 目前支持:<br/>
 * 1.View背景图<br/>
 * 2.ImageView<br/>
 *
 * Created by S.Violet on 2015/11/5.
 */
public class SimpleBitmapLoaderTaskFactory {

    /**
     * 控件图片加载任务
     */
    public static SimpleBitmapLoaderTask newLoaderTask(BitmapRequest request, SimpleBitmapLoader loader, View view){
        if (view instanceof ImageView){
            return new ImageViewLoaderTask(request, loader, (ImageView)view);
        }
        throw new RuntimeException("[SimpleBitmapLoaderTaskFactory]This view is not supported to load by SimpleBitmapLoader, use BitmapLoader please");
    }

    /**
     * 控件背景图片加载任务
     */
    public static SimpleBitmapLoaderTask newBackgroundLoaderTask(BitmapRequest request, SimpleBitmapLoader loader, View view){
        return new BackgroundLoaderTask(request, loader, view);
    }

}
