/*
 * Copyright (C) 2015-2016 S.Violet
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

/**
 * 日志打印器模块(实现)<p/>
 *
 * 日志打印器实际逻辑由此接口实现<p/>
 *
 * 用法:{@link TLogger}<p/>
 *
 * Created by S.Violet on 2016/1/14.
 */
public abstract class TLoggerModule {

    /**
     * 错误日志
     * @param host 信息发送者标识(nullable)
     * @param msg 错误信息
     */
    protected abstract void e(Class host, String tag, String msg);

    /**
     * 错误日志
     * @param host 信息发送者标识(nullable)
     * @param msg 错误信息
     * @param t 异常
     */
    protected abstract void e(Class host, String tag, String msg, Throwable t);

    /**
     * 错误日志
     * @param host 信息发送者标识(nullable)
     * @param t 异常
     */
    protected abstract void e(Class host, String tag, Throwable t);

    /**
     * 警告日志
     * @param host 信息发送者标识(nullable)
     * @param msg 错误信息
     */
    protected abstract void w(Class host, String tag, String msg);

    /**
     * 警告日志
     * @param host 信息发送者标识(nullable)
     * @param msg 错误信息
     * @param t 异常
     */
    protected abstract void w(Class host, String tag, String msg, Throwable t);

    /**
     * 警告日志
     * @param host 信息发送者标识(nullable)
     * @param t 异常
     */
    protected abstract void w(Class host, String tag, Throwable t);

    /**
     * info日志
     * @param host 信息发送者标识(nullable)
     * @param msg 信息
     */
    protected abstract void i(Class host, String tag, String msg);

    /**
     * debug日志
     * @param host 信息发送者标识(nullable)
     * @param msg 信息
     */
    protected abstract void d(Class host, String tag, String msg);

}
