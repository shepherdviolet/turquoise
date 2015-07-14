package sviolet.turquoise.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.ImageView;

import sviolet.turquoise.utils.DeviceUtils;

/**
 * 渐渐显示出来的ImageView<br/>
 * <br/>
 * 避免使用原有的setImageBitmap/setImageDrawable/setImageResource<br/>
 *
 * Created by S.Violet on 2015/7/10.
 */
public class GradualImageView extends ImageView {

    //渐变时间
    private static final long GRADUAL_DURATION = 500L;//ms
    //刷新间隔
    private static final long GRADUAL_REFRESH_INTERVAL = 50L;//ms

    //图片更新时间
    private long updateTimestamp = 0L;//ms

    public GradualImageView(Context context) {
        super(context);
    }

    public GradualImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置图片并渐渐显示
     * @param bitmap
     */
    public void setImageBitmapGradual(Bitmap bitmap) {
        resetToTranslucent();
        super.setImageBitmap(bitmap);
        mHandler.sendEmptyMessageDelayed(HANDLER_SET_ALPHA, GRADUAL_REFRESH_INTERVAL);//开始刷新
    }

    /**
     * 设置图片并渐渐显示
     * @param resId
     */
    public void setImageResourceGradual(int resId) {
        resetToTranslucent();
        super.setImageResource(resId);
        mHandler.sendEmptyMessageDelayed(HANDLER_SET_ALPHA, GRADUAL_REFRESH_INTERVAL);
    }

    /**
     * 设置图片并渐渐显示
     * @param drawable
     */
    public void setImageDrawableGradual(Drawable drawable) {
        resetToTranslucent();
        super.setImageDrawable(drawable);
        mHandler.sendEmptyMessageDelayed(HANDLER_SET_ALPHA, GRADUAL_REFRESH_INTERVAL);
    }

    /**
     * 设置图片并立即显示
     * @param drawable
     */
    public void setImageDrawableImmediate(Drawable drawable) {
        mHandler.removeMessages(HANDLER_SET_ALPHA);
        updateTimestamp = SystemClock.uptimeMillis();
        super.setImageDrawable(drawable);
        if (drawable != null)
            setAlphaCompat(255);
    }

    /**
     * 设置图片并立即显示
     * @param resId
     */
    public void setImageResourceImmediate(int resId) {
        mHandler.removeMessages(HANDLER_SET_ALPHA);
        updateTimestamp = SystemClock.uptimeMillis();
        super.setImageResource(resId);
        if (resId > 0)
            setAlphaCompat(255);
    }

    /**
     * 设置图片并立即显示
     * @param bm
     */
    public void setImageBitmapImmediate(Bitmap bm) {
        mHandler.removeMessages(HANDLER_SET_ALPHA);
        updateTimestamp = SystemClock.uptimeMillis();
        super.setImageBitmap(bm);
        if (bm != null)
            setAlphaCompat(255);
    }

    /**
     * 不建议使用
     * @param bm
     */
    @Override
    @Deprecated
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
    }

    /**
     * 不建议使用
     * @param drawable
     */
    @Override
    @Deprecated
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
    }

    /**
     * 不建议使用
     * @param resId
     */
    @Override
    @Deprecated
    public void setImageResource(int resId) {
        super.setImageResource(resId);
    }

    private static final int HANDLER_SET_ALPHA = 1;//设置透明度

    private final Handler mHandler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg) {
            //计算透明度
            long passTime = SystemClock.uptimeMillis() - updateTimestamp;
            int alpha = (int) (((double)passTime / (double)GRADUAL_DURATION) * 255);
            if (alpha > 255){
                alpha = 255;
            }else{
                //继续刷新
                mHandler.sendEmptyMessageDelayed(HANDLER_SET_ALPHA, GRADUAL_REFRESH_INTERVAL);
            }
            //设置透明度
            setAlphaCompat(alpha);
            return true;
        }
    });

    /**
     * 设置透明度
     * @param alpha
     */
    @SuppressLint("NewApi")
    private void setAlphaCompat(int alpha) {
        if (DeviceUtils.getVersionSDK() >= 16){
            GradualImageView.this.setImageAlpha(alpha);
        }else {
            GradualImageView.this.setAlpha(alpha);
        }
    }

    /**
     * 重置状态并设置为全透明
     */
    private void resetToTranslucent() {
        mHandler.removeMessages(HANDLER_SET_ALPHA);//移除消息
        updateTimestamp = SystemClock.uptimeMillis();//记录更新时间
        setAlphaCompat(0);//设为全透明
    }

}