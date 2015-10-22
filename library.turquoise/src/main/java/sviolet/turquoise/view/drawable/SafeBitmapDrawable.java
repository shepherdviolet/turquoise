package sviolet.turquoise.view.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import sviolet.turquoise.utils.Logger;

/**
 * 安全的BitmapDrawable<br/>
 * <br/>
 * 简易地防回收崩溃, 当Bitmap被回收时, 绘制空白, 不会崩溃, 但并不会重新加载图片<Br/>
 *
 * Created by S.Violet on 2015/10/21.
 */
public class SafeBitmapDrawable extends BitmapDrawable {

    private WeakReference<Logger> logger;//日志打印器
    private boolean drawEnable = true;//允许绘制

    public SafeBitmapDrawable(Resources res) {
        super(res);
    }

    public SafeBitmapDrawable(Bitmap bitmap) {
        super(bitmap);
    }

    public SafeBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    public SafeBitmapDrawable(String filepath) {
        super(filepath);
    }

    public SafeBitmapDrawable(Resources res, String filepath) {
        super(res, filepath);
    }

    public SafeBitmapDrawable(InputStream is) {
        super(is);
    }

    public SafeBitmapDrawable(Resources res, InputStream is) {
        super(res, is);
    }

    @Override
    public void draw(Canvas canvas) {
        if (drawEnable) {
            try {
                super.draw(canvas);
            } catch (Exception e) {
                drawEnable = false;//禁止绘制
                if (getLogger() != null) {
                    getLogger().e("[SafeBitmapDrawable]draw error, catch exception", e);
                }
            }
        }else{
            if (getLogger() != null) {
                getLogger().e("[SafeBitmapDrawable]draw skip, because of exception");
            }
        }
    }

    public SafeBitmapDrawable setLogger(Logger logger){
        this.logger = new WeakReference<Logger>(logger);
        return this;
    }

    private Logger getLogger(){
        if (logger != null)
            return logger.get();
        return null;
    }
}
