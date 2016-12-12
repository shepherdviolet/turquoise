/*
 * Copyright (C) 2015-2016 S.Violet
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

package sviolet.turquoise.enhance.app;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sviolet.turquoise.enhance.app.utils.InjectUtils;

/**
 * [组件扩展]Fragment<br>
 * <br>
 * 0.InjectUtils注释式注入控件对象/绑定监听<br/>
 * {@link InjectUtils};<br/>
 * <br>
 *
 * @author S.Violet
 */
@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public abstract class TFragment extends Fragment {

    private View fragmentViewCache;

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //get cache
        View fragmentView = fragmentViewCacheEnabled() ? this.fragmentViewCache : null;

        //create
        if(fragmentView == null){
            fragmentView = InjectUtils.inject(this, inflater, container);
            onInitView(fragmentView, savedInstanceState);
            if (fragmentViewCacheEnabled()){
                this.fragmentViewCache = fragmentView;
            }
        }

        //clear parent
        ViewGroup parent = (ViewGroup) fragmentView.getParent();
        if (parent != null) {
            parent.removeView(fragmentView);
        }

        //refresh
        onRefreshView(fragmentView, savedInstanceState);

        return fragmentView;
    }

    /**
     * 复写该方法实现View的创建, View复用模式下只会调用一次, 非复用模式下, 每次都会调用
     */
    protected abstract void onInitView(View fragmentView, Bundle savedInstanceState);

    /**
     * 复写该方法实现View的刷新, 每次都会调用
     */
    protected abstract void onRefreshView(View fragmentView, Bundle savedInstanceState);

    /**
     * <p>true:Fragment复用同一个view, onCreateView方法只会创建一次View, 流畅, 但是费内存</p>
     * <p>false:Fragment默认的机制, 每次attach都会执行onCreateView并创建新的View, 省内存, 但是卡顿</p>
     */
    protected abstract boolean fragmentViewCacheEnabled();

}
