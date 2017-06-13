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

/**
 * <p>TLogger日志打印器Kotlin入口 (Java参考TLogger.java)</p>
 *
 * <p>日志级别配置==============================================</p>
 *
 * <pre>{@code
 *  @ApplicationSettings(
 *      DEBUG = true
 *  )
 *  @ReleaseSettings(
 *      logGlobalLevel = TLogger.ERROR or TLogger.INFO
 *  )
 *  @DebugSettings(
 *      logGlobalLevel = TLogger.ERROR or TLogger.INFO or TLogger.WARNING or TLogger.DEBUG
 *  )
 *  class KtApplication : TApplicationForMultiDex() {
 *      override fun afterCreate() {
 *          super.afterCreate()
 *          tloggerAddRules(hashMapOf(
 *              "sviolet.turquoise" to TLogger.ALL,//sviolet.turquoise包名的类打印全部日志
 *              "sviolet.turquoise.x.imageloader" to (TLogger.ERROR or TLogger.INFO)//sviolet.turquoise.x.imageloader包名的类打印ERROR和INFO日志
 *          ))
 *      }
 *  }
 * }</pre>
 *
 * <p>日志打印==============================================</p>
 *
 * <pre>{@code
 *  class MyActivity : TActivity(){
 *      fun function(){
 *          logd("message")
 *          TActivity::class.logd("message")
 *      }
 *  }
 * }</pre>
 *
 * Created by S.Violet on 2017/5/23.
 */

fun Any?.logSetGlobalLevel(level: Int){
    TLoggerCenter.INSTANCE.setGlobalLevel(level)
}

fun Any?.logAddRules(rules: Map<String, Int>?){
    TLoggerCenter.INSTANCE.addRules(rules)
}

fun Any?.logResetRules(rules: Map<String, Int>?){
    TLoggerCenter.INSTANCE.resetRules(rules)
}

fun Any?.loge(msg: Any?) {
    var host = this
    if (this is kotlin.jvm.internal.ClassReference) host = this.jClass
    TLoggerCenter.INSTANCE.fetchLogger(host).e(msg)
}

fun Any?.loge(msg: Any?, t: Throwable?) {
    var host = this
    if (this is kotlin.jvm.internal.ClassReference) host = this.jClass
    TLoggerCenter.INSTANCE.fetchLogger(host).e(msg, t)
}

fun Any?.loge(t: Throwable?) {
    var host = this
    if (this is kotlin.jvm.internal.ClassReference) host = this.jClass
    TLoggerCenter.INSTANCE.fetchLogger(host).e(t)
}

fun Any?.logw(msg: Any?) {
    var host = this
    if (this is kotlin.jvm.internal.ClassReference) host = this.jClass
    TLoggerCenter.INSTANCE.fetchLogger(host).w(msg)
}

fun Any?.logw(msg: Any?, t: Throwable?) {
    var host = this
    if (this is kotlin.jvm.internal.ClassReference) host = this.jClass
    TLoggerCenter.INSTANCE.fetchLogger(host).w(msg, t)
}

fun Any?.logw(t: Throwable?) {
    var host = this
    if (this is kotlin.jvm.internal.ClassReference) host = this.jClass
    TLoggerCenter.INSTANCE.fetchLogger(host).w(t)
}

fun Any?.logi(msg: Any?) {
    var host = this
    if (this is kotlin.jvm.internal.ClassReference) host = this.jClass
    TLoggerCenter.INSTANCE.fetchLogger(host).i(msg)
}

fun Any?.logd(msg: Any?) {
    var host = this
    if (this is kotlin.jvm.internal.ClassReference) host = this.jClass
    TLoggerCenter.INSTANCE.fetchLogger(host).d(msg)
}
