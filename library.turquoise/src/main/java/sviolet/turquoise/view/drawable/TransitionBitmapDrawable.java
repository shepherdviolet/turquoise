package sviolet.turquoise.view.drawable;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import sviolet.turquoise.utils.sys.DeviceUtils;

/**
 * <pre>
 * 由浅及深显示的BitmapDrawable, 防回收崩溃 [完善中,慎用]<Br/>
 * <br/>
 * 1.setBitmap可动态改变显示的Bitmap<Br/>
 * 2.Bitmap被回收时,会显示默认图或透明图层<br/>
 * 3.setBitmap设置的图支持渐变效果,默认图无效果<br/>
 * <br/>
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
 * </pre>
 * Created by S.Violet on 2015/10/13.
 */
@Deprecated
public class TransitionBitmapDrawable extends TransitionDrawable {

    //默认的图片
    private BitmapDrawable defaultDrawable;

    /**
     * <pre>
     * 由浅及深显示的BitmapDrawable, 防回收崩溃 [完善中,慎用]<Br/>
     * <br/>
     * 1.setBitmap可动态改变显示的Bitmap<Br/>
     * 2.Bitmap被回收时,会显示默认图或透明图层<br/>
     * 3.setBitmap设置的图支持渐变效果,默认图无效果<br/>
     * <br/>
     * *********************************************************<br/>
     * <Br/>
     * 初始显示透明图层<br/>
     * 当设置的Bitmap被回收(recycle), 显示透明图层<br/>
     * </pre>
     */
    public TransitionBitmapDrawable(){
        super(new Drawable[]{new ColorDrawable(0x00000000), new ColorDrawable(0x00000000)});
        startTransition(0);
    }

    /**
     * <pre>
     * 由浅及深显示的BitmapDrawable, 防回收崩溃 [完善中,慎用]<Br/>
     * <br/>
     * 1.setBitmap可动态改变显示的Bitmap<Br/>
     * 2.Bitmap被回收时,会显示默认图或透明图层<br/>
     * 3.setBitmap设置的图支持渐变效果,默认图无效果<br/>
     * <br/>
     * *********************************************************<br/>
     * <br/>
     * 初始显示默认图<br/>
     * 默认图的显示无渐变效果, 当设置的Bitmap被回收(recycle), 会显示默认图或透明图层<br/>
     * </pre>
     *
     * @param resources getResources()
     * @param defaultBitmap 默认图,立即显示,无渐变效果
     */
    public TransitionBitmapDrawable(Resources resources, Bitmap defaultBitmap) {
        super(new Drawable[]{new ColorDrawable(0x00000000), new BitmapDrawable(resources, defaultBitmap)});
        this.defaultDrawable =  new BitmapDrawable(resources, defaultBitmap);
        startTransition(0);
    }

    @Override
    public void draw(Canvas canvas) {
        try {
            super.draw(canvas);
        } catch (Exception e) {
            //绘制异常处理
            onDrawError();
        }
    }

    /**
     * 绘制异常处理
     */
    protected void onDrawError(){
        resetToDefault();
    }

    /**
     * 图片重置为默认图或透明图层
     */
    protected void resetToDefault(){
        if (defaultDrawable == null || defaultDrawable.getBitmap() == null || defaultDrawable.getBitmap().isRecycled()) {
            //默认图被回收时使用空图
            setDrawable(null, 0);
        } else {
            //默认图可用使用默认图
            setDrawable(defaultDrawable, 0);
        }
    }

    /**
     * 设置显示图片,并立即显示
     *
     * @param resources Resources
     * @param bitmap 显示的图片
     */
    public TransitionBitmapDrawable setBitmap(Resources resources, Bitmap bitmap){
        setBitmap(resources, bitmap, 0);
        return this;
    }

    /**
     * 设置显示图片,并由浅及深逐渐显示
     *
     * @param resources Resources
     * @param bitmap 显示的图片
     * @param duration 渐变持续时间
     */
    public TransitionBitmapDrawable setBitmap(Resources resources, Bitmap bitmap, int duration){

        Drawable drawable;

        if (bitmap == null || bitmap.isRecycled()){
            drawable = newTransparentDrawable();
        }else{
            drawable = new BitmapDrawable(resources, bitmap);
        }

        setDrawable(drawable, duration);

        return this;
    }

    /**
     * 核心代码<br/>
     * 通过反射设置显示的图片,并试图重新计算尺寸, 刷新显示<br/>
     *
     * @param drawable 显示的Drawable
     * @param duration 渐变持续时间
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void setDrawable(Drawable drawable, int duration){

        //若Drawable为空, 重置为透明图层
        if (drawable == null){
            drawable = newTransparentDrawable();
        }

        /**
         * 通过反射设置显示的图片.
         *
         * 过程参考API23 LayerDrawable构造函数
         */
        try {
            //获得mLayerState
            Field mLayerStateField = LayerDrawable.class.getDeclaredField("mLayerState");
            mLayerStateField.setAccessible(true);
            Object mLayerState = mLayerStateField.get(this);

            //获得mLayerState.mChildren
            Field mChildrenField = mLayerStateField.getType().getDeclaredField("mChildren");
            mChildrenField.setAccessible(true);
            Object[] mChildren = (Object[]) mChildrenField.get(mLayerState);

            //获得mLayerState.mChildren[1].mDrawable
            Field mDrawableField = mChildren[1].getClass().getDeclaredField("mDrawable");
            mDrawableField.setAccessible(true);
            Drawable mDrawable = (Drawable) mDrawableField.get(mChildren[1]);

            //撤销原有drawable的回调
            mDrawable.setCallback(null);
            //设置新的drawable
            mDrawableField.set(mChildren[1], drawable);
            //新drawable设置回调
            drawable.setCallback(this);

            //更新mLayerState.mChildrenChangingConfigurations值
            Field mChildrenChangingConfigurationsField = mLayerStateField.getType().getDeclaredField("mChildrenChangingConfigurations");
            mChildrenChangingConfigurationsField.setAccessible(true);
            int mChildrenChangingConfigurations = (int) mChildrenChangingConfigurationsField.get(mLayerState);
            mChildrenChangingConfigurationsField.set(mLayerState, mChildrenChangingConfigurations | drawable.getChangingConfigurations());

            //ensurePadding
            Method ensurePaddingMethod = LayerDrawable.class.getDeclaredMethod("ensurePadding");
            ensurePaddingMethod.setAccessible(true);
            ensurePaddingMethod.invoke(this);

        }catch (Exception e){
            onSetBitmapError(new RuntimeException("[AsyncBitmapDrawable]setBitmap:setDrawable error!", e));
            return;
        }

        /**
         * API23以上刷新Padding.
         */
        if (DeviceUtils.getVersionSDK() >= 23) {
            try {
                Method refreshPaddingMethod = LayerDrawable.class.getDeclaredMethod("refreshPadding");
                refreshPaddingMethod.setAccessible(true);
                refreshPaddingMethod.invoke(this);
            } catch (Exception e) {
                onSetBitmapError(new RuntimeException("[AsyncBitmapDrawable]setBitmap:refreshPadding error!", e));
                return;
            }
        }

        /**
         * 获取回调(Callback/View).
         *
         * API11以下需要通过反射获取.
         */
        Callback callback = null;
        if (DeviceUtils.getVersionSDK() >= 11) {
            callback = getCallback();
        }else{
            try {
                Field callbackField = Drawable.class.getDeclaredField("mCallback");
                callbackField.setAccessible(true);
                callback = (Callback) callbackField.get(this);
            } catch (Exception e) {
                onSetBitmapError(new RuntimeException("[AsyncBitmapDrawable]setBitmap:getCallback error!", e));
                return;
            }
        }

        /**
         * 默认将drawable尺寸设置为和原先一致.
         *
         * 这种方式存在问题,但却是最为通用的解决方法.
         * 新设置的图片尺寸将和原来一致, 并不会根据实际情况重新调整尺寸, 特别是当控件尺寸设置为
         * wrap_content时, 控件长宽可能会初始化为0, 导致新设置的图片无法显示, 建议控件尺寸设置
         * 为固定值或给TransitionBitmapDrawable设置默认图, 新图片尺寸将与固定尺寸或默认图一致.
         */
        drawable.setBounds(getBounds());

        /**
         * 尝试重新计算控件和drawable尺寸, 解决上面drawable.setBounds(getBounds())的不足.
         *
         * 关键性待完善代码.
         *
         * 目前实现:
         * 1.ImageView的尺寸重计算
         *
         */
        if (callback != null && callback instanceof ImageView){
            /*
                ImageView
             */
            try {
                //调用ImageView.updateDrawable方法会重新计算尺寸
                Method ensurePaddingMethod = ImageView.class.getDeclaredMethod("updateDrawable", Drawable.class);
                ensurePaddingMethod.setAccessible(true);
                ensurePaddingMethod.invoke(callback, this);
            }catch (Exception e){
                onSetBitmapError(new RuntimeException("[AsyncBitmapDrawable]setBitmap:ImageView:updateDrawable error!", e));
                return;
            }
        }

        /**
         * 尝试回调requestLayout方法重新布局
         */
        if (callback != null && callback instanceof View){
            //requestLayout
            ((View)callback).requestLayout();
        }

        /**
         * 刷新图片
         */
        startTransition(duration);

    }

    /**
     * 创建一个新的透明Drawable
     */
    public Drawable newTransparentDrawable(){
        return new ColorDrawable(0x00000000);
    }

    /**
     * 反射方式设置图片出错处理<br/>
     * 默认为直接打印错误日志, 不抛出异常, 避免应用崩溃<br/>
     */
    protected void onSetBitmapError(RuntimeException e){
        e.printStackTrace();
    }

}
