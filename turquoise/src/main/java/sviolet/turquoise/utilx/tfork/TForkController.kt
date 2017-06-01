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

import android.os.Handler
import android.os.Looper
import sviolet.turquoise.model.queue.AsyncWaiter

/**
 * Created by S.Violet on 2017/5/31.
 */
class TForkController internal constructor() {

    private var blockCounter = 0
    private object mHandler : Handler(Looper.getMainLooper())

    fun <R> await(block: (TForkCallback<R>) -> Unit) : R?{
        return await(0, block)
    }

    fun <R> await(timeout: Long, block: (TForkCallback<R>) -> Unit) : R?{
        val blockIndex = blockCounter++
        val callback = TForkCallback<R>(timeout)
        TForkCenter.execute{
            try {
                block(callback)
            } catch (e: Exception){
                callback.callback(Exception("[TFork]catch exception from \"await\" block, block index($blockIndex)", e))
            }
        }
        val result = callback.waitForResult()
        when(result){
            AsyncWaiter.Result.SUCCESS -> return callback.getValue()
            AsyncWaiter.Result.ERROR -> throw callback.getException()
            AsyncWaiter.Result.TIMEOUT -> throw TForkParkTimeoutException("[TFork]await timeout($timeout ms), block index($blockIndex)")
        }
    }

    fun <R> await(block: (TForkCallback<R>) -> Unit, exceptionHandler: (Exception) -> Boolean) : R?{
        return await(0, block, exceptionHandler)
    }

    fun <R> await(timeout: Long, block: (TForkCallback<R>) -> Unit, exceptionHandler: (Exception) -> Boolean) : R?{
        val blockIndex = blockCounter++
        val callback = TForkCallback<R>(timeout)
        TForkCenter.execute{
            try {
                block(callback)
            } catch (e: Exception){
                callback.callback(Exception("[TFork]catch exception from \"await\" block, block index($blockIndex)", e))
            }
        }
        val result = callback.waitForResult()
        when(result){
            AsyncWaiter.Result.SUCCESS -> return callback.getValue()
            AsyncWaiter.Result.ERROR -> if (exceptionHandler(callback.getException())) return null else throw callback.getException()
            AsyncWaiter.Result.TIMEOUT -> throw TForkParkTimeoutException("[TFork]await timeout($timeout ms), block index($blockIndex)")
        }
    }

    fun uiPost(block: () -> Unit){
        val blockIndex = blockCounter++
        mHandler.post {
            block()
        }
    }

    fun ui(block: () -> Unit){
        val blockIndex = blockCounter++
        val callback = TForkCallback<Unit>(0)
        mHandler.post {
            block()
            callback.callback(null)
        }
        callback.waitForResult()
    }

}