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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.thistle.compat.CompatLruCache;
import sviolet.thistle.util.common.ConcurrentUtils;
import sviolet.turquoise.utilx.tlogger.printer.LoggerPrinter;
import sviolet.turquoise.utilx.tlogger.printer.NullLoggerPrinter;

/**
 * 日志打印器核心逻辑
 * <p>
 * Created by S.Violet on 2017/5/23.
 */
class TLoggerCenter {

    static TLoggerCenter INSTANCE = new TLoggerCenter();

    //全局日志级别
    private int globalLevel = TLogger.ALL;
    //规则
    private Map<String, Integer> customRules = new HashMap<>();
    //日志打印器缓存
    private CompatLruCache<Class<?>, TLogger> loggerCache = new CompatLruCache<>(100, false);

    //规则更新时间
    private AtomicInteger ruleUpdateTimes = new AtomicInteger(0);

    //空打印器
    private TLogger nullLogger = new TLoggerProxy(null);

    //同步锁
    private ReentrantLock ruleLock = new ReentrantLock();

    //磁盘输出
    private LoggerPrinter printer = new NullLoggerPrinter();

    //开发者自定义的日志实现
    private TLogger customLogger;

    /**
     * 添加规则
     */
    void addRules(Map<String, Integer> rules) {
        if (rules == null) {
            return;
        }
        try {
            ruleLock.lock();
            customRules.putAll(rules);
        } finally {
            ruleLock.unlock();
        }
        ruleUpdateTimes.incrementAndGet();
    }

    /**
     * 重新设置规则
     */
    void resetRules(Map<String, Integer> rules) {
        try {
            ruleLock.lock();
            customRules = new HashMap<>(rules != null ? rules.size() : 0);
            if (rules == null) {
                return;
            }
            customRules.putAll(rules);
        } finally {
            ruleLock.unlock();
        }
        ruleUpdateTimes.incrementAndGet();
    }

    /**
     * 设置全局日志级别
     */
    void setGlobalLevel(int level) {
        globalLevel = level;
        ruleUpdateTimes.incrementAndGet();
    }

    int getRuleUpdateTimes() {
        return ruleUpdateTimes.get();
    }

    /**
     * 根据规则生成打印器的日志级别
     */
    int check(Class<?> host) {
        if (host == null) {
            return TLogger.NULL;
        }
        String className = host.getName();
        int ruleKeyLength = 0;
        int ruleLevel = globalLevel;
        Map<String, Integer> snapshot = null;
        try {
            ruleLock.lock();
            snapshot = ConcurrentUtils.getSnapShot(customRules);
        } finally {
            ruleLock.unlock();
        }
        for (Map.Entry<String, Integer> entry : snapshot.entrySet()) {
            if (!className.startsWith(entry.getKey())) {
                continue;
            }
            if (entry.getKey().length() > ruleKeyLength) {
                ruleKeyLength = entry.getKey().length();
                ruleLevel = entry.getValue();
            }
        }
        return ruleLevel;
    }

    /**
     * 创建打印器
     */
    private TLogger newLogger(Class<?> host) {
        if (host == null){
            return new TLoggerProxy(null);
        }
        if (customLogger != null){
            return new TLoggerProxy(customLogger);
        }
        return new TLoggerImpl(host, check(host), ruleUpdateTimes.get());
    }

    /**
     * 尝试从缓存获取打印器
     */
    TLogger fetchLogger(Object hostObj) {

        Class<?> host = null;
        if (hostObj == null) {
            return nullLogger;
        } else if (hostObj instanceof Class<?>){
            host = (Class<?>) hostObj;
        } else{
            host = hostObj.getClass();
        }

        TLogger logger = loggerCache.get(host);
        if (logger == null) {
            //并发的时候多创建几个实例也没关系
            logger = newLogger(host);
            loggerCache.put(host, logger);
        }
        return logger;
    }

    void setPrinter(LoggerPrinter printer){
        if (printer == null){
            this.printer = new NullLoggerPrinter();
        } else {
            this.printer = printer;
        }
    }

    LoggerPrinter getPrinter(){
        return printer;
    }

    void flush(){
        getPrinter().flush();
    }

    void setCustomLogger(TLogger customLogger) {
        this.customLogger = customLogger;
    }

}

////////////////////////////////////////////////////////////////////////////////////////
// Kotlin 实现

//internal object TLoggerCenter {
//
//    //全局日志级别
//    private var globalLevel = TLogger.ALL
//    //规则
//    private var customRules = mutableMapOf<String, Int>()
//    //日志打印器缓存(用于kotlin)
//    private var loggerCache = mutableMapOf<Class<*>, TLogger>()
//
//    //规则更新时间
//    private var ruleUpdateTimes = AtomicInteger(0)
//
//    //空打印器
//    private val nullLogger = TLoggerImpl(null, TLogger.NULL, 0)
//
//    //同步锁
//    private val cacheLock = ReentrantLock()
//    private val ruleLock = ReentrantLock()
//
//    /**
//     * 添加规则
//     */
//    fun addRules(rules: Map<String, Int>?) {
//        if (rules == null) return
//        ruleLock.sync {
//            customRules.putAll(rules)
//        }
//        ruleUpdateTimes.incrementAndGet()
//    }
//
//    /**
//     * 重新设置规则
//     */
//    fun resetRules(rules: Map<String, Int>?) {
//        ruleLock.sync {
//            customRules = mutableMapOf<String, Int>()
//            if (rules == null) return@sync
//            customRules.putAll(rules)
//        }
//        ruleUpdateTimes.incrementAndGet()
//    }
//
//    /**
//     * 设置全局日志级别
//     */
//    fun setGlobalLevel(level: Int?) {
//        if (level == null) {
//            globalLevel = TLogger.NULL
//            return
//        }
//        globalLevel = level
//        ruleUpdateTimes.incrementAndGet()
//    }
//
//    fun getRuleUpdateTimes() : Int{
//        return ruleUpdateTimes.get()
//    }
//
//    /**
//     * 根据规则生成打印器的日志级别
//     */
//    fun check(host: Class<*>?): Int {
//        val className = host?.name ?: return TLogger.NULL
//        var ruleKeyLength = 0
//        var ruleLevel = globalLevel
//        var snapshot: Map<String, Int>? = null
//        ruleLock.sync {
//            snapshot = customRules.getSnapShot()
//        }
//        snapshot?.forEach { (key, value) ->
//            if (!className.startsWith(key)) {
//                return@forEach
//            }
//            if (key.length > ruleKeyLength) {
//                ruleKeyLength = key.length
//                ruleLevel = value
//            }
//        }
//        return ruleLevel
//    }
//
//    /**
//     * 创建打印器
//     */
//    fun newLogger(host: Class<*>?) : TLogger {
//        return TLoggerImpl(host, check(host), ruleUpdateTimes.get())
//    }
//
//    /**
//     * 创建打印器
//     */
//    fun newLogger(hostObj: Any?): TLogger {
//        return newLogger(hostObj?.getJClass())
//    }
//
//    /**
//     * 尝试从缓存获取打印器(用于kotlin)
//     */
//    fun fetchLogger(hostObj: Any?): TLogger {
//
//        val host: Class<*>
//        if (hostObj is kotlin.jvm.internal.ClassReference){
//            host = hostObj.jClass
//        } else if (hostObj is Class<*>) {
//            host = hostObj
//        } else {
//            host = hostObj?.getJClass() ?: return nullLogger
//        }
//
//        var logger = loggerCache[host]
//        if (logger == null) {
//            cacheLock.sync {
//                logger = loggerCache[host]
//                if (logger == null) {
//                    logger = newLogger(host)
//                    loggerCache.put(host, logger as TLogger)
//                }
//            }
//        }
//        return logger ?: nullLogger
//    }
//}
