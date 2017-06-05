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

/**
 * TFork公共配置
 *
 * Created by S.Violet on 2017/6/2.
 */
object TForkConfigure {

    //当同时执行的fork块达到该数量时, 打印日志警告
    var WARNING_THREAD_NUM = 10
    //当同时执行的fork块达到该数量时, 抛出异常(APP崩溃)
    var MAX_THREAD_NUM = 100

    //await/uiAwait默认超时时间(超时后流程终止)
    var DEFAULT_AWAIT_TIMEOUT = 5 * 60 * 1000L

}