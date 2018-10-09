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

package sviolet.turquoise.enhance.app.annotation.setting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import sviolet.turquoise.x.common.tlogger.TLogger;

/**
 * [TApplication注释]发布模式参数设置
 *
 * @author S.Violet
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ReleaseSettings {

    /**
     * 启用策略检测
     */
    boolean enableStrictMode() default false;

    /**
     * 启用崩溃自启
     */
    boolean enableCrashRestart() default false;

    /**
     * 启用崩溃日志处理
     */
    boolean enableCrashHandle() default false;

    /**
     * 日志级别开关<p/>
     *
     * 示例:<br/>
     * 开启ERROR和INFO日志 : TLogger.ERROR | TLogger.INFO
     */
    int logGlobalLevel() default TLogger.ERROR | TLogger.INFO;

}
