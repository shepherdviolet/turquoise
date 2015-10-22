package sviolet.turquoise.utils.bitmap.loader.enhanced;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;

import sviolet.turquoise.utils.Logger;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoader;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoaderImplementor;
import sviolet.turquoise.utils.cache.BitmapCache;
import sviolet.turquoise.view.drawable.SafeBitmapDrawable;

/**
 * SimpleBitmapLoader<Br/>
 * 便捷图片双缓存网络异步加载器<br/>
 * <br/>
 * 代理强化BitmapLoader, 传入url和View, 自动完成异步加载显示, 错误重新加载, 默认图显示, 淡入效果等.<br/>
 * 简易的"防回收崩溃",必需配合BitmapLoader回收站使用.<br/>
 * <br/>
 * ****************************************************************<br/>
 * * * * * SimpleBitmapLoader使用说明:<br/>
 * ****************************************************************<br/>
 * <br/>
 * -------------------初始化设置----------------<br/>
 * <br/>
 * 1.实现接口BitmapLoaderImplementor<br/>
 * 2.实例化SimpleBitmapLoader(Context,String,Bitmap,BitmapLoaderImplementor) <br/>
 * 3.设置参数:<br/>
 *   try {
 *       mSimpleBitmapLoader = new SimpleBitmapLoader(this, "bitmap", loadingBitmap, new MyBitmapLoaderImplementor())
 *          .setRamCache(0.125f, 0.125f)//设置内存缓存大小,启用回收站
 *          .setDiskCache(50, 5, 15)//设置磁盘缓存容量50M, 磁盘加载并发数5, 等待队列15
 *          .setNetLoad(3, 15)//设置网络加载并发数3, 等待队列15
 *          .setImageQuality(Bitmap.CompressFormat.JPEG, 70)//设置磁盘缓存保存格式和质量
 *          //.setDiskCacheInner()//强制使用内部储存
 *          //.setDuplicateLoadEnable(true)//允许相同图片同时加载(慎用)
 *          //.setWipeOnNewVersion()//当APP更新时清空磁盘缓存
 *          //.setLogger(getLogger())
 *          .setAnimationDuration(400)//设置图片淡入动画持续时间400ms
 *          .setReloadTimesMax(2)//设置图片加载失败重新加载次数限制
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
 *      设置日志打印器后, BitmapLoader会打印出一些日志用于调试, 例如内存缓存使用
 *      情况, 图片加载日志等, 可根据日志调试/选择上述参数的值.<br/>
 * <br/>
 * <br/>
 * -------------------加载器使用----------------<br/>
 * <br/>
 * 1.load <br/>
 *      加载图片,自动完成异步加载显示,错误重新加载,默认图显示,淡入效果等.<Br/>
 *      加载任务会绑定在控件TAG上(View.setTag()),请勿给控件设置自定义Tag,会导致加载失败.<Br/>
 *      加载方法会弃用(unused)先前绑定在控件上的加载任务.请注意.<br/>
 * 2.unused [重要] <br/>
 *      不再使用的图片须及时用该方法废弃,尤其是大量图片的场合,未被废弃(unused)的图片
 *      将不会被BitmapLoader回收.请参看"名词解释".<br/>
 *      该方法能取消加载任务,有助于减少不必要的加载,节省流量,使需要显示的图片尽快加载.<br/>
 * 3.destroy [重要] <br/>
 *      清除全部图片及加载任务,通常在Activity.onDestroy中调用<br/>
 * 4.reduce <br/>
 *      强制清空内存缓存中不再使用(unused)的图片.<br/>
 *      用于暂时减少缓存的内存占用,请勿频繁调用.<br/>
 *      通常是内存紧张的场合, 可以在Activity.onStop()中调用, Activity暂时不显示的情况下,
 *      将缓存中已被标记为unused的图片回收掉, 减少内存占用. 但这样会使得重新显示时, 加载
 *      变慢(需要重新加载).<br/>
 * 5.cancelAllTasks <br/>
 *      强制取消所有加载任务.不影响缓存,不弃用图片.<br/>
 *      用于BitmapLoader未销毁的情况下, 结束网络访问.<br/>
 * <Br/>
 * -------------------注意事项----------------<br/>
 * <br/>
 * 1.该加载器会占用控件(View)的Tag用于绑定任务, 若控件设置另外的Tag(View.setTag())
 *      将会无法正常使用<br/>
 * 2.ListView等View复用的场合,可省略unused操作.<br/>
 *      load方法会先弃用(unused)先前绑定在控件上的加载任务.<br/>
 * 3.SimpleBitmapLoader仅实现简易的"防回收崩溃",必须设置回收站.<br/>
 *      采用SafeBitmapDrawable,当显示中的Bitmap被意外回收时,会绘制空白,但不会重新加载图片,
 *      与AsyncBitmapDrawableLoader不同.配合回收站使用效果较好.<br/>
 *      @see SafeBitmapDrawable
 * <Br/>
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
 *      使用SimpleBitmapLoader/AsyncBitmapDrawableLoader无需特殊处理, 它们自带重新加载功能.<br/>
 *      推荐方案:<br/>
 *      1).在网络加载失败(failed)时, 有限次地重新加载, 不应重新加载unused的任务.<br/>
 * <Br/>
 * 4.Exception::[SimpleBitmapLoaderTask]don't use View.setTag() when view load by SimpleBitmapLoader!!<Br/>
 *      使用SimpleBitmapLoader加载控件时, 控件禁止使用View.setTag()自行设置TAG,
 *      因为SimpleBitmapLoader会把SimpleBitmapLoaderTask通过setTag()绑定在控件上!<Br/>
 * <Br/>
 * <Br/>
 * <Br/>
 * Created by S.Violet on 2015/10/19.
 */
public class SimpleBitmapLoader {

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
     * @param context 上下文
     * @param diskCacheName 磁盘缓存目录名
     * @param loadingBitmap 加载时的图片(可为空, SimpleBitmapLoader.destroy时会回收该Bitmap)
     * @param implementor 实现器
     */
    public SimpleBitmapLoader(Context context, String diskCacheName, Bitmap loadingBitmap, BitmapLoaderImplementor implementor) {
        bitmapLoader = new BitmapLoader(context, diskCacheName, implementor);
        this.loadingBitmap = loadingBitmap;
        this.resources = new WeakReference<Resources>(context.getResources());
    }

    /*********************************************************************
     * public
     */

    /**
     * 异步加载ImageView<br/>
     * <br/>
     * 注意:<Br/>
     * 1.该方法会弃用(unused)先前绑定在控件上的加载任务<br/>
     * 2.切勿自行给控件设置TAG(View.setTag()), 会无法加载!<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param url url
     * @param reqWidth 需求宽度
     * @param reqHeight 需求高度
     * @param view 被加载的控件(禁止使用View.setTag())
     */
    public void load(String url, int reqWidth, int reqHeight, ImageView view){
        new ImageViewLoaderTask(url, reqWidth, reqHeight, this, view);
    }

    /**
     * 异步加载背景图<br/>
     * <br/>
     * 注意:<Br/>
     * 1.该方法会弃用(unused)先前绑定在控件上的加载任务<br/>
     * 2.切勿自行给控件设置TAG(View.setTag()), 会无法加载!<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     *
     * @param url url
     * @param reqWidth 需求宽度
     * @param reqHeight 需求高度
     * @param view 被加载的控件(禁止使用View.setTag())
     */
    public void loadBackground(String url, int reqWidth, int reqHeight, View view){
        new BackgroundLoaderTask(url, reqWidth, reqHeight, this, view);
    }

    /**
     * [重要]弃用图片并取消加载任务<Br/>
     * <br/>
     *      不再使用的图片须及时用该方法废弃,尤其是大量图片的场合,未被废弃(unused)的图片
     *      将不会被BitmapLoader回收.请参看SimpleBitmapLoader"名词解释".<br/>
     *      该方法能取消加载任务,有助于减少不必要的加载,节省流量,使需要显示的图片尽快加载.<br/>
     */
    public void unused(View view){
        if (view == null)
            return;
        synchronized (view) {
            //取tag
            Object tag = view.getTag();
            //若tag为SimpleBitmapLoaderTask
            if (tag instanceof SimpleBitmapLoaderTask) {
                //弃用任务
                ((SimpleBitmapLoaderTask) tag).unused();
                //解除绑定
                view.setTag(null);
            }
        }
    }

    /**
     * [特殊]强制清空内存缓存中不再使用(unused)的图片<br/>
     * <br/>
     * 用于暂时减少缓存的内存占用,请勿频繁调用.<br/>
     * 通常是内存紧张的场合, 可以在Activity.onStop()中调用, Activity暂时不显示的情况下,
     * 将缓存中已被标记为unused的图片回收掉, 减少内存占用. 但这样会使得重新显示时, 加载
     * 变慢(需要重新加载).<br/>
     */
    public void reduce(){
        bitmapLoader.reduce();
    }

    /**
     * [特殊]强制取消所有加载任务<br/>
     * <br/>
     * 仅取消加载任务,不影响缓存,不弃用图片<br/>
     */
    public void cancelAllTasks(){
        bitmapLoader.cancelAllTasks();
    }

    /**
     * [重要]将所有资源回收销毁, 建议在Activity.onDestroy()时调用该方法<br/>
     * 1.销毁内部持有的BitmapLoader<br/>
     * 2.销毁加载图(loadingBitmap)<br/>
     */
    public void destroy(){
        bitmapLoader.destroy();

        //回收加载图
        if (loadingBitmap != null && !loadingBitmap.isRecycled()){
            loadingBitmap.recycle();
        }
    }

    /**
     * 是否已被销毁(不可用状态)
     */
    public boolean isDestroyed(){
        return bitmapLoader.isDestroyed();
    }

    /**
     * 获得内存缓存器
     */
    public BitmapCache getBitmapCache(){
        return bitmapLoader.getBitmapCache();
    }

    /*********************************************************************
     * protected
     */

    protected void load(SimpleBitmapLoaderTask task){
        if (task == null)
            return;
        bitmapLoader.load(task.getUrl(), task.getReqWidth(), task.getReqHeight(), null, task);
    }

    protected void unused(String url){
        bitmapLoader.unused(url);
    }

    protected Resources getResources(){
        if (resources != null)
            return resources.get();
        return null;
    }

    protected int getAnimationDuration(){
        return animationDuration;
    }

    protected Bitmap getLoadingBitmap(){
        if (loadingBitmap != null && loadingBitmap.isRecycled()){
            return null;
        }
        return loadingBitmap;
    }

    public int getReloadTimesMax() {
        return reloadTimesMax;
    }

    protected Logger getLogger(){
        return bitmapLoader.getLogger();
    }

    /**************************************************************
     * SETTINGS
     */

    /**
     * @param diskCacheSizeMib 磁盘缓存最大容量, 默认10, 单位Mb
     * @param diskLoadConcurrency 磁盘加载任务并发量, 默认5
     * @param diskLoadVolume 磁盘加载等待队列容量, 默认10
     */
    public SimpleBitmapLoader setDiskCache(int diskCacheSizeMib, int diskLoadConcurrency, int diskLoadVolume) {
        bitmapLoader.setDiskCache(diskCacheSizeMib, diskLoadConcurrency, diskLoadVolume);
        return this;
    }

    /**
     * 设置磁盘缓存路径为内部储存<br/>
     * 若不设置, 则优先选择外部储存, 当外部储存不存在时使用内部储存
     */
    public SimpleBitmapLoader setDiskCacheInner() {
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
    public SimpleBitmapLoader setDuplicateLoadEnable(boolean duplicateLoadEnable) {
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
    public SimpleBitmapLoader setImageQuality(Bitmap.CompressFormat format, int quality) {
        bitmapLoader.setImageQuality(format, quality);
        return this;
    }

    /**
     * 设置日志打印器, 用于输出调试日志, 不设置则不输出日志
     */
    public SimpleBitmapLoader setLogger(Logger logger) {
        bitmapLoader.setLogger(logger);
        return this;
    }

    /**
     * @param netLoadConcurrency 网络加载任务并发量, 默认3
     * @param netLoadVolume 网络加载等待队列容量, 默认10
     */
    public SimpleBitmapLoader setNetLoad(int netLoadConcurrency, int netLoadVolume) {
        bitmapLoader.setNetLoad(netLoadConcurrency, netLoadVolume);
        return this;
    }

    /**
     * 缓存区:缓存区满后, 会清理最早创建或最少使用的Bitmap. 若被清理的Bitmap已被置为unused不再
     * 使用状态, 则Bitmap会被立刻回收(recycle()), 否则会进入回收站等待被unused. 因此, 必须及时
     * 使用unused(url)方法将不再使用的Bitmap置为unused状态, 使得Bitmap尽快被回收.
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
     * @param ramCacheSizePercent 内存缓存区占用应用可用内存的比例 (0, 1], 默认值0.125f
     * @param ramCacheRecyclerSizePercent 内存缓存回收站占用应用可用内存的比例 [0, 1], 设置为0禁用回收站, 默认值0.125f
     */
    public SimpleBitmapLoader setRamCache(float ramCacheSizePercent, float ramCacheRecyclerSizePercent){
        bitmapLoader.setRamCache(ramCacheSizePercent, ramCacheRecyclerSizePercent);
        return this;
    }

    /**
     * 设置AsyncBitmapDrawable由浅及深显示动画效果持续时间
     * @param duration 持续时间 ms 默认500
     */
    public SimpleBitmapLoader setAnimationDuration(int duration){
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
    public SimpleBitmapLoader setReloadTimesMax(int times){
        this.reloadTimesMax = times;
        return this;
    }

    /**
     * 设置App更新时清空磁盘缓存<br/>
     * 默认:不清空缓存<br/>
     * <br/>
     * 当应用versionCode发生变化时, 会清空磁盘缓存. 注意是versionCode, 非versionName.<br/>
     */
    public SimpleBitmapLoader setWipeOnNewVersion(){
        bitmapLoader.setWipeOnNewVersion();
        return this;
    }

    /**
     * [重要]启用BitmapLoader, 在实例化并设置完BitmapLoader后, 必须调用此
     * 方法, 开启磁盘缓存/内存缓存. 否则会抛出异常.<Br/>
     *
     * @throws IOException 磁盘缓存启动失败抛出异常
     */
    public SimpleBitmapLoader open() throws IOException {
        bitmapLoader.open();
        return this;
    }

}
