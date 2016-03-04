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

package sviolet.turquoise.utilx.tlogger.def;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import sviolet.turquoise.util.common.CheckUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.utilx.tlogger.TLoggerModule;

/**
 * 简易日志打印器模块实现
 *
 * Created by S.Violet on 2016/1/14.
 */
public class SimpleTLoggerModule extends TLoggerModule {

    public SimpleTLoggerModule(){

    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param msg 错误信息
     */
    @Override
    protected void e(Class host, String tag, String msg) {
        if (checkRule(tag, TLogger.ERROR))
            Log.e(tag, getClassSimpleName(host) + msg);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param msg 错误信息
     * @param t 异常
     */
    @Override
    protected void e(Class host, String tag, String msg, Throwable t) {
        if (checkRule(tag, TLogger.ERROR))
            Log.e(tag, getClassSimpleName(host) + msg, t);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param t 异常
     */
    @Override
    protected void e(Class host, String tag, Throwable t) {
        if (checkRule(tag, TLogger.ERROR))
            Log.e(tag, getClassSimpleName(host), t);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param msg 错误信息
     */
    @Override
    protected void w(Class host, String tag, String msg) {
        if (checkRule(tag, TLogger.WARNING))
            Log.w(tag, getClassSimpleName(host) + msg);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param msg 错误信息
     * @param t 异常
     */
    @Override
    protected void w(Class host, String tag, String msg, Throwable t) {
        if (checkRule(tag, TLogger.WARNING))
            Log.w(tag, getClassSimpleName(host) + msg, t);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param t 异常
     */
    @Override
    protected void w(Class host, String tag, Throwable t) {
        if (checkRule(tag, TLogger.WARNING))
            Log.w(tag, getClassSimpleName(host), t);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param msg 信息
     */
    @Override
    protected void i(Class host, String tag, String msg) {
        if (checkRule(tag, TLogger.INFO))
            Log.i(tag, getClassSimpleName(host) + msg);
    }

    /**
     * {@inheritDoc}
     * @param host 信息发送者标识(nullable)
     * @param tag tag
     * @param msg 信息
     */
    @Override
    protected void d(Class host, String tag, String msg) {
        if (checkRule(tag, TLogger.DEBUG))
            Log.d(tag, getClassSimpleName(host) + msg);
    }

    /**
     * {@inheritDoc}
     * @param host tag
     */
    private String getClassSimpleName(Class host){
        if (host != null){
            return "Class<" + host.getSimpleName() + ">: ";
        }
        return "";
    }

    /**************************************************
     * RULE
     */

    //规则
    private Map<String, Rule> ruleMap = new HashMap<>();

    /**
     * 添加一个日志规则
     * @param tag 规则作用的TAG
     * @param rule 规则
     * @return SimpleTLoggerModule
     */
    public SimpleTLoggerModule addRule(String tag, Rule rule){
        if (tag == null){
            throw new RuntimeException("[SimpleTLoggerModule.Rule]tag must not be null");
        }
        ruleMap.put(tag, rule);
        return this;
    }

    /**
     * 检查规则
     * @param tag 日志TAG
     * @param level 日志打印级别
     * @return true:打印 false:不打印
     */
    private boolean checkRule(String tag, int level){
        Rule rule = ruleMap.get(tag);
        if (rule == null){
            return true;
        }
        if (CheckUtils.isFlagMatch(rule.getLevel(), level)){
            return true;
        }
        return false;
    }

    /**
     * 日志打印规则
     */
    public static class Rule{
        private int level;

        /**
         * @param level 例如: TLogger.ERROR | TLogger.INFO
         */
        public Rule(int level){
            this.level = level;
        }

        public int getLevel(){
            return level;
        }
    }

}
