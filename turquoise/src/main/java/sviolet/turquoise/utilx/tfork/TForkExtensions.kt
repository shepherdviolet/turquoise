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

package sviolet.turquoise.utilx.tfork

import sviolet.turquoise.utilx.tlogger.logw

/**
 * TFork
 * !!!实验性功能, 谨慎使用!!!
 *
 * Created by S.Violet on 2017/5/31.
 */

/**
 * 启动新线程执行代码块
 */
fun Any?.fork(block: (TForkController) -> Unit) {
    TForkCenter.executeFork {
        try {
            block(TForkController())
        } catch (e: TForkAwaitTimeoutException) {
            logw(e)
        }
    }
}

/**
 * 启动新线程执行代码块, 第二个代码块用于处理第一个代码块中抛出的异常, 第二个代码块返回true时, 表示异常已被
 * 妥善处理, 程序不再向外抛出异常, 若返回false, 则继续向外抛出异常
 */
fun Any?.fork(block: (TForkController) -> Unit, exceptionHandler: (Exception) -> Boolean) {
    TForkCenter.executeFork {
        try {
            block(TForkController())
        } catch (e: TForkAwaitTimeoutException) {
            logw(e)
        } catch (e: Exception) {
            if (exceptionHandler(e)) return@executeFork else throw e
        }
    }
}
