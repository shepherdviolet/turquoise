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

package sviolet.turquoise.util.bitmap;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

/**
 * Zxing工具
 *
 * @author S.Violet
 */
public class ZxingUtils {

    /**
     * 生成二维码图片
     * @param contents 数据(注意限定, 不要太大, 否则无法绘制)
     * @param width 二维码宽度(注意限定, 不要太大, 否则会内存溢出)
     * @param height 二维码高度(注意限定, 不要太大, 否则会内存溢出)
     * @param margin 边距
     * @param bitmapConfig 输出的Bitmap格式, 推荐RGB_565, 节省内存
     * @param charset 字符集
     * @param correctionLevel 纠错级别
     * @return Bitmap
     * @throws QrCodeGenerateException 生成异常
     */
    public static Bitmap generateQrCode(String contents, int width, int height, int margin, Bitmap.Config bitmapConfig, String charset, CorrectionLevel correctionLevel) throws QrCodeGenerateException {
        ErrorCorrectionLevel errorCorrectionLevel;
        switch (correctionLevel) {
            case L:
                errorCorrectionLevel = ErrorCorrectionLevel.L;
                break;
            case Q:
                errorCorrectionLevel = ErrorCorrectionLevel.Q;
                break;
            case H:
                errorCorrectionLevel = ErrorCorrectionLevel.H;
                break;
            case M:
            default:
                errorCorrectionLevel = ErrorCorrectionLevel.M;
        }

        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
        hints.put(EncodeHintType.CHARACTER_SET, charset);
        hints.put(EncodeHintType.MARGIN, margin);

        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    contents,
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints);

            width = bitMatrix.getWidth();
            height = bitMatrix.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0x00FFFFFF);
                }
            }
            return bitmap;
        } catch (Throwable t) {
            throw new QrCodeGenerateException("Error while generating qr-code bitmap", t);
        }
    }

    public static class QrCodeGenerateException extends Exception {
        public QrCodeGenerateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public enum CorrectionLevel {
        /** L = ~7% correction */
        L,
        /** M = ~15% correction */
        M,
        /** Q = ~25% correction */
        Q,
        /** H = ~30% correction */
        H
    }

}
