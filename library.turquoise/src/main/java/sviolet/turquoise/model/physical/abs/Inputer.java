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
 * 物理引擎输入器<p>
 * 
 * [[与lite的区别]]本引擎物理算法在子线程中进行, 输出时通过handler调用UI线程处理onOutput
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

public abstract class Inputer {
	
	private Engine mEngine;
	
	/**
	 * 停止输入回调
	 */
	protected abstract void onStop();
	
	/**
	 * 加入引擎
	 * 
	 * @param mEngine
	 */
	protected void setEngine(Engine mEngine){
		this.mEngine = mEngine;
	}
	
	/**
	 * 向引擎输入
	 * 
	 * @param time
	 */
	public void input(long time){
		if(mEngine != null && time > 0){
			mEngine.input(time);
		}
	}
}
