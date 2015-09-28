package sviolet.turquoise.model.physical.abs;

import android.view.MotionEvent;

/**
 * 运动把手,用于触摸驱动物体运动
 * 
 * @author S.Violet (ZhuQinChao)
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
