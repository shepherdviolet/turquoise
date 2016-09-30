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

package sviolet.demoa.other.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import sviolet.demoa.R;
import sviolet.turquoise.ui.util.ViewCommonUtils;
import sviolet.turquoise.uix.viewgesturectrl.ViewGestureControllerImpl;
import sviolet.turquoise.uix.viewgesturectrl.output.SimpleRectangleOutput;
import sviolet.turquoise.util.common.BitmapUtils;

/**
 * Created by S.Violet on 2016/9/22.
 */

public class ImageMotionView extends View implements ViewCommonUtils.InitListener {

    private ViewGestureControllerImpl viewGestureController;
    private SimpleRectangleOutput output;

    private Bitmap bitmap;
    private Paint clickPaint;
    private Paint longClickPaint;
    private Rect canvasRect = new Rect();
    private Rect bitmapRect = new Rect();

    public ImageMotionView(Context context) {
        super(context);
        init(context, null, -1);
    }

    public ImageMotionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1);
    }

    public ImageMotionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, null, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr){
        ViewCommonUtils.setInitListener(this, this);
    }

    @Override
    public void onInit() {
        bitmap = BitmapUtils.copy(BitmapUtils.decodeFromResource(getResources(), R.mipmap.async_image_1), true);

        clickPaint = new Paint();
        clickPaint.setColor(0x80FF0000);
        clickPaint.setStrokeWidth(5);
        clickPaint.setAntiAlias(true);

        longClickPaint = new Paint();
        longClickPaint.setColor(0x800000FF);
        longClickPaint.setStrokeWidth(5);
        longClickPaint.setAntiAlias(true);

        viewGestureController = new ViewGestureControllerImpl(getContext());
        output = new SimpleRectangleOutput(getContext(), bitmap.getWidth(), bitmap.getHeight(), getWidth(), getHeight(), 10);
        output.setMultiTouchMoveEnabled(true);
        output.setRefreshListener(new SimpleRectangleOutput.RefreshListener() {
            @Override
            public void onRefresh() {
                ImageMotionView.this.postInvalidate();
            }
        });
        output.setClickListener(new SimpleRectangleOutput.ClickListener() {
            @Override
            public void onClick(float actualX, float actualY, float displayX, float displayY) {
                Canvas bitmapCanvas = new Canvas(bitmap);
                bitmapCanvas.drawCircle(actualX, actualY, 30, clickPaint);
                ImageMotionView.this.postInvalidate();
            }
        });
        output.setLongClickListener(new SimpleRectangleOutput.LongClickListener() {
            @Override
            public void onLongClick(float actualX, float actualY, float displayX, float displayY) {
                Canvas bitmapCanvas = new Canvas(bitmap);
                bitmapCanvas.drawCircle(actualX, actualY, 80, longClickPaint);
                ImageMotionView.this.postInvalidate();
            }
        });
        viewGestureController.addOutput(output);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        viewGestureController.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(canvasRect);
        output.getSrcDstRect(bitmapRect, canvasRect);
        canvas.drawBitmap(bitmap, bitmapRect, canvasRect, null);

        if (output.isActive())
            postInvalidate();
    }

}
