package sviolet.turquoise.utils.sys;

import java.io.DataOutputStream;
import java.io.File;

/**
 * 应用程序获取Root权限，设备必须已Root
 * 
 * @author S.Violet
 */

public class RootUtils {

	private final static int ROOT_STATE_UNKNOW = -1;
	private final static int ROOT_STATE_FAILED = 0;
	private final static int ROOT_STATE_SUCCESS = 1;
	private static int rootState = ROOT_STATE_UNKNOW;

	public static int isRoot() {
		if (rootState != ROOT_STATE_UNKNOW) {
			return rootState;
		}
		File file = null;
		final String searchPaths[] = { "/system/bin/", "/system/xbin/",
				"/system/sbin/", "/sbin/", "/vendor/bin/" };
		try {
			for (int i = 0; i < searchPaths.length; i++) {
				file = new File(searchPaths[i] + "su");
				if (file.exists()) {
					rootState = ROOT_STATE_SUCCESS;
					return rootState;
				}
			}
		} catch (Exception ignored) {
		}
		rootState = ROOT_STATE_FAILED;
		return rootState;
	}

	/**
	 * 提升(获取)root权限
	 * 
	 * @return root是否成功
	 */
	public static synchronized boolean upgradeRootPermission() {
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes("exit\n");
			os.flush();
			int exitValue = process.waitFor();
			return exitValue == 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				if (process != null) {
					process.destroy();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 提升(获取)root权限,并提升软件目录权限为777
	 * 
	 * @param packageCodePath getPackageCodePath()
	 * 
	 * @return root是否成功
	 */
	public static synchronized boolean upgradeRootPermission(String packageCodePath) {
	    Process process = null;
	    DataOutputStream os = null;
	    try {
	        process = Runtime.getRuntime().exec("su");
	        os = new DataOutputStream(process.getOutputStream());
	        os.writeBytes("chmod 777 " + packageCodePath + "\n");
	        os.writeBytes("exit\n");
	        os.flush();
			int exitValue = process.waitFor();
			return exitValue == 0;
	    } catch (Exception e) {
	    	e.printStackTrace();
	        return false;
	    } finally {
	        try {
	            if (os != null) {
	                os.close();
	            }
				if (process != null) {
					process.destroy();
				}
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	    }
	}

	/**
	 * 提升(获取)root权限,并提升多个目录权限为777.
	 * 
	 * 在应用访问系统目录的情况下, 还需要通过该方法提升对应系统目录的权限
	 * 
	 * @param paths 例如:new String[]{"/dev","/dev/graphics","/dev/graphics/fb0"}
	 * 
	 * @return root是否成功
	 */
	public static synchronized boolean upgradeRootPermission(String[] paths) {
	    Process process = null;
	    DataOutputStream os = null;
	    try {
	        process = Runtime.getRuntime().exec("su");
	        os = new DataOutputStream(process.getOutputStream());
	        for(int i = 0 ; i < paths.length ; i++)
	        	os.writeBytes("chmod 777 " + paths[i] + "\n");
	        os.writeBytes("exit\n");
	        os.flush();
			int exitValue = process.waitFor();
			return exitValue == 0;
	    } catch (Exception e) {
	    	e.printStackTrace();
	        return false;
	    } finally {
	        try {
	            if (os != null) {
	                os.close();
	            }
				if (process != null) {
					process.destroy();
				}
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	    }
	}
	
	/**
	 * 把应用移动至系统目录(无用废弃)
	 * 
	 * @param res 例如:"/data/app/xx.apk"
	 * @param target 例如:"/system/app/xx.apk"
	 * @return
	 */
    public static boolean moveAppToSystem(String res,String target){ 
	    Process process = null;
	    DataOutputStream os = null;
        try { 
            process = Runtime.getRuntime().exec("su"); 
            os = new DataOutputStream(process.getOutputStream()); 
            os.writeBytes("mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system\n"); 
            os.writeBytes("cat " + res +" > " + target + "\n"); 
            os.writeBytes("mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system\n"); 
            os.writeBytes("exit\n");   
            os.flush(); 
			int exitValue = process.waitFor();
			return exitValue == 0;
        } catch (Exception e) {
	    	e.printStackTrace();
	        return false;
	    } finally {
	        try {
	            if (os != null) {
	                os.close();
	            }
				if (process != null) {
					process.destroy();
				}
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	    }
    } 


}
