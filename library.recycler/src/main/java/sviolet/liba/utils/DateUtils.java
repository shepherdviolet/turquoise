package sviolet.liba.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.annotation.SuppressLint;

/**
 * @author SunPeng
 */
@SuppressLint("SimpleDateFormat")
public class DateUtils {

	public static final long DAY_MILLIS = 1000 * 60 * 60 * 24L;
	public static final long ONE_WEEK_MILLIS = 7 * 24 * 60 * 60 * 1000L;

	private static final SimpleDateFormat datetimeFormat = new SimpleDateFormat(
			"yyyy/MM/dd  HH:mm:ss");
	private static final SimpleDateFormat datetimeFormat2 = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat(
			"HH:mm");

	private static final SimpleDateFormat cndateFormat = new SimpleDateFormat(
			"yyyy年MM月dd日");

	private static final SimpleDateFormat formatYearAndMonth = new SimpleDateFormat(
			"yy-MM");

	/**
	 * 获得当前日期时间 日期时间格式yyyy-MM-dd HH:mm:ss
	 */
	public static String currentDatetime() {
		return datetimeFormat.format(now());
	}

	public static String formatDateTime() {
		return datetimeFormat.format(new Date(millis()));
	}

	public static String formatDate() {
		return dateFormat.format(new Date(millis()));
	}

	public static String formatDateAfterOneDay() {
		return dateFormat.format(new Date(millis() + DAY_MILLIS));
	}

	public static String formatDateBeforOneWeek() {
		return dateFormat.format(new Date(millis() - ONE_WEEK_MILLIS));
	}

	public static String formatDateAfterOneWeek() {
		return dateFormat.format(new Date(millis() + ONE_WEEK_MILLIS));
	}

	/**
	 * 格式化日期时间 日期时间格式yyyy-MM-dd HH:mm:ss
	 */
	public static String formatDatetime(Date date) {
		return datetimeFormat.format(date);
	}

	/**
	 * 格式化日期时间 日期时间格式yyyy-MM-dd HH:mm
	 */
	public static String formatDatetime2(Date date) {
		return datetimeFormat2.format(date);
	}

	/**
	 * 格式化日期时间
	 */
	public static String formatDatetime(Date date, String pattern) {
		SimpleDateFormat customFormat = (SimpleDateFormat) datetimeFormat
				.clone();
		customFormat.applyPattern(pattern);
		return customFormat.format(date);
	}

	/**
	 * 获得当前日期 日期格式yyyy-MM-dd
	 */
	public static String currentDate() {
		return dateFormat.format(now());
	}

	/**
	 * 格式化日期 日期格式yyyy-MM-dd
	 */
	public static String formatDate(Date date) {
		return dateFormat.format(date);
	}

	/**
	 * 格式化日期 中文方式 日期格式yyyy年MM月dd日
	 */
	public static String formatCndate(Date date) {
		return cndateFormat.format(date);
	}

	/**
	 * 获得当前时间 时间格式HH:mm:ss
	 */
	public static String currentTime() {
		return timeFormat.format(now());
	}

	/**
	 * 格式化时间 时间格式HH:mm:ss
	 */
	public static String formatTime(Date date) {
		return timeFormat.format(date);
	}

	/**
	 * 获得当前时间的<code>java.util.Date</code>对象
	 */
	public static Date now() {
		return new Date();
	}

	public static Calendar calendar() {
		Calendar cal = GregorianCalendar.getInstance(Locale.CHINESE);
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		return cal;
	}

	/**
	 * 获得当前时间的毫秒数
	 */
	public static long millis() {
		return System.currentTimeMillis();
	}

	/**
	 * 将字符串日期时间转换成java.util.Date类型 日期时间格式yyyy-MM-dd HH:mm:ss
	 */
	public static Date parseDatetime(String datetime) throws ParseException {
		return datetimeFormat.parse(datetime);
	}

	/**
	 * 将字符串日期转换成java.util.Date类型 日期时间格式yyyy-MM-dd
	 */
	public static Date parseDate(String date) throws ParseException {
		return dateFormat.parse(date);
	}

	/**
	 * 将字符串日期转换成java.util.Date类型 时间格式 HH:mm:ss
	 */
	public static Date parseTime(String time) throws ParseException {
		return timeFormat.parse(time);
	}

	/**
	 * 根据自定义pattern将字符串日期转换成java.util.Date类型
	 */
	public static Date parseDatetime(String datetime, String pattern)
			throws ParseException {
		SimpleDateFormat format = (SimpleDateFormat) datetimeFormat.clone();
		format.applyPattern(pattern);
		return format.parse(datetime);
	}

	/**
	 * 得到字符串日期格式的毫秒值
	 * 
	 * @param date
	 *            yyyy-MM-dd
	 */
	public static long getDateMillis(String date) {
		try {
			return parseDate(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 获得两个字符串日期格式之间的天数
	 */
	public static int getDays(String date1, String date2) {
		return (int) (Math.abs(getDateMillis(date1) - getDateMillis(date2)) / DAY_MILLIS);
	}

	/**
	 * 格式化年月 YY-MM
	 */
	public static String formatYearAndMonth() {
		return formatYearAndMonth.format(new Date(millis()));
	}

	/**
	 * 比较两个字符串日期格式 YY-MM-dd 大小
	 * 
	 * @param startDate
	 * @param endDate
	 * @return true 表示开始日期大于结束日期
	 */
	public static boolean compareDate(String startDate, String endDate) {
		if (getDateMillis(startDate) - getDateMillis(endDate) > 0)
			return true;
		else
			return false;
	}

}