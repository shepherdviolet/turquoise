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
import android.app.Application;
import android.app.Fragment;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.util.common.ConcurrentUtils;
import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.utilx.lifecycle.LifeCycle;
import sviolet.turquoise.utilx.lifecycle.LifeCycleUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * <p>超轻量级事件总线</p>
 *
 * <p>在事件总线中, 我们用消息的类型(Class)来标识消息, 相同类型(Class)的消息, 视为相同的消息.</p>
 *
 * <pre>{@code
 *      //消息实际上是一个JavaBean, 这里要求消息必须实现EvMessage接口
 *      public static class Bean implements EvMessage{
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
 *              Toast.makeText(GuideActivity.this, message.getValue(), Toast.LENGTH_SHORT).show();
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
 *      bean.setValue("hello world");
 *      //发送消息
 *      EvBus.post(bean);
 * }</pre>
 *
 * <p>注意:这种方式必须先注册接收器, 再发送消息, 否则将不会收到消息. EvReceiver接收器的泛型必须指定, 且与
 * 发送的消息类型相符.</p>
 *
 * Created by S.Violet on 2017/1/16.
 */
public class EvBus {

    private static final String COMPONENT_ID = "TURQUOISE_EV_BUS_STATION_COMPONENT";

    private static final TLogger logger = TLogger.get(EvBus.class);
    private static final ReentrantLock lock = new ReentrantLock();

    private static final Set<EvStation> stations = Collections.newSetFromMap(new WeakHashMap<EvStation, Boolean>());

    /**
     * 发送消息, 在发送前注册(register)的接收器都会收到这个消息
     * @param message 消息, 类型必须匹配接收器指定的类型
     */
    public static void post(EvMessage message){
        if (message == null){
            return;
        }
        boolean result = false;
        //遍历所有station并推送消息
        for (EvStation station : ConcurrentUtils.getSnapShot(stations)){
            if (station.post(message)){
                result = true;
            }
        }
        if (!result){
            logger.d("no receiver handle this message, messageClass:" + message.getClass() + ", message:" + message);
        }
    }

    /**
     * 注册消息接收器
     * @param activity Activity
     * @param type 接收方式
     * @param receiver 接收器, 必须用泛型指定接受的消息类型
     */
    public static void register(Activity activity, Type type, EvReceiver receiver){
        check(activity, type, receiver);
        getStation(activity, true).register(type, receiver);
    }

    /**
     * 注册消息接收器
     * @param fragment fragment
     * @param type 接收方式
     * @param receiver 接收器, 必须用泛型指定接受的消息类型
     */
    public static void register(Fragment fragment, Type type, EvReceiver receiver){
        check(fragment, type, receiver);
        getStation(fragment, true).register(type, receiver);
    }

    /**
     * 注册消息接收器
     * @param activity Activity
     * @param type 接收方式
     * @param receiver 接收器, 必须用泛型指定接受的消息类型
     */
    public static void register(android.support.v4.app.FragmentActivity activity, Type type, EvReceiver receiver){
        check(activity, type, receiver);
        getStation(activity, true).register(type, receiver);
    }

    /**
     * 注册消息接收器
     * @param fragment fragment
     * @param type 接收方式
     * @param receiver 接收器, 必须用泛型指定接受的消息类型
     */
    public static void register(android.support.v4.app.Fragment fragment, Type type, EvReceiver receiver){
        check(fragment, type, receiver);
        getStation(fragment, true).register(type, receiver);
    }

    /**
     * 反注册消息接收器
     * @param activity activity
     * @param messageClass 消息类型
     */
    public static void unregister(Activity activity, Class<? extends EvMessage> messageClass) {
        check(activity, messageClass);
        getStation(activity, true).unregister(messageClass);
    }

    /**
     * 反注册消息接收器
     * @param fragment fragment
     * @param messageClass 消息类型
     */
    public static void unregister(Fragment fragment, Class<? extends EvMessage> messageClass) {
        check(fragment, messageClass);
        getStation(fragment, true).unregister(messageClass);
    }

    /**
     * 反注册消息接收器
     * @param activity activity
     * @param messageClass 消息类型
     */
    public static void unregister(android.support.v4.app.FragmentActivity activity, Class<? extends EvMessage> messageClass) {
        check(activity, messageClass);
        getStation(activity, true).unregister(messageClass);
    }

    /**
     * 反注册消息接收器
     * @param fragment fragment
     * @param messageClass 消息类型
     */
    public static void unregister(android.support.v4.app.Fragment fragment, Class<? extends EvMessage> messageClass) {
        check(fragment, messageClass);
        getStation(fragment, true).unregister(messageClass);
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

    /******************************************************************************************
     * transmit mode
     */

    private static EvTransmitPipeline transmitPipeline;

    /**
     * [API14+]
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void transmitPush(Activity activity, EvMessage message){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]transmitPush can only call in API14+");
        }
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (message == null){
            return;
        }
        getStation(activity, true).pushTransmitMessage(message);
    }

    /**
     * [API14+]
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void transmitPush(Fragment fragment, EvMessage message){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]transmitPush can only call in API14+");
        }
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (message == null){
            return;
        }
        getStation(fragment, true).pushTransmitMessage(message);
    }

    /**
     * [API14+]
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void transmitPush(android.support.v4.app.FragmentActivity activity, EvMessage message){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]transmitPush can only call in API14+");
        }
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (message == null){
            return;
        }
        getStation(activity, true).pushTransmitMessage(message);
    }

    /**
     * [API14+]
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void transmitPush(android.support.v4.app.Fragment fragment, EvMessage message){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]transmitPush can only call in API14+");
        }
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (message == null){
            return;
        }
        getStation(fragment, true).pushTransmitMessage(message);
    }

    /**
     * [API14+]
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitPop(Activity activity, Class<T> messageClass){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]transmitPop can only call in API14+");
        }
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (messageClass == null){
            return null;
        }
        return getStation(activity, true).popTransmitMessage(messageClass);
    }

    /**
     * [API14+]
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitPop(Fragment fragment, Class<T> messageClass){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]transmitRemove can only call in API14+");
        }
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (messageClass == null){
            return null;
        }
        return getStation(fragment, true).popTransmitMessage(messageClass);
    }

    /**
     * [API14+]
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitPop(android.support.v4.app.FragmentActivity activity, Class<T> messageClass){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]transmitRemove can only call in API14+");
        }
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (messageClass == null){
            return null;
        }
        return getStation(activity, true).popTransmitMessage(messageClass);
    }

    /**
     * [API14+]
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitPop(android.support.v4.app.Fragment fragment, Class<T> messageClass){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]transmitRemove can only call in API14+");
        }
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (messageClass == null){
            return null;
        }
        return getStation(fragment, true).popTransmitMessage(messageClass);
    }

    /**
     * [API14+]
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitRemove(Activity activity, Class<T> messageClass){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]transmitRemove can only call in API14+");
        }
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (messageClass == null){
            return null;
        }
        return getStation(activity, true).removeTransmitMessage(messageClass);
    }

    /**
     * [API14+]
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitRemove(Fragment fragment, Class<T> messageClass){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]transmitRemove can only call in API14+");
        }
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (messageClass == null){
            return null;
        }
        return getStation(fragment, true).removeTransmitMessage(messageClass);
    }

    /**
     * [API14+]
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitRemove(android.support.v4.app.FragmentActivity activity, Class<T> messageClass){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]transmitRemove can only call in API14+");
        }
        if (activity == null){
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (messageClass == null){
            return null;
        }
        return getStation(activity, true).removeTransmitMessage(messageClass);
    }

    /**
     * [API14+]
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitRemove(android.support.v4.app.Fragment fragment, Class<T> messageClass){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]transmitRemove can only call in API14+");
        }
        if (fragment == null){
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (messageClass == null){
            return null;
        }
        return getStation(fragment, true).removeTransmitMessage(messageClass);
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Deprecated
    public static void installTransmitPipeline(Application application){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]installTransmitPipeline can only call in API14+");
        }
        if (application == null){
            throw new NullPointerException("[EvBus]can not install TransmitPipeline on null Application");
        }
        if (transmitPipeline != null){
            throw new IllegalStateException("[EvBus]you can only install TransmitPipeline once");
        }
        transmitPipeline = new EvTransmitPipeline();
        application.registerActivityLifecycleCallbacks(transmitPipeline);
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Deprecated
    public static void uninstallTransmitPipeline(Application application){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]uninstallTransmitPipeline can only call in API14+");
        }
        if (application == null){
            throw new NullPointerException("[EvBus]can not uninstall TransmitPipeline on null Application");
        }
        if (transmitPipeline == null){
            throw new IllegalStateException("[EvBus]no EvTransmitPipeline to uninstall");
        }
        application.registerActivityLifecycleCallbacks(transmitPipeline);
        transmitPipeline.onDestroy();
        transmitPipeline = null;
    }

    /********************************************************************************************
     * private
     */

    private static void check(Object context, Type type, EvReceiver receiver) {
        if (context == null) {
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (type == null) {
            throw new IllegalArgumentException("[EvBus]type == null");
        }
        if (receiver == null) {
            throw new IllegalArgumentException("[EvBus]receiver == null");
        }
    }

    private static void check(Object context, Class<? extends EvMessage> messageClass){
        if (context == null) {
            throw new IllegalArgumentException("[EvBus]context == null");
        }
        if (messageClass == null){
            throw new IllegalArgumentException("[EvBus]messageClass == null");
        }
    }

    static EvStation getStation(Activity activity, boolean create){
        //获得station
        LifeCycle component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            if (!create){
                return null;
            }
            try{
                lock.lock();
                component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(activity);
                    LifeCycleUtils.addComponent(activity, COMPONENT_ID, component);
                    stations.add((EvStation) component);
                }
            } finally {
                lock.unlock();
            }
        }
        return (EvStation) component;
    }

    static EvStation getStation(Fragment fragment, boolean create){
        //获得station
        LifeCycle component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
        if (!(component instanceof EvStation)){
            if (!create){
                return null;
            }
            try{
                lock.lock();
                component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(fragment.getActivity());
                    LifeCycleUtils.addComponent(fragment, COMPONENT_ID, component);
                    stations.add((EvStation) component);
                }
            } finally {
                lock.unlock();
            }
        }
        return (EvStation)component;
    }

    static EvStation getStation(android.support.v4.app.FragmentActivity activity, boolean create){
        //获得station
        LifeCycle component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
        if (!create){
            return null;
        }
        if (!(component instanceof EvStation)){
            try{
                lock.lock();
                component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(activity);
                    LifeCycleUtils.addComponent(activity, COMPONENT_ID, component);
                    stations.add((EvStation) component);
                }
            } finally {
                lock.unlock();
            }
        }
        return (EvStation)component;
    }

    static EvStation getStation(android.support.v4.app.Fragment fragment, boolean create){
        //获得station
        LifeCycle component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
        if (!create){
            return null;
        }
        if (!(component instanceof EvStation)){
            try{
                lock.lock();
                component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(fragment.getActivity());
                    LifeCycleUtils.addComponent(fragment, COMPONENT_ID, component);
                    stations.add((EvStation) component);
                }
            } finally {
                lock.unlock();
            }
        }
        return (EvStation) component;
    }

}
