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

import sviolet.turquoise.kotlin.extensions.getClass
import sviolet.turquoise.kotlin.extensions.getSnapShot
import sviolet.turquoise.kotlin.extensions.sync
import java.util.concurrent.locks.ReentrantLock

/**
 * 日志打印器核心逻辑
 *
 * Created by S.Violet on 2017/5/23.
 */
internal object TLoggerCenter {

    //全局日志级别
    private var globalLevel = TLogger.ALL
    //规则
    private var customRules = mutableMapOf<String, Int>()
    //日志打印器缓存(用于kotlin)
    private var loggerCache = mutableMapOf<Class<Any>, TLogger>()

    //空打印器
    private val nullLogger = TLoggerProxy(null, TLogger.NULL)

    //同步锁
    private val cacheLock = ReentrantLock()
    private val ruleLock = ReentrantLock()

    /**
     * 添加规则
     */
    fun addRules(rules: Map<String, Int>?) {
        if (rules == null) return
        ruleLock.sync {
            customRules.putAll(rules)
        }
    }

    /**
     * 重新设置规则
     */
    fun resetRules(rules: Map<String, Int>?) {
        ruleLock.sync {
            customRules = mutableMapOf<String, Int>()
            if (rules == null) return@sync
            customRules.putAll(rules)
        }
    }

    /**
     * 设置全局日志级别
     */
    fun setGlobalLevel(level: Int?) {
        if (level == null) {
            globalLevel = TLogger.NULL
            return
        }
        globalLevel = level
    }

    /**
     * 根据规则生成打印器的日志级别
     */
    private fun check(host: Class<Any>?): Int {
        val className = host?.name ?: return TLogger.NULL
        var ruleKeyLength = 0
        var ruleLevel = globalLevel
        var snapshot: Map<String, Int>? = null
        ruleLock.sync {
            snapshot = customRules.getSnapShot()
        }
        snapshot?.forEach { (key, value) ->
            if (!className.startsWith(key)) {
                return@forEach
            }
            if (key.length > ruleKeyLength) {
                ruleKeyLength = key.length
                ruleLevel = value
            }
        }
        return ruleLevel
    }

    /**
     * 创建打印器
     */
    fun newLogger(host: Class<Any>?) : TLogger {
        return TLoggerProxy(host, check(host))
    }

    /**
     * 创建打印器
     */
    fun newLogger(hostObj: Any?): TLogger {
        return newLogger(hostObj?.getClass())
    }

    /**
     * 尝试从缓存获取打印器(用于kotlin)
     */
    fun fetchLogger(hostObj: Any?): TLogger {
        val host = hostObj?.getClass() ?: return nullLogger
        var logger = loggerCache[host]
        if (logger == null) {
            cacheLock.sync {
                logger = loggerCache[host]
                if (logger == null) {
                    logger = newLogger(host)
                    loggerCache.put(host, logger as TLogger)
                }
            }
        }
        return logger ?: nullLogger
    }
}