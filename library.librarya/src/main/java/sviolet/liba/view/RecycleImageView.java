package sviolet.liba.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 回收资源型ImageView(务必destroy())<br>
 * 替换图片时, 原图会被回收<br>
 * 关闭时调用destroy()方法可回收资源
 * 
 * @author S.Violet
 *
 */
public class RecycleImageView extends ImageView {
	
	private Bitmap invalidBitmap;
	
	public RecycleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public RecycleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RecycleImageView(Context context) {
		super(context);
	}

	/*************************************************************
	 * 					inner
	 */
	
	private static final int HANDLER_RECYCLE = 1;
	
	@SuppressLint("HandlerLeak")
	private final Handler recycleHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLER_RECYCLE://回收
				releaseBitmap(invalidBitmap);
				invalidBitmap = null;
				break;
			default:
				break;
			}
		}
	};
	
	/*************************************************************
	 * 					复写
	 */
	
	@Override
	public void setImageResource(int resId) {
		invalidBitmap = getBitmap();
		super.setImageResource(resId);
	}

	@Override
	public void setImageURI(Uri uri) {
		invalidBitmap = getBitmap();
		super.setImageURI(uri);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		invalidBitmap = getBitmap();
		super.setImageDrawable(drawable);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(invalidBitmap != null && recycleHandler != null)
			recycleHandler.sendEmptyMessage(HANDLER_RECYCLE);
	}
	
	/**
	 * 得到当前显示图片的Bitmap
	 * @return
	 */
	private Bitmap getBitmap(){
		Drawable _drawable = getDrawable();
        if (_drawable != null && _drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) _drawable).getBitmap();
        }
        return null;
	}
	
	/*************************************************************
	 * 					FUNC
	 */
	
	/**
	 * 释放BitmapDrawable资源
	 * @param drawable 需要释放资源的Drawable
	 */
	public void releaseBitmap(Bitmap bitmap){
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}
	
	/**
	 * 销毁, 释放资源
	 */
	public void destroy(){
		releaseBitmap(getBitmap());
	}
	
}
