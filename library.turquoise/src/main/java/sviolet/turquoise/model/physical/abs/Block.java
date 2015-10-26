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
 */

package sviolet.turquoise.model.physical.abs;

/**
 * 障碍区块
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

public abstract class Block {
	
	private float range_min;//作用范围下限
	private float range_max;//作用范围上限
	
	/**
	 * 是否在作用范围内
	 * 
	 * @param valueAmount
	 * @return
	 */
	public boolean isInRange(int valueAmount){
		if(valueAmount >= range_min && valueAmount <= range_max)
			return true;
		else
			return false;
	}
	
	//加速度变化效果(计算前)
	public abstract float effectB_acceleration(float acceleration);
	
	//累计值变化效果(计算后)
	public abstract float effectA_valueAmount(float valueAmount);
	
	//速度变化效果(计算后)
	public abstract float effectA_speed(float speed);
}
