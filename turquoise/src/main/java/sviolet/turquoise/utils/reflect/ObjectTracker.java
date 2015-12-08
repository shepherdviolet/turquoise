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
 *      //JavaBean
 *      Bean bean1 = new Bean();
 *      Bean bean2 = new Bean();
 *      Bean bean3 = new Bean();
 *      ...
 *      //访问路径(键组)
 *      ObjectTracker.KeyGroup keyGroup = new ObjectTracker.KeyGroup("elements.0.name");
 *      //ObjectTracker.KeyGroup keyGroup = new ObjectTracker.KeyGroup(new String[]{"elements", "2", "name"});
 *      ...
 *      //获取JavaBean的成员
 *      //keyGroup内部会持有JavaBean的Field, 使用同一个keyGroup反复获取能够提升性能
 *      Object name1 = ObjectTracker.get(bean1, keyGroup);
 *      Object name2 = ObjectTracker.get(bean2, keyGroup);
 *      Object name3 = ObjectTracker.get(bean3, keyGroup);
 * }</pre>
 *
 * Created by S.Violet on 2015/11/3.
 */
public class ObjectTracker {

    /**
     * 获取源对象的成员变量
     *
     * @param source 源对象
     * @param keyGroup 访问路径(键组)
     * @exception Exception 获取失败抛出异常
     */
    public static Object get(Object source, KeyGroup keyGroup) throws Exception {
        try {
            return innerGet(source, keyGroup);
        }catch(Exception e){
            throw new Exception("[ObjectTracker]illegal keyGroup:<" + keyGroup + ">", e);
        }
    }

    /**
     * 获取源对象的成员变量
     *
     * @param source 源对象
     * @param keyGroup 访问路径(键组)
     * @exception Exception 获取失败抛出异常
     */
    private static Object innerGet(Object source, KeyGroup keyGroup) throws Exception {
        if (keyGroup == null){
            throw new Exception("[ObjectTracker]illegal keyGroup, keyGroup must not be null");
        }
        if (keyGroup.keys == null){
            throw new Exception("[ObjectTracker]illegal keyGroup:<" + keyGroup.illegalKeyGroup + ">, keyGroup must not be null or empty");
        }

        //当前访问对象
        Object current = source;
        //遍历键组
        for(int i = 0 ; i < keyGroup.keys.length ; i++){
            /*
                对象为空
             */
            if (current == null) {
                //元素不存在
                return null;
            }
            /*
                对象为Map
             */
            else if(current instanceof Map){
                current = ((Map) current).get(keyGroup.keys[i]);
            }
            /*
                对象为List
             */
            else if (current instanceof List){
                //编号
                int index;
                try{
                    index = Integer.parseInt(keyGroup.keys[i]);
                }catch(Exception e){
                    throw new Exception("[ObjectTracker]illegal key:<" + keyGroup.keys[i] + ">, key must be an integer while in List element", e);
                }
                //获取对象
                try {
                    current = ((List) current).get(index);
                }catch (Exception e){
                    return null;
                }
            }
            /*
                对象为数组
             */
            else if (current.getClass().isArray()){
                //编号
                int index;
                try{
                    index = Integer.parseInt(keyGroup.keys[i]);
                }catch(Exception e){
                    throw new Exception("[ObjectTracker]illegal key:<" + keyGroup.keys[i] + ">, key must be an integer while in Array element", e);
                }
                //获取对象
                try {
                    current = Array.get(current, index);
                }catch (Exception e){
                    return null;
                }
            }
            /*
                反射方式访问
             */
            else{
                //当前访问的类
                Class currentClass = current.getClass();
                //目标Field, 从键组里获取Field缓存
                Field field = keyGroup.getField(i);
                //若键组里缓存存在Field, 则尝试用该Field获取
                if (field != null){
                    //获取对象
                    try {
                        current = field.get(current);
                    }catch (Exception e){
                        //获取失败, 尝试重新查找Field
                        field = null;
                    }
                }
                //若键组缓存不存在Field, 则查找Field
                while(field == null) {
                    //当前访问类中获取Field
                    try {
                        field = currentClass.getDeclaredField(keyGroup.keys[i]);
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
                    }else{
                        keyGroup.setField(i, field);
                        //获取对象
                        try {
                            current = field.get(current);
                        }catch (Exception e){
                            throw new Exception("[ObjectTracker]can not find target element in map (reflect way), key:<" + keyGroup.keys[i] + ">", e);
                        }
                    }
                }
                //Field不存在
                if (field == null){
                    throw new Exception("[ObjectTracker]illegal key:<" + keyGroup.keys[i] + ">, can not find target field in map (reflect way)");
                }
            }
        }

        return current;
    }

    /**
     * 访问路径(键组)<p/>
     *
     * 内部持有JavaBean的Field, 重复访问可加速<p/>
     */
    public static class KeyGroup{

        private String[] keys;//分割的路径
        private Field[] fields;//分割路径对应的Field
        private String illegalKeyGroup;//非法的路径

        /**
         * @param keyGroup 访问路径(键组), 例:elements.0.name
         * @throws Exception 路径非法抛出异常
         */
        public KeyGroup(String keyGroup) {
            if (keyGroup == null || "".equals(keyGroup)){
                illegalKeyGroup = "null";
                return;
            }
            //根据分隔符切开键组
            String[] keys = keyGroup.split("\\.");
            if (keys.length <= 0){
                illegalKeyGroup = keyGroup;
                return;
            }
            this.keys = keys;
            this.fields = new Field[this.keys.length];
        }

        /**
         *
         * @param keyGroup 访问路径(键组), 例:{"elements","0","name"}
         * @throws Exception 路径非法抛出异常
         */
        public KeyGroup(String[] keyGroup) {
            if (keyGroup == null || keyGroup.length <= 0){
                illegalKeyGroup = "null";
                return;
            }
            this.keys = keyGroup;
            this.fields = new Field[this.keys.length];
        }

        private void setField(int index, Field field){
            if (index < 0 || index >= fields.length)
                return;
            field.setAccessible(true);
            synchronized (this) {
                fields[index] = field;
            }
        }

        private Field getField(int index){
            if (index < 0 || index >= fields.length)
                return null;
            synchronized (this){
                return fields[index];
            }
        }

    }

}
