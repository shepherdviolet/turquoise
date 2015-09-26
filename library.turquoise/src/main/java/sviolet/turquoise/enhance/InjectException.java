package sviolet.turquoise.enhance;

/**
 * 注入异常[运行时]<br>
 * <br>
 * VActivity等注入布局或参数失败时抛出该异常<br>
 * 
 * @author S.Violet
 *
 */

public class InjectException extends RuntimeException {

	public InjectException(String msg){
		super(msg);
	}
	
	public InjectException(String msg, Throwable throwable){
		super(msg, throwable);
	}
	
}
