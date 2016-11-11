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
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

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
     * set degrees step of rotate
     * @param step degrees
     */
    public CircleLoadingAnimationDrawableFactory setRotateStep(int step){
        if (step <= 0){
            throw new RuntimeException("[CircleLoadingAnimationDrawableFactory]step must > 0");
        }
        settings.rotateStep = step;
        return this;
    }

    public CircleLoadingAnimationDrawableFactory setSweepAngle(int sweepAngle){
        if (sweepAngle <= 0){
            throw new RuntimeException("[CircleLoadingAnimationDrawableFactory]sweepAngle must > 0");
        }
        settings.sweepAngle = sweepAngle;
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
        private float progressStrokeWidth = 0.018f;
        private int rotateStep = 5;
        private int sweepAngle = 30;

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

        private int skipCount;
        private int displayPosition = 270;
        private int displayProgress = 0;

        private Paint circlePaint;
        private Paint progressPaint;

        //优化性能
        private RectF arcBounds = new RectF();

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
            if (progressInfo.total() <= 0){
                drawByDuration(canvas);
            }else{
                drawByProgress(canvas);
            }
            invalidateSelf();
        }

        private void drawByDuration(Canvas canvas){
            final int centerX = (getBounds().left + getBounds().right) >> 1;
            final int centerY = (getBounds().top + getBounds().bottom) >> 1;
            float radius = calculateSizeByUnit(settings.radius, settings.radiusUnit);
            float circleStrokeWidth = calculateSizeByUnit(settings.circleStrokeWidth, settings.circleStrokeUnit);

            circlePaint.setStrokeWidth(circleStrokeWidth);
            canvas.drawCircle(centerX, centerY, radius, circlePaint);

            if (skipCount < 4){
                skipCount++;
                return;
            }

            this.displayPosition = (this.displayPosition - settings.rotateStep) % 360;
            float progressStrokeWidth = calculateSizeByUnit(settings.progressStrokeWidth, settings.progressStrokeUnit);
            arcBounds.left = centerX - radius;
            arcBounds.top = centerY - radius;
            arcBounds.right = centerX + radius;
            arcBounds.bottom = centerY + radius;

            progressPaint.setStrokeWidth(progressStrokeWidth);
            canvas.drawArc(arcBounds, displayPosition, settings.sweepAngle, false, progressPaint);
        }

        private void drawByProgress(Canvas canvas){
            //calculate loading progress
            int loadingProgress = (int) ((progressInfo.loaded() * 360) / progressInfo.total());
            if (loadingProgress > 360){
                loadingProgress = 360;
            }
            if (displayProgress > loadingProgress){
                //reset display progress
                displayProgress = 0;
            } else if (displayProgress < loadingProgress) {
                //increase display progress
                int step = (loadingProgress - displayProgress) >> 3;
                if (step > 0){
                    displayProgress += step;
                }else{
                    displayProgress++;
                }
            }
            final int centerX = (getBounds().left + getBounds().right) >> 1;
            final int centerY = (getBounds().top + getBounds().bottom) >> 1;
            float radius = calculateSizeByUnit(settings.radius, settings.radiusUnit);
            float circleStrokeWidth = calculateSizeByUnit(settings.circleStrokeWidth, settings.circleStrokeUnit);
            float progressStrokeWidth = calculateSizeByUnit(settings.progressStrokeWidth, settings.progressStrokeUnit);
            arcBounds.left = centerX - radius;
            arcBounds.top = centerY - radius;
            arcBounds.right = centerX + radius;
            arcBounds.bottom = centerY + radius;

            circlePaint.setStrokeWidth(circleStrokeWidth);
            canvas.drawCircle(centerX, centerY, radius, circlePaint);

            progressPaint.setStrokeWidth(progressStrokeWidth);
            canvas.drawArc(arcBounds, 270, displayProgress, false, progressPaint);
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
            return PixelFormat.UNKNOWN;
        }
    }

}
