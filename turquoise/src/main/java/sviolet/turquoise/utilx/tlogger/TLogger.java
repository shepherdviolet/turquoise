/*
 * Copyright (C) 2015-2017 S.Violet
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

package sviolet.turquoise.utilx.tlogger;

import java.util.Map;

/**
 * KTLogger java interface
 *
 * Created by S.Violet on 2017/5/23.
 */
public abstract class TLogger {

    public static final int ALL = 0xffffffff;//打印所有日志级别
    public static final int NULL = 0x00000000;//不打印日志
    public static final int ERROR = 0x00000001;//打印ERROR日志
    public static final int WARNING = 0x00000010;//打印WARNING日志
    public static final int INFO = 0x00000100;//打印INFO日志
    public static final int DEBUG = 0x00001000;//打印DEBUG日志

    /**
     * 获得日志打印器实例
     * @param host 信息发送者标识, 通常为打印日志的当前类
     * @return 日志打印器(代理)
     */
    public static TLogger get(Object host){
        Class hostClass = null;
        if (host != null)
            hostClass = host.getClass();
        return get(hostClass);
    }

    /**
     * 获得日志打印器实例
     * @param host 信息发送者标识, 通常为打印日志的当前类
     * @return 日志打印器(代理)
     */
    public static TLogger get(Class<Object> host){
        if (host == null){
            return new TLoggerProxy(null, NULL);
        }
        return new TLoggerProxy(host, TLoggerCenter.Companion.check(host));
    }

    public static void setGlobalLevel(int level){
        TLoggerCenter.Companion.setGlobalLevel(level);
    }

    public static void addRules(Map<String, Integer> rules){
        TLoggerCenter.Companion.addRules(rules);
    }

    public static void resetRules(Map<String, Integer> rules){
        TLoggerCenter.Companion.resetRules(rules);
    }

    /**
     * 错误日志<p/>
     *
     * 用于严重的错误.
     *
     * @param msg 错误信息
     */
    public abstract void e(String msg);

    /**
     * 错误日志<p/>
     *
     * 用于严重的错误.
     *
     * @param msg 错误信息
     * @param t 异常
     */
    public abstract void e(String msg, Throwable t);

    /**
     * 错误日志<p/>
     *
     * 用于严重的错误.
     *
     * @param t 异常
     */
    public abstract void e(Throwable t);

    /**
     * 警告日志<p/>
     *
     * 用于错误, 但不至于崩溃或影响使用. 通常在生产环境关闭.
     *
     * @param msg 错误信息
     */
    public abstract void w(String msg);

    /**
     * 警告日志<p/>
     *
     * 用于错误, 但不至于崩溃或影响使用. 通常在生产环境关闭.
     *
     * @param msg 错误信息
     * @param t 异常
     */
    public abstract void w(String msg, Throwable t);

    /**
     * 警告日志<p/>
     *
     * 用于错误, 但不至于崩溃或影响使用. 通常在生产环境关闭.
     *
     * @param t 异常
     */
    public abstract void w(Throwable t);

    /**
     * info日志<p/>
     *
     * 用于非敏感信息打印, 少量必要信息打印.
     *
     * @param msg 信息
     */
    public abstract void i(String msg);

    /**
     * debug日志<p/>
     *
     * 用于敏感信息打印, 大量调试信息打印, 通常在生产环境关闭.
     *
     * @param msg 信息
     */
    public abstract void d(String msg);

    /**
     * 检查某个日志级别是否允许打印
     * @param level 日志级别， 例如TLogger.DEBUG
     * @return true:允许打印
     */
    public abstract boolean checkEnable(int level);

}
