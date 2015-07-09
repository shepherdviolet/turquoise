package sviolet.turquoise.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import java.io.InputStream;

import sviolet.turquoise.io.cache.BitmapCache;
import sviolet.turquoise.utils.BitmapUtils;

/**
 * 内置内存缓存的Bitmap工具<br/>
 * <br/>
 * 内置BitmapCache图片内存缓存器:<br/>
 * @see sviolet.turquoise.io.cache.BitmapCache
 * 调用Bitmap处理工具类:<br/>
 * @see sviolet.turquoise.utils.BitmapUtils
 * <br/>
 * 1.在解码/转换/编辑时, 若管理器中已存在同名(key)的Bitmap, 程序会先回收掉原有的Bitmap<br/>
 * 2.所有转换/编辑方法都会回收源Bitmap(与BitmapUtils可选不同)<br/>
 * 3.输入key=null时, 会分配Bitmap一个默认的不重复的标签<Br/>
 *
 * Created by S.Violet on 2015/7/1.
 */
public class CachedBitmapUtils {

    private static final String DEFAULT_KEY_PREFIX = "DEFAULT_KEY_PREFIX";//默认标签前缀

    private int defaultKeyIndex = 0;//默认标签编号

    private BitmapCache mBitmapCache;

    /**
     * 创建缓存实例<Br/>
     * 根据实际情况设置缓存占比, 参考值0.125, 不超过0.5<Br/>
     * <Br/>
     * BitmapCache内存占用最大值为设置值(maxSize)的两倍, 即缓存区占用maxSize, 回收
     * 站占用maxSize, 回收站内存占用超过maxSize会报异常.<br/>
     *
     * @param context
     * @param percent Bitmap缓存区占用应用可用内存的百分比 (0, 0.5)
     * @return
     */
    public CachedBitmapUtils(Context context, float percent){
        mBitmapCache = BitmapCache.newInstance(context, percent);
    }

    /**
     * 创建缓存实例<Br/>
     * 缓存最大值为默认值DEFAULT_CACHE_MEMORY_PERCENT = 0.125f<Br/>
     * <Br/>
     * BitmapCache内存占用最大值为设置值(maxSize)的两倍, 即缓存区占用maxSize, 回收
     * 站占用maxSize, 回收站内存占用超过maxSize会报异常.<br/>
     *
     * @param context
     * @return
     */
    public CachedBitmapUtils(Context context){
        mBitmapCache = BitmapCache.newInstance(context);
    }

    /**
     * 创建缓存实例<Br/>
     * 根据实际情况设置缓冲区占用最大内存<br/>
     * <Br/>
     * BitmapCache内存占用最大值为设置值(maxSize)的两倍, 即缓存区占用maxSize, 回收
     * 站占用maxSize, 回收站内存占用超过maxSize会报异常.<br/>
     *
     * @param maxSize Bitmap缓存区占用最大内存 单位byte
     */
    public CachedBitmapUtils(int maxSize){
        mBitmapCache = BitmapCache.newInstance(maxSize);
    }

    /************************************************************
     * 缓存管理
     ************************************************************/

    /**
     * 从缓存中取Bitmap<br/>
     * 若该Bitmap已被标记为unused, 则会清除unused标记<Br/>
     * 不会从回收站中取Bitmap<br/>
     *
     * @param key
     * @return
     */
    public Bitmap getBitmap(String key){
        return mBitmapCache.get(key);
    }

    public BitmapCache getBitmapCache(){
        return mBitmapCache;
    }

    /**
     * 将一个Bitmap放入缓存<Br/>
     * 放入前会强制回收已存在的同名Bitmap(包括缓存和回收站),
     * 不当的使用可能会导致异常 : 回收了正在使用的Bitmap
     *
     * @param key
     * @param bitmap
     * @return
     */
    public void cacheBitmap(String key, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        if (key == null || key.equals("")){
            key = DEFAULT_KEY_PREFIX + defaultKeyIndex;
            defaultKeyIndex++;
        }
        mBitmapCache.put(key, bitmap);
    }

    /**
     * 将一个Bitmap标记为不再使用, 缓存中的Bitmap不会被立即回收, 在内存不足时,
     * 会进行缓存清理, 清理时会将最早的被标记为unused的Bitmap.recycle()回收掉.
     * 已进入回收站的Bitmap会被立即回收.
     *
     * @param key
     */
    public void unused(String key){
        mBitmapCache.unused(key);
    }

    /**
     * 强制清除并回收所有Bitmap(包括缓存和回收站)
     */
    public void recycleAll(){
        mBitmapCache.removeAll();
    }

    /*********************************************
     * 				解码
     *********************************************/

    /**
     * 从资源文件中解码图片
     *
     * @param  key 标签
     * @param res   getResource()
     * @param resId 资源文件ID
     */
    public Bitmap decodeFromResource(String key, Resources res, int resId) {
        Bitmap bitmap = BitmapUtils.decodeFromResource(res, resId);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 从资源文件中解码图片(节省内存)<br/>
     * 根据宽高需求计算出缩放比率, 以整数倍缩放图片, 达到节省内存的效果,
     * 解码出的图片尺寸不等于需求尺寸.
     *
     * @param  key 标签
     * @param res       getResource()
     * @param resId     资源文件ID
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public Bitmap decodeFromResource(String key, Resources res, int resId, int reqWidth, int reqHeight) {
        Bitmap bitmap = BitmapUtils.decodeFromResource(res, resId, reqWidth, reqHeight);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 从文件中解码图片
     *
     * @param  key 标签
     * @param path 文件路径
     */
    public Bitmap decodeFromFile(String key, String path) {
        Bitmap bitmap = BitmapUtils.decodeFromFile(path);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 从文件中解码图片(节省内存)<br/>
     * 根据宽高需求计算出缩放比率, 以整数倍缩放图片, 达到节省内存的效果,
     * 解码出的图片尺寸不等于需求尺寸.
     *
     * @param  key 标签
     * @param path      文件路径
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public Bitmap decodeFromFile(String key, String path, int reqWidth, int reqHeight) {
        Bitmap bitmap = BitmapUtils.decodeFromFile(path, reqWidth, reqHeight);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 将二进制数据解码为图片
     *
     * @param  key 标签
     * @param data 二进制数据
     */
    public Bitmap decodeFromByteArray(String key, byte[] data) {
        Bitmap bitmap = BitmapUtils.decodeFromByteArray(data);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 将二进制数据解码为图片(节省内存)<br/>
     * 根据宽高需求计算出缩放比率, 以整数倍缩放图片, 达到节省内存的效果,
     * 解码出的图片尺寸不等于需求尺寸.
     *
     * @param  key 标签
     * @param data      二进制数据
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public Bitmap decodeFromByteArray(String key, byte[] data, int reqWidth, int reqHeight) {
        Bitmap bitmap = BitmapUtils.decodeFromByteArray(data, reqWidth, reqHeight);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 将Base64数据解码为图片
     *
     * @param  key 标签
     * @param base64 Base64数据
     */
    public Bitmap decodeFromBase64(String key, byte[] base64) {
        Bitmap bitmap = BitmapUtils.decodeFromBase64(base64);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 将Base64数据解码为图片(节省内存)<br/>
     * 根据宽高需求计算出缩放比率, 以整数倍缩放图片, 达到节省内存的效果,
     * 解码出的图片尺寸不等于需求尺寸.
     *
     * @param  key 标签
     * @param base64    Base64数据
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public Bitmap decodeFromBase64(String key, byte[] base64, int reqWidth, int reqHeight) {
        Bitmap bitmap = BitmapUtils.decodeFromBase64(base64, reqWidth, reqHeight);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 将Base64数据解码为图片
     *
     * @param  key 标签
     * @param base64 Base64数据
     */
    public Bitmap decodeFromBase64(String key, String base64) {
        Bitmap bitmap = BitmapUtils.decodeFromBase64(base64);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 将Base64数据解码为图片(节省内存)<br/>
     * 根据宽高需求计算出缩放比率, 以整数倍缩放图片, 达到节省内存的效果,
     * 解码出的图片尺寸不等于需求尺寸.
     *
     * @param  key 标签
     * @param base64    Base64数据
     * @param reqWidth  需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public Bitmap decodeFromBase64(String key, String base64, int reqWidth, int reqHeight) {
        Bitmap bitmap = BitmapUtils.decodeFromBase64(base64, reqWidth, reqHeight);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 从输入流中解码图片
     *
     * @param  key 标签
     * @param inputStream 输入流
     */
    public Bitmap decodeFromStream(String key, InputStream inputStream) {
        Bitmap bitmap = BitmapUtils.decodeFromStream(inputStream);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 从输入流中解码图片(节省内存)<br/>
     * 以整数倍缩放图片, 达到节省内存的效果<br/>
     *
     * InputStream只能使用一次, 因此不能通过第一次解码获得图片长宽,
     * 计算缩放因子, 再解码获得图片这种方式<br/>
     *
     * @param  key 标签
     * @param inputStream 输入流
     * @param inSampleSize 缩放因子 (1:原大小 2:缩小一倍 ...)
     */
    public Bitmap decodeFromStream(String key, InputStream inputStream, int inSampleSize) {
        Bitmap bitmap = BitmapUtils.decodeFromStream(inputStream, inSampleSize);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /*********************************************
     * 				转换/编辑
     *********************************************/

    /**
     * bitmap转为base64[回收源Bitmap]
     *
     * @param bitmap
     */
    public String bitmapToBase64(Bitmap bitmap) {
        return BitmapUtils.bitmapToBase64(bitmap, true);
    }

    /**
     * 按比例缩放图片[回收源Bitmap]
     *
     * @param  key 标签
     * @param bitmap
     * @param scale   缩放比例
     */
    public Bitmap zoom(String key, Bitmap bitmap, float scale) {
        Bitmap result = BitmapUtils.zoom(bitmap, scale, true);
        cacheBitmap(key, result);
        return result;
    }

    /**
     * 按比例缩放图片[回收源Bitmap]
     *
     * @param  key 标签
     * @param bitmap
     * @param scaleX  x缩放比例
     * @param scaleY  y缩放比例
     */
    public Bitmap zoom(String key, Bitmap bitmap, float scaleX, float scaleY) {
        Bitmap result = BitmapUtils.zoom(bitmap, scaleX, scaleY, true);
        cacheBitmap(key, result);
        return result;
    }

    /**
     * 将图片缩放至指定宽高[回收源Bitmap]
     *
     * @param  key 标签
     * @param bitmap
     * @param width   指定宽 >0
     * @param height  指定高 >0
     */
    public Bitmap zoom(String key, Bitmap bitmap, int width, int height) {
        Bitmap result = BitmapUtils.zoom(bitmap, width, height, true);
        cacheBitmap(key, result);
        return result;
    }

    /**
     * 图片圆角处理[回收源Bitmap]
     *
     * @param  key 标签
     * @param bitmap
     * @param radius  圆角半径
     */
    public Bitmap toRoundedCorner(String key, Bitmap bitmap, float radius) {
        Bitmap result = BitmapUtils.toRoundedCorner(bitmap, radius, true);
        cacheBitmap(key, result);
        return result;
    }


    /**
     * (自动cache)从资源文件中解码图片,并绘制文字(根据宽高需求"整数倍"缩放图片,节省内存)
     *
     * @param  key 标签
     * @param res       getResource()
     * @param resId     资源ID
     * @param text      需要绘制的文字
     * @param x         文字在X方向的位移
     * @param y         文字在Y方向的位移
     * @param textSize  字体大小
     * @param textColor 字体颜色
     */
    public Bitmap drawTextOnResource(String key, Resources res, int resId, String text, float x, float y, float textSize, int textColor) {
        Bitmap result = BitmapUtils.drawTextOnResource(res, resId, text, x, y, textSize, textColor);
        cacheBitmap(key, result);
        return result;
    }

    /**
     * 在Bitmap上绘制文字<br/>
     * <br/>
     * immutable bitmap pass to canvas异常解决:<br/>
     * 在绘制前复制: bitmap.copy(Bitmap.Config.ARGB_8888, true);<br/>
     *
     * @param  key 标签
     * @param bitmap
     * @param text      绘制的文本
     * @param x         位置
     * @param y         位置
     * @param textSize  字体大小
     * @param textColor 字体颜色
     */
    public Bitmap drawText(String key, Bitmap bitmap, String text, float x, float y, float textSize, int textColor) {
        Bitmap result = BitmapUtils.drawText(bitmap, text, x, y, textSize, textColor);
        cacheBitmap(key, result);
        return result;
    }

}
