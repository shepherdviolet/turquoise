package sviolet.turquoise.app;

/**
 * 通用RuntimeException
 * Created by S.Violet on 2015/6/30.
 */
public class CommonException extends RuntimeException {

    public CommonException(String msg){
        super(msg);
    }

    public CommonException(String msg, Throwable throwable){
        super(msg, throwable);
    }

}
