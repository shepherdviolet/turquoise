/*
 * Copyright (C) 2015-2017 S.Violet
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

package sviolet.turquoise.utilx.eventbus;

import android.app.Activity;
import android.app.Fragment;
import android.os.Looper;

/**
 * 事件总线
 *
 * Created by S.Violet on 2017/1/16.
 */
public class EvBus {

    /**
     * 推送消息
     * @param id 消息ID
     * @param message 消息
     */
    public static void post(String id, Object message){
        EvCenter.INSTANCE.post(id, message);
    }

    /**
     * [UI线程调用]注册消息
     * @param activity Activity
     * @param id 消息ID
     * @param type 接收方式
     * @param receiver 接收器
     */
    public static void register(Activity activity, String id, Type type, EvReceiver receiver){
        if (Looper.getMainLooper() != Looper.myLooper()){
            throw new RuntimeException("[EvBus]you must call register method in ui thread");
        }
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]activity == null");
        }
        if (id == null){
            throw new IllegalArgumentException("[EvBus]id == null");
        }
        if (type == null){
            throw new IllegalArgumentException("[EvBus]type == null");
        }
        if (receiver == null){
            return;
        }
        EvCenter.INSTANCE.register(activity, id, type, receiver);
    }

    /**
     * [UI线程调用]注册消息
     * @param fragment fragment
     * @param id 消息ID
     * @param type 接收方式
     * @param receiver 接收器
     */
    public static void register(Fragment fragment, String id, Type type, EvReceiver receiver){
        if (Looper.getMainLooper() != Looper.myLooper()){
            throw new RuntimeException("[EvBus]you must call register method in ui thread");
        }
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]fragment == null");
        }
        if (id == null){
            throw new IllegalArgumentException("[EvBus]id == null");
        }
        if (type == null){
            throw new IllegalArgumentException("[EvBus]type == null");
        }
        if (receiver == null){
            return;
        }
        EvCenter.INSTANCE.register(fragment, id, type, receiver);
    }

    /**
     * [UI线程调用]注册消息
     * @param activity Activity
     * @param id 消息ID
     * @param type 接收方式
     * @param receiver 接收器
     */
    public static void register(android.support.v4.app.FragmentActivity activity, String id, Type type, EvReceiver receiver){
        if (Looper.getMainLooper() != Looper.myLooper()){
            throw new RuntimeException("[EvBus]you must call register method in ui thread");
        }
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]activity == null");
        }
        if (id == null){
            throw new IllegalArgumentException("[EvBus]id == null");
        }
        if (type == null){
            throw new IllegalArgumentException("[EvBus]type == null");
        }
        if (receiver == null){
            return;
        }
        EvCenter.INSTANCE.register(activity, id, type, receiver);
    }

    /**
     * [UI线程调用]注册消息
     * @param fragment fragment
     * @param id 消息ID
     * @param type 接收方式
     * @param receiver 接收器
     */
    public static void register(android.support.v4.app.Fragment fragment, String id, Type type, EvReceiver receiver){
        if (Looper.getMainLooper() != Looper.myLooper()){
            throw new RuntimeException("[EvBus]you must call register method in ui thread");
        }
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]fragment == null");
        }
        if (id == null){
            throw new IllegalArgumentException("[EvBus]id == null");
        }
        if (type == null){
            throw new IllegalArgumentException("[EvBus]type == null");
        }
        if (receiver == null){
            return;
        }
        EvCenter.INSTANCE.register(fragment, id, type, receiver);
    }

    public enum Type{

        CURR_THREAD,
        UI_THREAD,
        ON_START,
        ON_RESUME,
        ON_PAUSE,
        ON_STOP,
        ON_DESTROY

    }

}
