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

package sviolet.demoaimageloader.custom;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;

import sviolet.turquoise.util.common.BitmapUtils;
import sviolet.turquoise.util.droid.MeasureUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * 自定义实现图片解码: 在图上绘制文字
 *
 * 注意::
 * 当taskInfo.getParams().isExactDecoding()==false时, 进行快速解码, 解码的尺寸只需接近请求尺寸即可
 * (taskInfo.getParams().getReqWidth()/taskInfo.getParams().getReqHeight()), 注重解码速度.
 *
 * 当taskInfo.getParams().isExactDecoding()==true时, 需要进行精确解码, 解码的尺寸因等于请求尺寸, 以满足
 * 特殊需求.
 *
 * Created by S.Violet on 2016/4/27.
 */
public class MyDecodeHandler extends DecodeHandler {

    @Override
    public ImageResource<?> onDecode(Context applicationContext, Context context, Task.Info taskInfo, byte[] data, TLogger logger) {
        //快速解码, 解码出的图片尺寸接近请求尺寸, 但不等于请求尺寸
        Bitmap bitmap = BitmapUtils.decodeFromByteArray(data,taskInfo.getParams().getReqWidth(), taskInfo.getParams().getReqHeight(), taskInfo.getParams().getBitmapConfig());
        if (bitmap == null)
            return null;
        //精确解码, 缩放后的图片尺寸等于请求尺寸
        if (taskInfo.getParams().isExactDecoding()){
            bitmap = BitmapUtils.scaleTo(bitmap, taskInfo.getParams().getReqWidth(), taskInfo.getParams().getReqHeight(), true);
            if (bitmap == null)
                return null;
        }
        //自定义内容:在图上绘制文字
        bitmap = BitmapUtils.drawText(bitmap, "custom decode", MeasureUtils.dp2px(applicationContext, 20), MeasureUtils.dp2px(applicationContext, 30), MeasureUtils.dp2px(applicationContext, 20), 0xFFFF0000, true);
        if (bitmap == null)
            return null;
        return new ImageResource<>(ImageResource.Type.BITMAP, bitmap);
    }

    @Override
    public ImageResource<?> onDecode(Context applicationContext, Context context, Task.Info taskInfo, File file, TLogger logger) {
        //快速解码, 解码出的图片尺寸接近请求尺寸, 但不等于请求尺寸
        Bitmap bitmap = BitmapUtils.decodeFromFile(file.getAbsolutePath(), taskInfo.getParams().getReqWidth(), taskInfo.getParams().getReqHeight(), taskInfo.getParams().getBitmapConfig());
        if (bitmap == null)
            return null;
        //精确解码, 缩放后的图片尺寸等于请求尺寸
        if (taskInfo.getParams().isExactDecoding()){
            bitmap = BitmapUtils.scaleTo(bitmap, taskInfo.getParams().getReqWidth(), taskInfo.getParams().getReqHeight(), true);
            if (bitmap == null)
                return null;
        }
        //自定义内容:在图上绘制文字
        bitmap = BitmapUtils.drawText(bitmap, "custom decode", MeasureUtils.dp2px(applicationContext, 20), MeasureUtils.dp2px(applicationContext, 30), MeasureUtils.dp2px(applicationContext, 20), 0xFFFF0000, true);
        if (bitmap == null)
            return null;
        return new ImageResource<>(ImageResource.Type.BITMAP, bitmap);
    }

}
