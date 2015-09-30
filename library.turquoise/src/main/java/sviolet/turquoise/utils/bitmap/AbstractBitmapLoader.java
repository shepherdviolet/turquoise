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
import sviolet.turquoise.utils.bitmap.implementor.BitmapLoaderImplementor;
import sviolet.turquoise.utils.bitmap.listener.OnBitmapLoadedListener;
import sviolet.turquoise.utils.cache.DiskLruCache;
import sviolet.turquoise.utils.sys.ApplicationUtils;
import sviolet.turquoise.utils.sys.DirectoryUtils;

/**
 * 基本实现类, 未实现load/get方法, 请勿直接使用<br/>
 * 请使用:<Br/>
 * 1.AsyncBitmapLoader<br/>
 * 2.AsyncBitmapDrawableLoader<Br/>
 * ****************************************************************<br/>
 * <br/>
 * AsyncBitmapLoader中每个位图资源都由url唯一标识, url在AsyncBitmapLoader内部
 * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
 * 这个cacheKey标识唯一的资源<br/>
 * <Br/>
 * ****************************************************************<br/>
 * <Br/>
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
 * Created by S.Violet on 2015/7/3.
 */
public class AbstractBitmapLoader {

    CachedBitmapUtils mCachedBitmapUtils;//带缓存的Bitmap工具
    private DiskLruCache mDiskLruCache;//磁盘缓存器

    TQueue mDiskCacheQueue;//磁盘缓存加载队列
    private TQueue mNetLoadQueue;//网络加载队列

    //SETTINGS//////////////////////////////////////////////

    private Context context;//(必须)
    private String diskCacheName;//磁盘缓存名(必须)
    BitmapLoaderImplementor implementor;//实现器(必须)
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
    Logger logger;//日志打印器

    /**
     * 请勿直接使用
     *
     * @param context 上下文
     * @param diskCacheName 磁盘缓存目录名
     * @param implementor 实现器
     */
    AbstractBitmapLoader(Context context, String diskCacheName, BitmapLoaderImplementor implementor) {
        if (implementor == null){
            throw new RuntimeException("[AbstractBitmapLoader]implementor is null !!", new NullPointerException());
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
    public AbstractBitmapLoader setNetLoad(int netLoadConcurrency, int netLoadVolume){
        this.netLoadConcurrency = netLoadConcurrency;
        this.netLoadVolume = netLoadVolume;
        return this;
    }

    /**
     * @param diskCacheSizeMib 磁盘缓存最大容量, 默认10, 单位Mb
     * @param diskLoadConcurrency 磁盘加载任务并发量, 默认5
     * @param diskLoadVolume 磁盘加载等待队列容量, 默认10
     */
    public AbstractBitmapLoader setDiskCache(int diskCacheSizeMib, int diskLoadConcurrency, int diskLoadVolume){
        this.diskCacheSize = 1024L * 1024L * diskCacheSizeMib;
        this.diskLoadConcurrency = diskLoadConcurrency;
        this.diskLoadVolume = diskLoadVolume;
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
     * @param ramCacheSizePercent 内存缓存区占用应用可用内存的比例 (0, 1]
     * @param ramCacheRecyclerSizePercent 内存缓存回收站占用应用可用内存的比例 [0, 1], 使用SafeBitmapDrawableFactory时设置为0禁用回收站
     */
    public AbstractBitmapLoader setRamCache(float ramCacheSizePercent, float ramCacheRecyclerSizePercent){
        this.ramCacheSizePercent = ramCacheSizePercent;
        this.ramCacheRecyclerSizePercent = ramCacheRecyclerSizePercent;
        return this;
    }

    /**
     * 设置磁盘缓存路径为内部储存<br/>
     * 若不设置, 则优先选择外部储存, 当外部储存不存在时使用内部储存
     */
    public AbstractBitmapLoader setDiskCacheInner(){
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
    public AbstractBitmapLoader setImageQuality(Bitmap.CompressFormat format, int quality){
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
    public AbstractBitmapLoader setDuplicateLoadEnable(boolean duplicateLoadEnable){
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
    public AbstractBitmapLoader setLogger(Logger logger) {
        this.logger = logger;
        if (mCachedBitmapUtils != null)
            mCachedBitmapUtils.getBitmapCache().setLogger(logger);
        return this;
    }

    /**
     * [重要]启用BitmapLoader, 在实例化并设置完BitmapLoader后, 必须调用此
     * 方法, 开启磁盘缓存/内存缓存. 否则会抛出异常.<Br/>
     *
     * @throws IOException 磁盘缓存启动失败抛出异常
     */
    public AbstractBitmapLoader open() throws IOException {
        this.mDiskLruCache = DiskLruCache.open(cacheDir, ApplicationUtils.getAppVersion(context), 1, diskCacheSize);
        this.mCachedBitmapUtils = new CachedBitmapUtils(context, ramCacheSizePercent, ramCacheRecyclerSizePercent);
        this.mDiskCacheQueue = new TQueue(true, diskLoadConcurrency).setVolumeMax(diskLoadVolume).waitCancelingTask(true).setKeyConflictPolicy(keyConflictPolicy);
        this.mNetLoadQueue = new TQueue(true, netLoadConcurrency).setVolumeMax(netLoadVolume).waitCancelingTask(true).setKeyConflictPolicy(keyConflictPolicy);
        if(logger != null)
            mCachedBitmapUtils.getBitmapCache().setLogger(logger);
        return this;
    }

    /******************************************
     * public
     */

    /**
     * [重要]将一个Bitmap标示为不再使用<Br/>
     * <br/>
     *  将一个Bitmap标记为不再使用, 缓存中的Bitmap不会被立即回收, 在内存不足时,
     * 会进行缓存清理, 清理时会将最早的被标记为unused的Bitmap.recycle()回收掉.
     * 已进入回收站的Bitmap会被立即回收.<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     *
     * @param url 图片URL地址
     */
    public void unused(String url) {
        checkIsOpen();
        //计算缓存key
        String cacheKey = implementor.getCacheKey(url);
        //网络加载队列取消
        mNetLoadQueue.cancel(cacheKey);
        //磁盘缓存加载队列取消
        mDiskCacheQueue.cancel(cacheKey);
        //将位图标识为不再使用
        mCachedBitmapUtils.unused(cacheKey);
        if (logger != null) {
            logger.d("[AbstractBitmapLoader]unused:  url<" + url + "> cacheKey<" + cacheKey + ">");
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
            logger.d("[AbstractBitmapLoader]destroy");
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
    class DiskCacheTask extends TTask {

        private static final int RESULT_SUCCEED = 0;
        private static final int RESULT_FAILED = 1;
        private static final int RESULT_CANCELED = 2;
        private static final int RESULT_CONTINUE = 3;

        private String url;
        private int reqWidth;
        private int reqHeight;
        private OnBitmapLoadedListener mOnBitmapLoadedListener;

        public DiskCacheTask(String url, int reqWidth, int reqHeight, OnBitmapLoadedListener mOnBitmapLoadedListener) {
            this.url = url;
            this.reqWidth = reqWidth;
            this.reqHeight = reqHeight;
            this.mOnBitmapLoadedListener = mOnBitmapLoadedListener;
        }

        @Override
        public void onPreExecute(Object params) {

        }

        @Override
        public Object doInBackground(Object params) {
            //异常检查
            if (mDiskLruCache == null || mCachedBitmapUtils == null) {
                implementor.onException(new RuntimeException("[AbstractBitmapLoader]cachedBitmapUtils is null"));
                return RESULT_CANCELED;
            }
            //计算缓存key
            String cacheKey = implementor.getCacheKey(url);
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
            } catch (Exception e) {
                implementor.onException(e);
            }
            return RESULT_FAILED;
        }

        @Override
        public void onPostExecute(Object result, boolean isCancel) {
            String cacheKey = implementor.getCacheKey(url);
            //若任务被取消
            if (isCancel) {
                if (mOnBitmapLoadedListener != null)
                    mOnBitmapLoadedListener.onLoadCanceled(url, getParams());
                if (mCachedBitmapUtils != null)
                    mCachedBitmapUtils.unused(cacheKey);
                if (logger != null) {
                    logger.d("[AbstractBitmapLoader]load:canceled:  from:DiskCache url<" + url + "> cacheKey<" + cacheKey + ">");
                }
                return;
            }
            switch ((int) result) {
                case RESULT_SUCCEED:
                    if (mOnBitmapLoadedListener != null && mCachedBitmapUtils != null)
                        mOnBitmapLoadedListener.onLoadSucceed(url, getParams(), mCachedBitmapUtils.getBitmap(cacheKey));
                    if (logger != null) {
                        logger.d("[AbstractBitmapLoader]load:succeed:  from:DiskCache url<" + url + "> cacheKey<" + cacheKey + ">");
                    }
                    break;
                case RESULT_FAILED:
                    if (mOnBitmapLoadedListener != null)
                        mOnBitmapLoadedListener.onLoadFailed(url, getParams());
                    if (mCachedBitmapUtils != null)
                        mCachedBitmapUtils.unused(cacheKey);
                    break;
                case RESULT_CANCELED:
                    if (mOnBitmapLoadedListener != null)
                        mOnBitmapLoadedListener.onLoadCanceled(url, getParams());
                    if (mCachedBitmapUtils != null)
                        mCachedBitmapUtils.unused(cacheKey);
                    break;
                case RESULT_CONTINUE:
                    //若缓存文件不存在, 加入网络加载队列
                    mNetLoadQueue.put(cacheKey, new NetLoadTask(url, reqWidth, reqHeight, mOnBitmapLoadedListener).setParams(getParams()));
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
        private int reqWidth;
        private int reqHeight;
        private OnBitmapLoadedListener mOnBitmapLoadedListener;
        private ResultHolder resultHolder;

        public NetLoadTask(String url, int reqWidth, int reqHeight, OnBitmapLoadedListener mOnBitmapLoadedListener) {
            this.url = url;
            this.reqWidth = reqWidth;
            this.reqHeight = reqHeight;
            this.mOnBitmapLoadedListener = mOnBitmapLoadedListener;
        }

        @Override
        public void onPreExecute(Object params) {

        }

        @Override
        public Object doInBackground(Object params) {
            //检查异常
            if (mDiskLruCache == null || mCachedBitmapUtils == null) {
                implementor.onException(new RuntimeException("[AbstractBitmapLoader]cachedBitmapUtils is null"));
                return RESULT_CANCELED;
            }
            //计算缓存key
            String cacheKey = implementor.getCacheKey(url);
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
                implementor.loadFromNet(url, reqWidth, reqHeight, resultHolder);
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
                            if (outputStream != null)
                                outputStream.flush();
                        } catch (Exception ignored) {
                        }
                        //写入缓存成功commit
                        editor.commit();
                        //写缓存日志
                        mDiskLruCache.flush();
                    }catch(Exception e){
                        implementor.onCacheWriteException(e);
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
                    //写入缓存失败abort
                    editor.abort();
                    //写缓存日志
                    mDiskLruCache.flush();
                }
            } catch (Exception e) {
                implementor.onException(e);
            }finally {
                try {
                    if (outputStream != null)
                        outputStream.close();
                } catch (Exception ignored) {
                }
            }
            return RESULT_FAILED;
        }

        @Override
        public void onPostExecute(Object result, boolean isCancel) {
            String cacheKey = implementor.getCacheKey(url);
            //若任务被取消
            if (isCancel) {
                if (mOnBitmapLoadedListener != null)
                    mOnBitmapLoadedListener.onLoadCanceled(url, getParams());
                if (mCachedBitmapUtils != null)
                    mCachedBitmapUtils.unused(cacheKey);
                if (logger != null) {
                    logger.d("[AbstractBitmapLoader]load:canceled:  from:NetLoad url<" + url + "> cacheKey<" + cacheKey + ">");
                }
                return;
            }
            switch ((int) result) {
                case RESULT_SUCCEED:
                    if (mOnBitmapLoadedListener != null && mCachedBitmapUtils != null)
                        mOnBitmapLoadedListener.onLoadSucceed(url, getParams(), mCachedBitmapUtils.getBitmap(cacheKey));
                    if (logger != null) {
                        logger.d("[AbstractBitmapLoader]load:succeed:  from:NetLoad url<" + url + "> cacheKey<" + cacheKey + ">");
                    }
                    break;
                case RESULT_FAILED:
                    if (mOnBitmapLoadedListener != null)
                        mOnBitmapLoadedListener.onLoadFailed(url, getParams());
                    if (mCachedBitmapUtils != null)
                        mCachedBitmapUtils.unused(cacheKey);
                    break;
                case RESULT_CANCELED:
                    if (mOnBitmapLoadedListener != null)
                        mOnBitmapLoadedListener.onLoadCanceled(url, getParams());
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
     * 检查BitmapLoader是否open(), 若未open()则抛出异常<br/>
     * 遇到此异常, 请检查代码, BitmapLoader实例化/设置后必须调用open()方法启动.
     */
    void checkIsOpen(){
        if (mDiskLruCache == null || mCachedBitmapUtils == null || mDiskCacheQueue == null || mNetLoadQueue == null){
            throw new RuntimeException("[AbstractBitmapLoader]can't use AbstractBitmapLoader without AbstractBitmapLoader.open()!!!");
        }
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
