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

    //默认的图片
    private BitmapDrawable defaultDrawable;

    public TransitionBitmapDrawable(){
        super(new Drawable[]{new ColorDrawable(0x00000000), new ColorDrawable(0x00000000)});
        startTransition(0);
    }

    public TransitionBitmapDrawable(Resources resources, Bitmap defaultBitmap) {
        super(new Drawable[]{new ColorDrawable(0x00000000), new BitmapDrawable(resources, defaultBitmap)});
        this.defaultDrawable =  new BitmapDrawable(resources, defaultBitmap);
        startTransition(0);
    }

    @Override
    public void draw(Canvas canvas) {
        try {
            super.draw(canvas);
        }catch(Exception e){
            onDrawError();
        }
    }

    protected void onDrawError(){
        resetToDefault();
    }

    protected void resetToDefault(){
        if (defaultDrawable == null || defaultDrawable.getBitmap() == null || defaultDrawable.getBitmap().isRecycled()) {
            //默认图被回收时使用空图
            setDrawable(null, 0);
        } else {
            //默认图可用使用默认图
            setDrawable(defaultDrawable, 0);
        }
    }

    public void setBitmap(Resources resources, Bitmap bitmap){
        setBitmap(resources, bitmap, 0);
    }

    public void setBitmap(Resources resources, Bitmap bitmap, int duration){

        Drawable drawable;

        if (bitmap == null || bitmap.isRecycled()){
            drawable = newTransparentDrawable();
        }else{
            drawable = new BitmapDrawable(resources, bitmap);
        }

        setDrawable(drawable, duration);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void setDrawable(Drawable drawable, int duration){

        if (drawable == null){
            drawable = newTransparentDrawable();
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

    }

    /**
     * 创建一个新的透明Drawable
     */
    public Drawable newTransparentDrawable(){
        return new ColorDrawable(0x00000000);
    }

}
