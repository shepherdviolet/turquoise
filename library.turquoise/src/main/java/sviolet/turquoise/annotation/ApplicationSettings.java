package sviolet.turquoise.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * [TApplication注释]Application设置
 *
 * @author S.Violet
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ApplicationSettings {

    /**
     * 调试模式, 设置为true, 则应用DebugSettings设置, 设置为false,
     * 则应用ReleaseSettings设置
     */
    boolean DEBUG() default false;

}
