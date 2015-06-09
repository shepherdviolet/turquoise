package sviolet.demoa;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public abstract class ListeningService extends Service {

	public static final String ACTION_PULSE = "sviolet.action.pulse";
	public static final int ALARM_TYPE = AlarmManager.RTC_WAKEUP;
	public static final long intervalMillis = 1000;
	
	PulseReceiver pulseReceiver;
	
	AlarmManager alarmManager;
	PendingIntent pulseReceiverIntent;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		pulseReceiver = new PulseReceiver();
		registerReceiver(pulseReceiver, new IntentFilter(ACTION_PULSE));
		
		alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		pulseReceiverIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_PULSE), 0);
		alarmManager.setRepeating(ALARM_TYPE, System.currentTimeMillis(), intervalMillis, pulseReceiverIntent);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(pulseReceiver);
		alarmManager.cancel(pulseReceiverIntent);
	}
	
	/*
	 * 必须在5s内执行完毕
	 * 耗时操作需要新开线程，必要时持有wake_lock
	 */
	public abstract void task();
	
	private class PulseReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			task();
		}
		
	}
}
