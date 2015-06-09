package sviolet.liba.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

/**
 * @author 田裕杰
 * 
 */
public class ImageUtils {

	@SuppressWarnings("deprecation")
	private static Drawable createDrawable(Drawable d, Paint p) {
		BitmapDrawable bd = (BitmapDrawable) d;
		Bitmap b = bd.getBitmap();
		Bitmap bitmap = Bitmap.createBitmap(bd.getIntrinsicWidth(),
				bd.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(b, 0, 0, p); // 关键代码，使用新的Paint画原图，
		return new BitmapDrawable(bitmap);
	}

	/** 设置Selector。 本次只增加点击变暗的效果，注释的代码为更多的效果 */
	public static StateListDrawable createSLD(Drawable drawable) {
		StateListDrawable bg = new StateListDrawable();
		Paint p = new Paint();
		p.setColor(0x40222222); // Paint ARGB色值，A = 0x40 不透明。RGB222222 暗色
		Drawable normal = drawable;
		Drawable pressed = createDrawable(drawable, p);
		// p = new Paint();
		// p.setColor(0x8000FF00);
		// Drawable focused = createDrawable(drawable, p);
		// p = new Paint();
		// p.setColor(0x800000FF);
		// Drawable unable = createDrawable(drawable, p);
		// View.PRESSED_ENABLED_STATE_SET
		bg.addState(new int[] { android.R.attr.state_pressed,
				android.R.attr.state_enabled }, pressed);
		// View.ENABLED_FOCUSED_STATE_SET
		// bg.addState(new int[] { android.R.attr.state_enabled,
		// android.R.attr.state_focused }, focused);
		// View.ENABLED_STATE_SET
		bg.addState(new int[] { android.R.attr.state_enabled }, normal);
		// View.FOCUSED_STATE_SET
		// bg.addState(new int[] { android.R.attr.state_focused }, focused);
		// // View.WINDOW_FOCUSED_STATE_SET
		// bg.addState(new int[] { android.R.attr.state_window_focused },
		// unable);
		// View.EMPTY_STATE_SET
		bg.addState(new int[] {}, normal);
		return bg;
	}

//	/**
//	 * 获取客户端本地图片
//	 * */
//	public static Drawable getImageDrawable(String imageName, Context context) {
//		int id = OtherUtils.getDrawableId(context, imageName);
//		return id != 0 ? context.getResources().getDrawable(id) : null;
//	}

//	/**
//	 * 获取带点击效果的图片对象
//	 * */
//	public static StateListDrawable getStateListDrawable(String imageName,
//			String defaultValue, Context context) {
//		Drawable drawable = getImageDrawable(imageName, context);
//		drawable = drawable != null ? drawable : getImageDrawable(defaultValue,
//				context);
//		return createSLD(drawable);
//	}

}
