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

package sviolet.turquoise.utilx.tlogger.printer;

/**
 * 日志磁盘输出接口
 *
 * <p>WARNING:: Need permission "android.permission.WRITE_EXTERNAL_STORAGE",
 * you should request runtime permission before TLogger.setLoggerPrinter(new SimpleLoggerPrinter(...))</p>
 *
 * Created by S.Violet on 2017/8/16.
 */
public interface LoggerPrinter {

    void e(Object msg, Throwable throwable);

    void w(Object msg, Throwable throwable);

    void i(Object msg);

    void d(Object msg);

    void start();

    void flush();

    void close();

}
