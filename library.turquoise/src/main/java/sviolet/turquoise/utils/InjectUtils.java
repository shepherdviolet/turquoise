package sviolet.turquoise.utils;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Field;

import sviolet.turquoise.annotation.ResourceId;
import sviolet.turquoise.app.InjectException;

/**
 * Activity的布局/View/监听器注入工具<br/>
 * 根据Activity的@ResourceId / @OnClick ...标签注入对象<br/>
 * <br/>
 * 1.Activity布局文件注入<br>
 * 注入Activity@ResourceId注释对应的布局文件<br>
 * <br>
 * 2.成员View对象注入<br>
 * 注入带@ResourceId注释的成员View对象<br>
 * <br>
 *
 * Created by S.Violet on 2015/8/3.
 */
public class InjectUtils {

    /**
     * 全部注入
     * @param activity
     */
    public static void inject(Activity activity){
        InjectUtils.injectContentView(activity);// 注入Activity布局
        InjectUtils.injectViewId(activity);// 注入成员View
    }

    /**
     * 根据Activity的@ResourceId标签, 注入Activity的布局文件
     */
    public static void injectContentView(Activity activity) {
        if (activity.getClass().isAnnotationPresent(ResourceId.class)) {
            try {
                int layoutId = activity.getClass().getAnnotation(ResourceId.class).value();
                activity.setContentView(layoutId);
            } catch (Exception e) {
                throw new InjectException("[InjectUtils]inject ContentView failed", e);
            }
        }
    }

    /**
     * 根据Activity中成员变量的@ResourceId标签, 注入对应ID的View对象
     */
    public static void injectViewId(Activity activity){
        injectViewId(activity, activity.getClass());
    }

    /**
     * 根据Activity中成员变量的@ResourceId标签, 注入对应ID的View对象
     */
    private static void injectViewId(Activity activity, Class<?> clazz) {

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ResourceId.class)) {
                try {
                    int resourceId = field.getAnnotation(ResourceId.class).value();
                    View view = activity.findViewById(resourceId);
                    if (view == null)
                        throw new InjectException("[InjectUtils]inject view [" + field.getName() + "] failed, can't find resource");
                    field.setAccessible(true);
                    field.set(activity, view);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    throw new InjectException("[InjectUtils]inject view [" + field.getName() + "] failed", e);
                }
            }
        }

        Class superClazz = clazz.getSuperclass();
        if (!Activity.class.equals(superClazz)) {
            injectViewId(activity, superClazz);
        }
    }

}
