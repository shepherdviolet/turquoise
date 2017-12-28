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

package sviolet.turquoise.enhance.app.utils;

/**
 * 注入异常[运行时]<br>
 * <br>
 * VActivity等注入布局或参数失败时抛出该异常<br>
 * 
 * @author S.Violet
 *
 */

public class InjectException extends RuntimeException {

	public InjectException(String msg){
		super(msg);
	}
	
	public InjectException(String msg, Throwable throwable){
		super(msg, throwable);
	}
	
}
