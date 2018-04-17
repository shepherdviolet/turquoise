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
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.thistle.util.concurrent.ConcurrentUtils;
import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.utilx.lifecycle.LifeCycle;
import sviolet.turquoise.utilx.lifecycle.LifeCycleUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * <p>超轻量级事件总线</p>
 *
 * <p>
 *     1.在事件总线中, 我们用消息的类型(Class)来标识消息, 相同类型(Class)的消息, 视为相同的消息.<br/>
 *     2.事件总线传递的是消息的引用, 不会进行深度拷贝.<br/>
 *     3.有两种消息传递模式, register/post模式和transmit模式, 两种模式传递的消息互不干扰, 即使类型一致.<br/>
 *     4.Activity生命周期结束时(onDestroy)消息和接收器会被自动清除, 无需调用unregister方法反注册接收器.<br/>
 * </p>
 *
 * <p>消息类型+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++</p>
 *
 * <p>普通消息: 在transmit模式中, 一条普通消息只能被消费一次(transmitPop)</p>
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
 * <p>常驻消息: 在transmit模式中, 常驻消息能被多次消费, 且在被移除(transmitRemove)前, 会一直被Context传递下去.</p>
 *
 * <pre>{@code
 *      //常驻消息
 *      public static class Bean implements EvResidentMessage{
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
 * <p>register/post模式: 这种模式用于新的页面向之前已存在的页面(Context)发送消息. 或在几个存在的页面间
 * 发送消息. 这个模式下, 常驻消息(EvResidentMessage)与普通消息(EvMessage)没有差别, 消息只在发送时有效.
 * 一条消息可以同时被复数个注册了的接收器接收并处理. 当你发送(post)时, 消息会被塞入所有注册了接收该类型
 * 消息的普通消息池中, 并寻找合适的时机处理消息. 普通消息池与transmit模式的传输专用消息池完全独立, 互不干扰.</p>
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
 *      //发送消息(广播)
 *      EvBus.post(bean);
 *      //发送消息(指定接收该消息的Activity类型)
 *      //EvBus.post(bean, SomeActivity.class);
 * }</pre>
 *
 * <p>注意:这种方式必须先注册接收器, 再发送消息, 否则将不会收到消息. EvReceiver接收器的泛型必须指定, 且与
 * 发送的消息类型相符.</p>
 *
 * <p>用法2+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++</p>
 *
 * <p>transmit模式: 这种模式用于当前页面(Context)向即将启动的新页面发送消息, 或向未来的复数个新页面发送
 * 消息. 塞入消息(transmitPush)时, 消息会先进入当前页面的传输专用消息池. 在新的页面启动时, 会从当前页面
 * 的传输专用消息池收取所有消息, 加入新页面的传输专用消息池. 前一个页面的消息池中, 普通消息会随之删除,
 * 但常驻消息(EvResidentMessage)会继续保存. 也就是说, 普通消息在任意新页面打开时, 会剪切到新页面消息池,
 * 而常驻消息则是复制到新页面消息池. 一个常驻消息如果不经删除(transmitRemove), 将像病毒一样传递到之后的
 * 所有新页面中. 普通消息建议在启动页面前再塞入消息池, 以免被其他页面收取.</p>
 *
 * <pre>{@code
 *      //前一个页面中, 新建GuideActivity.Bean类型的消息, 并放入消息池
 *      GuideActivity.Bean message = new GuideActivity.Bean();
 *      message.setValue("hello world");
 *      EvBus.transmitPush(this, message);
 *      //启动新页面
 *      startActivity(...);
 * }</pre>
 *
 * <pre>{@code
 *      //注意: 必须在新页面super.onCreate()后收取消息!!!
 *      //新页面中, 收取消息, transmitPop会从消息池删除普通消息, 但常驻消息会保留在消息池中允许多次收取
 *      GuideActivity.Bean message = EvBus.transmitPop(this, GuideActivity.Bean.class);
 *      //transmitRemove会从消息池删除消息, 即使是常驻消息
 *      GuideActivity.Bean message = EvBus.transmitRemove(this, GuideActivity.Bean.class);
 * }</pre>
 *
 * <pre>{@code
 *      //扩展用法:
 *      //transmit用于Activity间传递数据, 一般情况下, 不会出现Message为空的情况(前一个页面肯定送了消息)
 *      //但是从程序的健壮性考虑, 后一个页面还是要判断一下消息是否为空(避免出现空指针), 这样一个一个判断
 *      //十分繁琐, 因此, EvBus提供了transmitPopRequired和transmitRemoveRequired方法, 你可以参考下面的
 *      //方式获取消息, 并统一处理消息为空的情况(提示用户并结束当前Activity).
 *      try {
 *          message1 = EvBus.transmitPopRequired(this, GuideActivity.Bean1.class);
 *          message2 = EvBus.transmitPopRequired(this, GuideActivity.Bean2.class);
 *          ......
 *      } catch (MissingMessageException e) {
 *          //提示用户并结束当前Activity
 *          Toast.makeText(...).show();
 *          finish();
 *      }
 * }</pre>
 *
 * Created by S.Violet on 2017/1/16.
 */
public class EvBus {

    private static final String COMPONENT_ID = "TURQUOISE_EV_BUS_STATION_COMPONENT";

    private static final TLogger logger = TLogger.get(EvBus.class);
    private static final ReentrantLock createLock = new ReentrantLock();
    private static final ReentrantLock mapLock = new ReentrantLock();

    private static final Set<EvStation> stations = Collections.newSetFromMap(new WeakHashMap<EvStation, Boolean>());

    /*******************************************************************************************
     * register/post模式
     */

    /**
     * [register/post模式]发送消息, 在发送前注册(register)的接收器都会收到这个消息(广播)
     * @param message 消息, 类型必须匹配接收器指定的类型
     */
    public static void post(EvMessage message){
        post(message, null);
    }

    /**
     * [register/post模式]发送消息, 在发送前注册(register)的接收器都会收到这个消息(指定接收该消息的Activity类型)
     * @param message 消息, 类型必须匹配接收器指定的类型
     * @param specifiedActivityType 指定接收该消息的Activity类型(继承该类型的Activity也会收到消息)
     */
    public static void post(EvMessage message, Class<?> specifiedActivityType){
        if (message == null){
            return;
        }
        boolean result = false;
        List<EvStation> stations;
        try {
            mapLock.lock();
            stations = ConcurrentUtils.getSnapShot(EvBus.stations);
        } finally {
            mapLock.unlock();
        }
        //遍历所有station并推送消息
        for (EvStation station : stations){
            if (station.post(message, specifiedActivityType)){
                result = true;
            }
        }
        if (!result){
            logger.d("no receiver handle this message, messageClass:" + message.getClass() + ", message:" + message);
        }
    }

    /**
     * [register/post模式]
     * 1.根据EvTransmitPopDeclare注释, 从EvBus中获取消息, 注入到成员变量中
     * 2.根据EvReceiverDeclare注释, 注册消息接收器
     * @param activity activity
     * @throws MissingMessageException 标记为required的消息为空
     */
    public static void registerAnnotations(Activity activity) throws MissingMessageException {
        check(activity);
        EvAnnotationRegister.register(getStation(activity, true), activity);
    }

    /**
     * [register/post模式]
     * 静默处理异常
     * 1.根据EvTransmitPopDeclare注释, 从EvBus中获取消息, 注入到成员变量中
     * 2.根据EvReceiverDeclare注释, 注册消息接收器
     * @param activity activity
     * @return 异常时返回false
     */
    public static boolean registerAnnotationsQuiet(Activity activity) {
        try {
            registerAnnotations(activity);
            return true;
        } catch (MissingMessageException e) {
            TLogger.get(activity).e("Missing message, message type:" + e.getMessageType(), e);
            Toast.makeText(activity.getApplicationContext(), "未知错误[UnknownError]", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }
    }

    /**
     * [register/post模式]
     * 1.根据EvTransmitPopDeclare注释, 从EvBus中获取消息, 注入到成员变量中
     * 2.根据EvReceiverDeclare注释, 注册消息接收器
     * @param fragment fragment
     * @throws MissingMessageException 标记为required的消息为空
     */
    public static void registerAnnotations(Fragment fragment) throws MissingMessageException {
        check(fragment);
        EvAnnotationRegister.register(getStation(fragment, true), fragment);
    }

    /**
     * [register/post模式]
     * 静默处理异常
     * 1.根据EvTransmitPopDeclare注释, 从EvBus中获取消息, 注入到成员变量中
     * 2.根据EvReceiverDeclare注释, 注册消息接收器
     * @param fragment fragment
     * @return 异常时返回false
     */
    public static boolean registerAnnotationsQuiet(Fragment fragment) {
        try {
            registerAnnotations(fragment);
            return true;
        } catch (MissingMessageException e) {
            TLogger.get(fragment).e("Missing message, message type:" + e.getMessageType(), e);
            Activity activity = fragment.getActivity();
            if (activity != null) {
                Toast.makeText(activity.getApplicationContext(), "未知错误[UnknownError]", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
            return false;
        }
    }

    /**
     * [register/post模式]
     * 1.根据EvTransmitPopDeclare注释, 从EvBus中获取消息, 注入到成员变量中
     * 2.根据EvReceiverDeclare注释, 注册消息接收器
     * @param fragment fragment
     * @throws MissingMessageException 标记为required的消息为空
     */
    public static void registerAnnotations(android.support.v4.app.Fragment fragment) throws MissingMessageException {
        check(fragment);
        EvAnnotationRegister.register(getStation(fragment, true), fragment);
    }

    /**
     * [register/post模式]
     * 静默处理异常
     * 1.根据EvTransmitPopDeclare注释, 从EvBus中获取消息, 注入到成员变量中
     * 2.根据EvReceiverDeclare注释, 注册消息接收器
     * @param fragment fragment
     * @return 异常时返回false
     */
    public static boolean registerAnnotationsQuiet(android.support.v4.app.Fragment fragment) {
        try {
            registerAnnotations(fragment);
            return true;
        } catch (MissingMessageException e) {
            TLogger.get(fragment).e("Missing message, message type:" + e.getMessageType(), e);
            Activity activity = fragment.getActivity();
            if (activity != null) {
                Toast.makeText(activity.getApplicationContext(), "未知错误[UnknownError]", Toast.LENGTH_SHORT).show();
                activity.finish();
            }
            return false;
        }
    }

    /**
     * [register/post模式]注册消息接收器,
     * 在同一个Activity重复注册同类型的接收器会覆盖之前的接收器
     * @param activity Activity
     * @param type 接收方式
     * @param receiver 接收器, 必须用泛型指定接受的消息类型
     */
    public static void register(Activity activity, Type type, EvReceiver receiver){
        check(activity, type, receiver);
        getStation(activity, true).register(type, receiver);
    }

    /**
     * [register/post模式]注册消息接收器,
     * 在同一个Activity重复注册同类型的接收器会覆盖之前的接收器
     * @param fragment fragment
     * @param type 接收方式
     * @param receiver 接收器, 必须用泛型指定接受的消息类型
     */
    public static void register(Fragment fragment, Type type, EvReceiver receiver){
        check(fragment, type, receiver);
        getStation(fragment, true).register(type, receiver);
    }

    /**
     * [register/post模式]注册消息接收器,
     * 在同一个Activity重复注册同类型的接收器会覆盖之前的接收器
     * @param fragment fragment
     * @param type 接收方式
     * @param receiver 接收器, 必须用泛型指定接受的消息类型
     */
    public static void register(android.support.v4.app.Fragment fragment, Type type, EvReceiver receiver){
        check(fragment, type, receiver);
        getStation(fragment, true).register(type, receiver);
    }

    /**
     * [register/post模式]反注册消息接收器
     * @param activity activity
     * @param messageClass 消息类型
     */
    public static void unregister(Activity activity, Class<? extends EvMessage> messageClass) {
        check(activity, messageClass);
        getStation(activity, true).unregister(messageClass);
    }

    /**
     * [register/post模式]反注册消息接收器
     * @param fragment fragment
     * @param messageClass 消息类型
     */
    public static void unregister(Fragment fragment, Class<? extends EvMessage> messageClass) {
        check(fragment, messageClass);
        getStation(fragment, true).unregister(messageClass);
    }

    /**
     * [register/post模式]反注册消息接收器
     * @param fragment fragment
     * @param messageClass 消息类型
     */
    public static void unregister(android.support.v4.app.Fragment fragment, Class<? extends EvMessage> messageClass) {
        check(fragment, messageClass);
        getStation(fragment, true).unregister(messageClass);
    }

    /**
     * [register/post模式]反注册所有消息接收器,
     * Activity生命周期结束时(onDestroy)消息和接收器会被自动清除, 无需调用unregister方法反注册接收器.
     * @param activity activity
     */
    public static void unregisterAll(Activity activity) {
        check(activity);
        getStation(activity, true).unregisterAll();
    }

    /**
     * [register/post模式]反注册所有消息接收器,
     * Activity生命周期结束时(onDestroy)消息和接收器会被自动清除, 无需调用unregister方法反注册接收器.
     * @param fragment fragment
     */
    public static void unregisterAll(Fragment fragment) {
        check(fragment);
        getStation(fragment, true).unregisterAll();
    }

    /**
     * [register/post模式]反注册所有消息接收器,
     * Activity生命周期结束时(onDestroy)消息和接收器会被自动清除, 无需调用unregister方法反注册接收器.
     * @param fragment fragment
     */
    public static void unregisterAll(android.support.v4.app.Fragment fragment) {
        check(fragment);
        getStation(fragment, true).unregisterAll();
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
     * transmit模式
     */

    private static EvTransmitPipeline transmitPipeline;

    /**
     * [API14+][transmit模式] 在传输专用消息池中放入消息, 等待新页面从中收走消息
     * @param activity activity
     * @param message 消息
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
     * [API14+][transmit模式] 在传输专用消息池中放入消息, 等待新页面从中收走消息
     * @param fragment fragment
     * @param message 消息
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
     * [API14+][transmit模式] 在传输专用消息池中放入消息, 等待新页面从中收走消息
     * @param fragment fragment
     * @param message 消息
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
     * [API14+][transmit模式] 从传输专用消息池中获取消息, 普通消息会从消息池中删除, 常驻消息(EvResidentMessage)允许复数次获取,
     * 如果获取不到则抛出异常
     * @param activity activity
     * @param messageClass 消息 类型
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitPopRequired(Activity activity, Class<T> messageClass) throws MissingMessageException {
        T message = transmitPop(activity, messageClass);
        if (message == null) {
            throw new MissingMessageException(messageClass, "Missing required EvMessage, activity:" + activity + ", messageClass:" + messageClass);
        }
        return message;
    }

    /**
     * [API14+][transmit模式] 从传输专用消息池中获取消息, 普通消息会从消息池中删除, 常驻消息(EvResidentMessage)允许复数次获取,
     * 如果获取不到则抛出异常
     * @param fragment fragment
     * @param messageClass 消息 类型
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitPopRequired(Fragment fragment, Class<T> messageClass) throws MissingMessageException {
        T message = transmitPop(fragment, messageClass);
        if (message == null) {
            throw new MissingMessageException(messageClass, "Missing required EvMessage, fragment:" + fragment + ", messageClass:" + messageClass);
        }
        return message;
    }

    /**
     * [API14+][transmit模式] 从传输专用消息池中获取消息, 普通消息会从消息池中删除, 常驻消息(EvResidentMessage)允许复数次获取,
     * 如果获取不到则抛出异常
     * @param fragment fragment
     * @param messageClass 消息 类型
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitPopRequired(android.support.v4.app.Fragment fragment, Class<T> messageClass) throws MissingMessageException {
        T message = transmitPop(fragment, messageClass);
        if (message == null) {
            throw new MissingMessageException(messageClass, "Missing required EvMessage, fragment:" + fragment + ", messageClass:" + messageClass);
        }
        return message;
    }

    /**
     * [API14+][transmit模式] 从传输专用消息池中获取消息, 普通消息会从消息池中删除, 常驻消息(EvResidentMessage)允许复数次获取
     * @param activity activity
     * @param messageClass 消息 类型
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
     * [API14+][transmit模式] 从传输专用消息池中获取消息, 普通消息会从消息池中删除, 常驻消息(EvResidentMessage)允许复数次获取
     * @param fragment fragment
     * @param messageClass 消息 类型
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
     * [API14+][transmit模式] 从传输专用消息池中获取消息, 普通消息会从消息池中删除, 常驻消息(EvResidentMessage)允许复数次获取
     * @param fragment fragment
     * @param messageClass 消息 类型
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
     * [API14+][transmit模式] 从传输专用消息池中移除消息, 包括常驻消息(EvResidentMessage)也会被移除,
     * 如果获取不到则抛出异常
     * @param activity activity
     * @param messageClass 消息 类型
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitRemoveRequired(Activity activity, Class<T> messageClass) throws MissingMessageException {
        T message = transmitRemove(activity, messageClass);
        if (message == null) {
            throw new MissingMessageException(messageClass, "Missing required EvMessage, activity:" + activity + ", messageClass:" + messageClass);
        }
        return message;
    }

    /**
     * [API14+][transmit模式] 从传输专用消息池中移除消息, 包括常驻消息(EvResidentMessage)也会被移除,
     * 如果获取不到则抛出异常
     * @param fragment fragment
     * @param messageClass 消息 类型
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitRemoveRequired(Fragment fragment, Class<T> messageClass) throws MissingMessageException {
        T message = transmitRemove(fragment, messageClass);
        if (message == null) {
            throw new MissingMessageException(messageClass, "Missing required EvMessage, fragment:" + fragment + ", messageClass:" + messageClass);
        }
        return message;
    }

    /**
     * [API14+][transmit模式] 从传输专用消息池中移除消息, 包括常驻消息(EvResidentMessage)也会被移除,
     * 如果获取不到则抛出异常
     * @param fragment fragment
     * @param messageClass 消息 类型
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static <T extends EvMessage> T transmitRemoveRequired(android.support.v4.app.Fragment fragment, Class<T> messageClass) throws MissingMessageException {
        T message = transmitRemove(fragment, messageClass);
        if (message == null) {
            throw new MissingMessageException(messageClass, "Missing required EvMessage, fragment:" + fragment + ", messageClass:" + messageClass);
        }
        return message;
    }

    /**
     * [API14+][transmit模式] 从传输专用消息池中移除消息, 包括常驻消息(EvResidentMessage)也会被移除
     * @param activity activity
     * @param messageClass 消息 类型
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
     * [API14+][transmit模式] 从传输专用消息池中移除消息, 包括常驻消息(EvResidentMessage)也会被移除
     * @param fragment fragment
     * @param messageClass 消息 类型
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
     * [API14+][transmit模式] 从传输专用消息池中移除消息, 包括常驻消息(EvResidentMessage)也会被移除
     * @param fragment fragment
     * @param messageClass 消息 类型
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

    /**
     * [API14+][transmit模式] 使用transmit模式前必须安装传输管道, 在Application.onCreate中安装即可.
     * 注意::使用TApplication时, 无需调用此方法安装传输管道(TApplication已实现)
     * @param context context
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Deprecated
    public static void installTransmitPipeline(Context context){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]installTransmitPipeline can only call in API14+");
        }
        if (context == null){
            throw new NullPointerException("[EvBus]can not install TransmitPipeline on null context");
        }
        if (transmitPipeline != null){
            throw new IllegalStateException("[EvBus]you can only install TransmitPipeline once");
        }
        transmitPipeline = new EvTransmitPipeline();
        ((Application)context.getApplicationContext()).registerActivityLifecycleCallbacks(transmitPipeline);
    }

    /**
     * [API14+][transmit模式] 使用transmit模式后必须卸载传输管道, 在Application.onTerminate中卸载即可.
     * 注意::使用TApplication时, 无需调用此方法卸载传输管道
     * @param context context
     */
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Deprecated
    public static void uninstallTransmitPipeline(Context context){
        if (DeviceUtils.getVersionSDK() < 14){
            throw new IllegalStateException("[EvBus]uninstallTransmitPipeline can only call in API14+");
        }
        if (context == null){
            throw new NullPointerException("[EvBus]can not uninstall TransmitPipeline on null context");
        }
        if (transmitPipeline == null){
            throw new IllegalStateException("[EvBus]no EvTransmitPipeline to uninstall");
        }
        ((Application)context.getApplicationContext()).unregisterActivityLifecycleCallbacks(transmitPipeline);
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

    private static void check(Object context){
        if (context == null) {
            throw new IllegalArgumentException("[EvBus]context == null");
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
                createLock.lock();
                component = LifeCycleUtils.getComponent(activity, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(activity);
                    LifeCycleUtils.addComponent(activity, COMPONENT_ID, component);
                    try {
                        mapLock.lock();
                        stations.add((EvStation) component);
                    } finally {
                        mapLock.unlock();
                    }
                }
            } finally {
                createLock.unlock();
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
                createLock.lock();
                component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(fragment.getActivity());
                    LifeCycleUtils.addComponent(fragment, COMPONENT_ID, component);
                    try {
                        stations.add((EvStation) component);
                    } finally {
                        mapLock.unlock();
                    }
                }
            } finally {
                createLock.unlock();
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
                createLock.lock();
                component = LifeCycleUtils.getComponent(fragment, COMPONENT_ID);
                if (!(component instanceof EvStation)){
                    //新建station
                    component = new EvStation(fragment.getActivity());
                    LifeCycleUtils.addComponent(fragment, COMPONENT_ID, component);
                    try {
                        stations.add((EvStation) component);
                    } finally {
                        mapLock.unlock();
                    }
                }
            } finally {
                createLock.unlock();
            }
        }
        return (EvStation) component;
    }

    /********************************************************************************************
     * class
     */

    public static class MissingMessageException extends Exception {

        private Class<? extends EvMessage> messageType;

        public MissingMessageException(Class<? extends EvMessage> messageType, String message) {
            super(message);
            this.messageType = messageType;
        }

        public Class<? extends EvMessage> getMessageType() {
            return messageType;
        }

    }

}
