package sviolet.turquoise.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

public class DateTimeUtils {
	/**
	 * 获得当前日期
	 * @return
	 */
	public static String getDate(){
		return SimpleDateFormat.getDateInstance().format(new Date());
	}
	
	/**
	 * 获得当前时间
	 * @return
	 */
	public static String getTime(){
		return SimpleDateFormat.getTimeInstance().format(new Date());
	}
	
	/**
	 * 获得当前日期和时间
	 * @return
	 */
	public static String getDateTime(){
		return SimpleDateFormat.getDateTimeInstance().format(new Date());
	}
	
	/**
	 * 获得指定格式的时间
	 * @param template
	 * @return
	 */
	public static String getDateTime(String template){
		SimpleDateFormat formater = new SimpleDateFormat(template, Locale.SIMPLIFIED_CHINESE);
		return formater.format(new Date());
	}
	
	/**
	 * 得到当前毫秒值
	 * @return
	 */
	public static long getTimeMillis(){
		return System.currentTimeMillis();
	}
	
	////////////////////////////////////////////////////////////////////
	
	/**
	 * 根据timeMillis获得日期
	 * @return
	 */
	public static String getDate(long timeMillis){
		return SimpleDateFormat.getDateInstance().format(new Date(timeMillis));
	}
	
	/**
	 * 根据timeMillis获得时间
	 * @return
	 */
	public static String getTime(long timeMillis){
		return SimpleDateFormat.getTimeInstance().format(new Date(timeMillis));
	}
	
	/**
	 * 根据timeMillis获得日期和时间
	 * @return
	 */
	public static String getDateTime(long timeMillis){
		return SimpleDateFormat.getDateTimeInstance().format(new Date(timeMillis));
	}
	
	/**
	 * 根据timeMillis获得指定格式的时间
	 * @param template
	 * @return
	 */
	public static String getDateTime(String template, long timeMillis){
		SimpleDateFormat formater = new SimpleDateFormat(template, Locale.SIMPLIFIED_CHINESE);
		return formater.format(new Date(timeMillis));
	}
}
