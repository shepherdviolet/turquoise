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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import sviolet.turquoise.utils.Logger;
import sviolet.turquoise.utils.bitmap.loader.entity.BitmapRequest;
import sviolet.turquoise.utils.bitmap.loader.listener.OnBitmapLoadedListener;

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
 * 2.利用View.setTag({@link SimpleBitmapLoaderTask#TAG_KEY}, ?)在控件中绑定自身<br/>
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
 * 1.该类利用View.setTag({@link SimpleBitmapLoaderTask#TAG_KEY}, ?)在控件中绑定自身,
 * 请勿在View上使用相同的key({@link SimpleBitmapLoaderTask#TAG_KEY})绑定其他Tag<p/>
 *
 * 2.若设置了加载图(loadingBitmap), 加载出来的TransitionDrawable尺寸等于目的图<p/>
 *
 * @author S.Violet
 *
 * Created by S.Violet on 2015/10/16.
 */
public abstract class SimpleBitmapLoaderTask<V extends View> implements OnBitmapLoadedListener {

    //专用TagKey, 用于将自身作为TAG绑定在View上
    public static final int TAG_KEY = 0xff77f777;

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

    public static final int STATE_INIT = 0;//初始状态
    public static final int STATE_LOADING = 1;//加载中
    public static final int STATE_LOADED = 2;//加载完成
    public static final int STATE_UNUSED = 3;//弃用

    private int state = STATE_INIT;

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
            if (getLogger() != null)
                getLogger().e("[SimpleBitmapLoaderTask]setBitmap: view is null");
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
            //尺寸等于加载图
            return getLoadingDrawable(false);
        }else{
            //尺寸等于目的图
            TransitionDrawable drawable = new TransitionDrawable(new Drawable[]{
                    getLoadingDrawable(true),
                    new SafeBitmapDrawable(resources, bitmap)
                            .setLoaderTask(this)
                            .setLogger(getLogger())
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
            Object tag = view.getTag(TAG_KEY);
            //将原有任务取消
            if (tag != null && tag instanceof SimpleBitmapLoaderTask) {
                ((SimpleBitmapLoaderTask) tag).unused();
            }else if (tag != null){
                /**
                 * SimpleBitmapLoaderTask利用View.setTag(TAG_KEY, ?)将自身绑定在控件上, 若用相同的TAG_KEY,
                 * 在控件上绑定TAG将会出问题, 若抛出此异常, 请检查代码中是否用到View.setTag(?, ?), 且key是否
                 * 有可能相同, 采用了SimpleBitmapLoader加载的View尽可能使用.setTag(?).
                 */
                throw new RuntimeException("[SimpleBitmapLoaderTask]view's tag key conflict, key<" + TAG_KEY);
            }
            //绑定本任务
            view.setTag(TAG_KEY, this);
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
            Object tag = view.getTag(TAG_KEY);
            //若View的标签为本任务, 则设置标签为null
            if (tag == this) {
                view.setTag(TAG_KEY, null);
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
        if (state == STATE_INIT || state == STATE_LOADED) {
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
            if (getView().getTag(TAG_KEY) == null){
                //tag为空, 视为任务成功
                return false;
            }
            if (!(getView().getTag(TAG_KEY) instanceof SimpleBitmapLoaderTask)) {
                //tag不为SimpleBitmapLoaderTask
                /**
                 * SimpleBitmapLoaderTask利用View.setTag(TAG_KEY, ?)将自身绑定在控件上, 若用相同的TAG_KEY,
                 * 在控件上绑定TAG将会出问题, 若抛出此异常, 请检查代码中是否用到View.setTag(?, ?), 且key是否
                 * 有可能相同, 采用了SimpleBitmapLoader加载的View尽可能使用.setTag(?).
                 */
                throw new RuntimeException("[SimpleBitmapLoaderTask]view's tag key conflict, key<" + TAG_KEY);
            }
            //SimpleBitmapLoaderTask中url
            String taskUrl = ((SimpleBitmapLoaderTask) getView().getTag(TAG_KEY)).getUrl();
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

    protected Logger getLogger(){
        if (getLoader() != null){
            return getLoader().getLogger();
        }
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

    protected Drawable getLoadingDrawable(boolean noDimension){
        if (getLoader() != null){
            Bitmap loadingBitmap = getLoader().getLoadingBitmap();
            if (loadingBitmap != null && !loadingBitmap.isRecycled()){
                if (noDimension){
                    //尺寸为match_parent的SafeBitmapDrawable
                    return new NoDimensionSafeBitmapDrawable(getResources(), loadingBitmap)
                            .setLoaderTask(this)
                            .setLogger(getLogger());
                }else {
                    return new SafeBitmapDrawable(getResources(), loadingBitmap)
                            .setLoaderTask(this)
                            .setLogger(getLogger());
                }
            }else{
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

    protected String getUrl(){
        return request.getUrl();
    }

    protected int getReqWidth(){
        return request.getReqWidth();
    }

    protected int getReqHeight(){
        return request.getReqHeight();
    }

    /**
     * 加载任务执行状态<br/>
     * SimpleBitmapLoaderTask.STATE_INIT = 0;//初始状态<br/>
     * SimpleBitmapLoaderTask.STATE_LOADING = 1;//加载中<br/>
     * SimpleBitmapLoaderTask.STATE_LOADED = 2;//加载完成<br/>
     * SimpleBitmapLoaderTask.STATE_UNUSED = 3;//弃用<br/>
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
        state = STATE_LOADED;

        //检查
        if (checkView(request.getUrl())){
            unused();
            return;
        }

        if (bitmap == null || bitmap.isRecycled()) {
            //重新加载
            reload();
        }else {
            //加载成功,设置图片
            setBitmap(getView(), getResources(), bitmap, getAnimationDuration());
        }
    }

    @Override
    public void onLoadFailed(BitmapRequest request, Object params) {
        //加载结束
        state = STATE_LOADED;

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
        state = STATE_LOADED;

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
     * 无尺寸(自适应尺寸)的SafeBitmapDrawable<p/>
     *
     * 通过复写getIntrinsicWidth/getIntrinsicHeight两个方法,返回-1,
     * 使得该Drawable在计算尺寸时,作为match_parent处理.<p/>
     *
     * 用于TransitionDrawable,使得加载图尺寸等于目标图尺寸<p/>
     */
    private static class NoDimensionSafeBitmapDrawable extends SafeBitmapDrawable{

        public NoDimensionSafeBitmapDrawable(Resources res, Bitmap bitmap) {
            super(res, bitmap);
        }

        @Override
        public int getIntrinsicWidth() {
            return -1;
        }

        @Override
        public int getIntrinsicHeight() {
            return -1;
        }
    }


    /**
     * 安全的BitmapDrawable<br/>
     * <br/>
     * 简易地防回收崩溃, 当Bitmap被回收时, 绘制空白, 不会崩溃, 但并不会重新加载图片<Br/>
     *
     * Created by S.Violet on 2015/10/21.
     */
    static class SafeBitmapDrawable extends BitmapDrawable {

        private WeakReference<SimpleBitmapLoaderTask> loaderTask;//加载任务
        private Logger logger;//日志打印器
        private boolean drawEnable = true;//允许绘制

        public SafeBitmapDrawable(Resources res) {
            super(res);
        }

        public SafeBitmapDrawable(Bitmap bitmap) {
            super(bitmap);
        }

        public SafeBitmapDrawable(Resources res, Bitmap bitmap) {
            super(res, bitmap);
        }

        public SafeBitmapDrawable(String filepath) {
            super(filepath);
        }

        public SafeBitmapDrawable(Resources res, String filepath) {
            super(res, filepath);
        }

        public SafeBitmapDrawable(InputStream is) {
            super(is);
        }

        public SafeBitmapDrawable(Resources res, InputStream is) {
            super(res, is);
        }

        @Override
        public void draw(Canvas canvas) {
            if (drawEnable) {
                try {
                    super.draw(canvas);
                } catch (Exception e) {
                    //禁止绘制
                    drawEnable = false;
                    if (getLogger() != null) {
                        getLogger().e("[SafeBitmapDrawable]draw: error, catch exception: " + e.getMessage());
                    }
                    //重新加载图片
                    if (getLoaderTask() != null){
                        if (getLogger() != null) {
                            getLogger().e("[SafeBitmapDrawable]draw: reload url<" + getLoaderTask().getUrl() + ">");
                        }
                        getLoaderTask().resetToDefault();//设置为加载图
                        getLoaderTask().load();//重加载(不限制次数)
                    }
                }
            }else{
                if (getLogger() != null) {
                    getLogger().d("[SafeBitmapDrawable]draw: skip, because of exception");
                }
            }
        }

        public SafeBitmapDrawable setLoaderTask(SimpleBitmapLoaderTask loaderTask){
            this.loaderTask = new WeakReference<SimpleBitmapLoaderTask>(loaderTask);
            return this;
        }

        public SafeBitmapDrawable setLogger(Logger logger){
            this.logger = logger;
            return this;
        }

        private Logger getLogger(){
            return logger;
        }

        private SimpleBitmapLoaderTask getLoaderTask(){
            if (loaderTask != null)
                return loaderTask.get();
            return null;
        }
    }


}
