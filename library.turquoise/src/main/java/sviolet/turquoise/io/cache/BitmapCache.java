package sviolet.turquoise.io.cache;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;

import java.util.LinkedHashMap;
import java.util.Map;

import sviolet.turquoise.app.CommonException;
import sviolet.turquoise.compat.CompatLruCache;
import sviolet.turquoise.utils.DeviceUtils;

/**
 * Bitmap内存缓存<br/>
 * <Br/>
 * BitmapCache内存占用最大值为设置值(maxSize)的两倍, 即缓存区占用maxSize, 回收
 * 站占用maxSize, 回收站内存占用超过maxSize会报异常.
 * <br/>
 * CommonException: [BitmapCache]recycler Out Of Memory!!!<br/>
 * 当回收站内存占用超过设定值 (即内存总消耗超过设定值的两倍) 时, 会触发此异常<Br/>
 * 解决方案:<br/>
 * 1.请合理使用BitmapCache.unused()方法, 将不再使用的Bitmap设置为"不再使用"状态,
 * Bitmap只有被设置为此状态, 才会被回收(recycle()), 否则在缓存区满后, 会进入回收站,
 * 但并不会释放资源, 这么做是为了防止回收掉正在使用的Bitmap而报错.<br/>
 * 2.给BitmapCache设置合理的最大占用内存(或占比), 分配过小可能会导致不够用而报错,
 * 分配过大可能使应用其他占用内存受限.<br/>
 */
public class BitmapCache extends CompatLruCache<String, Bitmap> {

    private static final float DEFAULT_CACHE_MEMORY_PERCENT = 0.125f;

    //回收站 : 存放被清理出缓存但未被标记为unused的Bitmap
    private final LinkedHashMap<String, Bitmap> recyclerMap;
    //不再使用标记
    private final LinkedHashMap<String, Boolean> unusedMap;
    //回收站占用内存
    private int recyclerSize = 0;

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
    public static BitmapCache newInstance(Context context){
        //应用可用内存级别
        final int memoryClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        //计算缓存大小
        final int cacheSize = (int) (1024 * 1024 * memoryClass * DEFAULT_CACHE_MEMORY_PERCENT);
        //实例化
        return new BitmapCache(cacheSize);
    }

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
    public static BitmapCache newInstance(Context context, float percent){
        //应用可用内存级别
        final int memoryClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        //计算缓存大小
        final int cacheSize = (int) (1024 * 1024 * memoryClass * percent);
        //实例化
        return new BitmapCache(cacheSize);
    }

    /**
     * BitmapCache内存占用最大值为设置值(maxSize)的两倍, 即缓存区占用maxSize, 回收
     * 站占用maxSize, 回收站内存占用超过maxSize会报异常.<br/>
     *
     * @param maxSize Bitmap缓存区占用最大内存 单位byte
     */
    public BitmapCache(int maxSize) {
        super(maxSize);
        this.recyclerMap = new LinkedHashMap<String, Bitmap>(0, 0.75f, true);
        this.unusedMap = new LinkedHashMap<String, Boolean>(0, 0.75f, true);
    }

    /*****************************************************************************
     * function
     */

    /**
     * 将一个Bitmap标记为不再使用, 缓存中的Bitmap不会被立即回收, 在内存不足时,
     * 会进行缓存清理, 清理时会将最早的被标记为unused的Bitmap.recycle()回收掉.
     * 已进入回收站的Bitmap会被立即回收.
     *
     * @param key
     */
    public void unused(String key){
        Bitmap bitmap = null;
        synchronized (this) {
            //若Bitmap存在回收站中, 则直接清除回收资源
            if (recyclerMap.containsKey(key)) {
                bitmap = recyclerMap.remove(key);
                recyclerSize -= sizeOf(key, bitmap);
            }
            //置为不再使用状态
            unusedMap.put(key, true);
        }
        //回收资源
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            System.gc();
        }
    }

    /**
     * 从缓存中取Bitmap<br/>
     * 若该Bitmap已被标记为unused, 则会清除unused标记<Br/>
     * 不会从回收站中取Bitmap<br/>
     *
     * @param key
     * @return
     */
    @Override
    public Bitmap get(String key) {
        //移除不再使用标记
        unusedMap.remove(key);
        //返回Bitmap
        return super.get(key);
    }

    /**
     * 将一个Bitmap放入缓存<Br/>
     * 放入前会强制回收已存在的同名Bitmap(包括缓存和回收站),
     * 不当的使用可能会导致异常 : 回收了正在使用的Bitmap
     *
     * @param key
     * @param value
     * @return
     */
    @Override
    public Bitmap put(String key, Bitmap value) {
        //先强制移除并回收同名Bitmap
        remove(key);
        return super.put(key, value);
    }

    /**
     * 从缓存中移除<br/>
     * 强制回收Bitmap(包括缓存和回收站)并返回null<br/>
     *
     * @param key
     * @return 返回null
     */
    @Override
    public Bitmap remove(String key) {
        //GC标志
        boolean needGc = false;
        Bitmap bitmap;
        Bitmap recyclerBitmap;
        synchronized (this) {
            //从缓存中移除
            bitmap = super.remove(key);
            //从回收站移除
            recyclerBitmap = recyclerMap.remove(key);
            recyclerSize -= sizeOf(key, recyclerBitmap);
            //移除不再使用标志
            unusedMap.remove(key);
        }
        //回收资源
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            needGc = true;
        }
        //回收"回收站"中的资源
        if (recyclerBitmap != null && !recyclerBitmap.isRecycled()) {
            recyclerBitmap.recycle();
            needGc = true;
        }
        //GC
        if (needGc){
            System.gc();
        }
        //返回空
        return null;
    }

    /**
     * 强制清除并回收所有Bitmap(包括缓存和回收站)
     */
    public void removeAll(){
        //移除缓存中的所有资源
        for (Map.Entry<String, Bitmap> entry : getMap().entrySet()) {
            Bitmap bitmap = entry.getValue();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        //移除回收站资源
        for (Map.Entry<String, Bitmap> entry : recyclerMap.entrySet()) {
            Bitmap bitmap = entry.getValue();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        //清理
        synchronized (this) {
            getMap().clear();
            recyclerMap.clear();
            unusedMap.clear();
            setSize(0);
            recyclerSize = 0;
        }
        //GC
        System.gc();
    }

    /**
     * 获得回收站占用内存byte
     * @return
     */
    public int recyclerSize(){
        return recyclerSize;
    }

    /******************************************************
     * override
     */

    @Override
    protected void trimToSize(int maxSize) {
        while (true) {
            String key;
            Bitmap value;
            synchronized (this) {
                if (getSize() < 0 || (getMap().isEmpty() && getSize() != 0)) {
                    throw new IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent results!");
                }

                if (getSize() <= maxSize) {
                    break;
                }

                Map.Entry<String, Bitmap> toEvict = null;

                for (Map.Entry<String, Bitmap> entry : getMap().entrySet()) {
                    toEvict = entry;
                    //LruCache中没有break
                    //原来取最后一项, 即为最新加入或最近操作过的一项
                    //此处改为取第一项, 即为最早加入或最少操作的一项
                    break;
                }

                if (toEvict == null) {
                    break;
                }

                key = toEvict.getKey();
                value = toEvict.getValue();

                setSize(getSize() - safeSizeOf(key, value));

                //是否被标记为不再使用
                if (unusedMap.containsKey(key)){
                    //回收不再使用的Bitmap
                    if (value != null && !value.isRecycled()){
                        value.recycle();
                        System.gc();
                    }
                    //清除标记
                    unusedMap.remove(key);
                }else{
                    //加入回收站前清理回收站中的同名资源
                    if (recyclerMap.containsKey(key)){
                        Bitmap recyclerBitmap = recyclerMap.remove(key);
                        if (recyclerBitmap != null && !recyclerBitmap.isRecycled()){
                            recyclerSize -= sizeOf(key, recyclerBitmap);
                            recyclerBitmap.recycle();
                            System.gc();
                        }
                    }
                    //放入回收站
                    recyclerMap.put(key, value);
                    recyclerSize += sizeOf(key, value);
                    /*
                        当回收站内存占用超过设定值 (即内存总消耗超过设定值的两倍) 时, 会触发此异常,
                        解决方案:
                        1.请合理使用BitmapCache.unused()方法, 将不再使用的Bitmap设置为"不再使用"状态,
                           Bitmap只有被设置为此状态, 才会被回收(recycle()), 否则在缓存区满后, 会进入回收站,
                           但并不会释放资源, 这么做是为了防止回收掉正在使用的Bitmap而报错.
                        2.给BitmapCache设置合理的最大占用内存(或占比), 分配过小可能会导致不够用而报错,
                          分配过大可能使应用其他占用内存受限.
                     */
                    if (recyclerSize > getMaxSize()){
                        throw new CommonException("[BitmapCache]recycler Out Of Memory!!! see Notes of BitmapCache");
                    }
                }

                getMap().remove(key);
                setEvictionCount(getEvictionCount() + 1);
            }

            entryRemoved(true, key, value, null);
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected int sizeOf(String key, Bitmap value) {
        //资源不存在或被回收返回0
        if (value == null || value.isRecycled())
            return 0;
        //计算图片占内存大小
        if (DeviceUtils.getVersionSDK() >= 12) {
            return value.getByteCount();
        }
        return value.getRowBytes() * value.getHeight();
    }

}
