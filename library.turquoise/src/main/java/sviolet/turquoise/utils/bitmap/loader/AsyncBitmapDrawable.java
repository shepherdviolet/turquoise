package sviolet.turquoise.utils.bitmap.loader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import java.lang.reflect.Field;
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
            loader.load(url, reqWidth, reqHeight, null, this);
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

    /******************************************************************
     * function
     */

    /**
     * [建议使用]不再使用图片, 有利于Bitmap尽快回收<br/>
     * <br/>
     * 加载器模式时:会调用加载器的unused方法弃用图片<br/>
     * 默认图模式时:会直接回收掉Bitmap(不包括默认图)<br/>
     */
    public void unused(){
        if (loader != null){
            //加载器模式
            loader.unused(url);
        }else{
            if (getBitmap() != null && !getBitmap().isRecycled()){
                getBitmap().recycle();
            }
        }
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
                loader.load(url, reqWidth, reqHeight, null, this);
            }
            try {
                //重新绘制
                super.draw(canvas);
            }catch(Exception e2){
                //仍然出错绘制空图
                setBitmap(null);
                super.draw(canvas);
            }
        }
    }

    /**
     * 重置图片为加载图或默认图
     *
     * @return hasLoader
     */
    private boolean resetBitmap() {
        if (loader != null){
            //加载器模式
            if (loader.getLoadingBitmap() != null && loader.getLoadingBitmap().isRecycled()) {
                //加载图被回收时使用空图
                setBitmap(null);
            } else {
                //加载图可用使用加载图
                setBitmap(loader.getLoadingBitmap());
            }
            return true;
        }else {
            //默认图模式
            if (defaultBitmap != null && defaultBitmap.isRecycled()) {
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

    /*****************************************************
     * OnBitmapLoadedListener 回调
     */

    /**
     * 当加载器加载成功
     */
    @Override
    public void onLoadSucceed(String url, Object params, Bitmap bitmap) {
        if (bitmap != null && bitmap.isRecycled())
            resetBitmap();
        else
            setBitmap(bitmap);
    }

    /**
     * 当加载器加载失败
     */
    @Override
    public void onLoadFailed(String url, Object params) {
        resetBitmap();
    }

    /**
     * 当加载器加载取消
     */
    @Override
    public void onLoadCanceled(String url, Object params) {
        resetBitmap();
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
