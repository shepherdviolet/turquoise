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

    private static CompatLruCache<String, Field[]> declaredFields = new CompatLruCache<>(DEFAULT_MAX_SIZE);
    private static CompatLruCache<String, Field[]> publicFields = new CompatLruCache<>(DEFAULT_MAX_SIZE);
    private static CompatLruCache<String, Method[]> declaredMethods = new CompatLruCache<>(DEFAULT_MAX_SIZE);
    private static CompatLruCache<String, Method[]> publicMethods = new CompatLruCache<>(DEFAULT_MAX_SIZE);
    private static CompatLruCache<String, Constructor[]> declaredConstructors = new CompatLruCache<>(DEFAULT_MAX_SIZE);
    private static CompatLruCache<String, Constructor[]> publicConstructors = new CompatLruCache<>(DEFAULT_MAX_SIZE);

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

        final CompatLruCache<String, Field[]> cache = declaredFields;
        if (cache == null){
            return clazz.getDeclaredFields();
        }

        final String name = clazz.getName();
        Field[] fields = cache.get(name);
        if (fields == null){
            fields = clazz.getDeclaredFields();
            cache.put(name, fields);
        }
        return fields;
    }

    public static Field[] getFields(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final CompatLruCache<String, Field[]> cache = publicFields;
        if (cache == null){
            return clazz.getFields();
        }

        final String name = clazz.getName();
        Field[] fields = cache.get(name);
        if (fields == null){
            fields = clazz.getFields();
            cache.put(name, fields);
        }
        return fields;
    }

    public static Method[] getDeclaredMethods(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final CompatLruCache<String, Method[]> cache = declaredMethods;
        if (cache == null){
            return clazz.getDeclaredMethods();
        }

        final String name = clazz.getName();
        Method[] methods = cache.get(name);
        if (methods == null){
            methods = clazz.getDeclaredMethods();
            cache.put(name, methods);
        }
        return methods;
    }

    public static Method[] getMethods(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final CompatLruCache<String, Method[]> cache = publicMethods;
        if (cache == null){
            return clazz.getMethods();
        }

        final String name = clazz.getName();
        Method[] methods = cache.get(name);
        if (methods == null){
            methods = clazz.getMethods();
            cache.put(name, methods);
        }
        return methods;
    }

    public static Constructor[] getDeclaredConstructors(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final CompatLruCache<String, Constructor[]> cache = declaredConstructors;
        if (cache == null){
            return clazz.getDeclaredConstructors();
        }

        final String name = clazz.getName();
        Constructor[] constructors = cache.get(name);
        if (constructors == null){
            constructors = clazz.getDeclaredConstructors();
            cache.put(name, constructors);
        }
        return constructors;
    }

    public static Constructor[] getConstructors(Class clazz){
        if (clazz == null){
            throw new NullPointerException("[ReflectCache]class is null");
        }

        final CompatLruCache<String, Constructor[]> cache = publicConstructors;
        if (cache == null){
            return clazz.getConstructors();
        }

        final String name = clazz.getName();
        Constructor[] constructors = cache.get(name);
        if (constructors == null){
            constructors = clazz.getConstructors();
            cache.put(name, constructors);
        }
        return constructors;
    }

}
