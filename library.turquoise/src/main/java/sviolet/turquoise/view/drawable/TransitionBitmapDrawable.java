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
 * Created by S.Violet on 2015/10/13.
 */
public class TransitionBitmapDrawable extends TransitionDrawable {

    public TransitionBitmapDrawable(Bitmap bitmap) {
        super(new Drawable[]{new ColorDrawable(0x00000000), new ColorDrawable(0x00000000)});
    }

    @Override
    public void draw(Canvas canvas) {
        System.out.println("draw");
        super.draw(canvas);
    }

    /**
     *
     * @param resources
     * @param bitmap
     * @param duration
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setBitmap(Resources resources, Bitmap bitmap, int duration){

        Drawable drawable;

        if (bitmap == null || bitmap.isRecycled()){
            drawable = newTransparentDrawable();
        }else if (resources != null){
            drawable = new BitmapDrawable(resources, bitmap);
        }else{
            drawable = new BitmapDrawable(bitmap);
        }

        try {
            Field mLayerStateField = LayerDrawable.class.getDeclaredField("mLayerState");
            mLayerStateField.setAccessible(true);
            Object mLayerState = mLayerStateField.get(this);

            Field mChildrenField = mLayerStateField.getType().getDeclaredField("mChildren");
            mChildrenField.setAccessible(true);
            Object[] mChildren = (Object[]) mChildrenField.get(mLayerState);

            Field mDrawableField = mChildren[1].getClass().getDeclaredField("mDrawable");
            mDrawableField.setAccessible(true);
            Drawable mDrawable = (Drawable) mDrawableField.get(mChildren[1]);
            mDrawable.setCallback(null);
            mDrawableField.set(mChildren[1], drawable);
            drawable.setCallback(this);

            Field mChildrenChangingConfigurationsField = mLayerStateField.getType().getDeclaredField("mChildrenChangingConfigurations");
            mChildrenChangingConfigurationsField.setAccessible(true);
            int mChildrenChangingConfigurations = (int) mChildrenChangingConfigurationsField.get(mLayerState);
            mChildrenChangingConfigurationsField.set(mLayerState, mChildrenChangingConfigurations | drawable.getChangingConfigurations());

            Method ensurePaddingMethod = LayerDrawable.class.getDeclaredMethod("ensurePadding");
            ensurePaddingMethod.setAccessible(true);
            ensurePaddingMethod.invoke(this);

        }catch (Exception e){
            throw new RuntimeException("[AsyncBitmapDrawable]setBitmap:setDrawable error!", e);
        }

        if (DeviceUtils.getVersionSDK() >= 23) {
            try {
                Method refreshPaddingMethod = LayerDrawable.class.getDeclaredMethod("refreshPadding");
                refreshPaddingMethod.setAccessible(true);
                refreshPaddingMethod.invoke(this);
            } catch (Exception e) {
                throw new RuntimeException("[AsyncBitmapDrawable]setBitmap:refreshPadding error!", e);
            }
        }

        Callback callback = null;
        if (DeviceUtils.getVersionSDK() >= 11) {
            callback = getCallback();
        }else{
            try {
                Field callbackField = Drawable.class.getDeclaredField("mCallback");
                callbackField.setAccessible(true);
                callback = (Callback) callbackField.get(this);
            } catch (Exception e) {
                throw new RuntimeException("[AsyncBitmapDrawable]setBitmap:getCallback error!", e);
            }
        }

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
                throw new RuntimeException("[AsyncBitmapDrawable]setBitmap:updateDrawable error!", e);
            }
        }else{
            /*
                若callback为空,或callback为其他控件.
                无法根据控件重新计算尺寸等参数.
                因此只能将BitmapDrawable的Bounds设置为与AsyncBitmapDrawable相同.
                控件和Drawable的尺寸将与控件初始化时的尺寸相同,即不会根据新设置的Bitmap改变,
                特别是长宽设置为wrap_content的情况,会导致图片无法正常显示.
             */
            drawable.setBounds(getBounds());
        }

        if (callback != null && callback instanceof View){
            //requestLayout
            ((View)callback).requestLayout();
        }

        startTransition(duration);

//        try {
//            //获得TransitionState构造器
//            Constructor transitionStateConstructor = null;
//            Class<?>[] transitionDrawableInnerClasses = TransitionDrawable.class.getDeclaredClasses();
//            for (Class transitionStateClass : transitionDrawableInnerClasses) {
//                if ("TransitionState".equals(transitionStateClass.getSimpleName())) {
//                    transitionStateConstructor = transitionStateClass.getDeclaredConstructor(transitionStateClass, TransitionDrawable.class, Resources.class);
//                    transitionStateConstructor.setAccessible(true);
//                    break;
//                }
//            }
//
//            //获得layerState参数
//            Field layerStateField = LayerDrawable.class.getDeclaredField("mLayerState");
//            layerStateField.setAccessible(true);
//            //获得mLayerState实例
//            Object mLayerState = layerStateField.get(this);
//
//            //获得childDrawable参数
//            Field childDrawableField = layerStateField.getType().getDeclaredField("mChildren");
//            childDrawableField.setAccessible(true);
//            //获得mChildren实例
//            Object[] mChildren = (Object[]) childDrawableField.get(mLayerState);
//            //获得childDrawable的类, 注意不能用childDrawableField.getType()
//            Class childDrawableClass = mChildren[0].getClass();
//            //获得childDrawable构造方法
//            Constructor childDrawableConstructor = childDrawableClass.getDeclaredConstructor();
//            childDrawableConstructor.setAccessible(true);
//            //获得childDrawable中mDrawable参数
//            Field mDrawableField = childDrawableClass.getDeclaredField("mDrawable");
//            mDrawableField.setAccessible(true);
//            //清理原有drawable的callback
//            for (Object children : mChildren){
//                Drawable mDrawable = (Drawable) mDrawableField.get(children);
//                mDrawable.setCallback(null);
//            }
//
//            //获得layerState构造器
//            Constructor layerStateConstructor = layerStateField.getType().getDeclaredConstructor(layerStateField.getType(), LayerDrawable.class, Resources.class);
//            layerStateConstructor.setAccessible(true);
//            Object newLayerState = layerStateConstructor.newInstance(transitionStateConstructor.newInstance(null, null, null), this, null);
//            //放入新的layerState
//            layerStateField.set(this, newLayerState);
//
//            //获得layerState的mChildrenChangingConfigurations参数
//            Field mChildrenChangingConfigurationsField = layerStateField.getType().getDeclaredField("mChildrenChangingConfigurations");
//            mChildrenChangingConfigurationsField.setAccessible(true);
//
//            //新的mChildren实例
//            Object newChildrenArray = Array.newInstance(childDrawableClass, 2);
//            //第一个children
//            Object newChildren = childDrawableConstructor.newInstance();
//            Drawable drawable = newTransparentDrawable();
//            drawable.setCallback(this);
//            mDrawableField.set(newChildren, drawable);
//            int mChildrenChangingConfigurations = (int) mChildrenChangingConfigurationsField.get(newLayerState);
//            mChildrenChangingConfigurationsField.set(newLayerState, mChildrenChangingConfigurations | drawable.getChangingConfigurations());
//            Array.set(newChildrenArray, 0, newChildren);
//            //第二个children
//            newChildren = childDrawableConstructor.newInstance();
//            if (bitmap == null || bitmap.isRecycled()) {
//                drawable = newTransparentDrawable();
//            }else if (resources != null) {
//                drawable = new BitmapDrawable(resources, bitmap);
//            }else{
//                drawable = new BitmapDrawable(bitmap);
//            }
//            drawable.setCallback(this);
//            mDrawableField.set(newChildren, drawable);
//            mChildrenChangingConfigurations = (int) mChildrenChangingConfigurationsField.get(newLayerState);
//            mChildrenChangingConfigurationsField.set(newLayerState, mChildrenChangingConfigurations | drawable.getChangingConfigurations());
//            Array.set(newChildrenArray, 1, newChildren);
//
//            Field mNumField = layerStateField.getType().getDeclaredField("mNum");
//            mNumField.setAccessible(true);
//            mNumField.set(newLayerState, 2);
//            //放入新的mChildren
//            childDrawableField.set(newLayerState, newChildrenArray);
//
//            Method ensurePaddingMethod = LayerDrawable.class.getDeclaredMethod("ensurePadding");
//            ensurePaddingMethod.setAccessible(true);
//            ensurePaddingMethod.invoke(this);
//
////            Method onStateChangeMethod = LayerDrawable.class.getDeclaredMethod("onStateChange", int[].class);
////            onStateChangeMethod.setAccessible(true);
////            onStateChangeMethod.invoke(this, getState());
//
//        }catch (Exception e){
//            throw new RuntimeException("[AsyncBitmapDrawable]setBitmap error!", e);
//        }
//
//        try {
//            Method refreshPaddingMethod = LayerDrawable.class.getDeclaredMethod("refreshPadding");
//            refreshPaddingMethod.setAccessible(true);
//            refreshPaddingMethod.invoke(this);
//        }catch (Exception e){
//        }
//
//        startTransition(duration);

    }

    /**
     * 创建一个新的透明Drawable
     */
    public Drawable newTransparentDrawable(){
        return new ColorDrawable(0x00000000);
    }

}
