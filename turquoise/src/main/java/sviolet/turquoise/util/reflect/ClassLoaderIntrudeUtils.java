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

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.lang.reflect.Field;

import dalvik.system.BaseDexClassLoader;

/**
 * <p>ClassLoader侵入工具</p>
 *
 * Created by S.Violet on 2016/6/14.
 */
public class ClassLoaderIntrudeUtils {

    /**
     * 在原ClassLoader(originClassLoader)和它的父ClassLoader之间, 插入一个ClassLoader(intrusionClassLoader),
     * intrusionClassLoader作为originClassLoader的父级存在.
     *
     * @param originClassLoader 原ClassLoader
     * @param intrusionClassLoader 侵入的ClassLoader
     * @param clonePathList true:克隆originClassLoader的pathList到intrusionClassLoader中
     * @throws IntrusionException 侵入异常
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void intrudeBaseDexClassLoader(BaseDexClassLoader originClassLoader, BaseDexClassLoader intrusionClassLoader, boolean clonePathList) throws IntrusionException {
        try {
            //克隆dexPathList
            if (clonePathList) {
                Field dexPathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
                dexPathListField.setAccessible(true);
                dexPathListField.set(intrusionClassLoader, dexPathListField.get(originClassLoader));
            }

            //替换parent
            Field parentField = ClassLoader.class.getDeclaredField("parent");
            parentField.setAccessible(true);
            parentField.set(intrusionClassLoader, originClassLoader.getParent());
            parentField.set(originClassLoader, intrusionClassLoader);
        } catch (Exception e) {
            throw new IntrusionException("BaseDexClassLoader intrude failed", e);
        }
    }

}
