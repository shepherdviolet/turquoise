package sviolet.liba.utils;

import java.text.DecimalFormat;

import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * @author SunPeng
 */
public class FormatUtils {

	private static final int SEPARATOR_NUMBER = 4;

	/**
	 * 格式化手机号码
	 */
	public static String formate2PhoneNum(String text) {
		return text.substring(0, 3) + "-" + text.substring(3, 7) + "-"
				+ text.substring(7, 11);
	}

	/**
	 * 格式化金额，保留2位有效数字
	 */
	public static String formatAmount(String amount) {
		if (CommonUtils.isEmpty(amount))
			return "0.00";
		// return formatSpecialAmount(amount);
		return formatQueryAmount(amount);
	}

	public static String formatSpecialAmount(String amount) {
		DecimalFormat format = new DecimalFormat("#0.00");
		return format.format(Double.valueOf(amount));
	}

	/**
	 * 格式化账户余额，保留2位有效数字
	 */
	public static String formatQueryBalance(String amount) {
		if (CommonUtils.isEmpty(amount))
			return "00";
		return formatQueryAmount(amount);
	}

	public static String formatQueryAmount(String amount) {
		DecimalFormat format = new DecimalFormat("###,###,##0.00");
		return format.format(Double.valueOf(amount));
	}

	/**
	 * 格式化利率,保留4位有效数字
	 */
	public static String formatRate(Double amount) {
		DecimalFormat format = new DecimalFormat("#0.0000");
		return format.format(amount);
	}

	/**
	 * 格式化负数
	 */
	public static String formatNegativeAmount(String amount) {
		String value = amount.substring(1);
		int dotIndex = amount.indexOf(".");
		char[] charArray = value.toCharArray();
		int index = 0;
		for (int i = 0; i < charArray.length; i++) {
			if (charArray[i] != '0') {
				index = i;
				break;
			}
		}
		if (index != dotIndex - 1)
			return "-" + value.substring(index);
		else
			return "-0" + value.substring(index);
	}

	/**
	 * 监听输入的银行卡号并每隔4位加一个空格
	 */
	public static void formatCardNumber(final EditText text) {
		text.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				text.removeTextChangedListener(this);
				String value = CommonUtils.getRealValue(text);
				if (!CommonUtils.isEmpty(value)) {
					text.setText(separateCardNumber(value));
					Spannable editable = text.getText();
					Selection.setSelection(editable, editable.length());
				}
				text.addTextChangedListener(this);
			}
		});
	}

	/**
	 * 监听输入的手机号，开头3位加一个空格，后每隔4位加一个空格
	 */
	public static void formatPhoneNumber(final EditText text) {
		text.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				text.removeTextChangedListener(this);
				String value = CommonUtils.getRealValue(text);
				if (!CommonUtils.isEmpty(value)) {
					text.setText(separatePhoneNumber(value));
					Spannable editable = text.getText();
					Selection.setSelection(editable, editable.length());
				}
				text.addTextChangedListener(this);
			}
		});
	}

	/**
	 * 每隔4位加一个空格
	 */
	public static String separateCardNumber(String number) {

		int length = number.length();
		int tab_size = (float) length / (float) SEPARATOR_NUMBER - length
				/ SEPARATOR_NUMBER > 0 ? length / SEPARATOR_NUMBER : length
				/ SEPARATOR_NUMBER - 1;
		StringBuilder sb = new StringBuilder(number);
		for (int i = 1; i <= tab_size; i++) {
			if (i * SEPARATOR_NUMBER <= length) {
				sb.insert(i * SEPARATOR_NUMBER + i - 1, " ");
			}
		}
		return sb.toString();
	}

	/**
	 * 前3位加一个空格 后面每隔4位加一个空格 主要用于手机号码
	 */
	public static String separatePhoneNumber(String number) {

		int length = number.length();
		int tab_size = (float) (length - 3) / (float) SEPARATOR_NUMBER
				- (length - 3) / SEPARATOR_NUMBER > 0 ? (length - 3)
				/ SEPARATOR_NUMBER : (length - 3) / SEPARATOR_NUMBER - 1;
		tab_size++;
		StringBuilder sb = new StringBuilder(number);
		for (int i = 1; i <= tab_size; i++) {
			if (i == 1) {
				if (3 <= length) {
					sb.insert(3, " ");
				}
			} else {
				if (i * SEPARATOR_NUMBER <= length) {
					sb.insert(3 + (i - 1) * SEPARATOR_NUMBER + i - 1, " ");
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 格式化时间
	 */
	public static String formatTime(int second) {
		if (second < 60) {
			return second + "秒";
		} else if (second < 60 * 60) {
			return second / 60 + "分" + second % 60 + "秒";
		} else {
			return second / (60 * 60) + "小时" + second % (60 * 60) / 60 + "分"
					+ second % 60 + "秒";
		}
	}
}
