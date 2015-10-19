package sviolet.turquoise.utils.bitmap.loader.enhanced;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import java.io.IOException;
import java.lang.ref.WeakReference;

import sviolet.turquoise.utils.Logger;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoader;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoaderImplementor;

/**
 * AsyncBitmapDrawableLoader<br/>
 * AsyncBitmapDrawable双缓存网络异步加载器<br/>
 * <br/>
 * 加载AsyncBitmapDrawable, 使用简单, 但兼容性较差, 新API可能会出错. <br/>
 * AsyncBitmapDrawable支持加载图设置,异步显示,出错重加载,回收防崩溃.<br/>
 * @see AsyncBitmapDrawable
 * <br/>
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
 *           .setRamCache(0.15f)//缓存占15%内存(与BitmapLoader不同之处)
 *           .setDiskCache(50, 5, 25)//磁盘缓存50M, 5线程磁盘加载, 等待队列容量25
 *           .setNetLoad(3, 25)//3线程网络加载, 等待队列容量25
 *           .setImageQuality(Bitmap.CompressFormat.JPEG, 70)//设置保存格式和质量
 *           //.setDiskCacheInner()//强制使用内部储存
 *           //.setDuplicateLoadEnable(true)//允许相同图片同时加载(慎用)
 *           //.setLogger(getLogger())//打印日志
 *           .setAnimationDuration(500)//AsyncBitmapDrawable由浅及深显示效果持续时间
 *           .setReloadTimesMax(2)//设置图片加载失败重新加载次数限制
 *           .open();//启动(必须)
 *   } catch (IOException e) {
 *      //磁盘缓存打开失败的情况, 可提示客户磁盘已满等
 *   }
 * <br/>
 *      [上述代码说明]:<br/>
 *      通用设置说明省略, 参考BitmapLoader<br/>
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
 * 3.AsyncBitmapDrawable.unused [重要] <br/>
 *      当图片不再显示时,及时unused有助于减少不必要的加载,节省流量,使需要显示的图片尽快加载.<br/>
 * 4.destroy [重要] <br/>
 *      清除全部图片及加载任务,通常在Activity.onDestroy中调用<br/>
 * <Br/>
 * -------------------注意事项----------------<br/>
 * <br/>
 * 1.AsyncBitmapDrawableLoader不需要内存缓存回收站,因为AsyncBitmapDrawable防回收崩溃,
 *      与BitmapLoader不同.<br/>
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
 *      BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
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
 *      使用AsyncBitmapDrawableLoader无需特殊处理, AsyncBitmapDrawable自带重新加载功能.<br/>
 *      推荐方案:<br/>
 *      1).在网络加载失败(failed)时, 有限次地重新加载, 不应重新加载unused的任务.<br/>
 * <Br/>
 * <Br/>
 * Created by S.Violet on 2015/7/3.
 */
public class AsyncBitmapDrawableLoader {

    /**
     * 内部bitmapLoader<Br/>
     * 该类由代理方式实现<br/>
     */
    private BitmapLoader bitmapLoader;

    private WeakReference<Resources> resources;

    private Bitmap loadingBitmap;//加载状态的图片

    private int animationDuration = 500;//AsyncBitmapDrawable图片由浅及深显示的动画持续时间

    private int reloadTimesMax = 2;//图片加载失败重新加载次数限制

    /**
     * 内存缓存区默认0.125f
     *
     * @param context 上下文
     * @param diskCacheName 磁盘缓存目录名
     * @param loadingBitmap 加载时的图片(可为空, AsyncBitmapDrawableLoader.destroy时会回收该Bitmap)
     * @param implementor 实现器
     */
    public AsyncBitmapDrawableLoader(Context context, String diskCacheName, Bitmap loadingBitmap, BitmapLoaderImplementor implementor) {
        bitmapLoader = new BitmapLoader(context, diskCacheName, implementor);
        this.loadingBitmap = loadingBitmap;
        this.resources = new WeakReference<Resources>(context.getResources());
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
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
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
        bitmapLoader.load(asyncBitmapDrawable.getUrl(), asyncBitmapDrawable.getReqWidth(), asyncBitmapDrawable.getReqHeight(), null, asyncBitmapDrawable);
    }

    /**
     * 从内存缓存中取AsyncBitmapDrawable, 若不存在或已被回收, 则返回null<br/>
     * AsyncBitmapDrawable为异步的BitmapDrawable, 在加载时会显示加载图, 加载成功后会自动刷新为目标图片<br/>
     * ImageView.setImageDrawable()方法直接设置图片使用, 请勿获取其中的Bitmap使用.<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
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
        Bitmap bitmap = bitmapLoader.get(url);
        if (bitmap == null)
            return null;
        return new AsyncBitmapDrawable(url, reqWidth, reqHeight, this, bitmap);
    }

    /**
     * 尝试取消加载任务<br/>
     * <br/>
     * [[[注意]]] 使用AysncBitmapDrawable.unused()
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
    void unused(String url) {
        bitmapLoader.unused(url);
    }

    /**
     * [重要]将所有资源回收销毁, 请在Activity.onDestroy()时调用该方法<br/>
     * 1.销毁内部持有的BitmapLoader<br/>
     * 2.销毁加载图(loadingBitmap)<br/>
     */
    public void destroy() {
        bitmapLoader.destroy();

        //回收加载图
        if (loadingBitmap != null && !loadingBitmap.isRecycled()){
            loadingBitmap.recycle();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //GETTER
    ////////////////////////////////////////////////////////////////////////

    /**
     * [废弃]获得内部BitmapLoader<br/>
     * AsyncBitmapDrawableLoader不支持BitmapLoader共享<br/>
     */
//    public BitmapLoader getBitmapLoader(){
//        return bitmapLoader;
//    }

    Bitmap getLoadingBitmap(){
        return loadingBitmap;
    }

    Resources getResources(){
        if (resources != null){
            return resources.get();
        }
        return null;
    }

    int getAnimationDuration(){
        return animationDuration;
    }

    int getReloadTimesMax(){
        return reloadTimesMax;
    }

    ////////////////////////////////////////////////////////////////////////
    // 配置
    ////////////////////////////////////////////////////////////////////////

    /**
     * @param diskCacheSizeMib 磁盘缓存最大容量, 默认10, 单位Mb
     * @param diskLoadConcurrency 磁盘加载任务并发量, 默认5
     * @param diskLoadVolume 磁盘加载等待队列容量, 默认10
     */
    public AsyncBitmapDrawableLoader setDiskCache(int diskCacheSizeMib, int diskLoadConcurrency, int diskLoadVolume) {
        bitmapLoader.setDiskCache(diskCacheSizeMib, diskLoadConcurrency, diskLoadVolume);
        return this;
    }

    /**
     * 设置磁盘缓存路径为内部储存<br/>
     * 若不设置, 则优先选择外部储存, 当外部储存不存在时使用内部储存
     */
    public AsyncBitmapDrawableLoader setDiskCacheInner() {
        bitmapLoader.setDiskCacheInner();
        return this;
    }

    /**
     * 相同图片同时加载<br/>
     * <br/>
     * ----------------------------------------------<br/>
     * <br/>
     * false:禁用(默认)<Br/>
     * 适用于大多数场合,同一个页面不会出现相同图片(相同的url)的情况.<br/>
     * <br/>
     * 为优化性能,同一张图片并发加载时,采用TQueue的同名任务取消策略,取消多余的并发任务,只保留一个任务完成.
     * 因此,同一个页面同时加载同一张图片时,最终只有一张图片完成加载,其他会被取消.在使用ListView等场合时,
     * 可以避免在频繁滑动时重复执行加载,以优化性能.<br/>
     * <br/>
     * ----------------------------------------------<br/>
     * true:启用<br/>
     * 适用于同一个页面会出现相同图片(相同的url)的场合,性能可能会下降,不适合高并发加载,不适合ListView
     * 等View复用控件的场合.<br/>
     * <br/>
     * 为了满足在一个屏幕中同时显示多张相同图片(相同的url)的情况,在同一张图片并发加载时,采用TQueue的
     * 同名任务跟随策略,其中一个任务执行,其他同名任务等待其完成后,同时回调OnLoadCompleteListener,并传入
     * 同一个结果(Bitmap).这种方式在高并发场合,例如:频繁滑动ListView,任务会持有大量的对象用以回调,而绝大
     * 多数的View已不再显示在屏幕上.<Br/>
     *
     */
    public AsyncBitmapDrawableLoader setDuplicateLoadEnable(boolean duplicateLoadEnable) {
        bitmapLoader.setDuplicateLoadEnable(duplicateLoadEnable);
        return this;
    }

    /**
     * 设置缓存文件的图片保存格式和质量<br/>
     * 默认Bitmap.CompressFormat.JPEG, 70
     *
     * @param format 图片格式
     * @param quality 图片质量 0-100
     */
    public AsyncBitmapDrawableLoader setImageQuality(Bitmap.CompressFormat format, int quality) {
        bitmapLoader.setImageQuality(format, quality);
        return this;
    }

    /**
     * 设置日志打印器, 用于输出调试日志, 不设置则不输出日志
     */
    public AsyncBitmapDrawableLoader setLogger(Logger logger) {
        bitmapLoader.setLogger(logger);
        return this;
    }

    /**
     * @param netLoadConcurrency 网络加载任务并发量, 默认3
     * @param netLoadVolume 网络加载等待队列容量, 默认10
     */
    public AsyncBitmapDrawableLoader setNetLoad(int netLoadConcurrency, int netLoadVolume) {
        bitmapLoader.setNetLoad(netLoadConcurrency, netLoadVolume);
        return this;
    }

    /**
     * 缓存区:缓存区满后, 会清理被标记为unused/最早创建/最少使用的Bitmap. 并立刻回收(recycle()),
     * 及时地使用unused(url)方法将不再使用的Bitmap置为unused状态, 可以使得Bitmap尽快被回收.
     * <br/>
     * AsyncBitmapDrawableLoader禁用了BitmapCache回收站, 因为AsyncBitmapDrawable防回收崩溃<br/>
     *
     * @param ramCacheSizePercent 内存缓存区占用应用可用内存的比例 (0, 1], 默认值0.125f
     */
    public AsyncBitmapDrawableLoader setRamCache(float ramCacheSizePercent) {
        bitmapLoader.setRamCache(ramCacheSizePercent, 0);
        return this;
    }

    /**
     * 设置AsyncBitmapDrawable由浅及深显示动画效果持续时间
     * @param duration 持续时间 ms 默认500
     */
    public AsyncBitmapDrawableLoader setAnimationDuration(int duration){
        if (duration < 0)
            duration = 0;
        this.animationDuration = duration;
        return this;
    }

    /**
     * 设置图片加载失败后重新加载次数限制<Br/>
     *
     * @param times 重新加载次数限制 默认值2
     */
    public AsyncBitmapDrawableLoader setReloadTimesMax(int times){
        this.reloadTimesMax = times;
        return this;
    }

    /**
     * [重要]启用BitmapLoader, 在实例化并设置完BitmapLoader后, 必须调用此
     * 方法, 开启磁盘缓存/内存缓存. 否则会抛出异常.<Br/>
     *
     * @throws IOException 磁盘缓存启动失败抛出异常
     */
    public AsyncBitmapDrawableLoader open() throws IOException {
        bitmapLoader.open();
        return this;
    }
}
