package sviolet.liba.view.slidedriver.logic;

public interface OnMoveListener {
	/**
	 * 当移动时回调
	 * 
	 * 利用endX/Y可使View跟随触点移动
	 * 利用distanceX/Y控制View移动,可实现翻页和弹射
	 * 
	 * distance 0 -> -
	 * 
	 * @param endX 当前触点坐标(原始数据)
	 * @param endY 当前触点坐标(原始数据)
	 * @param distanceX X方向位移(输出数据)
	 * @param distanceY Y方向位移(输出数据)
	 * 
	 * @return 返回true截获事件
	 */
	public boolean onMove(float endX,float endY,float distanceX,float distanceY);
}
