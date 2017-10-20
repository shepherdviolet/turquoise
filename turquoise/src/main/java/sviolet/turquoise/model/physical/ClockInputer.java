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

package sviolet.turquoise.model.physical;

import java.util.Timer;
import java.util.TimerTask;

import sviolet.turquoise.model.physical.abs.Inputer;

/**
 * 
 * [实时时钟输入器]按照一定间隔刷新引擎
 * 
 * @author S.Violet ()
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
		
		if(timer != null) {
            onStop();
        }
		
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
