package sviolet.liba.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;

/**
 * Bitmap缓存
 * 
 * 用于记录每个Class下创建的Bitmap,便于集中回收内存
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

public class BitmapCache {

	HashMap<String, BitmapContainer> cache = null;

	/**
	 * 将一个bitmap添加到class名下的缓存中
	 * 
	 * @param c
	 * @param bitmap
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Bitmap add(Class c,Bitmap bitmap){
		if(c == null)
			return bitmap;
		
		return add(c.getSimpleName(),bitmap);
	}
	
	/**
	 * 将一个bitmap添加到名为className的缓存中
	 * 
	 * @param className
	 * @param bitmap
	 * @return
	 */
	public Bitmap add(String className, Bitmap bitmap) {

		if (className == null || bitmap == null)
			return bitmap;
		if (cache == null)
			cache = new HashMap<String, BitmapContainer>();
		
//		LogUtils.info("BitmapCache adding classname=" + className);
		if (cache.containsKey(className)) {
//			LogUtils.info("-->contain " + className);
			cache.get(className).add(bitmap);
		} else {
//			LogUtils.info("-->not contain " + className);
			BitmapContainer container = new BitmapContainer();
			container.add(bitmap);
			cache.put(className, container);
			container = null;
		}

		return bitmap;
	}

	/**
	 * 回收class名下bitmap的内存
	 * 
	 * @param c
	 */
	@SuppressWarnings("rawtypes")
	public void wipe(Class c){
		if(c == null)
			return;
		
		wipe(c.getSimpleName());
	}
	
	/**
	 * 回收名为className的bitmap的内存
	 * 
	 * @param c
	 */
	public void wipe(String className) {
		if (className == null || cache == null)
			return;
		
		if (cache.containsKey(className)) {
			cache.get(className).wipe();
			cache.remove(className);
		}
		System.gc();
	}

	/**
	 * 回收所有bitmap的内存
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void wipeAll() {
		if (cache == null)
			return;

		Iterator iter = cache.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, BitmapContainer> entry = (Map.Entry<String, BitmapContainer>) iter
					.next();
			entry.getValue().wipe();
		}
		cache.clear();
		System.gc();
	}

	/**
	 * Bitmap容器
	 * 
	 */
	public class BitmapContainer {

		List<Bitmap> bitmaps = null;

		/**
		 * 添加一个bitmap
		 * @param bitmap
		 */
		public void add(Bitmap bitmap) {
			if (bitmaps == null)
				bitmaps = new ArrayList<Bitmap>();
			bitmaps.add(bitmap);
//			LogUtils.info("-->BitmapContainer add bitmap");
		}

		/**
		 * 回收bitmap
		 */
		public void wipe() {
			if (bitmaps != null) {
				for (int i = 0; i < bitmaps.size(); i++) {
					Bitmap bitmap = bitmaps.get(i);
					if (bitmap != null && !bitmap.isRecycled()) {
						bitmap.recycle();
//						LogUtils.info("-->BitmapContainer recycle bitmap");
					}
					bitmap = null;
				}
				bitmaps = null;
			}
		}
	}

}
