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
 * 
 * 输出接口
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

public interface Outputer {
	
	/**
	 * 刷新UI回调
	 * 
	 * @param value 当前值
	 * @param valueAmount 累计值
	 * @param valueStep 步进值
	 */
	public void onRefresh(float value, float valueAmount, float valueStep);
	
	/**
	 * 停止运动回调
	 */
	public void onStop();
	
}
