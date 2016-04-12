/*
 * Copyright (C) 2015 S.Violet
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

package sviolet.turquoise.modelx.bitmaploader;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import sviolet.turquoise.modelx.taskqueue.TQueue;
import sviolet.turquoise.modelx.taskqueue.TTask;
import sviolet.turquoise.modelx.bitmaploader.entity.BitmapRequest;
import sviolet.turquoise.modelx.bitmaploader.handler.BitmapDecodeHandler;
import sviolet.turquoise.modelx.bitmaploader.handler.CommonExceptionHandler;
import sviolet.turquoise.modelx.bitmaploader.handler.DefaultBitmapDecodeHandler;
import sviolet.turquoise.modelx.bitmaploader.handler.DefaultCommonExceptionHandler;
import sviolet.turquoise.modelx.bitmaploader.handler.DefaultDiskCacheExceptionHandler;
import sviolet.turquoise.modelx.bitmaploader.handler.DefaultNetLoadHandler;
import sviolet.turquoise.modelx.bitmaploader.handler.DiskCacheExceptionHandler;
import sviolet.turquoise.modelx.bitmaploader.handler.NetLoadHandler;
import sviolet.turquoise.modelx.bitmaploader.listener.OnBitmapLoadedListener;
import sviolet.turquoise.model.cache.BitmapCache;
import sviolet.turquoise.model.cache.DiskLruCache;
import sviolet.turquoise.util.conversion.ByteUtils;
import sviolet.turquoise.util.crypt.DigestCipher;
import sviolet.turquoise.utilx.lifecycle.listener.LifeCycle;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.util.droid.ApplicationUtils;
import sviolet.turquoise.util.droid.DirectoryUtils;

/**
 * 
 * BitmapLoader<Br/>
 * 图片双缓存网络异步加载器<br/>
 * <br/>
 * 异步加载Bitmap, 适用性广泛, 兼容性好, 使用较复杂.<br/>
 * <br/>
 * ****************************************************************<br/>
 * * * * * BitmapLoader使用说明:<br/>
 * ****************************************************************<br/>
 * <br/>
 * -------------------初始化设置----------------<br/>
 * <br/>
 * <pre>{@code
 *       mBitmapLoader = new BitmapLoader.Builder(this, "bitmap")
 *          //.setNetLoadHandler(new DefaultNetLoadHandler(10000, 30000).setCompress(...))//设置网络加载超时/原图压缩等配置
 *          //.setNetLoadHandler(new MyNetLoadHandler(...))//自定义网络加载实现
 *          //.setBitmapDecodeHandler(new MyBitmapDecodeHandler(...))//自定义图片解码实现,或对图片进行特殊处理
 *          //.setCommonExceptionHandler(new MyCommonExceptionHandler(...))//自定义普通异常处理
 *          //.setDiskCacheExceptionHandler(new MyDiskCacheExceptionHandler(...))//自定义磁盘缓存异常处理
 *          .setNetLoad(3, 15)//设置网络加载并发数3, 等待队列15
 *          .setDiskCache(50, 5, 15)//设置磁盘缓存容量50M, 磁盘加载并发数5, 等待队列15
 *          .setRamCache(0.125f, 0.125f)//设置内存缓存大小,启用回收站
 *          //.setDiskCacheInner()//强制使用内部储存
 *          //.setDuplicateLoadEnable(true)//允许相同图片同时加载(慎用)
 *          //.setWipeOnNewVersion()//当APP更新时清空磁盘缓存
 *          .create();
 * }</pre>
 * <br/>
 * -------------------加载器使用----------------<br/>
 * <br/>
 * 1.load <br/>
 *      加载图片,加载结束后回调OnBitmapLoadedListener<p/>
 *
 * 2.get <br/>
 *      从内存缓冲获取图片,若不存在返回null<p/>
 *
 * 3.unused [重要] <br/>
 *      不再使用的图片须及时用该方法废弃,尤其是大量图片的场合,未被废弃(unused)的图片
 *      将不会被BitmapLoader回收.请参看"名词解释".<br/>
 *      该方法能取消加载任务,有助于减少不必要的加载,节省流量,使需要显示的图片尽快加载.<p/>
 *
 * 4.destroy [重要] <br/>
 *      清除全部图片及加载任务,通常在Activity.onDestroy中调用<p/>
 *
 * 5.reduceMemoryCache [特殊] <br/>
 *      强制回收内存缓存中不再使用(unused)的图片<br/>
 *      用于暂时减少缓存的内存占用, 请勿频繁调用.<br/>
 *      通常是内存紧张的场合, 可以在Activity.onStop()中调用, Activity暂时不显示的情况下,
 *      将缓存中已被标记为unused的图片回收掉, 减少内存占用.<p/>
 *
 * 6.cancelAllTasks [慎用] <br/>
 *      强制取消所有加载任务.用于BitmapLoader未销毁的情况下, 结束磁盘和网络的访问. 这会导致
 *      加载中的图片无法显示. <br/>
 *      Not recommended: This will cause the loading Bitmap to be unable to display. <p/>
 *
 * -------------------注意事项----------------<br/>
 * <br/>
 * 1.ListView等View复用的场合,应先unused废弃原Bitmap,再设置新的:
 * <pre>{@code
 *      holder.imageView.setImageBitmap(null);//置空(或默认图)
 *      String oldUrl = (String) holder.imageView.getTag();//原图的url
 *      if(oldUrl != null)
 *          bitmapLoader.unused(oldUrl);//将原图标识为不再使用,并取消原加载任务
 *      holder.imageView.setTag(newUrl);//记录新图的url,用于下次unused
 *      bitmapLoader.load(newUrl, reqWidth, reqHeight, holder.imageView, mOnBitmapLoadedListener);//加载图片
 * }</pre>
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
 *      使用SimpleBitmapLoader无需特殊处理, 它们自带重新加载功能.<br/>
 *      推荐方案:<br/>
 *      1).在网络加载失败(failed)时, 有限次地重新加载, 不应重新加载unused的任务.<br/>
 * <Br/>
 * 4.界面提示"磁盘缓存访问失败", 或日志打印:
 *      [DefaultDiskCacheExceptionHandler]DiskCache open failed, use BitmapLoader.Builder.setDiskCacheExceptionHandler to custom processing <br/>
 *      可自定义磁盘缓存打开失败处理方式, {@link DefaultDiskCacheExceptionHandler}<Br/>
 * <Br/>
 * 
 *
 * @author S.Violet
 *
 * Created by S.Violet on 2015/7/3.
 */
public class BitmapLoader implements LifeCycle {

    public static final String TAG = "BitmapLoader";

    private TLogger logger = TLogger.get(this, TAG);

    private BitmapCache mBitmapCache;//Bitmap内存缓存器
    private DiskLruCache mDiskLruCache;//磁盘缓存器

    private TQueue mDiskCacheQueue;//磁盘缓存加载队列
    private TQueue mNetLoadQueue;//网络加载队列

    //BitmapLoader状态//////////////////////////////////////////////

    private Settings settings;//配置
    private boolean diskCacheDisabled = false;//禁用磁盘缓存

    private static final int STATE_ACTIVE = 0;//可用状态
    private static final int STATE_OPEN_FAILED = 1;//启用失败状态
    private static final int STATE_DESTROYED = 2;//销毁状态
    private int state = STATE_ACTIVE;

    /**
     * 内部构造器, 利用settings构造实例
     */
    BitmapLoader(Settings settings) {
        //保存配置
        this.settings = settings;
        //启动加载器
        open();
    }

    /**
     * [特殊]<br/>
     * 禁用磁盘缓存后再次启动(流量增大风险)<p/>
     *
     * 说明:<br/>
     * 若内存缓存中不存在图片,则直接从网络加载,且加载后不存入磁盘缓存.<p/>
     *
     * 注意:仅磁盘缓存打开失败的情况可用, 在{@link DiskCacheExceptionHandler}.onCacheOpenException()
     * 回调方法中调用.<p/>
     *
     * {@link DefaultDiskCacheExceptionHandler}已实现几种磁盘缓存打开失败的处理方式, 可直接配置使用.<br/>
     */
    public void openWithoutDiskCache(){
        if (state == STATE_OPEN_FAILED) {
            diskCacheDisabled = true;
            open();
        }else{
            logger.e("openWithoutDiskCache() can only use when DiskCache open failed");
        }
    }

    /**
     * 启动图片加载器
     */
    void open(){
        //打开磁盘缓存
        if (!diskCacheDisabled) {
            try {
                this.mDiskLruCache = DiskLruCache.open(settings.cacheDir, settings.appVersionCode, 1, settings.diskCacheSize);
            } catch (Exception e) {
                state = STATE_OPEN_FAILED;
                getDiskCacheExceptionHandler().onCacheOpenException(getContext(), this, e);
                return;
            }
        }
        //打开内存缓存
        this.mBitmapCache = BitmapCache.newInstance(getContext(), settings.ramCacheSizePercent, settings.ramCacheRecyclerSizePercent);
        //打开队列
        this.mDiskCacheQueue = new TQueue(true, settings.diskLoadConcurrency)
                .setVolumeMax(settings.diskLoadVolume)
                .waitCancelingTask(true)
                .setKeyConflictPolicy(settings.keyConflictPolicy);
        this.mNetLoadQueue = new TQueue(true, settings.netLoadConcurrency)
                .setVolumeMax(settings.netLoadVolume)
                .waitCancelingTask(true)
                .setKeyConflictPolicy(settings.keyConflictPolicy);
        //设置状态为可用
        state = STATE_ACTIVE;
    }

    /************************************************************************************
     * FUNCTION
     */

    /**
     * 加载图片, 加载成功后回调mOnLoadCompleteListener<br/>
     * 回调方法的params参数为此方法传入的params, 并非Bitmap<Br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <br/>
     * @see BitmapRequest
     *
     * @param url 图片URL地址
     * @param params 参数,会带入mOnLoadCompleteListener回调方法,通常为ImageView,便于设置图片
     * @param mOnBitmapLoadedListener 回调监听器
     */
    public void load(String url, Object params, OnBitmapLoadedListener mOnBitmapLoadedListener) {
        load(new BitmapRequest(url), params, mOnBitmapLoadedListener);
    }

    /**
     * 加载图片, 加载成功后回调mOnLoadCompleteListener<br/>
     * 回调方法的params参数为此方法传入的params, 并非Bitmap<Br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <br/>
     * @see BitmapRequest
     *
     * @param url 图片URL地址
     * @param reqWidth 需求宽度 px
     * @param reqHeight 需求高度 px
     * @param params 参数,会带入mOnLoadCompleteListener回调方法,通常为ImageView,便于设置图片
     * @param mOnBitmapLoadedListener 回调监听器
     */
    public void load(String url, int reqWidth, int reqHeight, Object params, OnBitmapLoadedListener mOnBitmapLoadedListener) {
        load(new BitmapRequest(url).setReqDimension(reqWidth, reqHeight), params, mOnBitmapLoadedListener);
    }

    /**
     * 
     * 加载图片, 加载成功后回调mOnLoadCompleteListener<br/>
     * 回调方法的params参数为此方法传入的params, 并非Bitmap<Br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <br/>
     * @see BitmapRequest
     *
     * @param request 图片加载请求参数
     * @param params 参数,会带入mOnLoadCompleteListener回调方法,通常为ImageView,便于设置图片
     * @param mOnBitmapLoadedListener 回调监听器
     */
    public void load(BitmapRequest request, Object params, OnBitmapLoadedListener mOnBitmapLoadedListener) {
        if (request == null)
            throw new RuntimeException("[BitmapLoader]load: request must not be null");
        if(checkIsOpen())
            return;
        //计算缓存key
        String cacheKey = getCacheKey(request.getUrl());
        logger.d("load:start:  url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">");
        //尝试内存缓存中取Bitmap
        Bitmap bitmap = mBitmapCache.get(cacheKey);
        if (bitmap != null && !bitmap.isRecycled()) {
            //缓存中存在直接回调:成功
            mOnBitmapLoadedListener.onLoadSucceed(request, params, bitmap);
            logger.d("load:succeed:  from:BitmapCache url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">");
            return;
        }
        //若缓存中不存在, 加入加载队列
        if (!diskCacheDisabled) {
            //加入磁盘缓存加载队列
            mDiskCacheQueue.put(cacheKey, new DiskCacheTask(request, mOnBitmapLoadedListener).setParams(params));
        }else{
            logger.d("load:diskCacheDisabled:  url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">");
            //磁盘缓存禁用场合, 直接加入网络加载队列
            mNetLoadQueue.put(cacheKey, new NetLoadTask(request, mOnBitmapLoadedListener).setParams(params));
        }
    }

    /**
     * 
     * 从内存缓存中取Bitmap, 若不存在或已被回收, 则返回null<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * 
     *
     * @param url 图片URL地址
     * @return 若不存在或已被回收, 则返回null
     */
    public Bitmap get(String url) {
        if(checkIsOpen())
            return null;
        //计算缓存key
        String cacheKey = getCacheKey(url);
        //尝试从内存缓存中取Bitmap
        Bitmap bitmap = mBitmapCache.get(cacheKey);
        if (bitmap != null && !bitmap.isRecycled()) {
            //若存在且未被回收, 返回Bitmap
            return bitmap;
        }
        //若不存在或已被回收, 返回null
        return null;
    }

    /**
     * 
     * [重要]尝试取消加载任务,将指定Bitmap标示为不再使用,利于回收(Bitmap.recycle)<Br/>
     * <br/>
     * 当图片不再显示时,及时unused有助于减少不必要的加载,节省流量,使需要显示的图片尽快加载.
     * 例如:ListView高速滑动时,中间很多项是来不及加载的,也无需显示图片,及时取消加载任务,可
     * 以跳过中间项的加载,使滚动停止后需要显示的项尽快加载出来.<br/>
     * <Br/>
     * 将一个Bitmap标记为不再使用, 缓存中的Bitmap不会被立即回收, 在内存不足时,
     * 会进行缓存清理, 清理时会将最早的被标记为unused的Bitmap.recycle()回收掉.
     * 已进入回收站的Bitmap会被立即回收.<br/>
     * <br/>
     * <br/>
     * URL::<Br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * 
     *
     * @param url 图片URL地址
     */
    public void unused(String url) {
        if(checkIsOpen())
            return;
        //计算缓存key
        String cacheKey = getCacheKey(url);
        //网络加载队列取消
        mNetLoadQueue.cancel(cacheKey);
        //磁盘缓存加载队列取消
        mDiskCacheQueue.cancel(cacheKey);
        //将位图标识为不再使用
        mBitmapCache.unused(cacheKey);
        logger.d("unused:  url<" + url + "> cacheKey<" + cacheKey + ">");
    }

    /**
     * [特殊]强制回收内存缓存中不再使用(unused)的图片<p/>
     *
     * 用于暂时减少缓存的内存占用, 请勿频繁调用.<br/>
     * 通常是内存紧张的场合, 可以在Activity.onStop()中调用, Activity暂时不显示的情况下,
     * 将缓存中已被标记为unused的图片回收掉, 减少内存占用.<br/>
     */
    public void reduceMemoryCache(){
        if(checkIsOpen())
            return;
        mBitmapCache.reduce();
    }

    /**
     * [慎用]强制取消所有加载任务<p/>
     *
     * 用于BitmapLoader未销毁的情况下, 结束磁盘和网络的访问. 这会导致加载中的图片无法显示. <br/>
     *
     * @deprecated Not recommended: This will cause the loading Bitmap to be unable to display.
     */
    @Deprecated
    public void cancelAllTasks(){
        if(checkIsOpen())
            return;
        //网络加载队列取消
        mNetLoadQueue.cancelAll();
        //磁盘缓存加载队列取消
        mDiskCacheQueue.cancelAll();

        logger.d("BitmapLoader cancel all tasks");
    }

    /**
     * [重要]将所有资源回收销毁, 建议在Activity.onDestroy()时调用该方法
     */
    public void destroy() {

        state = STATE_DESTROYED;

        //销毁队列
        if (mNetLoadQueue != null) {
            mNetLoadQueue.destroy();
            mNetLoadQueue = null;
        }
        if (mDiskCacheQueue != null) {
            mDiskCacheQueue.destroy();
            mDiskCacheQueue = null;
        }

        //销毁缓存
        if (mDiskLruCache != null) {
            try {
                mDiskLruCache.close();
                mDiskLruCache = null;
            } catch (IOException ignored) {
            }
        }
        if (mBitmapCache != null) {
            mBitmapCache.removeAll();
            mBitmapCache = null;
        }

        //销毁处理器
        if (settings.mNetLoadHandler != null) {
            settings.mNetLoadHandler.onDestroy();
            settings.mNetLoadHandler = null;
        }
        if (settings.mBitmapDecodeHandler != null) {
            settings.mBitmapDecodeHandler.onDestroy();
            settings.mBitmapDecodeHandler = null;
        }
        if (settings.mCommonExceptionHandler != null) {
            settings.mCommonExceptionHandler.onDestroy();
            settings.mCommonExceptionHandler = null;
        }
        if (settings.mDiskCacheExceptionHandler != null) {
            settings.mDiskCacheExceptionHandler.onDestroy();
            settings.mDiskCacheExceptionHandler = null;
        }

        logger.d("BitmapLoader destroy");
    }

    /**
     * BitmapLoader是否可用状态<br/>
     * 满足条件:<br/>
     * 1.启用成功(open)<br/>
     * 2.未销毁(destroy)<br/>
     */
    public boolean isActive(){
        return state == STATE_ACTIVE;
    }

    /****************************************************************************
     * static function
     */

    /**
     * [慎用]清除磁盘缓存数据<br/>
     * 若外部储存存在, 则清除外部储存的缓存, 否则清除内部储存的缓存<Br/>
     * <br/>
     * 注意:在该方法调用期间, 若对该磁盘缓存区进行读写操作, 可能会
     * 抛出异常. 请确保调用期间该磁盘缓存区不被使用.
     *
     * @param context context
     * @param diskCacheName 缓存目录名
     * @throws Exception
     */
    public static void wipeDiskCache(Context context, String diskCacheName) throws Exception {
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
     * @throws Exception
     */
    public static void wipeInnerDiskCache(Context context, String diskCacheName) throws Exception {
        DiskLruCache.deleteContents(new File(DirectoryUtils.getInnerCacheDir(context).getAbsolutePath() + File.separator + diskCacheName));
    }

    /**********************************************************************
     * inner class
     */

    /**
     * 基本配置
     */
    static class Settings{

        WeakReference<Context> context;//上下文(弱引用)
        String diskCacheName;//磁盘缓存名(必须)
        File cacheDir;//缓存路径
        long diskCacheSize = 1024 * 1024 * 10;//磁盘缓存大小(Mb)
        float ramCacheSizePercent = 0.125f;//内存缓存大小(占应用可用内存比例)
        float ramCacheRecyclerSizePercent = 0.125f;//内存缓存回收站大小(占应用可用内存比例)
        int diskLoadConcurrency = 5;//磁盘加载任务并发量
        int diskLoadVolume = 10;//磁盘加载等待队列容量
        int netLoadConcurrency = 3;//网络加载任务并发量
        int netLoadVolume = 10;//网络加载等待队列容量
        int keyConflictPolicy = TQueue.KEY_CONFLICT_POLICY_CANCEL;//TQueue同名任务冲突策略
        int appVersionCode = 1;//应用版本versionCode

        //处理器////////////////////////////////////////////////////////

        volatile NetLoadHandler mNetLoadHandler;//网络加载处理器
        volatile BitmapDecodeHandler mBitmapDecodeHandler;//图片解码处理器
        volatile CommonExceptionHandler mCommonExceptionHandler;//普通异常处理器
        volatile DiskCacheExceptionHandler mDiskCacheExceptionHandler;//磁盘缓存异常处理器

    }

    /**
     * 构建器, 继承自BitmapLoader.AbsBuilder
     */
    public static class Builder extends AbsBuilder<Builder, BitmapLoader>{

        /**
         * @param context 上下文
         * @param diskCacheName 磁盘缓存目录
         */
        public Builder(Context context, String diskCacheName) {
            super(context, diskCacheName, new Settings());
        }

        /**
         * 创建BitmapLoader实例, 只能调用一次
         */
        @Override
        public BitmapLoader create(){
            if (settings == null)
                throw new RuntimeException("[BitmapLoader.Builder]builder can't create repeatly");
            BitmapLoader loader = new BitmapLoader(settings);
            settings = null;
            return loader;
        }

        @Override
        protected Builder cast(AbsBuilder builder) {
            return (Builder) builder;
        }
    }

    /**
     * 基本构建器, 含基本设置功能
     * @param <BuildType> 构建器类型
     * @param <LoaderType> 加载器类型
     */
    public static abstract class AbsBuilder<BuildType extends AbsBuilder, LoaderType>{

        protected Settings settings;//配置

        /**
         * @param context 上下文
         * @param diskCacheName 磁盘缓存目录
         * @param settings 配置实例
         */
        AbsBuilder(Context context, String diskCacheName, Settings settings){
            if (context == null){
                throw new RuntimeException("[BitmapLoader.AbsBuilder]context is null !!");
            }
            if (diskCacheName == null || "".equals(diskCacheName)){
                throw new RuntimeException("[BitmapLoader.AbsBuilder]diskCacheName is null !!");
            }
            if (settings == null){
                throw new RuntimeException("[BitmapLoader.AbsBuilder]settings is null !!");
            }
            this.settings = settings;
            settings.context = new WeakReference<Context>(context);
            settings.diskCacheName = diskCacheName;
            settings.cacheDir = DirectoryUtils.getCacheDir(context, diskCacheName);
        }

        /**
         * 1.网络加载任务并发量(netLoadConcurrency)根据网络情况和图片大小决定, 过多的并发量会阻塞网络
         * 过少会导致图片加载太慢<br/>
         * 2.网络加载等待队列容量(netLoadVolume)默认为10, 即只会加载最后请求的10个任务, 更早的加载请求
         * 会被取消. 根据屏幕中最多可能展示的图片数决定, 设定值为屏幕最多可能展示图片数的1.5-3倍为宜, 设置
         * 过少会导致屏幕中图片未全部加载完成. 例如屏幕中最多可能展示10张图片, 则设置15-30较为合适, 若设置
         * 了5, 屏幕中会有至少5张图未加载. <br/>
         *
         * @param netLoadConcurrency 网络加载任务并发量, 默认3
         * @param netLoadVolume 网络加载等待队列容量, 默认10
         */
        public BuildType setNetLoad(int netLoadConcurrency, int netLoadVolume){
            settings.netLoadConcurrency = netLoadConcurrency;
            settings.netLoadVolume = netLoadVolume;
            return cast(this);
        }

        /**
         *
         * 1.磁盘缓存最大容量(diskCacheSizeMib)根据实际情况设置,即磁盘缓存最大占用空间<br/>
         * 2.磁盘加载任务并发量(diskLoadConcurrency)应考虑图片数量/大小, 若图片较大, 应考虑减少并发量<br/>
         * 3.磁盘加载等待队列容量(diskLoadVolume)默认为10, 即只会加载最后请求的10个任务, 更早的加载请求
         * 会被取消. 根据屏幕中最多可能展示的图片数决定, 设定值为屏幕最多可能展示图片数的1.5-3倍为宜, 设置
         * 过少会导致屏幕中图片未全部加载完成. 例如屏幕中最多可能展示10张图片, 则设置15-30较为合适, 若设置
         * 了5, 屏幕中会有至少5张图未加载. <br/>
         *
         * @param diskCacheSizeMib 磁盘缓存最大容量, 默认10, 单位Mb
         * @param diskLoadConcurrency 磁盘加载任务并发量, 默认5
         * @param diskLoadVolume 磁盘加载等待队列容量, 默认10
         */
        public BuildType setDiskCache(int diskCacheSizeMib, int diskLoadConcurrency, int diskLoadVolume){
            settings.diskCacheSize = 1024L * 1024L * diskCacheSizeMib;
            settings.diskLoadConcurrency = diskLoadConcurrency;
            settings.diskLoadVolume = diskLoadVolume;
            return cast(this);
        }

        /**
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
         * <br/>
         * 1."内存缓存区"能容纳2-3页的图片为宜. 设置过小, 存放不下一页的内容, 图片显示不全, 设置过大,
         * 缓存占用应用可用内存过大, 影响性能或造成OOM. <br/>
         * 2."内存缓存回收站"通常设置与"内存缓存区"相同.<Br/>
         *
         * @param ramCacheSizePercent 内存缓存区占用应用可用内存的比例 (0, 0.5], 默认值0.125f
         * @param ramCacheRecyclerSizePercent 内存缓存回收站占用应用可用内存的比例 [0, 0.5], 设置为0禁用回收站, 默认值0.125f
         */
        public BuildType setRamCache(float ramCacheSizePercent, float ramCacheRecyclerSizePercent){
            settings.ramCacheSizePercent = ramCacheSizePercent;
            settings.ramCacheRecyclerSizePercent = ramCacheRecyclerSizePercent;
            return cast(this);
        }

        /**
         * 磁盘缓存强制使用内部储存<p/>
         *
         * 外部储存:/sdcard/Android/data/<application package>/cache/diskCacheName<br/>
         * 内部储存:/data/data/<application package>/cache/diskCacheName<p/>
         *
         * 默认设置(不调用该方法):<Br/>
         * 自动选择, 若外部储存可用, 则优先使用外部储存.<br/>
         */
        public BuildType setDiskCacheInner(){
            settings.cacheDir = new File(DirectoryUtils.getInnerCacheDir(settings.context.get()).getAbsolutePath() + File.separator + settings.diskCacheName);
            return cast(this);
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
        public BuildType setDuplicateLoadEnable(boolean duplicateLoadEnable){
            if (duplicateLoadEnable){
                settings.keyConflictPolicy = TQueue.KEY_CONFLICT_POLICY_FOLLOW;
            }else{
                settings.keyConflictPolicy = TQueue.KEY_CONFLICT_POLICY_CANCEL;
            }
            return cast(this);
        }

        /**
         * 设置App更新时清空磁盘缓存<p/>
         *
         * 当应用versionCode发生变化时, 会清空磁盘缓存. 注意是versionCode, 非versionName.<p/>
         *
         * 默认(不调用该方法):<Br/>
         * APP更新时不清空缓存<br/>
         *
         */
        public BuildType setWipeOnNewVersion(){
            settings.appVersionCode = ApplicationUtils.getAppVersion(settings.context.get());
            return cast(this);
        }

        /**
         * 设置网络加载处理器<br/>
         * 用于自定义实现网络加载.<p/>
         *
         * 不设置默认为:{@link DefaultNetLoadHandler}<p/>
         *
         * 基本设置方法(设置超时时间等):<br/>
         * @see DefaultNetLoadHandler
         */
        public BuildType setNetLoadHandler(NetLoadHandler mNetLoadHandler) {
            settings.mNetLoadHandler = mNetLoadHandler;
            return cast(this);
        }

        /**
         * 设置图片(Bitmap)解码处理器<br/>
         * 用于自定义实现图片解码过程, 或对图片进行特殊处理(缩放/圆角等)<br/>
         * 注意:网络加载或磁盘读取的数据均经过该处理器解码, 应尽量避免复杂的处理, 以免
         * 影响加载性能.<br/>
         *
         * 不设置默认为:{@link DefaultBitmapDecodeHandler}<p/>
         *
         * @see DefaultBitmapDecodeHandler
         */
        public BuildType setBitmapDecodeHandler(BitmapDecodeHandler mBitmapDecodeHandler){
            settings.mBitmapDecodeHandler = mBitmapDecodeHandler;
            return cast(this);
        }

        /**
         * 设置普通异常处理器<br/>
         * 用于自定义实现普通异常的处理, 通常用于打印错误日志<p/>
         *
         * 不设置默认为:{@link DefaultCommonExceptionHandler}<p/>
         *
         * @see DefaultCommonExceptionHandler
         */
        public BuildType setCommonExceptionHandler(CommonExceptionHandler mCommonExceptionHandler) {
            settings.mCommonExceptionHandler = mCommonExceptionHandler;
            return cast(this);
        }

        /**
         * 设置磁盘缓存异常处理器<br/>
         * 用于自定义实现磁盘缓存打开失败, 磁盘缓存写入失败的处理<p/>
         *
         * 不设置默认为:{@link DefaultDiskCacheExceptionHandler}<p/>
         *
         * 基本设置方法(磁盘缓存访问失败处理方式):<br/>
         * @see DefaultDiskCacheExceptionHandler
         */
        public BuildType setDiskCacheExceptionHandler(DiskCacheExceptionHandler mDiskCacheExceptionHandler) {
            settings.mDiskCacheExceptionHandler = mDiskCacheExceptionHandler;
            return cast(this);
        }

        /**
         * [待实现]创建实例方法
         */
        public abstract LoaderType create();

        /**
         * 将AbsBuilder转为目标Builder
         * @param builder absBuilder
         * @return 目标Builder
         */
        protected abstract BuildType cast(AbsBuilder builder);

    }

    /************************************************************************
     * task
     */

    /**
     * 磁盘缓存加载任务
     */
    class DiskCacheTask extends TTask {

        private static final int RESULT_SUCCEED = 0;
        private static final int RESULT_FAILED = 1;
        private static final int RESULT_CANCELED = 2;
        private static final int RESULT_CONTINUE = 3;

        private BitmapRequest request;
        private OnBitmapLoadedListener mOnBitmapLoadedListener;

        public DiskCacheTask(BitmapRequest request, OnBitmapLoadedListener mOnBitmapLoadedListener) {
            this.request = request;
            this.mOnBitmapLoadedListener = mOnBitmapLoadedListener;
        }

        @Override
        public void onPreExecute(Object params) {

        }

        @Override
        public Object doInBackground(Object params) {
            //异常检查
            if (mDiskLruCache == null) {
                getCommonExceptionHandler().onCommonException(getContext(), BitmapLoader.this, request, new RuntimeException("[BitmapLoader]mDiskLruCache is null"));
                return RESULT_CANCELED;
            }
            if (mBitmapCache == null) {
                getCommonExceptionHandler().onCommonException(getContext(), BitmapLoader.this, request, new RuntimeException("[BitmapLoader]mBitmapCache is null"));
                return RESULT_CANCELED;
            }
            //计算缓存key
            String cacheKey = getCacheKey(request.getUrl());
            try {
                //得到缓存文件
                File cacheFile = mDiskLruCache.getFile(cacheKey, 0);
                if (cacheFile != null && cacheFile.exists()) {
                    //若缓存文件存在, 从缓存中加载Bitmap
                    Bitmap bitmap = null;
                    try {
                        bitmap = getBitmapDecodeHandler().onDecode(getContext(), BitmapLoader.this, request, cacheFile.getAbsolutePath());
                    }catch(Exception e){
                        //Bitmap加载失败
                        logger.w("disk load failed, bad file, trying to net load, url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">", e);
                        //尝试从网络重新加载
                        return RESULT_CONTINUE;
                    }
                    //加载出的Bitmap为空
                    if (bitmap == null || bitmap.isRecycled()){
                        logger.w("disk load failed, bitmap decode failed, trying to net load, url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">");
                        //尝试从网络重新加载
                        return RESULT_CONTINUE;
                    }
                    //若此时任务已被取消, 则废弃位图
                    if (isCancel()){
                        return RESULT_CANCELED;
                    }
                    //存入内存缓存
                    mBitmapCache.put(cacheKey, bitmap);
                    return RESULT_SUCCEED;
                } else {
                    return RESULT_CONTINUE;
                }
            } catch (Exception e) {
                getCommonExceptionHandler().onCommonException(getContext(), BitmapLoader.this, request, e);
            }
            return RESULT_FAILED;
        }

        @Override
        public void onPostExecute(Object result, boolean isCancel) {
            String cacheKey = getCacheKey(request.getUrl());
            //若任务被取消
            if (isCancel) {
                if (mOnBitmapLoadedListener != null)
                    mOnBitmapLoadedListener.onLoadCanceled(request, getParams());
                if (mBitmapCache != null)
                    mBitmapCache.unused(cacheKey);
                logger.d("load:canceled:  from:DiskCache url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">");
                return;
            }
            switch ((int) result) {
                case RESULT_SUCCEED:
                    if (mOnBitmapLoadedListener != null && mBitmapCache != null)
                        mOnBitmapLoadedListener.onLoadSucceed(request, getParams(), mBitmapCache.get(cacheKey));
                    logger.d("load:succeed:  from:DiskCache url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">");
                    break;
                case RESULT_FAILED:
                    if (mOnBitmapLoadedListener != null)
                        mOnBitmapLoadedListener.onLoadFailed(request, getParams());
                    if (mBitmapCache != null)
                        mBitmapCache.unused(cacheKey);
                    logger.d("load:failed:  from:DiskCache url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">");
                    break;
                case RESULT_CANCELED:
                    if (mOnBitmapLoadedListener != null)
                        mOnBitmapLoadedListener.onLoadCanceled(request, getParams());
                    if (mBitmapCache != null)
                        mBitmapCache.unused(cacheKey);
                    logger.d("load:canceled:  from:DiskCache url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">");
                    break;
                case RESULT_CONTINUE:
                    //若缓存文件不存在, 加入网络加载队列
                    mNetLoadQueue.put(cacheKey, new NetLoadTask(request, mOnBitmapLoadedListener).setParams(getParams()));
                default:
                    break;
            }
        }
    }

    /**
     * 网络加载任务
     */
    class NetLoadTask extends TTask {

        private static final int RESULT_SUCCEED = 0;
        private static final int RESULT_FAILED = 1;
        private static final int RESULT_CANCELED = 2;
        private static final int RESULT_CONTINUE = 3;

        private BitmapRequest request;
        private OnBitmapLoadedListener mOnBitmapLoadedListener;
        private BitmapLoaderMessenger messenger;

        public NetLoadTask(BitmapRequest request, OnBitmapLoadedListener mOnBitmapLoadedListener) {
            this.request = request;
            this.mOnBitmapLoadedListener = mOnBitmapLoadedListener;
        }

        @Override
        public void onPreExecute(Object params) {

        }

        @Override
        public Object doInBackground(Object params) {
            //检查异常
            if (!diskCacheDisabled && mDiskLruCache == null) {
                getCommonExceptionHandler().onCommonException(getContext(), BitmapLoader.this, request, new RuntimeException("[BitmapLoader]mDiskLruCache is null"));
                return RESULT_CANCELED;
            }
            if (mBitmapCache == null) {
                getCommonExceptionHandler().onCommonException(getContext(), BitmapLoader.this, request, new RuntimeException("[BitmapLoader]mBitmapCache is null"));
                return RESULT_CANCELED;
            }
            //计算缓存key
            String cacheKey = getCacheKey(request.getUrl());
            OutputStream outputStream = null;
            DiskLruCache.Editor editor = null;
            try {
                //结果容器
                messenger = new BitmapLoaderMessenger();
                //从网络加载图片数据
                getNetLoadHandler().loadFromNet(getContext(), BitmapLoader.this, request, messenger);
                //阻塞等待并获取结果数据
                int result = messenger.getResult();
                if (result == BitmapLoaderMessenger.RESULT_SUCCEED){
                    //结果为加载成功,且数据正常的情况
                    if (messenger.getData() != null && messenger.getData().length > 0) {
                        //写入文件缓存即使失败也不影响返回
                        if (!diskCacheDisabled) {
                            try {
                                //打开缓存编辑对象
                                editor = mDiskLruCache.edit(cacheKey);
                                //若同时Edit一个缓存文件时, 会返回null, 取消任务
                                if (editor == null) {
                                    throw new Exception("[BitmapLoader]mDiskLruCache.edit(cacheKey) return null, multiple edit one file");
                                }
                                //获得输出流, 用于写入缓存
                                outputStream = editor.newOutputStream(0);
                                //把图片写入缓存
                                outputStream.write(messenger.getData());
                                //尝试flush输出流
                                try {
                                    outputStream.flush();
                                } catch (Exception ignored) {
                                }
                                //写入缓存成功commit
                                editor.commit();
                                //写缓存日志
                                mDiskLruCache.flush();
                            } catch (Exception e) {
                                try {
                                    //写入缓存失败abort
                                    if (editor != null)
                                        editor.abort();
                                    //写缓存日志
                                    mDiskLruCache.flush();
                                } catch (Exception ignored) {
                                }
                                getDiskCacheExceptionHandler().onCacheWriteException(getContext(), BitmapLoader.this, request, e);
                            } finally {
                                try {
                                    if (outputStream != null)
                                        outputStream.close();
                                } catch (Exception ignored) {
                                }
                            }
                        }

                        if (isCancel()) {
                            //若加载任务被取消,即使返回结果是加载成功,也只会存入缓存,不会返回成功的结果
                            return RESULT_CANCELED;
                        }

                        //解码图片
                        Bitmap bitmap = getBitmapDecodeHandler().onDecode(getContext(), BitmapLoader.this, request, messenger.getData());

                        if (bitmap == null || bitmap.isRecycled()){
                            //图片解码失败
                            logger.w("[BitmapLoader]net loaded failed, data decoding to Bitmap failed, url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">, dataHex<<" + ByteUtils.bytesToHex(messenger.getData()) + ">>");
                            return RESULT_FAILED;
                        }

                        //加入内存缓存
                        mBitmapCache.put(cacheKey, bitmap);
                        //加载成功
                        return RESULT_SUCCEED;

                    }else{
                        logger.w("net loaded failed, data is null, url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">");
                    }
                } else if (result == BitmapLoaderMessenger.RESULT_CANCELED || result == BitmapLoaderMessenger.RESULT_INTERRUPTED) {
                    return RESULT_CANCELED;
                } else {
                    //异常处理
                    if (messenger.getException() != null){
                        throw messenger.getException();
                    }
                }
            } catch (Exception e) {
                getCommonExceptionHandler().onCommonException(getContext(), BitmapLoader.this, request, e);
            }
            //加载失败
            return RESULT_FAILED;
        }

        @Override
        public void onPostExecute(Object result, boolean isCancel) {
            String cacheKey = getCacheKey(request.getUrl());
            //若任务被取消
            if (isCancel) {
                if (mOnBitmapLoadedListener != null)
                    mOnBitmapLoadedListener.onLoadCanceled(request, getParams());
                if (mBitmapCache != null)
                    mBitmapCache.unused(cacheKey);
                logger.d("load:canceled:  from:NetLoad url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">");
                return;
            }
            switch ((int) result) {
                case RESULT_SUCCEED:
                    if (mOnBitmapLoadedListener != null && mBitmapCache != null)
                        mOnBitmapLoadedListener.onLoadSucceed(request, getParams(), mBitmapCache.get(cacheKey));
                    logger.d("load:succeed:  from:NetLoad url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">");
                    break;
                case RESULT_FAILED:
                    if (mOnBitmapLoadedListener != null)
                        mOnBitmapLoadedListener.onLoadFailed(request, getParams());
                    if (mBitmapCache != null)
                        mBitmapCache.unused(cacheKey);
                    logger.d("load:failed:  from:NetLoad url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">");
                    break;
                case RESULT_CANCELED:
                    if (mOnBitmapLoadedListener != null)
                        mOnBitmapLoadedListener.onLoadCanceled(request, getParams());
                    if (mBitmapCache != null)
                        mBitmapCache.unused(cacheKey);
                    logger.d("load:canceled:  from:NetLoad url<" + request.getUrl() + "> cacheKey<" + cacheKey + ">");
                    break;
                default:
                    break;
            }
        }

        /**
         * 当任务被取消时
         */
        @Override
        public void onCancel() {
            super.onCancel();
            if (messenger != null)
                messenger.cancel();
//            if (messenger != null)
//                messenger.interrupt();
        }

        /**
         * 销毁
         */
        @Override
        protected void onDestroy() {
            super.onDestroy();

            if (messenger != null)
                messenger.destroy();

            this.mOnBitmapLoadedListener = null;
            this.messenger = null;
        }
    }

    /****************************************************
     * private
     */

    /**
     * url -> cacheKey
     */
    private String getCacheKey(String url) {
        //url->SHA1->hex->key
        return ByteUtils.bytesToHex(DigestCipher.digestStr(url, DigestCipher.TYPE_SHA1));
    }

    /**
     * 检查BitmapLoader是否open(), 若未open()则抛出异常, 若已被销毁/启用失败则返回true<br/>
     * 遇到此异常, 请检查代码, BitmapLoader实例化/设置后必须调用open()方法启动.
     */
    boolean checkIsOpen(){

        switch(state){
            case STATE_OPEN_FAILED:
                /*
                    出现该情况, 看DefaultDiskCacheExceptionHandler设置方法, 或直接调用openWithoutDiskCache()方法
                */
                logger.e("BitmapLoader: DiskCache open failed, can't use, see DefaultDiskCacheExceptionHandler or use openWithoutDiskCache()");
                return true;
            case STATE_DESTROYED:
                logger.e("can't use destroyed BitmapLoader");
                return true;
            default:
                return false;
        }

    }

    Context getContext(){
        if (settings.context != null){
            return settings.context.get();
        }
        return null;
    }

    Settings getSettings(){
        return settings;
    }

    private NetLoadHandler getNetLoadHandler() {
        if (settings.mNetLoadHandler == null){
            synchronized (this){
                if (settings.mNetLoadHandler == null){
                    settings.mNetLoadHandler = new DefaultNetLoadHandler();
                }
            }
        }
        return settings.mNetLoadHandler;
    }

    private BitmapDecodeHandler getBitmapDecodeHandler() {
        if (settings.mBitmapDecodeHandler == null){
            synchronized (this){
                if (settings.mBitmapDecodeHandler == null){
                    settings.mBitmapDecodeHandler = new DefaultBitmapDecodeHandler();
                }
            }
        }
        return settings.mBitmapDecodeHandler;
    }

    private CommonExceptionHandler getCommonExceptionHandler() {
        if (settings.mCommonExceptionHandler == null){
            synchronized (this){
                if (settings.mCommonExceptionHandler == null){
                    settings.mCommonExceptionHandler = new DefaultCommonExceptionHandler();
                }
            }
        }
        return settings.mCommonExceptionHandler;
    }

    private DiskCacheExceptionHandler getDiskCacheExceptionHandler() {
        if (settings.mDiskCacheExceptionHandler == null){
            synchronized (this){
                if (settings.mDiskCacheExceptionHandler == null){
                    settings.mDiskCacheExceptionHandler = new DefaultDiskCacheExceptionHandler();
                }
            }
        }
        return settings.mDiskCacheExceptionHandler;
    }

    /*********************************************************************
     * getter
     */

    /**
     * 获得内存缓存器
     */
    public BitmapCache getBitmapCache(){
        return mBitmapCache;
    }

    /**********************************************************************
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
        reduceMemoryCache();
    }

    @Override
    public void onDestroy() {
        destroy();
    }

}
