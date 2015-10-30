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
 * 旋转引擎<<stop()关闭>>
 *
 * @author S.Violet (ZhuQinChao)
 *
 */

/*
 * 例:
 * 
	private void rotate(){
		if(rotateEngine != null){
			//第二次改变加速度
			rotateEngine.changeAcceleration(-270F, 270F);
		}else{
			//第一次恒定速度旋转
			rotateEngine = new RotateEngine(0, 1080F, 0, true);
			//设置输出器
			rotateEngine.setOutputer(new RotateOutputer() {
				@Override
				public void onStop() {
					//停止回调
				}
				@Override
				public void onRefresh(float angle, float angleCounter, float angleStep) {
					image.rotate(angle);//设置图像角度
				}
			});
			//设置输入器(ClockInputer对象创建后就以25ms的刷新间隔调用engine的input(time))
			rotateEngine.setInputer(new ClockInputer(25));
		}
	}
 */

public class RotateEngine extends Engine {

	//参数
	private float acceleration;//加速度(+:顺时针,-:逆时针)
	private boolean willStop = false;//速度为0时是否停止

	//输出
	private float angle = 0;//当前角度(0-360F)
	private float angleAmount = 0;//累计角度
	private float angleStep = 0;//步进(度)
	private float speed;//当前速度

	//标志
	private boolean isStop = false;//临时停止标志

	///////////////////////////////////////////////////////////////////
	//                  输入输出
	///////////////////////////////////////////////////////////////////

	//输入处理
	@Override
	protected void onInput(long time) {
		calculate(time);
	}

	//处理输出
	@Override
	public void onOutput() {
		if(mOutputer != null){
			mOutputer.onRefresh(angle, angleAmount, angleStep);
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
		this.angleStep = step;
		this.angleAmount = this.angleAmount + step;
		this.angle = this.angleAmount % 360;
		output();
	}

	//处理触摸释放(加速度)
	@Override
	public void onHandleRelease(float acceleration) {
		this.acceleration = acceleration;
	}

	///////////////////////////////////////////////////////////////////

	/**
	 * 初始角度为0
	 *
	 * @param initSpeed 初始速度
	 * @param acceleration 加速度
	 * @param willStop 速度为0时是否停止
	 */
	public RotateEngine(float initSpeed, float acceleration, boolean willStop){
		init(initSpeed, acceleration, willStop);
	}

	/**
	 * @param initAngle 初始角度
	 * @param initSpeed 初始速度
	 * @param acceleration 加速度
	 * @param willStop 速度为0时是否停止
	 */
	public RotateEngine(float initAngle, float initSpeed, float acceleration, boolean willStop){
		init(initAngle, initSpeed, acceleration, willStop);
	}

	/**
	 * 初始化(初始角度为0)
	 *
	 * @param initSpeed 初始速度
	 * @param acceleration 加速度
	 * @param willStop 速度为0时是否停止
	 */
	public void init(float initSpeed, float acceleration, boolean willStop){
		init(0, initSpeed, acceleration, willStop);
	}

	/**
	 * 初始化
	 *
	 * @param initAngle 初始角度
	 * @param initSpeed 初始速度
	 * @param acceleration 加速度
	 * @param willStop 速度为0时是否停止
	 */
	public void init(float initAngle, float initSpeed, float acceleration, boolean willStop){
		this.angle = initAngle;
		this.angleAmount = initAngle;
		this.speed = initSpeed;
		this.acceleration = acceleration;
		this.willStop = willStop;

		this.isStop = false;
	}

	/**
	 * 运动时改变加速度
	 *
	 * @param acceleration 重设加速度
	 */
	public void changeAcceleration(float acceleration){
		this.acceleration = acceleration;
		this.isStop = false;
	}

	/**
	 * 运动时改变加速度(并重设起始角度)
	 *
	 * @param acceleration 重设加速度
	 * @param angle 重设起始角度
	 */
	public void changeAcceleration(float acceleration, float angle){
		this.acceleration = acceleration;
		this.angle = angle % 360;
		this.angleAmount = angle;
		this.isStop = false;
	}

	/**
	 * 获取当前角度(0-360)
	 *
	 * @return
	 */
	public float getAngle(){
		return angle;
	}

	/**
	 * 获取累计角度
	 *
	 * @return
	 */
	public float getAngleCounter(){
		return angleAmount;
	}

	/**
	 * 获取最后一次的步进角度
	 *
	 * @return
	 */
	public float getAngleStep(){
		return angleStep;
	}

	/**
	 * 计算
	 *
	 * @param time
	 */
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

		float _angleAmount = angleAmount +
				speed * _passtime + (acceleration * _passtime * _passtime) / 2;

		angleStep = _angleAmount - angleAmount;
		angleAmount = _angleAmount;
		angle = _angleAmount % 360;
		speed = _speed;

		output();//通知主线程输出显示
	}
}
