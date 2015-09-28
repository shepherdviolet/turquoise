package sviolet.liba.view.slidedriver.logic;

public interface OnActionListener {
	
	/**
	 * 在有效边界内点击,ACTION_DOWN会触发该接口
	 */
	public void onDown(float x, float y);
	
	/**
	 * 有效的移动后,ACTION_MOVE会触发该接口
	 */
	public void onMove(float x, float y);
	
	/**
	 * 有效的移动或点击后,ACTION_UP会触发该接口
	 */
	public void onUp(float x, float y);
}
