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

package sviolet.turquoise.util.common;

import android.os.SystemClock;

import sviolet.thistle.util.conversion.DateTimeUtils;

/**
 * 时间工具
 *
 * @author S.Violet ()
 *
 */
public class DateTimeUtilsForAndroid extends DateTimeUtils {

    /**
     * [Android]获得系统启动至今经过的毫秒数, 深睡眠时不计时
     */
    public static long getUptimeMillis(){
        return SystemClock.uptimeMillis();
    }

    /**
     * [Android]获得系统启动至今经过的毫秒数, 深睡眠时仍然计时
     */
    public static long getElapsedRealtime(){
        return SystemClock.elapsedRealtime();
    }

}
