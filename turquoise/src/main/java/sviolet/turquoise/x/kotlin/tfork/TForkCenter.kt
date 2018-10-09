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

package sviolet.turquoise.x.kotlin.tfork

import sviolet.turquoise.utilx.tlogger.logw
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * TFork线程池
 *
 * Created by S.Violet on 2017/5/31.
 */
internal object TForkCenter {

    //线程池
    private val threadPool = Executors.newCachedThreadPool()
    //fork计数
    private val counter = AtomicInteger(0)

    /**
     * fork块执行
     */
    fun executeFork(block: () -> Unit){
        threadPool.execute{
            val count = counter.incrementAndGet()
            try {
                if (count > TForkConfigure.MAX_THREAD_NUM) {
                    /*
                     * 同时执行的fork块过多, 通常是因为fork块的线程执行时间过长导致,
                     * 特别是await/uiAwait块中, 忘记使用callback返回结果, 导致大量线程阻塞
                     */
                    throw RuntimeException("[TFork]Too many fork threads:$count")
                } else if (count > TForkConfigure.WARNING_THREAD_NUM) {
                    logw("[TFork]Too many fork threads:$count")
                }
                block()
            } finally {
                counter.decrementAndGet()
            }
        }
    }

    /**
     * await/uiAwait/ui块执行
     */
    fun execute(block: () -> Unit){
        threadPool.execute{
            block()
        }
    }

}