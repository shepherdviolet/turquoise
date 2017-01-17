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

import java.util.List;

/**
 * 事件总线
 *
 * Created by S.Violet on 2017/1/16.
 */
public class EvBus {

    /**
     * 推送消息
     * @param message 消息
     */
    public static void post(Object message){
        if (message == null){
            return;
        }
        EvCenter.INSTANCE.post(message);
    }

    /**
     * 注册消息
     * @param activity Activity
     * @param type 接收方式
     * @param receiver 接收器
     */
    public static void register(Activity activity, Type type, EvReceiver receiver){
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]activity == null");
        }
        if (type == null){
            throw new IllegalArgumentException("[EvBus]type == null");
        }
        if (receiver == null){
            throw new IllegalArgumentException("[EvBus]receiver == null");
        }
        EvCenter.INSTANCE.register(activity, type, receiver);
    }

    /**
     * 注册消息
     * @param fragment fragment
     * @param type 接收方式
     * @param receiver 接收器
     */
    public static void register(Fragment fragment, Type type, EvReceiver receiver){
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]fragment == null");
        }
        if (type == null){
            throw new IllegalArgumentException("[EvBus]type == null");
        }
        if (receiver == null){
            throw new IllegalArgumentException("[EvBus]receiver == null");
        }
        EvCenter.INSTANCE.register(fragment, type, receiver);
    }

    /**
     * 注册消息
     * @param activity Activity
     * @param type 接收方式
     * @param receiver 接收器
     */
    public static void register(android.support.v4.app.FragmentActivity activity, Type type, EvReceiver receiver){
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]activity == null");
        }
        if (type == null){
            throw new IllegalArgumentException("[EvBus]type == null");
        }
        if (receiver == null){
            throw new IllegalArgumentException("[EvBus]receiver == null");
        }
        EvCenter.INSTANCE.register(activity, type, receiver);
    }

    /**
     * 注册消息
     * @param fragment fragment
     * @param type 接收方式
     * @param receiver 接收器
     */
    public static void register(android.support.v4.app.Fragment fragment, Type type, EvReceiver receiver){
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]fragment == null");
        }
        if (type == null){
            throw new IllegalArgumentException("[EvBus]type == null");
        }
        if (receiver == null){
            throw new IllegalArgumentException("[EvBus]receiver == null");
        }
        EvCenter.INSTANCE.register(fragment, type, receiver);
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

    /******************************************************************************
     *
     */

    public static <T> List<T> withdraw(Class<T> messageType){
        if (messageType == null){
            throw new IllegalArgumentException("[EvBus]messageType == null");
        }

        return (List<T>) EvCenter.INSTANCE.withdraw(messageType);
    }

    public static <T> T withdrawLastOne(Class<T> messageType){
        if (messageType == null){
            throw new IllegalArgumentException("[EvBus]messageType == null");
        }

        List<T> list = withdraw(messageType);
        if(list.size() <= 0){
            return null;
        }
        return list.get(0);
    }

    public static void store(Activity activity, Object message){
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]activity == null");
        }
        if (message == null){
            return;
        }

        EvCenter.INSTANCE.store(activity, message);
    }

    public static void store(Fragment fragment, Object message){
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]fragment == null");
        }
        if (message == null){
            return;
        }

        EvCenter.INSTANCE.store(fragment, message);
    }

    public static void store(android.support.v4.app.FragmentActivity activity, Object message){
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]activity == null");
        }
        if (message == null){
            return;
        }

        EvCenter.INSTANCE.store(activity, message);
    }

    public static void store(android.support.v4.app.Fragment fragment, Object message){
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]fragment == null");
        }
        if (message == null){
            return;
        }

        EvCenter.INSTANCE.store(fragment, message);
    }


}
