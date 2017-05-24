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

package sviolet.turquoise.utilx.tlogger

import android.util.Log
import sviolet.turquoise.common.statics.StringConstants
import sviolet.turquoise.util.common.CheckUtils

/**
 * Logger proxy
 *
 * Created by S.Violet on 2017/5/23.
 */
internal class TLoggerProxy(val host: Class<Any>?, val level: Int?) : TLogger(){

    override fun checkEnable(level: Int): Boolean {
        return CheckUtils.isFlagMatch(this.level ?: NULL, level)
    }

    override fun e(msg: String?) {
        if (CheckUtils.isFlagMatch(level ?: NULL, ERROR))
            Log.e(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg")
    }

    override fun e(msg: String?, t: Throwable?) {
        if (CheckUtils.isFlagMatch(level ?: NULL, ERROR))
            Log.e(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg", t)
    }

    override fun e(t: Throwable?) {
        if (CheckUtils.isFlagMatch(level ?: NULL, ERROR))
            Log.e(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]", t)
    }

    override fun w(msg: String?) {
        if (CheckUtils.isFlagMatch(level ?: NULL, WARNING))
            Log.w(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg")
    }

    override fun w(msg: String?, t: Throwable?) {
        if (CheckUtils.isFlagMatch(level ?: NULL, WARNING))
            Log.w(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg", t)
    }

    override fun w(t: Throwable?) {
        if (CheckUtils.isFlagMatch(level ?: NULL, WARNING))
            Log.w(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]", t)
    }

    override fun i(msg: String?) {
        if (CheckUtils.isFlagMatch(level ?: NULL, INFO))
            Log.i(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg")
    }

    override fun d(msg: String?) {
        if (CheckUtils.isFlagMatch(level ?: NULL, DEBUG))
            Log.d(StringConstants.LIBRARY_TAG, "[${host?.simpleName}]$msg")
    }

}
