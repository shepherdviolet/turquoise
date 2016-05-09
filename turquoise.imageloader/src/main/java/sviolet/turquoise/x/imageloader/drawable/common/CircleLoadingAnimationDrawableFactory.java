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

package sviolet.turquoise.x.imageloader.drawable.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import sviolet.turquoise.util.common.DateTimeUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.LoadProgress;
import sviolet.turquoise.x.imageloader.entity.Params;

/**
 * Circle style loading animation drawable factory for CommonLoadingDrawableFactory
 *
 * Created by S.Violet on 2016/5/6.
 */
public class CircleLoadingAnimationDrawableFactory implements CommonLoadingDrawableFactory.AnimationDrawableFactory {

    private AnimationSettings settings = new AnimationSettings();

    /**
     * set radius of circle
     * @param radius radius value, >0
     * @param unit {@link SizeUnit}
     */
    public CircleLoadingAnimationDrawableFactory setRadius(float radius, SizeUnit unit){
        if (unit == null){
            throw new RuntimeException("[CircleLoadingAnimationDrawableFactory]SizeUnit must not be null");
        }
        if (radius <= 0){
            throw new RuntimeException("[CircleLoadingAnimationDrawableFactory]radius must > 0");
        }
        settings.radiusUnit = unit;
        settings.radius = radius;
        return this;
    }

    /**
     * set color of circle
     * @param color color
     */
    public CircleLoadingAnimationDrawableFactory setCircleColor(int color){
        settings.circleColor = color;
        return this;
    }

    /**
     * set strokeWidth of circle
     * @param strokeWidth >0
     * @param unit {@link SizeUnit}
     */
    public CircleLoadingAnimationDrawableFactory setCircleStrokeWidth(float strokeWidth, SizeUnit unit){
        if (unit == null){
            throw new RuntimeException("[CircleLoadingAnimationDrawableFactory]SizeUnit must not be null");
        }
        if (strokeWidth <= 0){
            throw new RuntimeException("[CircleLoadingAnimationDrawableFactory]strokeWidth must > 0");
        }
        settings.circleStrokeUnit = unit;
        settings.circleStrokeWidth = strokeWidth;
        return this;
    }

    /**
     * set color of progress
     * @param color color
     */
    public CircleLoadingAnimationDrawableFactory setProgressColor(int color){
        settings.progressColor = color;
        return this;
    }

    /**
     * set strokeWidth of progress
     * @param strokeWidth >0
     * @param unit {@link SizeUnit}
     */
    public CircleLoadingAnimationDrawableFactory setProgressStrokeWidth(float strokeWidth, SizeUnit unit){
        if (unit == null){
            throw new RuntimeException("[CircleLoadingAnimationDrawableFactory]SizeUnit must not be null");
        }
        if (strokeWidth <= 0){
            throw new RuntimeException("[CircleLoadingAnimationDrawableFactory]strokeWidth must > 0");
        }
        settings.progressStrokeUnit = unit;
        settings.progressStrokeWidth = strokeWidth;
        return this;
    }

    /**
     * set duration of animation
     * @param duration ms
     */
    public CircleLoadingAnimationDrawableFactory setAnimationDuration(long duration){
        if (duration <= 0){
            throw new RuntimeException("[CircleLoadingAnimationDrawableFactory]duration must > 0");
        }
        settings.duration = duration;
        return this;
    }

    @Override
    public Drawable create(Context applicationContext, Context context, Params params, LoadProgress.Info progressInfo, TLogger logger) {
        return new CircleAnimationDrawable(settings, progressInfo);
    }

    /**
     * settings of animation factory
     */
    protected static class AnimationSettings{

        private SizeUnit radiusUnit = SizeUnit.PERCENT_OF_WIDTH;
        private float radius = 0.15f;
        private int circleColor = 0x20000000;
        private SizeUnit circleStrokeUnit = SizeUnit.PERCENT_OF_WIDTH;
        private float circleStrokeWidth = 0.012f;
        private int progressColor = 0x40000000;
        private SizeUnit progressStrokeUnit = SizeUnit.PERCENT_OF_WIDTH;
        private float progressStrokeWidth = 0.015f;
        private long duration = 1000;//ms, >0

    }

    /**
     * <p>PERCENT_OF_WIDTH:: percent of drawable width, realValue = value * getBounds().width()</p>
     * <p>PERCENT_OF_HEIGHT:: percent of drawable height, realValue = value * getBounds().height()</p>
     * <p>PX:: pixel, realValue = value</p>
     */
    public enum SizeUnit {
        PERCENT_OF_WIDTH,
        PERCENT_OF_HEIGHT,
        PX
    }

    /**
     * circle AnimationDrawable
     */
    public static class CircleAnimationDrawable extends Drawable {

        private AnimationSettings settings;
        private LoadProgress.Info progressInfo;

        private long startTime = 0;
        private float displayProgress = 0f;

        private Paint circlePaint;
        private Paint progressPaint;

        public CircleAnimationDrawable(AnimationSettings settings, LoadProgress.Info progressInfo) {
            this.settings = settings;
            this.progressInfo = progressInfo;
            initPaint();
        }

        private void initPaint() {
            circlePaint = new Paint();
            circlePaint.setColor(settings.circleColor);
            circlePaint.setAntiAlias(true);
            circlePaint.setStyle(Paint.Style.STROKE);

            progressPaint = new Paint();
            progressPaint.setColor(settings.progressColor);
            progressPaint.setAntiAlias(true);
            progressPaint.setStyle(Paint.Style.STROKE);
        }

        @Override
        public void draw(Canvas canvas) {
            if (getBounds().width() <= 0 || getBounds().height() <= 0){
                return;
            }
            if (startTime == 0){
                startTime = DateTimeUtils.getUptimeMillis();
            }
            if (progressInfo.total() <= 0){
                drawByDuration(canvas);
            }else{
                drawByProgress(canvas);
            }
            invalidateSelf();
        }

        private void drawByDuration(Canvas canvas){
            final long elapseTime = DateTimeUtils.getUptimeMillis() - startTime;
            final float progress = (float)(elapseTime % settings.duration) / (float)settings.duration;
            final int centerX = (getBounds().left + getBounds().right) / 2;
            final int centerY = (getBounds().top + getBounds().bottom) / 2;
            float radius = calculateSizeByUnit(settings.radius, settings.radiusUnit);
            float circleStrokeWidth = calculateSizeByUnit(settings.circleStrokeWidth, settings.circleStrokeUnit);
            float progressStrokeWidth = calculateSizeByUnit(settings.progressStrokeWidth, settings.progressStrokeUnit);
            RectF arcBounds = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

            circlePaint.setStrokeWidth(circleStrokeWidth);
            canvas.drawCircle(centerX, centerY, radius, circlePaint);

            progressPaint.setStrokeWidth(progressStrokeWidth);
            canvas.drawArc(arcBounds, - 360 * progress, 20, false, progressPaint);
        }

        private void drawByProgress(Canvas canvas){
            //calculate loading progress
            float loadingProgress = (float)progressInfo.loaded() / (float)progressInfo.total();
            if (loadingProgress > 1f){
                loadingProgress = 1f;
            }
            if (displayProgress > loadingProgress){
                //reset display progress
                displayProgress = 0f;
            }
            //increase display progress
            displayProgress += 0.04;
            if (displayProgress > loadingProgress){
                //limit display progress
                displayProgress = loadingProgress;
            }
            final int centerX = (getBounds().left + getBounds().right) / 2;
            final int centerY = (getBounds().top + getBounds().bottom) / 2;
            float radius = calculateSizeByUnit(settings.radius, settings.radiusUnit);
            float circleStrokeWidth = calculateSizeByUnit(settings.circleStrokeWidth, settings.circleStrokeUnit);
            float progressStrokeWidth = calculateSizeByUnit(settings.progressStrokeWidth, settings.progressStrokeUnit);
            RectF arcBounds = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

            circlePaint.setStrokeWidth(circleStrokeWidth);
            canvas.drawCircle(centerX, centerY, radius, circlePaint);

            progressPaint.setStrokeWidth(progressStrokeWidth);
            canvas.drawArc(arcBounds, 270, 360 * displayProgress, false, progressPaint);
        }

        private float calculateSizeByUnit(float value, SizeUnit unit){
            switch (unit){
                case PERCENT_OF_WIDTH:
                    return value * getBounds().width();
                case PERCENT_OF_HEIGHT:
                    return value * getBounds().height();
                case PX:
                default:
                    return value;
            }
        }

        @Override
        public void setAlpha(int alpha) {
            circlePaint.setAlpha(alpha);
            progressPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            circlePaint.setColorFilter(colorFilter);
            progressPaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return 0;
        }
    }

}