package sviolet.turquoise.util.common;

import android.os.SystemClock;

import java.text.ParseException;
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

    /**************************************************
     * 获得当前日期相关
     */

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
	public static long getCurrentTimeMillis(){
		return System.currentTimeMillis();
	}

	/**
	 * [Android]获得系统启动至今经过的毫秒数, 深睡眠时不计时
	 * @return
	 */
	public static long getUptimeMillis(){
		return SystemClock.uptimeMillis();
	}

	/**
	 * 获得当前纳秒时间, 该时间仅用于计算程序经过时间, 不保证精确
	 * @return
     */
	public static long getNanoTime(){
		return System.nanoTime();
	}

    /*************************************************
     * 指定时间相关
     */
	
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

    /**
     * 根据Date获得日期
     * @return
     */
    public static String getDate(Date date){
        return SimpleDateFormat.getDateInstance().format(date);
    }

    /**
     * 根据Date获得时间
     * @return
     */
    public static String getTime(Date date){
        return SimpleDateFormat.getTimeInstance().format(date);
    }

    /**
     * 根据Date获得日期和时间
     * @return
     */
    public static String getDateTime(Date date){
        return SimpleDateFormat.getDateTimeInstance().format(date);
    }

    /**
     * 根据Date获得指定格式的时间
     * @param template
     * @return
     */
    public static String getDateTime(String template, Date date){
        SimpleDateFormat formater = new SimpleDateFormat(template, Locale.SIMPLIFIED_CHINESE);
        return formater.format(date);
    }

	/*******************************************
	 *  String转换为日期
	 */

	/**
	 * 将字符串按格式转换为Date
	 * @param dateStr 日期字符串
	 * @param pattern 格式, 例如yyyy-MM-dd HH-mm-ss
	 * @throws ParseException
	 */
	public static Date parseDate(String dateStr, String pattern) throws ParseException {
		SimpleDateFormat formater = new SimpleDateFormat(pattern, Locale.SIMPLIFIED_CHINESE);
		return formater.parse(dateStr);
	}

    /********************************************
     * 日期计算
     */


}
