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

    private Map<String, EvReceiver> receivers = new ConcurrentHashMap<>();
    private List<MessageWrapper> onStartMessages = Collections.synchronizedList(new ArrayList<MessageWrapper>());
    private List<MessageWrapper> onResumeMessages = Collections.synchronizedList(new ArrayList<MessageWrapper>());
    private List<MessageWrapper> onPauseMessages = Collections.synchronizedList(new ArrayList<MessageWrapper>());
    private List<MessageWrapper> onStopMessages = Collections.synchronizedList(new ArrayList<MessageWrapper>());
    private List<MessageWrapper> onDestroyMessages = Collections.synchronizedList(new ArrayList<MessageWrapper>());

    EvStation(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }

    void post(String id, Object message){
        EvReceiver receiver = receivers.get(id);
        if (receiver == null){
            return;
        }
        switch (receiver.getType()){
            case CURR_THREAD:
                callReceiver(id, message, receiver);
                break;
            case UI_THREAD:
                if (Looper.getMainLooper() == Looper.myLooper()){
                    callReceiver(id, message, receiver);
                } else {
                    Message msg = myHandler.obtainMessage();
                    msg.obj = new MessageWrapper(id, message);
                    myHandler.sendMessage(msg);
                }
                break;
            case ON_START:
                onStartMessages.add(new MessageWrapper(id, message));
                break;
            case ON_RESUME:
                onResumeMessages.add(new MessageWrapper(id, message));
                break;
            case ON_PAUSE:
                onPauseMessages.add(new MessageWrapper(id, message));
                break;
            case ON_STOP:
                onStopMessages.add(new MessageWrapper(id, message));
                break;
            case ON_DESTROY:
                onDestroyMessages.add(new MessageWrapper(id, message));
                break;
            default:
                break;
        }
    }

    private void callReceiver(String id, Object message, EvReceiver receiver) {
        try {
            receiver.onReceive(message);
        } catch (ClassCastException e){
            logger.e("exception while receiver casting message, id:" + id + ", activity:" + getActivity() + ", message:" + message, e);
        }
    }

    private void callReceiver(MessageWrapper messageWrapper){
        EvReceiver receiver = receivers.get(messageWrapper.id);
        if (receiver == null){
            return;
        }
        callReceiver(messageWrapper.id, messageWrapper.message, receiver);
    }

    void register(String id, EvBus.Type type, EvReceiver receiver){
        receiver.setType(type);
        receivers.put(id, receiver);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {
        List<MessageWrapper> snap = ConcurrentUtils.getSnapShot(onStartMessages);
        for (MessageWrapper messageWrapper : snap){
            onStartMessages.remove(messageWrapper);
        }
        for (MessageWrapper messageWrapper : snap){
            callReceiver(messageWrapper);
        }
    }

    @Override
    public void onResume() {
        List<MessageWrapper> snap = ConcurrentUtils.getSnapShot(onResumeMessages);
        for (MessageWrapper messageWrapper : snap){
            onResumeMessages.remove(messageWrapper);
        }
        for (MessageWrapper messageWrapper : snap){
            callReceiver(messageWrapper);
        }
    }

    @Override
    public void onPause() {
        List<MessageWrapper> snap = ConcurrentUtils.getSnapShot(onPauseMessages);
        for (MessageWrapper messageWrapper : snap){
            onPauseMessages.remove(messageWrapper);
        }
        for (MessageWrapper messageWrapper : snap){
            callReceiver(messageWrapper);
        }
    }

    @Override
    public void onStop() {
        List<MessageWrapper> snap = ConcurrentUtils.getSnapShot(onStopMessages);
        for (MessageWrapper messageWrapper : snap){
            onStopMessages.remove(messageWrapper);
        }
        for (MessageWrapper messageWrapper : snap){
            callReceiver(messageWrapper);
        }
    }

    @Override
    public void onDestroy() {
        List<MessageWrapper> snap = ConcurrentUtils.getSnapShot(onDestroyMessages);
        for (MessageWrapper messageWrapper : snap){
            onDestroyMessages.remove(messageWrapper);
        }
        for (MessageWrapper messageWrapper : snap){
            callReceiver(messageWrapper);
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
            if (msg.obj instanceof MessageWrapper){
                host.callReceiver((MessageWrapper) msg.obj);
            }
        }
    }

    private static class MessageWrapper{

        private String id;
        private Object message;

        private MessageWrapper(String id, Object message) {
            this.id = id;
            this.message = message;
        }
    }

}
