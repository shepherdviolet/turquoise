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

package sviolet.demoa;
import sviolet.turquoise.enhance.app.MultiDexLoadingActivity;

/**
 * MultiDex加载界面
 *
 * Created by S.Violet on 2017/3/20.
 */
public class MyMultiDexLoadingActivity extends MultiDexLoadingActivity {

    @Override
    protected int getLayoutId() {
        //指定布局
        return R.layout.multidex_loading;
    }

}
