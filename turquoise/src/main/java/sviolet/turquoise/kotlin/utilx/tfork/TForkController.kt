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

import android.os.Handler
import android.os.Looper
import sviolet.turquoise.model.queue.AsyncWaiter

/**
 * 提供fork块中的操作
 *
 * Created by S.Violet on 2017/5/31.
 */
class TForkController internal constructor() {

    //块计数
    private var blockCounter = 0
    //回调主线程Handler
    private object mHandler : Handler(Looper.getMainLooper())

    /**
     * 启动新线程执行代码块, 阻塞fork块的线程, 直到结果返回.
     * 注意: await块中, 必须调用TForkCallback.callback方法返回结果或返回异常, 否则会导致fork块的线程永久
     * 阻塞!!!
     * @param block 代码块
     */
    fun <R> await(block: (TForkCallback<R>) -> Unit) : R?{
        return await(TForkConfigure.DEFAULT_AWAIT_TIMEOUT, block)
    }

    /**
     * 启动新线程执行代码块, 阻塞fork块的线程, 直到结果返回或超时时间到.
     * 注意: await块中, 必须调用TForkCallback.callback方法返回结果或返回异常. 若超时时间到, 会抛出异常
     * 结束整个fork块的调度.
     * @param timeout 等待超时时间
     * @param block 代码块
     */
    fun <R> await(timeout: Long, block: (TForkCallback<R>) -> Unit) : R?{
        val blockIndex = blockCounter++
        val callback = TForkCallback<R>(timeout)
        TForkCenter.execute {
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
            AsyncWaiter.Result.TIMEOUT -> throw TForkAwaitTimeoutException("[TFork]await timeout($timeout ms), block index($blockIndex)")
        }
    }

    /**
     * 启动新线程执行代码块, 阻塞fork块的线程, 直到结果返回.
     * 注意: await块中, 必须调用TForkCallback.callback方法返回结果或返回异常, 否则会导致fork块的线程永久
     * 阻塞!!!
     * @param block 代码块
     * @param exceptionHandler 处理代码块中抛出的异常(包括主动callback的异常); 返回true时, fork线程继续
     * 执行, await块返回结果为null; 返回false时, fork线程终止并抛出异常.
     */
    fun <R> await(block: (TForkCallback<R>) -> Unit, exceptionHandler: (Exception) -> Boolean) : R?{
        return await(TForkConfigure.DEFAULT_AWAIT_TIMEOUT, block, exceptionHandler)
    }

    /**
     * 启动新线程执行代码块, 阻塞fork块的线程, 直到结果返回或超时时间到.
     * 注意: await块中, 必须调用TForkCallback.callback方法返回结果或返回异常. 若超时时间到, 会抛出异常
     * 结束整个fork块的调度.
     * @param timeout 等待超时时间
     * @param block 代码块
     * @param exceptionHandler 处理代码块中抛出的异常(包括主动callback的异常); 返回true时, fork线程继续
     * 执行, await块返回结果为null; 返回false时, fork线程终止并抛出异常.
     */
    fun <R> await(timeout: Long, block: (TForkCallback<R>) -> Unit, exceptionHandler: (Exception) -> Boolean) : R?{
        val blockIndex = blockCounter++
        val callback = TForkCallback<R>(timeout)
        TForkCenter.execute {
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
            AsyncWaiter.Result.TIMEOUT -> throw TForkAwaitTimeoutException("[TFork]await timeout($timeout ms), block index($blockIndex)")
        }
    }

    /**
     * 在主线程执行代码块, 阻塞fork块的线程, 直到结果返回.
     * 注意: uiAwait块中, 必须调用TForkCallback.callback方法返回结果或返回异常, 否则会导致fork块的线程永久
     * 阻塞!!!
     * @param block 代码块
     */
    fun uiAwait(block: () -> Unit){
        uiAwait(TForkConfigure.DEFAULT_AWAIT_TIMEOUT, block)
    }

    /**
     * 在主线程执行代码块, 阻塞fork块的线程, 直到结果返回或超时时间到.
     * 注意: uiAwait块中, 必须调用TForkCallback.callback方法返回结果或返回异常. 若超时时间到, 会抛出异常
     * 结束整个fork块的调度.
     * @param timeout 等待超时时间
     * @param block 代码块
     */
    fun uiAwait(timeout: Long, block: () -> Unit){
        val blockIndex = blockCounter++
        val callback = TForkCallback<Unit>(timeout)
        mHandler.post {
            try {
                block()
                callback.callback(null)
            } catch (e: Exception){
                callback.callback(Exception("[TFork]catch exception from \"uiAwait\" block, block index($blockIndex)", e))
            }
        }
        val result = callback.waitForResult()
        when(result){
            AsyncWaiter.Result.SUCCESS -> return
            AsyncWaiter.Result.ERROR -> throw callback.getException()
            AsyncWaiter.Result.TIMEOUT -> throw TForkAwaitTimeoutException("[TFork]uiAwait timeout($timeout ms), block index($blockIndex)")
        }
    }

    /**
     * 在主线程执行代码块, 阻塞fork块的线程, 直到结果返回.
     * 注意: uiAwait块中, 必须调用TForkCallback.callback方法返回结果或返回异常, 否则会导致fork块的线程永久
     * 阻塞!!!
     * @param block 代码块
     * @param exceptionHandler 处理代码块中抛出的异常(包括主动callback的异常); 返回true时, fork线程继续
     * 执行, uiAwait块返回结果为null; 返回false时, fork线程终止并抛出异常.
     */
    fun uiAwait(block: () -> Unit, exceptionHandler: (Exception) -> Boolean){
        uiAwait(TForkConfigure.DEFAULT_AWAIT_TIMEOUT, block, exceptionHandler)
    }

    /**
     * 在主线程执行代码块, 阻塞fork块的线程, 直到结果返回或超时时间到.
     * 注意: uiAwait块中, 必须调用TForkCallback.callback方法返回结果或返回异常. 若超时时间到, 会抛出异常
     * 结束整个fork块的调度.
     * @param timeout 等待超时时间
     * @param block 代码块
     * @param exceptionHandler 处理代码块中抛出的异常(包括主动callback的异常); 返回true时, fork线程继续
     * 执行, uiAwait块返回结果为null; 返回false时, fork线程终止并抛出异常.
     */
    fun uiAwait(timeout: Long, block: () -> Unit, exceptionHandler: (Exception) -> Boolean){
        val blockIndex = blockCounter++
        val callback = TForkCallback<Unit>(timeout)
        mHandler.post {
            try {
                block()
                callback.callback(null)
            } catch (e: Exception){
                callback.callback(Exception("[TFork]catch exception from \"uiAwait\" block, block index($blockIndex)", e))
            }
        }
        val result = callback.waitForResult()
        when(result){
            AsyncWaiter.Result.SUCCESS -> return
            AsyncWaiter.Result.ERROR -> if (exceptionHandler(callback.getException())) return else throw callback.getException()
            AsyncWaiter.Result.TIMEOUT -> throw TForkAwaitTimeoutException("[TFork]uiAwait timeout($timeout ms), block index($blockIndex)")
        }
    }

    /**
     * 在主线程执行代码块, 与fork块异步.
     * @param block 代码块
     */
    fun ui(block: () -> Unit){
        blockCounter++
        mHandler.post {
            block()
        }
    }

}