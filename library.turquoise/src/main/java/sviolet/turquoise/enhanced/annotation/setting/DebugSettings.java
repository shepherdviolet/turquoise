package sviolet.turquoise.enhanced.annotation.setting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * [TApplication注释]调试模式参数设置
 *
 * @author S.Violet
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DebugSettings {

    /**
     * 启用策略检测
     */
    boolean enableStrictMode() default false;

    /**
     * 启用崩溃自启
     */
    boolean enableCrashRestart() default false;

    /**
     * 启用崩溃处理
     */
    boolean enableCrashHandle() default false;

    /**
     * 日志标签
     */
    String logTag() default "Undefined";

    /**
     * 允许Debug日志打印
     */
    boolean enableLogDebug() default false;

    /**
     * 允许Error日志打印
     */
    boolean enableLogError() default false;

    /**
     * 允许Info日志打印
     */
    boolean enableLogInfo() default false;

}
