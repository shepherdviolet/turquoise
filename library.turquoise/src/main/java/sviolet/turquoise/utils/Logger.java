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
 */

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
