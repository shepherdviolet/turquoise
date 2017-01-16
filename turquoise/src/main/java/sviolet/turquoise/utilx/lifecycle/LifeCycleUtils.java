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

package sviolet.turquoise.utilx.lifecycle;

import android.app.Activity;
import android.app.Fragment;

import sviolet.turquoise.util.droid.DeviceUtils;

/**
 * Context生命周期监听工具<p/>
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
        if (activity == null)
            throw new NullPointerException("[LifeCycleUtils] activity == null");
        if (lifeCycle == null)
            return;

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Activity before api 11");
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
    public static void attach(Fragment fragment, LifeCycle lifeCycle){
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

        LifeCycleManager manager = getLifeCycleManager(activity);
        manager.addWeakListener(lifeCycle);
    }

    /**
     * 监听FragmentActivity的生命周期, 弱引用持有LifeCycle实例<p/>
     *
     * @param fragmentActivity 被监听的FragmentActivity
     * @param lifeCycle 监听器(被弱引用持有)
     */
    public static void attach(android.support.v4.app.FragmentActivity fragmentActivity, LifeCycle lifeCycle){
        if (fragmentActivity == null)
            throw new NullPointerException("[LifeCycleUtils] fragmentActivity == null");
        if (lifeCycle == null)
            return;

        LifeCycleManager manager = getLifeCycleManagerV4(fragmentActivity);
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
        if (fragment == null)
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        if (lifeCycle == null)
            return;

        android.support.v4.app.FragmentActivity activity = fragment.getActivity();
        if (activity == null)
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");

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
     * @param lifeCycle 监听器(被强引用持有)
     */
    public static void addComponent(Activity activity, String componentName, LifeCycle lifeCycle){
        if (activity == null)
            throw new NullPointerException("[LifeCycleUtils] activity == null");
        if (lifeCycle == null)
            return;

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Activity before api 11");
        }

        LifeCycleManager manager = getLifeCycleManager(activity);
        manager.addListener(componentName, lifeCycle);
    }

    /**
     * 添加生命周期监听组件, 强引用持有LifeCycle实例<p/>
     *
     * 实际上是监听其对应的Activity<p/>
     *
     * @param fragment 被监听的Fragment
     * @param componentName 组件名(若和其他的组件名冲突, 之前的组件会被替换掉)
     * @param lifeCycle 监听器(被强引用持有)
     */
    public static void addComponent(Fragment fragment, String componentName, LifeCycle lifeCycle){
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

        LifeCycleManager manager = getLifeCycleManager(activity);
        manager.addListener(componentName, lifeCycle);
    }

    /**
     * 添加生命周期监听组件, 强引用持有LifeCycle实例<p/>
     *
     * @param fragmentActivity 被监听的FragmentActivity
     * @param componentName 组件名(若和其他的组件名冲突, 之前的组件会被替换掉)
     * @param lifeCycle 监听器(被强引用持有)
     */
    public static void addComponent(android.support.v4.app.FragmentActivity fragmentActivity, String componentName, LifeCycle lifeCycle){
        if (fragmentActivity == null)
            throw new NullPointerException("[LifeCycleUtils] fragmentActivity == null");
        if (lifeCycle == null)
            return;

        LifeCycleManager manager = getLifeCycleManagerV4(fragmentActivity);
        manager.addListener(componentName, lifeCycle);
    }

    /**
     * 添加生命周期监听组件, 强引用持有LifeCycle实例<p/>
     *
     * 实际上是监听其对应的Activity<p/>
     *
     * @param fragment 被监听的Fragment
     * @param componentName 组件名(若和其他的组件名冲突, 之前的组件会被替换掉)
     * @param lifeCycle 监听器(被强引用持有)
     */
    public static void addComponent(android.support.v4.app.Fragment fragment, String componentName, LifeCycle lifeCycle){
        if (fragment == null)
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        if (lifeCycle == null)
            return;

        android.support.v4.app.FragmentActivity activity = fragment.getActivity();
        if (activity == null)
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");

        LifeCycleManager manager = getLifeCycleManagerV4(activity);
        manager.addListener(componentName, lifeCycle);
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
        if (activity == null)
            throw new NullPointerException("[LifeCycleUtils] activity == null");

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Activity before api 11");
        }

        LifeCycleManager manager = getLifeCycleManager(activity);
        manager.removeListener(componentName);
    }

    /**
     * 移除生命周期监听组件<p/>
     *
     * @param fragment 被监听的Fragment
     * @param componentName 组件名
     */
    public static void removeComponent(Fragment fragment, String componentName){
        if (fragment == null)
            throw new NullPointerException("[LifeCycleUtils] fragment == null");

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Fragment before api 11");
        }

        Activity activity = fragment.getActivity();
        if (activity == null)
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");

        LifeCycleManager manager = getLifeCycleManager(activity);
        manager.removeListener(componentName);
    }

    /**
     * 移除生命周期监听组件<p/>
     *
     * @param fragmentActivity 被监听的FragmentActivity
     * @param componentName 组件名
     */
    public static void removeComponent(android.support.v4.app.FragmentActivity fragmentActivity, String componentName){
        if (fragmentActivity == null)
            throw new NullPointerException("[LifeCycleUtils] fragmentActivity == null");

        LifeCycleManager manager = getLifeCycleManagerV4(fragmentActivity);
        manager.removeListener(componentName);
    }

    /**
     * 移除生命周期监听组件<p/>
     *
     * @param fragment 被监听的Fragment
     * @param componentName 组件名
     */
    public static void removeComponent(android.support.v4.app.Fragment fragment, String componentName){
        if (fragment == null)
            throw new NullPointerException("[LifeCycleUtils] fragment == null");

        android.support.v4.app.FragmentActivity activity = fragment.getActivity();
        if (activity == null)
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");

        LifeCycleManager manager = getLifeCycleManagerV4(activity);
        manager.removeListener(componentName);
    }

    /**
     * 移除生命周期监听组件<p/>
     *
     * @param activity 被监听的Activity
     * @param lifeCycle 监听器
     */
    public static void removeComponent(Activity activity, LifeCycle lifeCycle){
        if (activity == null)
            throw new NullPointerException("[LifeCycleUtils] activity == null");
        if (lifeCycle == null)
            return;

        if (DeviceUtils.getVersionSDK() < 11){
            throw new RuntimeException("[LifeCycleUtils]can't use android.app.Activity before api 11");
        }

        LifeCycleManager manager = getLifeCycleManager(activity);
        manager.removeListener(lifeCycle);
    }

    /**
     * 移除生命周期监听组件<p/>
     *
     * @param fragment 被监听的Fragment
     * @param lifeCycle 监听器
     */
    public static void removeComponent(Fragment fragment, LifeCycle lifeCycle){
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

        LifeCycleManager manager = getLifeCycleManager(activity);
        manager.removeListener(lifeCycle);
    }

    /**
     * 移除生命周期监听组件<p/>
     *
     * @param fragmentActivity 被监听的FragmentActivity
     * @param lifeCycle 监听器
     */
    public static void removeComponent(android.support.v4.app.FragmentActivity fragmentActivity, LifeCycle lifeCycle){
        if (fragmentActivity == null)
            throw new NullPointerException("[LifeCycleUtils] fragmentActivity == null");
        if (lifeCycle == null)
            return;

        LifeCycleManager manager = getLifeCycleManagerV4(fragmentActivity);
        manager.removeListener(lifeCycle);
    }

    /**
     * 移除生命周期监听组件<p/>
     *
     * @param fragment 被监听的Fragment
     * @param lifeCycle 监听器
     */
    public static void removeComponent(android.support.v4.app.Fragment fragment, LifeCycle lifeCycle){
        if (fragment == null)
            throw new NullPointerException("[LifeCycleUtils] fragment == null");
        if (lifeCycle == null)
            return;

        android.support.v4.app.FragmentActivity activity = fragment.getActivity();
        if (activity == null)
            throw new RuntimeException("[LifeCycleUtils]can't attach fragment without host activity");

        LifeCycleManager manager = getLifeCycleManagerV4(activity);
        manager.removeListener(lifeCycle);
    }

    /*************************************************************************************
     * inner
     */

    private static LifeCycleManager getLifeCycleManager(Activity activity){
        //生命周期Fragment
        android.app.Fragment oldFragment = activity.getFragmentManager().findFragmentByTag(LifeCycleManager.FRAGMENT_TAG);
        //生命周期管理器
        LifeCycleManager manager = null;

        //获取生命周期管理器
        if (oldFragment instanceof LifeCycleFragment){
            manager = ((LifeCycleFragment) oldFragment).getLifeCycleManager();
        }

        //生命周期管理器不存在则新建
        if (manager == null){
            manager = new LifeCycleManagerImpl();//新建管理器
            android.app.Fragment fragment = new LifeCycleFragment(manager);//新生命周期监听Fragment
            android.app.FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
            if (oldFragment != null) {
                transaction.remove(oldFragment);//移除原有Fragment
            }
            transaction.add(fragment, LifeCycleManager.FRAGMENT_TAG);//绑定生命周期监听Fragment
            transaction.commit();
        }
        return manager;
    }

    private static LifeCycleManager getLifeCycleManagerV4(android.support.v4.app.FragmentActivity fragmentActivity){
        //生命周期Fragment
        android.support.v4.app.Fragment oldFragment = fragmentActivity.getSupportFragmentManager().findFragmentByTag(LifeCycleManager.FRAGMENT_TAG);
        //生命周期管理器
        LifeCycleManager manager = null;

        //获取生命周期管理器
        if (oldFragment instanceof LifeCycleFragmentV4){
            manager = ((LifeCycleFragmentV4) oldFragment).getLifeCycleManager();
        }

        //生命周期管理器不存在则新建
        if (manager == null){
            manager = new LifeCycleManagerImpl();//新建管理器
            android.support.v4.app.Fragment fragment = new LifeCycleFragmentV4(manager);//新生命周期监听Fragment
            android.support.v4.app.FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
            if (oldFragment != null) {
                transaction.remove(oldFragment);//移除原有Fragment
            }
            transaction.add(fragment, LifeCycleManager.FRAGMENT_TAG);//绑定生命周期监听Fragment
            transaction.commit();
        }
        return manager;
    }

}
