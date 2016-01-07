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

package sviolet.turquoise.utils.lifecycle.manager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import java.lang.ref.WeakReference;

import sviolet.turquoise.utils.lifecycle.ParasiticVars;
import sviolet.turquoise.utils.lifecycle.LifeCycleUtils;

/**
 * 生命周期监听Fragment for supportV4<p/>
 *
 * use{@link LifeCycleUtils}<p/>
 *
 * Created by S.Violet on 2015/11/24.
 */
@SuppressLint("ValidFragment")
public class LifeCycleFragmentV4 extends Fragment {

    private WeakReference<LifeCycleManager> manager;
    private WeakReference<FragmentActivity> activity;

    private boolean useless = false;

    /**
     * 屏幕旋转时会自动实例化并绑定原有Fragment,
     * 此类Fragment无用, 将自动从Activity中移除
     * {@link LifeCycleFragment#invalidateSelf}
     */
    @Deprecated
    public LifeCycleFragmentV4(){
        useless = true;
    }

    public LifeCycleFragmentV4(FragmentActivity activity, LifeCycleManager manager){
        if (activity == null)
            throw new NullPointerException("[LifeCycleFragmentV4]can't create without activity and manager");

        //管理器放入寄生变量
        ParasiticVars.set(activity, LifeCycleManager.MANAGER_TAG, manager);

        //弱引用
        this.activity = new WeakReference<FragmentActivity>(activity);
        this.manager = new WeakReference<LifeCycleManager>(manager);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (invalidateSelf())
            return;
        if (getWeakLifeCycleManager() != null)
            getWeakLifeCycleManager().onCreate();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (invalidateSelf())
            return;
        if (getWeakLifeCycleManager() != null)
            getWeakLifeCycleManager().onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (invalidateSelf())
            return;
        if (getWeakLifeCycleManager() != null)
            getWeakLifeCycleManager().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (invalidateSelf())
            return;
        if (getWeakLifeCycleManager() != null)
            getWeakLifeCycleManager().onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (invalidateSelf())
            return;
        if (getWeakLifeCycleManager() != null)
            getWeakLifeCycleManager().onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getWeakLifeCycleManager() != null)
            getWeakLifeCycleManager().onDestroy();
        //寄生变量中移除管理器
        if (getWeakActivity() != null)
            ParasiticVars.remove(getWeakActivity(), LifeCycleManager.MANAGER_TAG);
    }

    private FragmentActivity getWeakActivity(){
        if (activity != null)
            return activity.get();
        return null;
    }

    private LifeCycleManager getWeakLifeCycleManager(){
        if (manager != null)
            return manager.get();
        return null;
    }

    private boolean invalidateSelf(){
        if (useless){
            FragmentActivity activity = getActivity();
            if (activity != null){
                try{
                    activity.getSupportFragmentManager().beginTransaction().remove(this).commit();
                }catch (Exception ignored){
                }
            }
            return true;
        }
        return false;
    }

}
