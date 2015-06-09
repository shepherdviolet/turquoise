package sviolet.turquoise.app;

/**
 * 注入异常[运行时]<br>
 * <br>
 * VActivity等<br>
 * 
 * @author S.Violet
 *
 */

public class InjectException extends RuntimeException {

	private static final long serialVersionUID = -9110882525371099713L;

	public InjectException(String msg){
		super(msg);
	}
	
	public InjectException(String msg, Throwable throwable){
		super(msg, throwable);
	}
	
}
