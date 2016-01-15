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
 * TLogger使用:<p/>
 *
 * <pre>{@code
 *      public class Demo{
 *          //获得日志打印器实例
 *          private TLogger logger = TLogger.get(this);
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
 * 1.实现{@link TLoggerModule}接口.<br/>
 * 2.TLogger.install(TLoggerModule)方法安装(替换)自定义逻辑模块.<br/>
 * 3.所有TLogger实例会替换为自定义打印逻辑, 包括在安装前用TLogger.get(this)方法获取的实例.<br/>
 *
 * <pre>{@code
 *      //设置
 *      TLogger.setLevelSwitch(TLogger.ERROR | TLogger.INFO);//允许打印ERROR和INFO日志
 *      TLogger.setTag("demo");//设置日志标签
 *      //扩展
 *      TLogger.install(TLoggerModule);//自定义日志打印器模块
 * }</pre>
 *
 * Created by S.Violet on 2016/1/13.
 */
public abstract class TLogger {

    private static final String DEFAULT_TAG = "Turquoise";

    public static final int ALL = 0x11111111;//打印所有日志级别
    public static final int ERROR = 0x00000001;//打印ERROR日志
    public static final int WARNING = 0x00000010;//打印WARNING日志
    public static final int INFO = 0x00000100;//打印INFO日志
    public static final int DEBUG = 0x00001000;//打印DEBUG日志

    private static TLoggerModule module = new SimpleTLoggerModule();
    private static int levelSwitch = ERROR | WARNING | INFO | DEBUG;
    private static String tag = DEFAULT_TAG;

    /**
     * 获得日志打印器实例
     * @param host 信息发送者标识, 通常为打印日志的当前类
     * @return 日志打印器(代理)
     */
    public static TLogger get(Object host){
        Class hostClass = null;
        if (host != null)
            hostClass = host.getClass();
        return new TLoggerProxy(hostClass);
    }

    /**
     * 获得日志打印器实例
     * @param host 信息发送者标识, 通常为打印日志的当前类
     * @return 日志打印器(代理)
     */
    public static TLogger get(Class host){
        return new TLoggerProxy(host);
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
     * 设置日志打印级别<br/>
     * @param levelSwitch 例: TLogger.ERROR | TLogger.INFO
     */
    public static void setLevelSwitch(int levelSwitch){
        TLogger.levelSwitch = levelSwitch;
    }

    /**
     * 设置日志打印标签(Android的tag)
     * @param tag 标签
     */
    public static void setTag(String tag){
        TLogger.tag = tag;
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

    /**************************************************
     * package
     */

    static TLoggerModule getModule(){
        return module;
    }

    static int getLevelSwitch(){
        return levelSwitch;
    }

    static String getTag(){
        return tag;
    }

}
