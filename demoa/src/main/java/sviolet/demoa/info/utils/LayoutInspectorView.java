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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import sviolet.turquoise.util.droid.MeasureUtils;

/**
 * 布局分析界面
 *
 * Created by S.Violet on 2017/9/11.
 */
public class LayoutInspectorView extends View {

    private Paint textPaint = new Paint();
    private Paint[] paints = new Paint[6];
    private int[] locationOnScreen = new int[2];
    private Rect canvasRect = new Rect();

    private LayoutInspectorNodeInfo nodeInfo;
    private List<String> infos = new ArrayList<>();

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
        textPaint.setTextSize(MeasureUtils.dp2px(getContext(), 12));
        textPaint.setColor(0xffff0000);

        paints[0] = new Paint(Paint.ANTI_ALIAS_FLAG);
        paints[0].setColor(0xffff0000);
        paints[0].setStrokeWidth(2);
        paints[0].setStyle(Paint.Style.STROKE);

        paints[1] = new Paint(Paint.ANTI_ALIAS_FLAG);
        paints[1].setColor(0xff00ff00);
        paints[1].setStrokeWidth(1);
        paints[1].setStyle(Paint.Style.STROKE);

        paints[2] = new Paint(Paint.ANTI_ALIAS_FLAG);
        paints[2].setColor(0xff0000ff);
        paints[2].setStrokeWidth(2);
        paints[2].setStyle(Paint.Style.STROKE);

        paints[3] = new Paint(Paint.ANTI_ALIAS_FLAG);
        paints[3].setColor(0xffffff00);
        paints[3].setStrokeWidth(1);
        paints[3].setStyle(Paint.Style.STROKE);

        paints[4] = new Paint(Paint.ANTI_ALIAS_FLAG);
        paints[4].setColor(0xffff00ff);
        paints[4].setStrokeWidth(2);
        paints[4].setStyle(Paint.Style.STROKE);

        paints[5] = new Paint(Paint.ANTI_ALIAS_FLAG);
        paints[5].setColor(0xff00ffff);
        paints[5].setStrokeWidth(1);
        paints[5].setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        getLocationOnScreen(locationOnScreen);
        canvas.getClipBounds(canvasRect);
        int textLineHeight = MeasureUtils.dp2px(getContext(), 12);

        if (nodeInfo != null) {
            drawNode(canvas, nodeInfo, 0);
            for (int i = 0 ; i < infos.size() ; i++) {
                canvas.drawText(infos.get(i), 0, canvasRect.bottom - i * textLineHeight, textPaint);
            }
        }

    }

    private void drawNode(Canvas canvas, LayoutInspectorNodeInfo nodeInfo, int level){
        canvas.drawRect(
                nodeInfo.getRect().left - locationOnScreen[0],
                nodeInfo.getRect().top - locationOnScreen[1],
                nodeInfo.getRect().right - locationOnScreen[0],
                nodeInfo.getRect().bottom - locationOnScreen[1], paints[level % 6]);
        if (nodeInfo.getSubs().size() > 0){
            for (LayoutInspectorNodeInfo sub : nodeInfo.getSubs()){
                drawNode(canvas, sub, level + 1);
            }
        }
    }

    public void refreshNodeInfo(LayoutInspectorNodeInfo nodeInfo){
        this.nodeInfo = nodeInfo;
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        infos.clear();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (nodeInfo != null){
                    seekNode(nodeInfo, event.getRawX(), event.getRawY(), 0);
                }
                break;
        }
        postInvalidate();
        return true;
    }

    private void seekNode(LayoutInspectorNodeInfo nodeInfo, float rawX, float rawY, int level){
        if (rawX > nodeInfo.getRect().left && rawX < nodeInfo.getRect().right &&
                rawY > nodeInfo.getRect().top && rawY < nodeInfo.getRect().bottom){
            infos.add(level + "--" + nodeInfo.getId() + "--" + nodeInfo.getClazz());
        }
        if (nodeInfo.getSubs().size() > 0){
            for (LayoutInspectorNodeInfo sub : nodeInfo.getSubs()){
                seekNode(sub, rawX, rawY, level + 1);
            }
        }
    }

}
