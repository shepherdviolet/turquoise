package sviolet.demoa.image;

import sviolet.demoa.GuideActivity;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.DemoList;

/**************************************************************
 * Demo配置
 */

// Demo列表
@DemoList({
        CommonImageActivity.class,
        AsyncImageActivity.class,
        Async2ImageActivity.class
})

/**************************************************************
 *  Activity
 */

//Demo描述
@DemoDescription(
        title = "image Demo",
        type = "View",
        info = "Demo of bitmap package"
)
public class ImageActivity extends GuideActivity {
}
