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

package sviolet.turquoise.utilx.tlogger;

import java.util.Map;

/**
 * <p>TLogger日志打印器Java入口 (Kotlin参考TLoggerExtensions.kt)</p>
 *
 * <p>日志级别配置==============================================</p>
 *
 * <p>注意:请在Application初始化时配置日志级别. 若先获取打印器打印日志, 再配置, 新配置将不会生效.</p>
 *
 * <pre>{@code
 *  @ApplicationSettings(
 *      DEBUG = true
 *  )
 *  @ReleaseSettings(
 *      logGlobalLevel = TLogger.ERROR | TLogger.INFO
 *  )
 *  @DebugSettings(
 *      logGlobalLevel = TLogger.ERROR | TLogger.INFO | TLogger.WARNING | TLogger.DEBUG
 *  )
 *  public class MyApplication extends TApplicationForMultiDex {
 *      protected void afterCreate() {
 *          super.afterCreate();
 *
 *          Map<String, Integer> rules = new HashMap<>(3);
 *          rules.put("sviolet.turquoise", TLogger.ALL);//sviolet.turquoise包名的类打印全部日志
 *          rules.put("sviolet.turquoise.x.imageloader", TLogger.ERROR | TLogger.INFO);//sviolet.turquoise.x.imageloader包名的类打印ERROR和INFO日志
 *          TLogger.addRules(rules);
 *      }
 *  }
 * }</pre>
 *
 * <p>日志打印==============================================</p>
 *
 * <pre>{@code
 *  public class MyActivity extends TActivity{
 *
 *      private TLogger logger = TLogger.get(this);
 *
 *      public void function(){
 *          logger.d("message");
 *      }
 *
 *  }
 * }</pre>
 *
 * Created by S.Violet on 2017/5/23.
 */
public abstract class TLogger {

    public static final int ALL = 0xffffffff;//打印所有日志级别
    public static final int NULL = 0x00000000;//不打印日志
    public static final int ERROR = 0x00000001;//打印ERROR日志
    public static final int WARNING = 0x00000010;//打印WARNING日志
    public static final int INFO = 0x00000100;//打印INFO日志
    public static final int DEBUG = 0x00001000;//打印DEBUG日志

    /**
     * 获得日志打印器实例
     * @param host 信息发送者标识, 通常为打印日志的当前类
     * @return 日志打印器(代理)
     */
    public static TLogger get(Object host){
        return TLoggerCenter.INSTANCE.newLogger(host);
    }

    /**
     * 获得日志打印器实例
     * @param host 信息发送者标识, 通常为打印日志的当前类
     * @return 日志打印器(代理)
     */
    public static TLogger get(Class host){
        return TLoggerCenter.INSTANCE.newLogger(host);
    }

    /**
     * 设置全局日志级别
     * @param level 日志级别, 例如TLogger.ERROR | TLogger.INFO
     */
    public static void setGlobalLevel(int level){
        TLoggerCenter.INSTANCE.setGlobalLevel(level);
    }

    /**
     * 设置规则
     * @param rules Map<String, Integer>, {{"sviolet.turquoise.x.imageloader", TLogger.ERROR | TLogger.INFO}, {"sviolet.turquoise", TLogger.ALL}}
     */
    public static void addRules(Map<String, Integer> rules){
        TLoggerCenter.INSTANCE.addRules(rules);
    }

    /**
     * 重新设置规则
     * @param rules Map<String, Integer>, {{"sviolet.turquoise.x.imageloader", TLogger.ERROR | TLogger.INFO}, {"sviolet.turquoise", TLogger.ALL}}
     */
    public static void resetRules(Map<String, Integer> rules){
        TLoggerCenter.INSTANCE.resetRules(rules);
    }

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
     * 检查某个日志级别是否允许打印
     * @param level 日志级别， 例如TLogger.DEBUG
     * @return true:允许打印
     */
    public abstract boolean checkEnable(int level);

}
