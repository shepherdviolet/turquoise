package sviolet.demoa.slide;

import android.os.Bundle;
import android.widget.ListView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.EmulateListAdapter;
import sviolet.turquoise.annotation.ActivitySettings;
import sviolet.turquoise.annotation.ResourceId;
import sviolet.turquoise.app.TActivity;
import sviolet.turquoise.view.slide.view.RelativeLayoutDrawer;

@DemoDescription(
        title = "Drawer",
        type = "View",
        info = "this is a Drawer demo made by turquoise.view.slide"
)

/**
 * 抽屉控件Demo
 */

@ResourceId(R.layout.slide_drawer)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class DrawerSlideActivity extends TActivity {

    @ResourceId(R.id.slide_drawer_listview)
    private ListView backgroundListView;
    @ResourceId(R.id.slide_drawer_leftdrawer)
    private RelativeLayoutDrawer leftdrawer;
    @ResourceId(R.id.slide_drawer_leftdrawer_listview)
    private ListView leftdrawerListview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backgroundListView.setAdapter(new EmulateListAdapter(this, 30,
                "DrawerBackgroundList", "10 hours ago", "this is a Drawer demo made by turquoise.view.slide"));
        leftdrawerListview.setAdapter(new EmulateListAdapter(this, 5, "Title", null, null));

        //抽屉滑动配置
        leftdrawer
                .setSlideScrollDirection(RelativeLayoutDrawer.DIRECTION_LEFT)//设置抽屉方向
                .setSlideScrollDuration(700)//设置惯性滑动时间
                .setSlideDrawerWidth(280)//设置抽屉宽度
                .setSlideHandleWidth(50)//设置把手宽度(dp)
                .setSlideInitStage(RelativeLayoutDrawer.INIT_STAGE_PUSH_IN)//设置默认状态:拉出
                .setSlideOverScrollEnabled(true)//设置允许越界拖动
                .setHandleFeedback(true)//把手触摸反馈
                .setHandleFeedbackRange(25)//把手触摸反馈幅度
//			.setOnHandleClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					leftdrawer.pullOut(true);
//				}
//			})
//			.setOnHandleLongPressListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {

//				}
//			})
                .applySlideSetting();//应用设置

    }

}
