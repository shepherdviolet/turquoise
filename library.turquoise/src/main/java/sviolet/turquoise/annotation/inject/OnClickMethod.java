package sviolet.turquoise.annotation.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * [InjectUtils注释]点击事件方法绑定
 * 
 * @author S.Violet
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface OnClickMethod {
	int value() default 0;
}
