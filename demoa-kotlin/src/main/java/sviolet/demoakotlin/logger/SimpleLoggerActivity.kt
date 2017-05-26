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

package sviolet.demoakotlin.logger

import android.os.Bundle
import sviolet.demoakotlin.R
import sviolet.demoakotlin.common.DemoDescription
import sviolet.turquoise.enhance.app.TActivity
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings
import sviolet.turquoise.kotlin.extensiona.toast
import sviolet.turquoise.utilx.tlogger.*

/**
 * 简易的TLogger DEMO
 */

@DemoDescription(
        title = "Simple logger demo",
        type = "log",
        info = "Simple logger demo of TLogger"
)
@ResourceId(R.layout.logger_simple_main)
@ActivitySettings(
        statusBarColor = 0xFF30C0C0.toInt(),
        navigationBarColor = 0xFF30C0C0.toInt()
)
class SimpleLoggerActivity : TActivity() {

    override fun onInitViews(savedInstanceState: Bundle?) {

        println("Turquoise: SimpleLoggerDemo: global level = ALL, no rules")

        logSetGlobalLevel(TLogger.ALL)
        logResetRules(hashMapOf())

        printLog()

        println("Turquoise: SimpleLoggerDemo: global level = ERROR or DEBUG, no rules")

        logSetGlobalLevel(TLogger.ERROR or TLogger.DEBUG)
        logResetRules(hashMapOf())

        printLog()

        println("Turquoise: SimpleLoggerDemo: global level = ALL, sviolet.demoakotlin = DEBUG or WARNING, sviolet.demoakotlin.logger = ERROR or INFO")

        logSetGlobalLevel(TLogger.ALL)
        logResetRules(hashMapOf(
                "sviolet.demoakotlin" to (TLogger.DEBUG or TLogger.WARNING),
                "sviolet.demoakotlin.logger" to (TLogger.ERROR or TLogger.INFO)
        ))

        printLog()

        println("Turquoise: SimpleLoggerDemo: global level = NULL, sviolet.demoakotlin = ERROR or WARNING, sviolet.demoakotlin.logger = DEBUG or INFO")

        logSetGlobalLevel(TLogger.NULL)
        logResetRules(hashMapOf(
                "sviolet.demoakotlin" to (TLogger.ERROR or TLogger.WARNING),
                "sviolet.demoakotlin.logger" to (TLogger.DEBUG or TLogger.INFO)
        ))

        printLog()

        toast("See logcat please")

    }

    private fun printLog(){
        loge("Turquoise: SimpleLoggerDemo: error1")
        loge("Turquoise: SimpleLoggerDemo: error2", Exception("exception e2"))
        loge(Exception("exception e3"))
        logw("Turquoise: SimpleLoggerDemo: warning 1")
        logw("Turquoise: SimpleLoggerDemo: warning 2", Exception("exception w2"))
        logw(Exception("exception w3"))
        logi("Turquoise: SimpleLoggerDemo: info 1")
        logd("Turquoise: SimpleLoggerDemo: debug 1")
    }

}
