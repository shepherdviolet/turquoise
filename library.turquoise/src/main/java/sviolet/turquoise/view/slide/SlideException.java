package sviolet.turquoise.view.slide;

/**
 * 滑动通用异常[运行时]
 * 
 * @author S.Violet
 *
 */

public class SlideException extends RuntimeException{

	private static final long serialVersionUID = 2079948752950033425L;
	
	public SlideException(String msg){
		super(msg);
	}
	
	public SlideException(String msg, Throwable throwable){
		super(msg, throwable);
	}
	
}
