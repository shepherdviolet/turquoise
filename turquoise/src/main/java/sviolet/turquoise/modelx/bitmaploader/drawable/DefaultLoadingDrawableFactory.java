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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import sviolet.turquoise.util.sys.DateTimeUtils;

/**
 * 默认动态加载图工厂
 *
 * Created by S.Violet on 2015/11/17.
 */
public class DefaultLoadingDrawableFactory extends AbsLoadingDrawableFactory {

    protected Settings settings;

    DefaultLoadingDrawableFactory(Settings settings){
        this.settings = settings;
    }

    /**
     * 返回动态加载图实例
     */
    @Override
    public DefaultLoadingDrawable newLoadingDrawable() {
        return new DefaultLoadingDrawable(settings);
    }

    /**
     * 返回背景图, 用于图片加载完毕后淡入效果的背景
     */
    @Override
    public Drawable newBackgroundDrawable() {
        return settings.newBackgroundDrawable(true);//用于目的图淡入动画背景, 尺寸为match_parent
    }

    /**
     * 销毁Bitmap
     */
    @Override
    public void destroy() {
        if (settings == null)
            return;

        if (settings.backgroundBitmap != null && !settings.backgroundBitmap.isRecycled()){
            settings.backgroundBitmap.recycle();
            settings.backgroundBitmap = null;
        }
        if (settings.failedBitmap != null && !settings.failedBitmap.isRecycled()){
            settings.failedBitmap.recycle();
            settings.failedBitmap = null;
        }
    }

    protected static class Settings{

        static final int COLOR_DEF = 0xFFC0C0C0;
        static final int RADIUS_DEF = 10;
        static final int INTERVAL_DEF = 40;
        static final int DURATION = 1000;
        static final float OFFSET_X_DEF = 0.5f;
        static final float OFFSET_Y_DEF = 0.5f;

        int color = COLOR_DEF;//进度条颜色
        int radius = RADIUS_DEF;//进度条圆点半径
        int interval = INTERVAL_DEF;//进度条点间隔
        long duration = DURATION;//进度条动画时间
        float offsetX = OFFSET_X_DEF;//进度条X轴方向位置
        float offsetY = OFFSET_Y_DEF;//进度条Y轴方向位置

        Bitmap failedBitmap;//失败时显示的图
        Bitmap backgroundBitmap;//加载背景图
        int backgroundColor;//加载背景颜色

        /**
         * @param matchParent 尺寸是否填充父控件(match_parent)
         */
        Drawable newBackgroundDrawable(boolean matchParent) {
            //背景图模式
            if (backgroundBitmap != null && !backgroundBitmap.isRecycled())
                return new SafeBitmapDrawable(backgroundBitmap)
                        .setMatchParent(matchParent);
            //背景颜色模式
            return new ColorDrawable(backgroundColor);
        }

        Drawable newFailedDrawable(){
            //加载失败图存在的情况
            if (failedBitmap != null && !failedBitmap.isRecycled()){
                return new SafeBitmapDrawable(failedBitmap)
                        .setMatchParent(true);//尺寸为match_parent
            }
            return null;
        }
    }

    public static class Builder{

        private Settings settings = new Settings();

        /**
         * @param backgroundBitmap 背景图, BitmapLoader销毁时会自动回收
         */
        public Builder(Bitmap backgroundBitmap){
            settings.backgroundBitmap = backgroundBitmap;
        }

        /**
         * @param backgroundColor 背景颜色
         */
        public Builder(int backgroundColor){
            settings.backgroundColor = backgroundColor;
        }

        /**
         * @param color 进度条圆点颜色
         */
        public Builder setColor(int color){
            settings.color = color;
            return this;
        }

        /**
         * @param radius 进度条圆点半径 px
         */
        public Builder setRadius(int radius){
            settings.radius = radius;
            return this;
        }

        /**
         * @param interval 进度条圆点间隔 px
         */
        public Builder setInterval(int interval){
            settings.interval = interval;
            return this;
        }

        /**
         * @param duration 进度条动画时间 ms
         */
        public Builder setDuration(long duration){
            settings.duration = duration;
            return this;
        }

        /**
         * @param offsetX 进度条在X轴方向相对位置 [0, 1], 默认值0.5f
         */
        public Builder setOffsetX(float offsetX){
            settings.offsetX = offsetX;
            return this;
        }

        /**
         * @param offsetY 进度条在Y轴方向相对位置 [0, 1], 默认值0.5f
         */
        public Builder setOffsetY(float offsetY){
            settings.offsetY = offsetY;
            return this;
        }

        /**
         * @param failedBitmap 加载失败时显示的图片, BitmapLoader销毁时自动回收
         */
        public Builder setFailedBitmap(Bitmap failedBitmap){
            settings.failedBitmap = failedBitmap;
            return this;
        }

        /**
         * @return 创建工厂实例
         */
        public AbsLoadingDrawableFactory create(){
            if (settings == null)
                throw new RuntimeException("[DefaultLoadingDrawableFactory.Builder]builder can't create repeatly");
            AbsLoadingDrawableFactory factory = new DefaultLoadingDrawableFactory(settings);
            settings = null;
            return factory;
        }

    }

    /**
     * 默认动态加载图
     */
    private static class DefaultLoadingDrawable extends AbsLoadingDrawable{

        private Settings settings;
        private Drawable background;
        private Drawable failed;

        //动画变量
        private static final int QUANTITY = 3;//显示的总点数
        private static final int MIN_POSITION = -2;//点运动左边界
        private static final int MAX_POSITION = 4;//点运动右边界
        private long startTime = DateTimeUtils.getUptimeMillis();//动画起始时间

        //画笔
        private Paint paint;//圆点画笔

        public DefaultLoadingDrawable(Settings settings){
            this.settings = settings;

            initPaint();
        }

        @Override
        public void onDrawBackground(Canvas canvas) {
            if (background == null) {
                background = settings.newBackgroundDrawable(false);//作为加载图背景, 保持原有尺寸
            }
            //设置Bounds
            Rect rect = new Rect();
            canvas.getClipBounds(rect);
            background.setBounds(rect);
            //绘制背景
            background.draw(canvas);
        }

        @Override
        public void onDrawAnimation(Canvas canvas) {
            if(settings.radius <= 0 || settings.interval < 0 || settings.duration <= 0)
                return;

            //绘制加载指示
            drawCircle(canvas, calculateCurrentPosition());
        }

        @Override
        public void onDrawFailedBitmap(Canvas canvas) {
            if (failed == null){
                failed = settings.newFailedDrawable();
            }
            if (failed != null){
                //设置Bounds
                Rect rect = new Rect();
                canvas.getClipBounds(rect);
                failed.setBounds(rect);
                //绘制加载失败图
                failed.draw(canvas);
            }
        }

        /**
         * 计算点当前位置
         */
        private int calculateCurrentPosition(){
            final float progress = (float)((DateTimeUtils.getUptimeMillis() - startTime) % settings.duration) / (float)settings.duration;
            final int distance = MAX_POSITION - MIN_POSITION;
            final int position = (int) ((2 * distance + 1) * progress);
            if (position <= distance){
                return MIN_POSITION + position;
            }else{
                return MIN_POSITION + distance - (position - distance - 1);
            }
        }

        /**
         * 绘制圆点
         */
        private void drawCircle(Canvas canvas, int currentPosition) {
            /*
             * 背景图如果是Bitmap, 绘制后canvas的选区可能会被裁剪(小于画布), 因此绘制动画时,
             * 为保证定位准确, 大小保持原比例, 使用选区(clipBounds)尺寸, 且点半径和点间距根据
             * 选区与画布尺寸的比例缩小, 保证显示的大小保持一致.
             */
            Rect clipBounds = new Rect();
            canvas.getClipBounds(clipBounds);

            //画布或选区长宽为0不绘制
            if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0 || clipBounds.width() <=0 || clipBounds.height() <= 0)
                return;

            final float scale = (float)clipBounds.width() / (float)canvas.getWidth();//用于点半径和点间距的比例缩放

            final int width = clipBounds.width();
            final int height = clipBounds.height();
            final int length = (int) ((QUANTITY - 1) * settings.interval * scale);

            int x = (int) (width * settings.offsetX - length / 2);
            int y = (int) (height * settings.offsetY);

            for(int i = 0 ; i < QUANTITY ; i++){
                canvas.drawCircle(x, y, i == currentPosition ? (float) (settings.radius * 1.5 * scale) : settings.radius * scale, paint);
                x += settings.interval * scale;
            }
        }

        /**
         * 初始化画笔
         */
        private void initPaint() {
            paint = new Paint();
            paint.setColor(settings.color);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        public int getIntrinsicWidth() {
            if (background == null) {
                background = settings.newBackgroundDrawable(false);//作为加载图背景, 保持原有尺寸
            }
            return background.getIntrinsicWidth();//尺寸以背景图为准
        }

        @Override
        public int getIntrinsicHeight() {
            if (background == null) {
                background = settings.newBackgroundDrawable(false);//作为加载图背景, 保持原有尺寸
            }
            return background.getIntrinsicHeight();//尺寸以背景图为准
        }
    }

}
