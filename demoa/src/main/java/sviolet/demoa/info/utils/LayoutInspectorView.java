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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import sviolet.turquoise.util.droid.MeasureUtils;

/**
 * 布局分析控件
 *
 * Created by S.Violet on 2017/9/11.
 */
public class LayoutInspectorView extends View {

    private static final int TEXT_SIZE = 10;

    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint rectFirstPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint rectLastPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int[] viewLocationOnScreen = new int[]{0, 0};
    private Rect canvasRect = new Rect();
//    private int screenWidth;
    private int screenHeight;
    private int textLineHeight;

    private LayoutInspectorNodeInfo nodeInfo;
    private boolean drawOnUpperHalf = false;

    private List<String> infoList = new ArrayList<>();

    public LayoutInspectorView(Context context) {
        super(context);
        init();
    }

    public LayoutInspectorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LayoutInspectorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){

        //measure//////////////////////////////////

//        screenWidth = MeasureUtils.getScreenWidth(getContext());
        screenHeight = MeasureUtils.getScreenHeight(getContext());
        textLineHeight = MeasureUtils.dp2px(getContext(), TEXT_SIZE);

        //text paint//////////////////////////////////

        textPaint.setTextSize(MeasureUtils.dp2px(getContext(), TEXT_SIZE));
        textPaint.setColor(0xffffffff);

        textBackgroundPaint.setColor(0xA0000000);
        textBackgroundPaint.setStyle(Paint.Style.FILL);

        //rect paint///////////////////////////////////

        //level 0 paint
        rectFirstPaint.setColor(0x00000000);
        rectFirstPaint.setStrokeWidth(1);
        rectFirstPaint.setStyle(Paint.Style.STROKE);

        //last paint
        rectLastPaint.setColor(0xffff0000);
        rectLastPaint.setStrokeWidth(1);
        rectLastPaint.setStyle(Paint.Style.STROKE);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        getLocationOnScreen(viewLocationOnScreen);
        canvas.getClipBounds(canvasRect);

        if (nodeInfo != null) {
            drawNode(canvas, nodeInfo, 0);
            drawInfo(canvas);
        }

    }

    /**
     * 绘制视图节点的位置
     */
    private void drawNode(Canvas canvas, LayoutInspectorNodeInfo nodeInfo, int level){

        Paint paint = null;

        if (level <= 0){
            paint = rectFirstPaint;
        } else {
            paint = rectLastPaint;
        }

        canvas.drawRect(
                nodeInfo.getRect().left - viewLocationOnScreen[0],
                nodeInfo.getRect().top - viewLocationOnScreen[1],
                nodeInfo.getRect().right - viewLocationOnScreen[0],
                nodeInfo.getRect().bottom - viewLocationOnScreen[1], paint);

        if (nodeInfo.getSubs().size() > 0){
            for (LayoutInspectorNodeInfo sub : nodeInfo.getSubs()){
                drawNode(canvas, sub, level + 1);
            }
        }
    }

    /**
     * 绘制触摸位置的控件信息
     */
    private void drawInfo(Canvas canvas) {
        if (infoList.size() <= 0) {
            return;
        }

        int y = canvasRect.bottom - infoList.size() * textLineHeight;
        if (drawOnUpperHalf){
            y -= canvasRect.bottom / 2;
        }

        canvas.drawRect(0, y, canvasRect.right, (drawOnUpperHalf ? canvasRect.bottom / 2 : canvasRect.bottom) + textLineHeight, textBackgroundPaint);
        for (int i = 0; i < infoList.size() ; i++) {
            y += textLineHeight;
            canvas.drawText(infoList.get(i), 0, y, textPaint);
        }
    }

    public void refreshNodeInfo(LayoutInspectorNodeInfo nodeInfo){
        this.nodeInfo = nodeInfo;
        if (getVisibility() == View.VISIBLE) {
            postInvalidate();
        }
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        infoList.clear();
        drawOnUpperHalf = event.getRawY() > screenHeight * 2 / 3;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (nodeInfo != null){
                    infoList.add("package:" + nodeInfo.getPackageName());
                    printNodeInfos(nodeInfo, event.getRawX(), event.getRawY(), 0);
                }
                break;
        }
        postInvalidate();
        return true;
    }

    private void printNodeInfos(LayoutInspectorNodeInfo nodeInfo, float rawX, float rawY, int level){
        if (rawX > nodeInfo.getRect().left && rawX < nodeInfo.getRect().right &&
                rawY > nodeInfo.getRect().top && rawY < nodeInfo.getRect().bottom){

            if (nodeInfo.getId() != null) {
                int index = nodeInfo.getId().indexOf(":");
                if (index < 0) {
                    infoList.add("<" + level + "> " + nodeInfo.getId());
                } else {
                    infoList.add("<" + level + "> " + nodeInfo.getId().substring(index + 1));
                }
            } else {
                infoList.add("<" + level + "> undefined");
            }
            infoList.add("      " + nodeInfo.getClazz());

        }
        if (nodeInfo.getSubs().size() > 0){
            for (LayoutInspectorNodeInfo sub : nodeInfo.getSubs()){
                printNodeInfos(sub, rawX, rawY, level + 1);
            }
        }
    }

}
