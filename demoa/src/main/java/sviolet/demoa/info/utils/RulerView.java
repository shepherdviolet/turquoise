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

package sviolet.demoa.info.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * 标尺控件
 *
 * Created by S.Violet on 2017/1/26.
 */
public class RulerView extends View {

    private float centimeterPixels = 0;

    private Paint markPaint;

    private Rect canvasClipBounds = new Rect();

    public RulerView(Context context) {
        super(context);
        init();
    }

    public RulerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RulerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        markPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markPaint.setColor(0xFF505050);
        markPaint.setStrokeWidth(1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (centimeterPixels <= 0){
            return;
        }

        canvas.getClipBounds(canvasClipBounds);

        float x;
        float y = canvasClipBounds.top + 1;
        float step = centimeterPixels / 10f;
        int count = 0;

        while(y < canvasClipBounds.bottom) {
            if (count % 10 == 0){
                x = canvasClipBounds.left;
            } else if (count % 5 == 0){
                x = (canvasClipBounds.left + canvasClipBounds.right) / 3;
            } else {
                x = (canvasClipBounds.left + canvasClipBounds.right) / 2;
            }
            canvas.drawLine(x, y, canvasClipBounds.right, y, markPaint);
            y += step;
            count++;
        }

    }

    public void setCentimeterPixels(float centimeterPixels){
        this.centimeterPixels = centimeterPixels;
        postInvalidate();
    }

}
