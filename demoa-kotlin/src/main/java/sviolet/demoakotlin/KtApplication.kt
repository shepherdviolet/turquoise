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

package sviolet.demoakotlin

import android.app.Activity
import sviolet.turquoise.enhance.app.MultiDexLoadingActivity
import sviolet.turquoise.enhance.app.TApplicationForMultiDex
import sviolet.turquoise.enhance.app.annotation.setting.ApplicationSettings
import sviolet.turquoise.enhance.app.annotation.setting.DebugSettings
import sviolet.turquoise.enhance.app.annotation.setting.ReleaseSettings
import sviolet.turquoise.utilx.tlogger.TLogger
import sviolet.turquoise.utilx.tlogger.tloggerAddRules

@ApplicationSettings(
        DEBUG = BuildConfig._DEBUG,//Debug模式, 装载DebugSetting配置
        transmitPipeLine = true//启用EvBus:Transmit模式
)
//发布配置
@ReleaseSettings(
        enableStrictMode = false,
        enableCrashRestart = true,
        enableCrashHandle = true,
        logGlobalLevel = TLogger.ERROR or TLogger.INFO
)
//调试配置
@DebugSettings(
        enableStrictMode = true,
        enableCrashRestart = false,
        enableCrashHandle = true,
        logGlobalLevel = TLogger.ERROR or TLogger.INFO or TLogger.WARNING or TLogger.DEBUG
)
class KtApplication : TApplicationForMultiDex() {

    companion object Companion{
        val SHARED_PREF_COMMON_CONFIG = "common_config"
    }

    override fun afterCreate() {
        super.afterCreate()

        tloggerAddRules(hashMapOf(
                "sviolet.turquoise" to TLogger.ALL,//sviolet.turquoise包名的类打印全部日志
                "sviolet.turquoise.x.imageloader" to (TLogger.ERROR or TLogger.INFO)//sviolet.turquoise.x.imageloader包名的类打印ERROR和INFO日志
        ))
    }

    override fun getMultiDexLoadingActivityClass(): Class<out Activity> {
        return MultiDexLoadingActivity::class.java
    }

    override fun onUncaughtException(ex: Throwable, isCrashRestart: Boolean) {
        //异常处理
    }

}

