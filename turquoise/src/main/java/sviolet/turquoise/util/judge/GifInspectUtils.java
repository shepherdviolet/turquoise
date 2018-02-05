/*
 * Copyright (C) 2015-2018 S.Violet
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

package sviolet.turquoise.util.judge;

import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import sviolet.thistle.util.conversion.ByteUtils;

/**
 * GIF判断工具, 判断是不是GIF
 *
 * @author S.Violet
 */
public class GifInspectUtils {

    private static final byte[] GIF_HEADER = ByteUtils.hexToBytes("47494638");

    public static boolean isGif(byte[] data){
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

    public static boolean isGif(File file){
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

    public static boolean isGif(Resources resources, int resId){
        InputStream inputStream = null;
        try {
            inputStream = resources.openRawResource(resId);
            byte[] buffer = new byte[GIF_HEADER.length];
            long length = inputStream.read(buffer);
            if (length < GIF_HEADER.length){
                return false;
            }
            for (int i = 0 ; i < GIF_HEADER.length ; i++){
                if (buffer[i] != GIF_HEADER[i]){
                    return false;
                }
            }
            return true;
        } catch (Throwable t) {
            return false;
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (Exception ignore){
                }
            }
        }
    }

    public static boolean isGif(AssetManager assetManager, String assetsPath){
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(assetsPath);
            byte[] buffer = new byte[GIF_HEADER.length];
            long length = inputStream.read(buffer);
            if (length < GIF_HEADER.length){
                return false;
            }
            for (int i = 0 ; i < GIF_HEADER.length ; i++){
                if (buffer[i] != GIF_HEADER[i]){
                    return false;
                }
            }
            return true;
        } catch (Throwable t) {
            return false;
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (Exception ignore){
                }
            }
        }
    }

}
