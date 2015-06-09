package sviolet.demoa;

import android.util.Log;

/**
 * 调试工具
 * @author S.Violet
 */
public class DeUtils {
	
	////////////////////配置////////////////////////////////

//	public static final boolean DEBUG = false;//生产
	public static final boolean DEBUG = true;//测试

	private static final String TAG = "DeUtils";

	///////////////////内部变量/////////////////////////////
	
	private static long time = 0;
	
	////////////////////Log/////////////////////////////////
	
	/**
	 * debug日志(蓝)
	 * 
	 * @param msg
	 */
	public static void logd(String msg) {
		if(msg == null){
			loge("DeUtils msg == null");
			return;
		}
		if(DEBUG)
			Log.d(TAG, msg);
	}

	/**
	 * info日志(绿)
	 * 
	 * @param msg
	 */
	public static void logi(String msg) {
		if(msg == null){
			loge("DeUtils msg == null");
			return;
		}
		if(DEBUG)
			Log.i(TAG, msg);
	}

	/**
	 * error日志(红)
	 * 
	 * @param msg
	 */
	public static void loge(String msg) {
		if(msg == null){
			loge("DeUtils msg == null");
			return;
		}
		if(DEBUG)
			Log.e(TAG, msg);
	}
	
	/**
	 * error日志(红)
	 * 
	 * @param msg
	 * @param tr
	 */
	public static void loge(String msg, Throwable throwable) {
		if(msg == null){
			loge("DeUtils msg == null");
			return;
		}
		if(throwable == null){
			loge("DeUtils throwable == null");
			return;
		}
		if(DEBUG)
			Log.e(TAG, msg, throwable);
	}
	
	/**
	 * print日志
	 * 
	 * @param msg
	 */
	public static void print(String msg){
		if(msg == null){
			println("DeUtils msg == null");
			return;
		}
		if(DEBUG)
			System.out.print(msg);
	}
	
	/**
	 * println日志
	 * 
	 * @param msg
	 */
	public static void println(String msg){
		if(msg == null){
			println("DeUtils msg == null");
			return;
		}
		if(DEBUG)
			System.out.println(msg);
	}
	
	///////////////////////检测NULL////////////////////////////
	
	public static void logNull(Object obj, int tag){
		logNull(obj, Integer.toString(tag));
	}
	
	public static void logNull(Object obj, String tag){
		if(DEBUG){
			if(tag == null)
				tag = "";
			if(obj == null)
				loge("[Null Check] " + tag + " null");
			else
				loge("[Null Check] " + tag + " not null");
		}
	}
	
	public static void printNull(Object obj, int tag){
		printNull(obj, Integer.toString(tag));
	}
	
	public static void printNull(Object obj, String tag){
		if(DEBUG){
			if(tag == null)
				tag = "";
			if(obj == null)
				println("[Null Check] " + tag + " null");
			else
				println("[Null Check] " + tag + " not null");
		}
	}
	
	///////////////////////检测执行时间////////////////////////
	
	/**
	 * 重置计时(time=System.currentTimeMillis())
	 */
	public static void resetTime(){
		if(DEBUG)
			time = System.currentTimeMillis();
	}
	
	public static void logTime(int tag){
		logTime(Integer.toString(tag));
	} 
	
	/**
	 * log打印当前时间/经过时间
	 */
	public static void logTime(String tag){
		if(DEBUG){
			if(tag == null)
				tag = "";
			if(time == 0){
				loge("[DeUtils] 打印时间前请先resetTime()");
			}else{
				logi( tag + " time:" + (System.currentTimeMillis() - time));
			}
			resetTime();
		}
	}
	
	public static void printTime(int tag){
		printTime(Integer.toString(tag));
	} 
	
	/**
	 * println打印当前时间/经过时间
	 */	
	public static void printTime(String tag){
		if(DEBUG){
			if(tag == null)
				tag = "";
			if(time == 0){
				println("[DeUtils] 打印时间前请先resetTime()");
			}else{
				println( tag + " time:" + (System.currentTimeMillis() - time));
			}
			resetTime();
		}
	}
	
	///////////////////debug任务////////////////////////////
	
	/**
	 * 执行一个debug任务(DEBUG=true时执行)
	 * 
	 * @param task
	 */
	public void exe(Runnable task){
		if(DEBUG)
			task.run();
	}
	
	/**
	 * 新线程执行一个debug任务(DEBUG=true时执行)
	 * 
	 * @param task
	 */
	public void exet(Runnable task){
		if(DEBUG)
			new Thread(task).start();
	}
}
