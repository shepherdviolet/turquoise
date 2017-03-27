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

package sviolet.demoaimageloader.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.drawable.FailedDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.ResourceBitmapWrapper;
import sviolet.turquoise.x.imageloader.drawable.TIBitmapDrawable;
import sviolet.turquoise.x.imageloader.drawable.TIColorDrawable;
import sviolet.turquoise.x.imageloader.entity.Params;

/**
 * 自定义实现加载失败图工厂:根据CommonFailedDrawableFactory适当修改.
 *
 * 注意::
 * 实现LoadingDrawableFactory/FailedDrawableFactory时, 必须使用{@link TIBitmapDrawable}代替{@link BitmapDrawable}!!!
 * 因为部分机型在绘制被回收的Bitmap时不会抛出异常, TILoader是根据异常来判断是否进行重新加载, 因此在个别机型上如果使用
 * {@link BitmapDrawable}, 有可能会出现图片显示错误但不会重新加载的情况. {@link TIBitmapDrawable}解决了这个问题.
 *
 * 注意::
 * 失败图的尺寸建议固定为请求尺寸(params.getReqWidth()/params.getReqHeight()), 这样可以达到较好的显示效果.
 *
 * Created by S.Violet on 2016/4/28.
 */
public class MyFailedDrawableFactory implements FailedDrawableFactory {

    public static final int DEFAULT_COLOR = 0x20000000;

    //Bitmap包装类, 维护一个单例的Bitmap, 便于销毁
    private ResourceBitmapWrapper bitmap = new ResourceBitmapWrapper();
    private int color = DEFAULT_COLOR;

    @Override
    public Drawable create(Context applicationContext, Context context, Params params, TLogger logger) {
        //失败图的尺寸完全由请求尺寸决定
        int drawableWidth = params.getReqWidth();
        int drawableHeight = params.getReqHeight();

        //Bitmap存在则使用图片
        Bitmap bitmap = this.bitmap.getBitmap(applicationContext.getResources(), logger);
        if (bitmap != null && !bitmap.isRecycled()){
            //必须使用TIBitmapDrawable代替BitmapDrawable
            //设置为固定尺寸(请求尺寸)
            return new TIBitmapDrawable(applicationContext.getResources(), bitmap).setFixedSize(drawableWidth, drawableHeight);
        }
        //Bitmap不存在则使用颜色
        //设置为固定尺寸(请求尺寸)
        return new TIColorDrawable(color).setFixedSize(drawableWidth, drawableHeight);
    }

    @Override
    public void onDestroy() {
        //销毁Bitmap
        bitmap.destroy();
    }

    public MyFailedDrawableFactory setImageResId(int resId) {
        this.bitmap.setResId(resId);
        return this;
    }

    public MyFailedDrawableFactory setColor(int color) {
        this.color = color;
        return this;
    }

}
