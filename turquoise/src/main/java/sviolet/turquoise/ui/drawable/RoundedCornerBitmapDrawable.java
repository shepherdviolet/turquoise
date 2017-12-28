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
     * <p>绘制步骤1:控件(或外部Drawable)通过该方法获得尺寸, 该尺寸供外部控件参考, 但不一定适用.</p>
     *
     * <p>返回-1时, 表示建议拉伸填充外部.</p>
     *
     * <p>ImageView场合示例: ImageView尺寸为300*300, drawable尺寸(getIntrinsicWidth/Height)为200*200,
     * IImageView.scaleType=center时, 会裁剪canvas, 使得canvas.getWidth()/getHeight()为控件自身尺寸300*300,
     * 而canvas.getClipBounds()为Drawable的尺寸300*300(通过getIntrinsicWidth()/getIntrinsicHeight()获得).
     * IImageView.scaleType=centerCrop时, 会裁剪canvas, 使得canvas.getWidth()/getHeight()为控件自身尺寸300*300,
     * 而canvas.getClipBounds()为Drawable的尺寸200*200(通过getIntrinsicWidth()/getIntrinsicHeight()获得).
     * IImageView.scaleType=fitXY时, 会裁剪canvas, 使得canvas.getWidth()/getHeight()为控件自身尺寸300*300,
     * 而canvas.getClipBounds()为Drawable的尺寸300*300(通过getIntrinsicWidth()/getIntrinsicHeight()获得).</p>
     */
    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    /**
     * <p>绘制步骤2: 控件(或外部Drawable)决定该Drawable的绘制范围后, 通过该方法设置范围.</p>
     *
     * <p>控件通常会自动调用该方法. 在Drawable嵌套的场合(或其他特殊场合), 需要手动调用该方法指定绘制范围,
     * 通常将外部Drawable的绘制范围设置给内部Drawable即可, 例如:drawable.setBounds(getBounds());</p>
     *
     * <p>ImageView场合示例: ImageView尺寸为300*300, drawable尺寸(getIntrinsicWidth/Height)为200*200,
     * IImageView.scaleType=center时, 会通过该方法设置DrawableBounds为200*200.
     * IImageView.scaleType=centerCrop时, 会通过该方法设置DrawableBounds为200*200.
     * IImageView.scaleType=fitXY时, 会通过该方法设置DrawableBounds为300*300.</p>
     */
    @Override
    public void setBounds(Rect bounds) {
        super.setBounds(bounds);
    }

    /**
     * <p>绘制步骤3: 绘制图形, 通常根据getBounds()决定绘制范围.</p>
     *
     * <p>drawable.getBounds()是控件(或外部Drawable)建议的绘制范围, 通常采用该尺寸绘制图形.</p>
     *
     * <p>canvas.getWidth()和getHeight()是画布的实际尺寸(像素), 忽略缩放的情况.</p>
     *
     * <p>canvas.getClipBounds()是画布的裁剪范围, 可能大于或小于画布实际尺寸, 通常, 画布的缩放会导致裁剪范围变化.
     * 若绘制时需要填充整个控件, 可以使用该尺寸绘制图形. 若需要获得外层缩放比例, 可以通过如下方式计算:</p>
     *
     * <pre>{@code
     *      Rect canvasRect = new Rect();
     *      canvas.getClipBounds(canvasRect);
     *      //计算横向缩放比例
     *      float canvasWidthScale = (float)canvasRect.width() / (float)canvas.getWidth();
     * }</pre>
     *
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
