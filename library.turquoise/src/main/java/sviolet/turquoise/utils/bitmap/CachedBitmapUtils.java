package sviolet.turquoise.utils.bitmap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import java.io.InputStream;

import sviolet.turquoise.utils.cache.BitmapCache;

/**
 * 内置内存缓存的Bitmap工具<br/>
 * <br/>
 * 内置BitmapCache图片内存缓存器:<br/>
 * @see BitmapCache
 * 图片处理调用BitmapUtils工具类:<br/>
 * @see BitmapUtils
 * <br/>
 * ****************************************************************<br/>
 * * * * * 注意事项:<br/>
 * ****************************************************************<br/>
 * <br/>
 * 1.若key送空(null), 会自动为Bitmap分配一个默认的不重复的标签<Br/>
 * 2.在解码/转换/编辑时, 若管理器中已存在同名(key)的Bitmap, 程序会先回收掉原有的Bitmap<br/>
 * 3.所有转换/编辑方法都会回收源Bitmap(与BitmapUtils可选不同)<br/>
 * <br/>
 * ****************************************************************<br/>
 * * * * * 名词解释:<br/>
 * ****************************************************************<br/>
 * <br/>
 * KEY:<Br/>
 *      Bitmap在缓存中的唯一标识.每个不同的图(Bitmap)必须分配不同的key.当Bitmap加入缓存时,若
 *      已存在同名(key)Bitmap,会覆盖原有Bitmap,即原有Bitmap从缓存中移除,并回收(recycle).<br/>
 * <Br/>
 * 缓存区:<Br/>
 *      缓存区满后, 会清理最早创建或最少使用的Bitmap. 若被清理的Bitmap已被置为unused不再
 *      使用状态, 则Bitmap会被立刻回收(recycle()), 否则会进入回收站等待被unused. 因此, 必须及时
 *      使用unused(key)方法将不再使用的Bitmap置为unused状态, 使得Bitmap尽快被回收.
 * <Br/>
 * 回收站:<Br/>
 *      用于存放因缓存区满被清理,但仍在被使用的Bitmap(未被标记为unused).<br/>
 *      显示中的Bitmap可能因为被引用(get)早,判定为优先度低而被清理出缓存区,绘制时出现"trying to use a
 *      recycled bitmap"异常,设置合适大小的回收站有助于减少此类事件发生.但回收站的使用会增加内存消耗,
 *      请适度设置.<br/>
 *      若设置为0禁用,缓存区清理时无视unused状态一律做回收(Bitmap.recycle)处理,且不进入回收站!!<br/>
 * <br/>
 * ****************************************************************<br/>
 * * * * * 错误处理:<br/>
 * ****************************************************************<br/>
 * <br/>
 * 1.Exception::[BitmapCache]recycler Out Of Memory!!!<br/>
 *      当回收站内存占用超过设定值时, 会触发此异常<Br/>
 *      解决方案:<br/>
 *      1).请合理使用BitmapCache.unused()方法, 将不再使用的Bitmap设置为"不再使用"状态,
 *          Bitmap只有被设置为此状态, 才会被回收(recycle()), 否则在缓存区满后, 会进入回收站,
 *          但并不会释放资源, 这么做是为了防止回收掉正在使用的Bitmap而报错.<br/>
 *      2).设置合理的缓存区及回收站大小, 分配过小可能会导致不够用而报错, 分配过大会使应用
 *          其他占用内存受限.<br/>
 * <br/>
 * <br/>
 *
 * Created by S.Violet on 2015/7/1.
 */
public class CachedBitmapUtils {

    private static final String DEFAULT_KEY_PREFIX = "DEFAULT_KEY_PREFIX";//默认标签前缀

    private int defaultKeyIndex = 0;//默认标签编号

    private BitmapCache mBitmapCache;

    /**
     * 创建缓存实例<Br/>
     * 根据实际情况设置缓存占比, 参考值0.125, 建议不超过0.25<Br/>
     * <Br/>
     * 缓存区:缓存区满后, 会清理最早创建或最少使用的Bitmap. 若被清理的Bitmap已被置为unused不再
     * 使用状态, 则Bitmap会被立刻回收(recycle()), 否则会进入回收站等待被unused. 因此, 必须及时
     * 使用unused(key)方法将不再使用的Bitmap置为unused状态, 使得Bitmap尽快被回收.
     * <Br/>
     * 回收站:用于存放因缓存区满被清理,但仍在被使用的Bitmap(未被标记为unused).<br/>
     * 显示中的Bitmap可能因为被引用(get)早,判定为优先度低而被清理出缓存区,绘制时出现"trying to use a
     * recycled bitmap"异常,设置合适大小的回收站有助于减少此类事件发生.但回收站的使用会增加内存消耗,
     * 请适度设置.<br/>
     * 若设置为0禁用,缓存区清理时无视unused状态一律做回收(Bitmap.recycle)处理,且不进入回收站!!<br/>
     * <br/>
     * Exception: [BitmapCache]recycler Out Of Memory!!!<br/>
     * 当回收站内存占用超过设定值时, 会触发此异常<Br/>
     * 解决方案:<br/>
     * 1.请合理使用BitmapCache.unused()方法, 将不再使用的Bitmap设置为"不再使用"状态,
     * Bitmap只有被设置为此状态, 才会被回收(recycle()), 否则在缓存区满后, 会进入回收站,
     * 但并不会释放资源, 这么做是为了防止回收掉正在使用的Bitmap而报错.<br/>
     * 2.设置合理的缓存区及回收站大小, 分配过小可能会导致不够用而报错, 分配过大会使应用
     * 其他占用内存受限.<br/>
     *
     * @param context
     * @param cachePercent Bitmap缓存区占用应用可用内存的比例 (0, 1]
     * @param recyclerPercent Bitmap回收站占用应用可用内存的比例 [0, 1], 设置为0禁用回收站
     * @return
     */
    public CachedBitmapUtils(Context context, float cachePercent, float recyclerPercent){
        mBitmapCache = BitmapCache.newInstance(context, cachePercent, recyclerPercent);
    }

    /**
     * 创建缓存实例<Br/>
     * 缓存区容量为默认值DEFAULT_CACHE_MEMORY_PERCENT = 0.125f<Br/>
     * 回收站容量为默认值DEFAULT_CACHE_MEMORY_PERCENT = 0.125f<Br/>
     * <Br/>
     * 缓存区:缓存区满后, 会清理最早创建或最少使用的Bitmap. 若被清理的Bitmap已被置为unused不再
     * 使用状态, 则Bitmap会被立刻回收(recycle()), 否则会进入回收站等待被unused. 因此, 必须及时
     * 使用unused(key)方法将不再使用的Bitmap置为unused状态, 使得Bitmap尽快被回收.
     * <Br/>
     * 回收站:用于存放因缓存区满被清理,但仍在被使用的Bitmap(未被标记为unused).<br/>
     * 显示中的Bitmap可能因为被引用(get)早,判定为优先度低而被清理出缓存区,绘制时出现"trying to use a
     * recycled bitmap"异常,设置合适大小的回收站有助于减少此类事件发生.但回收站的使用会增加内存消耗,
     * 请适度设置.<br/>
     * 若设置为0禁用,缓存区清理时无视unused状态一律做回收(Bitmap.recycle)处理,且不进入回收站!!<br/>
     * <br/>
     * Exception: [BitmapCache]recycler Out Of Memory!!!<br/>
     * 当回收站内存占用超过设定值时, 会触发此异常<Br/>
     * 解决方案:<br/>
     * 1.请合理使用BitmapCache.unused()方法, 将不再使用的Bitmap设置为"不再使用"状态,
     * Bitmap只有被设置为此状态, 才会被回收(recycle()), 否则在缓存区满后, 会进入回收站,
     * 但并不会释放资源, 这么做是为了防止回收掉正在使用的Bitmap而报错.<br/>
     * 2.设置合理的缓存区及回收站大小, 分配过小可能会导致不够用而报错, 分配过大会使应用
     * 其他占用内存受限.<br/>
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
     * 缓存区:缓存区满后, 会清理最早创建或最少使用的Bitmap. 若被清理的Bitmap已被置为unused不再
     * 使用状态, 则Bitmap会被立刻回收(recycle()), 否则会进入回收站等待被unused. 因此, 必须及时
     * 使用unused(key)方法将不再使用的Bitmap置为unused状态, 使得Bitmap尽快被回收.
     * <Br/>
     * 回收站:用于存放因缓存区满被清理,但仍在被使用的Bitmap(未被标记为unused).<br/>
     * 显示中的Bitmap可能因为被引用(get)早,判定为优先度低而被清理出缓存区,绘制时出现"trying to use a
     * recycled bitmap"异常,设置合适大小的回收站有助于减少此类事件发生.但回收站的使用会增加内存消耗,
     * 请适度设置.<br/>
     * 若设置为0禁用,缓存区清理时无视unused状态一律做回收(Bitmap.recycle)处理,且不进入回收站!!<br/>
     * <br/>
     * Exception: [BitmapCache]recycler Out Of Memory!!!<br/>
     * 当回收站内存占用超过设定值时, 会触发此异常<Br/>
     * 解决方案:<br/>
     * 1.请合理使用BitmapCache.unused()方法, 将不再使用的Bitmap设置为"不再使用"状态,
     * Bitmap只有被设置为此状态, 才会被回收(recycle()), 否则在缓存区满后, 会进入回收站,
     * 但并不会释放资源, 这么做是为了防止回收掉正在使用的Bitmap而报错.<br/>
     * 2.设置合理的缓存区及回收站大小, 分配过小可能会导致不够用而报错, 分配过大会使应用
     * 其他占用内存受限.<br/>
     *
     * @param cacheMaxSize Bitmap缓存区占用最大内存 单位byte (0, ?)
     * @param recyclerMaxSize Bitmap回收站占用最大内存 单位byte [0, ?), 设置为0禁用回收站
     */
    public CachedBitmapUtils(int cacheMaxSize, int recyclerMaxSize){
        mBitmapCache = BitmapCache.newInstance(cacheMaxSize, recyclerMaxSize);
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
     * 放入前会强制回收已存在的同名(key)Bitmap(包括缓存和回收站),不当的使用可能会导致
     * 异常 : 回收了正在使用的Bitmap<br/>
     *
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
     * @param bitmap
     * @return
     */
    public void cacheBitmap(String key, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        //若key为空, 自动分配一个
        if (key == null || key.equals("")){
            key = DEFAULT_KEY_PREFIX + defaultKeyIndex;
            defaultKeyIndex++;
        }
        mBitmapCache.put(key, bitmap);
    }

    /**
     * [重要]将一个Bitmap标示为不再使用,利于更快被回收<Br/>
     * <br/>
     * 将一个Bitmap标记为不再使用, 缓存中的Bitmap不会被立即回收, 在内存不足时,
     * 会进行缓存清理, 清理时会将最早的被标记为unused的Bitmap.recycle()回收掉.
     * 已进入回收站的Bitmap会被立即回收.<br/>
     *
     * @param key
     */
    public void asyncUnused(String key){
        mBitmapCache.asyncUnused(key);
    }

    /**
     * [重要]将一个Bitmap标示为不再使用,利于回收(Bitmap.recycle)<Br/>
     * <br/>
     * 将一个Bitmap标记为不再使用, 缓存中的Bitmap不会被立即回收, 在内存不足时,
     * 会进行缓存清理, 清理时会将最早的被标记为unused的Bitmap.recycle()回收掉.
     * 已进入回收站的Bitmap会被立即回收.<br/>
     * <br/>
     * 同步操作, 可能会阻塞
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
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
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
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
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
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
     * @param path 文件路径
     */
    public Bitmap decodeFromFile(String key, String path) {
        Bitmap bitmap = BitmapUtils.decodeFromFile(path);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 从文件中解码图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
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
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
     * @param data 二进制数据
     */
    public Bitmap decodeFromByteArray(String key, byte[] data) {
        Bitmap bitmap = BitmapUtils.decodeFromByteArray(data);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 将二进制数据解码为图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
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
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
     * @param base64 Base64数据
     */
    public Bitmap decodeFromBase64(String key, byte[] base64) {
        Bitmap bitmap = BitmapUtils.decodeFromBase64(base64);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 将Base64数据解码为图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
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
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
     * @param base64 Base64数据
     */
    public Bitmap decodeFromBase64(String key, String base64) {
        Bitmap bitmap = BitmapUtils.decodeFromBase64(base64);
        cacheBitmap(key, bitmap);
        return bitmap;
    }

    /**
     * 将Base64数据解码为图片(节省内存)<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
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
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
     * @param inputStream 输入流
     */
    public Bitmap decodeFromStream(String key, InputStream inputStream) {
        Bitmap bitmap = BitmapUtils.decodeFromStream(inputStream);
        cacheBitmap(key, bitmap);
        return bitmap;
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
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
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
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
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
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
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
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
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
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
     * @param bitmap 原图(会被回收)
     * @param radius 圆角半径
     * @param type BitmapUtils.RoundedCornerType 指定哪些角需要圆角处理
     */
    public Bitmap toRoundedCorner(String key, Bitmap bitmap, float radius, BitmapUtils.RoundedCornerType type) {
        Bitmap result = BitmapUtils.toRoundedCorner(bitmap, radius, type, true);
        cacheBitmap(key, result);
        return result;
    }


    /**
     * (自动cache)从资源文件中解码图片,并绘制文字(根据宽高需求"整数倍"缩放图片,节省内存)
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
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
    public Bitmap drawTextOnResource(String key, Resources res, int resId, int reqWidth, int reqHeight, String text, float x, float y, float textSize, int textColor) {
        Bitmap result = BitmapUtils.drawTextOnResource(res, resId, reqWidth, reqHeight, text, x, y, textSize, textColor);
        cacheBitmap(key, result);
        return result;
    }

    /**
     * 在Bitmap上绘制文字[回收源Bitmap]<br/>
     *
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
     * @param bitmap 原图
     * @param text      绘制的文本
     * @param x         位置
     * @param y         位置
     * @param textSize  字体大小
     * @param textColor 字体颜色
     */
    public Bitmap drawText(String key, Bitmap bitmap, String text, float x, float y, float textSize, int textColor) {
        Bitmap result = BitmapUtils.drawText(bitmap, text, x, y, textSize, textColor, true);
        cacheBitmap(key, result);
        return result;
    }

}
