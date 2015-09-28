package sviolet.liba.view.slidedriver.logic;

public interface OnPageChangedListener {
	/**
	 * 翻页事件
	 * 
	 * @param axis 轴(X/Y)
	 * @param page 当前页码
	 */
	public void onPageChanged(int axis, int page);
}
