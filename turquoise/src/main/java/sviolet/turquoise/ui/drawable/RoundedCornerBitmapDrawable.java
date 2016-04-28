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

package sviolet.turquoise.ui.drawable;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * Simple example of rounded corner bitmap drawable
 * <p/>
 * Created by S.Violet on 2016/4/28.
 */
public class RoundedCornerBitmapDrawable extends Drawable {

    private Paint paint;
    private int width;
    private int height;
    private float roundRadius = 10;

    public RoundedCornerBitmapDrawable(Bitmap bitmap) {
        if (bitmap == null){
            throw new RuntimeException("[RoundedCornerBitmapDrawable]bitmap is null");
        }
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        initPaint(bitmap);
    }

    public RoundedCornerBitmapDrawable setRoundRadius(float roundRadius){
        this.roundRadius = roundRadius;
        return this;
    }

    private void initPaint(Bitmap bitmap) {
        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(bitmapShader);
    }

    /**
     * 绘制步骤1:外部控件通过该方法获得尺寸, 该尺寸供外部控件参考, 但不一定适用
     */
    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    /**
     * 绘制步骤1:外部控件通过该方法获得尺寸, 该尺寸供外部控件参考, 但不一定适用.
     */
    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    /**
     * 绘制步骤2: 外部控件决定该Drawable的绘制范围, 通过该方法设置范围.
     */
    @Override
    public void setBounds(Rect bounds) {
        super.setBounds(bounds);
    }

    /**
     * 绘制步骤3: 绘制图形, 通常根据getBounds()决定绘制范围, drawable.getBounds()获得的范围可能比canvas.getClipBounds()的
     * 值小, 因为canvas.getClipBounds()是整个画布的绘制范围, 而drawable.getBounds()是Drawable被允许绘制的范围. 另外,
     * canvas.getWidth()和getHeight()的值为画布的真实尺寸, 通常为绘制范围的数倍(比例缩放), 可以根据canvas.getWidth()/canvasRect.width()
     * 的比值计算缩放比例.
     */
    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(new RectF(getBounds()), roundRadius, roundRadius, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

}
