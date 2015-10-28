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

package sviolet.turquoise.view.slide;

/**
 * 滑动通用异常[运行时]
 * 
 * @author S.Violet
 *
 */

public class SlideException extends RuntimeException{

	private static final long serialVersionUID = 2079948752950033425L;
	
	public SlideException(String msg){
		super(msg);
	}
	
	public SlideException(String msg, Throwable throwable){
		super(msg, throwable);
	}
	
}
