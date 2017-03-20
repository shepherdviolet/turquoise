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

package sviolet.turquoise.enhance.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.util.Log;

import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import sviolet.turquoise.common.statics.StringConstants;
import sviolet.turquoise.util.conversion.StringUtils;
import sviolet.turquoise.util.droid.ApplicationUtils;

/**
 * [组件扩展]开启MultiDex的Application<br>
 * <p>
 * Created by S.Violet on 2015/6/12.
 */
public abstract class TApplicationForMultiDex extends TApplication {

    @Override
    protected boolean handleMultiDexLoading(Context base) {
        super.handleMultiDexLoading(base);
        Log.i("TurquoiseMultiDex", "handle multi dex loading start");
        if (isMiniProcess()) {
            Log.i("TurquoiseMultiDex", "mini process: skip handle");
            return true;//当前进程为MultiDex加载进程
        }
        //API21+的系统在安装时就对dex进行了oat优化, 无需进行处理
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.i("TurquoiseMultiDex", "current is API21-");
            if (needLoadingLayer(base)) {
                Log.i("TurquoiseMultiDex", "launch loading activity to dex opt");
                waitForDexopt(base);
            }
            //加载MultiDex
            Log.i("TurquoiseMultiDex", "load multi dex");
            MultiDex.install(this);
            Log.i("TurquoiseMultiDex", "load multi dex finished");
        } else {
            Log.i("TurquoiseMultiDex", "current is API21+, skip install");
        }
        return false;//当前为APP进程
    }

    /**
     * 根据进程名中的:mini判断当前进程是否为加载进程
     */
    private boolean isMiniProcess() {
        return StringUtils.contains(getCurrentProcessName(this), ":mini");
    }

    /**
     * @return true: 需要启动加载进程加载dex
     */
    private boolean needLoadingLayer(Context context) {
        String sha1 = get2thDexSHA1(context);
        String version = ApplicationUtils.getAppVersionName(context);
        Log.i("TurquoiseMultiDex", "dex2 sha1:" + sha1);
        SharedPreferences sp = context.getSharedPreferences(StringConstants.MULTI_DEX_PREF_NAME, MODE_MULTI_PROCESS);
        String recentSha1 = sp.getString(StringConstants.MULTI_DEX_PREF_SHA1_KEY, "");
        String recentVersion = sp.getString(StringConstants.MULTI_DEX_PREF_VERSION_KEY, "");
        return !recentSha1.equals(sha1) || !recentVersion.equals(version);
    }

    /**
     * Get classes.dex file signature
     */
    static String get2thDexSHA1(Context context) {
        ApplicationInfo appInfo = context.getApplicationInfo();
        String sourceDir = appInfo.sourceDir;
        try {
            JarFile jarFile = new JarFile(sourceDir);
            Manifest manifest = jarFile.getManifest();
            Map<String, Attributes> map = manifest.getEntries();
            Attributes dex2Attrs = map.get("classes2.dex");
            return dex2Attrs.getValue("SHA1-Digest");
        } catch (Exception e) {
            Log.e("TurquoiseMultiDex", "get dex2 sha1 failed", e);
        }
        return null;
    }

    private String getCurrentProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        } catch (Exception e) {
            Log.e("TurquoiseMultiDex", "get current process name failed", e);
        }
        return null;
    }

    private void waitForDexopt(Context base) {
        Class<? extends Activity> className = getMultiDexLoadingActivityClass();
        if (className == null){
            throw new IllegalArgumentException("[TurquoiseMultiDex]you must override TApplicationForMultiDex#getMultiDexLoadingActivityClass, and return multi dex loading Activity class");
        }
        //启动Activity
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(getPackageName(), className.getName());
        intent.setComponent(componentName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        base.startActivity(intent);
        //等待加载完成
        long startTime = System.currentTimeMillis();
        long timeout = 10 * 1000;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            //实测发现某些场景下有些2.3版本有可能10s都不能完成optdex
            timeout = 20 * 1000;
        }
        while (needLoadingLayer(base)) {
            try {
                long elapse = System.currentTimeMillis() - startTime;
                Log.i("TurquoiseMultiDex", "waiting for opt, elapse:" + elapse);
                if (elapse >= timeout) {
                    Log.e("TurquoiseMultiDex", "waiting for opt, time out!!!");
                    return;
                }
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * @return 返回指定的MultiDexLoadingActivity的类
     */
    protected abstract Class<? extends Activity> getMultiDexLoadingActivityClass();

}
