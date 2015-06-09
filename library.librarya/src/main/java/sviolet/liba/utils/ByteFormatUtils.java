package sviolet.liba.utils;

import java.text.DecimalFormat;

/**
 * @author SunPeng Email:sunpeng2013@csii.com.cn
 */
public class ByteFormatUtils {

	public static String formatByte(long data) {
		String pattern = "###.00";
		DecimalFormat format = new DecimalFormat(pattern);
		if (data <= -1) {
			return "0B";
		} else if (data < 1024) {
			return data + "B";
		} else if (data < 1024 * 1024) {
			return format.format(data / 1024f) + "K";
		} else if (data < 1024 * 1024 * 1024) {
			return format.format(data / 1024f / 1024f) + "M";
		} else if (data < 1024 * 1024 * 1024 * 1024) {
			return format.format(data / 1024f / 1024f / 1024f) + "G";
		}
		return "超出统计范围";
	}

	public static String formatKB(long data) {
		String pattern = "###.00";
		DecimalFormat format = new DecimalFormat(pattern);
		if (data <= -1) {
			return "0K";
		} else if (data < 1024) {
			return data + "K";
		} else if (data < 1024 * 1024) {
			return format.format(data / 1024f) + "M";
		} else if (data < 1024 * 1024 * 1024) {
			return format.format(data / 1024f / 1024f) + "G";
		}
		return "超出统计范围";
	}
}
