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

import sviolet.turquoise.model.physical.abs.Engine;

/**
 * 
 * 直线引擎<<stop()关闭>>
 * 
 * @author S.Violet ()
 *
 */

public class LineEngine extends Engine {
	
	//参数
	private float acceleration;//加速度
	private boolean willStop = false;//速度为0时是否停止
	
	//输出
	private float position;//当前位置
	private float step;//步进距离
	private float speed;//当前速度
	
	//标志
	private boolean isStop = false;

	///////////////////////////////////////////////////////////////////
	//                  输入输出
	///////////////////////////////////////////////////////////////////
	
	//输入调用
	@Override
	public void onInput(long time) {
		calculate(time);
	}

	//处理输出
	@Override
	public void onOutput() {
		if(mOutputer != null){
			mOutputer.onRefresh(position, position, step);
		}
		if(isStop){
			stop();
		}
		isStop = false;//清除stop标志
	}
	
	//处理停止
	@Override
	public void onStop() {
		super.onStop();
	}
	
	//处理触摸滑动
	@Override
	public void onHandleMove(float step) {
		this.step = step;
		this.position = this.position + step;
		output();
	}
	
	//处理触摸释放(加速度)
	@Override
	public void onHandleRelease(float acceleration) {
		this.acceleration = acceleration;
	}
	
	///////////////////////////////////////////////////////////////////
	
	public LineEngine(float initSpeed, float acceleration, boolean willStop){
		init(0, initSpeed, acceleration, willStop);
	}
	
	public LineEngine(float initPosition, float initSpeed, float acceleration, boolean willStop){
		init(initPosition, initSpeed, acceleration, willStop);
	}
	
	public void init(float initPosition, float initSpeed, float acceleration, boolean willStop){
		this.position = initPosition;
		this.speed = initSpeed;
		this.acceleration = acceleration;
		this.willStop = willStop;
		
		this.isStop = false;
	}
	
	public void changeAcceleration(float acceleration){
		this.acceleration = acceleration;
		
		this.isStop = false;
	}
	
	private void calculate(long time){
		
		float _passtime = ((float)time) / 1000;
		
		float _speed = speed + _passtime * acceleration;
		
		if(willStop){
			if((_speed >= 0 && speed <= 0) || (_speed <= 0 && speed >= 0)){
				float _stopTime = Math.abs(speed) / Math.abs(acceleration);
				if(_passtime >= _stopTime){
					_passtime = _stopTime;
					_speed = 0;
					isStop = true;
				}
			}
		}
		
		float _position = position + 
				speed * _passtime + (acceleration * _passtime * _passtime) / 2;
		
		step = _position - position;
		position = _position;
		speed = _speed;
		
		output();
	}
}
