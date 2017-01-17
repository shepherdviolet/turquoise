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
 * <p>超轻量级事件总线</p>
 *
 * <p>在事件总线中, 我们用消息的类型(Class)来标识消息, 相同类型(Class)的消息, 视为相同的消息.</p>
 *
 * <pre>{@code
 *      //消息实际上是一个JavaBean, 这里要求消息必须实现EvBean接口
 *      public static class Bean implements EvBean{
 *          private String value;
 *          public String getValue() {
 *              return value;
 *          }
 *          public void setValue(String value) {
 *              this.value = value;
 *          }
 *      }
 * }</pre>
 *
 * <p>用法1+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++</p>
 *
 * <p>先在一个Activity或Fragment中注册一个接收器, EvBus.Type指定处理方式, EvReceiver的泛型指定接受消息类
 * 型(JavaBean).</p>
 *
 * <pre>{@code
 *      //注册接收器, 接受GuideActivity.Bean类型的消息, 收到消息后在onStart之后处理
 *      EvBus.register(this, EvBus.Type.ON_START, new EvReceiver<GuideActivity.Bean>(){
 *          protected void onReceive(GuideActivity.Bean message) {
 *              Toast.makeText(GuideActivity.this, message.value, Toast.LENGTH_SHORT).show();
 *          }
 *      });
 * }</pre>
 *
 * <p>确保接收器已经注册好. 实例化JavaBean, 调用EvBus.post()方法发送消息, 所有注册了该JavaBean类型的接收
 * 器都会收到消息, 具体的调用时机根据EvBus.Type指定.</p>
 *
 * <pre>{@code
 *      //实例化JavaBean, 类型与EvReceiver的泛型相符
 *      GuideActivity.Bean bean = new GuideActivity.Bean();
 *      bean.value = "hello world";
 *      //发送消息
 *      EvBus.post(bean);
 * }</pre>
 *
 * <p>注意:这种方式必须先注册接收器, 再发送消息, 否则将不会收到消息. EvReceiver接收器的泛型必须指定, 且与
 * 发送的消息类型相符.</p>
 *
 * <p>用法2+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++</p>
 *
 * <p>先实例化一个JavaBean, 调用EvBus.store()方法储存一个消息.</p>
 *
 * <pre>{@code
 *      //实例化JavaBean
 *      Bean bean = new Bean();
 *      bean.value = "stored message";
 *      //储存消息
 *      EvBus.store(this, bean);
 * }</pre>
 *
 * <p>调用EvBus.withdrawLastOne()方法, 参数指定要提取的消息类型, 程序会提取所有用EvBus.store()方法存入的
 * 指定JavaBean类型的消息, 并返回最新的一个(最后存入的消息), 其余的消息会被废弃. 你也可以使用EvBus.withdraw()
 * 方法提取指定JavaBean类型的所有消息, 结果将返回一个List. 无论哪种方法, 指定类型的消息都会被一次性消费掉,
 * 再次提取将返回空值.</p>
 *
 * <pre>{@code
 *      //指定JavaBean类型, 提取最新一个消息
 *      GuideActivity.Bean receivedBean = EvBus.withdrawLastOne(GuideActivity.Bean.class);
 * }</pre>
 *
 * Created by S.Violet on 2017/1/16.
 */
public class EvBus {

    /**
     * 发送消息, 在发送前注册(register)的接收器都会收到这个消息
     * @param message 消息, 类型必须匹配接收器指定的类型
     */
    public static void post(EvBean message){
        if (message == null){
            return;
        }
        EvCenter.INSTANCE.post(message);
    }

    /**
     * 注册消息接收器
     * @param activity Activity
     * @param type 接收方式
     * @param receiver 接收器, 必须用泛型指定接受的消息类型
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
     * 注册消息接收器
     * @param fragment fragment
     * @param type 接收方式
     * @param receiver 接收器, 必须用泛型指定接受的消息类型
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
     * 注册消息接收器
     * @param activity Activity
     * @param type 接收方式
     * @param receiver 接收器, 必须用泛型指定接受的消息类型
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
     * 注册消息接收器
     * @param fragment fragment
     * @param type 接收方式
     * @param receiver 接收器, 必须用泛型指定接受的消息类型
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

    /**
     * 接收器处理方式
     */
    public enum Type{

        CURR_THREAD,//当前线程处理消息(调用EvBus.post的线程)
        UI_THREAD,//UI线程处理消息
        ON_START,//onStart之后处理消息
        ON_RESUME,//onResume之后处理消息
        ON_PAUSE,//onPause之后处理消息
        ON_STOP,//onStop之后处理消息
        ON_DESTROY//onDestroy之后处理消息

    }

    /******************************************************************************
     *
     */

    /**
     * 提取消息, 只能提取之前储存(store)的消息
     * @param messageType 指定提取的消息类型
     * @return 之前储存的所有指定类型的消息
     */
    public static <T extends EvBean> List<T> withdraw(Class<T> messageType){
        if (messageType == null){
            throw new IllegalArgumentException("[EvBus]messageType == null");
        }

        return (List<T>) EvCenter.INSTANCE.withdraw(messageType);
    }

    /**
     * 提取最新一个消息, 只能提取之前储存(store)的消息, 方法实际上会提取全部指定类型的消息, 其余消息会被废弃
     * @param messageType 指定提取的消息类型
     * @return 最新储存的一个消息
     */
    public static <T extends EvBean> T withdrawLastOne(Class<T> messageType){
        if (messageType == null){
            throw new IllegalArgumentException("[EvBus]messageType == null");
        }

        List<T> list = withdraw(messageType);
        if(list.size() <= 0){
            return null;
        }
        return list.get(0);
    }

    /**
     * 储存消息, 储存的消息只能被提取一次, 提取时会从消息池删除
     * @param activity activity
     * @param message 消息
     */
    public static void store(Activity activity, EvBean message){
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]activity == null");
        }
        if (message == null){
            return;
        }

        EvCenter.INSTANCE.store(activity, message);
    }

    /**
     * 储存消息, 储存的消息只能被提取一次, 提取时会从消息池删除
     * @param fragment fragment
     * @param message 消息
     */
    public static void store(Fragment fragment, EvBean message){
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]fragment == null");
        }
        if (message == null){
            return;
        }

        EvCenter.INSTANCE.store(fragment, message);
    }

    /**
     * 储存消息, 储存的消息只能被提取一次, 提取时会从消息池删除
     * @param activity activity
     * @param message 消息
     */
    public static void store(android.support.v4.app.FragmentActivity activity, EvBean message){
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]activity == null");
        }
        if (message == null){
            return;
        }

        EvCenter.INSTANCE.store(activity, message);
    }

    /**
     * 储存消息, 储存的消息只能被提取一次, 提取时会从消息池删除
     * @param fragment fragment
     * @param message 消息
     */
    public static void store(android.support.v4.app.Fragment fragment, EvBean message){
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]fragment == null");
        }
        if (message == null){
            return;
        }

        EvCenter.INSTANCE.store(fragment, message);
    }


}
