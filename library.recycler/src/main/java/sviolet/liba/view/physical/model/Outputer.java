package sviolet.liba.view.physical.model;

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
