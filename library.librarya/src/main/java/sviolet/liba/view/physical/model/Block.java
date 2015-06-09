package sviolet.liba.view.physical.model;

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
