package sviolet.turquoise.util.sys;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

/**
 * 进程管理工具
 * 
 * @author S.Violet
 *
 */

public class ProcessUtils {
	
    /** 
     * 判断当前程序是否在前台运行 
     *  
     * @return 
     */  
    public static boolean isRunningForeground(Context applicationContext) {  
               
            List<RunningAppProcessInfo> appProcesses = getRunningAppProcessInfo(applicationContext);
            if (appProcesses == null)
                    return false;
            
            String packageName = applicationContext.getPackageName();

            for (RunningAppProcessInfo appProcess : appProcesses) {
                    if (appProcess.processName.equals(packageName) && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            return true;
                    }
            }
            return false;
    }
    
    /**
     * 获取系统进程信息
     * 
     * @param applicationContext
     * @return
     */
    public static List<RunningAppProcessInfo> getRunningAppProcessInfo(Context applicationContext){
        ActivityManager activityManager = (ActivityManager) applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
        return activityManager .getRunningAppProcesses();
    }
	
}
