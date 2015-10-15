package sviolet.turquoise.utils.bitmap.loader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sviolet.turquoise.utils.sys.DateTimeUtils;
import sviolet.turquoise.utils.sys.DeviceUtils;

/**
 * 异步BitmapDrawable<br/>
 * 加载失败自动重新加载<br/>
 * <br/>
 * 1.加载器模式, AsyncBitmapDrawableLoader.load方法返回<br/>
 * --初始显示加载器(AsyncBitmapDrawableLoader)的加载图(loadingBitmap),当图片加载完毕,显示加载出来的图片,
 * 当加载的图片被回收(recycle),显示加载图或空图<br/>
 * <br/>
 * 2.默认图模式, AsyncBitmapDrawable构造器实例化<br/>
 * --当图被回收(recycle)时,显示默认图(defaultBitmap)<br/>
 *
 * <br/>
 * Created by S.Violet on 2015/9/29.
 */
public class AsyncBitmapDrawable2 extends BitmapDrawable implements OnBitmapLoadedListener{

    private static final long RELOAD_DELAY = 2000;//图片加载失败重新加载时延
    private static final int RELOAD_TIMES_LIMIT = 2;//图片加载失败重新加载次数限制
    private int reloadTimes = 0;//图片重新加载次数

    private String url;
    private int reqWidth;
    private int reqHeight;

    //默认的图片
    private Bitmap defaultBitmap;

    //加载器
    private AsyncBitmapDrawableLoader loader;
    //加载器加载中
    private boolean loading = false;
    //是否被弃用
    private boolean unused = false;

    //渐变动画刷新间隔
    private static final long GRADUAL_EFFECT_REFRESH_INTERVAL = 50L;//ms
    //图片逐渐显示效果时间(0关闭效果)
    private long gradualEffectDuration = 0;//ms
    //图片逐渐显示效果开始时间
    private long gradualEffectStartTime = 0;//ms
    //图片当前透明度
    private int alpha = 255;

    /**
     * [默认图模式]<br/>
     * 当图被回收(recycle)时,显示默认图(defaultBitmap)
     *
     * @param bitmap 图
     * @param defaultBitmap 默认图
     */
    @Deprecated
    public AsyncBitmapDrawable2(Bitmap bitmap, Bitmap defaultBitmap) {
        super(bitmap);
        this.defaultBitmap = defaultBitmap;
    }

    /**
     * [默认图模式]<br/>
     * 当图被回收(recycle)时,显示默认图(defaultBitmap)
     *
     * @param res resource
     * @param defaultBitmap 默认图
     */
    @Deprecated
    public AsyncBitmapDrawable2(Resources res, Bitmap defaultBitmap) {
        super(res);
        this.defaultBitmap = defaultBitmap;
    }

    /**
     * [默认图模式]<br/>
     * 当图被回收(recycle)时,显示默认图(defaultBitmap)
     *
     * @param res resource
     * @param bitmap 图
     * @param defaultBitmap 默认图
     */
    public AsyncBitmapDrawable2(Resources res, Bitmap bitmap, Bitmap defaultBitmap) {
        super(res, bitmap);
        this.defaultBitmap = defaultBitmap;
    }

    /**
     * [加载器模式]<br/>
     * 初始显示加载器(AsyncBitmapDrawableLoader)的加载图(loadingBitmap),当图片加载完毕,显示加载出来的图片,
     * 当加载的图片被回收(recycle),显示加载图或空图<br/>
     *
     * @param url 图片链接
     * @param reqWidth 需求宽度
     * @param reqHeight 需求高度
     * @param loader 加载器
     */
    AsyncBitmapDrawable2(String url, int reqWidth, int reqHeight, AsyncBitmapDrawableLoader loader) {
        super();
        this.url = url;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
        this.loader = loader;

        if (resetBitmap()) {
            load();
        }
    }

    AsyncBitmapDrawable2(String url, int reqWidth, int reqHeight, AsyncBitmapDrawableLoader loader, Resources resources) {
        super(resources);
        this.url = url;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
        this.loader = loader;

        if (resetBitmap()) {
            load();
        }
    }

    /**
     * [加载器模式]<br/>
     * 初始显示加载器(AsyncBitmapDrawableLoader)的加载图(loadingBitmap),当图片加载完毕,显示加载出来的图片,
     * 当加载的图片被回收(recycle),显示加载图或空图<br/>
     *
     * @param url 图片链接
     * @param reqWidth 需求宽度
     * @param reqHeight 需求高度
     * @param loader 加载器
     * @param bitmap 图
     */
    AsyncBitmapDrawable2(String url, int reqWidth, int reqHeight, AsyncBitmapDrawableLoader loader, Bitmap bitmap) {
        super(bitmap);
        this.url = url;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
        this.loader = loader;
    }

    AsyncBitmapDrawable2(String url, int reqWidth, int reqHeight, AsyncBitmapDrawableLoader loader, Resources resources, Bitmap bitmap) {
        super(resources, bitmap);
        this.url = url;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
        this.loader = loader;
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
        if (loader != null)
            loader.unused(url);
        this.unused = true;
        //停止动态效果刷新和重加载
        destroyHandler();
    }

    /**
     * 启用图片逐渐显示效果(会增大性能开销)<Br/>
     * 仅在图片加载成功时生效<br/>
     *
     * @param duration 动画时间(ms), 大于0生效
     */
    public AsyncBitmapDrawable2 enableGradualEffect(long duration){
        this.gradualEffectDuration = duration;
        return this;
    }

    /**
     * 设置图片加载失败后重新加载次数限制<Br/>
     */
    public AsyncBitmapDrawable2 setReloadTimes(int times){
        this.reloadTimes = times;
        return this;
    }

    /******************************************************************
     * override
     */

    @Override
    public void draw(Canvas canvas) {
        try {
            super.draw(canvas);
        }catch(Exception e){
            if (resetBitmap()) {
                load();
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {
        //记录当前透明度
        this.alpha = alpha;
        super.setAlpha(alpha);
    }

    @Override
    public int getAlpha() {
        if (DeviceUtils.getVersionSDK() >= 19)
            return super.getAlpha();
        else
            return alpha;
    }

    /**
     * 重置图片为加载图或默认图
     *
     * @return needLoad true:需要加载图片
     */
    private boolean resetBitmap() {
        //重置图片时,取消渐变动画
        stopAnimation();
        //重置图片时,变为不透明
        if (gradualEffectDuration > 0 && getAlpha() < 255)
            setAlpha(255);

        if (loader != null){
            //加载器模式
            if (loader.getLoadingBitmap() == null || loader.getLoadingBitmap().isRecycled()) {
                //加载图被回收时使用空图
                setBitmap(null);
            } else {
                //加载图可用使用加载图
                setBitmap(loader.getLoadingBitmap());
            }
            //若加载器已在加载, 则返回false
            return !loading;
        }else {
            //默认图模式
            if (defaultBitmap == null || defaultBitmap.isRecycled()) {
                //默认图被回收时使用空图
                setBitmap(null);
            } else {
                //默认图可用使用默认图
                setBitmap(defaultBitmap);
            }
            return false;
        }
    }

    /**
     * [反射]设置Bitmap
     */
    private void setBitmap(Bitmap bitmap){
        try {
            Method method = BitmapDrawable.class.getDeclaredMethod("setBitmap", Bitmap.class);
            method.setAccessible(true);
            method.invoke(this, bitmap);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载
     */
    private void load() {
        //加载开始
        if (loader != null && !loading) {
            loading = true;
//            loader.load(this);
        }
    }

    /*****************************************************
     * OnBitmapLoadedListener 回调
     */

    /**
     * 当加载器加载成功
     */
    @Override
    public void onLoadSucceed(String url, Object params, Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            resetBitmap();
        }else {
            if (gradualEffectDuration > 0){
                setAlpha(0);
                gradualEffectStartTime = DateTimeUtils.getUptimeMillis();
                getHandler().sendEmptyMessageDelayed(HANDLER_REFRESH, GRADUAL_EFFECT_REFRESH_INTERVAL);
            }
            setBitmap(bitmap);
        }
        //加载结束
        loading = false;
    }

    /**
     * 当加载器加载失败
     */
    @Override
    public void onLoadFailed(String url, Object params) {
        //重置图片
        resetBitmap();
        //未被弃用的情况下重新加载图片
        if (loader != null && !unused && reloadTimes < RELOAD_TIMES_LIMIT) {
            reloadTimes++;
            getHandler().sendEmptyMessageDelayed(HANDLER_RELOAD, RELOAD_DELAY);//重新加载图片
        }
        //加载结束
        loading = false;
    }

    /**
     * 当加载器加载取消
     */
    @Override
    public void onLoadCanceled(String url, Object params) {
        resetBitmap();
        //加载结束
        loading = false;
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

    private static final int HANDLER_REFRESH = 0;//渐变动画刷新
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

    private void stopAnimation(){
        if (handler != null){
            synchronized (this){
                if (handler != null){
                    handler.removeMessages(HANDLER_REFRESH);
                }
            }
        }
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
                case HANDLER_REFRESH:
                    //计算透明度
                    long passTime = DateTimeUtils.getUptimeMillis() - gradualEffectStartTime;
                    int alpha = (int) (((double)passTime / (double)gradualEffectDuration) * 255);
                    if (alpha > 255){
                        alpha = 255;
                    }else{
                        //继续刷新
                        getHandler().sendEmptyMessageDelayed(HANDLER_REFRESH, GRADUAL_EFFECT_REFRESH_INTERVAL);
                    }
                    //设置透明度
                    setAlpha(alpha);
                    break;
                case HANDLER_RELOAD:
                    //未被弃用
                    if (!unused){
                        //重新加载图片
                        load();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    }

}
