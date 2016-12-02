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
public class TFragment extends Fragment {

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = InjectUtils.inject(this, inflater, container);
        afterCreateView(view, savedInstanceState);
        return view;
    }

    protected void afterCreateView(View fragmentView, Bundle savedInstanceState){

    }

}
