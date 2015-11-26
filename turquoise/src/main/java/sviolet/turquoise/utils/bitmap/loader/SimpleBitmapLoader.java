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

package sviolet.turquoise.utils.bitmap.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import sviolet.turquoise.utils.Logger;
import sviolet.turquoise.utils.bitmap.loader.drawable.AbsLoadingDrawableFactory;
import sviolet.turquoise.utils.bitmap.loader.entity.BitmapRequest;
import sviolet.turquoise.utils.bitmap.loader.handler.DefaultDiskCacheExceptionHandler;
import sviolet.turquoise.utils.bitmap.loader.handler.DiskCacheExceptionHandler;
import sviolet.turquoise.utils.bitmap.loader.task.SimpleBitmapLoaderTaskFactory;
import sviolet.turquoise.utils.cache.BitmapCache;
import sviolet.turquoise.utils.lifecycle.listener.LifeCycle;

/**
 * 
 * SimpleBitmapLoader<Br/>
 * 便捷图片双缓存网络异步加载器<br/>
 * <br/>
 * 代理强化BitmapLoader, 传入url和View, 自动完成异步加载显示, 错误重新加载, 默认图显示, 淡入效果等.<br/>
 * {@link sviolet.turquoise.utils.bitmap.loader.drawable.SafeBitmapDrawable}拥有防崩溃, 重新加载功能.<br/>
 * <br/>
 * ****************************************************************<br/>
 * * * * * SimpleBitmapLoader使用说明:<br/>
 * ****************************************************************<br/>
 * <br/>
 * -------------------初始化设置----------------<br/>
 * <br/>
 * <pre>{@code
 *       mSimpleBitmapLoader = new SimpleBitmapLoader.Builder(this, "bitmap", loadingBitmap)
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
 *          //.setLogger(getLogger())//设置日志打印器
 *          .setAnimationDuration(400)//设置图片淡入动画持续时间400ms
 *          .setReloadTimesMax(2)//设置图片加载失败重新加载次数限制
 *          .create();
 * }</pre>
 * <br/>
 * -------------------加载器使用----------------<br/>
 * <br/>
 * 1.load <br/>
 *      加载图片,自动完成异步加载显示,错误重新加载,默认图显示,淡入效果等.<Br/>
 *      加载方法会弃用(unused)先前绑定在控件上的加载任务.请注意.<p/>
 *
 * 2.unused [重要] <br/>
 *      不再使用的图片须及时用该方法废弃,尤其是大量图片的场合,未被废弃(unused)的图片
 *      将不会被BitmapLoader回收.请参看"名词解释".<br/>
 *      该方法能取消加载任务,有助于减少不必要的加载,节省流量,使需要显示的图片尽快加载.<p/>
 *
 * 3.destroy [重要] <br/>
 *      清除全部图片及加载任务,通常在Activity.onDestroy中调用<p/>
 *
 * 4.reduceMemoryCache [特殊] <br/>
 *      强制回收内存缓存中不再使用(unused)的图片<br/>
 *      用于暂时减少缓存的内存占用, 请勿频繁调用.<br/>
 *      通常是内存紧张的场合, 可以在Activity.onStop()中调用, Activity暂时不显示的情况下,
 *      将缓存中已被标记为unused的图片回收掉, 减少内存占用.<p/>
 *
 * 5.cleanMemoryCache [慎用] <br/>
 *      强制清空内存缓存.<br/>
 *      用于暂时清空缓存的内存占用, 仅在特殊场合使用. 通常在Activity.onStop()中调用, 在界面暂时不显示
 *      的情况下, 将内存缓存清空, 减少内存占用, 在界面重新显示时, 利用SimpleBitmapLoader的绘制异常重新
 *      加载特性, 显示失败的图片会重新加载.<p/>
 *
 * 6.cancelAllTasks [慎用] <br/>
 *      强制取消所有加载任务.用于BitmapLoader未销毁的情况下, 结束磁盘和网络的访问. 这会导致
 *      加载中的图片无法显示. <br/>
 *      Not recommended: This will cause the loading Bitmap to be unable to display. <p/>
 *
 * -------------------注意事项----------------<br/>
 * <br/>
 * 1.ListView等View复用的场合,可省略unused操作.<br/>
 *      load方法会先弃用(unused)先前绑定在控件上的加载任务.<br/>
 * 2.若设置了加载图(loadingBitmap), 加载出来的TransitionDrawable尺寸等于目的图.<br/>
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
 * Created by S.Violet on 2015/10/19.
 */
public class SimpleBitmapLoader implements LifeCycle {

    /**
     * 内部bitmapLoader<Br/>
     * 该类由代理方式实现<br/>
     */
    private BitmapLoader bitmapLoader;

    /**
     * 内部构造器, 利用settings构造实例
     */
    SimpleBitmapLoader(Settings settings) {
        bitmapLoader = new BitmapLoader(settings);
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
        bitmapLoader.openWithoutDiskCache();
    }

    /*********************************************************************
     * public
     */

    /**
     * 异步加载控件图片<p/>
     *
     * 目前支持控件, 详见{@link SimpleBitmapLoaderTaskFactory}:<br/>
     * 1.ImageView<br/>
     *
     * <br/>注意:<Br/>
     * 1.该方法会弃用(unused)先前绑定在控件上的加载任务<br/>
     * 2.一个View不会重复加载相同的url<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <Br/>
     * @see BitmapRequest
     *
     * @param url url
     * @param view 被加载的控件
     */
    public void load(String url, View view){
        load(new BitmapRequest(url), view);
    }

    /**
     * 异步加载控件图片<p/>
     *
     * 目前支持控件, 详见{@link SimpleBitmapLoaderTaskFactory}:<br/>
     * 1.ImageView<br/>
     *
     * <br/>注意:<Br/>
     * 1.该方法会弃用(unused)先前绑定在控件上的加载任务<br/>
     * 2.一个View不会重复加载相同的url<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <Br/>
     * @see BitmapRequest
     *
     * @param url url
     * @param reqWidth 需求宽度
     * @param reqHeight 需求高度
     * @param view 被加载的控件
     */
    public void load(String url, int reqWidth, int reqHeight, View view){
        load(new BitmapRequest(url).setReqDimension(reqWidth, reqHeight), view);
    }

    /**
     * 异步加载控件图片<p/>
     *
     * 目前支持控件, 详见{@link SimpleBitmapLoaderTaskFactory}:<br/>
     * 1.ImageView<br/>
     *
     * <br/>注意:<Br/>
     * 1.该方法会弃用(unused)先前绑定在控件上的加载任务<br/>
     * 2.一个View不会重复加载相同的url<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <Br/>
     * @see BitmapRequest
     *
     * @param request 图片加载请求参数
     * @param view 被加载的控件
     */
    public void load(BitmapRequest request, View view){
        if (checkInput(request, view))
            return;
        newLoaderTask(request, view);
    }

    protected void newLoaderTask(BitmapRequest request, View view){
        SimpleBitmapLoaderTaskFactory.newLoaderTask(request, this, view);
    }

    /**
     *
     * 异步加载控件背景图<br/>
     * <br/>
     * 注意:<Br/>
     * 1.该方法会弃用(unused)先前绑定在控件上的加载任务<br/>
     * 2.一个View不会重复加载相同的url<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <Br/>
     * @see BitmapRequest
     *
     * @param url url
     * @param view 被加载的控件
     */
    public void loadBackground(String url, View view) {
        loadBackground(new BitmapRequest(url), view);
    }

    /**
     * 
     * 异步加载控件背景图<br/>
     * <br/>
     * 注意:<Br/>
     * 1.该方法会弃用(unused)先前绑定在控件上的加载任务<br/>
     * 2.一个View不会重复加载相同的url<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <Br/>
     * @see BitmapRequest
     *
     * @param url url
     * @param reqWidth 需求宽度
     * @param reqHeight 需求高度
     * @param view 被加载的控件
     */
    public void loadBackground(String url, int reqWidth, int reqHeight, View view) {
        loadBackground(new BitmapRequest(url).setReqDimension(reqWidth, reqHeight), view);
    }

    /**
     *
     * 异步加载控件背景图<br/>
     * <br/>
     * 注意:<Br/>
     * 1.该方法会弃用(unused)先前绑定在控件上的加载任务<br/>
     * 2.一个View不会重复加载相同的url<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <Br/>
     * @see BitmapRequest
     *
     * @param request 图片加载请求参数
     * @param view 被加载的控件
     */
    public void loadBackground(BitmapRequest request, View view){
        if (checkInput(request, view))
            return;
        newBackgroundLoaderTask(request, view);
    }

    protected void newBackgroundLoaderTask(BitmapRequest request, View view){
        SimpleBitmapLoaderTaskFactory.newBackgroundLoaderTask(request, this, view);
    }

    /**
     * 
     * [重要]弃用图片并取消加载任务<Br/>
     * <br/>
     *      不再使用的图片须及时用该方法废弃,尤其是大量图片的场合,未被废弃(unused)的图片
     *      将不会被BitmapLoader回收.请参看SimpleBitmapLoader"名词解释".<br/>
     *      该方法能取消加载任务,有助于减少不必要的加载,节省流量,使需要显示的图片尽快加载.<br/>
     * 
     */
    public void unused(View view){
        if (view == null)
            return;
        synchronized (view) {
            //取tag
            Object tag = view.getTag(SimpleBitmapLoaderTask.TAG_KEY);
            //若tag为SimpleBitmapLoaderTask
            if (tag instanceof SimpleBitmapLoaderTask) {
                //弃用任务
                ((SimpleBitmapLoaderTask) tag).unused();
                //解除绑定
                view.setTag(SimpleBitmapLoaderTask.TAG_KEY, null);
            }
        }
    }

    /**
     * [特殊]强制回收内存缓存中不再使用(unused)的图片<p/>
     *
     * 用于暂时减少缓存的内存占用, 请勿频繁调用.<br/>
     * 通常是内存紧张的场合, 可以在Activity.onStop()中调用, Activity暂时不显示的情况下,
     * 将缓存中已被标记为unused的图片回收掉, 减少内存占用.<br/>
     */
    public void reduceMemoryCache(){
        bitmapLoader.reduceMemoryCache();
    }

    /**
     * [慎用]强制清空内存缓存<p/>
     *
     * 用于暂时清空缓存的内存占用, 仅在特殊场合使用. 通常在Activity.onStop()中调用, 在界面暂时不显示
     * 的情况下, 将内存缓存清空, 减少内存占用, 在界面重新显示时, 利用SimpleBitmapLoader的绘制异常重新
     * 加载特性, 显示失败的图片会重新加载.<br/>
     */
    public void cleanMemoryCache(){
        if (bitmapLoader.getBitmapCache() != null)
            bitmapLoader.getBitmapCache().removeAll();
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
        bitmapLoader.cancelAllTasks();
    }

    /**
     * [重要]将所有资源回收销毁, 建议在Activity.onDestroy()时调用该方法<br/>
     * 1.销毁内部持有的BitmapLoader<br/>
     * 2.销毁加载图(loadingBitmap)<br/>
     */
    public void destroy(){
        bitmapLoader.destroy();

        //销毁加载动画工厂
        if (getSettings().loadingDrawableFactory != null){
            getSettings().loadingDrawableFactory.destroy();
        }

        //回收加载图
        if (getSettings().loadingBitmap != null && !getSettings().loadingBitmap.isRecycled()){
            getSettings().loadingBitmap.recycle();
            getSettings().loadingBitmap = null;
        }
    }

    /**
     * BitmapLoader是否可用状态<br/>
     * 满足条件:<br/>
     * 1.启用成功(open)<br/>
     * 2.未销毁(destroy)<br/>
     */
    public boolean isActive(){
        return bitmapLoader.isActive();
    }

    /**
     * 获得内部的内存缓存器
     */
    public BitmapCache getBitmapCache(){
        return bitmapLoader.getBitmapCache();
    }

    /**
     * 获得内部的BitmapLoader
     */
    public BitmapLoader getBitmapLoader(){
        return bitmapLoader;
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
        BitmapLoader.wipeDiskCache(context, diskCacheName);
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
        BitmapLoader.wipeInnerDiskCache(context, diskCacheName);
    }

    /**********************************************************************
     * inner class
     */

    /**
     * 配置, 继承自BitmapLoader.Settings
     */
    static class Settings extends BitmapLoader.Settings{

        Bitmap loadingBitmap;//加载状态的图片(三选一)
        int loadingColor = 0x00000000;//加载状态的颜色(三选一)
        AbsLoadingDrawableFactory loadingDrawableFactory;//加载动态图工厂(三选一)

        int animationDuration = 500;//AsyncBitmapDrawable图片由浅及深显示的动画持续时间
        int reloadTimesMax = 2;//图片加载失败重新加载次数限制

    }

    /**
     * 构建器, 继承自BitmapLoader.AbsBuilder
     */
    public static class Builder extends BitmapLoader.AbsBuilder<Builder, SimpleBitmapLoader>{

        /**
         * @param context 上下文
         * @param diskCacheName 磁盘缓存目录
         * @param loadingBitmap 加载图, SimpleBitmapLoader销毁时, 加载图会一并销毁
         */
        public Builder(Context context, String diskCacheName, Bitmap loadingBitmap) {
            super(context, diskCacheName, new Settings());
            getSettings().loadingBitmap = loadingBitmap;
        }

        /**
         * @param context 上下文
         * @param diskCacheName 磁盘缓存目录
         * @param loadingColor 加载颜色
         */
        public Builder(Context context, String diskCacheName, int loadingColor) {
            super(context, diskCacheName, new Settings());
            getSettings().loadingColor = loadingColor;
        }

        /**
         * @param context 上下文
         * @param diskCacheName 磁盘缓存目录
         * @param loadingDrawableFactory 加载动态图工厂
         */
        public Builder(Context context, String diskCacheName, AbsLoadingDrawableFactory loadingDrawableFactory) {
            super(context, diskCacheName, new Settings());
            getSettings().loadingDrawableFactory = loadingDrawableFactory;
        }

        /**
         * 设置图片由浅及深显示动画效果持续时间
         * @param duration 持续时间 ms 默认500
         */
        public Builder setAnimationDuration(int duration){
            if (duration < 0)
                duration = 0;
            getSettings().animationDuration = duration;
            return this;
        }

        /**
         * 设置图片加载失败后重新加载次数限制<Br/>
         *
         * @param times 重新加载次数限制 默认值2
         */
        public Builder setReloadTimesMax(int times){
            getSettings().reloadTimesMax = times;
            return this;
        }

        /**
         * 创建SimpleBitmapLoader实例
         */
        public SimpleBitmapLoader create(){
            if (settings == null)
                throw new RuntimeException("[SimpleBitmapLoader.Builder]builder can't create repeatly");
            SimpleBitmapLoader loader = new SimpleBitmapLoader(getSettings());
            settings = null;
            return loader;
        }

        /**
         * 获得父类中的settings
         */
        private Settings getSettings(){
            return ((Settings)settings);
        }

    }

    /*********************************************************************
     * protected
     */

    protected void load(SimpleBitmapLoaderTask task){
        if (task == null)
            return;
        bitmapLoader.load(task.getRequest(), null, task);
    }

    protected void unused(String url){
        bitmapLoader.unused(url);
    }

    /**
     * 检查输入<br/>
     * 1.检查url和view是否为空<br/>
     * 2.判断url与原有任务是否相同, 若相同则返回true, 不再次加载.<br/>
     */
    protected boolean checkInput(BitmapRequest request, View view){
        //bitmapLoader状态异常
        if (bitmapLoader.checkIsOpen())
            return true;
        //检查输入
        if (view == null)
            throw new RuntimeException("[SimpleBitmapLoader]load: view must not be null");
        if (request == null)
            throw new RuntimeException("[SimpleBitmapLoader]load: request must not be null");
        //取View绑定的加载任务
        Object tag = view.getTag(SimpleBitmapLoaderTask.TAG_KEY);
        if (tag != null && tag instanceof SimpleBitmapLoaderTask){
            //任务未被弃用 且 图片未被篡改 且 URL相同, 跳过加载
            SimpleBitmapLoaderTask task = ((SimpleBitmapLoaderTask) tag);
            if (task.getState() != SimpleBitmapLoaderTask.STATE_UNUSED
                    && !task.checkViewModified()
                    && request.getUrl().equals(task.getRequest().getUrl())){
                if (getLogger() != null)
                    getLogger().i("[SimpleBitmapLoader]load skipped (url:" + request.getUrl() + "), because of the same url");
                return true;
            }
        }
        return false;
    }

    /**************************************************************
     * GETTER
     */

    protected Context getContext(){
        return bitmapLoader.getContext();
    }

    protected Logger getLogger(){
        return bitmapLoader.getLogger();
    }

    protected Settings getSettings(){
        return ((Settings)bitmapLoader.getSettings());
    }

    protected int getAnimationDuration(){
        return getSettings().animationDuration;
    }

    protected Bitmap getLoadingBitmap(){
        if (getSettings().loadingBitmap != null && getSettings().loadingBitmap.isRecycled()){
            return null;
        }
        return getSettings().loadingBitmap;
    }

    protected int getLoadingColor(){
        return getSettings().loadingColor;
    }

    protected AbsLoadingDrawableFactory getLoadingDrawableFactory(){
        return getSettings().loadingDrawableFactory;
    }

    public int getReloadTimesMax() {
        return getSettings().reloadTimesMax;
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
