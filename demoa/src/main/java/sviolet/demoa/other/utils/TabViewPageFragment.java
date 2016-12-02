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

package sviolet.demoa.other.utils;

import android.os.Bundle;
import android.view.View;

import sviolet.demoa.R;
import sviolet.turquoise.enhance.app.TFragmentV4;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;

/**
 * Created by S.Violet on 2016/12/2.
 */
@ResourceId(R.layout.other_tab_view_page)
public class TabViewPageFragment extends TFragmentV4 {

    @ResourceId(R.id.other_tab_view_page_background)
    private View background;

    @Override
    protected void afterCreateView(View fragmentView, Bundle savedInstanceState) {
        super.afterCreateView(fragmentView, savedInstanceState);

        Bundle bundle = getArguments();
        background.setBackgroundColor(bundle.getInt("color"));
    }
}
