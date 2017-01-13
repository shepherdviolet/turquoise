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

package sviolet.turquoise.util.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import sviolet.turquoise.common.compat.CompatLruCache;

/**
 * <p>反射缓存, 用于一些反复使用反射的Class</p>
 *
 * Created by S.Violet on 2016/8/5.
 */

public class ReflectCache {

    private static final int DEFAULT_MAX_SIZE = 50;

    private static CompatLruCache<Class, Field[]> declaredFields = new CompatLruCache<>(DEFAULT_MAX_SIZE);
    private static CompatLruCache<Class, Field[]> publicFields = new CompatLruCache<>(DEFAULT_MAX_SIZE);
    private static CompatLruCache<Class, Method[]> declaredMethods = new CompatLruCache<>(DEFAULT_MAX_SIZE);
    private static CompatLruCache<Class, Method[]> publicMethods = new CompatLruCache<>(DEFAULT_MAX_SIZE);
    private static CompatLruCache<Class, Constructor[]> declaredConstructors = new CompatLruCache<>(DEFAULT_MAX_SIZE);
    private static CompatLruCache<Class, Constructor[]> publicConstructors = new CompatLruCache<>(DEFAULT_MAX_SIZE);

    /**
     * 设置缓存大小
     * @param size 若<=0, 则关闭缓存
     */
    public static void setCacheSize(int size){
        if (size <= 0){
            declaredFields = null;
            declaredMethods = null;
            publicFields = null;
            publicMethods = null;
            declaredConstructors = null;
            publicConstructors = null;
        }else {
            declaredFields = new CompatLruCache<>(size);
            declaredMethods = new CompatLruCache<>(size);
            publicFields = new CompatLruCache<>(size);
            publicMethods = new CompatLruCache<>(size);
            declaredConstructors = new CompatLruCache<>(size);
        }   publicConstructors = new CompatLruCache<>(size);
    }

    public static Field[] getDeclaredFields(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final CompatLruCache<Class, Field[]> cache = declaredFields;
        if (cache == null){
            return clazz.getDeclaredFields();
        }

        Field[] fields = cache.get(clazz);
        if (fields == null){
            fields = clazz.getDeclaredFields();
            cache.put(clazz, fields);
        }
        return fields;
    }

    public static Field[] getFields(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final CompatLruCache<Class, Field[]> cache = publicFields;
        if (cache == null){
            return clazz.getFields();
        }

        Field[] fields = cache.get(clazz);
        if (fields == null){
            fields = clazz.getFields();
            cache.put(clazz, fields);
        }
        return fields;
    }

    public static Method[] getDeclaredMethods(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final CompatLruCache<Class, Method[]> cache = declaredMethods;
        if (cache == null){
            return clazz.getDeclaredMethods();
        }

        Method[] methods = cache.get(clazz);
        if (methods == null){
            methods = clazz.getDeclaredMethods();
            cache.put(clazz, methods);
        }
        return methods;
    }

    public static Method[] getMethods(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final CompatLruCache<Class, Method[]> cache = publicMethods;
        if (cache == null){
            return clazz.getMethods();
        }

        Method[] methods = cache.get(clazz);
        if (methods == null){
            methods = clazz.getMethods();
            cache.put(clazz, methods);
        }
        return methods;
    }

    public static Constructor[] getDeclaredConstructors(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final CompatLruCache<Class, Constructor[]> cache = declaredConstructors;
        if (cache == null){
            return clazz.getDeclaredConstructors();
        }

        Constructor[] constructors = cache.get(clazz);
        if (constructors == null){
            constructors = clazz.getDeclaredConstructors();
            cache.put(clazz, constructors);
        }
        return constructors;
    }

    public static Constructor[] getConstructors(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final CompatLruCache<Class, Constructor[]> cache = publicConstructors;
        if (cache == null){
            return clazz.getConstructors();
        }

        Constructor[] constructors = cache.get(clazz);
        if (constructors == null){
            constructors = clazz.getConstructors();
            cache.put(clazz, constructors);
        }
        return constructors;
    }

}
