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

package sviolet.turquoise.kotlin.utilx.tjson

import com.google.gson.JsonArray

/**
 * Kotlin json 工具
 *
 * Created by S.Violet on 2017/7/31.
 */
class TJsonArray
internal constructor() {

    internal val provider = JsonArray()

    /**
     * Build string item or JsonObject item
     */
    val item: ItemBuilder
        get() = ItemBuilder()

    /**
     * Build JsonArray item
     */
    val list: ListBuilder
        get() = ListBuilder()

    override fun toString(): String {
        return provider.toString()
    }

    inner class ItemBuilder
    internal constructor(){

        /**
         * value(add String)
         */
        infix fun v(value: Any?) {
            provider.add(value?.toString() ?: "")
        }

        /**
         * block(to build JsonObject)
         */
        infix fun v(block: TJsonObject.() -> Unit) {
            val obj = TJsonObject()
            obj.block()
            provider.add(obj.provider)
        }

    }

    inner class ListBuilder
    internal constructor(){

        private var hasIterable = false
        private var iterable: Any? = null

        /**
         * iterable, optional
         */
        infix fun i(iterable: Iterable<*>): ListBuilder {
            this.iterable = iterable
            hasIterable = true
            return this
        }

        /**
         * iterable, optional
         */
        infix fun i(array: Array<*>): ListBuilder {
            this.iterable = array
            hasIterable = true
            return this
        }

        /**
         * block(to build JsonArray)
         */
        infix fun v(block: TJsonArray.(Any?) -> Unit) {
            val array = TJsonArray()
            if (hasIterable) {
                val i = iterable
                if (i is Iterable<*>) {
                    i.forEach {
                        array.block(it)
                    }
                } else if (i is Array<*>) {
                    i.forEach {
                        array.block(it)
                    }
                } else {
                    throw IllegalArgumentException("[TJson]The \"iterable\" argument cannot be iterate")
                }
            } else {
                array.block(null)
            }
            provider.add(array.provider)
        }

    }

}