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

package sviolet.demoa;

import android.os.Bundle;

import sviolet.demoa.common.DemoDescription;
import sviolet.turquoise.enhanced.TActivity;
import sviolet.turquoise.enhanced.annotation.inject.ResourceId;
import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;

/**
 * 临时调试用Activity
 */

@DemoDescription(
        title = "TempActivity",
        type = "Temp",
        info = "the activity for test"
)

@ResourceId(R.layout.temp_main)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class TempActivity extends TActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}