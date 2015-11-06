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
import sviolet.turquoise.utils.bitmap.loader.handler.DiskCacheExceptionHandler;
import sviolet.turquoise.utils.bitmap.loader.task.SimpleBitmapLoaderTaskFactory;
import sviolet.turquoise.utils.cache.BitmapCache;
import sviolet.turquoise.view.drawable.SafeBitmapDrawable;

/**
 * 
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
 *      加载任务会绑定在控件TAG上(View.setTag()),请勿给控件设置自定义Tag,会导致加载失败.<Br/>
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
 * 4.reduce <br/>
 *      强制清空内存缓存中不再使用(unused)的图片.<br/>
 *      用于暂时减少缓存的内存占用,请勿频繁调用.<br/>
 *      通常是内存紧张的场合, 可以在Activity.onStop()中调用, Activity暂时不显示的情况下,
 *      将缓存中已被标记为unused的图片回收掉, 减少内存占用. 但这样会使得重新显示时, 加载
 *      变慢(需要重新加载).<p/>
 *
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
 *      采用SafeBitmapDrawable,当显示中的Bitmap被意外回收时,会绘制空白,但不会重新加载图片.<br/>
 *      {@link SafeBitmapDrawable}<Br/>
 * 4.若设置了加载图(loadingBitmap), 加载出来的TransitionDrawable尺寸等于目的图.<br/>
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
 * 4.Exception::[SimpleBitmapLoaderTask]don't use View.setTag() when view load by SimpleBitmapLoader!!<Br/>
 *      使用SimpleBitmapLoader加载控件时, 控件禁止使用View.setTag()自行设置TAG,
 *      因为SimpleBitmapLoader会把SimpleBitmapLoaderTask通过setTag()绑定在控件上!<Br/>
 * <Br/>
 * <Br/>
 * 
 *
 * @author S.Violet
 *
 * Created by S.Violet on 2015/10/19.
 */
public class SimpleBitmapLoader {

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
     * 禁用磁盘缓存后再次启动(流量增大风险,特殊场合使用)<p/>
     *
     * 若内存缓存中不存在图片,则直接从网络加载,且加载后不存入磁盘缓存.用于磁盘缓存打不开的场合,
     * 建议询问客户是否允许不使用磁盘缓存.<p/>
     *
     * 注意:仅磁盘缓存打开失败的情况可用, 详情请看{@link DiskCacheExceptionHandler}.onCacheOpenException<Br/>
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
     * 3.切勿自行给控件设置TAG(View.setTag()), 会无法加载!<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     * 
     *
     * @param url url
     * @param reqWidth 需求宽度
     * @param reqHeight 需求高度
     * @param view 被加载的控件(禁止使用View.setTag())
     */
    public void load(String url, int reqWidth, int reqHeight, View view){
        if (checkInput(url, reqWidth, reqHeight, view))
            return;
        newLoaderTask(url, reqWidth, reqHeight, view);
    }

    protected void newLoaderTask(String url, int reqWidth, int reqHeight, View view){
        SimpleBitmapLoaderTaskFactory.newLoaderTask(url, reqWidth, reqHeight, this, view);
    }

    /**
     * 
     * 异步加载控件背景图<br/>
     * <br/>
     * 注意:<Br/>
     * 1.该方法会弃用(unused)先前绑定在控件上的加载任务<br/>
     * 2.一个View不会重复加载相同的url<br/>
     * 3.切勿自行给控件设置TAG(View.setTag()), 会无法加载!<br/>
     * <br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     * <Br/>
     * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).图片解码时会
     * 根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     * 
     *
     * @param url url
     * @param reqWidth 需求宽度
     * @param reqHeight 需求高度
     * @param view 被加载的控件(禁止使用View.setTag())
     */
    public void loadBackground(String url, int reqWidth, int reqHeight, View view){
        if (checkInput(url, reqWidth, reqHeight, view))
            return;
        newBackgroundLoaderTask(url, reqWidth, reqHeight, view);
    }

    protected void newBackgroundLoaderTask(String url, int reqWidth, int reqHeight, View view){
        SimpleBitmapLoaderTaskFactory.newBackgroundLoaderTask(url, reqWidth, reqHeight, this, view);
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

    /**********************************************************************
     * inner class
     */

    /**
     * 配置, 继承自BitmapLoader.Settings
     */
    static class Settings extends BitmapLoader.Settings{

        Bitmap loadingBitmap;//加载状态的图片(二选一)
        int loadingColor = 0x00000000;//加载状态的颜色(二选一)
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
        bitmapLoader.load(task.getUrl(), task.getReqWidth(), task.getReqHeight(), null, task);
    }

    protected void unused(String url){
        bitmapLoader.unused(url);
    }

    /**
     * 检查输入<br/>
     * 1.检查url和view是否为空<br/>
     * 2.判断url与原有任务是否相同, 若相同则返回true, 不再次加载.<br/>
     */
    protected boolean checkInput(String url, int reqWidth, int reqHeight, View view){
        //bitmapLoader状态异常
        if (bitmapLoader.checkIsOpen())
            return true;
        //检查输入
        if (view == null)
            throw new RuntimeException("[SimpleBitmapLoader]view must not be null");
        if (url == null)
            throw new RuntimeException("[SimpleBitmapLoader]url must not be null");
        //取View绑定的加载任务
        Object tag = view.getTag();
        if (tag != null && tag instanceof SimpleBitmapLoaderTask){
            //任务未被弃用 且 图片未被篡改 且 URL相同, 跳过加载
            SimpleBitmapLoaderTask task = ((SimpleBitmapLoaderTask) tag);
            if (!task.isUnused() && !task.checkViewModified() && url.equals(task.getUrl())){
                if (getLogger() != null)
                    getLogger().i("[SimpleBitmapLoader]load skipped (url:" + url + "), because of the same url");
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

    public int getReloadTimesMax() {
        return getSettings().reloadTimesMax;
    }

}
