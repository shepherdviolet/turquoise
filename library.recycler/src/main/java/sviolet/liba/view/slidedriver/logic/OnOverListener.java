package sviolet.liba.view.slidedriver.logic;

public interface OnOverListener {
	/**
	 * 当移动越界时回调
	 * 
	 * 返回true时: SlideDriver继续处理事件
	 * 返回false时: SlideDriver抛弃事件,交由下层处理
	 * 
	 * Tips:当需要实现整体翻页到底时,能翻动其中的子控件,安装该监听器,返回false,
	 *      在越界发生时,事件即可穿透SlideDriver,分发给下层控件
	 * 
	 * @return 
	 */
	public boolean onOver(int overDirection);
}
