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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import sviolet.thistle.util.common.ParasiticVars;

/**
 * 生命周期监听Fragment for supportV4<p/>
 *
 * use{@link LifeCycleUtils}<p/>
 *
 * Created by S.Violet on 2015/11/24.
 */
@SuppressLint("ValidFragment")
public class LifeCycleFragmentV4 extends Fragment {

    private LifeCycleManager manager;

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

    LifeCycleFragmentV4(FragmentActivity fragmentActivity, LifeCycleManager manager){
        if (manager == null){
            throw new IllegalArgumentException("[LifeCycleFragment]LifeCycleManager is null");
        }
        this.manager = manager;
        //管理器放入寄生变量
        ParasiticVars.set(fragmentActivity, LifeCycleManager.MANAGER_TAG, manager);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        try {
            super.onActivityCreated(savedInstanceState);
        } catch (Exception ignore) {
        }
        if (invalidateSelf())
            return;
        manager.onCreate();
    }

    @Override
    public void onStart() {
        try {
            super.onStart();
        } catch (Exception ignore) {
        }
        if (invalidateSelf())
            return;
        manager.onStart();
    }

    @Override
    public void onResume() {
        try {
            super.onResume();
        } catch (Exception ignore) {
        }
        if (invalidateSelf())
            return;
        manager.onResume();
    }

    @Override
    public void onPause() {
        try {
            super.onPause();
        } catch (Exception ignore) {
        }
        if (invalidateSelf())
            return;
        manager.onPause();
    }

    @Override
    public void onStop() {
        try {
            super.onStop();
        } catch (Exception ignore) {
        }
        if (invalidateSelf())
            return;
        manager.onStop();
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
        } catch (Exception ignore) {
        }
        //寄生变量中移除管理器
        ParasiticVars.remove(getActivity(), LifeCycleManager.MANAGER_TAG);
        if (invalidateSelf())
            return;
        manager.onDestroy();
    }

    private boolean invalidateSelf(){
        if (useless){
            try {
                getFragmentManager().beginTransaction().remove(this).commit();
            } catch (Exception ignore){
            }
            return true;
        }
        return false;
    }

    public LifeCycleManager getLifeCycleManager(){
        return manager;
    }

}
