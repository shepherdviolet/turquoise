package sviolet.turquoise.app;

/**
 * [异常]弃用异常<Br/>
 * 当调用一个被弃用的方法或类时,抛出该异常
 *
 * Created by S.Violet on 2015/6/30.
 */
public class DeprecatedException extends RuntimeException {

    public DeprecatedException(String msg){
        super(msg);
    }

    public DeprecatedException(String msg, Throwable throwable){
        super(msg, throwable);
    }

}
