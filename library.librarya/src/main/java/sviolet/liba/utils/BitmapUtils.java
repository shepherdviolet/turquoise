package sviolet.liba.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.text.TextPaint;
import android.util.Base64;

/**
 * Bitmap工具(自动回收Bitmap功能, 务必使用wipe()方法回收)
 * 
 * @author S.Violet
 * 
 */

@SuppressWarnings("rawtypes")
public class BitmapUtils {

	private static BitmapCache cache = new BitmapCache();
	
	/*********************************************
	 * 				解码方法
	 *********************************************/
	
	/**
	 * (自动cache)从资源文件中解码图片(根据宽高需求"整数倍"缩放图片,节省内存)
	 * 
	 * @param c Class:回收类标记
	 * @param res getResource()
	 * @param resId 资源文件ID
	 * @param reqWidth 需求宽度
	 * @param reqHeight 需求高度
	 * @return
	 */
	public static Bitmap decodeFromResource(Class c,Resources res, int resId, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		return cache(c,BitmapFactory.decodeResource(res, resId, options));
	}

	/**
	 * (自动cache)从指定文件中解码图片(根据宽高需求"整数倍"缩放图片,节省内存)
	 * 
	 * @param c Class:回收类标记
	 * @param filename 文件路径
	 * @param reqWidth 需求宽度
	 * @param reqHeight 需求高度
	 * @return
	 */
	public static Bitmap decodeFromFile(Class c, String filename, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		return cache(c,BitmapFactory.decodeFile(filename, options));
	}

	/**
	 * 将指定的图片数据按照指定宽高转成bitmap(自动cache)
	 */
	
	/**
	 * (自动cache)用二进制数据解码图片(根据宽高需求"整数倍"缩放图片,节省内存)
	 * 
	 * @param c Class:回收类标记
	 * @param data 二进制数据
	 * @param reqWidth 需求宽度
	 * @param reqHeight 需求高度
	 * @return
	 */
	public static Bitmap decodeFromByteArray(Class c, byte[] data, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		return cache(c,BitmapFactory.decodeByteArray(data, 0, data.length, options));
	}
	
	/**
	 * (自动cache)从资源文件中解码图片,并绘制文字(根据宽高需求"整数倍"缩放图片,节省内存)
	 * 
	 * @param c Class:回收类标记
	 * @param res getResource()
	 * @param resId 资源ID
	 * @param text 需要绘制的文字
	 * @param x 文字在X方向的位移
	 * @param y 文字在Y方向的位移
	 * @param textSize 字体大小
	 * @param textColor 字体颜色
	 * @return
	 */
	public static Bitmap drawTextOnResource(Class c, Resources res, int resId, String text,float x,float y,float textSize,int textColor){		
		Bitmap resBitmap = BitmapFactory.decodeResource(res, resId);
		if(text == null)
			return cache(c,resBitmap);
		//copy, 防止出现immutable bitmap异常
		Bitmap bitmap = resBitmap.copy(Bitmap.Config.ARGB_8888, true);
		resBitmap.recycle();
		drawText(bitmap,text,x,y,textSize,textColor);
		return cache(c,bitmap);
	}
	
	/**
	 * 根据指定的宽高计算缩放因子
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		//不缩放的情况
		if(reqWidth  == 0 || reqHeight == 0){
			return 1;
		}
		// 源图片宽高
		final int width = options.outWidth;
		final int height = options.outHeight;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
			final float totalPixels = width * height;
			final float totalReqPixelsCap = reqWidth * reqHeight * 2;
			while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
				inSampleSize++;
			}
		}
		return inSampleSize;
	}
	
	/**
	 * (未cache)在Bitmap上绘制文字
	 * 
	 * immutable bitmap pass to canvas异常解决:
	 * bitmap.copy(Bitmap.Config.ARGB_8888, true);
	 * 
	 * @author S.Violet
	 */
	public static Bitmap drawText(Bitmap bitmap,String text,float x,float y,float textSize,int textColor){
		Canvas canvas = new Canvas(bitmap);
		TextPaint textPaint = new TextPaint();
		textPaint.setTextSize(textSize);
		textPaint.setColor(textColor);
		canvas.drawText(text, x, y, textPaint);
		return bitmap;
	}

	/**
	 * bitmap转成drawable
	 */
	@SuppressWarnings("deprecation")
	public static BitmapDrawable bitmap2Drawable(Bitmap bitmap) {
		return new BitmapDrawable(bitmap);
	}

	/**
	 * 保存图片到相册
	 * 
	 * @param bm
	 * @param fileName
	 *            （.png）
	 */
	public static void saveBitmap2photo(Context context, Bitmap bitmap, String appName,
			String fileName) {
		String path = FileUtils.getSDPath();
		if (path == null) {
			// CustomDialogUtils.alertDialog(context, "保存失败，请检查SD卡是否挂载");
			return;
		}
		path = path + File.separator + appName;
		File dirFile = new File(path);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File file = new File(path + File.separator + fileName);
		OutputStream os;
		try {
			os = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
			os.flush();
			os.close();

			Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			Uri uri = Uri.fromFile(file);
			intent.setData(uri);
			context.sendBroadcast(intent);
			
			//ToastUtils.showShortToast(context, "图片成功保存到相册中");
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	/**
	 * 线程方式保存图片到相册
	 * 
	 * @param bitmap
	 * @param fileName 图片名称
	 * @param what 图片标识(message.what)
	 * @param handler handler(处理后续工作)
	 * 
	 * @author S.Violet
	 */
	public static void saveBitmap2photoByThread(final Bitmap bitmap,final String appName,final String fileName,final int what,final Handler handler) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				String path = FileUtils.getSDPath();
				if (path != null) {
					path = path + File.separator + appName;
					File dirFile = new File(path);
					if (!dirFile.exists()) {
						dirFile.mkdir();
					}
					File file = new File(path + File.separator + fileName);
					OutputStream os;
					try {
						os = new FileOutputStream(file);
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
						os.flush();
						os.close();
						if(handler != null)
							handler.sendEmptyMessage(what);
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
					}
				}
			}
		}).start();
	}
	
	/**
	 * 将base64格式数据按照指定宽高转成bitmap
	 */
	public static Bitmap decodeFromBase64(Class c,String base64Data, int reqWidth,
			int reqHeight) {
		return decodeFromByteArray(c,Base64.decode(base64Data, Base64.DEFAULT),
				reqWidth, reqHeight);
	}

	/**
	 * bitmap转为base64
	 */
	public static String bitmapToBase64(Bitmap bitmap) {
		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
				baos.flush();
				baos.close();
				byte[] bitmapBytes = baos.toByteArray();
				result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * (自动Cache)按比例缩放图片(实际比例)<p>
	 * 原始bitmap会被回收
	 * 
	 * @param bitmap
	 * @param scale 缩放比例
	 * @return
	 */
	public static Bitmap zoom(Class c, Bitmap bitmap, float scale) {
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap result = cache(c, Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true));
		bitmap.recycle();
		return result;
	}

	/**
	 * (自动Cache)按比例缩放图片(实际比例)<p>
	 * 原始bitmap会被回收
	 * 
	 * @param bitmap
	 * @param scaleX x缩放比例
	 * @param scaleY y缩放比例
	 * @return
	 */
	public static Bitmap zoom(Class c, Bitmap bitmap, float scaleX, float scaleY) {
		Matrix matrix = new Matrix();
		matrix.postScale(scaleX, scaleY);
		Bitmap result = cache(c, Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true));
		bitmap.recycle();
		return result;
	}

	/**
	 * (自动Cache)将图片缩放至指定宽高<p>
	 * 原始bitmap会被回收,宽高不得为0
	 * s
	 * @param bitmap
	 * @param width 指定宽
	 * @param height 指定高
	 * @return
	 */
	public static Bitmap zoom(Class c, Bitmap bitmap, int width, int height){
		return zoom(c, bitmap, (float)width / (float)bitmap.getWidth(), (float)height / (float)bitmap.getHeight());
	}
	
	/**
	 * 图片圆角处理
	 * 
	 * @param bitmap
	 * @param roundPX
	 * @return
	 */
	public static Bitmap getRCB(Bitmap bitmap, float roundPX) {
		// RCB means
		// Rounded
		// Corner Bitmap
		Bitmap dstbmp = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(dstbmp);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return dstbmp;
	}
	
	/*********************************************
	 * Bitmap回收机制
	 * 
	 * @param c
	 * @param bitmap
	 * 
	 * @author S.Violet ZhuQinChao
	 *********************************************/
	
	/**
	 * 将bitmap加入缓存中
	 * 
	 * @param c bitmap关联的class
	 * @param bitmap
	 * @return
	 */
	public static Bitmap cache(String className,Bitmap bitmap){
		cache.add(className, bitmap);
		return bitmap;
	}
	
	/**
	 * 将bitmap加入缓存中
	 * 
	 * @param c bitmap关联的class
	 * @param bitmap
	 * @return
	 */
	public static Bitmap cache(Class c,Bitmap bitmap){
		cache.add(c, bitmap);
		return bitmap;
	}
	
	/**
	 * 将className的bitmap内存回收
	 * 
	 * @param c
	 */
	public static void wipe(String className){
		if(cache != null)
			cache.wipe(className);
	}
	
	/**
	 * 将class名下的bitmap内存回收
	 * 
	 * @param c
	 */
	public static void wipe(Class c){
		if(cache != null)
			cache.wipe(c);
	}
	
	/**
	 * 回收所有bitmap内存
	 */
	public static void wipeAll(){
		if(cache != null)
			cache.wipeAll();
	}
}
