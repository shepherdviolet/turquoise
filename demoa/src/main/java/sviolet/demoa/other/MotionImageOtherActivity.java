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

package sviolet.demoa.other;

import android.os.Bundle;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;

@DemoDescription(
        title = "Gesture Ctrl Demo",
        type = "Other",
        info = "gesture controller demo"
)

/**
 * 手势控制器Demo<br/>
 *
 * Created by S.Violet on 2015/7/7.
 */
@ResourceId(R.layout.other_motion_image)
@ActivitySettings(
        statusBarColor = 0xFF30C0C0,
        navigationBarColor = 0xFF30C0C0
)
public class MotionImageOtherActivity extends TActivity {

    @Override
    protected void onInitViews(Bundle savedInstanceState) {

    }

}
