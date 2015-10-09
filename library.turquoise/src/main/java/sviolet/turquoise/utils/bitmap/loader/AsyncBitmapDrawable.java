package sviolet.turquoise.utils.bitmap.loader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 异步BitmapDrawable<br/>
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
public class AsyncBitmapDrawable extends BitmapDrawable implements OnBitmapLoadedListener{

    private String url;
    private int reqWidth;
    private int reqHeight;

    //加载器加载中
    private boolean loading = false;

    //默认的图片
    private Bitmap defaultBitmap;
    //加载器
    private AsyncBitmapDrawableLoader loader;

    /**
     * [默认图模式]<br/>
     * 当图被回收(recycle)时,显示默认图(defaultBitmap)
     *
     * @param bitmap 图
     * @param defaultBitmap 默认图
     */
    public AsyncBitmapDrawable(Bitmap bitmap, Bitmap defaultBitmap) {
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
    public AsyncBitmapDrawable(Resources res, Bitmap defaultBitmap) {
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
    public AsyncBitmapDrawable(Resources res, Bitmap bitmap, Bitmap defaultBitmap) {
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
    AsyncBitmapDrawable(String url, int reqWidth, int reqHeight, AsyncBitmapDrawableLoader loader) {
        super();
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
    AsyncBitmapDrawable(String url, int reqWidth, int reqHeight, AsyncBitmapDrawableLoader loader, Bitmap bitmap) {
        super(bitmap);
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
        if (loader != null)
            loader.unused(url);
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

    /**
     * 重置图片为加载图或默认图
     *
     * @return needLoad true:需要加载图片
     */
    private boolean resetBitmap() {
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
        loading = true;
        loader.load(this);
    }

    /*****************************************************
     * OnBitmapLoadedListener 回调
     */

    /**
     * 当加载器加载成功
     */
    @Override
    public void onLoadSucceed(String url, Object params, Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled())
            resetBitmap();
        else
            setBitmap(bitmap);
        //加载结束
        loading = false;
    }

    /**
     * 当加载器加载失败
     */
    @Override
    public void onLoadFailed(String url, Object params) {
        resetBitmap();
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
}
