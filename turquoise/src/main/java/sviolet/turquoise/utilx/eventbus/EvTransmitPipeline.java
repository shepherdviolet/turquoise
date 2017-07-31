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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import java.lang.ref.WeakReference;
import java.util.List;

import sviolet.thistle.common.entity.Destroyable;

/**
 * [API14+]传输消息管道(transmit模式专用), 实现新页面onCreate时, 从前一个页面的传输专用消息池中读取消息.
 *
 * Created by S.Violet on 2017/3/17.
 */

@RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class EvTransmitPipeline implements Application.ActivityLifecycleCallbacks, Destroyable {

    private WeakReference<Activity> currentActivity;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        WeakReference<Activity> recentActivityRef = currentActivity;
        if (recentActivityRef == null){
            return;
        }
        Activity recentActivity = recentActivityRef.get();
        if (recentActivity == null){
            return;
        }
        EvStation recentStation = EvBus.getStation(recentActivity, false);
        if (recentStation == null){
            return;
        }

        List<EvMessage> messages = recentStation.popTransmitMessages();
        if (messages.size() <= 0){
            return;
        }

        EvStation station = EvBus.getStation(activity, true);
        for (EvMessage message : messages){
            station.pushTransmitMessage(message);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = new WeakReference<>(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    public void onDestroy() {
        currentActivity = null;
    }

}
