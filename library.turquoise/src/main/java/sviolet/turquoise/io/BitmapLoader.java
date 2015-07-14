package sviolet.turquoise.io;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import sviolet.turquoise.app.CommonException;
import sviolet.turquoise.app.Logger;
import sviolet.turquoise.io.cache.DiskLruCache;
import sviolet.turquoise.utils.ApplicationUtils;
import sviolet.turquoise.utils.CachedBitmapUtils;

/**
 * [抽象类]图片双缓存网络加载器<br/>
 * <br/>
 * Bitmap内存缓存+磁盘缓存+网络加载+防OOM<br/>
 * <br/>
 * BitmapLoader中每个位图资源都由url和key共同标识, url和key在BitmapLoader内部
 * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
 * 这个cacheKey<br/>
 * <Br/>
 * ****************************************************************<br/>
 * [使用说明]<br/>
 * 1.实现抽象类BitmapLoader -> MyBitmapLoader <br/>
 * 2.实例化MyBitmapLoader(必须) <br/>
 * 3.setRamCache/setDiskCache/setNetLoad/setLogger设置参数(可选) <br/>
 * 4.open() 启用BitmapLoader(必须, 否则抛异常)<br/>
 * <br/>
 * [代码示例]:<Br/>
    private BitmapLoader mBitmapLoader;
    try {
        mBitmapLoader = new MyBitmapLoader(this, "bitmap")
            .setRamCache(0.125f)
            .setDiskCache(50, 5, 15)
            .setNetLoad(3, 15)
            .setLogger(getLogger())
            .open();//必须调用
    } catch (IOException e) {
        //磁盘缓存打开失败的情况, 可提示客户磁盘已满等
    }
 * <br/>
 * [上述代码说明]:<br/>
 * 位图内存缓存占用应用最大可用内存的12.5%(回收站最大可能占用额外的12.5%),
 * 内存缓存能容纳2-3页的图片为宜. 设置过小, 存放不下一页的内容, 影响显示效果,
 * 设置过大, 缓存占用应用可用内存过大, 影响性能或造成OOM. <br/>
 * 在路径/sdcard/Android/data/<application package>/cache/bitmap或
 * /data/data/<application package>/cache/bitmap下存放磁盘缓存数据,
 * 缓存最大容量50M, 磁盘缓存容量根据实际情况设置, 磁盘缓存加载最大并发量10,
 * 并发量应考虑图片质量/大小, 若图片较大, 应考虑减少并发量, 磁盘缓存等待队列
 * 容量15, 即只会加载最后请求的15个任务, 更早的加载请求会被取消, 等待队列容
 * 量根据屏幕中最多可能展示的图片数决定, 设定值为屏幕最多可能展示图片数的
 * 2倍为宜, 设置过少会导致屏幕中图片未全部加载完, 例如屏幕中最多可能展示10
 * 张图片, 则设置15-20较为合适, 若设置了10, 屏幕中会有2张图未加载. <br/>
 * 网络加载并发量为3, 根据网络情况和图片大小决定, 过多的并发量会阻塞网络, 过
 * 少会导致图片加载太慢, 网络加载等待队列容量15, 建议与磁盘缓存等待队列容量
 * 相等, 根据屏幕中最多可能展示的图片数决定(略大于), 设置过少会导致屏幕中图
 * 片未全部加载完.<br/>
 * 设置日志打印器后, BitmapLoader会打印出一些日志用于调试, 例如内存缓存使用
 * 情况, 图片加载日志等, 可根据日志调试/选择上述参数的值.<br/>
 * <br/>
 * ****************************************************************<br/>
 * [CommonException]: [BitmapCache]recycler Out Of Memory!!!<br/>
 * 当回收站内存占用超过设定值 (即内存总消耗超过设定值的两倍) 时, 会触发此异常<Br/>
 * 解决方案:<br/>
 * 1.请合理使用BitmapCache.unused()方法, 将不再使用的Bitmap设置为"不再使用"状态,
 * Bitmap只有被设置为此状态, 才会被回收(recycle()), 否则在缓存区满后, 会进入回收站,
 * 但并不会释放资源, 这么做是为了防止回收掉正在使用的Bitmap而报错.<br/>
 * 2.给BitmapCache设置合理的最大占用内存(或占比), 分配过小可能会导致不够用而报错,
 * 分配过大可能使应用其他占用内存受限.<br/>
 *
 * Created by S.Violet on 2015/7/3.
 */
public abstract class BitmapLoader {

    private CachedBitmapUtils mCachedBitmapUtils;//带缓存的Bitmap工具
    private DiskLruCache mDiskLruCache;//磁盘缓存器

    private TQueue mDiskCacheQueue;//磁盘缓存加载队列
    private TQueue mNetLoadQueue;//网络加载队列

    //SETTINGS//////////////////////////////////////////////

    private Context context;
    private String diskCacheName;//磁盘缓存名
    private long diskCacheSize = 1024 * 1024 * 10;//磁盘缓存大小(Mb)
    private float ramCacheSizePercent = 0.125f;//内存缓存大小(占应用可用内存比例)
    private int diskLoadConcurrency = 5;//磁盘加载任务并发量
    private int diskLoadVolume = 10;//磁盘加载等待队列容量
    private int netLoadConcurrency = 3;//网络加载任务并发量
    private int netLoadVolume = 10;//网络加载等待队列容量
    private Logger logger;//日志打印器

    /**
     * 1.实现抽象类BitmapLoader -> MyBitmapLoader <br/>
     * 2.实例化MyBitmapLoader(必须) <br/>
     * 3.setRamCache/setDiskCache/setNetLoad/setLogger设置参数(可选) <br/>
     * 4.open() 启用BitmapLoader(必须, 否则抛异常)<br/>
     * <br/>
     * [代码示例]:<Br/>
            private BitmapLoader mBitmapLoader;
            try {
                mBitmapLoader = new MyBitmapLoader(this, "bitmap")
                    .setRamCache(0.125f)
                    .setDiskCache(50, 10, 15)
                    .setNetLoad(3, 15)
                    .setLogger(getLogger())
                    .open();//必须调用
            } catch (IOException e) {
                //磁盘缓存打开失败的情况, 可提示客户磁盘已满等
            }
     * <br/>
     * [上述代码说明]:<br/>
     * 位图内存缓存占用应用最大可用内存的12.5%(回收站最大可能占用额外的12.5%),
     * 内存缓存能容纳2-3页的图片为宜. 设置过小, 存放不下一页的内容, 影响显示效果,
     * 设置过大, 缓存占用应用可用内存过大, 影响性能或造成OOM. <br/>
     * 在路径/sdcard/Android/data/<application package>/cache/bitmap或
     * /data/data/<application package>/cache/bitmap下存放磁盘缓存数据,
     * 缓存最大容量50M, 磁盘缓存容量根据实际情况设置, 磁盘缓存加载最大并发量10,
     * 并发量应考虑图片质量/大小, 若图片较大, 应考虑减少并发量, 磁盘缓存等待队列
     * 容量15, 即只会加载最后请求的15个任务, 更早的加载请求会被取消, 等待队列容
     * 量根据屏幕中最多可能展示的图片数决定, 设定值为屏幕最多可能展示图片数的
     * 2倍为宜, 设置过少会导致屏幕中图片未全部加载完, 例如屏幕中最多可能展示10
     * 张图片, 则设置15-20较为合适, 若设置了10, 屏幕中会有2张图未加载. <br/>
     * 网络加载并发量为3, 根据网络情况和图片大小决定, 过多的并发量会阻塞网络, 过
     * 少会导致图片加载太慢, 网络加载等待队列容量15, 建议与磁盘缓存等待队列容量
     * 相等, 根据屏幕中最多可能展示的图片数决定(略大于), 设置过少会导致屏幕中图
     * 片未全部加载完.<br/>
     * 设置日志打印器后, BitmapLoader会打印出一些日志用于调试, 例如内存缓存使用
     * 情况, 图片加载日志等, 可根据日志调试/选择上述参数的值.<br/>
     * <br/>
     * @param context 上下文
     * @param diskCacheName 磁盘缓存目录名
     */
    public BitmapLoader(Context context, String diskCacheName) {
        this.context = context;
        this.diskCacheName = diskCacheName;
    }

    /**
     * @param netLoadConcurrency 网络加载任务并发量, 默认3
     * @param netLoadVolume 网络加载等待队列容量, 默认10
     */
    public BitmapLoader setNetLoad(int netLoadConcurrency, int netLoadVolume){
        this.netLoadConcurrency = netLoadConcurrency;
        this.netLoadVolume = netLoadVolume;
        return this;
    }

    /**
     * @param diskCacheSizeMib 磁盘缓存最大容量, 默认10, 单位Mb
     * @param diskLoadConcurrency 磁盘加载任务并发量, 默认5
     * @param diskLoadVolume 磁盘加载等待队列容量, 默认10
     */
    public BitmapLoader setDiskCache(int diskCacheSizeMib, int diskLoadConcurrency, int diskLoadVolume){
        this.diskCacheSize = 1024L * 1024L * diskCacheSizeMib;
        this.diskLoadConcurrency = diskLoadConcurrency;
        this.diskLoadVolume = diskLoadVolume;
        return this;
    }

    /**
     * @param ramCacheSizePercent 内存缓存最大容量占应用可用内存的比例 (0, 0.5f], 默认0.125, 建议不超过0.25
     */
    public BitmapLoader setRamCache(float ramCacheSizePercent){
        this.ramCacheSizePercent = ramCacheSizePercent;
        return this;
    }

    /**
     * 设置日志打印器, 用于输出调试日志, 不设置则不输出日志
     */
    public BitmapLoader setLogger(Logger logger) {
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
    public BitmapLoader open() throws IOException {
        this.mDiskLruCache = DiskLruCache.open(ApplicationUtils.getDiskCacheDir(context, diskCacheName),
                ApplicationUtils.getAppVersion(context), 1, diskCacheSize);
        this.mCachedBitmapUtils = new CachedBitmapUtils(context, ramCacheSizePercent);
        this.mDiskCacheQueue = new TQueue(true, diskLoadConcurrency).setVolumeMax(diskLoadVolume).waitCancelingTask(true).overrideSameKeyTask(false);
        this.mNetLoadQueue = new TQueue(true, netLoadConcurrency).setVolumeMax(netLoadVolume).waitCancelingTask(true).overrideSameKeyTask(false);
        if(logger != null)
            mCachedBitmapUtils.getBitmapCache().setLogger(logger);
        return this;
    }

    /******************************************
     * abstract
     */

    /**
     * [实现提示]:<br/>
     * 可以直接使用key值作为cacheKey, 也可以将url或者key进行摘要
     * 计算, 得到摘要值作为cacheKey, 根据实际情况实现.  <Br/>
     * BitmapLoader中每个位图资源都由url和key共同标识, url和key在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey<br/>
     *
     * @return 实现根据URL连接和指定Key, 计算并返回缓存Key
     */
    protected abstract String getCacheKey(String url, String key);

    /**
     * 实现根据参数从网络下载图片并写入cacheOutputStream输出流<Br/>
     * 网络加载成功返回true, 加载失败返回false<Br/>
     * 可根据task.getState() >= TTask.STATE_CANCELING判断任务当前是否被取消<br/>
     * BitmapLoader中任务的取消为软取消, 仅将任务置为完成+取消状态,
     * 若网络加载不针对取消状态做处理, 取消中的任务将会占用并发量,
     * 导致新的加载请求无法执行(一直在等待队列).
     */
    protected abstract boolean loadFromNet(String url, String key, OutputStream cacheOutputStream, TTask task);

    /**
     * 实现异常处理
     */
    protected abstract void onException(Throwable throwable);

    /******************************************
     * public
     */

    /**
     * 加载图片, 加载成功后回调mOnLoadCompleteListener<br/>
     * 回调方法的params参数为此方法传入的params, 并非Bitmap<Br/>
     * 回调方法中使用Bitmap, 调用BitmapLoader.get()方法从内存缓存
     * 中取.<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url和key共同标识, url和key在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey<br/>
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
        String cacheKey = getCacheKey(url, key);
        if (logger != null) {
            logger.d("[BitmapLoader]load:start:  url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
        }
        //尝试内存缓存中取Bitmap
        Bitmap bitmap = mCachedBitmapUtils.getBitmap(cacheKey);
        if (bitmap != null && !bitmap.isRecycled()) {
            //缓存中存在直接回调:成功
            mOnLoadCompleteListener.onLoadSucceed(url, key, params, bitmap);
            if (logger != null) {
                logger.d("[BitmapLoader]load:succeed:  from:BitmapCache url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
            }
            return;
        }
        //若缓存中不存在, 加入磁盘缓存加载队列
        mDiskCacheQueue.put(cacheKey, new DiskCacheTask(url, key, reqWidth, reqHeight, mOnLoadCompleteListener).setParams(params));
    }

    /**
     * 从内存缓存中取Bitmap, 若不存在或已被回收, 则返回null<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url和key共同标识, url和key在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey<br/>
     *
     * @param url 图片URL地址
     * @param key 图片自定义key
     * @return 若不存在或已被回收, 则返回null
     */
    public Bitmap get(String url, String key) {
        checkIsOpen();
        //计算缓存key
        String cacheKey = getCacheKey(url, key);
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
     * BitmapLoader中每个位图资源都由url和key共同标识, url和key在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey<br/>
     *
     * @param url 图片URL地址
     * @param key 图片自定义key
     */
    public void unused(String url, String key) {
        checkIsOpen();
        //计算缓存key
        String cacheKey = getCacheKey(url, key);
        //网络加载队列取消
        mNetLoadQueue.cancel(cacheKey);
        //磁盘缓存加载队列取消
        mDiskCacheQueue.cancel(cacheKey);
        //将位图标识为不再使用
        mCachedBitmapUtils.unused(cacheKey);
        if (logger != null) {
            logger.d("[BitmapLoader]unused:  url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
        }
    }

    /**
     * [重要]将所有资源回收销毁, 请在Activity.onDestroy()时调用该方法
     */
    public void destroy() {
        checkIsOpen();
        if (mNetLoadQueue != null) {
            mNetLoadQueue.cancelAll();
            mNetLoadQueue = null;
        }
        if (mDiskCacheQueue != null) {
            mDiskCacheQueue.cancelAll();
            mDiskCacheQueue = null;
        }
        if (mDiskLruCache != null) {
            try {
                mDiskLruCache.close();
                mDiskLruCache = null;
            } catch (IOException e) {
                onException(e);
            }
        }
        if (mCachedBitmapUtils != null) {
            mCachedBitmapUtils.recycleAll();
            mCachedBitmapUtils = null;
        }
        if (logger != null) {
            logger.d("[BitmapLoader]destroy");
        }
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
                onException(new CommonException("[BitmapLoader]cachedBitmapUtils is null"));
                return RESULT_CANCELED;
            }
            //计算缓存key
            String cacheKey = getCacheKey(url, key);
            try {
                //得到缓存文件
                File cacheFile = mDiskLruCache.getFile(cacheKey, 0);
                if (cacheFile != null) {
                    //若缓存文件存在, 从缓存中加载Bitmap
                    mCachedBitmapUtils.decodeFromFile(cacheKey, cacheFile.getAbsolutePath(), reqWidth, reqHeight);
                    //若此时任务已被取消, 则废弃位图
                    if (isCancel()){
                        mCachedBitmapUtils.unused(cacheKey);
                    }
                    return RESULT_SUCCEED;
                } else {
                    //若缓存文件不存在, 加入网络加载队列
                    mNetLoadQueue.put(cacheKey, new NetLoadTask(url, key, reqWidth, reqHeight, mOnLoadCompleteListener).setParams(getParams()));
                    return RESULT_CONTINUE;
                }
            } catch (IOException e) {
                onException(e);
            }
            return RESULT_FAILED;
        }

        @Override
        public void onPostExecute(Object result, boolean isCancel) {
            if (mOnLoadCompleteListener != null) {
                String cacheKey = getCacheKey(url, key);
                //若任务被取消
                if (isCancel) {
                    mOnLoadCompleteListener.onLoadCanceled(url, key, getParams());
                    mCachedBitmapUtils.unused(cacheKey);
                    if (logger != null) {
                        logger.d("[BitmapLoader]load:canceled:  from:DiskCache url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
                    }
                    return;
                }
                switch ((int) result) {
                    case RESULT_SUCCEED:
                        mOnLoadCompleteListener.onLoadSucceed(url, key, getParams(), mCachedBitmapUtils.getBitmap(cacheKey));
                        if (logger != null) {
                            logger.d("[BitmapLoader]load:succeed:  from:DiskCache url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
                        }
                        break;
                    case RESULT_FAILED:
                        mOnLoadCompleteListener.onLoadFailed(url, key, getParams());
                        mCachedBitmapUtils.unused(cacheKey);
                        break;
                    case RESULT_CANCELED:
                        mOnLoadCompleteListener.onLoadCanceled(url, key, getParams());
                        mCachedBitmapUtils.unused(cacheKey);
                        break;
                    default:
                        break;
                }
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
                onException(new CommonException("[BitmapLoader]cachedBitmapUtils is null"));
                return RESULT_CANCELED;
            }
            //计算缓存key
            String cacheKey = getCacheKey(url, key);
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
                //从网络加载图片, 并写入缓存的输出流
                if (loadFromNet(url, key, outputStream, this)) {
                    //尝试flush输出流
                    try {outputStream.flush();} catch (Exception ignored) {}
                    //写入缓存成功commit
                    editor.commit();
                    //写缓存日志
                    mDiskLruCache.flush();
                    //尝试关闭输出流
                    try {outputStream.close();} catch (Exception ignored) {}
                    //得到缓存文件
                    File cacheFile = mDiskLruCache.getFile(cacheKey, 0);
                    if (cacheFile != null) {
                        //若缓存文件存在, 则读取
                        mCachedBitmapUtils.decodeFromFile(cacheKey, cacheFile.getAbsolutePath(), reqWidth, reqHeight);
                        //若此时任务已被取消, 则废弃位图
                        if (isCancel()){
                            mCachedBitmapUtils.unused(cacheKey);
                        }
                        return RESULT_SUCCEED;
                    }
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
                onException(e);
            }
            return RESULT_FAILED;
        }

        @Override
        public void onPostExecute(Object result, boolean isCancel) {
            if (mOnLoadCompleteListener != null) {
                String cacheKey = getCacheKey(url, key);
                //若任务被取消
                if (isCancel) {
                    mOnLoadCompleteListener.onLoadCanceled(url, key, getParams());
                    mCachedBitmapUtils.unused(cacheKey);
                    if (logger != null) {
                        logger.d("[BitmapLoader]load:canceled:  from:NetLoad url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
                    }
                    return;
                }
                switch ((int) result) {
                    case RESULT_SUCCEED:
                        mOnLoadCompleteListener.onLoadSucceed(url, key, getParams(), mCachedBitmapUtils.getBitmap(cacheKey));
                        if (logger != null) {
                            logger.d("[BitmapLoader]load:succeed:  from:NetLoad url<" + url + "> key<" + key + "> cacheKey<" + cacheKey + ">");
                        }
                        break;
                    case RESULT_FAILED:
                        mOnLoadCompleteListener.onLoadFailed(url, key, getParams());
                        mCachedBitmapUtils.unused(cacheKey);
                        break;
                    case RESULT_CANCELED:
                        mOnLoadCompleteListener.onLoadCanceled(url, key, getParams());
                        mCachedBitmapUtils.unused(cacheKey);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 检查BitmapLoader是否open(), 若未open()则抛出异常<br/>
     * 遇到此异常, 请检查代码, BitmapLoader实例化/设置后必须调用open()方法启动.
     */
    private void checkIsOpen(){
        if (mDiskLruCache == null || mCachedBitmapUtils == null || mDiskCacheQueue == null || mNetLoadQueue == null){
            throw new CommonException("[BitmapLoader]can't use BitmapLoader without BitmapLoader.open()!!!");
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
        public void onLoadSucceed(String url, String key, Object params, Bitmap bitmap);

        /**
         * 加载失败
         *
         * @param params 由load传入的参数, 并非Bitmap
         */
        public void onLoadFailed(String url, String key, Object params);

        /**
         * 加载取消
         *
         * @param params 由load传入的参数, 并非Bitmap
         */
        public void onLoadCanceled(String url, String key, Object params);
    }

}