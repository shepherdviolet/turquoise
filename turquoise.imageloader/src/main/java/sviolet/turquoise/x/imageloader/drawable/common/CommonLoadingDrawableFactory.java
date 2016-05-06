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
import sviolet.turquoise.x.imageloader.entity.LoadProgress;
import sviolet.turquoise.x.imageloader.entity.Params;

/**
 * <p><p>create drawable for loading status</p></p>
 *
 * <p>you must use this {@link TIBitmapDrawable} instead of {@link BitmapDrawable}
 * to implements {@link LoadingDrawableFactory}/{@link BackgroundDrawableFactory}/{@link FailedDrawableFactory}</p>
 *
 * <p>implement notes::</p>
 *
 * <p>1.if Params->sizeMatchView is true, loadingDrawable's size match View (-1) or itself (size of loading image).
 * if Params->sizeMatchView is false, loadingDrawable's size match Params->reqWidth/reqHeight.</p>
 *
 * Created by S.Violet on 2016/3/17.
 */
public class CommonLoadingDrawableFactory implements LoadingDrawableFactory {

    private static final int BACKGROUND_COLOR_DEF = 0x00000000;

    private ResourceBitmapWrapper imageBitmap = new ResourceBitmapWrapper();
    private int backgroundColor = BACKGROUND_COLOR_DEF;
    private AnimationDrawableFactory animationDrawableFactory = new CommonAnimationDrawableFactory();

    private Settings settings = new Settings();

    @Override
    public Drawable create(Context applicationContext, Context context, Params params, LoadProgress.Info progressInfo, TLogger logger) {
        //size, match reqSize Params->sizeMatchView is false
        int drawableWidth = -1;
        int drawableHeight = -1;
        if (!params.isSizeMatchView()){
            drawableWidth = params.getReqWidth();
            drawableHeight = params.getReqHeight();
        }
        //image
        BitmapDrawable imageDrawable = null;
        Bitmap bitmap = imageBitmap.getBitmap(applicationContext.getResources(), logger);
        if (bitmap != null && !bitmap.isRecycled()){
            //use TIBitmapDrawable instead of BitmapDrawable
            imageDrawable = new TIBitmapDrawable(applicationContext.getResources(), bitmap);
        }
        //background
        Drawable backgroundDrawable = null;
        if (backgroundColor != 0x00000000){
            backgroundDrawable = new ColorDrawable(backgroundColor);
        }
        //animation
        Drawable animationDrawable = animationDrawableFactory.create(applicationContext, context, params, progressInfo, logger);
        //loading drawable
        return new LoadingDrawable(settings, animationDrawable, imageDrawable, backgroundDrawable, drawableWidth, drawableHeight);
    }

    @Override
    public void onDestroy() {
        imageBitmap.destroy();
    }

    /**
     * set loading image
     * @param imageResId resource id
     */
    public CommonLoadingDrawableFactory setImageResId(int imageResId){
        imageBitmap.setResId(imageResId);
        return this;
    }

    /**
     * set scale type of loading image
     * @param imageScaleType ImageScaleType
     */
    public CommonLoadingDrawableFactory setImageScaleType(ImageScaleType imageScaleType){
        settings.imageScaleType = imageScaleType;
        return this;
    }

    /**
     * set loading background color
     * @param backgroundColor color
     */
    public CommonLoadingDrawableFactory setBackgroundColor(int backgroundColor){
        this.backgroundColor = backgroundColor;
        return this;
    }

    /**
     * set if animation enabled
     * @param enabled true by default
     */
    public CommonLoadingDrawableFactory setAnimationEnabled(boolean enabled){
        settings.animationEnabled = enabled;
        return this;
    }

    /**
     * set custom animation, reference {@link CommonLoadingDrawableFactory.CommonAnimationDrawableFactory} & {@link CommonLoadingDrawableFactory.CommonAnimationDrawable}
     * @param factory AnimationDrawableFactory
     */
    public CommonLoadingDrawableFactory setAnimationDrawableFactory(AnimationDrawableFactory factory){
        if (factory != null) {
            this.animationDrawableFactory = factory;
        }
        return this;
    }

    /**
     * settings of LoadingDrawableFactory
     */
    protected static class Settings{

        ImageScaleType imageScaleType = ImageScaleType.NORMAL;
        boolean animationEnabled = true;

    }

    /**
     * LoadingDrawable, contains background color, loading image, animation drawable
     */
    private static class LoadingDrawable extends Drawable {

        private Settings settings;

        private Drawable animationDrawable;
        private BitmapDrawable imageDrawable;
        private Drawable backgroundDrawable;
        private int drawableWidth = 0;
        private int drawableHeight = 0;

        public LoadingDrawable(Settings settings, Drawable animationDrawable, BitmapDrawable imageDrawable, Drawable backgroundDrawable, int drawableWidth, int drawableHeight){
            this.settings = settings;
            this.animationDrawable = animationDrawable;
            this.imageDrawable = imageDrawable;
            this.backgroundDrawable = backgroundDrawable;
            this.drawableWidth = drawableWidth;
            this.drawableHeight = drawableHeight;
        }

        @Override
        public void draw(Canvas canvas) {
            //skip drawing
            if (canvas.getWidth() <=0 || canvas.getHeight() <= 0){
                return;
            }

            //draw background and image
            onDrawStatic(canvas);

            //draw animation
            if (animationDrawable != null){
                //set bounds as parent drawable
                animationDrawable.setBounds(getBounds());
                animationDrawable.draw(canvas);
                invalidateSelf();
            }

        }

        public void onDrawStatic(Canvas canvas) {

            //draw background
            if (backgroundDrawable != null){
                //set Bounds as parent drawable
                backgroundDrawable.setBounds(getBounds());
                //draw
                backgroundDrawable.draw(canvas);
            }

            //draw image
            if (imageDrawable != null && imageDrawable.getBitmap() != null) {
                //calculate bounds
                Rect imageRect = null;
                if (settings.imageScaleType == ImageScaleType.FORCE_CENTER){
                    //new image rect
                    imageRect = new Rect();
                    //canvas rect
                    Rect canvasRect = new Rect();
                    canvas.getClipBounds(canvasRect);
                    //calculate canvas scale
                    float canvasWidthScale = (float)canvasRect.width() / (float)canvas.getWidth();
                    float canvasHeightScale = (float)canvasRect.height() / (float)canvas.getHeight();
                    //scale image size
                    int bitmapWidth = (int) ((float)imageDrawable.getIntrinsicWidth() * canvasWidthScale);
                    int bitmapHeight = (int) ((float)imageDrawable.getIntrinsicHeight() * canvasHeightScale);
                    //calculate bounds
                    int horizontalPadding = (canvasRect.width() - bitmapWidth) / 2;
                    int verticalPadding = (canvasRect.height() - bitmapHeight) / 2;
                    imageRect.left = canvasRect.left + horizontalPadding;
                    imageRect.top = canvasRect.top + verticalPadding;
                    imageRect.right = canvasRect.right - horizontalPadding;
                    imageRect.bottom = canvasRect.bottom - verticalPadding;
                } else {
                    //set Bounds as parent drawable
                    imageRect = getBounds();
                }
                //set Bounds
                imageDrawable.setBounds(imageRect);
                //draw
                imageDrawable.draw(canvas);
            }
        }


        @Override
        public int getIntrinsicWidth() {
            //return reqWidth if valid
            if (drawableWidth > 0){
                return drawableWidth;
            }
            //calculate width
            int maxWidth = -1;
            if (imageDrawable != null && imageDrawable.getBitmap() != null) {
                int width = imageDrawable.getIntrinsicWidth();
                if (width > maxWidth){
                    maxWidth = width;
                }
            }
            if (animationDrawable != null){
                int width = animationDrawable.getIntrinsicWidth();
                if (width > maxWidth){
                    maxWidth = width;
                }
            }
            return maxWidth > 0 ? maxWidth : -1;
        }

        @Override
        public int getIntrinsicHeight() {
            //return reqHeight if valid
            if (drawableHeight > 0){
                return drawableHeight;
            }
            //calculate height
            int maxHeight = -1;
            if (imageDrawable != null && imageDrawable.getBitmap() != null){
                int height = imageDrawable.getIntrinsicHeight();
                if (height > maxHeight){
                    maxHeight = height;
                }
            }
            if (animationDrawable != null){
                int height = animationDrawable.getIntrinsicHeight();
                if (height > maxHeight){
                    maxHeight = height;
                }
            }
            return maxHeight > 0 ? maxHeight : -1;
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

    /**
     * scale type of loading image
     */
    public enum ImageScaleType{
        NORMAL,
        FORCE_CENTER
    }

    /**
     * AnimationDrawableFactory interface, create AnimationDrawable.
     */
    public interface AnimationDrawableFactory{

        Drawable create(Context applicationContext, Context context, Params params, LoadProgress.Info progressInfo, TLogger logger);

    }

    /**********************************************************************************************************************
     * Common Animation
     **********************************************************************************************************************/

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
     * common AnimationDrawableFactory
     */
    public static class CommonAnimationDrawableFactory implements AnimationDrawableFactory{

        private AnimationSettings settings = new AnimationSettings();

        @Override
        public Drawable create(Context applicationContext, Context context, Params params, LoadProgress.Info progressInfo, TLogger logger) {
            return new CommonAnimationDrawable(settings);
        }

        public CommonAnimationDrawableFactory setPointColor(int color){
            settings.pointColor = color;
            return this;
        }

        public CommonAnimationDrawableFactory setPointRadius(int radius){
            settings.pointRadius = radius;
            return this;
        }

        public CommonAnimationDrawableFactory setPointInterval(int interval){
            settings.pointInterval = interval;
            return this;
        }

        public CommonAnimationDrawableFactory setAnimationDuration(long duration){
            settings.animationDuration = duration;
            return this;
        }

        public CommonAnimationDrawableFactory setPointOffsetX(float offsetX){
            settings.pointOffsetX = offsetX;
            return this;
        }

        public CommonAnimationDrawableFactory setPointOffsetY(float offsetY){
            settings.pointOffsetY = offsetY;
            return this;
        }

    }

    /**
     * Common AnimationDrawable
     */
    public static class CommonAnimationDrawable extends Drawable {

        private AnimationSettings settings;

        private static final int QUANTITY = 3;
        private static final int MIN_POSITION = -2;
        private static final int MAX_POSITION = 4;
        private long startTime = DateTimeUtils.getUptimeMillis();

        private Paint paint;

        public CommonAnimationDrawable(AnimationSettings settings) {
            this.settings = settings;
            initPaint();
        }

        @Override
        public void draw(Canvas canvas) {
            //skip
            if(settings.pointRadius <= 0 || settings.pointInterval < 0 || settings.animationDuration <= 0)
                return;

            //draw
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
            return 0;
        }
    }

}
