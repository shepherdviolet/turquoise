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

package sviolet.turquoise.utilx.tlogger.def;

import android.util.Log;

import sviolet.turquoise.utilx.tlogger.TLoggerModule;

/**
 * 简易日志打印器模块实现
 *
 * Created by S.Violet on 2016/1/14.
 */
public class SimpleTLoggerModule implements TLoggerModule {

    public SimpleTLoggerModule(){

    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param msg 错误信息
     */
    @Override
    public void e(Class host, String tag, String msg) {
        Log.e(tag + getClassSimpleName(host), msg);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param msg 错误信息
     * @param t 异常
     */
    @Override
    public void e(Class host, String tag, String msg, Throwable t) {
        Log.e(tag + getClassSimpleName(host), msg, t);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param t 异常
     */
    @Override
    public void e(Class host, String tag, Throwable t) {
        Log.e(tag + getClassSimpleName(host), "", t);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param msg 错误信息
     */
    @Override
    public void w(Class host, String tag, String msg) {
        Log.w(tag + getClassSimpleName(host), msg);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param msg 错误信息
     * @param t 异常
     */
    @Override
    public void w(Class host, String tag, String msg, Throwable t) {
        Log.w(tag + getClassSimpleName(host), msg, t);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param t 异常
     */
    @Override
    public void w(Class host, String tag, Throwable t) {
        Log.w(tag + getClassSimpleName(host), "", t);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param msg 信息
     */
    @Override
    public void i(Class host, String tag, String msg) {
        Log.i(tag + getClassSimpleName(host), msg);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param msg 信息
     */
    @Override
    public void d(Class host, String tag, String msg) {
        Log.d(tag + getClassSimpleName(host), msg);
    }

    /**
     * {@inheritDoc}
     * @param host tag
     */
    private String getClassSimpleName(Class host){
        if (host != null){
            return ":" + host.getSimpleName();
        }
        return "";
    }

}