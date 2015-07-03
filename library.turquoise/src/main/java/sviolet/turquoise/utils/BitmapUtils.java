package sviolet.turquoise.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.TextPaint;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sviolet.turquoise.io.cache.BitmapManager;

/**
 * Bitmap工具<br/>
 *<Br/>
 * Bitmap解码/编辑/转换工具, 不含回收缓存功能, 需要回收功能使用:
 * @see BitmapManager
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
        return decodeFromResource(res, resId, 0, 0);
    }

    /**
     * 从资源文件中解码图片(节省内存)<br/>
     * 根据宽高需求计算出缩放比率, 以整数倍缩放图片, 达到节省内存的效果,
     * 解码出的图片尺寸不等于需求尺寸.
     *
     * @param res       getResource()
     * @param resId     资源文件ID
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public static Bitmap decodeFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//仅计算参数, 不解码
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);//缩放因子(整数倍)
        options.inJustDecodeBounds = false;//解码模式
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 从文件中解码图片
     *
     * @param path 文件路径
     */
    public static Bitmap decodeFromFile(String path) {
        return decodeFromFile(path, 0, 0);
    }

    /**
     * 从文件中解码图片(节省内存)<br/>
     * 根据宽高需求计算出缩放比率, 以整数倍缩放图片, 达到节省内存的效果,
     * 解码出的图片尺寸不等于需求尺寸.
     *
     * @param path      文件路径
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public static Bitmap decodeFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//仅计算参数, 不解码
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);//缩放因子(整数倍)
        options.inJustDecodeBounds = false;//解码模式
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 将二进制数据解码为图片
     *
     * @param data 二进制数据
     */
    public static Bitmap decodeFromByteArray(byte[] data) {
        return decodeFromByteArray(data, 0, 0);
    }

    /**
     * 将二进制数据解码为图片(节省内存)<br/>
     * 根据宽高需求计算出缩放比率, 以整数倍缩放图片, 达到节省内存的效果,
     * 解码出的图片尺寸不等于需求尺寸.
     *
     * @param data      二进制数据
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public static Bitmap decodeFromByteArray(byte[] data, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    /**
     * 将Base64数据解码为图片
     *
     * @param base64 Base64数据
     */
    public static Bitmap decodeFromBase64(byte[] base64) {
        return decodeFromBase64(base64, 0, 0);
    }

    /**
     * 将Base64数据解码为图片(节省内存)<br/>
     * 根据宽高需求计算出缩放比率, 以整数倍缩放图片, 达到节省内存的效果,
     * 解码出的图片尺寸不等于需求尺寸.
     *
     * @param base64    Base64数据
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public static Bitmap decodeFromBase64(byte[] base64, int reqWidth, int reqHeight) {
        return decodeFromByteArray(Base64.decode(base64, Base64.DEFAULT), reqWidth, reqHeight);
    }

    /**
     * 将Base64数据解码为图片
     *
     * @param base64 Base64数据
     */
    public static Bitmap decodeFromBase64(String base64) {
        return decodeFromBase64(base64, 0, 0);
    }

    /**
     * 将Base64数据解码为图片(节省内存)<br/>
     * 根据宽高需求计算出缩放比率, 以整数倍缩放图片, 达到节省内存的效果,
     * 解码出的图片尺寸不等于需求尺寸.
     *
     * @param base64    Base64数据
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public static Bitmap decodeFromBase64(String base64, int reqWidth, int reqHeight) {
        return decodeFromByteArray(Base64.decode(base64, Base64.DEFAULT), reqWidth, reqHeight);
    }

    /**
     * 从输入流中解码图片
     *
     * @param inputStream 输入流
     */
    public static Bitmap decodeFromStream(InputStream inputStream) {
        return decodeFromStream(inputStream, 0, 0);
    }

    /**
     * 从输入流中解码图片(节省内存)<br/>
     * 根据宽高需求计算出缩放比率, 以整数倍缩放图片, 达到节省内存的效果,
     * 解码出的图片尺寸不等于需求尺寸.
     *
     * @param inputStream 输入流
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public static Bitmap decodeFromStream(InputStream inputStream, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//仅计算参数, 不解码
        BitmapFactory.decodeStream(inputStream, null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);//缩放因子(整数倍)
        options.inJustDecodeBounds = false;//解码模式
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
    @SuppressWarnings("deprecation")
    public static BitmapDrawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @param recycle 是否回收源Bitmap
     */
    public static String bitmapToBase64(Bitmap bitmap, boolean recycle) {
        String result = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            if (bitmap != null) {
                byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
                byte[] bitmapBytes = byteArrayOutputStream.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //回收源Bitmap
        if (recycle && bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            System.gc();
        }
        return result;
    }

    /**
     * 按比例缩放图片
     *
     * @param bitmap
     * @param scale   缩放比例
     * @param recycle 是否回收源Bitmap
     */
    public static Bitmap zoom(Bitmap bitmap, float scale, boolean recycle) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        //回收源Bitmap
        if (recycle && !bitmap.isRecycled()) {
            bitmap.recycle();
            System.gc();
        }
        return result;
    }

    /**
     * 按比例缩放图片
     *
     * @param bitmap
     * @param scaleX  x缩放比例
     * @param scaleY  y缩放比例
     * @param recycle 是否回收源Bitmap
     */
    public static Bitmap zoom(Bitmap bitmap, float scaleX, float scaleY, boolean recycle) {
        Matrix matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        //回收源Bitmap
        if (recycle && !bitmap.isRecycled()) {
            bitmap.recycle();
            System.gc();
        }
        return result;
    }

    /**
     * 将图片缩放至指定宽高<Br/>
     *
     * @param bitmap
     * @param width   指定宽 >0
     * @param height  指定高 >0
     * @param recycle 是否回收源Bitmap
     */
    public static Bitmap zoom(Bitmap bitmap, int width, int height, boolean recycle) {
        return zoom(bitmap, (float) width / (float) bitmap.getWidth(), (float) height / (float) bitmap.getHeight(), recycle);
    }

    /**
     * 图片圆角处理
     *
     * @param bitmap
     * @param radius  圆角半径
     * @param recycle 是否回收源Bitmap
     */
    public static Bitmap toRoundedCorner(Bitmap bitmap, float radius, boolean recycle) {
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //回收源Bitmap
        if (recycle && !bitmap.isRecycled()) {
            bitmap.recycle();
            System.gc();
        }
        return result;
    }


    /**
     * (自动cache)从资源文件中解码图片,并绘制文字(根据宽高需求"整数倍"缩放图片,节省内存)
     *
     * @param res       getResource()
     * @param resId     资源ID
     * @param text      需要绘制的文字
     * @param x         文字在X方向的位移
     * @param y         文字在Y方向的位移
     * @param textSize  字体大小
     * @param textColor 字体颜色
     */
    public static Bitmap drawTextOnResource(Resources res, int resId, String text, float x, float y, float textSize, int textColor) {
        Bitmap resBitmap = BitmapFactory.decodeResource(res, resId);
        if (text == null)
            return resBitmap;
        //copy, 防止出现immutable bitmap异常
        Bitmap bitmap = resBitmap.copy(Config.ARGB_8888, true);
        resBitmap.recycle();
        drawText(bitmap, text, x, y, textSize, textColor);
        return bitmap;
    }

    /**
     * 在Bitmap上绘制文字<br/>
     * <br/>
     * immutable bitmap pass to canvas异常解决:<br/>
     * 在绘制前复制: bitmap.copy(Bitmap.Config.ARGB_8888, true);<br/>
     *
     * @param bitmap
     * @param text      绘制的文本
     * @param x         位置
     * @param y         位置
     * @param textSize  字体大小
     * @param textColor 字体颜色
     */
    public static Bitmap drawText(Bitmap bitmap, String text, float x, float y, float textSize, int textColor) {
        Canvas canvas = new Canvas(bitmap);
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        canvas.drawText(text, x, y, textPaint);
        return bitmap;
    }

    /**
     * 保存Bitmap到本地(异步)
     *
     * @param bitmap
     * @param outputStream 输出流
     * @param recycle 是否回收源Bitmap
     * @param onSaveCompleteListener 完成回调
     */
    public static void saveBitmap(final Bitmap bitmap, final OutputStream outputStream, final boolean recycle, final OnSaveCompleteListener onSaveCompleteListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Throwable throwable = null;
                if (outputStream != null && bitmap != null) {
                    try {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
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
                                System.gc();
                            }
                        } catch (IOException ignored) {
                        }
                    }
                    if (onSaveCompleteListener != null) {
                        onSaveCompleteListener.onSaveFailed(throwable);
                    }
                }
            }
        }).start();
    }

    /**
     * 保存Bitmap到本地(异步)
     *
     * @param bitmap
     * @param path 路径
     * @param fileName 文件名
     * @param recycle 是否回收源Bitmap
     * @param onSaveCompleteListener 完成回调
     */
    public static void saveBitmap(final Bitmap bitmap, final String path, final String fileName, final boolean recycle, final OnSaveCompleteListener onSaveCompleteListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OutputStream outputStream = null;
                Throwable throwable = null;
                if (path != null && fileName != null && bitmap != null) {
                    File pathFile = new File(path);
                    if (!pathFile.exists()) {
                        pathFile.mkdir();
                    }
                    File file = new File(path + File.separator + fileName);
                    try {
                        outputStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
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
                                System.gc();
                            }
                        } catch (IOException ignored) {
                        }
                    }
                    if (onSaveCompleteListener != null) {
                        onSaveCompleteListener.onSaveFailed(throwable);
                    }
                }
            }
        }).start();
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
     * 图片保存结束监听
     */
    public interface OnSaveCompleteListener {
        public void onSaveSucceed();

        public void onSaveFailed(Throwable e);
    }

    /***********************************************
     * private
     */

    /**
     * 根据指定的需求宽高计算缩放因子
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        //不缩放的情况
        if (reqWidth <= 0 || reqHeight <= 0) {
            return 1;
        }
        // 源图片宽高
        final int width = options.outWidth;
        final int height = options.outHeight;
        //缩放因子
        int inSampleSize = 1;
        //原图大于需求尺寸时缩放
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;
            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

}
