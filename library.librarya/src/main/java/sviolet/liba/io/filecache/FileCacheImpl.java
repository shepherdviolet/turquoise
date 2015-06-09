package sviolet.liba.io.filecache;

import java.io.File;
import java.io.IOException;

/**
 * 缓存器实现类
 * 
 * @author S.Violet
 *
 * @param <T> 缓存对象类
 * @param <P> 缓存参数类
 */
public abstract class FileCacheImpl implements FileCache{

	@Override
	public void preload(String url, String fileSignature) {
		if(fileSignature == null)
			return;
		
		File file = seekFile(fileSignature);
		if(file == null){
			onLoadFromNet(url, fileSignature);
		}
	}

	@Override
	public void preload(String url, String folderSignature, String fileSignature) {
		if(folderSignature == null)
			preload(url, fileSignature);
		
		File file = seekFile(folderSignature, fileSignature);
		if(file == null){
			onLoadFromNet(url, folderSignature, fileSignature);
		}
	}

	@Override
	public void asyncPreload(final String url, final String fileSignature) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				preload(url, fileSignature);
			}
		}).start();
	}
	
	@Override
	public void asyncPreload(final String url, final String folderSignature, final String fileSignature) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				preload(url, folderSignature, fileSignature);
			}
		}).start();
	}

	@Override
	public File load(String url, String fileSignature) {
		if(fileSignature == null)
			return null;
		
		File file = seekFile(fileSignature);
		if(file == null){
			file = onLoadFromNet(url, fileSignature);
		}
		return file;
	}

	@Override
	public File load(String url, String folderSignature, String fileSignature) {
		if(folderSignature == null)
			load(url, fileSignature);
			
		File file = seekFile(folderSignature, fileSignature);
		if(file == null){
			file = onLoadFromNet(url, folderSignature, fileSignature);
		}
		return file;
	}

	@Override
	public void asyncLoad(final String url, final String fileSignature, final Result result) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				File file = load(url, fileSignature);
				result.onResult(file);
			}
		}).start();
	}

	@Override
	public void asyncLoad(final String url, final String folderSignature, final String fileSignature, final Result result) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				File file = load(url, folderSignature, fileSignature);
				result.onResult(file);
			}
		}).start();
	}
	
	@Override
	public File createFile(String fileSignature) throws IOException {
		File root = new File(getRootPath());
		if(!root.exists())
			root.mkdirs();
		File path = new File(getRootPath() + File.separator + fileSignature);
		if(!path.exists())
			path.createNewFile();
		return path;
	}

	@Override
	public File createFile(String folderSignature, String fileSignature) throws IOException {
		File folder = new File(getRootPath() + File.separator + folderSignature);
		if(!folder.exists())
			folder.mkdirs();
		File path = new File(getRootPath() + File.separator  + folderSignature + File.separator + fileSignature);
		if(!path.exists())
			path.createNewFile();
		return path;
	}

	@Override
	public File seekFile(String fileSignature) {
		File file = new File(getRootPath() + File.separator + fileSignature);
		if(file.exists()){
			String _fileSignature = getFileSignature(file);
			if(_fileSignature != null){
				if(_fileSignature.equals(fileSignature)){
					return file;
				}
			}else{
				onInnerError("[FileCache]getFileSignatur/getFolderSignatur方法返回值为null");
			}
		}
		return null;
	}

	@Override
	public File seekFile(String folderSignature, String fileSignature) {
		File file = new File(getRootPath() + File.separator  + folderSignature + File.separator + fileSignature);
		if(file.exists()){
			String _folderSignature = getFolderSignature(file);
			String _fileSignature = getFileSignature(file);
			if(_folderSignature != null && _fileSignature != null){
				if(_folderSignature.equals(folderSignature) && _fileSignature.equals(fileSignature)){
					return file;
				}
			}else{
				onInnerError("[FileCache]getFileSignatur/getFolderSignatur方法返回值为null");
			}
		}
		return null;
	}
	
}
