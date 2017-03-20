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
 * <p>[组件扩展]开启MultiDex的Application</p>
 *
 * <p>本解决方案兼容低版本安卓系统, 采用独立进程的dex加载界面(API21以下时显示, 仅在APP安装或升级时显示一次)</p>
 *
 * <p>使用说明:</p>
 *
 * <p>*********************************************************************************</p>
 *
 * <pre>{@code
 *  //配置module的build.gradle
 *  //开启multiDexEnabled
 *  //增加dexOptions, 指定每个dex的方法数为40000
 *  android {
 *      ...
 *      defaultConfig {
 *          ...
 *          multiDexEnabled true
 *      }
 *      dexOptions {
 *          javaMaxHeapSize "4g"
 *          preDexLibraries = false
 *          additionalParameters = ['--multi-dex', '--set-max-idx-number=40000']
 *      }
 *  }
 *  dependencies {
 *      ...
 *      compile project(':turquoise')
 *      compile project(':turquoise.multidex')
 *  }
 * }</pre>
 *
 * <p>*********************************************************************************</p>
 *
 * <pre>{@code
 *  //编写一个加载界面, 该界面用于API21以下的系统, 在APP安装或更新后的第一次打开显示
 *  //继承MultiDexLoadingActivity
 *  public class MyMultiDexLoadingActivity extends MultiDexLoadingActivity {
 *      @Override
 *      protected int getLayoutId() {
 *          //指定页面布局
 *          return R.layout.multi_dex_loading;
 *      }
 *  }
 * }</pre>
 *
 * <p>*********************************************************************************</p>
 *
 * <pre>{@code
 *  //继承TApplicationForMultiDex实现Application
 *  //配置
 *  @ApplicationSettings(
 *      DEBUG = BuildConfig._DEBUG //Debug模式, 装载DebugSetting配置
 *  )
 *  //发布配置
 *  @ReleaseSettings(
 *      enableStrictMode = false,
 *      enableCrashRestart = true,
 *      enableCrashHandle = true,
 *      logDefaultTag = Constants.TAG,
 *      logGlobalLevel = TLogger.ERROR | TLogger.INFO | TLogger.WARNING | TLogger.DEBUG
 *  )
 *  //调试配置
 *  @DebugSettings(
 *      enableStrictMode = true,
 *      enableCrashRestart = false,
 *      enableCrashHandle = true,
 *      logDefaultTag = Constants.TAG,
 *      logGlobalLevel = TLogger.ERROR
 *  )
 *  public class MyApplication extends TApplicationForMultiDex {
 *
 *      @Override
 *      public void onUncaughtException(Throwable ex, boolean isCrashRestart) {
 *          //异常处理
 *      }
 *
 *      @Override
 *      protected Class<? extends Activity> getMultiDexLoadingActivityClass() {
 *          //指定加载页面的类
 *          return MyMultiDexLoadingActivity.class;
 *      }
 *
 *  }
 *
 * }</pre>
 *
 * <p>*********************************************************************************</p>
 *
 * <pre>{@code
 *  //AndroidManifest.xml中配置自定义Application
 *  //配置加载界面, 注意是:mini进程, singleTask启动模式
 *  <application
 *      android:name=".MyApplication"
 *      ...
 *      <activity
 *          android:name=".MyMultiDexLoadingActivity"
 *          android:process=":mini"
 *          android:launchMode="singleTask"
 *          android:alwaysRetainTaskState="false"
 *          android:excludeFromRecents="true"
 *          android:screenOrientation="portrait" />
 * }</pre>
 *
 * <p>*********************************************************************************</p>
 *
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
            } else {
                Log.i("TurquoiseMultiDex", "dex has already opted");
            }
            //加载MultiDex
            Log.i("TurquoiseMultiDex", "load odex");
            MultiDex.install(this);
            Log.i("TurquoiseMultiDex", "load odex finished");
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
