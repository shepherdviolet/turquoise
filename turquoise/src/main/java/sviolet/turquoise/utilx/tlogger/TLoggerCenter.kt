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

/**
 * logger rule
 *
 * Created by S.Violet on 2017/5/23.
 */
internal class TLoggerCenter {
    companion object Companion {

        private var globalLevel = TLogger.ALL
        private var customRules = mutableMapOf<String, Int>()

        private var loggerCache = mutableMapOf<Class<Any>, TLogger>()
        private var defaultLogger = TLoggerProxy(null, TLogger.ALL)

        fun addRules(rules: Map<String, Int>?) {
            if (rules == null) return
            customRules.putAll(rules)
        }

        fun resetRules(rules: Map<String, Int>?) {
            customRules = mutableMapOf<String, Int>()
            if (rules == null) return
            customRules.putAll(rules)
        }

        fun setGlobalLevel(level: Int?){
            if (level == null){
                globalLevel = TLogger.NULL
                return
            }
            globalLevel = level
        }

        fun check(host: Class<Any>?): Int {
            val className = host?.name ?: return TLogger.ALL
            var ruleKeyLength = 0
            var ruleLevel = globalLevel
            customRules.forEach { (key, value) ->
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

        fun fetchLogger(hostObj: Any?) : TLogger {
            val host = hostObj?.getClass() ?: return defaultLogger
            var logger = loggerCache[host]
            if (logger != null){
                return logger
            }
            logger = TLoggerProxy(host, check(host))
            loggerCache.put(host, logger)
            return logger
        }

    }
}