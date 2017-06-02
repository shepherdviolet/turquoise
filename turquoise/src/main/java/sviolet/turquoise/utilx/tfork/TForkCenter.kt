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
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by S.Violet on 2017/5/31.
 */
internal object TForkCenter {

    private val threadPool = Executors.newCachedThreadPool()
    private val counter = AtomicInteger(0)

    fun executeFork(block: () -> Unit){
        threadPool.execute{
            val count = counter.incrementAndGet()
            try {
                if (count > TForkConfigure.MAX_THREAD_NUM) {
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

    fun execute(block: () -> Unit){
        threadPool.execute{
            block()
        }
    }

}