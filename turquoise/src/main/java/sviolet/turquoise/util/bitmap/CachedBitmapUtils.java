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
import android.content.res.Resources;
import android.graphics.Bitmap;

import java.io.InputStream;

import sviolet.turquoise.model.cache.BitmapCache;
import sviolet.turquoise.utilx.lifecycle.listener.LifeCycle;

/**
 * 
 * 内置内存缓存的Bitmap工具<br/>
 * <br/>
 * 内置BitmapCache图片内存缓存器:<br/>
 * {@link BitmapCache}
 * 图片处理调用BitmapUtils工具类:<br/>
 * {@link BitmapUtils}
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
 *      使用unused(key)方法将不再使用的Bitmap置为unused状态, 使得Bitmap尽快被回收.<br/>
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
 *
 * @author S.Violet
 *
 * Created by S.Violet on 2015/7/1.
 */
public class CachedBitmapUtils implements LifeCycle {

    private static final String DEFAULT_KEY_PREFIX = "DEFAULT_KEY_PREFIX";//默认标签前缀

    private int defaultKeyIndex = 0;//默认标签编号

    private BitmapCache mBitmapCache;

    /**
     * 
     * 创建缓存实例<Br/>
     * 根据实际情况设置缓存占比, 参考值0.125, 建议不超过0.25<Br/>
     * <Br/>
     * 缓存区:缓存区满后, 会清理最早创建或最少使用的Bitmap. 若被清理的Bitmap已被置为unused不再
     * 使用状态, 则Bitmap会被立刻回收(recycle()), 否则会进入回收站等待被unused. 因此, 必须及时
     * 使用unused(key)方法将不再使用的Bitmap置为unused状态, 使得Bitmap尽快被回收.<br/>
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
     * 
     * 创建缓存实例<Br/>
     * 缓存区容量为默认值DEFAULT_CACHE_MEMORY_PERCENT = 0.125f<Br/>
     * 回收站容量为默认值DEFAULT_CACHE_MEMORY_PERCENT = 0.125f<Br/>
     * <Br/>
     * 缓存区:缓存区满后, 会清理最早创建或最少使用的Bitmap. 若被清理的Bitmap已被置为unused不再
     * 使用状态, 则Bitmap会被立刻回收(recycle()), 否则会进入回收站等待被unused. 因此, 必须及时
     * 使用unused(key)方法将不再使用的Bitmap置为unused状态, 使得Bitmap尽快被回收.<br/>
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
     *
     * @param context
     * @return
     */
    public CachedBitmapUtils(Context context){
        mBitmapCache = BitmapCache.newInstance(context);
    }

    /**
     * 
     * 创建缓存实例<Br/>
     * 根据实际情况设置缓冲区占用最大内存<br/>
     * <Br/>
     * 缓存区:缓存区满后, 会清理最早创建或最少使用的Bitmap. 若被清理的Bitmap已被置为unused不再
     * 使用状态, 则Bitmap会被立刻回收(recycle()), 否则会进入回收站等待被unused. 因此, 必须及时
     * 使用unused(key)方法将不再使用的Bitmap置为unused状态, 使得Bitmap尽快被回收.<br/>
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
        //若key为空, 自动分配一个
        if (key == null || key.equals("")){
            key = DEFAULT_KEY_PREFIX + defaultKeyIndex;
            defaultKeyIndex++;
        }
        mBitmapCache.put(key, bitmap);
    }

    /**
     * [重要]将一个Bitmap标示为不再使用,利于回收(Bitmap.recycle)<Br/>
     * <br/>
     * 将一个Bitmap标记为不再使用, 缓存中的Bitmap不会被立即回收, 在内存不足时,
     * 会进行缓存清理, 清理时会将最早的被标记为unused的Bitmap.recycle()回收掉.
     * 已进入回收站的Bitmap会被立即回收.<br/>
     *
     * @param key
     */
    public void unused(String key){
        mBitmapCache.unused(key);
    }

    /**
     * 强制清空缓存中不再使用(unused)的图片<br/>
     * <br/>
     * 用于暂时减少缓存的内存占用,请勿频繁调用.<br/>
     * 通常是内存紧张的场合, 可以在Activity.onStop()中调用, Activity暂时不显示的情况下,
     * 将缓存中已被标记为unused的图片回收掉, 减少内存占用. 但这样会使得重新显示时, 加载
     * 变慢(需要重新加载).<br/>
     */
    public void reduce(){
        mBitmapCache.reduce();
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
        Bitmap bitmap = getBitmap(key);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapUtils.decodeFromResource(res, resId);
            cacheBitmap(key, bitmap);
        }
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
        Bitmap bitmap = getBitmap(key);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapUtils.decodeFromResource(res, resId, reqWidth, reqHeight);
            cacheBitmap(key, bitmap);
        }
        return bitmap;
    }

    /**
     * 从文件中解码图片
     *
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
     * @param path 文件路径
     */
    public Bitmap decodeFromFile(String key, String path) {
        Bitmap bitmap = getBitmap(key);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapUtils.decodeFromFile(path);
            cacheBitmap(key, bitmap);
        }
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
        Bitmap bitmap = getBitmap(key);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapUtils.decodeFromFile(path, reqWidth, reqHeight);
            cacheBitmap(key, bitmap);
        }
        return bitmap;
    }

    /**
     * 将二进制数据解码为图片
     *
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
     * @param data 二进制数据
     */
    public Bitmap decodeFromByteArray(String key, byte[] data) {
        Bitmap bitmap = getBitmap(key);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapUtils.decodeFromByteArray(data);
            cacheBitmap(key, bitmap);
        }
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
        Bitmap bitmap = getBitmap(key);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapUtils.decodeFromByteArray(data, reqWidth, reqHeight);
            cacheBitmap(key, bitmap);
        }
        return bitmap;
    }

    /**
     * 将Base64数据解码为图片
     *
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
     * @param base64 Base64数据
     */
    public Bitmap decodeFromBase64(String key, byte[] base64) {
        Bitmap bitmap = getBitmap(key);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapUtils.decodeFromBase64(base64);
            cacheBitmap(key, bitmap);
        }
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
        Bitmap bitmap = getBitmap(key);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapUtils.decodeFromBase64(base64, reqWidth, reqHeight);
            cacheBitmap(key, bitmap);
        }
        return bitmap;
    }

    /**
     * 将Base64数据解码为图片
     *
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
     * @param base64 Base64数据
     */
    public Bitmap decodeFromBase64(String key, String base64) {
        Bitmap bitmap = getBitmap(key);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapUtils.decodeFromBase64(base64);
            cacheBitmap(key, bitmap);
        }
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
        Bitmap bitmap = getBitmap(key);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapUtils.decodeFromBase64(base64, reqWidth, reqHeight);
            cacheBitmap(key, bitmap);
        }
        return bitmap;
    }

    /**
     * 从输入流中解码图片
     *
     * @param key 唯一标识,若送空(null),会自动分配一个不重复的key
     * @param inputStream 输入流
     */
    public Bitmap decodeFromStream(String key, InputStream inputStream) {
        Bitmap bitmap = getBitmap(key);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapUtils.decodeFromStream(inputStream);
            cacheBitmap(key, bitmap);
        }
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
        Bitmap bitmap = getBitmap(key);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapUtils.decodeFromStream(inputStream, inSampleSize);
            cacheBitmap(key, bitmap);
        }
        return bitmap;
    }

    /***************************************************************
     * LifeCycle
     */

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {
        reduce();
    }

    @Override
    public void onDestroy() {
        recycleAll();
    }
}
