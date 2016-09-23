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

package sviolet.turquoise.util.common;

public class MathUtils {
	/**
	 * <p>
	 *	根据角度计算sin值 <br/>
	 *	Math.sin()中的参数为弧度值, 弧度值 = 角度 * PI / 180 <br/>
	 * </p>
	 * 
	 * @param angle [0, 360]
	 * @return sin值 [-1, 1]
	 */
	public static double sinAngle(double angle){
		return Math.sin(angle * Math.PI / 180);
	}

	/**
	 * <p>
	 *	根据sin值计算角度 <br/>
	 *	Math.asin()返回值为弧度值, 弧度值 = 角度 * PI / 180 <br/>
	 * </p>
	 *
	 * @param sin sin值 [-1, 1]
	 * @return 角度 [0, 360]
     */
	public static double asinAngle(double sin){
		return Math.asin(sin) / (Math.PI / 180);
	}

	/**
	 * <p>
	 *	根据角度计算tan值 <br/>
	 *	Math.tan()中的参数为弧度值, 弧度值 = 角度 * PI / 180 <br/>
	 * </p>
	 *
	 * @param angle [0, 360]
	 * @return tan值
     */
	public static double tanAngle(double angle){
		return Math.tan(angle * Math.PI / 180);
	}

	/**
	 * <p>
	 *	根据tan值计算角度 <br/>
	 *	Math.atan()返回值为弧度值, 弧度值 = 角度 * PI / 180 <br/>
	 * </p>
	 *
	 * @param tan tan值
	 * @return 角度 [0, 360]
     */
	public static double atanAngle(double tan){
		return Math.atan(tan) / (Math.PI / 180);
	}

}
