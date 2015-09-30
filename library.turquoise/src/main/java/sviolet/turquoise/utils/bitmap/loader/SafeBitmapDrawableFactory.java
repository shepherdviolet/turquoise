package sviolet.turquoise.utils.bitmap.loader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import java.lang.reflect.Field;

/**
 * 安全的BitmapDrawable工厂<br/>
 * 若内部的Bitmap被回收(recycle),绘制时不会抛出异常,
 * 而是显示设置的默认图(Bitmap),若默认图也被回收,则会
 * 抛出异常,若默认图设置null,则显示空白<br/>
 *
 * 默认图(defaultBitmap)需要自行回收,建议在视图不再显示时
 * 回收默认图(Bitmap.recycle)<br/>
 *
 * Created by S.Violet on 2015/9/23.
 */
public class SafeBitmapDrawableFactory {

    //默认图
    private Bitmap defaultBitmap;

    /**
     * 安全的BitmapDrawable工厂<br/>
     * 若内部的Bitmap被回收(recycle),绘制时不会抛出异常,
     * 而是显示设置的默认图(Bitmap),若默认图也被回收,则会
     * 抛出异常,若默认图设置null,则显示空白<br/>
     *
     * 默认图(defaultBitmap)需要自行回收,建议在视图不再显示时
     * 回收默认图(Bitmap.recycle)<br/>
     *
     * @param defaultBitmap 默认图
     */
    public SafeBitmapDrawableFactory(Bitmap defaultBitmap){
        this.defaultBitmap = defaultBitmap;
    }

    /**
     * 创建安全的BitmapDrawable
     */
    public SafeBitmapDrawable create(Bitmap bitmap){
        if (bitmap == null)
            return null;
        return new SafeBitmapDrawable(bitmap, defaultBitmap);
    }

    /**
     * 创建安全的BitmapDrawable
     */
    public SafeBitmapDrawable create(Resources res){
        if (res == null)
            return null;
        return new SafeBitmapDrawable(res, defaultBitmap);
    }

    /**
     * 创建安全的BitmapDrawable
     */
    public SafeBitmapDrawable create(Resources res, Bitmap bitmap){
        if (bitmap == null || res == null)
            return null;
        return new SafeBitmapDrawable(res, bitmap, defaultBitmap);
    }

    /**
     * 安全的BitmapDrawable<br/>
     * 若内部的Bitmap被回收(recycle),绘制时不会抛出异常,
     * 而是显示设置的默认图(Bitmap),若默认图也被回收,则会
     * 抛出异常,若默认图设置null,则显示空白<br/>
     *
     * 默认图(defaultBitmap)需要自行回收,建议在视图不再显示时
     * 回收默认图(Bitmap.recycle)<br/>
     */
    class SafeBitmapDrawable extends BitmapDrawable {

        //默认图
        private Bitmap defaultBitmap;

        private SafeBitmapDrawable(Bitmap bitmap, Bitmap defaultBitmap) {
            super(bitmap);
            this.defaultBitmap = defaultBitmap;
        }

        private SafeBitmapDrawable(Resources res, Bitmap defaultBitmap) {
            super(res);
            this.defaultBitmap = defaultBitmap;
        }

        private SafeBitmapDrawable(Resources res, Bitmap bitmap, Bitmap defaultBitmap) {
            super(res, bitmap);
            this.defaultBitmap = defaultBitmap;
        }

        @Override
        public void draw(Canvas canvas) {
            try {
                super.draw(canvas);
            }catch(Exception e){
                if(defaultBitmap != null && defaultBitmap.isRecycled())
                    throw new RuntimeException("[SafeBitmapDrawable]default bitmap is recycled!");
                setBitmapToDefault();
                try {
                    super.draw(canvas);
                }catch(Exception e2){
                    throw new RuntimeException("[SafeBitmapDrawable]default bitmap is recycled!", e2);
                }
            }
        }

        /**
         * 设置为默认图
         */
        private void setBitmapToDefault(){
            try {
                Field field = BitmapDrawable.class.getDeclaredField("mBitmapState");
                field.setAccessible(true);
                Object bitmapState = field.get(this);
                Class bitmapStateClass = bitmapState.getClass();
                Field bitmapStateField = bitmapStateClass.getDeclaredField("mBitmap");
                bitmapStateField.setAccessible(true);
                bitmapStateField.set(bitmapState, defaultBitmap);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

}
