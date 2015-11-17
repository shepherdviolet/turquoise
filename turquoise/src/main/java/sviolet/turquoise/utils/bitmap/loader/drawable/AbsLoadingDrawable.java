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

package sviolet.turquoise.utils.bitmap.loader.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import java.lang.ref.WeakReference;

import sviolet.turquoise.utils.bitmap.loader.SimpleBitmapLoaderTask;

/**
 * [抽象]动态加载图<p/>
 *
 * 实现onDraw()方法, 绘制动态图. 父类已实现根据加载任务状态, 确定是否需要刷新显示.<p/>
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

    public boolean isLoading() {
        return loaderTask != null && loaderTask.get() != null
                && loaderTask.get().getState() <= SimpleBitmapLoaderTask.STATE_LOADING;
    }

    @Override
    public final void draw(Canvas canvas) {
        //任务加载中刷新显示
        if (isLoading()){
            onDraw(canvas);
            invalidateSelf();
        }
    }

    public abstract void onDraw(Canvas canvas);

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
