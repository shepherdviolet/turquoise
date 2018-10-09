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

package sviolet.turquoise.utilx.lifecycle;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;

import java.lang.reflect.Field;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.thistle.util.common.ParasiticVars;
import sviolet.turquoise.entity.statics.StringConstants;
import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * 生命周期监听工具, 可为Activity添加生命周期监听, 或添加含生命周期的组件<p/>
 *
 * Created by S.Violet on 2015/11/24.
 */
public class LifeCycleUtils {

    /**
     * 监听Activity的生命周期, 弱引用持有LifeCycle实例<p/>
     *
     * @param activity 被监听的Activity
     * @param lifeCycle 监听器(被弱引用持有)
     */
    public static void attach(Activity activity, LifeCycle lifeCycle){
        if (activity == null) {
            throw new NullPointerException("[LifeCycleUtils] activity == null");
        }
        if (lifeCycle == null) {
            return;
        }

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Activity before api 11");
        }

        LifeCycleManager manager = activity instanceof FragmentActivity ? getLifeCycleManagerV4((FragmentActivity) activity) : getLifeCycleManager(activity);
        manager.addWeakListener(lifeCycle);
    }

    /**
     * 监听Fragment的生命周期, 弱引用持有LifeCycle实例<p/>
     *
     * 实际上是监听其对应的Activity<p/>
     *
     * @param fragment 被监听的Fragment
     * @param lifeCycle 监听器(被弱引用持有)
     */
    public static void attach(Fragment fragment, LifeCycle lifeCycle){
        if (fragment == null) {
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        }
        if (lifeCycle == null) {
            return;
        }

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Fragment before api 11");
        }

        Activity activity = fragment.getActivity();
        if (activity == null) {
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");
        }

        LifeCycleManager manager = getLifeCycleManager(activity);
        manager.addWeakListener(lifeCycle);
    }

    /**
     * 监听Fragment的生命周期, 弱引用持有LifeCycle实例<p/>
     *
     * 实际上是监听其对应的Activity<p/>
     *
     * @param fragment 被监听的Fragment
     * @param lifeCycle 监听器(被弱引用持有)
     */
    public static void attach(android.support.v4.app.Fragment fragment, LifeCycle lifeCycle){
        if (fragment == null) {
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        }
        if (lifeCycle == null) {
            return;
        }

        android.support.v4.app.FragmentActivity activity = fragment.getActivity();
        if (activity == null) {
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");
        }

        LifeCycleManager manager = getLifeCycleManagerV4(activity);
        manager.addWeakListener(lifeCycle);
    }

    /*************************************************************************************
     * component
     */

    /**
     * 添加生命周期监听组件, 强引用持有LifeCycle实例<p/>
     *
     * @param activity 被监听的Activity
     * @param componentName 组件名(若和其他的组件名冲突, 之前的组件会被替换掉)
     * @param lifeCycle 组件(被强引用持有)
     */
    public static void addComponent(Activity activity, String componentName, LifeCycle lifeCycle){
        if (activity == null) {
            throw new NullPointerException("[LifeCycleUtils] activity == null");
        }
        if (lifeCycle == null) {
            return;
        }

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Activity before api 11");
        }

        LifeCycleManager manager = activity instanceof FragmentActivity ? getLifeCycleManagerV4((FragmentActivity) activity) : getLifeCycleManager(activity);
        manager.addComponent(componentName, lifeCycle);
    }

    /**
     * 添加生命周期监听组件, 强引用持有LifeCycle实例<p/>
     *
     * 实际上是监听其对应的Activity<p/>
     *
     * @param fragment 被监听的Fragment
     * @param componentName 组件名(若和其他的组件名冲突, 之前的组件会被替换掉)
     * @param lifeCycle 组件(被强引用持有)
     */
    public static void addComponent(Fragment fragment, String componentName, LifeCycle lifeCycle){
        if (fragment == null) {
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        }
        if (lifeCycle == null) {
            return;
        }

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Fragment before api 11");
        }

        Activity activity = fragment.getActivity();
        if (activity == null) {
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");
        }

        LifeCycleManager manager = getLifeCycleManager(activity);
        manager.addComponent(componentName, lifeCycle);
    }

    /**
     * 添加生命周期监听组件, 强引用持有LifeCycle实例<p/>
     *
     * 实际上是监听其对应的Activity<p/>
     *
     * @param fragment 被监听的Fragment
     * @param componentName 组件名(若和其他的组件名冲突, 之前的组件会被替换掉)
     * @param lifeCycle 组件(被强引用持有)
     */
    public static void addComponent(android.support.v4.app.Fragment fragment, String componentName, LifeCycle lifeCycle){
        if (fragment == null) {
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        }
        if (lifeCycle == null) {
            return;
        }

        android.support.v4.app.FragmentActivity activity = fragment.getActivity();
        if (activity == null) {
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");
        }

        LifeCycleManager manager = getLifeCycleManagerV4(activity);
        manager.addComponent(componentName, lifeCycle);
    }

    /*************************************************************************************
     * get
     */

    /**
     * 获取生命周期监听组件<p/>
     *
     * @param activity 被监听的Activity
     * @param componentName 组件名
     */
    public static LifeCycle getComponent(Activity activity, String componentName){
        if (activity == null) {
            throw new NullPointerException("[LifeCycleUtils] activity == null");
        }

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Activity before api 11");
        }

        LifeCycleManager manager = activity instanceof FragmentActivity ? getLifeCycleManagerV4((FragmentActivity) activity) : getLifeCycleManager(activity);
        return manager.getComponent(componentName);
    }

    /**
     * 获取生命周期监听组件<p/>
     *
     * @param fragment 被监听的Fragment
     * @param componentName 组件名
     */
    public static LifeCycle getComponent(Fragment fragment, String componentName){
        if (fragment == null) {
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        }

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Fragment before api 11");
        }

        Activity activity = fragment.getActivity();
        if (activity == null) {
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");
        }

        LifeCycleManager manager = getLifeCycleManager(activity);
        return manager.getComponent(componentName);
    }

    /**
     * 获取生命周期监听组件<p/>
     *
     * @param fragment 被监听的Fragment
     * @param componentName 组件名
     */
    public static LifeCycle getComponent(android.support.v4.app.Fragment fragment, String componentName){
        if (fragment == null) {
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        }

        android.support.v4.app.FragmentActivity activity = fragment.getActivity();
        if (activity == null) {
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");
        }

        LifeCycleManager manager = getLifeCycleManagerV4(activity);
        return manager.getComponent(componentName);
    }

    /*************************************************************************************
     * remove
     */

    /**
     * 移除生命周期监听组件<p/>
     *
     * @param activity 被监听的Activity
     * @param componentName 组件名
     */
    public static void removeComponent(Activity activity, String componentName){
        if (activity == null) {
            throw new NullPointerException("[LifeCycleUtils] activity == null");
        }

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Activity before api 11");
        }

        LifeCycleManager manager = activity instanceof FragmentActivity ? getLifeCycleManagerV4((FragmentActivity) activity) : getLifeCycleManager(activity);
        manager.removeComponent(componentName);
    }

    /**
     * 移除生命周期监听组件<p/>
     *
     * @param fragment 被监听的Fragment
     * @param componentName 组件名
     */
    public static void removeComponent(Fragment fragment, String componentName){
        if (fragment == null) {
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        }

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Fragment before api 11");
        }

        Activity activity = fragment.getActivity();
        if (activity == null) {
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");
        }

        LifeCycleManager manager = getLifeCycleManager(activity);
        manager.removeComponent(componentName);
    }

    /**
     * 移除生命周期监听组件<p/>
     *
     * @param fragment 被监听的Fragment
     * @param componentName 组件名
     */
    public static void removeComponent(android.support.v4.app.Fragment fragment, String componentName){
        if (fragment == null) {
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        }

        android.support.v4.app.FragmentActivity activity = fragment.getActivity();
        if (activity == null) {
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");
        }

        LifeCycleManager manager = getLifeCycleManagerV4(activity);
        manager.removeComponent(componentName);
    }

    /**
     * 移除生命周期监听组件<p/>
     *
     * @param activity 被监听的Activity
     * @param lifeCycle 监听器
     */
    public static void removeComponent(Activity activity, LifeCycle lifeCycle){
        if (activity == null) {
            throw new NullPointerException("[LifeCycleUtils] activity == null");
        }
        if (lifeCycle == null) {
            return;
        }

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Activity before api 11");
        }

        LifeCycleManager manager = activity instanceof FragmentActivity ? getLifeCycleManagerV4((FragmentActivity) activity) : getLifeCycleManager(activity);
        manager.removeComponent(lifeCycle);
    }

    /**
     * 移除生命周期监听组件<p/>
     *
     * @param fragment 被监听的Fragment
     * @param lifeCycle 监听器
     */
    public static void removeComponent(Fragment fragment, LifeCycle lifeCycle){
        if (fragment == null) {
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        }
        if (lifeCycle == null) {
            return;
        }

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Fragment before api 11");
        }

        Activity activity = fragment.getActivity();
        if (activity == null) {
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");
        }

        LifeCycleManager manager = getLifeCycleManager(activity);
        manager.removeComponent(lifeCycle);
    }

    /**
     * 移除生命周期监听组件<p/>
     *
     * @param fragment 被监听的Fragment
     * @param lifeCycle 监听器
     */
    public static void removeComponent(android.support.v4.app.Fragment fragment, LifeCycle lifeCycle){
        if (fragment == null) {
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        }
        if (lifeCycle == null) {
            return;
        }

        android.support.v4.app.FragmentActivity activity = fragment.getActivity();
        if (activity == null) {
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");
        }

        LifeCycleManager manager = getLifeCycleManagerV4(activity);
        manager.removeComponent(lifeCycle);
    }

    /*************************************************************************************
     * inner
     */

    private static final ReentrantLock lock = new ReentrantLock();

    private static LifeCycleManager getLifeCycleManager(Activity activity){
        //ParasiticVars中获取Activity的生命周期管理器
        Object manager = ParasiticVars.get(activity, StringConstants.LIFECYCLE_MANAGER_TAG);

        if (!(manager instanceof LifeCycleManager)) {
            try {
                lock.lock();
                //ParasiticVars中获取Activity的生命周期管理器
                manager = ParasiticVars.get(activity, StringConstants.LIFECYCLE_MANAGER_TAG);
                //生命周期管理器不存在则新建
                if (!(manager instanceof LifeCycleManager)) {
                    manager = new LifeCycleManagerImpl();//新建管理器
                    FragmentManager fragmentManager = activity.getFragmentManager();
                    android.app.Fragment oldFragment = fragmentManager.findFragmentByTag(StringConstants.LIFECYCLE_FRAGMENT_TAG);//原有Fragment
                    android.app.Fragment fragment = new LifeCycleFragment(activity, (LifeCycleManager) manager);//新生命周期监听Fragment
                    android.app.FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
                    if (oldFragment != null) {
                        transaction.remove(oldFragment);//移除原有Fragment
                    }
                    transaction.add(fragment, StringConstants.LIFECYCLE_FRAGMENT_TAG);//绑定生命周期监听Fragment
                    try {
                        transaction.commitAllowingStateLoss();
                    } catch (Exception e){
                        if (!isDestroyed(fragmentManager)) {
                            /*
                             * 在Activity或FragmentActivity初始化之前，不可以使用LifeCycleUtils绑定生命周期，
                             * 一般建议在Context.onCreate()方法中使用
                             */
                            throw new RuntimeException("You should use LifeCycleUtils in Context.onCreate() method or after onCreate()", e);
                        }
                        //通常在Activity.onDestroy后提交会抛出异常
                        fragment.onDestroy();//销毁Fragment, 同时会销毁Manager
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        return (LifeCycleManager) manager;
    }

    private static LifeCycleManager getLifeCycleManagerV4(android.support.v4.app.FragmentActivity fragmentActivity){
        //ParasiticVars中获取FragmentActivity的生命周期管理器
        Object manager = ParasiticVars.get(fragmentActivity, StringConstants.LIFECYCLE_MANAGER_TAG);

        if (!(manager instanceof LifeCycleManager)) {
            try {
                lock.lock();
                //ParasiticVars中获取FragmentActivity的生命周期管理器
                manager = ParasiticVars.get(fragmentActivity, StringConstants.LIFECYCLE_MANAGER_TAG);
                //生命周期管理器不存在则新建
                if (!(manager instanceof LifeCycleManager)){
                    manager = new LifeCycleManagerImpl();//新建管理器
                    android.support.v4.app.FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
                    android.support.v4.app.Fragment oldFragment = fragmentManager.findFragmentByTag(StringConstants.LIFECYCLE_FRAGMENT_TAG);//原有Fragment
                    android.support.v4.app.Fragment fragment = new LifeCycleFragmentV4(fragmentActivity, (LifeCycleManager) manager);//新生命周期监听Fragment
                    android.support.v4.app.FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
                    if (oldFragment != null) {
                        transaction.remove(oldFragment);//移除原有Fragment
                    }
                    transaction.add(fragment, StringConstants.LIFECYCLE_FRAGMENT_TAG);//绑定生命周期监听Fragment
                    try {
                        transaction.commitAllowingStateLoss();
                    } catch (Exception e){
                        if (!isDestroyed(fragmentManager)) {
                            /*
                             * 在Activity或FragmentActivity初始化之前，不可以使用LifeCycleUtils绑定生命周期，
                             * 一般建议在Context.onCreate()方法中使用
                             */
                            throw new RuntimeException("You should use LifeCycleUtils in Context.onCreate() method or after onCreate()", e);
                        }
                        //通常在Activity.onDestroy后提交会抛出异常
                        fragment.onDestroy();//销毁Fragment, 同时会销毁Manager
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        return (LifeCycleManager) manager;
    }

    private static Field fragmentManagerIsDestroyedField;
    private static Field fragmentManagerIsDestroyedFieldV4;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("PrivateApi")
    private static boolean isDestroyed(FragmentManager fragmentManager){
        if (DeviceUtils.getVersionSDK() >= 17){
            return fragmentManager.isDestroyed();
        }
        try {
            if (fragmentManagerIsDestroyedField == null) {
                Class<?> clazz = Class.forName("android.app.FragmentManagerImpl");
                fragmentManagerIsDestroyedField = clazz.getDeclaredField("mDestroyed");
                fragmentManagerIsDestroyedField.setAccessible(true);
            }
            return (boolean) fragmentManagerIsDestroyedField.get(fragmentManager);
        } catch (Exception e) {
            TLogger.get(LifeCycleUtils.class).e("we can not get mDestroyed field in FragmentManagerImpl class", e);
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint("PrivateApi")
    private static boolean isDestroyed(android.support.v4.app.FragmentManager fragmentManager){
        if (DeviceUtils.getVersionSDK() >= 17){
            return fragmentManager.isDestroyed();
        }
        try {
            if (fragmentManagerIsDestroyedFieldV4 == null) {
                Class<?> clazz = Class.forName("android.support.v4.app.FragmentManagerImpl");
                fragmentManagerIsDestroyedFieldV4 = clazz.getDeclaredField("mDestroyed");
                fragmentManagerIsDestroyedFieldV4.setAccessible(true);
            }
            return (boolean) fragmentManagerIsDestroyedFieldV4.get(fragmentManager);
        } catch (Exception e) {
            TLogger.get(LifeCycleUtils.class).e("we can not get mDestroyed field in FragmentManagerImpl class", e);
            return true;
        }
    }

}
