package sviolet.demoa.slide;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.EmulateListAdapter;
import sviolet.demoa.slide.view.MySlideView;
import sviolet.demoa.slide.view.ZoomRelativeLayoutDrawer;
import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhanced.annotation.inject.ResourceId;
import sviolet.turquoise.enhanced.TActivity;
import sviolet.turquoise.utils.sys.MeasureUtils;

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
                .setSlideInitStage(ZoomRelativeLayoutDrawer.STAGE_PULL_OUT)//设置默认状态:拉出
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
         * 设置内部引擎, 当内部引擎截获事件后, 会阻断外部引擎的拦截,
         * 防止内部控件滑动时, 外部控件滑动
         */
        drawer.getSlideEngine().addInnerEngine(drawerMyslideview.getSlideEngine());

        /**
         * 背景ListView
         */
        backgroundListView.setAdapter(new EmulateListAdapter(this, 5, "      Menu", null, null, 0xF0FFFFFF));

    }

}
