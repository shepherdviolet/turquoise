/*
 * Copyright (C) 2015 S.Violet
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

package sviolet.turquoise.enhance.app.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import java.lang.reflect.Field;

import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.util.reflect.ReflectCache;

/**
 * <p>Activity/Dialog的注释式注入工具</p>
 *
 * <p>无需再使用setContentView()和findViewById()方法</p>
 *
 * 1.根据"类"的@ResourceId注释注入对应的Activity/Dialog布局文件<br/>
 * 2.根据"成员变量"的@ResourceId注释注入布局中对应的View对象<br/>
 *
 * Created by S.Violet on 2015/8/3.
 */
public class InjectUtils {

    /**
     * 1.根据"类"的@ResourceId注释注入对应的Activity布局文件<br/>
     * 2.根据"成员变量"的@ResourceId注释注入布局中对应的View对象<br/>
     * @param activity activity
     */
    public static void inject(Activity activity){
        InjectUtils.injectClassAnnotation(activity);// 注入类的注释
        InjectUtils.injectFieldAnnotation(activity);// 注入成员变量的注释
    }

    /**
     * 1.根据"类"的@ResourceId注释注入对应的Activity布局文件<br/>
     * 2.根据"成员变量"的@ResourceId注释注入布局中对应的View对象<br/>
     * @param dialog dialog
     */
    public static void inject(Dialog dialog){
        InjectUtils.injectClassAnnotation(dialog);// 注入类的注释
        InjectUtils.injectFieldAnnotation(dialog);// 注入成员变量的注释
    }

    /**
     * 根据"类"的注释注入<Br/>
     * 1.根据"类"的@ResourceId注释注入对应的Activity布局文件<br/>
     */
    public static void injectClassAnnotation(Activity activity) {
        if (activity == null){
            return;
        }
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
     * 根据"类"的注释注入<Br/>
     * 1.根据"类"的@ResourceId注释注入对应的Dialog布局文件<br/>
     */
    public static void injectClassAnnotation(Dialog dialog) {
        if (dialog == null){
            return;
        }
        if (dialog.getClass().isAnnotationPresent(ResourceId.class)) {
            try {
                int layoutId = dialog.getClass().getAnnotation(ResourceId.class).value();
                dialog.setContentView(layoutId);
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
        if (activity == null){
            return;
        }
        injectFieldAnnotation(activity, activity.getClass(), activityInjectCallback);
    }

    /**
     * 根据成员变量的注释注入<br/>
     * 2.根据"成员变量"的@ResourceId注释注入布局中对应的View对象<br/>
     */
    public static void injectFieldAnnotation(Dialog dialog){
        if (dialog == null){
            return;
        }
        injectFieldAnnotation(dialog, dialog.getClass(), dialogInjectCallback);
    }

    private static void injectFieldAnnotation(Object object, Class<?> clazz, InjectCallback callback) {
        Field[] fields = ReflectCache.getDeclaredFields(clazz);
        int resourceId;
        View view;
        for (Field field : fields) {
            if (field.isAnnotationPresent(ResourceId.class)) {
                try {
                    resourceId = field.getAnnotation(ResourceId.class).value();
                    view = callback.findViewById(object, resourceId);
                    if (view == null)
                        throw new InjectException("[InjectUtils]inject view [" + field.getName() + "] failed, can't find resource");
                    field.setAccessible(true);
                    field.set(object, view);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    throw new InjectException("[InjectUtils]inject view [" + field.getName() + "] failed", e);
                }
            }
        }
        Class superClazz = clazz.getSuperclass();
        if (callback.continueInjectSuper(superClazz)) {
            injectFieldAnnotation(object, superClazz, callback);
        }
    }

    private interface InjectCallback{

        View findViewById(Object object, int resId);

        boolean continueInjectSuper(Class superClazz);

    }

    private static final InjectCallback activityInjectCallback = new InjectCallback() {
        @Override
        public View findViewById(Object object, int resId) {
            return ((Activity)object).findViewById(resId);
        }

        @Override
        public boolean continueInjectSuper(Class superClazz) {
            return !Activity.class.equals(superClazz);
        }
    };

    private static final InjectCallback dialogInjectCallback = new InjectCallback() {
        @Override
        public View findViewById(Object object, int resId) {
            return ((Dialog)object).findViewById(resId);
        }

        @Override
        public boolean continueInjectSuper(Class superClazz) {
            return !Dialog.class.equals(superClazz);
        }
    };

//    /**
//     * 根据成员方法的注释注入<br/>
//     * 3.根据"成员方法"的@OnClickMethod注释绑定对应View的点击监听器<Br/>
//     *
//     * @deprecated i don't like this way
//     */
//    @Deprecated
//    public static void injectMethodAnnotation(Activity activity){
//        injectMethodAnnotation(activity, activity.getClass());
//    }
//
//    /**
//     * 根据成员方法的注释注入<br/>
//     */
//    private static void injectMethodAnnotation(Activity activity, Class<?> clazz){
//        Method[] methods = clazz.getDeclaredMethods();
//        int resourceId;
//        View view;
//        for (Method method : methods) {
//            if (method.isAnnotationPresent(OnClickMethod.class)){
//                resourceId = method.getAnnotation(OnClickMethod.class).value();
//                view = activity.findViewById(resourceId);
//                if (view == null)
//                    throw new InjectException("[InjectUtils]inject [" + method.getName() + "]'s OnClickMethod failed, can't find resource");
//                final Method finalMethod = method;
//                final WeakReference<Activity> weakActivity = new WeakReference<>(activity);
//                view.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Activity activity = weakActivity.get();
//                        if (activity != null) {
//                            invokeMethod(finalMethod, activity, v);
//                        }
//                    }
//                });
//            }
//        }
//        Class superClazz = clazz.getSuperclass();
//        if (!Activity.class.equals(superClazz)) {
//            injectMethodAnnotation(activity, superClazz);
//        }
//    }
//
//    /**
//     * 执行方法
//     *
//     * @param finalMethod 方法
//     * @param obj 执行的实例
//     * @param v 参数(View)
//     */
//    private static void invokeMethod(Method finalMethod, Object obj, View v) {
//        try {
//            final Class<?>[] parameterTypes = finalMethod.getParameterTypes();
//            finalMethod.setAccessible(true);
//            if (parameterTypes.length == 1 && View.class.equals(parameterTypes[0])) {
//                finalMethod.invoke(obj, v);
//            }else{
//                finalMethod.invoke(obj, new Object[parameterTypes.length]);
//            }
//        }catch(IllegalAccessException e){
//            throw new InjectException("[InjectUtils]invoke method [" + finalMethod.getName() + "] failed");
//        }catch(InvocationTargetException e){
//            throw new InjectException("[InjectUtils]invoke method [" + finalMethod.getName() + "] failed");
//        }
//    }

}
