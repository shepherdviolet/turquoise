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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import sviolet.turquoise.util.common.DateTimeUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.drawable.BackgroundDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.FailedDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.LoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.ResourceBitmapWrapper;
import sviolet.turquoise.x.imageloader.drawable.TIBitmapDrawable;
import sviolet.turquoise.x.imageloader.entity.Params;

/**
 *
 * <p>you must use this {@link TIBitmapDrawable} instead of {@link BitmapDrawable}
 * to implements {@link LoadingDrawableFactory}/{@link BackgroundDrawableFactory}/{@link FailedDrawableFactory}</p>
 *
 * Created by S.Violet on 2016/3/17.
 */
public class CommonLoadingDrawableFactory implements LoadingDrawableFactory {

    private static final int BACKGROUND_COLOR_DEF = 0x00000000;

    private ResourceBitmapWrapper imageBitmap = new ResourceBitmapWrapper();
    private int backgroundColor = BACKGROUND_COLOR_DEF;

    private Settings settings = new Settings();

    @Override
    public Drawable create(Context applicationContext, Context context, Params params, TLogger logger) {
        //size
        int drawableWidth = 0;
        int drawableHeight = 0;
        if (!params.isSizeMatchView()){
            drawableWidth = params.getReqWidth();
            drawableHeight = params.getReqHeight();
        }
        //image
        BitmapDrawable imageDrawable = null;
        Bitmap bitmap = imageBitmap.getBitmap(applicationContext.getResources(), logger);
        if (bitmap != null && !bitmap.isRecycled()){
            imageDrawable = new TIBitmapDrawable(bitmap);
        }
        //background
        Drawable backgroundDrawable = null;
        if (backgroundColor != 0x00000000){
            backgroundDrawable = new ColorDrawable(backgroundColor);
        }
        //loading drawable
        return new LoadingDrawable(settings, imageDrawable, backgroundDrawable, drawableWidth, drawableHeight);
    }

    @Override
    public void onDestroy() {
        imageBitmap.destroy();
    }

    public CommonLoadingDrawableFactory setImageResId(int imageResId){
        imageBitmap.setResId(imageResId);
        return this;
    }

    public CommonLoadingDrawableFactory setBackgroundColor(int backgroundColor){
        this.backgroundColor = backgroundColor;
        return this;
    }

    public CommonLoadingDrawableFactory setPointColor(int color){
        settings.pointColor = color;
        return this;
    }

    public CommonLoadingDrawableFactory setPointRadius(int radius){
        settings.pointRadius = radius;
        return this;
    }

    public CommonLoadingDrawableFactory setPointInterval(int interval){
        settings.pointInterval = interval;
        return this;
    }

    public CommonLoadingDrawableFactory setAnimationDuration(long duration){
        settings.animationDuration = duration;
        return this;
    }

    public CommonLoadingDrawableFactory setPointOffsetX(float offsetX){
        settings.pointOffsetX = offsetX;
        return this;
    }

    public CommonLoadingDrawableFactory setPointOffsetY(float offsetY){
        settings.pointOffsetY = offsetY;
        return this;
    }

    public CommonLoadingDrawableFactory setAnimationEnabled(boolean enabled){
        settings.animationEnabled = enabled;
        return this;
    }

    public CommonLoadingDrawableFactory setImageScaleType(ImageScaleType imageScaleType){
        settings.imageScaleType = imageScaleType;
        return this;
    }

    protected static class Settings{
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
        boolean animationEnabled = true;

        ImageScaleType imageScaleType = ImageScaleType.CENTER;

    }

    private static class LoadingDrawable extends Drawable {

        private Settings settings;
        private BitmapDrawable imageDrawable;
        private Drawable backgroundDrawable;
        int drawableWidth = 0;
        int drawableHeight = 0;

        private static final int QUANTITY = 3;
        private static final int MIN_POSITION = -2;
        private static final int MAX_POSITION = 4;
        private long startTime = DateTimeUtils.getUptimeMillis();

        private Paint paint;

        public LoadingDrawable(Settings settings, BitmapDrawable imageDrawable, Drawable backgroundDrawable, int drawableWidth, int drawableHeight){
            this.settings = settings;
            this.imageDrawable = imageDrawable;
            this.backgroundDrawable = backgroundDrawable;
            this.drawableWidth = drawableWidth;
            this.drawableHeight = drawableHeight;
            initPaint();
        }

        @Override
        public void draw(Canvas canvas) {
            //skip drawing
            if (canvas.getWidth() <=0 || canvas.getHeight() <= 0){
                return;
            }

            onDrawStatic(canvas);

            if (settings.animationEnabled) {
                onDrawAnimation(canvas);
                invalidateSelf();
            }

        }

        public void onDrawStatic(Canvas canvas) {
            //canvas rect
            Rect canvasRect = new Rect();
            canvas.getClipBounds(canvasRect);

            //draw background
            if (backgroundDrawable != null){
                //set Bounds
                backgroundDrawable.setBounds(canvasRect);
                //draw
                backgroundDrawable.draw(canvas);
            }
            //draw image
            if (imageDrawable != null && imageDrawable.getBitmap() != null) {
                //calculate bounds
                Rect imageRect = null;
                if (settings.imageScaleType == ImageScaleType.CENTER){
                    imageRect = new Rect();
                    float canvasWidthScale = (float)canvasRect.width() / (float)canvas.getWidth();
                    float canvasHeightScale = (float)canvasRect.height() / (float)canvas.getHeight();
                    int bitmapWidth = (int) ((float)imageDrawable.getBitmap().getWidth() * canvasWidthScale);
                    int bitmapHeight = (int) ((float)imageDrawable.getBitmap().getHeight() * canvasHeightScale);
                    int horizontalPadding = (canvasRect.width() - bitmapWidth) / 2;
                    int verticalPadding = (canvasRect.height() - bitmapHeight) / 2;
                    imageRect.left = canvasRect.left + horizontalPadding;
                    imageRect.top = canvasRect.top + verticalPadding;
                    imageRect.right = canvasRect.right - horizontalPadding;
                    imageRect.bottom = canvasRect.bottom - verticalPadding;
                } else {
                    imageRect = canvasRect;
                }
                //set Bounds
                imageDrawable.setBounds(imageRect);
                //draw
                imageDrawable.draw(canvas);
            }
        }

        public void onDrawAnimation(Canvas canvas) {
            if(settings.pointRadius <= 0 || settings.pointInterval < 0 || settings.animationDuration <= 0)
                return;

            drawCircle(canvas, calculateCurrentPosition());
        }

        private int calculateCurrentPosition(){
            final float progress = (float)((DateTimeUtils.getUptimeMillis() - startTime) % settings.animationDuration) / (float)settings.animationDuration;
            final int distance = MAX_POSITION - MIN_POSITION;
            final int position = (int) ((2 * distance + 1) * progress);
            if (position <= distance){
                return MIN_POSITION + position;
            }else{
                return MIN_POSITION + distance - (position - distance - 1);
            }
        }

        private void drawCircle(Canvas canvas, int currentPosition) {
            /*
             * 背景图如果是Bitmap, 绘制后canvas的选区可能会被裁剪(小于画布), 因此绘制动画时,
             * 为保证定位准确, 大小保持原比例, 使用选区(clipBounds)尺寸, 且点半径和点间距根据
             * 选区与画布尺寸的比例缩小, 保证显示的大小保持一致.
             */
            Rect clipBounds = new Rect();
            canvas.getClipBounds(clipBounds);

            final float scale = (float)clipBounds.width() / (float)canvas.getWidth();//用于点半径和点间距的比例缩放

            final int length = (int) ((QUANTITY - 1) * settings.pointInterval * scale);

            int x = (int) (clipBounds.left + clipBounds.width() * settings.pointOffsetX - length / 2);
            int y = (int) (clipBounds.top + clipBounds.height() * settings.pointOffsetY);

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
        public int getIntrinsicWidth() {
            if (drawableWidth > 0){
                return drawableWidth;
            } else if (imageDrawable != null && imageDrawable.getBitmap() != null) {
                return imageDrawable.getBitmap().getWidth();
            } else if (backgroundDrawable != null){
                return backgroundDrawable.getIntrinsicWidth();
            } else {
                return -1;
            }
        }

        @Override
        public int getIntrinsicHeight() {
            if (drawableHeight > 0){
                return drawableHeight;
            } else if (imageDrawable != null && imageDrawable.getBitmap() != null){
                return imageDrawable.getBitmap().getHeight();
            } else if (backgroundDrawable != null){
                return backgroundDrawable.getIntrinsicHeight();
            } else {
                return -1;
            }
        }

        @Override
        public void setAlpha(int alpha) {
            //do nothing
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            //do nothing
        }

        @Override
        public int getOpacity() {
            return 0;
        }
    }

    public enum ImageScaleType{
        CENTER,
        STRETCH
    }

}
