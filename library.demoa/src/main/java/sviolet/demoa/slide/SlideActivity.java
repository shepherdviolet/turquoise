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
 */

package sviolet.demoa.slide;

import sviolet.demoa.GuideActivity;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.DemoList;

/**************************************************************
 * Demo配置
 */

// Demo列表
@DemoList({
        DrawerSlideActivity.class,
        ZoomDrawerSlideActivity.class,
        FlingSlideActivity.class,
        CardSlideActivity.class,
        ListSlideActivity.class,
        ActivitySlideActivity.class
})

/**************************************************************
 *  Activity
 */

//Demo描述
@DemoDescription(
        title = "Slide Demo",
        type = "View",
        info = "Demo of SlideEngine"
)

public class SlideActivity extends GuideActivity {

}