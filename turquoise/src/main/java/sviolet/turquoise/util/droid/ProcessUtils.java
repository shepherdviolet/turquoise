/*
 * Copyright (C) 2015-2017 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.turquoise.util.droid;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

/**
 * APP进程工具
 *
 * @author S.Violet
 */

public class ProcessUtils {

    /**
     * 判断当前程序是否在前台运行
     *
     * @param applicationContext context
     * @return true:前台运行
     */
    public static boolean isRunningForeground(Context applicationContext) {

        List<RunningAppProcessInfo> appProcesses = getRunningAppProcessInfo(applicationContext);
        if (appProcesses == null) {
            return false;
        }

        String packageName = getCurrentAppProcessName(applicationContext);

        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName) && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获得当前程序进程名(包名)
     *
     * @param applicationContext context
     * @return 进程名(包名)
     */
    public static String getCurrentAppProcessName(Context applicationContext){
        return applicationContext.getPackageName();
    }

    /**
     * 获取系统当前的进程信息
     *
     * @param applicationContext context
     * @return 进程信息
     */
    public static List<RunningAppProcessInfo> getRunningAppProcessInfo(Context applicationContext) {
        ActivityManager activityManager = (ActivityManager) applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
        return activityManager.getRunningAppProcesses();
    }

}
