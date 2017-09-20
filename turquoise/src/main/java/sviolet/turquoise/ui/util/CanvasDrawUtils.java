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

package sviolet.turquoise.ui.util;

import android.graphics.Canvas;
import android.graphics.Paint;

import sviolet.thistle.util.common.CheckUtils;

/**
 * 画布绘制工具
 *
 * Created by S.Violet on 2017/9/20.
 */
public class CanvasDrawUtils {

    /**
     * 在矩形范围内绘制文字
     * @param canvas 画布
     * @param left 画布中的左边界
     * @param top 画布中的上边界
     * @param right 画布中的右边界
     * @param bottom 画布中的下边界
     * @param offsetX 绘制偏移量X
     * @param offsetY 绘制偏移量Y
     * @param rowSpacing 行间距
     * @param isClipBound 是否切除矩形范围外的文字
     * @param textWidthBuffer 用于计算文字宽度的Buffer, 请复用同一个实例(不要每次new), 建议new float[100]
     * @param text 要绘制的文字
     * @param paint 画笔
     */
    public static void drawText(Canvas canvas, float left, float top, float right, float bottom, float offsetX, float offsetY, float rowSpacing, boolean isClipBound, float[] textWidthBuffer, String text, Paint paint){
        if (canvas == null || paint == null || CheckUtils.isEmpty(text) || left >= right || top >= bottom){
            return;
        }
        if (textWidthBuffer == null || textWidthBuffer.length <= 0){
            throw new RuntimeException("textWidthBuffer is null or 0 size");
        }

        //裁剪画布
        if (isClipBound) {
            canvas.save();
            canvas.clipRect(left, top, right, bottom);
        }

        //默认行间距
        if (rowSpacing < 0){
            rowSpacing = paint.getTextSize();
        }

        int textLength = text.length();//字数
        float lineMaxWidth = right - left;
        float textHeight = paint.getTextSize();//字体高度

        int measureStart = 0;//宽度测量起始位置
        int measureEnd = textWidthBuffer.length - 1;//宽度测量结束位置
        if (measureEnd >= textLength){
            measureEnd = textLength - 1;
        }

        int drawStart = 0;//绘制起始位置
        int drawEnd = 0;//绘制结束位置
        int drawLines = 0;//当前绘制的行数

        int widthCount = 0;

        //循环测量文字宽度
        while (measureEnd < textLength) {
            //测量文字宽度
            paint.getTextWidths(text, measureStart, measureEnd + 1, textWidthBuffer);

            for (int i = 0; i <= measureEnd - measureStart ; i++){
                //超过行最大宽度时绘制
                if (widthCount + textWidthBuffer[i] > lineMaxWidth){
                    canvas.drawText(text, drawStart, drawEnd, left + offsetX, top + textHeight + drawLines * rowSpacing + offsetY, paint);
                    drawStart = drawEnd;
                    drawLines++;
                    widthCount = 0;
                }
                widthCount += textWidthBuffer[i];
                drawEnd++;
            }

            //测量下一段文字
            measureStart = measureEnd + 1;
            measureEnd = measureStart + textWidthBuffer.length - 1;
            if (measureEnd >= textLength){
                measureEnd = textLength - 1;
            }
            if (measureEnd < measureStart) {
                break;
            }
        }

        //绘制剩余字
        if (drawEnd > drawStart) {
            canvas.drawText(text, drawStart, drawEnd, left + offsetX, top + textHeight + drawLines * rowSpacing + offsetY, paint);
        }

        //裁剪恢复
        if (isClipBound) {
            canvas.restore();
        }

    }

}
