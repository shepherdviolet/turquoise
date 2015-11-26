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

package sviolet.turquoise.utils.lifecycle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import sviolet.turquoise.utils.lifecycle.listener.LifeCycle;
import sviolet.turquoise.utils.lifecycle.manager.LifeCycleFragment;
import sviolet.turquoise.utils.lifecycle.manager.LifeCycleFragmentV4;
import sviolet.turquoise.utils.lifecycle.manager.LifeCycleManager;
import sviolet.turquoise.utils.lifecycle.manager.LifeCycleManagerImpl;
import sviolet.turquoise.utils.sys.DeviceUtils;

/**
 * Android生命周期监听工具
 *
 * Created by S.Violet on 2015/11/24.
 */
public class LifeCycleUtils {

    /**
     * 监听Activity的生命周期<p/>
     *
     * 注意:<br/>
     * API < 11 时, 不建议使用该方法, 只能利用{@link GlobalVars}捕获onDestroy()事件, 建议改用
     * {@link android.support.v4.app.FragmentActivity} 或 {@Link android.support.v4.app.Fragment}<p/>
     *
     * @param activity 被监听的Activity
     * @param lifeCycle 监听器
     */
    public static void attach(Activity activity, LifeCycle lifeCycle){
        if (activity == null)
            throw new NullPointerException("[LifeCycleUtils] activity == null");
        if (lifeCycle == null)
            return;

        //API < 11 使用GlobalVars做简易监听
        if (DeviceUtils.getVersionSDK() >= 11){
            addListener(activity, lifeCycle);
        }else{
            GlobalVars.set(activity, LifeCycleManager.MANAGER_TAG + "#" + lifeCycle.getClass().getSimpleName() + "#" + lifeCycle.hashCode(), lifeCycle);
        }
    }

    /**
     * 监听FragmentActivity的生命周期
     *
     * @param fragmentActivity 被监听的FragmentActivity
     * @param lifeCycle 监听器
     */
    public static void attach(FragmentActivity fragmentActivity, LifeCycle lifeCycle){
        if (fragmentActivity == null)
            throw new NullPointerException("[LifeCycleUtils] fragmentActivity == null");
        if (lifeCycle == null)
            return;

        addListener(fragmentActivity, lifeCycle);
    }

    /**
     * 监听Fragment的生命周期<p/>
     *
     * 实际上是监听其对应的Activity<p/>
     *
     * @param fragment 被监听的Fragment
     * @param lifeCycle 监听器
     */
    public static void attach(Fragment fragment, LifeCycle lifeCycle){
        if (fragment == null)
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        if (lifeCycle == null)
            return;

        FragmentActivity activity = fragment.getActivity();
        if (activity == null)
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");
        addListener(activity, lifeCycle);
    }

    /**
     * 监听Fragment的生命周期<p/>
     *
     * 实际上是监听其对应的Activity<p/>
     *
     * @param fragment 被监听的Fragment
     * @param lifeCycle 监听器
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void attach(android.app.Fragment fragment, LifeCycle lifeCycle){
        if (fragment == null)
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        if (lifeCycle == null)
            return;

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Fragment before api 11");
        }

        Activity activity = fragment.getActivity();
        if (activity == null)
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");
        addListener(activity, lifeCycle);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void addListener(Activity activity, LifeCycle lifeCycle) {

        //GlobalVars中获取Activity的生命周期管理器
        Object manager = GlobalVars.get(activity, LifeCycleManager.MANAGER_TAG);

        //生命周期管理器不存在则新建
        if (manager == null || !(manager instanceof LifeCycleManager)){
            manager = new LifeCycleManagerImpl();//新建管理器
            android.app.Fragment oldFragment = activity.getFragmentManager().findFragmentByTag(LifeCycleManager.FRAGMENT_TAG);//原有Fragment
            android.app.Fragment fragment = new LifeCycleFragment(activity, (LifeCycleManager) manager);//新生命周期监听Fragment
            android.app.FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
            if (oldFragment != null) {
                transaction.remove(oldFragment);//移除原有Fragment
            }
            transaction.add(fragment, LifeCycleManager.FRAGMENT_TAG);//绑定生命周期监听Fragment
            transaction.commit();
        }

        //加入监听器
        ((LifeCycleManager)manager).addListener(lifeCycle);
    }

    private static void addListener(FragmentActivity fragmentActivity, LifeCycle lifeCycle) {

        //GlobalVars中获取FragmentActivity的生命周期管理器
        Object manager = GlobalVars.get(fragmentActivity, LifeCycleManager.MANAGER_TAG);

        //生命周期管理器不存在则新建
        if (manager == null || !(manager instanceof LifeCycleManager)){
            manager = new LifeCycleManagerImpl();//新建管理器
            Fragment oldFragment = fragmentActivity.getSupportFragmentManager().findFragmentByTag(LifeCycleManager.FRAGMENT_TAG);//原有Fragment
            Fragment fragment = new LifeCycleFragmentV4(fragmentActivity, (LifeCycleManager) manager);//新生命周期监听Fragment
            FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
            if (oldFragment != null) {
                transaction.remove(oldFragment);//移除原有Fragment
            }
            transaction.add(fragment, LifeCycleManager.FRAGMENT_TAG);//绑定生命周期监听Fragment
            transaction.commit();
        }

        //加入监听器
        ((LifeCycleManager)manager).addListener(lifeCycle);
    }

}
