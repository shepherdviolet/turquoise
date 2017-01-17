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
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sviolet.turquoise.enhance.common.WeakHandler;
import sviolet.turquoise.util.common.ConcurrentUtils;
import sviolet.turquoise.util.common.DateTimeUtils;
import sviolet.turquoise.util.reflect.ReflectUtils;
import sviolet.turquoise.utilx.lifecycle.LifeCycle;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * 事件处理节点
 *
 * Created by S.Violet on 2017/1/16.
 */
class EvStation implements LifeCycle {

    private TLogger logger = TLogger.get(this);

    private WeakReference<Activity> activityWeakReference;

    private Map<Class<? extends EvMessage>, EvReceiver> receivers = new ConcurrentHashMap<>();
    private List<EvMessage> onStartMessages = Collections.synchronizedList(new ArrayList<EvMessage>());
    private List<EvMessage> onResumeMessages = Collections.synchronizedList(new ArrayList<EvMessage>());
    private List<EvMessage> onPauseMessages = Collections.synchronizedList(new ArrayList<EvMessage>());
    private List<EvMessage> onStopMessages = Collections.synchronizedList(new ArrayList<EvMessage>());
    private List<EvMessage> onDestroyMessages = Collections.synchronizedList(new ArrayList<EvMessage>());

    private Map<Class<? extends EvMessage>, StoredMessage> storedMessages = new ConcurrentHashMap<>();

    EvStation(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }

    boolean post(EvMessage message){
        if (getActivity() == null){
            return false;
        }
        //根据消息类型获取接收器
        EvReceiver receiver = receivers.get(message.getClass());
        if (receiver == null){
            return false;
        }
        switch (receiver.getType()){
            case CURR_THREAD:
                //直接回调接收器
                callReceiver(message, receiver);
                break;
            case UI_THREAD:
                if (Looper.getMainLooper() == Looper.myLooper()){
                    //直接回调接收器
                    callReceiver(message, receiver);
                } else {
                    //handler中回调接收器
                    Message msg = myHandler.obtainMessage();
                    msg.obj = message;
                    myHandler.sendMessage(msg);
                }
                break;
            case ON_START:
                //放入消息池
                onStartMessages.add(message);
                break;
            case ON_RESUME:
                //放入消息池
                onResumeMessages.add(message);
                break;
            case ON_PAUSE:
                //放入消息池
                onPauseMessages.add(message);
                break;
            case ON_STOP:
                //放入消息池
                onStopMessages.add(message);
                break;
            case ON_DESTROY:
                //放入消息池
                onDestroyMessages.add(message);
                break;
            default:
                return false;
        }
        return true;
    }

    private void callReceiver(EvMessage message, EvReceiver receiver) {
        try {
            receiver.onReceive(message);
        } catch (ClassCastException e){
            logger.e("exception while receiver casting message, activity:" + getActivity() + ", messageClass:" + message.getClass() + ", message:" + message, e);
        }
    }

    private void callReceiver(EvMessage message){
        EvReceiver receiver = receivers.get(message.getClass());
        if (receiver == null){
            return;
        }
        callReceiver(message, receiver);
    }

    void register(EvBus.Type type, EvReceiver receiver){
        if (getActivity() == null){
            return;
        }
        //获得接收器的泛型,根据接收器的泛型来接受指定的消息
        List<Class> actualTypes = ReflectUtils.getActualTypes(receiver.getClass());
        if(actualTypes.size() != 1){
            logger.e("generic type's size of EvReceiver is not 1");
            return;
        }
        receiver.setType(type);
        receivers.put(actualTypes.get(0), receiver);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {
        List<EvMessage> snap = ConcurrentUtils.getSnapShot(onStartMessages);
        for (EvMessage obj : snap){
            onStartMessages.remove(obj);
        }
        for (EvMessage obj : snap){
            callReceiver(obj);
        }
    }

    @Override
    public void onResume() {
        List<EvMessage> snap = ConcurrentUtils.getSnapShot(onResumeMessages);
        for (EvMessage obj : snap){
            onResumeMessages.remove(obj);
        }
        for (EvMessage obj : snap){
            callReceiver(obj);
        }
    }

    @Override
    public void onPause() {
        List<EvMessage> snap = ConcurrentUtils.getSnapShot(onPauseMessages);
        for (EvMessage obj : snap){
            onPauseMessages.remove(obj);
        }
        for (EvMessage obj : snap){
            callReceiver(obj);
        }
    }

    @Override
    public void onStop() {
        List<EvMessage> snap = ConcurrentUtils.getSnapShot(onStopMessages);
        for (EvMessage obj : snap){
            onStopMessages.remove(obj);
        }
        for (EvMessage obj : snap){
            callReceiver(obj);
        }
    }

    @Override
    public void onDestroy() {
        List<EvMessage> snap = ConcurrentUtils.getSnapShot(onDestroyMessages);
        for (EvMessage obj : snap){
            onDestroyMessages.remove(obj);
        }
        for (EvMessage obj : snap){
            callReceiver(obj);
        }

        //清空数据
        receivers.clear();
        onStartMessages.clear();
        onResumeMessages.clear();
        onPauseMessages.clear();
        onStopMessages.clear();
        onDestroyMessages.clear();
        storedMessages.clear();
    }

    void store(EvMessage message){
        //根据消息的类型来指定接收器
        storedMessages.put(message.getClass(), new StoredMessage(DateTimeUtils.getCurrentTimeMillis(), message));
    }

    StoredMessage withdraw(Class<? extends EvMessage> messageType){
        //根据类型来获取消息
        return storedMessages.remove(messageType);
    }

    private Activity getActivity(){
        if (activityWeakReference != null){
            return activityWeakReference.get();
        }
        return null;
    }

    /*****************************************************************8
     * handler
     */

    private final MyHandler myHandler = new MyHandler(this);

    private static class MyHandler extends WeakHandler<EvStation>{

        public MyHandler(EvStation host) {
            super(Looper.getMainLooper(), host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, EvStation host) {
            host.callReceiver((EvMessage) msg.obj);
        }
    }

    class StoredMessage{

        long time;
        EvMessage message;

        private StoredMessage() {
        }

        private StoredMessage(long time, EvMessage message) {
            this.time = time;
            this.message = message;
        }

    }

}
