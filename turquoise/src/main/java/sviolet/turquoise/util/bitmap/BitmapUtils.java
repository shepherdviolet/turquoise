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

package sviolet.turquoise.util.bitmap;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.TextPaint;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Bitmap工具</p>
 *
 * <p>Bitmap解码/编辑/转换工具, 不含回收缓存功能, 需要回收功能使用:{@link CachedBitmapUtils}</p>
 *
 * <p>加载超大图且局部显示请使用BitmapRegionDecoder</p>
 *
 * @author S.Violet
 */

public class BitmapUtils {

    /*********************************************
     * 				解码
     *********************************************/

    /**
     * 从资源文件中解码图片
     *
     * @param res   getResource()
     * @param resId 资源文件ID
     */
    public static Bitmap decodeFromResource(Resources res, int resId) {
        return decodeFromResource(res, resId, 0, 0, Config.ARGB_8888, InSampleQuality.MEDIUM);
    }

    /**
     * 从资源文件中解码图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param res       getResource()
     * @param resId     资源文件ID
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public static Bitmap decodeFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        return decodeFromResource(res, resId, reqWidth, reqHeight, Config.ARGB_8888, InSampleQuality.MEDIUM);
    }

    /**
     * 从资源文件中解码图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param res       getResource()
     * @param resId     资源文件ID
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     * @param bitmapConfig 颜色深度
     * @param quality 图片质量, 默认InSampleQuality.MEDIUM, 若不需要缩小, 设置ORIGINAL
     */
    public static Bitmap decodeFromResource(Resources res, int resId, int reqWidth, int reqHeight, Bitmap.Config bitmapConfig, InSampleQuality quality) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//仅计算参数, 不解码
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight, quality);//缩放因子(整数倍)
        options.inJustDecodeBounds = false;//解码模式
        options.inPreferredConfig = bitmapConfig;//颜色深度
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 从文件中解码图片
     *
     * @param path 文件路径
     */
    public static Bitmap decodeFromFile(String path) {
        return decodeFromFile(path, 0, 0, Config.ARGB_8888, InSampleQuality.MEDIUM);
    }

    /**
     * 从文件中解码图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param path      文件路径
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public static Bitmap decodeFromFile(String path, int reqWidth, int reqHeight) {
        return decodeFromFile(path, reqWidth, reqHeight, Config.ARGB_8888, InSampleQuality.MEDIUM);
    }

    /**
     * 从文件中解码图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param path      文件路径
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     * @param bitmapConfig 颜色深度
     * @param quality 图片质量, 默认InSampleQuality.MEDIUM, 若不需要缩小, 设置ORIGINAL
     */
    public static Bitmap decodeFromFile(String path, int reqWidth, int reqHeight, Bitmap.Config bitmapConfig, InSampleQuality quality) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//仅计算参数, 不解码
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight, quality);//缩放因子(整数倍)
        options.inJustDecodeBounds = false;//解码模式
        options.inPreferredConfig = bitmapConfig;//颜色深度
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 将二进制数据解码为图片
     *
     * @param data 二进制数据
     */
    public static Bitmap decodeFromByteArray(byte[] data) {
        return decodeFromByteArray(data, 0, 0, Config.ARGB_8888, InSampleQuality.MEDIUM);
    }

    /**
     * 将二进制数据解码为图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param data      二进制数据
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public static Bitmap decodeFromByteArray(byte[] data, int reqWidth, int reqHeight) {
        return decodeFromByteArray(data, reqWidth, reqHeight, Config.ARGB_8888, InSampleQuality.MEDIUM);
    }

    /**
     * 将二进制数据解码为图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param data      二进制数据
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     * @param bitmapConfig 颜色深度
     * @param quality 图片质量, 默认InSampleQuality.MEDIUM, 若不需要缩小, 设置ORIGINAL
     */
    public static Bitmap decodeFromByteArray(byte[] data, int reqWidth, int reqHeight, Bitmap.Config bitmapConfig, InSampleQuality quality) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight, quality);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = bitmapConfig;//颜色深度
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    /**
     * 将Base64数据解码为图片
     *
     * @param base64 Base64数据
     */
    public static Bitmap decodeFromBase64(byte[] base64) {
        return decodeFromBase64(base64, 0, 0, Config.ARGB_8888, InSampleQuality.MEDIUM);
    }

    /**
     * 将Base64数据解码为图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param base64    Base64数据
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public static Bitmap decodeFromBase64(byte[] base64, int reqWidth, int reqHeight) {
        return decodeFromBase64(base64, reqWidth, reqHeight, Config.ARGB_8888, InSampleQuality.MEDIUM);
    }

    /**
     * 将Base64数据解码为图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param base64    Base64数据
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     * @param bitmapConfig 颜色深度
     * @param quality 图片质量, 默认InSampleQuality.MEDIUM, 若不需要缩小, 设置ORIGINAL
     */
    public static Bitmap decodeFromBase64(byte[] base64, int reqWidth, int reqHeight, Bitmap.Config bitmapConfig, InSampleQuality quality) {
        return decodeFromByteArray(Base64.decode(base64, Base64.DEFAULT), reqWidth, reqHeight, bitmapConfig, quality);
    }

    /**
     * 将Base64数据解码为图片
     *
     * @param base64 Base64数据
     */
    public static Bitmap decodeFromBase64(String base64) {
        return decodeFromBase64(base64, 0, 0, Config.ARGB_8888, InSampleQuality.MEDIUM);
    }

    /**
     * 将Base64数据解码为图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param base64    Base64数据
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public static Bitmap decodeFromBase64(String base64, int reqWidth, int reqHeight) {
        return decodeFromBase64(base64, reqWidth, reqHeight, Config.ARGB_8888, InSampleQuality.MEDIUM);
    }

    /**
     * 将Base64数据解码为图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param base64    Base64数据
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     * @param bitmapConfig 颜色深度
     * @param quality 图片质量, 默认InSampleQuality.MEDIUM, 若不需要缩小, 设置ORIGINAL
     */
    public static Bitmap decodeFromBase64(String base64, int reqWidth, int reqHeight, Bitmap.Config bitmapConfig, InSampleQuality quality) {
        return decodeFromByteArray(Base64.decode(base64, Base64.DEFAULT), reqWidth, reqHeight, bitmapConfig, quality);
    }

    /**
     * 从输入流中解码图片
     *
     * @param inputStream 输入流
     */
    public static Bitmap decodeFromStream(InputStream inputStream) {
        return decodeFromStream(inputStream, 1, Config.ARGB_8888);
    }

    /**
     * 从输入流中解码图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     * <br/>
     * InputStream只能使用一次, 因此不能通过第一次解码获得图片长宽,
     * 计算缩放因子, 再解码获得图片这种方式<br/>
     *
     * @param inputStream 输入流
     * @param inSampleSize 缩放因子 (1:原大小 2:缩小一倍 ...)
     */
    public static Bitmap decodeFromStream(InputStream inputStream, int inSampleSize) {
        return decodeFromStream(inputStream, inSampleSize, Config.ARGB_8888);
    }

    /**
     * 从输入流中解码图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     * <br/>
     * InputStream只能使用一次, 因此不能通过第一次解码获得图片长宽,
     * 计算缩放因子, 再解码获得图片这种方式<br/>
     *
     * @param inputStream 输入流
     * @param inSampleSize 缩放因子 (1:原大小 2:缩小一倍 ...)
     * @param bitmapConfig 颜色深度
     */
    public static Bitmap decodeFromStream(InputStream inputStream, int inSampleSize, Bitmap.Config bitmapConfig) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;//缩放因子(整数倍)
        options.inJustDecodeBounds = false;//解码模式
        options.inPreferredConfig = bitmapConfig;//颜色深度
        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    /*********************************************
     * 				转换/编辑
     *********************************************/

    /**
     * bitmap转成drawable
     *
     * @param bitmap
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static BitmapDrawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    /**
     * bitmap转成drawable
     *
     * @param bitmap
     */
    public static BitmapDrawable bitmapToDrawable(Resources resources, Bitmap bitmap) {
        return new BitmapDrawable(resources, bitmap);
    }

    /**
     * bitmap转为byteArray
     *
     * @param bitmap 原图
     * @param format 图片转码格式
     * @param quality 图片转码质量(0, 100]
     * @param recycle 是否回收源Bitmap
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap, Bitmap.CompressFormat format, int quality, boolean recycle) throws IOException {
        if (bitmap == null || bitmap.isRecycled()){
            throw new NullPointerException("[BitmapUtils]bitmap is null or recycled");
        }

        byte[] bitmapBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(format, quality, byteArrayOutputStream);
            byteArrayOutputStream.flush();
            bitmapBytes = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
            }
        }

        //回收源Bitmap
        if (recycle && !bitmap.isRecycled()) {
            bitmap.recycle();
        }

        return bitmapBytes;
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap 原图
     * @param format 图片转码格式
     * @param quality 图片转码质量(0, 100]
     * @param recycle 是否回收源Bitmap
     */
    public static String bitmapToBase64(Bitmap bitmap, Bitmap.CompressFormat format, int quality, boolean recycle) throws IOException {
        byte[] byteArray = bitmapToByteArray(bitmap, format, quality, recycle);
        if (byteArray == null || byteArray.length <= 0){
            return "";
        }
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * 按比例缩放图片
     *
     * @param bitmap 原图
     * @param scale   缩放比例
     * @param recycle 是否回收源Bitmap
     */
    public static Bitmap scale(Bitmap bitmap, float scale, boolean recycle) {
        return scale(bitmap, scale, scale, recycle);
    }

    /**
     * 按比例缩放图片
     *
     * @param bitmap 原图
     * @param scaleX  x缩放比例
     * @param scaleY  y缩放比例
     * @param recycle 是否回收源Bitmap
     */
    public static Bitmap scale(Bitmap bitmap, float scaleX, float scaleY, boolean recycle) {
        if (bitmap == null || bitmap.isRecycled()){
            throw new NullPointerException("[BitmapUtils]bitmap is null or recycled");
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        //回收源Bitmap
        if (bitmap != result && recycle && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return result;
    }

    /**
     * 将图片缩放至指定宽高<p/>
     *
     * <pre>{@code
     * width>0 & height>0   : 宽高分别缩放到指定值<br/>
     * width>0 & height<=0  : 宽缩放到指定值,高同比例缩放,保持宽高比<br/>
     * width<=0 & height>0  : 高缩放到指定值,宽同比例缩放,保持宽高比<br/>
     * width<=0 & height<=0 : 返回原图<br/>
     * }</pre>
     * @param bitmap 原图
     * @param width   指定宽
     * @param height  指定高
     * @param recycle 是否回收源Bitmap
     */
    public static Bitmap scaleTo(Bitmap bitmap, int width, int height, boolean recycle) {

        float scaleX;
        float scaleY;

        if (width <= 0 && height <= 0){
            //返回原图
            return bitmap;
        }else if(width > 0 && height <= 0){
            //宽度缩放, 高度等比例缩放
            scaleX = (float) width / (float) bitmap.getWidth();
            scaleY = scaleX;
        }else if (width <= 0 && height > 0){
            //高度缩放, 宽度等比例缩放
            scaleY = (float) height / (float) bitmap.getHeight();
            scaleX = scaleY;
        }else{
            //不同比例缩放
            scaleX = (float) width / (float) bitmap.getWidth();
            scaleY = (float) height / (float) bitmap.getHeight();
        }

        return scale(bitmap, scaleX, scaleY, recycle);
    }

    /**
     * 图片圆角处理
     *
     * @param bitmap 原图
     * @param radius  圆角半径
     * @param type BitmapUtils.RoundedCornerType 指定哪些角需要圆角处理
     * @param recycle 是否回收源Bitmap
     */
    public static Bitmap toRoundedCorner(Bitmap bitmap, float radius, RoundedCornerType type, boolean recycle) {
        if (bitmap == null || bitmap.isRecycled()){
            throw new NullPointerException("[BitmapUtils]bitmap is null or recycled");
        }
        Config config = Config.ARGB_8888;
        if (bitmap.getConfig() != null)
            config = bitmap.getConfig();
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
        Canvas canvas = new Canvas(result);
        final int color = 0xff424242;
        final Paint paint = new Paint();
//        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());//四个角全部圆角处理
        final Rect rect = new Rect(
                - bitmap.getWidth() * matchBinaryFlag(type.value(), 0x1000),
                - bitmap.getHeight() * matchBinaryFlag(type.value(), 0x0100),
                bitmap.getWidth() + bitmap.getWidth() * matchBinaryFlag(type.value(), 0x0010),
                bitmap.getHeight() + bitmap.getHeight() * matchBinaryFlag(type.value(), 0x0001));
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //回收源Bitmap
        if (bitmap != result && recycle && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return result;
    }


    /**
     * 从资源文件中解码图片,并绘制文字
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param res       getResource()
     * @param resId     资源ID
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     * @param text      需要绘制的文字
     * @param x         文字在X方向的位移
     * @param y         文字在Y方向的位移
     * @param textSize  字体大小
     * @param textColor 字体颜色
     */
    public static Bitmap drawTextOnResource(Resources res, int resId, int reqWidth, int reqHeight, String text, float x, float y, float textSize, int textColor) {
        Bitmap bitmap = decodeFromResource(res, resId, reqWidth, reqHeight);
        if (text == null)
            return bitmap;
        return drawText(bitmap, text, x, y, textSize, textColor, true);
    }

    /**
     * 在Bitmap上绘制文字<br/>
     *
     * @param bitmap
     * @param text      绘制的文本
     * @param x         位置
     * @param y         位置
     * @param textSize  字体大小
     * @param textColor 字体颜色
     * @param recycle 是否回收源Bitmap
     */
    public static Bitmap drawText(Bitmap bitmap, String text, float x, float y, float textSize, int textColor, boolean recycle) {
        if (bitmap == null || bitmap.isRecycled()){
            throw new NullPointerException("[BitmapUtils]bitmap is null or recycled");
        }
        //copy, 防止出现immutable bitmap异常
        Config config = Config.ARGB_8888;
        if (bitmap.getConfig() != null)
            config = bitmap.getConfig();
        Bitmap result = bitmap.copy(config, true);
        if (bitmap != result && recycle)
            bitmap.recycle();

        Canvas canvas = new Canvas(result);
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        canvas.drawText(text, x, y, textPaint);
        return result;
    }

    /**
     * 复制Bitmap
     * @param bitmap 源bitmap
     * @param recycle 是否回收源Bitmap
     * @return 新Bitmap
     */
    public static Bitmap copy(Bitmap bitmap, boolean recycle){
        if (bitmap == null || bitmap.isRecycled()){
            return null;
        }
        Config config = Config.ARGB_8888;
        if (bitmap.getConfig() != null) {
            config = bitmap.getConfig();
        }
        Bitmap result = bitmap.copy(config, true);
        if (bitmap != result && recycle) {
            bitmap.recycle();
        }
        return result;
    }

    /**
     * 保存Bitmap到本地(异步)
     *
     * @param bitmap
     * @param outputStream 输出流
     * @param format 图片保存格式
     * @param quality 图片保存质量(0, 100]
     * @param recycle 是否回收源Bitmap
     * @param onSaveCompleteListener 完成回调
     */
    public static void saveBitmap(final Bitmap bitmap, final OutputStream outputStream, final Bitmap.CompressFormat format, final int quality, final boolean recycle, final OnSaveCompleteListener onSaveCompleteListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncSaveBitmap(bitmap, outputStream, format, quality, recycle, onSaveCompleteListener);
            }
        }).start();
    }

    /**
     * 保存Bitmap到本地(同步, 会阻塞线程)
     *
     * @param bitmap
     * @param outputStream 输出流
     * @param format 图片保存格式
     * @param quality 图片保存质量(0, 100]
     * @param recycle 是否回收源Bitmap
     * @param onSaveCompleteListener 完成回调
     */
    public static void syncSaveBitmap(Bitmap bitmap, OutputStream outputStream, Bitmap.CompressFormat format, int quality, boolean recycle, OnSaveCompleteListener onSaveCompleteListener) {
        if (bitmap == null || bitmap.isRecycled()){
            throw new NullPointerException("[BitmapUtils]bitmap is null or recycled");
        }
        if (outputStream == null){
            throw new NullPointerException("[BitmapUtils]outputStream is null");
        }
        Throwable throwable = null;
        try {
            bitmap.compress(format, quality, outputStream);
            outputStream.flush();
            if (onSaveCompleteListener != null) {
                onSaveCompleteListener.onSaveSucceed();
            }
            return;
        } catch (IOException e) {
            throwable = e;
        } finally {
            try {
                outputStream.close();
                //回收源Bitmap
                if (recycle && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (IOException ignored) {
            }
        }
        if (onSaveCompleteListener != null) {
            onSaveCompleteListener.onSaveFailed(throwable);
        }
    }

    /**
     * 保存Bitmap到本地(异步)
     *
     * @param bitmap
     * @param path 路径
     * @param fileName 文件名
     * @param format 图片保存格式
     * @param quality 图片保存质量(0, 100]
     * @param recycle 是否回收源Bitmap
     * @param onSaveCompleteListener 完成回调
     */
    public static void saveBitmap(final Bitmap bitmap, final String path, final String fileName, final Bitmap.CompressFormat format, final int quality, final boolean recycle, final OnSaveCompleteListener onSaveCompleteListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncSaveBitmap(bitmap, path, fileName, format, quality, recycle, onSaveCompleteListener);
            }
        }).start();
    }

    /**
     * 保存Bitmap到本地(同步, 会阻塞线程)
     *
     * @param bitmap
     * @param path 路径
     * @param fileName 文件名
     * @param format 图片保存格式
     * @param quality 图片保存质量(0, 100]
     * @param recycle 是否回收源Bitmap
     * @param onSaveCompleteListener 完成回调
     */
    public static void syncSaveBitmap(Bitmap bitmap, String path, String fileName, Bitmap.CompressFormat format, int quality, boolean recycle, OnSaveCompleteListener onSaveCompleteListener) {
        if (bitmap == null || bitmap.isRecycled()){
            throw new NullPointerException("[BitmapUtils]bitmap is null or recycled");
        }
        if (path == null && fileName == null){
            throw new NullPointerException("[BitmapUtils]path and fileName are null");
        }
        OutputStream outputStream = null;
        Throwable throwable = null;
        File pathFile = new File(path);
        if (!pathFile.exists()) {
            pathFile.mkdir();
        }
        File file = new File(path + File.separator + fileName);
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(format, quality, outputStream);
            outputStream.flush();
            if (onSaveCompleteListener != null) {
                onSaveCompleteListener.onSaveSucceed();
            }
            return;
        } catch (IOException e) {
            throwable = e;
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                //回收源Bitmap
                if (recycle && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (IOException ignored) {
            }
        }
        if (onSaveCompleteListener != null) {
            onSaveCompleteListener.onSaveFailed(throwable);
        }
    }

    /**
     * 通知相册刷新(主线程)
     *
     * @param context
     * @param path 路径
     * @param fileName 文件名
     */
    public static void notifyPhotoAlbum(Context context, String path, String fileName){
        File file = new File(path + File.separator + fileName);
        notifyPhotoAlbum(context, file);
    }

    /**
     * 通知相册刷新(主线程)
     *
     * @param context
     * @param file 图片文件
     */
    public static void notifyPhotoAlbum(Context context, File file){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    /**
     * 获取View的显示缓存图
     * @param view view
     * @param bitmapConfig 颜色深度
     */
    public static Bitmap getViewDrawingCache(View view, Bitmap.Config bitmapConfig){
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        Bitmap result = bitmap.copy(bitmapConfig, true);
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        return result;
    }

    /**
     * 获得WebView整个页面的图像(包括未显示部分), 图片会比较大, 因此建议设置宽度高度限制, 若超过限制, 图片会
     * 进行缩小.
     * @param webView webView
     * @param bitmapConfig Bitmap参数
     * @param limitWidth 限制宽度(0不限)
     * @param limitHeight 限制高度(0不限)
     */
    public static Bitmap getWebViewCapturePicture(WebView webView, Bitmap.Config bitmapConfig, int limitWidth, int limitHeight){
        Picture picture = webView.capturePicture();
        float scale = 1f;
        if (limitWidth > 0 && picture.getWidth() > limitWidth){
            scale = (float)limitWidth / (float)picture.getWidth();
        }
        if (limitHeight > 0 && picture.getHeight() > limitHeight){
            float heightScale = (float)limitHeight / (float)picture.getHeight();
            if (heightScale < scale){
                scale = heightScale;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap((int)(picture.getWidth() * scale), (int)(picture.getHeight() * scale), bitmapConfig);
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Canvas canvas = new Canvas(bitmap);
        canvas.setMatrix(matrix);
        picture.draw(canvas);
        return bitmap;
    }

    /**********************************************
     * interface
     */

    /**
     * 图片保存结束监听
     */
    public interface OnSaveCompleteListener {
        void onSaveSucceed();

        void onSaveFailed(Throwable e);
    }

    /***********************************************
     * in sample size
     */

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight, InSampleQuality quality) {
        return calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight, quality);
    }

    /**
     * 根据需求尺寸, 计算图片解码采样率, 用于适当缩小图片节省内存, 防止OOM.
     * @param originWidth 原图宽度
     * @param originHeight 原图高度
     * @param reqWidth 需求宽度
     * @param reqHeight 需求高度
     * @param quality 图片质量, 默认InSampleQuality.MEDIUM, 若不需要缩小, 设置ORIGINAL
     * @return 采样率
     */
    public static int calculateInSampleSize(int originWidth, int originHeight, int reqWidth, int reqHeight, InSampleQuality quality){
        if (quality == InSampleQuality.ORIGINAL || originWidth <= 0 || originHeight <= 0){
            return 1;
        }
        final int widthRatio = calculateInSampleRatio(originWidth, reqWidth, quality);
        final int heightRatio = calculateInSampleRatio(originHeight, reqHeight, quality);
        int inSampleSize = widthRatio < heightRatio ? widthRatio : heightRatio;
        if (inSampleSize == Integer.MAX_VALUE){
            return 1;
        }
        final float maxPixels = reqWidth * reqHeight * calculateInSampleMaxPixelsMultiple(quality);
        if (maxPixels <= 0){
            return inSampleSize;
        }
        final float originalPixels = originWidth * originHeight;
        while (originalPixels / (inSampleSize * inSampleSize) > maxPixels) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    private static float calculateInSampleMaxPixelsMultiple(InSampleQuality quality){
        switch (quality){
            case HIGH:
                return 3f;
            case LOW:
                return 1.5f;
            case MEDIUM:
            default:
                return 2f;
        }
    }

    private static int calculateInSampleRatio(int origin, int req, InSampleQuality quality){
        if (req <= 0){
            return Integer.MAX_VALUE;
        }
        int ratio;
        switch (quality){
            case HIGH:
                ratio = (int) Math.floor((float)origin / (float)req);
                break;
            case LOW:
                ratio = (int) Math.ceil((float)origin / (float)req);
                break;
            case MEDIUM:
            default:
                ratio = Math.round((float)origin / (float)req);
                break;
        }
        //minimum
        if (ratio < 1){
            ratio = 1;
        }
        return ratio;
    }

    /**
     * 判断input是否符合flag
     * @return 1:符合 0:不符
     */
    private static int matchBinaryFlag(int input, int flag){
        return input == (input | flag) ? 1 : 0;
    }

    /***********************************************
     * inner class
     */

    /**
     * 圆角处理类型,
     * 用于指定哪些角做圆角处理.
     */
    public enum RoundedCornerType{
        All(0x0000),//四个角全部圆角处理
        TopLeft_And_TopRight(0x0001),//上面两个角圆角处理
        BottomLeft_And_BottomRight(0x0100),//下面两个角圆角处理
        TopLeft_And_BottomLeft(0x0010),//左边两个角圆角处理
        TopRight_And_BottomRight(0x1000),//右边两个角圆角处理
        TopLeft(0x0011),//左上角圆角处理
        TopRight(0x1001),//右上角圆角处理
        BottomRight(0x1100),//右下角圆角处理
        BottomLeft(0x0110);//左下角圆角处理

        private int params;

        RoundedCornerType(int params){
            this.params = params;
        }

        public int value() {
            return params;
        }
    }

    /**
     * <p>解码采样质量</p>
     *
     * <p>ORIGINAL::原图</p>
     * <p>HIGH::高质量(缩小)</p>
     * <p>MEDIUM::中质量(缩小), 建议值</p>
     * <p>LOW::低质量(缩小)</p>
     *
     * <p>Decode inSample quality</p>
     *
     * <p>ORIGINAL::original size</p>
     * <p>HIGH::high quality</p>
     * <p>MEDIUM::medium quality, by default</p>
     * <p>LOW::low quality</p>
     */
    public enum InSampleQuality{
        ORIGINAL,
        HIGH,
        MEDIUM,
        LOW
    }

}
