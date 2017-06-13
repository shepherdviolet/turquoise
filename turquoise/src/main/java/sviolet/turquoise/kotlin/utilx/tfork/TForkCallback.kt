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

import sviolet.turquoise.model.queue.AsyncWaiter

/**
 * 用于在await/uiAwait异步块中返回异步处理的结果, 使fork调度块的线程继续执行
 *
 * Created by S.Violet on 2017/5/31.
 */
class TForkCallback<T>(val timeout: Long){

    private val waiter: AsyncWaiter<T> = AsyncWaiter(timeout)

    /**
     * 返回结果
     */
    fun callback(value: T?){
        waiter.callback(value)
    }

    /**
     * 返回异常
     */
    fun callback(exception: Exception){
        waiter.callback(exception)
    }

    internal fun getValue() : T?{
        return waiter.value
    }

    internal fun getException() : Exception{
        return waiter.exception
    }

    internal fun waitForResult() : AsyncWaiter.Result {
        return waiter.waitForResult()
    }

    internal fun destroy(){
        waiter.onDestroy()
    }

}