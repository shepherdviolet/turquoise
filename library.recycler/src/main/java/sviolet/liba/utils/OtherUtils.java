package sviolet.liba.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

/**
 * @author 田裕杰
 */
@SuppressWarnings("deprecation")
public class OtherUtils {

	/**
	 * sendMessage: 一步发消息
	 */
	public static void sendMessage(Handler handler, int msgWhat, Bundle msgData) {
		Message msg = handler.obtainMessage(msgWhat);
		msg.setData(msgData);
		msg.sendToTarget();
	}

	/**
	 * spitList: 拆分List
	 * 
	 * @param arrayList
	 *            原List
	 * @param number
	 *            拆分基数
	 * @return 拆分生成的List的集合
	 */
	public static <T> List<List<T>> spitList(List<T> arrayList, int number) {
		List<List<T>> result = new ArrayList<List<T>>();
		int totalSize = arrayList.size();
		if (totalSize <= number) {
			result.add(arrayList);
			return result;
		}
		int muti = totalSize / number;
		if (totalSize % number == 0) {
			for (int i = 0; i < muti; i++) {
				List<T> single = new ArrayList<T>();
				single.addAll(arrayList.subList(i * number, (i + 1) * number));
				result.add(single);
			}
			return result;
		}
		if (totalSize % number != 0) {
			for (int i = 0; i <= muti; i++) {
				ArrayList<T> single = new ArrayList<T>();
				int start = i * number;
				int end = i < muti ? (i + 1) * number : totalSize;
				single.addAll(arrayList.subList(start, end));
				result.add(single);
			}
			return result;
		}
		return result;
	}

	public static void createMessage(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	@SuppressWarnings("rawtypes")
	public static String getRequestUrl(String ip, String transname,
			JSONObject jsonObject) {
		Map<String, String> map = new HashMap<String, String>();
		for (Iterator iter = jsonObject.keys(); iter.hasNext();) {
			String key = (String) iter.next();
			String value = JsonUtils.getString(jsonObject, key);
			map.put(key, value);
		}
		return getRequestUrl(ip, transname, map);
	}

	public static String getRequestUrl(String ip, String transname,
			Map<String, String> map) {
		String url = ip + transname + "?";
		url = createUrl(url, map);
		return url;
	}

	public static String createUrl(String url, Map<String, String> map) {
		Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			if (CheckisEmptyorNull(entry.getValue())) {// 校验value是否为空
				entry.setValue(" ");
			}
			url = url + entry.getKey() + "="
					+ entry.getValue().replaceAll(" ", "%20") + "&";
		}
		return url.substring(0, url.length() - 1);
	}

	public static boolean CheckisEmptyorNull(String str) {
		return null == str || 0 == str.length() || "".equals(str);
	}

	/**
	 * 获取本地assets文件夹下的数据
	 * 
	 * @param name
	 *            :本地文件全名
	 * */
	public static String getNativeData(Context context, String name) {
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
		} finally {
			try {
				if (in != null)
					in.close();
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 获取本地assets文件夹下的数据
	 * 
	 * @param name
	 *            :本地文件全名
	 * */
	public static String getNativeHtml(String name, Context context) {
		InputStream in = null;
		String res = null;
		try {
			in = context.getResources().getAssets().open(name);
			// 得到数据的大小
			int length = in.available();
			byte[] buffer = new byte[length];
			// 读取数据
			in.read(buffer);
			res = new String(buffer, "utf-8");
			// 关闭
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 初始化message 如果无数据则bundle传入null
	 * */
	public static Message getMessage(int what, Bundle bundle) {
		Message message = new Message();
		message.what = what;
		if (bundle != null) {
			message.setData(bundle);
		}
		return message;
	}

//	/**
//	 * 获取图片资源在工程中的id
//	 * */
//	@SuppressWarnings("rawtypes")
//	public static int getDrawableId(Context context, String xmlName) {
//		Class drawable = R.drawable.class;
//		Field field = null;
//		try {
//			field = drawable.getField(xmlName);
//			int resource_id = field.getInt(field.getName());
//			return resource_id;
//		} catch (Exception e) {
//			return R.drawable.ic_launcher;
//		}
//	}

}
