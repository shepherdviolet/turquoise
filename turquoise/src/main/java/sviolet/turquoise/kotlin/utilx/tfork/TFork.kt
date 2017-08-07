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

package sviolet.turquoise.kotlin.utilx.tfork

import sviolet.turquoise.utilx.tlogger.logw

/**
 * TFork
 * !!!实验性功能, 谨慎使用!!!
 *
 * Created by S.Violet on 2017/5/31.
 */

object TFork {

    /**
     * 启动新线程执行代码块
     */
    fun fork(block: TForkController.() -> Unit) {
        TForkCenter.executeFork {
            try {
                val controller = TForkController()
                controller.block()
            } catch (e: TForkAwaitTimeoutException) {
                logw(e)
            }
        }
    }

    /**
     * 启动新线程执行代码块
     * @param block 代码块
     * @param exceptionHandler 异常处理块, 用于处理代码块中抛出的异常. 返回true时, 表示异常已被妥善处理,
     * 程序不再向外抛出异常; 若返回false, 则继续向外抛出异常. 异常处理块第一个参数exception为异常对象,
     * 第二个参数isTimeout=true时表示fork代码块由于内部的await/uiAwait块等待超时导致流程终止.
     */
    fun fork(block: TForkController.() -> Unit, exceptionHandler: (exception: Exception, isTimeout: Boolean) -> Boolean) {
        TForkCenter.executeFork {
            try {
                val controller = TForkController()
                controller.block()
            } catch (e: TForkAwaitTimeoutException) {
                if (exceptionHandler(e, true)) return@executeFork else logw(e)
            } catch (e: Exception) {
                if (exceptionHandler(e, false)) return@executeFork else throw e
            }
        }
    }

}