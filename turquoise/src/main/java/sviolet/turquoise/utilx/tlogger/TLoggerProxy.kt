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

package sviolet.turquoise.utilx.tlogger

import android.util.Log
import sviolet.turquoise.common.statics.StringConstants
import sviolet.turquoise.kotlin.extensions.sync
import sviolet.turquoise.util.common.CheckUtils
import java.util.concurrent.locks.ReentrantLock

/**
 * 日志打印器代理类
 *
 * Created by S.Violet on 2017/5/23.
 */
internal class TLoggerProxy(
        private val host: Class<Any>?,
        private var level: Int?,
        private var ruleUpdateTimes: Int?
) : TLogger(){

    private val lock = ReentrantLock()

    override fun checkEnable(level: Int): Boolean {
        updateLevel()
        return CheckUtils.isFlagMatch(this.level ?: NULL, level)
    }

    override fun e(msg: String?) {
        updateLevel()
        if (CheckUtils.isFlagMatch(level ?: NULL, ERROR))
            Log.e(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg")
    }

    override fun e(msg: String?, t: Throwable?) {
        updateLevel()
        if (CheckUtils.isFlagMatch(level ?: NULL, ERROR))
            Log.e(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg", t)
    }

    override fun e(t: Throwable?) {
        updateLevel()
        if (CheckUtils.isFlagMatch(level ?: NULL, ERROR))
            Log.e(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]", t)
    }

    override fun w(msg: String?) {
        updateLevel()
        if (CheckUtils.isFlagMatch(level ?: NULL, WARNING))
            Log.w(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg")
    }

    override fun w(msg: String?, t: Throwable?) {
        updateLevel()
        if (CheckUtils.isFlagMatch(level ?: NULL, WARNING))
            Log.w(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg", t)
    }

    override fun w(t: Throwable?) {
        updateLevel()
        if (CheckUtils.isFlagMatch(level ?: NULL, WARNING))
            Log.w(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]", t)
    }

    override fun i(msg: String?) {
        updateLevel()
        if (CheckUtils.isFlagMatch(level ?: NULL, INFO))
            Log.i(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg")
    }

    override fun d(msg: String?) {
        updateLevel()
        if (CheckUtils.isFlagMatch(level ?: NULL, DEBUG))
            Log.d(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg")
    }

    private fun updateLevel(){
        if (ruleUpdateTimes != TLoggerCenter.getRuleUpdateTimes()){
            lock.sync {
                if (ruleUpdateTimes != TLoggerCenter.getRuleUpdateTimes()) {
                    this.level = TLoggerCenter.check(host)
                    this.ruleUpdateTimes = TLoggerCenter.getRuleUpdateTimes()
                }
            }
        }
    }

}
