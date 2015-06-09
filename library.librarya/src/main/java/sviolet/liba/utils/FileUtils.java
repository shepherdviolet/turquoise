package sviolet.liba.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import sviolet.liba.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

/**
 * @author SunPeng Email:sunpeng2013@csii.com.cn
 */
public class FileUtils {

	/**
	 * 获取asset文件
	 */
	public static String getJsonDataFromAsset(Context context,
			String jsonFileName) {
		InputStream is = null;
		BufferedReader reader = null;
		try {
			is = context.getAssets().open(jsonFileName);
			StringBuilder sb = new StringBuilder();
			reader = new BufferedReader(new InputStreamReader(is));
			sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			is.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 获取SD卡根目录
	 */
	public static String getSDPath() {
		String path = null;
		if (checkSdcard())
			path = Environment.getExternalStorageDirectory().getAbsolutePath();
		return path;
	}

	/**
	 * 安装apk
	 * 
	 * @param context
	 * @param fileName
	 */
	public static void installApk(Context context, String appName, String fileName) {
		File file = getFile(context, appName, fileName);
		if (file != null) {
			if (fileName.endsWith(".apk")) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		}
	}

	/**
	 * 得到文件名称
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileName(String path) {
		return path.substring(path.lastIndexOf("/") + 1);
	}

	/**
	 * 获取sd中目标文件
	 */
	public static File getExternalFile(String appName, String fileName) {
		return new File(getExternalFilesDir(appName), fileName);
	}

	/**
	 * 根据文件名称获取文件
	 */
	public static File getFile(Context context, String appName, String fileName) {
		if (checkSdcard())
			return getExternalFile(appName, fileName);
		else
			return new File(context.getFilesDir(), fileName);
	}

	/**
	 * 判断sd卡是否挂载
	 */
	public static boolean checkSdcard() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	/**
	 * 删除文件
	 */
	public static void deleteFile(Context context, String appName, String fileName) {
		File file = getFile(context, appName, fileName);
		if (file != null && file.exists()) {
			file.delete();
		}
	}

	/**
	 * 获取sd中本程序根目录
	 * 
	 * @param appRootName SD卡中程序根目录"app"
	 */
	public static File getExternalFilesDir(String appName) {
		File dir = new File(Environment.getExternalStorageDirectory()
				+ File.separator + appName);
		if (!dir.exists())
			dir.mkdir();
		return dir;
	}

	/**
	 * 获取sd中的目标文件
	 */
	public static File getFileFromSd(Context context, String appName, String fileName) {
		if (checkSdcard()) {
			File file = getExternalFile(appName, fileName);
			if (file.exists())
				return file;
			else
				return null;
		} else
			return null;
	}
}
