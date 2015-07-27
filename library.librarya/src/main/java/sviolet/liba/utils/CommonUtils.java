package sviolet.liba.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author SunPeng
 */
public class CommonUtils {

	/**
	 * 显示前start位和后end位 中间所有数字用****号代替
	 * */
	public static String convertAcNo(String acNo, int start, int end) {
		if (isEmpty(acNo))
			return null;
		int length = acNo.length();
		if (length <= start + end) {
			return acNo;
		} else {
			return acNo.substring(0, start) + "****"
					+ acNo.substring(acNo.length() - end, acNo.length());
		}
	}

	/**
	 * 前2 后2位明文 中间用****代替
	 */
	public static String convertLoginName(String acNo) {
		if (isEmpty(acNo))
			return null;
		return acNo.substring(0, 2) + "****"
				+ acNo.substring(acNo.length() - 2, acNo.length());
	}

	/**
	 * 显示前start位和后end位，中间每个数字用*代替
	 */
	public static String convertString(String value, int start, int end) {
		if (isEmpty(value))
			return null;
		int length = value.length();
		if (length <= start + end) {
			return value;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(value.substring(0, start));
			for (int i = start; i < length - end; i++) {
				sb.append('*');
			}
			sb.append(value.substring(length - end, length));
			return sb.toString();
		}
	}

	/**
	 * 去掉所有空格
	 */
	public static String getRealValue(EditText editText) {
		String value = editText.getText().toString();
		if (isEmpty(value))
			return null;
		else
			return value.replaceAll(" ", "");
	}

	/**
	 * 获取卡号后4位
	 * 
	 * @param number
	 * @return
	 */
	public static String getLast4Number(String number) {
		if (number != null && number.length() >= 4)
			return number.substring(number.length() - 4);
		else
			return null;
	}

	/**
	 * 比较两个double数的大小
	 * 
	 * @return true 表示double2比double1大
	 */
	public static boolean compare(String double1, String double2) {
		if (new BigDecimal(double2).compareTo(new BigDecimal(double1)) > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 比较两个double数的大小
	 * 
	 * @return true 表示double2比double1*percent大
	 */
	public static boolean compareWithPercent(String double1, String double2,
			float percent) {
		if (Double.parseDouble(double2) - Double.parseDouble(double1) * percent > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 比较两个double数的大小
	 * 
	 * @return true 表示double1比double2小
	 */
	public static boolean getSmaller(String double1, String double2) {
		if (new BigDecimal(double1).compareTo(new BigDecimal(double2)) <= 0) {
			return true;
		}
		return false;
	}

	/**
	 * 验证输入的是否是100的整数
	 */
	public static boolean checkAmountIsMultipleOf100(String amount) {
		return Integer.parseInt(amount) % 100 == 0 ? true : false;
	}

	public static boolean checkListSize(ArrayList<String> list) {
		return (list != null && list.size() > 0) ? true : false;
	}

	public static boolean isEmpty(String value) {
		if (value == null || "".equals(value.trim()))
			return true;
		return false;
	}

	/**
	 * 获得字符的长度，中文2个字符
	 */
	public static int getTextLength(String value) {
		int len = 0;
		char[] charArray = value.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			if (RegexUtils.isContainChinese(charArray[i]))
				len += 2;
			else
				len++;
		}
		return len;
	}

	/**
	 * 获取asset资源文件
	 */
	public static String getAssetsData(Context context, String name) {
		InputStream in = null;
		BufferedReader reader = null;
		StringBuilder sb = null;
		try {
			in = context.getResources().getAssets().open(name);
			reader = new BufferedReader(new InputStreamReader(in));
			sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (in != null)
				in.close();
			if (reader != null)
				reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 统一下拉框选择列表
	 */
	public static Dialog createSingleChoiceDialog(Context context,
			String title, final TextView textView, final ArrayList<String> list) {

		return new AlertDialog.Builder(context)
				.setTitle(title)
				.setSingleChoiceItems(parseList2Array(list), 0,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								textView.setText(list.get(which));
								dialog.dismiss();
							}
						}).create();
	}

	/**
	 * list 2 array
	 */
	public static String[] parseList2Array(ArrayList<String> list) {
		String[] data = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			data[i] = list.get(i);
		}
		return data;
	}

	/**
	 * 获取versionName
	 */
	public static String getAppVersionName(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(),
							PackageManager.GET_UNINSTALLED_PACKAGES);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "1.0.0";
	}

	/**
	 * 获取versionCode
	 */
	public static int getAppVersionCode(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(),
							PackageManager.GET_UNINSTALLED_PACKAGES);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 1;
	}

	/**
	 * 获取Apk包的md5值
	 * */
	public static String getApkMD5(Context context) {
		String md5 = "";
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(),
							PackageManager.GET_SIGNATURES);
			md5 = MD5Utils.getFileMD5String(new File(packageInfo.applicationInfo.sourceDir));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return md5;
	}

	/**
	 * 2.3以上
	 */
	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	/**
	 * 3.0以上
	 */
	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	/**
	 * 退出程序
	 */
	public static void exitApp() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				//退出应用时回收所有bitmap内存
//				BitmapUtils.wipeAll();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		}).start();
	}

	/**
	 * 账号选择
	 * 
	 * @param acNum
	 * @param acNickName
	 * @return
	 */
	public static String convertSelectorAccount(String acNum, String acNickName) {
		if (CommonUtils.isEmpty(acNickName))
			return acNum;
		else
			return acNum + "/" + acNickName;
	}

	/**
	 * 从选择的账号信息中得到账号
	 * 
	 * @param selectorAccNum
	 * @return
	 */
	public static String getRealAccNum(String selectorAccNum) {
		if (selectorAccNum.contains("/"))
			return selectorAccNum.split("/")[0];
		else
			return selectorAccNum;
	}

	/**
	 * 从联系人列表选择的手机号码
	 */
	public static String getPhoneNumber(Activity context, Intent data) {
		String phone = getNumber(context, data);
		if (phone != null)
			return phone.replaceAll(" ", "").replaceAll("-", "");
		else
			return null;
	}

	/**
	 * 从电话薄账户获取手机号码
	 *
	 * @param context
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	private static String getNumber(Activity context, Intent data) {
		String s = null;
		try {
			Uri uri = data.getData();
			Cursor c = context.managedQuery(uri, null, null, null, null);
			if (c.moveToFirst()) {
				String id = c.getString(c
						.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
				Cursor phones = context.managedQuery(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = " + id, null, null);
				if (phones.moveToFirst()) {
					s = phones
							.getString(phones
									.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return s;
	}
}
