package sviolet.turquoise.utils.bitmap;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.enhance.utils.Logger;
import sviolet.turquoise.model.queue.TQueue;
import sviolet.turquoise.model.queue.TTask;
import sviolet.turquoise.utils.cache.DiskLruCache;
import sviolet.turquoise.utils.sys.ApplicationUtils;
import sviolet.turquoise.utils.sys.DirectoryUtils;

/**
 * [抽象类]图片双缓存网络异步加载器<br/>
 * <br/>
 * Bitmap内存缓存+磁盘缓存+网络加载+防OOM<br/>
 * <br/>
 * AsyncBitmapLoader中每个位图资源都由url和key共同标识, url和key在AsyncBitmapLoader内部
 * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
 * 这个cacheKey标识唯一的资源<br/>
 * <Br/>
 * ****************************************************************<br/>
 * [使用说明]<br/>
 * 1.实现接口AsyncBitmapLoader.Implementor -> BitmapLoaderImplementor <br/>
 * 2.实例化AsyncBitmapLoader(Context,String,AsyncBitmapLoader.Implementor) <br/>
 * 3.setRamCache/setDiskCache/setNetLoad/setLogger设置参数(可选) <br/>
 * 4.open() 启用AsyncBitmapLoader(必须, 否则抛异常)<br/>
 * <br/>
 * [代码示例]:<Br/>
    private AsyncBitmapLoader mAsyncBitmapLoader;
    try {
        mAsyncBitmapLoader = new AsyncBitmapLoader(this, "bitmap", new BitmapLoaderImplementor())
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
 * <Br/>
 * Tips::<br/>
 * <br/>
 * 1.当一个页面中需要同时加载相同图片,却发现只加载出一个,其余的都被取消(onLoadCanceled).
 *   尝试设置setDuplicateLoadEnable(true);<Br/>
 * <br/>
 * <br/>
 * ****************************************************************<br/>
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
 * Created by S.Violet on 2015/7/3.
 */
public class AsyncBitmapLoader {

    private CachedBitmapUtils mCachedBitmapUtils;//带缓存的Bitmap工具
    private DiskLruCache mDiskLruCache;//磁盘缓存器

    private TQueue mDiskCacheQueue;//磁盘缓存加载队列
    private TQueue mNetLoadQueue;//网络加载队列

    //SETTINGS//////////////////////////////////////////////

    private Context context;//(必须)
    private String diskCacheName;//磁盘缓存名(必须)
    private Implementor implementor;//实现器(必须)
    private long diskCacheSize = 1024 * 1024 * 10;//磁盘缓存大小(Mb)
    private float ramCacheSizePercent = 0.125f;//内存缓存大小(占应用可用内存比例)
    private float ramCacheRecyclerSizePercent = 0.125f;//内存缓存回收站大小(占应用可用内存比例)
    private int diskLoadConcurrency = 5;//磁盘加载任务并发量
    private int diskLoadVolume = 10;//磁盘加载等待队列容量
    private int netLoadConcurrency = 3;//网络加载任务并发量
    private int netLoadVolume = 10;//网络加载等待队列容量
    private Bitmap.CompressFormat imageFormat = Bitmap.CompressFormat.JPEG;//缓存图片保存格式
    private int imageQuality = 100;//缓存图片保存质量
    private int keyConflictPolicy = TQueue.KEY_CONFLICT_POLICY_CANCEL;//TQueue同名任务冲突策略
    private File cacheDir;//缓存路径
    private Logger logger;//日志打印器

    /**
     * @param context 上下文
     * @param diskCacheName 磁盘缓存目录名
     * @param implementor 实现器
     */
    public AsyncBitmapLoader(Context context, String diskCacheName, Implementor implementor) {
        if (implementor == null){
            throw new RuntimeException("[AsyncBitmapLoader]implementor is null !!", new NullPointerException());
        }
        this.context = context;
        this.diskCacheName = diskCacheName;
        this.implementor = implementor;
        cacheDir = DirectoryUtils.getCacheDir(context, diskCacheName);
    }

    /**
     * @param netLoadConcurrency 网络加载任务并发量, 默认3
     * @param netLoadVolume 网络加载等待队列容量, 默认10
     */
    public AsyncBitmapLoader setNetLoad(int netLoadConcurrency, int netLoadVolume){
        this.netLoadConcurrency = netLoadConcurrency;
        this.netLoadVolume = netLoadVolume;
        return this;
    }

    /**
     * @param diskCacheSizeMib 磁盘缓存最大容量, 默认10, 单位Mb
     * @param diskLoadConcurrency 磁盘加载任务并发量, 默认5
     * @param diskLoadVolume 磁盘加载等待队列容量, 默认10
     */
    public AsyncBitmapLoader setDiskCache(int diskCacheSizeMib, int diskLoadConcurrency, int diskLoadVolume){
        this.diskCacheSize = 1024L * 1024L * diskCacheSizeMib;
        this.diskLoadConcurrency = diskLoadConcurrency;
        this.diskLoadVolume = diskLoadVolume;
        return this;
    }

    /**
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
     * @param ramCacheSizePercent 内存缓存区占用应用可用内存的比例 (0, 1]
     * @param ramCacheRecyclerSizePercent 内存缓存回收站占用应用可用内存的比例 [0, 1], 使用SafeBitmapDrawableFactory时设置为0禁用回收站
     */
    public AsyncBitmapLoader setRamCache(float ramCacheSizePercent, float ramCacheRecyclerSizePercent){
        this.ramCacheSizePercent = ramCacheSizePercent;
        this.ramCacheRecyclerSizePercent = ramCacheRecyclerSizePercent;
        return this;
    }

    /**
     * 设置磁盘缓存路径为内部储存<br/>
     * 若不设置, 则优先选择外部储存, 当外部储存不存在时使用内部储存
     */
    public AsyncBitmapLoader setDiskCacheInner(){
        cacheDir = new File(DirectoryUtils.getInnerCacheDir(context).getAbsolutePath() + File.separator + diskCacheName);
        return this;
    }

    /**
     * 设置缓存文件的图片保存格式和质量<br/>
     * 默认Bitmap.CompressFormat.JPEG, 100
     *
     * @param format 图片格式
     * @param quality 图片质量 0-100
     */
    public AsyncBitmapLoader setImageQuality(Bitmap.CompressFormat format, int quality){
        this.imageFormat = format;
        this.imageQuality = quality;
        return this;
    }

    /**
     * 相同图片同时加载<br/>
     * <br/>
     * ----------------------------------------------<br/>
     * <br/>
     * false:禁用(默认)<Br/>
     * 适用于大多数场合,同一个页面不会出现相同图片(相同的url和key)的情况.<br/>
     * <br/>
     * 为优化性能,同一张图片并发加载时,采用TQueue的同名任务取消策略,取消多余的并发任务,只保留一个任务完成.
     * 因此,同一个页面同时加载同一张图片时,最终只有一张图片完成加载,其他会被取消.在使用ListView等场合时,
     * 可以避免在频繁滑动时重复执行加载,以优化性能.<br/>
     * <br/>
     * ----------------------------------------------<br/>
     * true:启用<br/>
     * 适用于同一个页面会出现相同图片(相同的url和key)的场合,性能可能会下降,不适合高并发加载,不适合ListView
     * 等View复用控件的场合.<br/>
     * <br/>
     * 为了满足在一个屏幕中同时显示多张相同图片(相同的url和key)的情况,在同一张图片并发加载时,采用TQueue的
     * 同名任务跟随策略,其中一个任务执行,其他同名任务等待其完成后,同时回调OnLoadCompleteListener,并传入
     * 同一个结果(Bitmap).这种方式在高并发场合,例如:频繁滑动ListView,任务会持有大量的对象用以回调,而绝大
     * 多数的View已不再显示在屏幕上.<Br/>
     *
     */
    public AsyncBitmapLoader setDuplicateLoadEnable(boolean duplicateLoadEnable){
        if (duplicateLoadEnable){
            keyConflictPolicy = TQueue.KEY_CONFLICT_POLICY_FOLLOW;
        }else{
            keyConflictPolicy = TQueue.KEY_CONFLICT_POLICY_CANCEL;
        }
        return this;
    }

    /**
     * 设置日志打印器, 用于输出调试日志, 不设置则不输出日志
     */
    public AsyncBitmapLoader setLogger(Logger logger) {
        this.logger = logger;
        if (mCachedBitmapUtils != null)
            mCachedBitmapUtils.getBitmapCache().setLogger(logger);
        return this;
    }

    /**
     * [重要]启用AsyncBitmapLoader, 在实例化并设置完AsyncBitmapLoader后, 必须调用此
     * 方法, 开启磁盘缓存/内存缓存. 否则会抛出异常.<Br/>
     *
     * @throws IOException 磁盘缓存启动失败抛出异常
     */
    public AsyncBitmapLoader open() throws IOException {
        this.mDiskLruCache = DiskLruCache.open(cacheDir, ApplicationUtils.getAppVersion(context), 1, diskCacheSize);
        this.mCachedBitmapUtils = new CachedBitmapUtils(context, ramCacheSizePercent, ramCacheRecyclerSizePercent);
        this.mDiskCacheQueue = new TQueue(true, diskLoadConcurrency).setVolumeMax(diskLoadVolume).waitCancelingTask(true).setKeyConflictPolicy(keyConflictPolicy);
        this.mNetLoadQueue = new TQueue(true, netLoadConcurrency).setVolumeMax(netLoadVolume).waitCancelingTask(true).setKeyConflictPolicy(keyConflictPolicy);
        if(logger != null)
            mCachedBitmapUtils.getBitmapCache().setLogger(logger);
        return this;
    }

    /******************************************
     * inner class
     */

    /**
     * 实现器<br/>
     * 1.实现cacheKey的生成规则<br/>
     * 2.实现网络加载图片<br/>
     * 3.实现异常处理<br/>
     */
    public interface Implementor{
        /**
         * [实现提示]:<br/>
         * 可以直接使用key值作为cacheKey, 也可以将url或者key进行摘要
         * 计算, 得到摘要值作为cacheKey, 根据实际情况实现.  <Br/>
         * AsyncBitmapLoader中每个位图资源都由url和key共同标识, url和key在AsyncBitmapLoader内部
         * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
         * 这个cacheKey标识唯一的资源<br/>
         *
         * @return 实现根据URL连接和指定Key, 计算并返回缓存Key
         */
        public String getCacheKey(String url, String key);

        /**
         * 实现根据url和key参数从网络下载图片数据, 依照需求尺寸reqWidth和reqHeight解析为合适大小的Bitmap,
         * 并调用结果容器resultHolder.set(Bitmap)方法将Bitmap返回, 若加载失败则set(null)<br/>
         * <br/>
         * 注意:<br/>
         * 1.网络请求注意做超时处理,否则任务可能会一直等待<br/>
         * 2.数据解析为Bitmap时,请根据需求尺寸reqWidth和reqHeight解析, 以节省内存<br/>
         * <br/>
         * 线程会阻塞等待,直到resultHolder.set(Bitmap)方法执行.若任务被cancel,阻塞也会被中断,且即使后续
         * 网络请求返回了Bitmap,也会被Bitmap.recycle().<br/>
         * <br/>
         * 无论同步还是异步的情况,均使用resultHolder.set(Bitmap)返回结果<br/>
         * 同步网络请求:<br/>
         *
         *      //网络加载代码
         *      ......
         *      resultHolder.set(bitmap);
         *
         * <br/>
         * 异步网络请求:<br/>
         *
         *      //异步处理的情况
         *      new Thread(new Runnable(){
         *          public void run() {
         *              //网络加载代码
         *              ......
         *              resultHolder.set(bitmap);
         *          }
         *      }).start();
         *
         *
         * @param url url
         * @param key key
         * @param reqWidth 请求宽度
         * @param reqHeight 请求高度
         * @param resultHolder 结果容器
         */
        public void loadFromNet(String url, String key, int reqWidth, int reqHeight, ResultHolder resultHolder);

        /**
         * 实现异常处理
         */
        public void onException(Throwable throwable);

        /**
         * 实现写入缓存文件时的异常处理, 通常只需要打印日志或提醒即可
         */
        public void onCacheWriteException(Throwable throwable);
    }

    /******************************************
     * public
     */

    /**
     * 加载图片, 加载成功后回调mOnLoadCompleteListener<br/>
     * 回调方法的params参数为此方法传入的params, 并非Bitmap<Br/>
     * 回调方法中使用Bitmap, 调用AsyncBitmapLoader.get()方法从内存缓存
     * 中取.<br/>
     * <br/>
     * AsyncBitmapLoader中每个位图资源都由url和key共同标识, url和key在AsyncBitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     *
     * @param url 图片URL地址
     * @param key 图片自定义key
     * @param reqWidth 需求宽度 px
     * @param reqHeight 需求高度 px
     * @param params 参数, 会带入mOnLoadCompleteListener回调方法
     * @param mOnLoadCompleteListener 回调监听器
     */
    public void load(String url, String key, int reqWidth, int reqHeight, Object params, OnLoadCompleteListener mOnLoadCompleteListener) {
        checkIsOpen();
        //计算缓存key
        String cacheKey = implementor.getCacheKey(url, key);
        if (logger != null) {
            logger.d("[AsyncBitmapLoader]load:start:  url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
        }
        //尝试内存缓存中取Bitmap
        Bitmap bitmap = mCachedBitmapUtils.getBitmap(cacheKey);
        if (bitmap != null && !bitmap.isRecycled()) {
            //缓存中存在直接回调:成功
            mOnLoadCompleteListener.onLoadSucceed(url, key, params, bitmap);
            if (logger != null) {
                logger.d("[AsyncBitmapLoader]load:succeed:  from:BitmapCache url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
            }
            return;
        }
        //若缓存中不存在, 加入磁盘缓存加载队列
        mDiskCacheQueue.put(cacheKey, new DiskCacheTask(url, key, reqWidth, reqHeight, mOnLoadCompleteListener).setParams(params));
    }

    /**
     * 从内存缓存中取Bitmap, 若不存在或已被回收, 则返回null<br/>
     * <br/>
     * AsyncBitmapLoader中每个位图资源都由url和key共同标识, url和key在AsyncBitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     *
     * @param url 图片URL地址
     * @param key 图片自定义key
     * @return 若不存在或已被回收, 则返回null
     */
    public Bitmap get(String url, String key) {
        checkIsOpen();
        //计算缓存key
        String cacheKey = implementor.getCacheKey(url, key);
        //尝试从内存缓存中取Bitmap
        Bitmap bitmap = mCachedBitmapUtils.getBitmap(cacheKey);
        if (bitmap != null && !bitmap.isRecycled()) {
            //若存在且未被回收, 返回Bitmap
            return bitmap;
        }
        //若不存在或已被回收, 返回null
        return null;
    }

    /**
     * [重要]将一个Bitmap标示为不再使用<Br/>
     * <br/>
     *  将一个Bitmap标记为不再使用, 缓存中的Bitmap不会被立即回收, 在内存不足时,
     * 会进行缓存清理, 清理时会将最早的被标记为unused的Bitmap.recycle()回收掉.
     * 已进入回收站的Bitmap会被立即回收.<br/>
     * <br/>
     * AsyncBitmapLoader中每个位图资源都由url和key共同标识, url和key在AsyncBitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     *
     * @param url 图片URL地址
     * @param key 图片自定义key
     */
    public void unused(String url, String key) {
        checkIsOpen();
        //计算缓存key
        String cacheKey = implementor.getCacheKey(url, key);
        //网络加载队列取消
        mNetLoadQueue.cancel(cacheKey);
        //磁盘缓存加载队列取消
        mDiskCacheQueue.cancel(cacheKey);
        //将位图标识为不再使用
        mCachedBitmapUtils.unused(cacheKey);
        if (logger != null) {
            logger.d("[AsyncBitmapLoader]unused:  url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
        }
    }

    /**
     * [重要]将所有资源回收销毁, 请在Activity.onDestroy()时调用该方法
     */
    public void destroy() {
        checkIsOpen();
        if (mNetLoadQueue != null) {
            mNetLoadQueue.destroy();
            mNetLoadQueue = null;
        }
        if (mDiskCacheQueue != null) {
            mDiskCacheQueue.destroy();
            mDiskCacheQueue = null;
        }
        if (mDiskLruCache != null) {
            try {
                mDiskLruCache.close();
                mDiskLruCache = null;
            } catch (IOException e) {
                implementor.onException(e);
            }
        }
        if (mCachedBitmapUtils != null) {
            mCachedBitmapUtils.recycleAll();
            mCachedBitmapUtils = null;
        }
        if (logger != null) {
            logger.d("[AsyncBitmapLoader]destroy");
        }
    }

    /**
     * [慎用]清除磁盘缓存数据<br/>
     * 若外部储存存在, 则清除外部储存的缓存, 否则清除内部储存的缓存<Br/>
     * <br/>
     * 注意:在该方法调用期间, 若对该磁盘缓存区进行读写操作, 可能会
     * 抛出异常. 请确保调用期间该磁盘缓存区不被使用.
     *
     * @param context context
     * @param diskCacheName 缓存目录名
     * @throws IOException
     */
    public static void wipeDiskCache(Context context, String diskCacheName) throws IOException {
        DiskLruCache.deleteContents(DirectoryUtils.getCacheDir(context, diskCacheName));
    }

    /**
     * [慎用]清除磁盘缓存数据<br/>
     * 强制清除内部储存的缓存<br/>
     * <br/>
     * 注意:在该方法调用期间, 若对该磁盘缓存区进行读写操作, 可能会
     * 抛出异常. 请确保调用期间该磁盘缓存区不被使用.
     *
     * @param context context
     * @param diskCacheName 缓存目录名
     * @throws IOException
     */
    public static void wipeInnerDiskCache(Context context, String diskCacheName) throws IOException {
        DiskLruCache.deleteContents(new File(DirectoryUtils.getInnerCacheDir(context).getAbsolutePath() + File.separator + diskCacheName));
    }

    /******************************************
     * inner
     */

    /**
     * 磁盘缓存加载任务
     */
    private class DiskCacheTask extends TTask {

        private static final int RESULT_SUCCEED = 0;
        private static final int RESULT_FAILED = 1;
        private static final int RESULT_CANCELED = 2;
        private static final int RESULT_CONTINUE = 3;

        private String url;
        private String key;
        private int reqWidth;
        private int reqHeight;
        private OnLoadCompleteListener mOnLoadCompleteListener;

        public DiskCacheTask(String url, String key, int reqWidth, int reqHeight, OnLoadCompleteListener mOnLoadCompleteListener) {
            this.url = url;
            this.key = key;
            this.reqWidth = reqWidth;
            this.reqHeight = reqHeight;
            this.mOnLoadCompleteListener = mOnLoadCompleteListener;
        }

        @Override
        public void onPreExecute(Object params) {

        }

        @Override
        public Object doInBackground(Object params) {
            //异常检查
            if (mDiskLruCache == null || mCachedBitmapUtils == null) {
                implementor.onException(new RuntimeException("[AsyncBitmapLoader]cachedBitmapUtils is null"));
                return RESULT_CANCELED;
            }
            //计算缓存key
            String cacheKey = implementor.getCacheKey(url, key);
            try {
                //得到缓存文件
                File cacheFile = mDiskLruCache.getFile(cacheKey, 0);
                if (cacheFile != null) {
                    //若缓存文件存在, 从缓存中加载Bitmap
                    mCachedBitmapUtils.decodeFromFile(cacheKey, cacheFile.getAbsolutePath(), reqWidth, reqHeight);
                    //若此时任务已被取消, 则废弃位图
                    if (isCancel()){
                        mCachedBitmapUtils.unused(cacheKey);
                        return RESULT_CANCELED;
                    }
                    return RESULT_SUCCEED;
                } else {
                    return RESULT_CONTINUE;
                }
            } catch (IOException e) {
                implementor.onException(e);
            }
            return RESULT_FAILED;
        }

        @Override
        public void onPostExecute(Object result, boolean isCancel) {
            String cacheKey = implementor.getCacheKey(url, key);
            //若任务被取消
            if (isCancel) {
                if (mOnLoadCompleteListener != null)
                    mOnLoadCompleteListener.onLoadCanceled(url, key, getParams());
                if (mCachedBitmapUtils != null)
                    mCachedBitmapUtils.unused(cacheKey);
                if (logger != null) {
                    logger.d("[AsyncBitmapLoader]load:canceled:  from:DiskCache url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
                }
                return;
            }
            switch ((int) result) {
                case RESULT_SUCCEED:
                    if (mOnLoadCompleteListener != null && mCachedBitmapUtils != null)
                        mOnLoadCompleteListener.onLoadSucceed(url, key, getParams(), mCachedBitmapUtils.getBitmap(cacheKey));
                    if (logger != null) {
                        logger.d("[AsyncBitmapLoader]load:succeed:  from:DiskCache url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
                    }
                    break;
                case RESULT_FAILED:
                    if (mOnLoadCompleteListener != null)
                        mOnLoadCompleteListener.onLoadFailed(url, key, getParams());
                    if (mCachedBitmapUtils != null)
                        mCachedBitmapUtils.unused(cacheKey);
                    break;
                case RESULT_CANCELED:
                    if (mOnLoadCompleteListener != null)
                        mOnLoadCompleteListener.onLoadCanceled(url, key, getParams());
                    if (mCachedBitmapUtils != null)
                        mCachedBitmapUtils.unused(cacheKey);
                    break;
                case RESULT_CONTINUE:
                    //若缓存文件不存在, 加入网络加载队列
                    mNetLoadQueue.put(cacheKey, new NetLoadTask(url, key, reqWidth, reqHeight, mOnLoadCompleteListener).setParams(getParams()));
                default:
                    break;
            }
        }
    }

    /**
     * 网络加载任务
     */
    private class NetLoadTask extends TTask {

        private static final int RESULT_SUCCEED = 0;
        private static final int RESULT_FAILED = 1;
        private static final int RESULT_CANCELED = 2;
        private static final int RESULT_CONTINUE = 3;

        private String url;
        private String key;
        private int reqWidth;
        private int reqHeight;
        private OnLoadCompleteListener mOnLoadCompleteListener;
        private ResultHolder resultHolder;

        public NetLoadTask(String url, String key, int reqWidth, int reqHeight, OnLoadCompleteListener mOnLoadCompleteListener) {
            this.url = url;
            this.key = key;
            this.reqWidth = reqWidth;
            this.reqHeight = reqHeight;
            this.mOnLoadCompleteListener = mOnLoadCompleteListener;
        }

        @Override
        public void onPreExecute(Object params) {

        }

        @Override
        public Object doInBackground(Object params) {
            //检查异常
            if (mDiskLruCache == null || mCachedBitmapUtils == null) {
                implementor.onException(new RuntimeException("[AsyncBitmapLoader]cachedBitmapUtils is null"));
                return RESULT_CANCELED;
            }
            //计算缓存key
            String cacheKey = implementor.getCacheKey(url, key);
            OutputStream outputStream = null;
            DiskLruCache.Editor editor;
            try {
                //打开缓存编辑对象
                editor = mDiskLruCache.edit(cacheKey);
                //若同时Edit一个缓存文件时, 会返回null, 取消任务
                if (editor == null) {
                    return RESULT_CANCELED;
                }
                //获得输出流, 用于写入缓存
                outputStream = editor.newOutputStream(0);
                //结果容器
                resultHolder = new ResultHolder();
                //从网络加载Bitmap
                implementor.loadFromNet(url, key, reqWidth, reqHeight, resultHolder);
                //阻塞等待并获取结果Bitmap
                Bitmap bitmap = resultHolder.get();
                //判断
                if (bitmap != null && !bitmap.isRecycled()) {
                    //写入文件缓存即使失败也不影响返回Bitmap
                    try {
                        //把图片写入缓存
                        BitmapUtils.syncSaveBitmap(bitmap, outputStream, imageFormat, imageQuality, false, null);
                        //尝试flush输出流
                        try {
                            outputStream.flush();
                        } catch (Exception ignored) {
                        }
                        //写入缓存成功commit
                        editor.commit();
                        //写缓存日志
                        mDiskLruCache.flush();
                    }catch(Exception e){
                        implementor.onCacheWriteException(e);
                    }finally {
                        //尝试关闭输出流
                        try {outputStream.close();} catch (Exception ignored) {}
                    }
                    //若任务尚未被取消
                    if (!isCancel()) {
                        //加入内存缓存
                        mCachedBitmapUtils.cacheBitmap(cacheKey, bitmap);
                        return RESULT_SUCCEED;
                    }
                    return RESULT_CANCELED;
                } else {
                    //网络加载失败
                    //尝试flush输出流
                    try {outputStream.flush();} catch (Exception ignored) {}
                    //写入缓存失败abort
                    editor.abort();
                    //写缓存日志
                    mDiskLruCache.flush();
                    //尝试关闭输出流
                    try {outputStream.close();} catch (Exception ignored) {}
                }
            } catch (IOException e) {
                implementor.onException(e);
            }
            return RESULT_FAILED;
        }

        @Override
        public void onPostExecute(Object result, boolean isCancel) {
            String cacheKey = implementor.getCacheKey(url, key);
            //若任务被取消
            if (isCancel) {
                if (mOnLoadCompleteListener != null)
                    mOnLoadCompleteListener.onLoadCanceled(url, key, getParams());
                if (mCachedBitmapUtils != null)
                    mCachedBitmapUtils.unused(cacheKey);
                if (logger != null) {
                    logger.d("[AsyncBitmapLoader]load:canceled:  from:NetLoad url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
                }
                return;
            }
            switch ((int) result) {
                case RESULT_SUCCEED:
                    if (mOnLoadCompleteListener != null && mCachedBitmapUtils != null)
                        mOnLoadCompleteListener.onLoadSucceed(url, key, getParams(), mCachedBitmapUtils.getBitmap(cacheKey));
                    if (logger != null) {
                        logger.d("[AsyncBitmapLoader]load:succeed:  from:NetLoad url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
                    }
                    break;
                case RESULT_FAILED:
                    if (mOnLoadCompleteListener != null)
                        mOnLoadCompleteListener.onLoadFailed(url, key, getParams());
                    if (mCachedBitmapUtils != null)
                        mCachedBitmapUtils.unused(cacheKey);
                    break;
                case RESULT_CANCELED:
                    if (mOnLoadCompleteListener != null)
                        mOnLoadCompleteListener.onLoadCanceled(url, key, getParams());
                    if (mCachedBitmapUtils != null)
                        mCachedBitmapUtils.unused(cacheKey);
                    break;
                default:
                    break;
            }
        }

        /**
         * 当任务被取消时, 中断阻塞等待
         */
        @Override
        public void onCancel() {
            super.onCancel();
            if (resultHolder != null)
                resultHolder.interrupt();
        }
    }

    /**
     * 检查AsyncBitmapLoader是否open(), 若未open()则抛出异常<br/>
     * 遇到此异常, 请检查代码, AsyncBitmapLoader实例化/设置后必须调用open()方法启动.
     */
    private void checkIsOpen(){
        if (mDiskLruCache == null || mCachedBitmapUtils == null || mDiskCacheQueue == null || mNetLoadQueue == null){
            throw new RuntimeException("[AsyncBitmapLoader]can't use AsyncBitmapLoader without AsyncBitmapLoader.open()!!!");
        }
    }

    /**
     * 加载结束监听
     */
    public interface OnLoadCompleteListener {
        /**
         * 加载成功
         *
         * @param params 由load传入的参数, 并非Bitmap
         * @param bitmap 加载成功的位图, 可能为null
         */
        void onLoadSucceed(String url, String key, Object params, Bitmap bitmap);

        /**
         * 加载失败
         *
         * @param params 由load传入的参数, 并非Bitmap
         */
        void onLoadFailed(String url, String key, Object params);

        /**
         * 加载取消
         *
         * @param params 由load传入的参数, 并非Bitmap
         */
        void onLoadCanceled(String url, String key, Object params);
    }

    /**
     * 结果容器<br/>
     * get方法会阻塞, 一直到set方法执行后
     */
    public class ResultHolder{

        private boolean hasInterrupted = false;//是否被打断
        private boolean hasSet = false;//是否设置了值

        private Bitmap result;//结果

        private final ReentrantLock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();

        public void set(Bitmap result){
            lock.lock();
            try{
                //若get()阻塞被中断后, set(Bitmap),为防止内存泄露,将Bitmap回收
                if (hasInterrupted) {
                    if (result != null && !result.isRecycled()) {
                        result.recycle();
                    }
                    return;
                }
                this.result = result;
                this.hasSet = true;
                condition.signalAll();
            }finally {
                lock.unlock();
            }
        }

        private Bitmap get(){
            lock.lock();
            try{
                if (!hasSet && !hasInterrupted)
                    condition.await();
                return result;
            } catch (InterruptedException ignored) {
                hasInterrupted = true;
            } finally {
                lock.unlock();
            }
            return null;
        }

        private void interrupt(){
            lock.lock();
            try{
                hasInterrupted = true;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }

    }

}
