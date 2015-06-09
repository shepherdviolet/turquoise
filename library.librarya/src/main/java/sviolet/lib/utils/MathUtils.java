package sviolet.lib.utils;

public class MathUtils {
	/**
	 * 根据角度计算sin值<p>
	 * Math.sin()中的参数为弧度值, 弧度值 = 角度 * PI / 180
	 * 
	 * @param angle (0~360)
	 * @return sin值(-1~1)
	 */
	public static double sin(float angle){
		return Math.sin(angle * Math.PI / 180);
	}
}
