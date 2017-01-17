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

    private Map<Class, EvReceiver> receivers = new ConcurrentHashMap<>();
    private List<Object> onStartMessages = Collections.synchronizedList(new ArrayList<>());
    private List<Object> onResumeMessages = Collections.synchronizedList(new ArrayList<>());
    private List<Object> onPauseMessages = Collections.synchronizedList(new ArrayList<>());
    private List<Object> onStopMessages = Collections.synchronizedList(new ArrayList<>());
    private List<Object> onDestroyMessages = Collections.synchronizedList(new ArrayList<>());

    EvStation(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }

    boolean post(Object message){
        if (getActivity() == null){
            return false;
        }
        EvReceiver receiver = receivers.get(message.getClass());
        if (receiver == null){
            return false;
        }
        switch (receiver.getType()){
            case CURR_THREAD:
                callReceiver(message, receiver);
                break;
            case UI_THREAD:
                if (Looper.getMainLooper() == Looper.myLooper()){
                    callReceiver(message, receiver);
                } else {
                    Message msg = myHandler.obtainMessage();
                    msg.obj = message;
                    myHandler.sendMessage(msg);
                }
                break;
            case ON_START:
                onStartMessages.add(message);
                break;
            case ON_RESUME:
                onResumeMessages.add(message);
                break;
            case ON_PAUSE:
                onPauseMessages.add(message);
                break;
            case ON_STOP:
                onStopMessages.add(message);
                break;
            case ON_DESTROY:
                onDestroyMessages.add(message);
                break;
            default:
                return false;
        }
        return true;
    }

    private void callReceiver(Object message, EvReceiver receiver) {
        try {
            receiver.onReceive(message);
        } catch (ClassCastException e){
            logger.e("exception while receiver casting message, activity:" + getActivity() + ", messageClass:" + message.getClass() + ", message:" + message, e);
        }
    }

    private void callReceiver(Object message){
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
        List<Object> snap = ConcurrentUtils.getSnapShot(onStartMessages);
        for (Object obj : snap){
            onStartMessages.remove(obj);
        }
        for (Object obj : snap){
            callReceiver(obj);
        }
    }

    @Override
    public void onResume() {
        List<Object> snap = ConcurrentUtils.getSnapShot(onResumeMessages);
        for (Object obj : snap){
            onResumeMessages.remove(obj);
        }
        for (Object obj : snap){
            callReceiver(obj);
        }
    }

    @Override
    public void onPause() {
        List<Object> snap = ConcurrentUtils.getSnapShot(onPauseMessages);
        for (Object obj : snap){
            onPauseMessages.remove(obj);
        }
        for (Object obj : snap){
            callReceiver(obj);
        }
    }

    @Override
    public void onStop() {
        List<Object> snap = ConcurrentUtils.getSnapShot(onStopMessages);
        for (Object obj : snap){
            onStopMessages.remove(obj);
        }
        for (Object obj : snap){
            callReceiver(obj);
        }
    }

    @Override
    public void onDestroy() {
        List<Object> snap = ConcurrentUtils.getSnapShot(onDestroyMessages);
        for (Object obj : snap){
            onDestroyMessages.remove(obj);
        }
        for (Object obj : snap){
            callReceiver(obj);
        }

        //清空数据
        receivers.clear();
        onStartMessages.clear();
        onResumeMessages.clear();
        onPauseMessages.clear();
        onStopMessages.clear();
        onDestroyMessages.clear();
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
            host.callReceiver(msg.obj);
        }
    }

}
