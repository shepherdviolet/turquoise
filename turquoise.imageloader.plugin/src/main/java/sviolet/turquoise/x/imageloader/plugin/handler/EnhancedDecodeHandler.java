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

package sviolet.turquoise.x.imageloader.plugin.handler;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import sviolet.turquoise.util.conversion.ByteUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.common.CommonDecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import pl.droidsonroids.gif.EnhancedGifDrawable;

/**
 * TODO
 * Created by S.Violet on 2016/5/4.
 */
public class EnhancedDecodeHandler extends CommonDecodeHandler {

    private static final byte[] GIF_HEADER = ByteUtils.hexToBytes("47494638");

    @Override
    public ImageResource<?> onDecode(Context applicationContext, Context context, Task.Info taskInfo, byte[] data, TLogger logger) {
        if (!isGif(data)){
            return super.onDecode(applicationContext, context, taskInfo, data, logger);
        }
        try {
            return new ImageResource<>(ImageResource.Type.GIF, EnhancedGifDrawable.decode(data, taskInfo.getParams().getReqWidth(), taskInfo.getParams().getReqHeight()));
        } catch (IOException e) {
            throw new RuntimeException("[TILoader:EnhancedDecodeHandler]error while decoding gif from bytes", e);
        }
    }

    @Override
    public ImageResource<?> onDecode(Context applicationContext, Context context, Task.Info taskInfo, File file, TLogger logger) {
        if (!isGif(file)) {
            return super.onDecode(applicationContext, context, taskInfo, file, logger);
        }
        try {
            return new ImageResource<>(ImageResource.Type.GIF, EnhancedGifDrawable.decode(file, taskInfo.getParams().getReqWidth(), taskInfo.getParams().getReqHeight()));
        } catch (IOException e) {
            throw new RuntimeException("[TILoader:EnhancedDecodeHandler]error while decoding gif from file", e);
        }
    }

    private boolean isGif(byte[] data){
        if (data == null || data.length < GIF_HEADER.length){
            return false;
        }
        for (int i = 0 ; i < GIF_HEADER.length ; i++){
            if (data[i] != GIF_HEADER[i]){
                return false;
            }
        }
        return true;
    }

    private boolean isGif(File file){
        if (file == null || !file.exists()){
            return false;
        }
        RandomAccessFile randomAccessFile = null;
        try{
            randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(0);
            byte[] buffer = new byte[GIF_HEADER.length];
            long length = randomAccessFile.read(buffer);
            if (length < GIF_HEADER.length){
                return false;
            }
            for (int i = 0 ; i < GIF_HEADER.length ; i++){
                if (buffer[i] != GIF_HEADER[i]){
                    return false;
                }
            }
            return true;
        }catch (Exception e){
            return false;
        }finally {
            if (randomAccessFile != null){
                try {
                    randomAccessFile.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

}
