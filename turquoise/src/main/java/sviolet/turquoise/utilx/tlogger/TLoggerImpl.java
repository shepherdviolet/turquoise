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

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.locks.ReentrantLock;

import sviolet.thistle.util.judge.CheckUtils;
import sviolet.turquoise.common.statics.StringConstants;

/**
 * <p>日志打印器实现类, 实现日志打印逻辑</p>
 *
 * Created by S.Violet on 2017/5/23.
 */
class TLoggerImpl extends TLogger {

    private Class<?> host;
    private int level;
    private int ruleUpdateTimes;
    private String className;

    private ReentrantLock lock = new ReentrantLock();

    TLoggerImpl(@NonNull Class<?> host, int level, int ruleUpdateTimes) {
        this.host = host;
        this.level = level;
        this.ruleUpdateTimes = ruleUpdateTimes;

        className = host.getSimpleName();
        if (CheckUtils.isEmpty(className)) {
            String[] names = host.getName().split("\\.");
            if (names.length > 0) {
                className = names[names.length - 1];
            }
        }
    }

    @Override
    public boolean checkEnable(int level) {
        updateLevel();
        return CheckUtils.isFlagMatch(this.level, level);
    }

    @Override
    public void e(Object msg) {
        updateLevel();
        if (CheckUtils.isFlagMatch(level, ERROR)) {
            String message = "[" + className + "]" + msg;
            Log.e(StringConstants.LIBRARY_TAG, message);
            TLoggerCenter.INSTANCE.getPrinter().e(message, null);
        }
    }

    @Override
    public void e(Object msg, Throwable throwable) {
        updateLevel();
        if (CheckUtils.isFlagMatch(level, ERROR)) {
            String message = "[" + className + "]" + msg;
            Log.e(StringConstants.LIBRARY_TAG, message, throwable);
            TLoggerCenter.INSTANCE.getPrinter().e(message, throwable);
        }
    }

    @Override
    public void e(Throwable throwable) {
        updateLevel();
        if (CheckUtils.isFlagMatch(level, ERROR)) {
            String message = "[" + className + "]ERROR";
            Log.e(StringConstants.LIBRARY_TAG, message, throwable);
            TLoggerCenter.INSTANCE.getPrinter().e(message, throwable);
        }
    }

    @Override
    public void w(Object msg) {
        updateLevel();
        if (CheckUtils.isFlagMatch(level, WARNING)) {
            String message = "[" + className + "]" + msg;
            Log.w(StringConstants.LIBRARY_TAG, message);
            TLoggerCenter.INSTANCE.getPrinter().w(message, null);
        }
    }

    @Override
    public void w(Object msg, Throwable throwable) {
        updateLevel();
        if (CheckUtils.isFlagMatch(level, WARNING)) {
            String message = "[" + className + "]" + msg;
            Log.w(StringConstants.LIBRARY_TAG, message, throwable);
            TLoggerCenter.INSTANCE.getPrinter().w(message, throwable);
        }
    }

    @Override
    public void w(Throwable throwable) {
        updateLevel();
        if (CheckUtils.isFlagMatch(level, WARNING)) {
            String message = "[" + className + "]WARNING";
            Log.w(StringConstants.LIBRARY_TAG, message, throwable);
            TLoggerCenter.INSTANCE.getPrinter().w(message, throwable);
        }
    }

    @Override
    public void i(Object msg) {
        updateLevel();
        if (CheckUtils.isFlagMatch(level, INFO)) {
            String message = "[" + className + "]" + msg;
            Log.i(StringConstants.LIBRARY_TAG, message);
            TLoggerCenter.INSTANCE.getPrinter().i(message);
        }
    }

    @Override
    public void d(Object msg) {
        updateLevel();
        if (CheckUtils.isFlagMatch(level, DEBUG)) {
            String message = "[" + className + "]" + msg;
            Log.d(StringConstants.LIBRARY_TAG, message);
            TLoggerCenter.INSTANCE.getPrinter().d(message);
        }
    }

    private void updateLevel() {
        if (ruleUpdateTimes != TLoggerCenter.INSTANCE.getRuleUpdateTimes()) {
            try {
                lock.lock();
                if (ruleUpdateTimes != TLoggerCenter.INSTANCE.getRuleUpdateTimes()) {
                    this.level = TLoggerCenter.INSTANCE.check(host);
                    this.ruleUpdateTimes = TLoggerCenter.INSTANCE.getRuleUpdateTimes();
                }
            } finally {
                lock.unlock();
            }
        }
    }

}

/////////////////////////////////////////////////////////////////////////////////////////
// Kotlin 实现

//internal class TLoggerImpl(
//        private val host: Class<*>?,
//        private var level: Int?,
//        private var ruleUpdateTimes: Int?
//) : TLogger(){
//
//    private val lock = ReentrantLock()
//
//    override fun checkEnable(level: Int): Boolean {
//        updateLevel()
//        return CheckUtils.isFlagMatch(this.level ?: NULL, level)
//    }
//
//    override fun e(msg: Any?) {
//        updateLevel()
//        if (CheckUtils.isFlagMatch(level ?: NULL, ERROR))
//            Log.e(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg")
//    }
//
//    override fun e(msg: Any?, t: Throwable?) {
//        updateLevel()
//        if (CheckUtils.isFlagMatch(level ?: NULL, ERROR))
//            Log.e(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg", t)
//    }
//
//    override fun e(t: Throwable?) {
//        updateLevel()
//        if (CheckUtils.isFlagMatch(level ?: NULL, ERROR))
//            Log.e(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]", t)
//    }
//
//    override fun w(msg: Any?) {
//        updateLevel()
//        if (CheckUtils.isFlagMatch(level ?: NULL, WARNING))
//            Log.w(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg")
//    }
//
//    override fun w(msg: Any?, t: Throwable?) {
//        updateLevel()
//        if (CheckUtils.isFlagMatch(level ?: NULL, WARNING))
//            Log.w(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg", t)
//    }
//
//    override fun w(t: Throwable?) {
//        updateLevel()
//        if (CheckUtils.isFlagMatch(level ?: NULL, WARNING))
//            Log.w(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]", t)
//    }
//
//    override fun i(msg: Any?) {
//        updateLevel()
//        if (CheckUtils.isFlagMatch(level ?: NULL, INFO))
//            Log.i(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg")
//    }
//
//    override fun d(msg: Any?) {
//        updateLevel()
//        if (CheckUtils.isFlagMatch(level ?: NULL, DEBUG))
//            Log.d(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg")
//    }
//
//    private fun updateLevel(){
//        if (ruleUpdateTimes != TLoggerCenter.getRuleUpdateTimes()){
//            lock.sync {
//                if (ruleUpdateTimes != TLoggerCenter.getRuleUpdateTimes()) {
//                    this.level = TLoggerCenter.check(host)
//                    this.ruleUpdateTimes = TLoggerCenter.getRuleUpdateTimes()
//                }
//            }
//        }
//    }
//
//}
