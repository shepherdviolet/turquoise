package sviolet.turquoise.utils.bitmap.loader.enhanced;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import sviolet.turquoise.utils.bitmap.loader.OnBitmapLoadedListener;
import sviolet.turquoise.view.drawable.TransitionBitmapDrawable;

/**
 * 异步BitmapDrawable, 防回收崩溃<br/>
 * <br/>
 * 由AsyncBitmapDrawableLoader.load方法返回.<br/>
 * 初始阶段显示由AsyncBitmapDrawableLoader指定的加载图, 当图片加载成功, 会自动显示目标图,
 * 若加载失败, 会尝试重新加载(有次数限制), 若显示的Bitmap被回收(recycle), 则会显示加载图
 * 或透明图层.<br/>
 * <br/>
 * 注意::<br/>
 * 不再使用时, 请及时调用unused方法, 以取消加载任务<br/>
 * <br/>
 * 其他说明:<br/>
 * @see sviolet.turquoise.view.drawable.TransitionBitmapDrawable
 * *********************************************************<br/>
 * * * * 注意事项<br/>
 * *********************************************************<br/>
 * <br/>
 * 1.由于通过反射机制实现核心代码, 在未来新API中可能会出现问题.<br/>
 * <br/>
 * *********************************************************<br/>
 * * * * 控件支持情况<br/>
 * *********************************************************<br/>
 * <br/>
 * ----支持控件-------------------------------<br/>
 * <br/>
 * 1.ImageView<Br/>
 * 支持尺寸重新计算,控件大小动态调整,支持wrap_content参数<br/>
 * <br/>
 * ----存在问题-------------------------------<br/>
 * <br/>
 * 除支持控件外.<br/>
 * 新设置的图片尺寸将和原来一致, 并不会根据实际情况重新调整尺寸, 特别是当控件尺寸设置为
 * wrap_content时, 控件长宽可能会初始化为0, 导致新设置的图片无法显示, 建议控件尺寸设置
 * 为固定值或给TransitionBitmapDrawable设置默认图, 新图片尺寸将与固定尺寸或默认图一致.<br/>
 * <br/>
 * <br/>
 * Created by S.Violet on 2015/10/15.
 */
public class AsyncBitmapDrawable extends TransitionBitmapDrawable implements OnBitmapLoadedListener {

    private static final long RELOAD_DELAY = 2000;//图片加载失败重新加载时延
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

    /**********************************************************
     * override
     */

    @Override
    protected void onDrawError() {
        resetToDefault();
        reload();
    }

    /*****************************************************
     * private
     */

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
        if (getLoader() != null && !unused && reloadTimes < getLoader().getReloadTimesMax()) {
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
    public void onLoadSucceed(String url, int reqWidth, int reqHeight, Object params, Bitmap bitmap) {
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
    public void onLoadFailed(String url, int reqWidth, int reqHeight, Object params) {
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
    public void onLoadCanceled(String url, int reqWidth, int reqHeight, Object params) {
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
