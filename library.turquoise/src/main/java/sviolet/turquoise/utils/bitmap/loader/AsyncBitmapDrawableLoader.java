package sviolet.turquoise.utils.bitmap.loader;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.IOException;

import sviolet.turquoise.enhance.utils.Logger;

/**
 * 图片双缓存网络异步加载器<br/>
 * <br/>
 * Bitmap内存缓存+磁盘缓存+网络加载+防OOM<br/>
 * <br/>
 * 请使用:<br/>
 * 1.AsyncBitmapLoader<br/>
 * 2.AsyncBitmapDrawableLoader<br/>
 * <Br/>
 * ****************************************************************<br/>
 * <br/>
 * AsyncBitmapLoader中每个位图资源都由url唯一标识, url在AsyncBitmapLoader内部
 * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
 * 这个cacheKey标识唯一的资源<br/>
 * <Br/>
 * ****************************************************************<br/>
 * [使用说明]<br/>
 * 1.实现接口BitmapLoaderImplementor -> MyBitmapLoaderImplementor <br/>
 * 2.实例化AsyncBitmapLoader(Context,String,BitmapLoaderImplementor) <br/>
 * 3.setRamCache/setDiskCache/setNetLoad/setLogger设置参数(可选) <br/>
 * 4.open() 启用AsyncBitmapLoader(必须, 否则抛异常)<br/>
 * <br/>
 * [AsyncBitmapLoader代码示例]:<Br/>
     private AsyncBitmapLoader mAsyncBitmapLoader;
     try {
         mAsyncBitmapLoader = new AsyncBitmapLoader(this, "bitmap", new MyBitmapLoaderImplementor())
             .setRamCache(0.125f, 0.125f)//启用回收站
             //.setRamCache(0.125f, 0)//回收站设置为0禁用
             .setDiskCache(50, 5, 15)
             .setNetLoad(3, 15)
             .setImageQuality(Bitmap.CompressFormat.JPEG, 70)//设置保存格式和质量
             .setDiskCacheInner()//强制使用内部储存
             //.setDuplicateLoadEnable(true)//允许相同图片同时加载(慎用)
             .setLogger(getLogger())
             .open();//必须调用
     } catch (IOException e) {
        //磁盘缓存打开失败的情况, 可提示客户磁盘已满等
     }
 * <br/>
 * [上述代码说明]:<br/>
 * 位图内存缓存占用应用最大可用内存的12.5%,回收站最大可能占用额外的12.5%,
 * 内存缓存能容纳2-3页的图片为宜. 设置过小, 存放不下一页的内容, 影响显示效果,
 * 设置过大, 缓存占用应用可用内存过大, 影响性能或造成OOM. <br/>
 * 在路径/sdcard/Android/data/<application package>/cache/bitmap或
 * /data/data/<application package>/cache/bitmap下存放磁盘缓存数据,
 * 缓存最大容量50M, 磁盘缓存容量根据实际情况设置, 磁盘缓存加载最大并发量10,
 * 并发量应考虑图片质量/大小, 若图片较大, 应考虑减少并发量, 磁盘缓存等待队列
 * 容量15, 即只会加载最后请求的15个任务, 更早的加载请求会被取消, 等待队列容
 * 量根据屏幕中最多可能展示的图片数决定, 设定值为屏幕最多可能展示图片数的1-
 * 2倍为宜, 设置过少会导致屏幕中图片未全部加载完, 例如屏幕中最多可能展示10
 * 张图片, 则设置15-20较为合适, 若设置了10, 屏幕中会有2张图未加载. <br/>
 * 网络加载并发量为3, 根据网络情况和图片大小决定, 过多的并发量会阻塞网络, 过
 * 少会导致图片加载太慢, 网络加载等待队列容量15, 建议与磁盘缓存等待队列容量
 * 相等, 根据屏幕中最多可能展示的图片数决定(略大于), 设置过少会导致屏幕中图
 * 片未全部加载完.<br/>
 * 设置日志打印器后, AsyncBitmapLoader会打印出一些日志用于调试, 例如内存缓存使用
 * 情况, 图片加载日志等, 可根据日志调试/选择上述参数的值.<br/>
 * <br/>
 * <br/>
 * [AsyncBitmapDrawableLoader代码示例]:<br/>
     try {
         mAsyncBitmapDrawableLoader = new AsyncBitmapDrawableLoader(this, "AsyncImageActivity",
             BitmapUtils.decodeFromResource(getResources(), R.mipmap.async_image_null), new MyBitmapLoaderImplementor())
             .setRamCache(0.15f)//缓存占15%内存(与AsyncBitmapLoader不同之处)
             .setDiskCache(50, 5, 25)//磁盘缓存50M, 5线程磁盘加载, 等待队列容量25
             .setNetLoad(3, 25)//3线程网络加载, 等待队列容量25
             .setDiskCacheInner()//强制使用内部储存
             .setImageQuality(Bitmap.CompressFormat.JPEG, 70)//设置保存格式和质量
             //.setDuplicateLoadEnable(true)//允许相同图片同时加载(慎用)
             //.setLogger(getLogger())//打印日志
             .open();//启动(必须)
     } catch (IOException e) {
        //磁盘缓存打开失败的情况, 可提示客户磁盘已满等
     }
 * <Br/>
 * ****************************************************************<br/>
 * Tips::<br/>
 * <br/>
 * 1.当一个页面中需要同时加载相同图片,却发现只加载出一个,其余的都被取消(onLoadCanceled).
 *   尝试设置setDuplicateLoadEnable(true);<Br/>
 * <br/>
 * ****************************************************************<br/>
 * <Br/>
 * 缓存区:缓存区满后, 会清理最早创建或最少使用的Bitmap. 若被清理的Bitmap已被置为unused不再
 * 使用状态, 则Bitmap会被立刻回收(recycle()), 否则会进入回收站等待被unused. 因此, 必须及时
 * 使用unused(url)方法将不再使用的Bitmap置为unused状态, 使得Bitmap尽快被回收.<br/>
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
 * Created by S.Violet on 2015/7/3.
 */
public class AsyncBitmapDrawableLoader extends AbstractBitmapLoader {

    private Bitmap loadingBitmap;//加载状态的图片

    /**
     * 内存缓存区默认0.125f
     *
     * @param context 上下文
     * @param diskCacheName 磁盘缓存目录名
     * @param loadingBitmap 加载时的图片(可为空, AsyncBitmapDrawableLoader.destroy时会回收该Bitmap)
     * @param implementor 实现器
     */
    public AsyncBitmapDrawableLoader(Context context, String diskCacheName, Bitmap loadingBitmap, BitmapLoaderImplementor implementor) {
        super(context, diskCacheName, implementor);
        this.loadingBitmap = loadingBitmap;
        setRamCache(0.125f);
    }

    ////////////////////////////////////////////////////////////////////////
    //开放的方法
    ////////////////////////////////////////////////////////////////////////

    /**
     * 加载图片, 立即返回AsyncBitmapDrawable<br/>
     * AsyncBitmapDrawable为异步的BitmapDrawable, 在加载时会显示加载图, 加载成功后会自动刷新为目标图片<br/>
     * ImageView.setImageDrawable()方法直接设置图片使用, 请勿获取其中的Bitmap使用.<br/>
     * <br/>
     * AsyncBitmapLoader中每个位图资源都由url唯一标识, url在AsyncBitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param url 图片URL地址
     * @param reqWidth 需求宽度 px
     * @param reqHeight 需求高度 px
     */
    public AsyncBitmapDrawable load(String url, int reqWidth, int reqHeight) {
        return new AsyncBitmapDrawable(url, reqWidth, reqHeight, this);
    }

    /**
     * 从内存缓存中取AsyncBitmapDrawable, 若不存在或已被回收, 则返回null<br/>
     * AsyncBitmapDrawable为异步的BitmapDrawable, 在加载时会显示加载图, 加载成功后会自动刷新为目标图片<br/>
     * ImageView.setImageDrawable()方法直接设置图片使用, 请勿获取其中的Bitmap使用.<br/>
     * <br/>
     * AsyncBitmapLoader中每个位图资源都由url唯一标识, url在AsyncBitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param url 图片URL地址
     * @param reqWidth 需求宽度 px
     * @param reqHeight 需求高度 px
     * @return 若不存在或已被回收, 则返回null
     */
    public AsyncBitmapDrawable get(String url, int reqWidth, int reqHeight) {
        Bitmap bitmap = super.get(url);
        if (bitmap == null)
            return null;
        return new AsyncBitmapDrawable(url, reqWidth, reqHeight, this, bitmap);
    }

    @Override
    public void unused(String url) {
        super.unused(url);
    }

    @Override
    public void destroy() {
        super.destroy();
        //回收加载图
        if (loadingBitmap != null && !loadingBitmap.isRecycled()){
            loadingBitmap.recycle();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //GETTER
    ////////////////////////////////////////////////////////////////////////

    Bitmap getLoadingBitmap(){
        return loadingBitmap;
    }

    ////////////////////////////////////////////////////////////////////////
    //变换返回值
    ////////////////////////////////////////////////////////////////////////

    @Override
    public AsyncBitmapDrawableLoader setDiskCache(int diskCacheSizeMib, int diskLoadConcurrency, int diskLoadVolume) {
        return (AsyncBitmapDrawableLoader) super.setDiskCache(diskCacheSizeMib, diskLoadConcurrency, diskLoadVolume);
    }

    @Override
    public AsyncBitmapDrawableLoader setDiskCacheInner() {
        return (AsyncBitmapDrawableLoader) super.setDiskCacheInner();
    }

    @Override
    public AsyncBitmapDrawableLoader setDuplicateLoadEnable(boolean duplicateLoadEnable) {
        return (AsyncBitmapDrawableLoader) super.setDuplicateLoadEnable(duplicateLoadEnable);
    }

    @Override
    public AsyncBitmapDrawableLoader setImageQuality(Bitmap.CompressFormat format, int quality) {
        return (AsyncBitmapDrawableLoader) super.setImageQuality(format, quality);
    }

    @Override
    public AsyncBitmapDrawableLoader setLogger(Logger logger) {
        return (AsyncBitmapDrawableLoader) super.setLogger(logger);
    }

    @Override
    public AsyncBitmapDrawableLoader setNetLoad(int netLoadConcurrency, int netLoadVolume) {
        return (AsyncBitmapDrawableLoader) super.setNetLoad(netLoadConcurrency, netLoadVolume);
    }

    /**
     * 缓存区:缓存区满后, 会清理被标记为unused/最早创建/最少使用的Bitmap. 并立刻回收(recycle()),
     * 及时地使用unused(url)方法将不再使用的Bitmap置为unused状态, 可以使得Bitmap尽快被回收.
     * <br/>
     * AsyncBitmapDrawableLoader禁用了BitmapCache回收站<br/>
     *
     * @param ramCacheSizePercent 内存缓存区占用应用可用内存的比例 (0, 1], 默认值0.125f
     */
    public AsyncBitmapDrawableLoader setRamCache(float ramCacheSizePercent) {
        return (AsyncBitmapDrawableLoader) super.setRamCache(ramCacheSizePercent, 0);
    }

    @Override
    public AsyncBitmapDrawableLoader open() throws IOException {
        return (AsyncBitmapDrawableLoader) super.open();
    }
}
