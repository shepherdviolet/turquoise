/*
 * Copyright (C) 2015 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.turquoise.utils.common;

import android.util.Log;

/**
 * 日志打印器<br />
 * 含日志级别控制<br/>
 *
 * Created by S.Violet on 2015/10/22.
 */
public class LoggerImpl extends Logger {

    private String tag;
    private boolean debugEnabled, infoEnabled, errorEnabled;

    /**
     * @param tag 标签
     * @param debugEnabled 允许debug日志
     * @param infoEnabled 允许info日志
     * @param errorEnabled 允许error日志
     */
    LoggerImpl(String tag, boolean debugEnabled, boolean infoEnabled, boolean errorEnabled){
        this.tag = tag;
        this.debugEnabled = debugEnabled;
        this.infoEnabled = infoEnabled;
        this.errorEnabled = errorEnabled;
    }

    @Override
    public void d(String msg){
        if (debugEnabled)
            if (msg == null)
                Log.d(tag, "null");
            else
                Log.d(tag, msg);
    }

    @Override
    public void i(String msg){
        if (infoEnabled)
            if (msg == null)
                Log.i(tag, "null");
            else
                Log.i(tag, msg);
    }

    @Override
    public void e(String msg){
        if (errorEnabled)
            if (msg == null)
                Log.e(tag, "null");
            else
                Log.e(tag, msg);
    }

    @Override
    public void e(String msg, Throwable t){
        if (errorEnabled)
            if (msg == null)
                Log.e(tag, "null", t);
            else
                Log.e(tag, msg, t);
    }

    @Override
    public void e(Throwable t){
        if (errorEnabled)
            if (t != null)
                t.printStackTrace();
    }

}
