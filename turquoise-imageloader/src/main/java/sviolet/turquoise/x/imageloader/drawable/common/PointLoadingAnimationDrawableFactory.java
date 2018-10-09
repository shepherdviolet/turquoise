/*
 * Copyright (C) 2015-2017 S.Violet
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

package sviolet.turquoise.x.imageloader.drawable.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import sviolet.turquoise.util.common.DateTimeUtilsForAndroid;
import sviolet.turquoise.x.common.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.LoadProgress;
import sviolet.turquoise.x.imageloader.entity.Params;

/**
 * Point style loading animation drawable factory for CommonLoadingDrawableFactory
 *
 * Created by S.Violet on 2016/5/6.
 */
public class PointLoadingAnimationDrawableFactory implements CommonLoadingDrawableFactory.AnimationDrawableFactory {

    private AnimationSettings settings = new AnimationSettings();

    @Override
    public Drawable create(Context applicationContext, Context context, Params params, LoadProgress.Info progressInfo, TLogger logger) {
        return new PointAnimationDrawable(settings);
    }

    public PointLoadingAnimationDrawableFactory setPointColor(int color){
        settings.pointColor = color;
        return this;
    }

    /**
     * @param radius px
     */
    public PointLoadingAnimationDrawableFactory setPointRadius(int radius){
        settings.pointRadius = radius;
        return this;
    }

    /**
     * @param interval px
     */
    public PointLoadingAnimationDrawableFactory setPointInterval(int interval){
        settings.pointInterval = interval;
        return this;
    }

    /**
     * @param duration ms
     */
    public PointLoadingAnimationDrawableFactory setAnimationDuration(long duration){
        settings.animationDuration = duration;
        return this;
    }

    /**
     * @param offsetX 0f~1f, position of width
     */
    public PointLoadingAnimationDrawableFactory setPointOffsetX(float offsetX){
        settings.pointOffsetX = offsetX;
        return this;
    }

    /**
     * @param offsetY 0f~1f, position of height
     */
    public PointLoadingAnimationDrawableFactory setPointOffsetY(float offsetY){
        settings.pointOffsetY = offsetY;
        return this;
    }

    /**
     * settings of animation factory
     */
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

    /**
     * point AnimationDrawable
     */
    public static class PointAnimationDrawable extends Drawable {

        private AnimationSettings settings;

        private static final int QUANTITY = 3;
        private static final int MIN_POSITION = -2;
        private static final int MAX_POSITION = 4;
        private long startTime = DateTimeUtilsForAndroid.getUptimeMillis();

        private Paint paint;

        //优化性能
        private Rect canvasBounds = new Rect();

        public PointAnimationDrawable(AnimationSettings settings) {
            this.settings = settings;
            initPaint();
        }

        @Override
        public void draw(Canvas canvas) {
            //skip
            if(settings.pointRadius <= 0 || settings.pointInterval < 0 || settings.animationDuration <= 0) {
                return;
            }

            //draw
            drawCircle(canvas, calculateCurrentPosition());
        }

        private int calculateCurrentPosition(){
            final float progress = (float)((DateTimeUtilsForAndroid.getUptimeMillis() - startTime) % settings.animationDuration) / (float)settings.animationDuration;
            final int distance = MAX_POSITION - MIN_POSITION;
            final int position = (int) ((2 * distance + 1) * progress);
            if (position <= distance){
                return MIN_POSITION + position;
            }else{
                return MIN_POSITION + distance - (position - distance - 1);
            }
        }

        private void drawCircle(Canvas canvas, int currentPosition) {
            Rect drawBounds = getBounds();
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
            paint.setStyle(Paint.Style.FILL);
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
