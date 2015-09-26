package sviolet.turquoise.enhance;

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
import android.util.SparseArray;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import sviolet.turquoise.enhance.annotation.setting.ApplicationSettings;
import sviolet.turquoise.enhance.annotation.setting.DebugSettings;
import sviolet.turquoise.enhance.annotation.setting.ReleaseSettings;
import sviolet.turquoise.enhance.utils.Logger;
import sviolet.turquoise.utils.sys.ApplicationUtils;

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

    private SparseArray<Activity> mSubActivityList = new SparseArray<Activity>();
    private int subActivityIntex = 0;

    private boolean crashHandleToken = true;//崩溃处理令牌
    private boolean crashHandleTokenInner = false;//崩溃处理令牌(内部)

    private Logger logger;//日志打印器

    protected int addActivity(Activity activity) {
        //编号递增
        subActivityIntex++;
        //存入Activity
        mSubActivityList.put(subActivityIntex, activity);

        //第一个Activity提示当前Debug模式
        if (mSubActivityList.size() == 1){
            if (isDebugMode()){
                getLogger().i("[TApplication]DebugMode");
                Toast.makeText(getApplicationContext(), "Debug Mode", Toast.LENGTH_SHORT).show();
            }
        }

        //返回编号
        return subActivityIntex;
    }

    protected void removeActivity(int index){
        mSubActivityList.remove(index);
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
            //日志打印权限
            logger = new Logger(getDebugSettings().logTag(),
                    getDebugSettings().enableLogDebug(),
                    getDebugSettings().enableLogInfo(),
                    getDebugSettings().enableLogError());
        }else {
            //策略检测
            if (getReleaseSettings().enableStrictMode()) {
                ApplicationUtils.enableStrictMode();
            }
            //日志打印权限
            logger = new Logger(getReleaseSettings().logTag(),
                    getReleaseSettings().enableLogDebug(),
                    getReleaseSettings().enableLogInfo(),
                    getReleaseSettings().enableLogError());
        }
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
        getLogger().e(ex);
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
     * 获得日志打印器<br/>
     * 必须配置ReleaseSettings/DebugSettings, 否则将获得一个无效的打印器
     */
    public Logger getLogger(){
        if (logger == null){
            logger = new Logger("", false, false, false);//返回无效的日志打印器
        }
        return logger;
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
            for (int i = 0 ; i < mSubActivityList.size() ; i ++){
                mSubActivityList.valueAt(i).finish();
            }
            mSubActivityList.clear();
        } catch (Exception ignored) {
        } finally {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * 把异常转为String信息
     */
    public String parseThrowableToString(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        printWriter.close();
        return writer.toString();
    }

}
