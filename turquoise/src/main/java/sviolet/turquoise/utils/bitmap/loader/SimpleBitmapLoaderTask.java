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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;

import java.lang.ref.WeakReference;

import sviolet.turquoise.utils.bitmap.loader.drawable.SafeBitmapDrawable;
import sviolet.turquoise.utils.bitmap.loader.entity.BitmapRequest;
import sviolet.turquoise.utils.bitmap.loader.listener.OnBitmapLoadedListener;
import sviolet.turquoise.common.statics.SpecialResourceId;
import sviolet.turquoise.utils.log.TLogger;

/**
 *
 * BitmapLoader加载任务<br/>
 * 实现OnBitmapLoadedListener接口<br/>
 * <br/>
 * {@link SafeBitmapDrawable}拥有防崩溃, 重新加载功能.<br/>
 * <br/>
 * ----已实现------------------------------------<br/>
 * <br/>
 * 1.[重要]提供unused()方法,用于废弃图片,可回收资源/取消加载任务<br/>
 * 2.利用View.setTag({@link SpecialResourceId.ViewTag#SimpleBitmapLoaderTask}, ?)在控件中绑定自身<br/>
 * 3.支持加载图设置<br/>
 * 3.加载成功设置图片,支持淡入效果<br/>
 * 4.加载失败重新加载,含次数限制<br/>
 * 5.内置{@link SafeBitmapDrawable}拥有防崩溃, 重新加载功能<br/>
 * <br/>
 * ----待实现------------------------------------<br/>
 * <Br/>
 * 1.控件设置图片<br/>
 * <Br/>
 * ----注意事项-----------------------------------<br/>
 * <br/>
 * 1.该类利用View.setTag({@link SpecialResourceId.ViewTag#SimpleBitmapLoaderTask}, ?)在控件中绑定自身,
 * 请勿在View上使用相同的key({@link SpecialResourceId.ViewTag#SimpleBitmapLoaderTask})绑定其他Tag<p/>
 *
 * 2.若设置了加载图(loadingBitmap), 加载出来的TransitionDrawable尺寸等于目的图<p/>
 *
 * @author S.Violet
 *
 * Created by S.Violet on 2015/10/16.
 */
public abstract class SimpleBitmapLoaderTask<V extends View> implements OnBitmapLoadedListener {

    private TLogger logger = TLogger.get(this);

    //图片加载请求参数
    private BitmapRequest request;

    //图片重新加载次数
    private int reloadTimes = 0;
    //记录由加载任务设置的Drawable的hashCode, 用于判断控件的Drawable是否意外改变
    private int drawableHashCode = 0;

    //绑定的控件
    private WeakReference<V> view;
    //加载器
    private WeakReference<SimpleBitmapLoader> loader;

    //任务状态/////////////////////////////

    public static final int STATE_WAITING = 0;//等待加载
    public static final int STATE_LOADING = 1;//加载中
    public static final int STATE_LOADED = 2;//加载成功
    public static final int STATE_UNUSED = 3;//弃用任务

    private int state = STATE_WAITING;

    public SimpleBitmapLoaderTask(BitmapRequest request, SimpleBitmapLoader loader, V view){
        if (request == null)
            throw new RuntimeException("[SimpleBitmapLoaderTask]constractor: request must not be null");
        if (loader == null)
            throw new RuntimeException("[SimpleBitmapLoaderTask]constractor: loader must not be null");
        if (view == null)
            throw new RuntimeException("[SimpleBitmapLoaderTask]constractor:　view must not be null");

        this.request = request;
        this.loader = new WeakReference<SimpleBitmapLoader>(loader);

        bindView(view);//绑定控件
        resetToDefault();//设为加载图
        load();//加载
    }

    /**
     * [重要]弃用图片并取消加载任务<Br/>
     * <br/>
     *      不再使用的图片须及时用该方法废弃,尤其是大量图片的场合,未被废弃(unused)的图片
     *      将不会被BitmapLoader回收.请参看SimpleBitmapLoader"名词解释".<br/>
     *      该方法能取消加载任务,有助于减少不必要的加载,节省流量,使需要显示的图片尽快加载.<br/>
     */
    public void unused(){
        //废弃图片/取消任务
        if (getLoader() != null) {
            getLoader().unused(request.getUrl());
        }
        //解除绑定
        unbindView();
        //置为弃用
        state = STATE_UNUSED;
    }

    /****************************************************************
     * private
     */

    /**
     * 给控件设置图片(Bitmap)<br/>
     */
    private void setBitmap(V view, Resources resources, Bitmap bitmap, int animationDuration){
        if (view == null){
            logger.e("setBitmap: view is null");
            return;
        }
        //生成Drawable
        Drawable drawable = createDrawable(resources, bitmap);
        //记录hashCode
        drawableHashCode = drawable.hashCode();
        //控件设置Drawable
        setDrawable(view, drawable);
        //开始动画
        if (drawable instanceof TransitionDrawable){
            ((TransitionDrawable) drawable).startTransition(animationDuration);
        }
    }

    /**
     * 根据Bitmap创建Drawable, 注意null的情况
     */
    private Drawable createDrawable(Resources resources, Bitmap bitmap){
        if (bitmap == null || bitmap.isRecycled()){
            //获得加载图
            return getLoadingDrawable(false);
        }else{
            //目的图
            Drawable targetDrawable = new ReloadableSafeBitmapDrawable(resources, bitmap)
                    .setLoaderTask(this);
            //若淡入动画时间为0, 则直接返回
            if (getAnimationDuration() <= 0){
                return targetDrawable;
            }
            TransitionDrawable drawable = new TransitionDrawable(new Drawable[]{
                    getLoadingDrawable(true),//获得目的图的背景图
                    targetDrawable
            });
            drawable.setCrossFadeEnabled(true);//加载图消失
            return drawable;
        }
    }

    /**
     * 绑定控件<br/>
     * 会将原有任务取消<br/>
     *
     * @param view
     */
    protected void bindView(V view){
        if (view == null)
            return;

        //持有控件
        this.view = new WeakReference<V>(view);

        synchronized (view) {
            //原有tag
            Object tag = view.getTag(SpecialResourceId.ViewTag.SimpleBitmapLoaderTask);
            //将原有任务取消
            if (tag != null && tag instanceof SimpleBitmapLoaderTask) {
                ((SimpleBitmapLoaderTask) tag).unused();
            }else if (tag != null){
                /**
                 * SimpleBitmapLoaderTask利用View.setTag(TAG_KEY, ?)将自身绑定在控件上, 若用相同的TAG_KEY,
                 * 在控件上绑定TAG将会出问题, 若抛出此异常, 请检查代码中是否用到View.setTag(?, ?), 且key是否
                 * 有可能相同, 采用了SimpleBitmapLoader加载的View尽可能使用.setTag(?).
                 */
                throw new RuntimeException("[SimpleBitmapLoaderTask]view's tag key conflict, key<" + SpecialResourceId.ViewTag.SimpleBitmapLoaderTask);
            }
            //绑定本任务
            view.setTag(SpecialResourceId.ViewTag.SimpleBitmapLoaderTask, this);
        }
    }

    /**
     * 解绑控件<br/>
     */
    protected void unbindView(){
        View view = getView();
        if (view == null)
            return;

        synchronized (view) {
            //原有tag
            Object tag = view.getTag(SpecialResourceId.ViewTag.SimpleBitmapLoaderTask);
            //若View的标签为本任务, 则设置标签为null
            if (tag == this) {
                view.setTag(SpecialResourceId.ViewTag.SimpleBitmapLoaderTask, null);
            }
        }
    }

    /**
     * 加载
     */
    protected void load() {
        if (getLoader() == null){
            return;
        }
        //初始状态和加载完成状态允许加载
        if (state == STATE_WAITING || state == STATE_LOADED) {
            state = STATE_LOADING;
            getLoader().load(this);
        }
    }

    /**
     * 重加载(出现错误时,有次数限制)
     */
    protected void reload(){
        //判断重加载次数
        if (getLoader() != null && reloadTimes < getLoader().getReloadTimesMax()) {
            reloadTimes++;
            load();
        }else{
            //重新加载失败弃用任务
            unused();
        }
    }

    /**
     * 图片重置为默认图或透明图层
     */
    protected void resetToDefault(){
        setBitmap(getView(), null, null, 0);
    }

    /**
     * 检查绑定的View
     *
     * @param url 加载成功的url
     */
    protected boolean checkView(String url){
        if (getView() == null) {
            //绑定View为空
            return true;
        }
        synchronized (getView()) {
            if (getView().getTag(SpecialResourceId.ViewTag.SimpleBitmapLoaderTask) == null){
                //tag为空, 视为任务成功
                return false;
            }
            if (!(getView().getTag(SpecialResourceId.ViewTag.SimpleBitmapLoaderTask) instanceof SimpleBitmapLoaderTask)) {
                //tag不为SimpleBitmapLoaderTask
                /**
                 * SimpleBitmapLoaderTask利用View.setTag(TAG_KEY, ?)将自身绑定在控件上, 若用相同的TAG_KEY,
                 * 在控件上绑定TAG将会出问题, 若抛出此异常, 请检查代码中是否用到View.setTag(?, ?), 且key是否
                 * 有可能相同, 采用了SimpleBitmapLoader加载的View尽可能使用.setTag(?).
                 */
                throw new RuntimeException("[SimpleBitmapLoaderTask]view's tag key conflict, key<" + SpecialResourceId.ViewTag.SimpleBitmapLoaderTask);
            }
            //SimpleBitmapLoaderTask中url
            String taskUrl = ((SimpleBitmapLoaderTask) getView().getTag(SpecialResourceId.ViewTag.SimpleBitmapLoaderTask)).getRequest().getUrl();
            //若加载成功的url和SimpleBitmapLoaderTask中的url不同, 则取消任务不予显示
            if (url != null && !url.equals(taskUrl)) {
                return true;
            }
        }
        return false;
    }

    protected SimpleBitmapLoader getLoader(){
        if (loader != null)
            return loader.get();
        return null;
    }

    protected Resources getResources(){
        if (getLoader() != null && getLoader().getContext() != null)
            return getLoader().getContext().getResources();
        return null;
    }

    protected V getView(){
        if (view != null)
            return view.get();
        return null;
    }

    /**
     * @param forTransitionBackground 用于TransitionDrawable的背景图
     */
    protected Drawable getLoadingDrawable(boolean forTransitionBackground){
        if (getLoader() != null){
            if (getLoader().getLoadingDrawableFactory() != null){//动态加载图
                if (forTransitionBackground) {
                    //作为目的图背景时, 返回工厂的背景图
                    return getLoader().getLoadingDrawableFactory().newBackgroundDrawable();
                } else {
                    //作为加载图时, 返回动态加载图
                    return getLoader().getLoadingDrawableFactory().newLoadingDrawable().setLoaderTask(this);
                }
            } else if (getLoader().getLoadingBitmap() != null && !getLoader().getLoadingBitmap().isRecycled()){//加载图
                if (forTransitionBackground){
                    //作为目的图背景时, 返回尺寸为match_parent的SafeBitmapDrawable
                    return new SafeBitmapDrawable(getResources(), getLoader().getLoadingBitmap())
                            .setMatchParent(true);
                } else {
                    //作为加载图时, 返回普通SafeBitmapDrawable
                    return new SafeBitmapDrawable(getResources(), getLoader().getLoadingBitmap());
                }
            }else{//加载颜色
                return new ColorDrawable(getLoader().getLoadingColor());
            }
        }
        return new ColorDrawable(0x00000000);
    }

    protected int getAnimationDuration(){
        if (getLoader() != null)
            return getLoader().getAnimationDuration();
        return 0;
    }

    protected BitmapRequest getRequest(){
        return request;
    }

    /**
     * 加载任务执行状态<br/>
     * SimpleBitmapLoaderTask.STATE_WAITING = 0;//等待加载<br/>
     * SimpleBitmapLoaderTask.STATE_LOADING = 1;//加载中<br/>
     * SimpleBitmapLoaderTask.STATE_LOADED = 2;//加载成功<br/>
     * SimpleBitmapLoaderTask.STATE_UNUSED = 3;//弃用任务<br/>
     */
    public int getState(){
        return state;
    }

    /**
     * 检查绑定的View是否有意外变化
     */
    protected boolean checkViewModified(){
        //绑定的View不存在, 表示View有意外变化
        if (getView() == null){
            return true;
        }
        synchronized (getView()) {
            Drawable drawable = getDrawable(getView());
            if (drawable == null) {
                //drawable不存在, 但记录的hashCode不为0, 表示View有意外变化
                if (drawableHashCode != 0) {
                    return true;
                }
            } else {
                //drawable存在, 但与记录的hashCode不同, 表示View有意外变化
                if (drawable.hashCode() != drawableHashCode) {
                    return true;
                }
            }
            return false;
        }
    }

    /*****************************************************
     * OnBitmapLoadedListener 回调
     */

    @Override
    public void onLoadSucceed(BitmapRequest request, Object params, Bitmap bitmap) {
        //加载结束
        state = STATE_WAITING;

        //检查
        if (checkView(request.getUrl())){
            unused();
            return;
        }

        if (bitmap == null || bitmap.isRecycled()) {
            //重新加载
            reload();
        }else {
            //加载成功
            state = STATE_LOADED;
            //加载成功,设置图片
            setBitmap(getView(), getResources(), bitmap, getAnimationDuration());
        }
    }

    @Override
    public void onLoadFailed(BitmapRequest request, Object params) {
        //加载结束
        state = STATE_WAITING;

        //检查
        if (checkView(request.getUrl())){
            unused();
            return;
        }

        //重新加载
        reload();
    }

    @Override
    public void onLoadCanceled(BitmapRequest request, Object params) {
        //加载结束
        state = STATE_WAITING;

        //检查
        if (checkView(request.getUrl())){
            unused();
            return;
        }

        //弃用任务
        unused();
    }

    /*****************************************************
     * 待实现
     */

    /**
     * [复写]<br/>
     * 实现给控件设置图片(Drawable)
     * @param view 控件
     * @param drawable 图片
     */
    protected abstract void setDrawable(V view, Drawable drawable);

    /**
     * [复写]<br/>
     * 实现从控件获取图片(Drawable), 用于判断控件的图片是否被意外改变
     * @param view 控件
     */
    protected abstract Drawable getDrawable(V view);

    /*****************************************************
     * 内部类
     */

    /**
     * 可重加载的SafeBitmapDrawable<p/>
     *
     * 实现了绘制错误重新加载的功能<br/>
     */
    static class ReloadableSafeBitmapDrawable extends SafeBitmapDrawable{

        private TLogger logger = TLogger.get(this);

        private WeakReference<SimpleBitmapLoaderTask> loaderTask;//加载任务

        public ReloadableSafeBitmapDrawable(Resources res, Bitmap bitmap) {
            super(res, bitmap);
        }

        @Override
        protected void onDrawError(Canvas canvas, Exception e){
            //重新加载图片
            if (getLoaderTask() != null){
                logger.d("draw: reload url<" + getLoaderTask().getRequest().getUrl() + ">");
                getLoaderTask().resetToDefault();//设置为加载图
                getLoaderTask().load();//重加载(不限制次数)
            }
        }

        public SafeBitmapDrawable setLoaderTask(SimpleBitmapLoaderTask loaderTask){
            this.loaderTask = new WeakReference<SimpleBitmapLoaderTask>(loaderTask);
            return this;
        }

        private SimpleBitmapLoaderTask getLoaderTask(){
            if (loaderTask != null)
                return loaderTask.get();
            return null;
        }

    }

}
