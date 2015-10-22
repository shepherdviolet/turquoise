package sviolet.turquoise.utils;

/**
 * 日志打印器<br />
 *
 * Created by S.Violet on 2015/6/12.
 */
public abstract class Logger {

    /**
     * @param tag 标签
     * @param debugEnabled 允许debug日志
     * @param infoEnabled 允许info日志
     * @param errorEnabled 允许error日志
     */
    public static Logger newInstance(String tag, boolean debugEnabled, boolean infoEnabled, boolean errorEnabled){
        return new LoggerImpl(tag, debugEnabled, infoEnabled, errorEnabled);
    }

    /**
     * @param msg debug日志
     */
    public abstract void d(String msg);

    /**
     * @param msg info日志
     */
    public abstract void i(String msg);

    /**
     * @param msg error日志
     */
    public abstract void e(String msg);

    /**
     * @param msg error日志
     * @param t 异常
     */
    public abstract void e(String msg, Throwable t);

    /**
     * @param t 异常
     */
    public abstract void e(Throwable t);

}
