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

package sviolet.turquoise.x.imageloader.handler.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import pl.droidsonroids.gif.EnhancedGifDrawable;
import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.turquoise.util.bitmap.BitmapUtils;
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

    private static final byte[] GIF_HEADER = ByteUtils.hexToBytes("47494638");

    private static final int GIF_DRAWABLE_REFERENCE_STATE_UNKNOWN = 0;
    private static final int GIF_DRAWABLE_REFERENCE_STATE_MISSING = -1;
    private static final int GIF_DRAWABLE_REFERENCE_STATE_EXISTS = 1;

    private int gifDrawableReferenceState = 0;

    //bytes//////////////////////////////////////////////////////////////////////////

    @Override
    public ImageResource onDecode(Context applicationContext, Context context, Task.Info taskInfo, byte[] data, TLogger logger) {
        Integer customReqWidth = taskInfo.getParams().getExtraInteger(DecodeHandler.CUSTOM_REQ_WIDTH);
        Integer customReqHeight = taskInfo.getParams().getExtraInteger(DecodeHandler.CUSTOM_REQ_HEIGHT);
        int reqWidth = customReqWidth == null ? taskInfo.getParams().getReqWidth() : customReqWidth;
        int reqHeight = customReqHeight == null ? taskInfo.getParams().getReqHeight() : customReqHeight;
        return onDecodeInner(applicationContext, context, taskInfo, data, logger, reqWidth, reqHeight);
    }

    protected ImageResource onDecodeInner(Context applicationContext, Context context, Task.Info taskInfo, byte[] data, TLogger logger, int reqWidth, int reqHeight) {
        if (!isGif(data, logger)){
            //bitmap
            return onDecodeBitmap(applicationContext, context, taskInfo, data, logger, reqWidth, reqHeight);
        } else {
            //gif
            return onDecodeGif(applicationContext, context, taskInfo, data, logger, reqWidth, reqHeight);
        }
    }

    private ImageResource onDecodeBitmap(Context applicationContext, Context context, Task.Info taskInfo, byte[] data, TLogger logger, int reqWidth, int reqHeight) {
        //decoding
        Bitmap bitmap = BitmapUtils.decodeFromByteArray(data, reqWidth, reqHeight, taskInfo.getParams().getBitmapConfig(), taskInfo.getParams().getDecodeInSampleQuality());
        if (bitmap == null) {
            throw new RuntimeException("[CommonDecodeHandler]decoding failed, illegal image data");
        }
        //scale
        switch (taskInfo.getParams().getDecodeScaleStrategy()) {
            case SCALE_FIT_WIDTH_HEIGHT:
                bitmap = BitmapUtils.scaleTo(bitmap, reqWidth, reqHeight, true);
                break;
            case SCALE_FIT_WIDTH:
                bitmap = BitmapUtils.scaleTo(bitmap, reqWidth, 0, true);
                break;
            case SCALE_FIT_HEIGHT:
                bitmap = BitmapUtils.scaleTo(bitmap, 0, reqHeight, true);
                break;
            default:
                break;
        }
        if (bitmap == null) {
            throw new RuntimeException("[CommonDecodeHandler]scale: scale failed");
        }
        if (logger.checkEnable(TLogger.DEBUG)){
            logger.d("[CommonDecodeHandler]decoded size:" + bitmap.getWidth() + "*" + bitmap.getHeight() + " task:" + taskInfo);
        }
        return new ImageResource(ImageResource.Type.BITMAP, bitmap);
    }

    private ImageResource onDecodeGif(Context applicationContext, Context context, Task.Info taskInfo, byte[] data, TLogger logger, int reqWidth, int reqHeight) {
        try {
            return new ImageResource(ImageResource.Type.GIF, EnhancedGifDrawable.decode(data, reqWidth, reqHeight, taskInfo.getParams().getDecodeInSampleQuality()));
        } catch (IOException e) {
            throw new RuntimeException("[EnhancedDecodeHandler]error while decoding gif from bytes", e);
        }
    }

    private boolean isGif(byte[] data, TLogger logger){
        if (checkGifDrawableReference(logger)) {
            return false;
        }

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

    //file//////////////////////////////////////////////////////////////////////////

    @Override
    public ImageResource onDecode(Context applicationContext, Context context, Task.Info taskInfo, File file, TLogger logger) {
        Integer customReqWidth = taskInfo.getParams().getExtraInteger(DecodeHandler.CUSTOM_REQ_WIDTH);
        Integer customReqHeight = taskInfo.getParams().getExtraInteger(DecodeHandler.CUSTOM_REQ_HEIGHT);
        int reqWidth = customReqWidth == null ? taskInfo.getParams().getReqWidth() : customReqWidth;
        int reqHeight = customReqHeight == null ? taskInfo.getParams().getReqHeight() : customReqHeight;
        return onDecodeInner(applicationContext, context, taskInfo, file, logger, reqWidth, reqHeight);
    }

    protected ImageResource onDecodeInner(Context applicationContext, Context context, Task.Info taskInfo, File file, TLogger logger, int reqWidth, int reqHeight){
        if (!isGif(file, logger)) {
            return onDecodeBitmap(applicationContext, context, taskInfo, file, logger, reqWidth, reqHeight);
        } else {
            return onDecodeGif(applicationContext, context, taskInfo, file, logger, reqWidth, reqHeight);
        }
    }

    private ImageResource onDecodeBitmap(Context applicationContext, Context context, Task.Info taskInfo, File file, TLogger logger, int reqWidth, int reqHeight){
        //decoding
        Bitmap bitmap = BitmapUtils.decodeFromFile(file.getAbsolutePath(), reqWidth, reqHeight, taskInfo.getParams().getBitmapConfig(), taskInfo.getParams().getDecodeInSampleQuality());
        if (bitmap == null) {
            throw new RuntimeException("[CommonDecodeHandler]decoding failed, illegal image data");
        }
        //scale
        switch (taskInfo.getParams().getDecodeScaleStrategy()){
            case SCALE_FIT_WIDTH_HEIGHT:
                bitmap = BitmapUtils.scaleTo(bitmap, reqWidth, reqHeight, true);
                break;
            case SCALE_FIT_WIDTH:
                bitmap = BitmapUtils.scaleTo(bitmap, reqWidth, 0, true);
                break;
            case SCALE_FIT_HEIGHT:
                bitmap = BitmapUtils.scaleTo(bitmap, 0, reqHeight, true);
                break;
            default:
                break;
        }
        if (bitmap == null) {
            throw new RuntimeException("[CommonDecodeHandler]scale: scale failed");
        }
        if (logger.checkEnable(TLogger.DEBUG)) {
            logger.d("[CommonDecodeHandler]decoded size:" + bitmap.getWidth() + "*" + bitmap.getHeight() + " task:" + taskInfo);
        }
        return new ImageResource(ImageResource.Type.BITMAP, bitmap);
    }

    private ImageResource onDecodeGif(Context applicationContext, Context context, Task.Info taskInfo, File file, TLogger logger, int reqWidth, int reqHeight) {
        try {
            return new ImageResource(ImageResource.Type.GIF, EnhancedGifDrawable.decode(file, reqWidth, reqHeight, taskInfo.getParams().getDecodeInSampleQuality()));
        } catch (IOException e) {
            throw new RuntimeException("[EnhancedDecodeHandler]error while decoding gif from file", e);
        }
    }

    private boolean isGif(File file, TLogger logger){
        if (checkGifDrawableReference(logger)) {
            return false;
        }

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

    //resource//////////////////////////////////////////////////////////////////////////

    @Override
    public ImageResource onDecodeRes(Context applicationContext, Context context, Task.Info taskInfo, int resId, TLogger logger) {
        Integer customReqWidth = taskInfo.getParams().getExtraInteger(DecodeHandler.CUSTOM_REQ_WIDTH);
        Integer customReqHeight = taskInfo.getParams().getExtraInteger(DecodeHandler.CUSTOM_REQ_HEIGHT);
        int reqWidth = customReqWidth == null ? taskInfo.getParams().getReqWidth() : customReqWidth;
        int reqHeight = customReqHeight == null ? taskInfo.getParams().getReqHeight() : customReqHeight;
        return onDecodeResInner(applicationContext, context, taskInfo, resId, logger, reqWidth, reqHeight);
    }

    protected ImageResource onDecodeResInner(Context applicationContext, Context context, Task.Info taskInfo, int resId, TLogger logger, int reqWidth, int reqHeight){
        if (!isResGif(applicationContext.getResources(), resId, logger)) {
            return onDecodeResBitmap(applicationContext, context, taskInfo, resId, logger, reqWidth, reqHeight);
        } else {
            return onDecodeResGif(applicationContext, context, taskInfo, resId, logger, reqWidth, reqHeight);
        }
    }

    private ImageResource onDecodeResBitmap(Context applicationContext, Context context, Task.Info taskInfo, int resId, TLogger logger, int reqWidth, int reqHeight){
        //decoding
        Bitmap bitmap = BitmapUtils.decodeFromResource(applicationContext.getResources(), resId, reqWidth, reqHeight, taskInfo.getParams().getBitmapConfig(), taskInfo.getParams().getDecodeInSampleQuality());
        if (bitmap == null) {
            throw new RuntimeException("[CommonDecodeHandler]decoding failed, illegal image data");
        }
        //scale
        switch (taskInfo.getParams().getDecodeScaleStrategy()){
            case SCALE_FIT_WIDTH_HEIGHT:
                bitmap = BitmapUtils.scaleTo(bitmap, reqWidth, reqHeight, true);
                break;
            case SCALE_FIT_WIDTH:
                bitmap = BitmapUtils.scaleTo(bitmap, reqWidth, 0, true);
                break;
            case SCALE_FIT_HEIGHT:
                bitmap = BitmapUtils.scaleTo(bitmap, 0, reqHeight, true);
                break;
            default:
                break;
        }
        if (bitmap == null) {
            throw new RuntimeException("[CommonDecodeHandler]scale: scale failed");
        }
        if (logger.checkEnable(TLogger.DEBUG)) {
            logger.d("[CommonDecodeHandler]decoded size:" + bitmap.getWidth() + "*" + bitmap.getHeight() + " task:" + taskInfo);
        }
        return new ImageResource(ImageResource.Type.BITMAP, bitmap);
    }

    private ImageResource onDecodeResGif(Context applicationContext, Context context, Task.Info taskInfo, int resId, TLogger logger, int reqWidth, int reqHeight) {
        try {
            return new ImageResource(ImageResource.Type.GIF, EnhancedGifDrawable.decode(applicationContext.getResources(), resId, reqWidth, reqHeight, taskInfo.getParams().getDecodeInSampleQuality()));
        } catch (IOException e) {
            throw new RuntimeException("[EnhancedDecodeHandler]error while decoding gif from resource", e);
        }
    }

    private boolean isResGif(Resources resources, int resId, TLogger logger){
        if (checkGifDrawableReference(logger)) {
            return false;
        }

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

    //assets///////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ImageResource onDecodeAssets(Context applicationContext, Context context, Task.Info taskInfo, String assetsPath, TLogger logger) {
        Integer customReqWidth = taskInfo.getParams().getExtraInteger(DecodeHandler.CUSTOM_REQ_WIDTH);
        Integer customReqHeight = taskInfo.getParams().getExtraInteger(DecodeHandler.CUSTOM_REQ_HEIGHT);
        int reqWidth = customReqWidth == null ? taskInfo.getParams().getReqWidth() : customReqWidth;
        int reqHeight = customReqHeight == null ? taskInfo.getParams().getReqHeight() : customReqHeight;
        return onDecodeAssetsInner(applicationContext, context, taskInfo, assetsPath, logger, reqWidth, reqHeight);
    }

    protected ImageResource onDecodeAssetsInner(Context applicationContext, Context context, Task.Info taskInfo, String assetsPath, TLogger logger, int reqWidth, int reqHeight){
        if (!isAssetsGif(applicationContext, assetsPath, logger)) {
            return onDecodeAssetsBitmap(applicationContext, context, taskInfo, assetsPath, logger, reqWidth, reqHeight);
        } else {
            return onDecodeAssetsGif(applicationContext, context, taskInfo, assetsPath, logger, reqWidth, reqHeight);
        }
    }

    private ImageResource onDecodeAssetsBitmap(Context applicationContext, Context context, Task.Info taskInfo, String assetsPath, TLogger logger, int reqWidth, int reqHeight){
        //decoding
        Bitmap bitmap = BitmapUtils.decodeFromAssets(applicationContext, assetsPath, reqWidth, reqHeight, taskInfo.getParams().getBitmapConfig(), taskInfo.getParams().getDecodeInSampleQuality());
        if (bitmap == null) {
            throw new RuntimeException("[CommonDecodeHandler]decoding failed, illegal image data");
        }
        //scale
        switch (taskInfo.getParams().getDecodeScaleStrategy()){
            case SCALE_FIT_WIDTH_HEIGHT:
                bitmap = BitmapUtils.scaleTo(bitmap, reqWidth, reqHeight, true);
                break;
            case SCALE_FIT_WIDTH:
                bitmap = BitmapUtils.scaleTo(bitmap, reqWidth, 0, true);
                break;
            case SCALE_FIT_HEIGHT:
                bitmap = BitmapUtils.scaleTo(bitmap, 0, reqHeight, true);
                break;
            default:
                break;
        }
        if (bitmap == null) {
            throw new RuntimeException("[CommonDecodeHandler]scale: scale failed");
        }
        if (logger.checkEnable(TLogger.DEBUG)) {
            logger.d("[CommonDecodeHandler]decoded size:" + bitmap.getWidth() + "*" + bitmap.getHeight() + " task:" + taskInfo);
        }
        return new ImageResource(ImageResource.Type.BITMAP, bitmap);
    }

    private ImageResource onDecodeAssetsGif(Context applicationContext, Context context, Task.Info taskInfo, String assetsPath, TLogger logger, int reqWidth, int reqHeight) {
        try {
            return new ImageResource(ImageResource.Type.GIF, EnhancedGifDrawable.decode(applicationContext.getAssets(), assetsPath, reqWidth, reqHeight, taskInfo.getParams().getDecodeInSampleQuality()));
        } catch (IOException e) {
            throw new RuntimeException("[EnhancedDecodeHandler]error while decoding gif from assets", e);
        }
    }

    private boolean isAssetsGif(Context applicationContext, String assetsPath, TLogger logger){
        if (checkGifDrawableReference(logger)) {
            return false;
        }

        InputStream inputStream = null;
        try {
            inputStream = applicationContext.getAssets().open(assetsPath);
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

    //private//////////////////////////////////////////////////////////////////////////////////////

    private boolean checkGifDrawableReference(TLogger logger) {
        if (gifDrawableReferenceState == GIF_DRAWABLE_REFERENCE_STATE_UNKNOWN) {
            try {
                Class.forName("pl.droidsonroids.gif.GifDrawable");
                gifDrawableReferenceState = GIF_DRAWABLE_REFERENCE_STATE_EXISTS;
            } catch (ClassNotFoundException e) {
                gifDrawableReferenceState = GIF_DRAWABLE_REFERENCE_STATE_MISSING;
                logger.e("[EnhancedDecodeHandler]Your project lacks a dependency of pl.droidsonroids.gif:android-gif-drawable:?.?.?, can not display GIF", e);
            }
        }
        if (gifDrawableReferenceState == GIF_DRAWABLE_REFERENCE_STATE_MISSING) {
            //no class def found : GifDrawable
            return true;
        }
        return false;
    }

}
