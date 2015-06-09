package sviolet.demoa.slide;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.EmulateListAdapter;
import sviolet.demoa.slide.sviolet.demoa.slide.view.MySlideView;
import sviolet.demoa.slide.sviolet.demoa.slide.view.ZoomRelativeLayoutDrawer;
import sviolet.turquoise.annotation.ActivitySettings;
import sviolet.turquoise.annotation.ResourceId;
import sviolet.turquoise.app.TActivity;
import sviolet.turquoise.utils.MeasureUtils;
import sviolet.turquoise.view.slide.view.RelativeLayoutDrawer;

@DemoDescription(
        title = "ZoomDrawer",
        type = "View",
        info = "this is a ZoomDrawer demo made by turquoise.view.slide"
)

/**
 * 抽屉控件Demo
 */

@ResourceId(R.layout.slide_zoomdrawer)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class ZoomDrawerSlideActivity extends TActivity {

    @ResourceId(R.id.slide_zoomdrawer_drawer)
    private ZoomRelativeLayoutDrawer drawer;
    @ResourceId(R.id.slide_zoomdrawer_drawer_listview)
    private ListView drawerListview;
    @ResourceId(R.id.slide_zoomdrawer_drawer_myslideview)
    private MySlideView drawerMyslideview;
    @ResourceId(R.id.slide_zoomdrawer_background)
    private ListView backgroundListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //缩放抽屉配置
        drawer
                .setSlideScrollDirection(ZoomRelativeLayoutDrawer.DIRECTION_RIGHT)//设置抽屉方向
                .setSlideScrollDuration(700)//设置惯性滑动时间
                .setSlideDrawerWidth(MeasureUtils.getScreenWidthDp(getApplicationContext()) - 90)//设置抽屉宽度
                .setSlideInitStage(ZoomRelativeLayoutDrawer.INIT_STAGE_PULL_OUT)//设置默认状态:拉出
                .applySlideSetting();//应用设置

        //抽屉中的ListView
        drawerListview.setAdapter(new EmulateListAdapter(this, 30,
                "ZoomDrawer", "type", "this is a ZoomDrawer demo made by turquoise.view.slide"));
        drawerListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (drawer.getSlideEngine().getCurrentStage() < 0.1f) {
                    drawer.pullOut();
                }else {
                    Toast.makeText(getApplicationContext(), "click", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**
         * 在里层SlideView hold事件时, 拦截外层的本次后续事件,
         * 防止里层在滑动时, 外层滑动拦截掉里层事件
         */
        drawerMyslideview.setOnGestureHoldListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.getGestureDriver().otherIntercepted();
            }
        });

        /**
         * 背景ListView
         */

        backgroundListView.setAdapter(new EmulateListAdapter(this, 5, "      Menu", null, null, 0xF0FFFFFF));

    }

}
