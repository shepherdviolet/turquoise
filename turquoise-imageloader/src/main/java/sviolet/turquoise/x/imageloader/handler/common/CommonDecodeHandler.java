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
import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.EnhancedGifDrawable;
import pl.droidsonroids.gif.GifDrawable;
import sviolet.turquoise.util.bitmap.BitmapUtils;
import sviolet.turquoise.util.bitmap.ZxingUtils;
import sviolet.turquoise.util.judge.GifInspectUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>common implementation of DecodeHandler</p>
 *
 * @author S.Violet
 */
public class CommonDecodeHandler extends DecodeHandler {

    private static final int GIF_DRAWABLE_REFERENCE_STATE_UNKNOWN = 0;
    private static final int GIF_DRAWABLE_REFERENCE_STATE_MISSING = -1;
    private static final int GIF_DRAWABLE_REFERENCE_STATE_EXISTS = 1;

    private int gifDrawableReferenceState = 0;

    private static final int ZXING_REFERENCE_STATE_UNKNOWN = 0;
    private static final int ZXING_REFERENCE_STATE_MISSING = -1;
    private static final int ZXING_REFERENCE_STATE_EXISTS = 1;

    private int zxingReferenceState = 0;

    //decode//////////////////////////////////////////////////////////////////////////

    @Override
    public ImageResource onDecode(Context applicationContext, Context context, Task.Info taskInfo, DecodeType decodeType, Object data, TLogger logger) {
        int reqWidth = taskInfo.getParams().getExtraInteger(DecodeHandler.EXTRA_CUSTOM_REQ_WIDTH, taskInfo.getParams().getReqWidth());
        int reqHeight = taskInfo.getParams().getExtraInteger(DecodeHandler.EXTRA_CUSTOM_REQ_HEIGHT, taskInfo.getParams().getReqHeight());
        float reqDimensionZoom = taskInfo.getParams().getExtraFloat(DecodeHandler.EXTRA_REQ_DIMENSION_ZOOM, 1f);
        if (reqDimensionZoom != 1 && reqWidth > 0 && reqHeight > 0){
            reqWidth = (int) ((float)reqWidth * reqDimensionZoom);
            reqHeight = (int) ((float)reqHeight * reqDimensionZoom);
        }
        return distinguishDataType(applicationContext, context, taskInfo, decodeType, data, logger, reqWidth, reqHeight);
    }

    private ImageResource distinguishDataType(Context applicationContext, Context context, Task.Info taskInfo, DecodeType decodeType, Object data, TLogger logger, int reqWidth, int reqHeight) {
        if (!isGif(applicationContext, decodeType, data, logger)){
            //bitmap
            return handleBitmap(applicationContext, context, taskInfo, decodeType, data, logger, reqWidth, reqHeight);
        } else {
            //gif
            return handleGif(applicationContext, context, taskInfo, decodeType, data, logger, reqWidth, reqHeight);
        }
    }

    //is gif/////////////////////////////////////////////////////////////////////////

    private boolean isGif(Context applicationContext, DecodeType decodeType, Object data, TLogger logger){
        if (checkGifDrawableReference(logger)) {
            return false;
        }
        switch (decodeType) {
            case IMAGE_BYTES:
                return GifInspectUtils.isGif((byte[]) data);
            case IMAGE_FILE:
                return GifInspectUtils.isGif((File) data);
            case IMAGE_RES:
                return GifInspectUtils.isGif(applicationContext.getResources(), (int) data);
            case IMAGE_ASSETS:
                return GifInspectUtils.isGif(applicationContext.getAssets(), (String) data);
            case QR_CODE:
                return false;
            default:
                throw new RuntimeException("[CommonDecodeHandler]Unsupported decodeType:" + decodeType);
        }
    }

    //check reference////////////////////////////////////////////////////////////////////////////

    private boolean checkGifDrawableReference(TLogger logger) {
        if (gifDrawableReferenceState == GIF_DRAWABLE_REFERENCE_STATE_UNKNOWN) {
            try {
                Class.forName("pl.droidsonroids.gif.GifDrawable");
                gifDrawableReferenceState = GIF_DRAWABLE_REFERENCE_STATE_EXISTS;
            } catch (ClassNotFoundException e) {
                gifDrawableReferenceState = GIF_DRAWABLE_REFERENCE_STATE_MISSING;
                logger.e("[CommonDecodeHandler]Your project lacks a dependency of pl.droidsonroids.gif:android-gif-drawable:?.?.?, can not display GIF", e);
            }
        }
        if (gifDrawableReferenceState == GIF_DRAWABLE_REFERENCE_STATE_MISSING) {
            //no class def found : GifDrawable
            return true;
        }
        return false;
    }

    private boolean checkZxingReference(TLogger logger) {
        if (zxingReferenceState == ZXING_REFERENCE_STATE_UNKNOWN) {
            try {
                Class.forName("com.google.zxing.qrcode.decoder.ErrorCorrectionLevel");
                Class.forName("com.google.zxing.MultiFormatWriter");
                zxingReferenceState = ZXING_REFERENCE_STATE_EXISTS;
            } catch (ClassNotFoundException e) {
                zxingReferenceState = ZXING_REFERENCE_STATE_MISSING;
                logger.e("[CommonDecodeHandler]Your project lacks a dependency of com.google.zxing:core:?.?.?, can not generate qr-code", e);
            }
        }
        if (zxingReferenceState == ZXING_REFERENCE_STATE_MISSING) {
            return true;
        }
        return false;
    }

    //decode bitmap///////////////////////////////////////////////////////////////////////////

    private ImageResource handleBitmap(Context applicationContext, Context context, Task.Info taskInfo, DecodeType decodeType, Object data, TLogger logger, int reqWidth, int reqHeight) {
        //decoding
        Bitmap bitmap = decodeBitmap(applicationContext, taskInfo, decodeType, data, logger, reqWidth, reqHeight, taskInfo.getParams().getBitmapConfig(), taskInfo.getParams().getDecodeInSampleQuality());
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

    private Bitmap decodeBitmap(Context applicationContext, Task.Info taskInfo, DecodeType decodeType, Object data, TLogger logger, int reqWidth, int reqHeight, Bitmap.Config bitmapConfig, BitmapUtils.InSampleQuality quality){
        switch (decodeType) {
            case IMAGE_BYTES:
                return BitmapUtils.decodeFromByteArray((byte[]) data, reqWidth, reqHeight, bitmapConfig, quality);
            case IMAGE_FILE:
                return BitmapUtils.decodeFromFile(((File) data).getAbsolutePath(), reqWidth, reqHeight, bitmapConfig, quality);
            case IMAGE_RES:
                return BitmapUtils.decodeFromResource(applicationContext.getResources(), (int) data, reqWidth, reqHeight, bitmapConfig, quality);
            case IMAGE_ASSETS:
                return BitmapUtils.decodeFromAssets(applicationContext.getAssets(), (String) data, reqWidth, reqHeight, bitmapConfig, quality);
            case QR_CODE:
                return generateQrCode(applicationContext, taskInfo, (String)data, logger, reqWidth, reqHeight, bitmapConfig, quality);
            default:
                throw new RuntimeException("[CommonDecodeHandler]Unsupported decodeType for bitmap:" + decodeType);
        }
    }

    private Bitmap generateQrCode(Context applicationContext, Task.Info taskInfo, String data, TLogger logger, int reqWidth, int reqHeight, Bitmap.Config bitmapConfig, BitmapUtils.InSampleQuality quality) {
        if (checkZxingReference(logger)){
            return null;
        }
        //extras
        String charset = taskInfo.getParams().getExtraString(Params.EXTRA_URL_TO_QR_CODE_CHARSET, "utf-8");
        int margin = taskInfo.getParams().getExtraInteger(Params.EXTRA_URL_TO_QR_CODE_MARGIN, 1);
        Object correctionLevel = taskInfo.getParams().getExtra(Params.EXTRA_URL_TO_QR_CODE_CORRECTION_LEVEL);
        if (!(correctionLevel instanceof ZxingUtils.CorrectionLevel)){
            correctionLevel = ZxingUtils.CorrectionLevel.M;
        }
        //generate
        try {
            return ZxingUtils.generateQrCode(data, reqWidth, reqHeight, margin, charset, (ZxingUtils.CorrectionLevel) correctionLevel);
        } catch (ZxingUtils.QrCodeGenerateException e) {
            throw new RuntimeException("Error while generating qr-code bitmap from url, url:" + String.valueOf(data), e);
        }
    }

    //decode gif//////////////////////////////////////////////////////////////////////////////////

    private ImageResource handleGif(Context applicationContext, Context context, Task.Info taskInfo, DecodeType decodeType, Object data, TLogger logger, int reqWidth, int reqHeight) {
        try {
            return new ImageResource(ImageResource.Type.GIF, decodeGif(applicationContext, taskInfo, decodeType, data, logger, reqWidth, reqHeight, taskInfo.getParams().getDecodeInSampleQuality()));
        } catch (IOException e) {
            throw new RuntimeException("[EnhancedDecodeHandler]error while decoding gif from bytes", e);
        }
    }

    private GifDrawable decodeGif(Context applicationContext, Task.Info taskInfo, DecodeType decodeType, Object data, TLogger logger, int reqWidth, int reqHeight, BitmapUtils.InSampleQuality quality) throws IOException {
        switch (decodeType) {
            case IMAGE_BYTES:
                return EnhancedGifDrawable.decode((byte[]) data, reqWidth, reqHeight, quality);
            case IMAGE_FILE:
                return EnhancedGifDrawable.decode((File) data, reqWidth, reqHeight, quality);
            case IMAGE_RES:
                return EnhancedGifDrawable.decode(applicationContext.getResources(), (int) data, reqWidth, reqHeight, quality);
            case IMAGE_ASSETS:
                return EnhancedGifDrawable.decode(applicationContext.getAssets(), (String) data, reqWidth, reqHeight, quality);
            default:
                throw new RuntimeException("[CommonDecodeHandler]Unsupported decodeType for gif:" + decodeType);
        }
    }
}
