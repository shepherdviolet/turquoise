package sviolet.liba.view.slidedriver.logic;

public interface OnFlipStopListener {
	/**
	 * 当弹射停止时(弹射至目标/速度为0)
	 * 
	 * @param distanceX
	 * @param distanceY
	 */
	public void OnFlipStop(float distanceX, float distanceY);
}
