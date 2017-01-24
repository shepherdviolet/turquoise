package sviolet.demoa.other;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.other.utils.GradualTitleListAdapter;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.ui.util.ListViewUtils;
import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.util.droid.MeasureUtils;

/**
 * <p>标题渐变效果demo</p>
 *
 * Created by S.Violet on 2016/3/9.
 */

@DemoDescription(
        title = "Gradual Title Demo",
        type = "Other",
        info = "demo of Gradual Title"
)

@ResourceId(R.layout.other_gradualtitle_main)
@ActivitySettings(
        noTitle = true,
        translucentStatus = true,
        statusBarColor = 0x40000000,
        navigationBarColor = 0xFF30C0C0
)
public class GradualTitleOtherActivity extends TActivity {

    @ResourceId(R.id.other_gradualtitle_main_listview)
    private ListView listView;
    @ResourceId(R.id.other_gradualtitle_main_statusbackground)
    private View statusBackground;
    @ResourceId(R.id.other_gradualtitle_main_title)
    private View title;

    //标题透明度
    private int titleAlpha = 0;
    //标题颜色
    private int titleColor = 0;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        //初始化标题颜色
        titleColor = (getResources().getColor(R.color.turquoise) << 8) >>> 8;

        //API21以上显示状态栏背景
        if (DeviceUtils.getVersionSDK() >= Build.VERSION_CODES.LOLLIPOP){
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) statusBackground.getLayoutParams();
            params.height = MeasureUtils.getStatusBarHeight(this);
            statusBackground.setLayoutParams(params);
            statusBackground.setVisibility(View.VISIBLE);
        }

        //适配器
        listView.setAdapter(new GradualTitleListAdapter(this, 30, "title", "type", "content"));

        //滚动监听
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //计算滚动进度
                float progress = ListViewUtils.scrollProgressFromTop(view);
                //计算透明度
                int alpha = (int) (progress * 1.5f * 255);
                if (alpha > 255) {
                    alpha = 255;
                }
                //过滤透明度不变的情况
                if (titleAlpha == alpha){
                    return;
                }
                titleAlpha = alpha;

                //合成颜色
                int color = titleColor | (alpha << 24);

                //标题栏颜色改变
                title.setBackgroundColor(color);
                //状态栏背景颜色改变
                if (DeviceUtils.getVersionSDK() >= Build.VERSION_CODES.LOLLIPOP) {
                    statusBackground.setBackgroundColor(color);
                }
            }
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
        });

    }
}
