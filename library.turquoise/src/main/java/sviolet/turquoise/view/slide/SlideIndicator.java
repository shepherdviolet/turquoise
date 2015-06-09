package sviolet.turquoise.view.slide;

/**
 * Slide指示器
 * 
 * @author S.Violet
 */
public interface SlideIndicator{
	/**
	 * 设置当前页数(阶段)
	 */
	public void setStage(float stage);
	
	/**
	 * 设置总页数
	 */
	public void setQuantity(int quantity);
}
