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

package sviolet.turquoise.x.common.tlogger;

/**
 * 日志打印器代理(用于实现空打印器或转由开发者自行实现)
 *
 * Created by S.Violet on 2017/11/1.
 */
class TLoggerProxy extends TLogger {

    private TLogger provider;

    public TLoggerProxy(TLogger provider) {
        this.provider = provider;
    }

    @Override
    public void e(Object msg) {
        if (provider != null) {
            provider.e(msg);
        }
    }

    @Override
    public void e(Object msg, Throwable t) {
        if (provider != null) {
            provider.e(msg, t);
        }
    }

    @Override
    public void e(Throwable t) {
        if (provider != null) {
            provider.e(t);
        }
    }

    @Override
    public void w(Object msg) {
        if (provider != null) {
            provider.w(msg);
        }
    }

    @Override
    public void w(Object msg, Throwable t) {
        if (provider != null) {
            provider.w(msg, t);
        }
    }

    @Override
    public void w(Throwable t) {
        if (provider != null) {
            provider.w(t);
        }
    }

    @Override
    public void i(Object msg) {
        if (provider != null) {
            provider.i(msg);
        }
    }

    @Override
    public void d(Object msg) {
        if (provider != null) {
            provider.d(msg);
        }
    }

    @Override
    public boolean checkEnable(int level) {
        if (provider != null) {
            return provider.checkEnable(level);
        }
        return false;
    }

}
