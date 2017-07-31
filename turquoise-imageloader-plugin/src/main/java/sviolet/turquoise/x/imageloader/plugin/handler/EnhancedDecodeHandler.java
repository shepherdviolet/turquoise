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

import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.common.CommonDecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import pl.droidsonroids.gif.EnhancedGifDrawable;

/**
 * <p>EnhancedDecodeHandler</p>
 *
 * <p>NOTICE:: TILoader will load this plugin automatically, as long as your project dependent on
 * module "turquoise.imageloader.plugin". Don't need to do anything else.</p>
 *
 * <p>1.Add support for GIF.</p>
 *
 * Created by S.Violet on 2016/5/4.
 */
public class EnhancedDecodeHandler extends CommonDecodeHandler {

    private static final byte[] GIF_HEADER = ByteUtils.hexToBytes("47494638");

    @Override
    public ImageResource onDecodeInner(Context applicationContext, Context context, Task.Info taskInfo, byte[] data, TLogger logger, int reqWidth, int reqHeight) {
        if (!isGif(data)){
            return super.onDecodeInner(applicationContext, context, taskInfo, data, logger, reqWidth, reqHeight);
        }
        try {
            return new ImageResource(ImageResource.Type.GIF, EnhancedGifDrawable.decode(data, reqWidth, reqHeight, taskInfo.getParams().getDecodeInSampleQuality()));
        } catch (IOException e) {
            throw new RuntimeException("[TILoader:EnhancedDecodeHandler]error while decoding gif from bytes", e);
        }
    }

    @Override
    public ImageResource onDecodeInner(Context applicationContext, Context context, Task.Info taskInfo, File file, TLogger logger, int reqWidth, int reqHeight) {
        if (!isGif(file)) {
            return super.onDecodeInner(applicationContext, context, taskInfo, file, logger, reqWidth, reqHeight);
        }
        try {
            return new ImageResource(ImageResource.Type.GIF, EnhancedGifDrawable.decode(file, reqWidth, reqHeight, taskInfo.getParams().getDecodeInSampleQuality()));
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
