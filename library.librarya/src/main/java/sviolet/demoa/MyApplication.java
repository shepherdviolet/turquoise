package sviolet.demoa;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.LinkedList;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.SystemClock;

/**
 * @author S.Violet
 * 
 */
public class MyApplication extends Application implements
		UncaughtExceptionHandler {

	public static String CONFIG_NAME = "config_name";//pref的名称
	public static String LATEST_CRASH_TIME = "latest_crash_time";
	
	public static String PACKAGE_NAME = "";//包名(重启用)
	public static String RESTART_CLASS_NAME = "";//类名(重启用)
	
//	private Context context;
	private static MyApplication application;
	private LinkedList<Activity> activityList = new LinkedList<Activity>();

	public static MyApplication getInstance() {
		return application;
	}

	public void addActivity(Activity activity) {
		activityList.add(activity);
	}

	public void exitApp() {
		try {
			for (Activity activity : activityList) {
				if (activity != null) {
					activity.finish();
				}
			}
		} catch (Exception e) {
		} finally {
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}

	public void onCreate() {
//		CommonUtils.enableStrictMode();
		super.onCreate();
		application = this;
//		context = getApplicationContext();
		// 设置重写的uncaughtException为程序的默认处理
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 捕获"未捕获"的异常
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		boolean isRestart = isEnableRestart(60);//判断是否需重启(60秒内不允许)
		showErrorToast(isRestart);//显示错误提示
		uploadErrorMsg(ex);//上传错误日志
		SystemClock.sleep(3000);//暂停几秒,保证错误日志上传和pref保存成功
		if(isRestart)
			restartApp(1000);//一秒后重启
		exitApp();//退出当前程序
	}

	/**
	 * 上传错误信息
	 * 
	 * @param errorMsg
	 */
	private void uploadErrorMsg(final Throwable ex) {
		if (ex != null) {
			ex.printStackTrace();
			//上传代码
//			HttpUtils.uploadANRmsg(context, DeviceUtils.getDeviceModel(),
//					DeviceUtils.getDeviceVersionName(),
//					DeviceUtils.getDeviceId(context),
//					CommonUtils.getAppVersionName(context),getErrorInfo(ex), null);
		} 
	}

	/**
	 * 显示错误提示
	 */
	private void showErrorToast(final boolean isRestart) {
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				//错误提示界面
//				View view = View.inflate(context, R.layout.crash_toast, null);
//				if(isRestart){
//					((TextView)view.findViewById(R.id.crash_toast_text)).setText(R.string.restart_toast_msg);
//				}
//				Toast toast = new Toast(context);
//				toast.setGravity(Gravity.CENTER, 0, 0);
//				toast.setDuration(Toast.LENGTH_LONG);
//				toast.setView(view);
//				toast.show();
				
				Looper.loop();
			}
		}.start();
	}

	/**
	 * 重启应用程序
	 * 
	 * @param delay 时延
	 * 
	 * @author S.Violet (zhuqinchao)
	 */
	private void restartApp(long delay){
		if(PACKAGE_NAME.equals(""))
			return;
		Intent intent = new Intent();
		intent.setClassName(PACKAGE_NAME, RESTART_CLASS_NAME);
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, pendingIntent);
	}
	
	/**
	 * 判断是否要重启(防止BUG导致无休止重启)
	 * 
	 * @param long 允许再次重启的间隔(秒)
	 * 
	 * @return true:允许重启
	 * 
	 * @author S.Violet (zhuqinchao)
	 */
	private boolean isEnableRestart(long interval){
		SharedPreferences preferences = getSharedPreferences(CONFIG_NAME,Activity.MODE_PRIVATE);
		//读取上次崩溃时间
		long latestCrashTime = preferences.getLong(LATEST_CRASH_TIME, 0);
		//当前时间
		long thisCrashTime = System.currentTimeMillis();
		//记录本次崩溃时间
		preferences.edit().putLong(LATEST_CRASH_TIME, thisCrashTime).apply();
		
		//60秒内再次报错不允许重启
		if((thisCrashTime - latestCrashTime) > (interval * 1000L))
			return true;
		return false;
	}
	
	/**
	 * 获取错误的信息
	 */
	protected String getErrorInfo(Throwable ex) {
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		ex.printStackTrace(pw);
		pw.close();
		String error = writer.toString();
		return error;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
	}

}