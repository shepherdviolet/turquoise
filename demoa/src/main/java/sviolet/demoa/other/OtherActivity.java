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

package sviolet.demoa.other;

import sviolet.demoa.GuideActivity;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.DemoList;

/**************************************************************
 * Demo配置
 */

// Demo列表
@DemoList({
        ShadowOtherActivity.class,
        GradualTitleOtherActivity.class,
        RuntimePermissionOtherActivity.class,
        MotionImageOtherActivity.class,
        MultiItemViewPagerOtherActivity.class,
        TabViewOtherActivity.class,
        TabViewHelperOtherActivity.class,
        WebViewOtherActivity.class
})

/**************************************************************
 *  Activity
 */

//Demo描述
@DemoDescription(
        title = "Other Demo",
        type = "View",
        info = "Demo of others"
)
public class OtherActivity extends GuideActivity {
}
