package sviolet.liba.view.slidedriver.logic;

public interface OnShortClickListener {
	/**
	 * 当点击时回调
	 * 
	 * @param endX 当前触点坐标(原始数据)
	 * @param endY 当前触点坐标(原始数据)
	 * 
	 * @return 返回true截获事件
	 */
	public boolean onClick(float x,float y);
}
