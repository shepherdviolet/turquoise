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