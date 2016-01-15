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

package sviolet.turquoise.modelx.bitmaploader.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import java.lang.ref.WeakReference;

import sviolet.turquoise.modelx.bitmaploader.SimpleBitmapLoaderTask;

/**
 * [抽象]动态加载图<p/>
 *
 * 实现onDrawBackground(), 绘制背景图.<Br/>
 * 实现onDrawAnimation(), 绘制加载动画. 父类已实现根据加载任务状态, 决定是否需要绘制和刷新.<br/>
 * 实现onDrawFailedBitmap(), 绘制加载失败图.<p/>
 *
 * 实现参考{@link DefaultLoadingDrawableFactory.DefaultLoadingDrawable}<p/>
 *
 * Created by S.Violet on 2015/11/17.
 */

public abstract class AbsLoadingDrawable extends Drawable {

    private WeakReference<SimpleBitmapLoaderTask> loaderTask;//加载任务

    public AbsLoadingDrawable setLoaderTask(SimpleBitmapLoaderTask loaderTask){
        this.loaderTask = new WeakReference<SimpleBitmapLoaderTask>(loaderTask);
        return this;
    }

    private boolean isLoading() {
        return loaderTask != null && loaderTask.get() != null
                && loaderTask.get().getState() <= SimpleBitmapLoaderTask.STATE_LOADING;
    }

    @Override
    public final void draw(Canvas canvas) {
        //绘制背景/加载错误图
        onDrawBackground(canvas);

        if (isLoading()){
            //加载时绘制加载动画
            onDrawAnimation(canvas);
            invalidateSelf();
        }else{
            //绘制加载失败图
            onDrawFailedBitmap(canvas);
        }
    }

    public abstract void onDrawBackground(Canvas canvas);

    public abstract void onDrawAnimation(Canvas canvas);

    public abstract void onDrawFailedBitmap(Canvas canvas);

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

}
