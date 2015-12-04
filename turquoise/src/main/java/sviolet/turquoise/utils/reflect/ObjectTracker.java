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

package sviolet.turquoise.utils.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 对象成员访问器<p/>
 *
 * 支持JavaBean/Map/List/Array<p/>
 *
 * JavaBean(无需getter/setter):<br/>
 * <pre>{@code
 *      class Bean{
 *          private List<Element) elements;
 *          ...
 *      }
 *      class Element{
 *          private String name;
 *          ...
 *      }
 * }</pre>
 *
 * 获取成员变量:<br/>
 * <pre>{@code
 *      Bean bean = new Bean();
 *      ...
 *      Object name1 = ObjectTracker.get(bean, "elements.0.name");
 *      Object name2 = ObjectTracker.get(bean, "elements.1.name");
 *      Object name3 = ObjectTracker.get(bean, {"elements", "2", "name"});
 * }</pre>
 *
 * Created by S.Violet on 2015/12/3.
 */
public class ObjectTracker {

    /**
     * 获取源对象的成员变量, 异常返回null
     *
     * @param source 源对象
     * @param keyGroup 访问路径(键组) 例:branches.2.name
     */
    public static Object fetch(Object source, String keyGroup){
        try{
            return get(source, keyGroup);
        }catch (Exception ignored){
        }
        return null;
    }

    /**
     * 获取源对象的成员变量, 异常返回null
     *
     * @param source 源对象
     * @param keyGroup 访问路径(键组) 例:{"branches","2","name"}
     */
    public static Object fetch(Object source, String[] keyGroup){
        try{
            return get(source, keyGroup);
        }catch (Exception ignored){
        }
        return null;
    }

    /**
     * 获取源对象的成员变量
     *
     * @param source 源对象
     * @param keyGroup 访问路径(键组) 例:branches.2.name
     * @exception Exception 获取失败抛出异常
     */
    public static Object get(Object source, String keyGroup) throws Exception {
        if (keyGroup == null || "".equals(keyGroup)){
            throw new Exception("[ObjectTracker]keyGroup must not be null");
        }
        //根据分隔符切开键组
        String[] keys = keyGroup.split("\\.");
        if (keys.length <= 0){
            throw new Exception("[ObjectTracker]illegal keyGroup:<" + keyGroup + ">, no key elements");
        }
        try {
            return get(source, keys);
        }catch(Exception e){
            throw new Exception("[ObjectTracker]illegal keyGroup:<" + keyGroup + ">", e);
        }
    }

    /**
     * 获取源对象的成员变量
     *
     * @param source 源对象
     * @param keyGroup 访问路径(键组) 例:{"branches","2","name"}
     * @exception Exception 获取失败抛出异常
     */
    public static Object get(Object source, String[] keyGroup) throws Exception {
        if (keyGroup == null || keyGroup.length <= 0){
            throw new Exception("[ObjectTracker]keyGroup must not be null or empty");
        }

        //当前访问对象
        Object current = source;
        //遍历键组
        for(String key : keyGroup){
            /*
                对象为空
             */
            if (current == null) {
                throw new Exception("[ObjectTracker]can not find target element in map");
            }
            /*
                对象为Map
             */
            else if(current instanceof Map){
                current = ((Map) current).get(key);
            }
            /*
                对象为List
             */
            else if (current instanceof List){
                //编号
                int index;
                try{
                    index = Integer.parseInt(key);
                }catch(Exception e){
                    throw new Exception("[ObjectTracker]illegal key:<" + key + ">, key must be an integer while in List element", e);
                }
                //获取对象
                try {
                    current = ((List) current).get(index);
                }catch (Exception e){
                    throw new Exception("[ObjectTracker]illegal key:<" + key + ">, index out of bounds", e);
                }
            }
            /*
                对象为数组
             */
            else if (current.getClass().isArray()){
                //编号
                int index;
                try{
                    index = Integer.parseInt(key);
                }catch(Exception e){
                    throw new Exception("[ObjectTracker]illegal key:<" + key + ">, key must be an integer while in Array element", e);
                }
                //获取对象
                try {
                    current = Array.get(current, index);
                }catch (Exception e){
                    throw new Exception("[ObjectTracker]illegal key:<" + key + ">, index out of bounds", e);
                }
            }
            /*
                反射方式访问
             */
            else{
                //当前访问的类
                Class currentClass = current.getClass();
                //目标Field
                Field field = null;
                while(field == null) {
                    //当前访问类中获取Field
                    try {
                        field = currentClass.getDeclaredField(key);
                    } catch (Exception ignored) {
                    }
                    //Field不存在
                    if (field == null){
                        //访问父类
                        currentClass = currentClass.getSuperclass();
                        //若父类为Object则break, 视为找不到Field
                        if (currentClass == null || currentClass == Object.class){
                            break;
                        }
                    }
                }
                //Field不存在
                if (field == null){
                    throw new Exception("[ObjectTracker]illegal key:<" + key + ">, can not find target field in map (reflect way)");
                }
                //获取对象
                try {
                    field.setAccessible(true);
                    current = field.get(current);
                }catch (Exception e){
                    throw new Exception("[ObjectTracker]can not find target element in map (reflect way), key:<" + key + ">", e);
                }
            }
            //对象不存在
            if (current == null) {
                throw new Exception("[ObjectTracker]can not find target element in map, key:<" + key + ">");
            }
        }

        return current;
    }

}
