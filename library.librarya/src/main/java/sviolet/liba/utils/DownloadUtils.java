package sviolet.liba.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

/**
 * @author SunPeng
 */
public class DownloadUtils {

	@SuppressWarnings("deprecation")
	public static void download(Context context, String url,String appName) {

		DownloadManager downloadManager = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		// 创建下载请求
		DownloadManager.Request request = new DownloadManager.Request(
				Uri.parse(url));
		// 设置允许使用的网络类型，这里是移动网络和wifi都可以
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
				| DownloadManager.Request.NETWORK_WIFI);
		String file_name = FileUtils.getFileName(url);
		request.setTitle(file_name);
		// 设置下载后文件存放的位置
		FileUtils.getExternalFilesDir(appName).mkdirs();
		// 如果存在先删除再重新下载
		FileUtils.deleteFile(context, appName, file_name);
		// request.setDestinationInExternalPublicDir(
		// Environment.DIRECTORY_DOWNLOADS, file_name);
		request.setDestinationInExternalFilesDir(context,
				Environment.DIRECTORY_DOWNLOADS, file_name);
		// 发出通知，既后台下载
		request.setShowRunningNotification(true);
		// 显示下载界面
		request.setVisibleInDownloadsUi(true);
		// 将下载请求放入队列
		downloadManager.enqueue(request);
	}

}
