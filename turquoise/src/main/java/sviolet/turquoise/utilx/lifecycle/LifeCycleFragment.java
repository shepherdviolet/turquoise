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
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import sviolet.turquoise.util.common.ParasiticVars;

/**
 * 生命周期监听Fragment<p/>
 *
 * use{@link LifeCycleUtils}<p/>
 *
 * Created by S.Violet on 2015/11/24.
 */
@SuppressLint("ValidFragment")
class LifeCycleFragment extends Fragment {

    private LifeCycleManager manager;

    private boolean useless = false;

    /**
     * 屏幕旋转时会自动实例化并绑定原有Fragment,
     * 此类Fragment无用, 将自动从Activity中移除
     * {@link LifeCycleFragment#invalidateSelf}
     */
    @Deprecated
    public LifeCycleFragment(){
        useless = true;
    }

    LifeCycleFragment(Activity activity, LifeCycleManager manager){
        if (manager == null){
            throw new IllegalArgumentException("[LifeCycleFragment]LifeCycleManager is null");
        }
        this.manager = manager;
        //管理器放入寄生变量
        ParasiticVars.set(activity, LifeCycleManager.MANAGER_TAG, manager);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (invalidateSelf())
            return;
        manager.onCreate();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (invalidateSelf())
            return;
        manager.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (invalidateSelf())
            return;
        manager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (invalidateSelf())
            return;
        manager.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (invalidateSelf())
            return;
        manager.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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