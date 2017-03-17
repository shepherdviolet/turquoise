/*
 * Copyright (C) 2015 S.Violet
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import sviolet.turquoise.common.statics.StringConstants;
import sviolet.turquoise.enhance.app.annotation.setting.ApplicationSettings;
import sviolet.turquoise.enhance.app.annotation.setting.DebugSettings;
import sviolet.turquoise.enhance.app.annotation.setting.ReleaseSettings;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.utilx.tlogger.TLoggerModule;
import sviolet.turquoise.util.droid.ApplicationUtils;

/**
 * [组件扩展]Application<br>
 *
 * Created by S.Violet on 2015/6/12.
 */
public abstract class TApplication extends Application  implements Thread.UncaughtExceptionHandler {

    private ApplicationSettings mApplicationSettings;
    private ReleaseSettings mReleaseSettings;
    private DebugSettings mDebugSettings;

    private static TApplication mApplication;//实例

    private final Set<Activity> mActivities = Collections.newSetFromMap(new WeakHashMap<Activity, Boolean>());

    private boolean crashHandleToken = true;//崩溃处理令牌
    private boolean crashHandleTokenInner = false;//崩溃处理令牌(内部)

    private TLogger logger = TLogger.get(this, StringConstants.LIBRARY_TAG);//日志打印器

    protected void addActivity(Activity activity) {
        //存入Activity
        synchronized (this) {
            mActivities.add(activity);
        }

        //第一个Activity提示当前Debug模式
        if (mActivities.size() == 1){
            if (isDebugMode()){
                logger.i("DebugMode");
                Toast.makeText(getApplicationContext(), "Debug Mode", Toast.LENGTH_SHORT).show();
            }
        }

    }

    protected void removeActivity(Activity activity){
        //移除Activity
        synchronized (this) {
            mActivities.remove(activity);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //APP基础Context接收入口
    }

    public void onCreate() {
        injectSettings();
        super.onCreate();
        mApplication = this;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     *  实现此方法进行未捕获异常处理(已为新线程, 并加入Looper消息队列)<br />
     *  需开启ReleaseSettings/DebugSettings.enableCrashHandle<br />
     *  应用会最长等待十秒钟用于处理异常, 当该方法执行完毕, 且未持有或已释放"异常处理令牌",
     *  应用会立即结束. 若需要另启线程处理事务, 请在新线程中, takeCrashHandleToken()持有
     *  "异常处理令牌", 并在新线程结束时releaseCrashHandleToken()释放令牌, 以保证在此期间
     *  应用不会结束进程. 所有异常处理务必在十秒内完成, 超时将强制结束.
     */
    public abstract void onUncaughtException(Throwable ex, boolean isCrashRestart);

    /**
     * 加载设置: ApplicationSettings/ReleaseSettings/DebugSettings
     */
    private void injectSettings(){
        //配置不存在的情况
        if (isDebugMode()){
            if (getDebugSettings() == null){
                return;
            }
        }else{
            if (getReleaseSettings() == null){
                return;
            }
        }

        //应用配置参数
        if (isDebugMode()){
            //策略检测
            if (getDebugSettings().enableStrictMode()){
                ApplicationUtils.enableStrictMode();
            }
            //日志打印器配置
            installTLogger(getDebugSettings().logModule(), getDebugSettings().logDefaultTag(), getDebugSettings().logGlobalLevel());
        }else {
            //策略检测
            if (getReleaseSettings().enableStrictMode()) {
                ApplicationUtils.enableStrictMode();
            }
            //日志打印器配置
            installTLogger(getReleaseSettings().logModule(), getReleaseSettings().logDefaultTag(), getReleaseSettings().logGlobalLevel());
        }
    }

    private void installTLogger(Class<?> moduleClass, String tag, int level){
        if (moduleClass != null){
            if (TLoggerModule.class.isAssignableFrom(moduleClass)){
                try {
                    TLogger.install((TLoggerModule)moduleClass.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException("[TApplication]TLogger install error", e);
                }
            }else{
                /**
                 * ReleaseSettings/DebugSettings中配置的logModule参数, 必须为实现TLoggerModule接口的类
                 */
                throw new RuntimeException("[TApplication]\"logModule\" is not assignable from TLoggerModule.class which setting in ReleaseSettings/DebugSettings");
            }
        }
        TLogger.setDefaultTag(tag);
        TLogger.setGlobalLevel(level);
    }

    /**
     * 崩溃处理<br/>
     * 新启一个线程, 并加入Looper消息队列
     */
    private void handleUncaughtException(final Throwable ex, final boolean isCrashRestart) {
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                onUncaughtException(ex, isCrashRestart);
                crashHandleTokenInner = true;//释放内部令牌, 标为执行完毕
                Looper.loop();
            }
        }.start();
    }

    /**
     * 捕获"未捕获"的异常
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        //打印错误日志
        logger.e(ex);
        //是否需要重启
        boolean isCrashRestart = isCrashRestart();

        //判断是否处理异常
        if (isDebugMode()){
            if (getDebugSettings().enableCrashHandle()){
                handleUncaughtException(ex, isCrashRestart);
            }else{
                crashHandleToken = true;
                crashHandleTokenInner = true;
            }
        }else{
            if (getReleaseSettings().enableCrashHandle()){
                handleUncaughtException(ex, isCrashRestart);
            }else{
                crashHandleToken = true;
                crashHandleTokenInner = true;
            }
        }

        //等待异常处理完成
        for (int i = 0 ; i < 20 ; i++){
            SystemClock.sleep(500);
            //内部/外部异常处理令牌都释放后, 结束等待
            if (crashHandleToken && crashHandleTokenInner){
                break;
            }
        }

        //重启
        if (isCrashRestart){
            restartApp(1000);
        }

        killApp();
    }

    /**************************************
     * private
     */

    private ApplicationSettings getApplicationSettings() {
        if (mApplicationSettings == null) {
            if (this.getClass().isAnnotationPresent(ApplicationSettings.class)) {
                mApplicationSettings = this.getClass().getAnnotation(ApplicationSettings.class);
            }
        }
        return mApplicationSettings;
    }

    private ReleaseSettings getReleaseSettings() {
        if (mReleaseSettings == null) {
            if (this.getClass().isAnnotationPresent(ReleaseSettings.class)) {
                mReleaseSettings = this.getClass().getAnnotation(ReleaseSettings.class);
            }
        }
        return mReleaseSettings;
    }

    private DebugSettings getDebugSettings() {
        if (mDebugSettings == null) {
            if (this.getClass().isAnnotationPresent(DebugSettings.class)) {
                mDebugSettings = this.getClass().getAnnotation(DebugSettings.class);
            }
        }
        return mDebugSettings;
    }

    /**
     * 重启应用程序
     *
     * @param delay 时延
     *
     */
    private void restartApp(long delay){
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, pendingIntent);
    }

    /**
     * 判断是否允许崩溃重启
     */
    @SuppressLint("CommitPrefEdits")
    private boolean isCrashRestart(){
        //判断是否允许崩溃重启
        if (isDebugMode()){
            if (!getDebugSettings().enableCrashRestart()) {
                return false;
            }
        }else {
            if (!getReleaseSettings().enableCrashRestart()) {
                return false;
            }
        }
        //防止循环重启
        SharedPreferences preferences = getSharedPreferences("TApplication", Activity.MODE_PRIVATE);
        //读取上次崩溃时间
        long lastCrashTimestamp = preferences.getLong("last_crash_timestamp", 0);
        //当前时间
        long now = System.currentTimeMillis();
        //记录本次崩溃时间(commit同步提交)
        preferences.edit().putLong("last_crash_timestamp", now).commit();
        //60秒内再次报错不重启, 防止循环重启
        return (now - lastCrashTimestamp) > (60 * 1000L);
    }

    /************************************
     * public
     */

    /**
     * 持有异常处理令牌, 十秒内程序不会结束/重启
     */
    protected void takeCrashHandleToken(){
        crashHandleToken = false;
    }

    /**
     * 释放异常处理令牌, 程序会结束/重启
     */
    protected void releaseCrashHandleToken(){
        crashHandleToken = true;
    }

    /**
     * 获得实例
     */
    public static TApplication getInstance() {
        return mApplication;
    }

    /**
     * 判断是否为调试模式(由TApplication的@ApplicationSettings注释决定)
     */
    public boolean isDebugMode(){
        if (getApplicationSettings() != null && getApplicationSettings().DEBUG()){
            return true;
        }
        return false;
    }

    /**
     * 强杀应用
     */
    public void killApp() {
        try {
            for (Activity activity : mActivities){
                activity.finish();
            }
            mActivities.clear();
        } catch (Exception ignored) {
        } finally {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

}
