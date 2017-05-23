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

package sviolet.turquoise.kotlin.extensions

/**
 * kotlin common extensions
 *
 * Created by S.Violet on 2017/5/23.
 */

/**
 * Get jvm class.
 * Can be called with a null receiver, in which case it returns null.
 */
fun <T: Any> T?.getClass() : Class<T>?{
    return this?.javaClass
}

/**
 * Get jvm class name.
 * Can be called with a null receiver, in which case it returns "null".
 */
fun Any?.getClassName() : String{
    return this.getClass()?.name ?: "null"
}

/**
 * Get jvm class simple name (without package).
 * Can be called with a null receiver, in which case it returns "null".
 */
fun Any?.getSimpleClassName() : String{
    return this.getClass()?.simpleName ?: "null"
}