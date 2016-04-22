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

import sviolet.turquoise.utilx.tlogger.def.SimpleTLoggerModule;

/**
 * 日志打印器(入口)<p/>
 *
 * 日志打印器使用入口, 实际逻辑由{@link TLoggerModule}接口实现.<br/>
 *
 * *************************************************<p/>
 *
 * 使用方法:<p/>
 *
 * <pre>{@code
 *      public class Demo1{
 *          //获得日志打印器实例(默认Tag)
 *          private TLogger logger = TLogger.get(this);
 *          public void method(){
 *              //打印日志
 *              logger.d("hello world");
 *          }
 *      }
 *      public class Demo2{
 *          //获得日志打印器实例(带Tag)
 *          private TLogger logger = TLogger.get(this, "demo2");
 *          public void method(){
 *              //打印日志
 *              logger.d("hello world");
 *          }
 *      }
 * }</pre>
 *
 * *************************************************<p/>
 *
 * TLogger配置及扩展:<p/>
 *
 * TLogger默认使用{@link SimpleTLoggerModule}模块实现打印逻辑.<br/>
 * TLogger支持自定义日志打印逻辑:<br/>
 * 1.实现{@link TLoggerModule}抽象类.<br/>
 * 2.TLogger.install(TLoggerModule)方法安装(替换)自定义逻辑模块.<br/>
 * 3.所有TLogger实例会替换为自定义打印逻辑, 包括在安装前用TLogger.get(this)方法获取的实例.<p/>
 *
 * 基本配置方法:<br/>
 * <pre>{@code
 *      //设置
 *      TLogger.getGlobalLevel(TLogger.ERROR | TLogger.INFO);//设置日志打印级别(全局),允许打印ERROR和INFO日志
 *      TLogger.getDefaultTag("Default");//设置默认日志标签
 *      //扩展
 *      TLogger.install(myTLoggerModule);//自定义日志打印器模块
 * }</pre>
 *
 * SimpleTLoggerModule配置方法:<br/>
 * <pre>{@code
 *      //设置日志打印规则
 *      //tag为demo1时, 打印ERROR和INFO日志, tag为demo2时, 不打印日志
 *      ((SimpleTLoggerModule)TLogger.getModule())
 *          .addRule("demo1", new SimpleTLoggerModule.Rule(TLogger.ERROR | TLogger.INFO))
 *          .addRule("demo2", new SimpleTLoggerModule.Rule(TLogger.NULL));
 *      //获得tag为demo1的日志打印器, 该日志打印器打印ERROR和INFO日志
 *      TLogger logger1 = TLogger.get(this, "demo1");
 *      //获得tag为demo2的日志打印器, 该日志打印器不打印日志
 *      TLogger logger2 = TLogger.get(this, "demo2");
 * }</pre>
 *
 * Created by S.Violet on 2016/1/13.
 */
public abstract class TLogger {

    private static final String DEFAULT_TAG = "Turquoise";

    public static final int ALL = 0xffffffff;//打印所有日志级别
    public static final int NULL = 0x00000000;//不打印日志
    public static final int ERROR = 0x00000001;//打印ERROR日志
    public static final int WARNING = 0x00000010;//打印WARNING日志
    public static final int INFO = 0x00000100;//打印INFO日志
    public static final int DEBUG = 0x00001000;//打印DEBUG日志

    private static TLoggerModule module = new SimpleTLoggerModule();
    private static int globalLevel = ERROR | WARNING | INFO | DEBUG;
    private static String defaultTag = DEFAULT_TAG;

    /**
     * 获得日志打印器实例(默认标签)
     * @param host 信息发送者标识, 通常为打印日志的当前类
     * @return 日志打印器(代理)
     */
    public static TLogger get(Object host){
        return get(host, null);
    }

    /**
     * 获得日志打印器实例(默认标签)
     * @param host 信息发送者标识, 通常为打印日志的当前类
     * @return 日志打印器(代理)
     */
    public static TLogger get(Class host){
        return get(host, null);
    }

    /**
     * 获得日志打印器实例
     * @param host 信息发送者标识, 通常为打印日志的当前类
     * @param tag 自定义标签
     * @return 日志打印器(代理)
     */
    public static TLogger get(Object host, String tag){
        Class hostClass = null;
        if (host != null)
            hostClass = host.getClass();
        return get(hostClass, tag);
    }

    /**
     * 获得日志打印器实例
     * @param host 信息发送者标识, 通常为打印日志的当前类
     * @param tag 自定义标签
     * @return 日志打印器(代理)
     */
    public static TLogger get(Class host, String tag){
        return new TLoggerProxy(host, tag);
    }

    /**
     * @return 获得一个不打印任何内容的日志打印器
     */
    public static TLogger getNullLogger(){
        return TLoggerProxy.LOGGER_NULL;
    }

    /**************************************************
     * setting
     */

    /**
     * 安装日志打印器模块<br/>
     * 所有TLogger实例会替换为自定义打印逻辑, 包括在安装前用TLogger.get(this)方法获取的实例.<br/>
     * @param module
     */
    public static void install(TLoggerModule module){
        TLogger.module = module;
    }

    /**
     * 设置全局日志打印级别<br/>
     * @param globalLevel 例: TLogger.ERROR | TLogger.INFO
     */
    public static void setGlobalLevel(int globalLevel){
        TLogger.globalLevel = globalLevel;
    }

    /**
     * 设置默认日志打印标签(Android的tag)
     * @param defaultTag 默认标签
     */
    public static void setDefaultTag(String defaultTag){
        TLogger.defaultTag = defaultTag;
    }

    /**************************************************
     * abstract
     */

    /**
     * 错误日志<p/>
     *
     * 用于严重的错误.
     *
     * @param msg 错误信息
     */
    public abstract void e(String msg);

    /**
     * 错误日志<p/>
     *
     * 用于严重的错误.
     *
     * @param msg 错误信息
     * @param t 异常
     */
    public abstract void e(String msg, Throwable t);

    /**
     * 错误日志<p/>
     *
     * 用于严重的错误.
     *
     * @param t 异常
     */
    public abstract void e(Throwable t);

    /**
     * 警告日志<p/>
     *
     * 用于错误, 但不至于崩溃或影响使用. 通常在生产环境关闭.
     *
     * @param msg 错误信息
     */
    public abstract void w(String msg);

    /**
     * 警告日志<p/>
     *
     * 用于错误, 但不至于崩溃或影响使用. 通常在生产环境关闭.
     *
     * @param msg 错误信息
     * @param t 异常
     */
    public abstract void w(String msg, Throwable t);

    /**
     * 警告日志<p/>
     *
     * 用于错误, 但不至于崩溃或影响使用. 通常在生产环境关闭.
     *
     * @param t 异常
     */
    public abstract void w(Throwable t);

    /**
     * info日志<p/>
     *
     * 用于非敏感信息打印, 少量必要信息打印.
     *
     * @param msg 信息
     */
    public abstract void i(String msg);

    /**
     * debug日志<p/>
     *
     * 用于敏感信息打印, 大量调试信息打印, 通常在生产环境关闭.
     *
     * @param msg 信息
     */
    public abstract void d(String msg);

    /**
     * @return true:空日志打印器
     */
    public abstract boolean isNullLogger();

    /**************************************************
     * public
     */

    /**
     * @return 获取日志打印器实现类
     */
    public static TLoggerModule getModule(){
        return module;
    }

    /**
     * @return 全局日志级别
     */
    public static int getGlobalLevel(){
        return globalLevel;
    }

    /**
     * @return 默认日志TAG
     */
    public static String getDefaultTag(){
        return defaultTag;
    }

}
