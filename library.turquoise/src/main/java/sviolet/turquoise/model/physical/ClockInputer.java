package sviolet.turquoise.model.physical;

import java.util.Timer;
import java.util.TimerTask;

import sviolet.turquoise.model.physical.abs.Inputer;

/**
 * 
 * [实时时钟输入器]按照一定间隔刷新引擎
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

public class ClockInputer extends Inputer {

	private long lastTime;
	
	private Timer timer;
	private TimerTask timerTask;
	
	/**
	 * (创建对象时启动输入)
	 * 
	 * @param interval 刷新间隔
	 */
	public ClockInputer(long interval) {
		start(interval);
	}
	
	/**
	 * 重启输入
	 * 
	 * @param interval 刷新间隔
	 */
	public void start(long interval){
		
		if(timer != null)
			onStop();
		
		lastTime = System.currentTimeMillis();
		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				long thisTime = System.currentTimeMillis();
				input(thisTime - lastTime);
				lastTime = thisTime;
			}
		};
		timer.schedule(timerTask, 1, interval);
	}
	
	/**
	 * 停止输入
	 */
	@Override
	public void onStop() {
		if(timerTask != null){
			timerTask.cancel();
			timerTask = null;
		}
		if(timer != null){
			timer.cancel();
			timer = null;
		}
	}

}
