package sviolet.liba.io.filecache;

import java.io.File;
import java.io.IOException;

/**
 * 文件缓存器(接口)
 * 
 * @author S.Violet
 *
 */

public interface FileCache  {
	
	/**
	 * 预加载(无返回)<br>
	 * 若缓存不存在则下载
	 * @param url
	 * @param fileSignature
	 */
	public void preload(String url, String fileSignature);
	
	/**
	 * 预加载(无返回)<br>
	 * 若缓存不存在则下载
	 * @param url
	 * @param folderSignature
	 * @param fileSignature
	 */
	public void preload(String url, String folderSignature, String fileSignature);
	
	/**
	 * 预加载(异步,无返回)<br>
	 * 若缓存不存在则下载
	 * @param url
	 * @param fileSignature
	 */
	public void asyncPreload(String url, String fileSignature);
	
	/**
	 * 预加载(异步,无返回)<br>
	 * 若缓存不存在则下载
	 * @param url
	 * @param folderSignature
	 * @param fileSignature
	 */
	public void asyncPreload(String url, String folderSignature, String fileSignature);
	
	/**
	 * 读取缓存
	 * @param url
	 * @param fileSignature 文件特征码
	 * @return
	 */
	public File load(String url, String fileSignature);
	
	/**
	 * 读取缓存
	 * @param url
	 * @param folderSignature 目录特征码
	 * @param fileSignature 文件特征码
	 * @return
	 */
	public File load(String url, String folderSignature, String fileSignature);
	
	/**
	 * 读取缓存(异步)
	 * @param url
	 * @param fileSignature 文件特征码
	 * @return
	 */
	public void asyncLoad(String url, String fileSignature, Result result);
	
	/**
	 * 读取缓存(异步)
	 * @param url
	 * @param folderSignature 目录特征码
	 * @param fileSignature 文件特征码
	 * @return
	 */
	public void asyncLoad(String url, String folderSignature, String fileSignature, Result result);
	
	/**
	 * 查找缓存文件(并校验摘要)
	 * 
	 * @param fileSignature
	 * @param params
	 * @return 返回空则未找到缓存
	 */
	public File seekFile(String fileSignature);
	
	/**
	 *  查找缓存文件(并校验摘要)
	 * 
	 * @param folderSignature
	 * @param fileSignature
	 * @param params
	 * @return 返回空则未找到缓存
	 */
	public File seekFile(String folderSignature, String fileSignature);
	
	/**
	 * 根据摘要创建空的缓存文件
	 * @param fileSignature
	 * @return
	 */
	public File createFile(String fileSignature) throws IOException;
	
	/**
	 * 根据摘要创建空的缓存文件
	 * @param folderSignature
	 * @param fileSignature
	 * @return
	 */
	public File createFile(String folderSignature, String fileSignature) throws IOException;
	
	/*******************************************************************************
	 * 					使用时实现
	 *******************************************************************************/
	
	/**
	 * [使用时复写]<br>
	 * 实现返回缓存根目录
	 * 
	 * @return
	 */
	public String getRootPath();
	
	/**
	 * [使用时复写]<br>
	 * 计算文件特征码, 即为缓存文件的文件名<br>
	 * 实现摘要算法
	 * 
	 * @return
	 */
	public String getFileSignature(File file);
	
	/**
	 * [使用时复写]<br>
	 * 计算目录特征码, 即为缓存文件的上层目录名<br>
	 * 返回null则无上层目录(缓存存放在根目录)<br>
	 * 实现摘要算法2
	 * 
	 * @return
	 */
	public String getFolderSignature(File file);
	
	/**
	 * [使用时复写]<br>
	 * 当本地缓存不存在时, 网络下载数据并储存<br>
	 * 实现网络下载, 本地储存, 最后返回File
	 * 
	 * @param url
	 * @param fileSignature
	 * @return
	 */
	public File onLoadFromNet(String url, String fileSignature);
	
	/**
	 * [使用时复写]<br>
	 * 当本地缓存不存在时, 网络下载数据并储存<br>
	 * 实现网络下载, 本地储存, 最后返回File
	 * 
	 * @param url
	 * @param folderSignature
	 * @param fileSignature
	 * @return
	 */
	public File onLoadFromNet(String url, String folderSignature, String fileSignature);
	
	/**
	 * 	[使用时复写]<br>
	 * 处理开发错误性内部异常
	 * 
	 * @param errorMsg
	 */
	public void onInnerError(String errorMsg);
	
	/**
	 * [内部类]<br>
	 * 结果回调
	 * 
	 * @author S.Violet
	 *
	 */
	public interface Result {
		/**
		 * @param obj 读取结果(null失败)
		 */
		public void onResult(File file);
	}
	
}
