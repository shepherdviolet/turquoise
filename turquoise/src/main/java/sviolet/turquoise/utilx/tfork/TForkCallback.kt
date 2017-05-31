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

import sviolet.turquoise.model.queue.AsyncWaiter

/**
 * Created by S.Violet on 2017/5/31.
 */
class TForkCallback<T>(val timeout: Long){

    private val waiter: AsyncWaiter<T> = AsyncWaiter(timeout)

    fun callback(value: T?){
        waiter.callback(value)
    }

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