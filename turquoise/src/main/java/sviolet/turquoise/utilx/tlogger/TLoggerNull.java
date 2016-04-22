/*
 * Copyright (C) 2015-2016 S.Violet
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

package sviolet.turquoise.utilx.tlogger;

/**
 * 空的日志打印器
 *
 * Created by S.Violet on 2016/4/8.
 */
class TLoggerNull extends TLogger {

    @Override
    public void e(String msg) {

    }

    @Override
    public void e(String msg, Throwable t) {

    }

    @Override
    public void e(Throwable t) {

    }

    @Override
    public void w(String msg) {

    }

    @Override
    public void w(String msg, Throwable t) {

    }

    @Override
    public void w(Throwable t) {

    }

    @Override
    public void i(String msg) {

    }

    @Override
    public void d(String msg) {

    }

    @Override
    public boolean isNullLogger() {
        return true;
    }

}
