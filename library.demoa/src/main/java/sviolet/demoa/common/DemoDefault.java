package sviolet.demoa.common;

import android.app.Activity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 默认打开的Activity
 *
 * Created by S.Violet on 2015/6/2.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DemoDefault {
    Class<? extends Activity> value();
}