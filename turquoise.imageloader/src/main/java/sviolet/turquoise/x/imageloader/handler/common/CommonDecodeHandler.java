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

package sviolet.turquoise.x.imageloader.handler.common;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;

import sviolet.turquoise.util.common.BitmapUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>common implementation of DecodeHandler</p>
 *
 * Created by S.Violet on 2016/4/1.
 */
public class CommonDecodeHandler extends DecodeHandler {

    @Override
    public ImageResource<?> onDecode(Context applicationContext, Context context, Task.Info taskInfo, byte[] data, TLogger logger) {
        //routine decoding
        Bitmap bitmap = BitmapUtils.decodeFromByteArray(data,taskInfo.getParams().getReqWidth(), taskInfo.getParams().getReqHeight(), taskInfo.getParams().getBitmapConfig());
        if (bitmap == null)
            throw new RuntimeException("[TILoader:CommonDecodeHandler]decoding failed, illegal image data");
        //exact decoding
        if (taskInfo.getParams().isExactDecoding()){
            bitmap = BitmapUtils.scaleTo(bitmap, taskInfo.getParams().getReqWidth(), taskInfo.getParams().getReqHeight(), true);
            if (bitmap == null)
                throw new RuntimeException("[TILoader:CommonDecodeHandler]exact decoding: scale failed");
        }
        return new ImageResource<>(ImageResource.Type.BITMAP, bitmap);
    }

    @Override
    public ImageResource<?> onDecode(Context applicationContext, Context context, Task.Info taskInfo, File file, TLogger logger) {
        //routine decoding
        Bitmap bitmap = BitmapUtils.decodeFromFile(file.getAbsolutePath(), taskInfo.getParams().getReqWidth(), taskInfo.getParams().getReqHeight(), taskInfo.getParams().getBitmapConfig());
        if (bitmap == null)
            throw new RuntimeException("[TILoader:CommonDecodeHandler]decoding failed, illegal image data");
        //exact decoding
        if (taskInfo.getParams().isExactDecoding()){
            bitmap = BitmapUtils.scaleTo(bitmap, taskInfo.getParams().getReqWidth(), taskInfo.getParams().getReqHeight(), true);
            if (bitmap == null)
                throw new RuntimeException("[TILoader:CommonDecodeHandler]exact decoding: scale failed");
        }
        return new ImageResource<>(ImageResource.Type.BITMAP, bitmap);
    }
}
