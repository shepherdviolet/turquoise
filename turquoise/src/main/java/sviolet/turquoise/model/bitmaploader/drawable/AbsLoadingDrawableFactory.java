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

package sviolet.turquoise.model.bitmaploader.drawable;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

/**
 * [抽象]动态加载图工厂<p/>
 *
 * 1.实现newLoadingDrawable()方法, 返回新的动态图实例. 用于加载时的动态图显示.<br/>
 * 2.实现destroy(), 用于销毁资源, 特别是Bitmap.<br/>
 * 3.实现newBackgroundDrawable(), 返回新的动态图背景实例, 用于目的图显示时的淡入效果背景,
 * 注意尺寸需为match_parent, 保证加载成功的图片, 尺寸等于目的图. 可不复写.<p/>
 *
 * 注意: Bitmap需由工厂持有, 便于getBackgroundDrawable()返回给加载任务, 便于destroy()销毁Bitmap.<p/>
 *
 * 实现参考{@link DefaultLoadingDrawableFactory}<p/>
 *
 * Created by S.Violet on 2015/11/17.
 */
public abstract class AbsLoadingDrawableFactory {

    public abstract AbsLoadingDrawable newLoadingDrawable();

    public abstract void destroy();

    public Drawable newBackgroundDrawable(){
        return new ColorDrawable(0x00000000);
    }

}