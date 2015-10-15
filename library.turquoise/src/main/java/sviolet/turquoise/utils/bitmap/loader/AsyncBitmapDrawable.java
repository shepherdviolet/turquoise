package sviolet.turquoise.utils.bitmap.loader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import sviolet.turquoise.view.drawable.TransitionBitmapDrawable;

/**
 * Created by S.Violet on 2015/10/15.
 */
public class AsyncBitmapDrawable extends TransitionBitmapDrawable implements OnBitmapLoadedListener {

    private static final long RELOAD_DELAY = 2000;//图片加载失败重新加载时延
    private static final int RELOAD_TIMES_LIMIT = 2;//图片加载失败重新加载次数限制
    private int reloadTimes = 0;//图片重新加载次数

    private String url;
    private int reqWidth;
    private int reqHeight;

    //加载器
    private WeakReference<AsyncBitmapDrawableLoader> loader;
    //Resources
    private WeakReference<Resources> resources;
    //加载器加载中
    private boolean loading = false;
    //是否被弃用
    private boolean unused = false;

    private int animationDuration = 500;//图片由浅及深显示的动画持续时间

    AsyncBitmapDrawable(String url, int reqWidth, int reqHeight, AsyncBitmapDrawableLoader loader) {
        super(loader.getResources(), loader.getLoadingBitmap());
        this.url = url;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
        this.loader = new WeakReference<AsyncBitmapDrawableLoader>(loader);
        this.animationDuration = loader.getAnimationDuration();
        if (loader.getResources() != null)
            this.resources = new WeakReference<Resources>(loader.getResources());

        loader.load(this);
    }

    AsyncBitmapDrawable(String url, int reqWidth, int reqHeight, AsyncBitmapDrawableLoader loader, Bitmap bitmap) {
        super(loader.getResources(), loader.getLoadingBitmap());
        this.url = url;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
        this.loader = new WeakReference<AsyncBitmapDrawableLoader>(loader);
        this.animationDuration = loader.getAnimationDuration();
        if (loader.getResources() != null)
            this.resources = new WeakReference<Resources>(loader.getResources());

        setBitmap(loader.getResources(), bitmap, animationDuration);
    }

    /**
     * [重要]尝试取消加载任务<Br/>
     * <br/>
     * 当图片不再显示时,及时unused有助于减少不必要的加载,节省流量,使需要显示的图片尽快加载.
     * 例如:ListView高速滑动时,中间很多项是来不及加载的,也无需显示图片,及时取消加载任务,可
     * 以跳过中间项的加载,使滚动停止后需要显示的项尽快加载出来.<br/>
     */
    public void unused(){
        //取消加载任务
        if (getLoader() != null)
            getLoader().unused(url);
        this.unused = true;
        //停止重加载
        destroyHandler();
    }

    /**
     * 设置图片加载失败后重新加载次数限制<Br/>
     */
    public AsyncBitmapDrawable setReloadTimes(int times){
        this.reloadTimes = times;
        return this;
    }

    /**
     * 加载
     */
    private void load() {
        //加载开始
        if (getLoader() != null && !loading && !unused) {
            loading = true;
            getLoader().load(this);
        }
    }

    /**
     * 重加载(出现错误时,有次数限制)
     */
    private void reload(){
        //未被弃用的情况下重新加载图片
        if (getLoader() != null && !unused && reloadTimes < RELOAD_TIMES_LIMIT) {
            reloadTimes++;
            getHandler().sendEmptyMessageDelayed(HANDLER_RELOAD, RELOAD_DELAY);//重新加载图片
        }
    }

    private AsyncBitmapDrawableLoader getLoader(){
        if (loader != null){
            return loader.get();
        }
        return null;
    }

    private Resources getResources(){
        if (resources != null){
            return resources.get();
        }
        return null;
    }

    /*****************************************************
     * OnBitmapLoadedListener 回调
     */

    /**
     * 当加载器加载成功
     */
    @Override
    public void onLoadSucceed(String url, Object params, Bitmap bitmap) {
        //加载结束
        loading = false;
        if (bitmap == null || bitmap.isRecycled()) {
            //重置图片
            resetToDefault();
            //重新加载
            reload();
        }else {
            setBitmap(getResources(), bitmap, animationDuration);
        }
    }

    /**
     * 当加载器加载失败
     */
    @Override
    public void onLoadFailed(String url, Object params) {
        //加载结束
        loading = false;
        //重置图片
        resetToDefault();
        //重新加载
        reload();
    }

    /**
     * 当加载器加载取消
     */
    @Override
    public void onLoadCanceled(String url, Object params) {
        //加载结束
        loading = false;
        //重置图片
        resetToDefault();
    }

    /**********************************************************
     * getter
     */

    public String getUrl() {
        return url;
    }

    public int getReqWidth() {
        return reqWidth;
    }

    public int getReqHeight() {
        return reqHeight;
    }

    /**********************************************************
     * override
     */

    @Override
    protected void onDrawError() {
        resetToDefault();
        reload();
    }

    /**********************************************************
     * handler
     */

    private static final int HANDLER_RELOAD = 1;//重新加载图片

    private Handler handler;

    private Handler getHandler(){
        if (handler == null){
            synchronized (this) {
                if (handler == null) {
                    handler = new Handler(new HandlerCallback());
                }
            }
        }
        return handler;
    }

    private void destroyHandler(){
        if (handler != null){
            synchronized (this){
                if (handler != null){
                    handler.removeCallbacksAndMessages(null);
                    handler = null;
                }
            }
        }
    }

    private class HandlerCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case HANDLER_RELOAD:
                    //重新加载图片
                    load();
                    break;
                default:
                    break;
            }
            return true;
        }
    }

}
