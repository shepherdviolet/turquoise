package sviolet.turquoise.enhance.utils;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sviolet.turquoise.enhance.annotation.inject.OnClickMethod;
import sviolet.turquoise.enhance.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.InjectException;

/**
 * Activity的注释式注入工具<br/>
 * <br/>
 * 1.根据"类"的@ResourceId注释注入对应的Activity布局文件<br/>
 * 2.根据"成员变量"的@ResourceId注释注入布局中对应的View对象<br/>
 * 3.根据"成员方法"的@OnClickMethod注释绑定对应View的点击监听器, 方法仅有一个View参数时传入OnClickListener的View参数<Br/>
 *
 * Created by S.Violet on 2015/8/3.
 */
public class InjectUtils {

    /**
     * 全部注入
     * @param activity
     */
    public static void inject(Activity activity){
        InjectUtils.injectClassAnnotation(activity);// 注入类的注释
        InjectUtils.injectFieldAnnotation(activity);// 注入成员变量的注释
        InjectUtils.injectMethodAnnotation(activity);// 注入成员方法的注释
    }

    /**
     * 根据"类"的注释注入<Br/>
     * 1.根据"类"的@ResourceId注释注入对应的Activity布局文件<br/>
     */
    public static void injectClassAnnotation(Activity activity) {
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
     * 根据成员变量的注释注入<br/>
     * 2.根据"成员变量"的@ResourceId注释注入布局中对应的View对象<br/>
     */
    public static void injectFieldAnnotation(Activity activity){
        injectFieldAnnotation(activity, activity.getClass());
    }

    /**
     * 根据成员变量的注释注入<br/>
     */
    private static void injectFieldAnnotation(Activity activity, Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        int resourceId;
        View view;
        for (Field field : fields) {
            if (field.isAnnotationPresent(ResourceId.class)) {
                try {
                    resourceId = field.getAnnotation(ResourceId.class).value();
                    view = activity.findViewById(resourceId);
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
            injectFieldAnnotation(activity, superClazz);
        }
    }

    /**
     * 根据成员方法的注释注入<br/>
     * 3.根据"成员方法"的@OnClickMethod注释绑定对应View的点击监听器<Br/>
     */
    public static void injectMethodAnnotation(Activity activity){
        injectMethodAnnotation(activity, activity.getClass());
    }

    /**
     * 根据成员方法的注释注入<br/>
     */
    private static void injectMethodAnnotation(final Activity activity, Class<?> clazz){
        Method[] methods = clazz.getDeclaredMethods();
        int resourceId;
        View view;
        for (Method method : methods) {
            if (method.isAnnotationPresent(OnClickMethod.class)){
                resourceId = method.getAnnotation(OnClickMethod.class).value();
                view = activity.findViewById(resourceId);
                if (view == null)
                    throw new InjectException("[InjectUtils]inject [" + method.getName() + "]'s OnClickMethod failed, can't find resource");
                final Method finalMethod = method;
                if(view instanceof ListView) {
                    ((ListView)view).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            invokeMethod(finalMethod, activity, view);
                        }
                    });
                }else{
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            invokeMethod(finalMethod, activity, v);
                        }
                    });
                }
            }
        }
        Class superClazz = clazz.getSuperclass();
        if (!Activity.class.equals(superClazz)) {
            injectMethodAnnotation(activity, superClazz);
        }
    }

    /**
     * 执行方法
     *
     * @param finalMethod 方法
     * @param obj 执行的实例
     * @param v 参数(View)
     */
    private static void invokeMethod(Method finalMethod, Object obj, View v) {
        try {
            final Class<?>[] parameterTypes = finalMethod.getParameterTypes();
            finalMethod.setAccessible(true);
            if (parameterTypes.length == 1 && View.class.equals(parameterTypes[0])) {
                finalMethod.invoke(obj, v);
            }else{
                finalMethod.invoke(obj, new Object[parameterTypes.length]);
            }
        }catch(IllegalAccessException e){
            throw new InjectException("[InjectUtils]invoke method [" + finalMethod.getName() + "] failed");
        }catch(InvocationTargetException e){
            throw new InjectException("[InjectUtils]invoke method [" + finalMethod.getName() + "] failed");
        }
    }

}
