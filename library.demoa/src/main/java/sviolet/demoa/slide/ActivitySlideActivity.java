package sviolet.demoa.slide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.EmulateListAdapter;
import sviolet.turquoise.annotation.ActivitySettings;
import sviolet.turquoise.annotation.ResourceId;
import sviolet.turquoise.app.TActivity;

/**
 * 可侧滑关闭Activity的demo(主界面)
 *
 * Created by S.Violet on 2015/6/30.
 */

@DemoDescription(
        title = "SlideActivity",
        type = "View",
        info = "slide to finish an Activity"
)

@ResourceId(R.layout.slide_activity_main)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class ActivitySlideActivity extends TActivity {

    @ResourceId(R.id.slide_activity_main_listview)
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listView.setAdapter(new EmulateListAdapter(this, 30, "title", "type", "content.............................................."));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //右侧滑入动画
                startActivity(new Intent(ActivitySlideActivity.this, SubActivitySlideActivity.class));
                overridePendingTransition(R.anim.in_from_right, 0);
            }
        });
    }
}
