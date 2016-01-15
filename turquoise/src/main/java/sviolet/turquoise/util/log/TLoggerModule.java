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

package sviolet.turquoise.util.log;

import sviolet.turquoise.util.log.def.SimpleTLoggerModule;

/**
 * 日志打印器模块(实现)<p/>
 *
 * 日志打印器实际逻辑由此接口实现<p/>
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
 * Created by S.Violet on 2016/1/14.
 */
public interface TLoggerModule {

    /**
     * 错误日志
     * @param host 信息发送者标识(nullable)
     * @param msg 错误信息
     */
    void e(Class host, String tag, String msg);

    /**
     * 错误日志
     * @param host 信息发送者标识(nullable)
     * @param msg 错误信息
     * @param t 异常
     */
    void e(Class host, String tag, String msg, Throwable t);

    /**
     * 错误日志
     * @param host 信息发送者标识(nullable)
     * @param t 异常
     */
    void e(Class host, String tag, Throwable t);

    /**
     * 警告日志
     * @param host 信息发送者标识(nullable)
     * @param msg 错误信息
     */
    void w(Class host, String tag, String msg);

    /**
     * 警告日志
     * @param host 信息发送者标识(nullable)
     * @param msg 错误信息
     * @param t 异常
     */
    void w(Class host, String tag, String msg, Throwable t);

    /**
     * 警告日志
     * @param host 信息发送者标识(nullable)
     * @param t 异常
     */
    void w(Class host, String tag, Throwable t);

    /**
     * info日志
     * @param host 信息发送者标识(nullable)
     * @param msg 信息
     */
    void i(Class host, String tag, String msg);

    /**
     * debug日志
     * @param host 信息发送者标识(nullable)
     * @param msg 信息
     */
    void d(Class host, String tag, String msg);

}
