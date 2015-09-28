package sviolet.liba.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.widget.EditText;

/**
 * @author SunPeng
 */
public class RegexUtils {

	/**
	 * 登录名称格式验证
	 * 
	 * @param loginName
	 * @return
	 */
	public static boolean checkLoginName(String loginName) {
		Pattern p = Pattern.compile("^[a-zA-Z0-9\\_]{6,20}$");
		Matcher m = p.matcher(loginName);
		return m.matches();
	}

	/**
	 * 交易/查询密码格式验证
	 * 
	 * @param password
	 * @return
	 */
	public static boolean checkPassword(EditText password) {
		Pattern p = Pattern.compile("^[0-9]{6}$");
		Matcher m = p.matcher(password.getText().toString());
		return m.matches();
	}

	/**
	 * 登录密码格式验证 格式：6~16位数字或字母
	 * 
	 * @param password
	 * @return
	 */
	public static boolean checkLoginPassword(String password) {
		if (firstCheckLoginPassword(password)) {
			return secondeCheckLoginPassword(password);
		}
		return false;
	}

	public static boolean firstCheckLoginPassword(String password) {
		Pattern p = Pattern.compile("^[a-zA-Z0-9]{6,16}$");
		Matcher m = p.matcher(password);
		return m.matches();
	}

	private static boolean secondeCheckLoginPassword(String password) {
		Pattern p = Pattern
				.compile("^[a-zA-Z]+\\d+[a-zA-Z0-9]*|\\d+[a-zA-Z]+[a-zA-Z0-9]*$");
		Matcher m = p.matcher(password);
		return m.matches();
	}

	/**
	 * 验证手机号码
	 * 
	 * @param number
	 * @return
	 */
	public static boolean checkPhoneNumber(String number) {
		Pattern p = Pattern.compile("^1[34578]\\d{9}$");
		Matcher m = p.matcher(number);
		return m.matches();
	}

	/**
	 * 验证电信天翼手机号码
	 */
	public static boolean checkTYPhoneNumber(String number) {
		Pattern p = Pattern.compile("^1[358]\\d{9}$");
		Matcher m = p.matcher(number);
		return m.matches();
	}

	/**
	 * 验证邮箱
	 * 
	 * @param email
	 * @return
	 */
	public static boolean checkEmail(String email) {
		String pattern = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(email);
		return m.matches();
	}

	/**
	 * 验证身份证号
	 * 
	 * @param idNumber
	 * @return
	 */
	public static boolean checkID(String idNumber) {
		String pattern = "((11|12|13|14|15|21|22|23|31|32|33|34|35|36|37|41|42|43|44|45|46|50|51|52|53|54|61|62|63|64|65|71|81|82|91)\\d{4})((((19|20)(([02468][048])|([13579][26]))0229))|((20[0-9][0-9])|(19[0-9][0-9]))((((0[1-9])|(1[0-2]))((0[1-9])|(1\\d)|(2[0-8])))|((((0[1,3-9])|(1[0-2]))(29|30))|(((0[13578])|(1[02]))31))))((\\d{1})|(\\d{3}(x|X))|(\\d{4}))";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(idNumber);
		return m.matches();
	}

	/**
	 * 验证是否是正整数
	 * 
	 * @param number
	 * @return
	 */
	public static boolean checkNumber(String number) {
		String pattern = "^[1-9]\\d*$";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(number);
		return m.matches();
	}

	/**
	 * 验证家庭电话号码
	 * 
	 * @param number
	 * @return
	 */
	public static boolean checkFamilyNumber(String number) {
		String pattern = "\\d{6,12}";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(number);
		return m.matches();
	}

	/**
	 * 验证金额格式 最多只能保留2位有效数字
	 * 
	 * @param amount
	 * @return
	 */
	public static boolean checkAmount(String amount) {
		Pattern p = Pattern
				.compile("^[1-9]{1}\\d*|[1-9]{1}\\d*\\.\\d{0,2}$|^0\\.[1-9]{1}\\d?$|^0\\.\\d[1-9]$");
		Matcher m = p.matcher(amount);
		return m.matches();
	}

	/**
	 * 验证是否是正整数
	 * 
	 * @param amount
	 * @return
	 */
	public static boolean checkIntegerAmount(String amount) {
		Pattern p = Pattern.compile("^[1-9]{1}\\d*$");
		Matcher m = p.matcher(amount);
		return m.matches();
	}

	/**
	 * 判断是否是中文
	 * 
	 * @param password
	 * @return
	 */
	public static boolean isContainChinese(char c) {
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(String.valueOf(c));
		return m.find();
	}

	/**
	 * 验证有线电视数字号码
	 */
	public static boolean checkTVNumber(String tv_number) {
		Pattern p = Pattern.compile("^[0-9]{12}$");
		Matcher m = p.matcher(tv_number);
		return m.matches();
	}

}
