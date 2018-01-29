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
import com.google.zxing.WriterException;
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
     * @param charset 字符集
     * @param errorCorrectionLevel 纠错级别
     * @return Bitmap
     * @throws WriterException 生成异常
     */
    public static Bitmap generateQrCode(String contents, int width, int height, int margin, String charset, ErrorCorrectionLevel errorCorrectionLevel) throws WriterException {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
        hints.put(EncodeHintType.CHARACTER_SET, charset);
        hints.put(EncodeHintType.MARGIN, margin);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                contents,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints);

        width = bitMatrix.getWidth();
        height = bitMatrix.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bitmap;
    }

}
