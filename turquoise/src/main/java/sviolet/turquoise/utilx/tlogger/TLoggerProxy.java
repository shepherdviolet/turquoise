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

import sviolet.turquoise.util.common.CheckUtils;

/**
 * 日志打印器(代理类)
 *
 * Created by S.Violet on 2016/1/13.
 */
class TLoggerProxy extends TLogger {

    static final TLogger LOGGER_NULL = new TLoggerNull();

    private Class host;
    private String tag;
    private boolean enabled;

    /**
     * @param host 信息发送者标识(nullable)
     * @param tag 自定义标签
     */
    TLoggerProxy(Class host, String tag){
        this(host, tag, true);
    }

    private TLoggerProxy(Class host, String tag, boolean enabled){
        this.host = host;
        this.tag = tag;
        this.enabled = enabled;
    }

    @Override
    public void e(String msg) {
        if (enabled && getModule() != null && CheckUtils.isFlagMatch(getGlobalLevel(), ERROR)){
            getModule().e(host, getTag(), msg);
        }
    }

    @Override
    public void e(String msg, Throwable t) {
        if (enabled && getModule() != null && CheckUtils.isFlagMatch(getGlobalLevel(), ERROR)){
            getModule().e(host, getTag(), msg, t);
        }
    }

    @Override
    public void e(Throwable t) {
        if (enabled && getModule() != null && CheckUtils.isFlagMatch(getGlobalLevel(), ERROR)){
            getModule().e(host, getTag(), t);
        }
    }

    @Override
    public void w(String msg) {
        if (enabled && getModule() != null && CheckUtils.isFlagMatch(getGlobalLevel(), WARNING)){
            getModule().w(host, getTag(), msg);
        }
    }

    @Override
    public void w(String msg, Throwable t) {
        if (enabled && getModule() != null && CheckUtils.isFlagMatch(getGlobalLevel(), WARNING)){
            getModule().w(host, getTag(), msg, t);
        }
    }

    @Override
    public void w(Throwable t) {
        if (enabled && getModule() != null && CheckUtils.isFlagMatch(getGlobalLevel(), WARNING)){
            getModule().w(host, getTag(), t);
        }
    }

    @Override
    public void i(String msg) {
        if (enabled && getModule() != null && CheckUtils.isFlagMatch(getGlobalLevel(), INFO)){
            getModule().i(host, getTag(), msg);
        }
    }

    @Override
    public void d(String msg) {
        if (enabled && getModule() != null && CheckUtils.isFlagMatch(getGlobalLevel(), DEBUG)){
            getModule().d(host, getTag(), msg);
        }
    }

    protected String getTag() {
        if (tag != null){
            return tag;
        }
        return getDefaultTag();
    }
}
