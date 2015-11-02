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

package sviolet.turquoise.utils.bitmap.loader.enhanced;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;

import java.lang.ref.WeakReference;

import sviolet.turquoise.utils.Logger;
import sviolet.turquoise.utils.bitmap.loader.OnBitmapLoadedListener;
import sviolet.turquoise.view.drawable.SafeBitmapDrawable;

/**
 * 
 * BitmapLoader加载任务<br/>
 * 实现OnBitmapLoadedListener接口<br/>
 * <br/>
 * 简易的"防回收崩溃",必需配合BitmapLoader回收站使用.<br/>
 * {@link SafeBitmapDrawable}<br/>
 * <br/>
 * ----已实现------------------------------------<br/>
 * <br/>
 * 1.[重要]提供unused()方法,用于废弃图片,可回收资源/取消加载任务<br/>
 * 2.利用View.setTag()在控件中绑定自身<br/>
 * 3.支持加载图设置<br/>
 * 3.加载成功设置图片,支持淡入效果<br/>
 * 4.加载失败重新加载,含次数限制<br/>
 * 5.加载结束后,解除View对本对象的持有<br/>
 * <br/>
 * ----待实现------------------------------------<br/>
 * <Br/>
 * 1.控件设置图片<br/>
 * <Br/>
 * ----注意事项-----------------------------------<br/>
 * <br/>
 * 该类占用了控件(View)的Tag用于绑定本身, 若控件设置另外的Tag(View.setTag())将会无法正常使用<br/>
 * 
 *
 * @author S.Violet
 *
 * Created by S.Violet on 2015/10/16.
 */
public abstract class SimpleBitmapLoaderTask<V extends View> implements OnBitmapLoadedListener {

    private int reloadTimes = 0;//图片重新加载次数

    private String url;
    private int reqWidth;
    private int reqHeight;

    //记录由加载任务设置的Drawable的hashCode, 用于判断控件的Drawable是否意外改变
    private int drawableHashCode = 0;

    //绑定的控件
    private WeakReference<V> view;
    //加载器
    private WeakReference<SimpleBitmapLoader> loader;
    //加载器加载中
    private boolean loading = false;
    //是否被弃用
    private boolean unused = false;

    public SimpleBitmapLoaderTask(String url, int reqWidth, int reqHeight, SimpleBitmapLoader loader, V view){
        this.url = url;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
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
            getLoader().unused(url);
        }
        //解除绑定
        unbindView();
        //置为弃用
        unused = true;
    }

    /****************************************************************
     * private
     */

    /**
     * 给控件设置图片(Bitmap)<br/>
     */
    private void setBitmap(V view, Resources resources, Bitmap bitmap, int animationDuration){
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
            return getLoadingDrawable();
        }else{
            TransitionDrawable drawable = new TransitionDrawable(new Drawable[]{getLoadingDrawable(), new SafeBitmapDrawable(resources, bitmap).setLogger(getLogger())});
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
            Object tag = view.getTag();
            //将原有任务取消
            if (tag != null && tag instanceof SimpleBitmapLoaderTask) {
                ((SimpleBitmapLoaderTask) tag).unused();
            }else if (tag != null){
                /**
                 * 使用SimpleBitmapLoader加载控件时, 控件禁止使用View.setTag()自行设置TAG,
                 * 因为SimpleBitmapLoader会把SimpleBitmapLoaderTask通过setTag()绑定在控件上!
                 */
                throw new RuntimeException("[SimpleBitmapLoaderTask]don't use View.setTag() when view load by SimpleBitmapLoader!!");
            }
            //绑定本任务
            view.setTag(this);
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
            Object tag = view.getTag();
            //若View的标签为本任务, 则设置标签为null
            if (tag == this) {
                view.setTag(null);
            }
        }
    }

    /**
     * 加载
     */
    protected void load() {
        //加载开始
        if (getLoader() != null && !loading && !unused) {
            loading = true;
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
            if (getView().getTag() == null){
                //tag为空, 视为任务成功
                return false;
            }
            if (!(getView().getTag() instanceof SimpleBitmapLoaderTask)) {
                //tag不为SimpleBitmapLoaderTask
                /**
                 * 使用SimpleBitmapLoader加载控件时, 控件禁止使用View.setTag()自行设置TAG,
                 * 因为SimpleBitmapLoader会把SimpleBitmapLoaderTask通过setTag()绑定在控件上!
                 */
                throw new RuntimeException("[SimpleBitmapLoaderTask]don't use View.setTag() when view load by SimpleBitmapLoader!!");
            }
            //SimpleBitmapLoaderTask中url
            String taskUrl = ((SimpleBitmapLoaderTask) getView().getTag()).getUrl();
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
        if (getLoader() != null)
            return getLoader().getResources();
        return null;
    }

    protected V getView(){
        if (view != null)
            return view.get();
        return null;
    }

    protected Drawable getLoadingDrawable(){
        if (getLoader() != null){
            Bitmap loadingBitmap = getLoader().getLoadingBitmap();
            if (loadingBitmap != null && !loadingBitmap.isRecycled()){
                return new SafeBitmapDrawable(getResources(), loadingBitmap).setLogger(getLogger());
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
        return url;
    }

    protected int getReqWidth(){
        return reqWidth;
    }

    protected int getReqHeight(){
        return reqHeight;
    }

    /**
     * 是否被弃用
     */
    protected boolean isUnused(){
        return unused;
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
    public void onLoadSucceed(String url, int reqWidth, int reqHeight, Object params, Bitmap bitmap) {
        //加载结束
        loading = false;

        //检查
        if (checkView(url)){
            unused();
            return;
        }

        if (bitmap == null || bitmap.isRecycled()) {
            //重置图片
            resetToDefault();
            //重新加载
            reload();
        }else {
            //加载成功,设置图片
            setBitmap(getView(), getResources(), bitmap, getAnimationDuration());
        }
    }

    @Override
    public void onLoadFailed(String url, int reqWidth, int reqHeight, Object params) {
        //加载结束
        loading = false;

        //检查
        if (checkView(url)){
            unused();
            return;
        }

        //重置图片
        resetToDefault();
        //重新加载
        reload();
    }

    @Override
    public void onLoadCanceled(String url, int reqWidth, int reqHeight, Object params) {
        //加载结束
        loading = false;

        //检查
        if (checkView(url)){
            unused();
            return;
        }

        //重置图片
        resetToDefault();
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

}
