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
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import sviolet.turquoise.util.common.DateTimeUtilsForAndroid;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.drawable.common.CommonLoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.entity.LoadProgress;
import sviolet.turquoise.x.imageloader.entity.Params;

/**
 * 自定义实现加载动画工厂:根据CommonAnimationDrawableFactory适当修改.
 *
 * 实现要点:
 * 1.实现CommonLoadingDrawableFactory.AnimationDrawableFactory.create(...)方法, 返回自定义的动画Drawable.
 * 2.实现动画Drawable.draw(...), 绘制动画.
 *
 * Created by S.Violet on 2016/4/28.
 */
public class MyAnimationDrawableFactory implements CommonLoadingDrawableFactory.AnimationDrawableFactory {

    private AnimationSettings settings = new AnimationSettings();

    @Override
    public Drawable create(Context applicationContext, Context context, Params params, LoadProgress.Info progressInfo, TLogger logger) {
        //返回Drawable
        return new MyAnimationDrawable(settings);
    }

    public MyAnimationDrawableFactory setPointColor(int color){
        settings.pointColor = color;
        return this;
    }

    public MyAnimationDrawableFactory setPointRadius(int radius){
        settings.pointRadius = radius;
        return this;
    }

    public MyAnimationDrawableFactory setPointInterval(int interval){
        settings.pointInterval = interval;
        return this;
    }

    public MyAnimationDrawableFactory setAnimationDuration(long duration){
        settings.animationDuration = duration;
        return this;
    }

    public MyAnimationDrawableFactory setPointOffsetX(float offsetX){
        settings.pointOffsetX = offsetX;
        return this;
    }

    public MyAnimationDrawableFactory setPointOffsetY(float offsetY){
        settings.pointOffsetY = offsetY;
        return this;
    }

    protected static class AnimationSettings{

        static final int COLOR_DEF = 0xFFC0C0C0;
        static final int RADIUS_DEF = 10;
        static final int INTERVAL_DEF = 40;
        static final int DURATION = 1000;
        static final float OFFSET_X_DEF = 0.5f;
        static final float OFFSET_Y_DEF = 0.5f;

        int pointColor = COLOR_DEF;
        int pointRadius = RADIUS_DEF;
        int pointInterval = INTERVAL_DEF;
        long animationDuration = DURATION;
        float pointOffsetX = OFFSET_X_DEF;
        float pointOffsetY = OFFSET_Y_DEF;

    }

    public static class MyAnimationDrawable extends Drawable {

        private AnimationSettings settings;

        private static final int QUANTITY = 3;
        private static final int MIN_POSITION = -2;
        private static final int MAX_POSITION = 4;
        private long startTime = DateTimeUtilsForAndroid.getUptimeMillis();

        private Paint paint;

        public MyAnimationDrawable(AnimationSettings settings) {
            this.settings = settings;
            initPaint();
        }

        /**
         * 实现绘制流程
         */
        @Override
        public void draw(Canvas canvas) {
            //skip
            if(settings.pointRadius <= 0 || settings.pointInterval < 0 || settings.animationDuration <= 0)
                return;

            //draw
            drawCircle(canvas, calculateCurrentPosition());
        }

        private int calculateCurrentPosition(){
            final float progress = (float)((DateTimeUtilsForAndroid.getUptimeMillis() - startTime) % settings.animationDuration) / (float)settings.animationDuration;
            final int distance = MAX_POSITION - MIN_POSITION;
            final int position = (int) (distance * progress);
            return MIN_POSITION + position;
        }

        private void drawCircle(Canvas canvas, int currentPosition) {
            Rect drawBounds = getBounds();
            Rect canvasBounds = new Rect();
            canvas.getClipBounds(canvasBounds);

            final float scale = (float)canvasBounds.width() / (float)canvas.getWidth();//calculate scale of canvas

            final int length = (int) ((QUANTITY - 1) * settings.pointInterval * scale);

            int x = (int) (drawBounds.left + drawBounds.width() * settings.pointOffsetX - length / 2);
            int y = (int) (drawBounds.top + drawBounds.height() * settings.pointOffsetY);

            for(int i = 0 ; i < QUANTITY ; i++){
                canvas.drawCircle(x, y, i == currentPosition ? (float) (settings.pointRadius * 1.5 * scale) : settings.pointRadius * scale, paint);
                x += settings.pointInterval * scale;
            }
        }

        private void initPaint() {
            paint = new Paint();
            paint.setColor(settings.pointColor);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(settings.pointRadius / 3);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }
    }

}
