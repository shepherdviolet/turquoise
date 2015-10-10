package sviolet.turquoise.utils.bitmap.loader;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.IOException;

import sviolet.turquoise.enhance.utils.Logger;

/**
 * 图片双缓存网络异步加载器<br/>
 * <br/>
 * ****************************************************************<br/>
 * * * * * 种类:<br/>
 * ****************************************************************<br/>
 * <br/>
 * 1.AsyncBitmapLoader<br/>
 *      加载Bitmap, 适用性广泛, 使用较复杂.<br/>
 * 2.AsyncBitmapDrawableLoader<br/>
 *      加载AsyncBitmapDrawable, 使用简单.<br/>
 * <Br/>
 * ****************************************************************<br/>
 * * * * * AsyncBitmapLoader使用说明:<br/>
 * ****************************************************************<br/>
 * <br/>
 * -------------------初始化设置----------------<br/>
 * <br/>
 * 1.实现接口BitmapLoaderImplementor<br/>
 * 2.实例化AsyncBitmapLoader(Context,String,BitmapLoaderImplementor) <br/>
 * 3.设置参数:<br/>
 *   try {
 *       mAsyncBitmapLoader = new AsyncBitmapLoader(this, "bitmap", new MyBitmapLoaderImplementor())
 *          .setRamCache(0.125f, 0.125f)//设置内存缓存大小,启用回收站
 *          //.setRamCache(0.125f, 0)//回收站设置为0禁用
 *          .setDiskCache(50, 5, 15)//设置磁盘缓存容量50M, 磁盘加载并发数5, 等待队列15
 *          .setNetLoad(3, 15)//设置网络加载并发数3, 等待队列15
 *          .setImageQuality(Bitmap.CompressFormat.JPEG, 70)//设置磁盘缓存保存格式和质量
 *          //.setDiskCacheInner()//强制使用内部储存
 *          //.setDuplicateLoadEnable(true)//允许相同图片同时加载(慎用)
 *          //.setLogger(getLogger())
 *          .open();//必须调用
 *   } catch (IOException e) {
 *      //磁盘缓存打开失败的情况, 可提示客户磁盘已满等
 *   }
 * <br/>
 *      [上述代码说明]:<br/>
 *      位图内存缓存占用应用最大可用内存的12.5%,回收站最大可能占用额外的12.5%,
 *      内存缓存能容纳2-3页的图片为宜. 设置过小, 存放不下一页的内容, 图片显示不全,
 *      设置过大, 缓存占用应用可用内存过大, 影响性能或造成OOM. <br/>
 *      在路径/sdcard/Android/data/<application package>/cache/bitmap或
 *      /data/data/<application package>/cache/bitmap下存放磁盘缓存数据,
 *      缓存最大容量50M, 磁盘缓存容量根据实际情况设置, 磁盘缓存加载最大并发量5,
 *      并发量应考虑图片质量/大小, 若图片较大, 应考虑减少并发量, 磁盘缓存等待队列
 *      容量15, 即只会加载最后请求的15个任务, 更早的加载请求会被取消, 等待队列容
 *      量根据屏幕中最多可能展示的图片数决定, 设定值为屏幕最多可能展示图片数的1-
 *      2倍为宜, 设置过少会导致屏幕中图片未全部加载完, 例如屏幕中最多可能展示10
 *      张图片, 则设置15-20较为合适, 若设置了10, 屏幕中会有几张图未加载. <br/>
 *      网络加载并发量为3, 根据网络情况和图片大小决定, 过多的并发量会阻塞网络, 过
 *      少会导致图片加载太慢, 网络加载等待队列容量15, 建议与磁盘缓存等待队列容量
 *      相等, 根据屏幕中最多可能展示的图片数决定(略大于), 设置过少会导致屏幕中图
 *      片未全部加载完.<br/>
 *      设置日志打印器后, AsyncBitmapLoader会打印出一些日志用于调试, 例如内存缓存使用
 *      情况, 图片加载日志等, 可根据日志调试/选择上述参数的值.<br/>
 * <br/>
 * <br/>
 * -------------------加载器使用----------------<br/>
 * <br/>
 * 1.load <br/>
 *      加载图片,加载结束后回调OnBitmapLoadedListener<Br/>
 * 2.get <br/>
 *      从内存缓冲获取图片,若不存在返回null<br/>
 * 3.unused [重要] <br/>
 *      不再使用的图片须及时用该方法废弃,尤其是大量图片的场合,未被废弃(unused)的图片
 *      将不会被AsyncBitmapLoader回收.请参看"名词解释".<br/>
 *      该方法能取消加载任务,有助于减少不必要的加载,节省流量,使需要显示的图片尽快加载.<br/>
 * 4.destroy [重要] <br/>
 *      清除全部图片及加载任务,通常在Activity.onDestroy中调用<br/>
 * <Br/>
 * -------------------注意事项----------------<br/>
 * <br/>
 * 1.ListView等View复用的场合,应先unused废弃原Bitmap,再设置新的:
 *      holder.imageView.setImageBitmap(null);//置空(或默认图)
 *      String oldUrl = (String) holder.imageView.getTag();//原图的url
 *      if(oldUrl != null)
 *          asyncBitmapLoader.unused(oldUrl);//将原图标识为不再使用,并取消原加载任务
 *      holder.imageView.setTag(newUrl);//记录新图的url,用于下次unused
 *      asyncBitmapLoader.load(newUrl, reqWidth, reqHeight, holder.imageView, mOnBitmapLoadedListener);//加载图片
 * <Br/>
 * ****************************************************************<br/>
 * * * * * AsyncBitmapDrawableLoader使用说明:<br/>
 * ****************************************************************<br/>
 * <br/>
 * -------------------初始化设置----------------<br/>
 * <br/>
 * 1.实现接口BitmapLoaderImplementor<br/>
 * 2.实例化AsyncBitmapDrawableLoader(Context,String,Bitmap,BitmapLoaderImplementor) <br/>
 * 3.设置参数:<br/>
 *   try {
 *       mAsyncBitmapDrawableLoader = new AsyncBitmapDrawableLoader(this, "AsyncImageActivity",
 *           BitmapUtils.decodeFromResource(getResources(), R.mipmap.async_image_null), new MyBitmapLoaderImplementor())
 *           .setRamCache(0.15f)//缓存占15%内存(与AsyncBitmapLoader不同之处)
 *           .setDiskCache(50, 5, 25)//磁盘缓存50M, 5线程磁盘加载, 等待队列容量25
 *           .setNetLoad(3, 25)//3线程网络加载, 等待队列容量25
 *           .setImageQuality(Bitmap.CompressFormat.JPEG, 70)//设置保存格式和质量
 *           //.setDiskCacheInner()//强制使用内部储存
 *           //.setDuplicateLoadEnable(true)//允许相同图片同时加载(慎用)
 *           //.setLogger(getLogger())//打印日志
 *           .open();//启动(必须)
 *   } catch (IOException e) {
 *      //磁盘缓存打开失败的情况, 可提示客户磁盘已满等
 *   }
 * <br/>
 *      [上述代码说明]:<br/>
 *      通用设置说明省略<br/>
 *      注意AsyncBitmapDrawableLoader中回收站是强制关闭的,因为配合AsyncBitmapDrawable使用无需回收站,
 *      同样也无需unused方法<br/>
 *      构造函数第三个参数loadingBitmap在AsyncBitmapDrawableLoader.destroy中销毁,因此只需BitmapUtils
 *      解析即可,无需考虑手工回收.<br/>
 * <br/>
 * <Br/>
 * -------------------加载器使用----------------<br/>
 * <br/>
 * 1.load <br/>
 *      加载,立即返回AsyncBitmapDrawable,直接赋给ImageView即可,AsyncBitmapDrawable会自动处理后续工作
 *      (显示图片,防止异常等).注意切不可获取AsyncBitmapDrawable中的Bitmap直接使用.<Br/>
 * 2.get <br/>
 *      从内存缓冲获取AsyncBitmapDrawable,若不存在返回null<br/>
 * 3.unused [重要] <br/>
 *      当图片不再显示时,及时unused有助于减少不必要的加载,节省流量,使需要显示的图片尽快加载.<br/>
 * 4.destroy [重要] <br/>
 *      清除全部图片及加载任务,通常在Activity.onDestroy中调用<br/>
 * <Br/>
 * -------------------注意事项----------------<br/>
 * <br/>
 * 1.AsyncBitmapDrawableLoader不需要内存缓存回收站,与AsyncBitmapLoader不同.<br/>
 * 2.ListView等View复用的场合,应先unused废弃原AsyncBitmapDrawable,再设置新的:
 *      AsyncBitmapDrawable drawable = (AsyncBitmapDrawable) holder.imageView.getDrawable();
 *      if (drawable != null)
 *          drawable.unused();
 *      holder.imageView.setImageDrawable(asyncBitmapDrawableLoader.load(url, reqWidth, reqHeight));
 * <br/>
 * ****************************************************************<br/>
 * * * * * 名词解释:<br/>
 * ****************************************************************<br/>
 * <Br/>
 * url:<br/>
 *      AsyncBitmapLoader中每个位图资源都由url唯一标识, url在AsyncBitmapLoader内部
 *      将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
 *      这个cacheKey标识唯一的资源<br/>
 * <Br/>
 * 缓存区:<br/>
 *      缓存区满后, 会清理最早创建或最少使用的Bitmap. 若被清理的Bitmap已被置为unused不再
 *      使用状态, 则Bitmap会被立刻回收(recycle()), 否则会进入回收站等待被unused. 因此, 必须及时
 *      使用unused(url)方法将不再使用的Bitmap置为unused状态, 使得Bitmap尽快被回收.<br/>
 * <Br/>
 * 回收站:<br/>
 *      用于存放因缓存区满被清理,但仍在被使用的Bitmap(未被标记为unused).<br/>
 *      显示中的Bitmap可能因为被引用(get)早,判定为优先度低而被清理出缓存区,绘制时出现"trying to use a
 *      recycled bitmap"异常,设置合适大小的回收站有助于减少此类事件发生.但回收站的使用会增加内存消耗,
 *      请适度设置.若设置为0禁用,缓存区清理时无视unused状态一律做回收(Bitmap.recycle)处理,且不进入回收站!!<br/>
 *      AsyncBitmapDrawableLoader中禁用.<br/>
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
 * 2.当一个页面中需要同时加载相同图片(相同url).<br/>
 *      当同时加载相同图片时,若发现只加载出一个,其余的都被取消(onLoadCanceled).<br/>
 *      解决方案:<br/>
 *      尝试设置setDuplicateLoadEnable(true);<Br/>
 * <Br/>
 * 3.网络加载失败,需要重新加载.<br/>
 *      推荐方案:<br/>
 *      1).定时刷新UI(1-5s),以此触发显示中的图片重新加载.这样做的优点是,只重新加载显示中的图片.
 *          适合ListView/GridView等View复用/适配器模式的场合,图片加载在适配器中实现,定时对适配
 *          器(Adapter)进行刷新,即可达到重新加载的目的.<br/>
 * <Br/>
 * <Br/>
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

    void load(AsyncBitmapDrawable asyncBitmapDrawable){
        if (asyncBitmapDrawable == null)
            return;
        super.load(asyncBitmapDrawable.getUrl(), asyncBitmapDrawable.getReqWidth(), asyncBitmapDrawable.getReqHeight(), null, asyncBitmapDrawable);
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

    /**
     * 尝试取消加载任务<br/>
     * <br/>
     * [[[注意]]] 建议使用AysncBitmapDrawable.unused()
     * <br/>
     * 当图片不再显示时,及时unused有助于减少不必要的加载,节省流量,使需要显示的图片尽快加载.
     * 例如:ListView高速滑动时,中间很多项是来不及加载的,也无需显示图片,及时取消加载任务,可
     * 以跳过中间项的加载,使滚动停止后需要显示的项尽快加载出来.<br/>
     * <br/>
     * <br/>
     * URL::<Br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     *
     * @param url 图片URL地址
     */
    @Override
    @Deprecated
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
