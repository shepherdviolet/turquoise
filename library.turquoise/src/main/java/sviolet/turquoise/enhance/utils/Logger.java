package sviolet.turquoise.enhance.utils;

import android.util.Log;

/**
 * 日志打印器<br />
 * 控制日志级别权限
 *
 * Created by S.Violet on 2015/6/12.
 */
public class Logger {

    private String tag;
    private boolean debugEnabled, infoEnabled, errorEnabled;

    /**
     * @param tag 标签
     * @param debugEnabled 允许debug日志
     * @param infoEnabled 允许info日志
     * @param errorEnabled 允许error日志
     */
    public Logger(String tag, boolean debugEnabled, boolean infoEnabled, boolean errorEnabled){
        this.tag = tag;
        this.debugEnabled = debugEnabled;
        this.infoEnabled = infoEnabled;
        this.errorEnabled = errorEnabled;
    }

    public void d(String msg){
        if (debugEnabled)
            if (msg == null)
                Log.d(tag, "null");
            else
                Log.d(tag, msg);
    }

    public void i(String msg){
        if (infoEnabled)
            if (msg == null)
                Log.i(tag, "null");
            else
                Log.i(tag, msg);
    }

    public void e(String msg){
        if (errorEnabled)
            if (msg == null)
                Log.e(tag, "null");
            else
                Log.e(tag, msg);
    }

    public void e(String msg, Throwable t){
        if (errorEnabled)
            if (msg == null)
                Log.e(tag, "null", t);
            else
                Log.e(tag, msg, t);
    }

    public void e(Throwable t){
        if (errorEnabled)
            if (t != null)
                t.printStackTrace();
    }

}
