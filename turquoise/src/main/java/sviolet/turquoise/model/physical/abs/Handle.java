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

package sviolet.turquoise.model.physical.abs;

import android.view.MotionEvent;

/**
 * 运动把手,用于触摸驱动物体运动
 * 
 * @author S.Violet ()
 *
 */

public abstract class Handle {
	
	private boolean isReleased = true;
	
	private Engine engine;
	
	/**
	 * 物体是否被把手释放
	 * @return
	 */
	protected boolean isReleased(){
		return isReleased;
	}
	
	//////////////////////////////////////////////////////////
	
	/**
	 * 驱动事件
	 * 
	 * @param event
	 * @return
	 */
	public abstract boolean drive(MotionEvent event);
	
	/**
	 * 持有
	 */
	protected void hold(){
		isReleased = false;
	}
	
	/**
	 * 移动
	 * 
	 * @param step
	 */
	protected void move(float step){
		if(engine != null){
			engine.onHandleMove(step);
		}
	}

	/**
	 * 释放
	 */
	protected void release(float acceleration){
		isReleased = true;
		if(engine != null){
			engine.onHandleRelease(acceleration);
		}
	}
	
	//////////////////////////////////////////////////////////
	
	/**
	 * 绑定引擎
	 */
	protected void setEngine(Engine engine){
		this.engine = engine;
	}
	
}
