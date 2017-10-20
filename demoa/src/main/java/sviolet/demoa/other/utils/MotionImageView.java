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
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Random;

import sviolet.demoa.R;
import sviolet.turquoise.ui.util.ViewCommonUtils;
import sviolet.turquoise.uix.viewgesturectrl.ViewGestureControllerImpl;
import sviolet.turquoise.uix.viewgesturectrl.output.SimpleRectangleOutput;
import sviolet.turquoise.util.bitmap.BitmapUtils;
import sviolet.turquoise.util.droid.MeasureUtils;

/**
 * 图片移动/缩放/编辑Demo控件
 *
 * Created by S.Violet on 2016/9/22.
 */

public class MotionImageView extends View implements ViewCommonUtils.InitListener {

    //触摸控制器
    private ViewGestureControllerImpl viewGestureController;
    //触摸输出
    private SimpleRectangleOutput output;

    private Bitmap bitmap;
    private Paint clickPaint;
    private Paint longClickPaint;
    private Rect bitmapRect = new Rect();
    private Rect canvasRect = new Rect();

    private boolean sizeChanged = false;
    private int originHeight;
    private Random random = new Random(System.currentTimeMillis());

    public MotionImageView(Context context) {
        super(context);
        init(context, null, -1);
    }

    public MotionImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1);
    }

    public MotionImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr){
        //初始化
        ViewCommonUtils.setInitListener(this, this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        sizeChanged = true;
    }

    @Override
    public void onInit() {
        //解码图片, 测试用, 这种方式如果图片大了会OOM~
        bitmap = BitmapUtils.copy(BitmapUtils.decodeFromResource(getResources(), R.mipmap.async_image_2), true);

        //点击画笔
        clickPaint = new Paint();
        clickPaint.setColor(0x80FF0000);//颜色写死
        clickPaint.setStrokeWidth(MeasureUtils.dp2px(getContext(), 3));//写死3dp
        clickPaint.setAntiAlias(true);

        //长按画笔
        longClickPaint = new Paint();
        longClickPaint.setColor(0x800000FF);//颜色写死
        clickPaint.setStrokeWidth(MeasureUtils.dp2px(getContext(), 3));//写死3dp
        longClickPaint.setAntiAlias(true);

        //手势控制器实例化
        viewGestureController = new ViewGestureControllerImpl(getContext());
        //简单的矩形输出, 图片长宽作为实际矩形, 控件长宽作为显示矩形, 最大放大率10
        output = new SimpleRectangleOutput(getContext(), bitmap.getWidth(), bitmap.getHeight(), getWidth(), getHeight(), 5, SimpleRectangleOutput.InitScaleType.FIT_CENTER);
        output.setMultiTouchMoveEnabled(true);
        //必须实现刷新接口, 调用postInvalidate()刷新
        output.setRefreshListener(new SimpleRectangleOutput.RefreshListener() {
            @Override
            public void onRefresh() {
                MotionImageView.this.postInvalidate();
            }
        });
        //点击事件
        output.setClickListener(new SimpleRectangleOutput.ClickListener() {
            @Override
            public void onClick(float actualX, float actualY, float displayX, float displayY) {
                Canvas bitmapCanvas = new Canvas(bitmap);
                bitmapCanvas.drawCircle(actualX, actualY, 30, clickPaint);
                MotionImageView.this.postInvalidate();

                //若当前放大率低于2, 则缩放到2
                if (output.getCurrZoomMagnification() < 2){
                    //手动缩放过程中, 所有手势会被禁用
                    output.manualZoom(displayX, displayY, 2, 300);
                }
            }
        });
        //长按时间
        output.setLongClickListener(new SimpleRectangleOutput.LongClickListener() {
            @Override
            public void onLongClick(float actualX, float actualY, float displayX, float displayY) {
                //长按时随机调整控件高度
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
                layoutParams.height = random.nextInt(originHeight);
                setLayoutParams(layoutParams);
                Toast.makeText(getContext(), "控件高度调整为:" + layoutParams.height, Toast.LENGTH_LONG).show();
            }
        });

        //给手势控制器设置简单矩形输出
        viewGestureController.addOutput(output);

        //记录控件原始长宽
        originHeight = getHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        //接收触摸事件
        viewGestureController.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (output == null) {
            return;
        }

        //控件尺寸变化时, 调整输出的显示矩形尺寸
        if (sizeChanged){
            sizeChanged = false;
            output.resetDisplayDimension(getWidth(), getHeight());
        }

        //从output获得当前矩形
        output.getSrcDstRect(bitmapRect, canvasRect);

        //绘制图片
        canvas.drawBitmap(bitmap, bitmapRect, canvasRect, null);

        //必须:继续刷新
        if (output.isActive()) {
            postInvalidate();
        }

    }

}
