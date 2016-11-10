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

package sviolet.demoa.refresh;

import android.os.Bundle;
import android.os.Message;
import android.widget.ListView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.EmulateListAdapter;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.common.WeakHandler;
import sviolet.turquoise.ui.viewgroup.refresh.SimpleVerticalRefreshIndicatorGroup;
import sviolet.turquoise.ui.viewgroup.refresh.VerticalOverDragContainer;
import sviolet.turquoise.utilx.tlogger.TLogger;

@DemoDescription(
        title = "RefreshIndicator Demo",
        type = "View",
        info = "pull to refresh implement by VerticalOverDragContainer + VerticalRefreshIndicatorGroup"
)

/**
 * VerticalOverDragContainer+VerticalRefreshIndicatorGroup实现简单的下拉刷新上拉加载<br/>
 *
 * Created by S.Violet on 2016/11/10
 */
@ResourceId(R.layout.refresh_refreshindicator)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class RefreshIndicatorRefreshActivity extends TActivity {

    @ResourceId(R.id.refresh_indicator_listview)
    private ListView listView;
    @ResourceId(R.id.refresh_indicator_container)
    private VerticalOverDragContainer container;
    @ResourceId(R.id.refresh_indicator_refreshindicator)
    private SimpleVerticalRefreshIndicatorGroup refreshIndicator;
    @ResourceId(R.id.refresh_indicator_loadindicator)
    private SimpleVerticalRefreshIndicatorGroup loadIndicator;

    private TLogger logger = TLogger.get(this, "OverDragDemo");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //listView初始化
        listView.setAdapter(new EmulateListAdapter(this, 15,
                "RefreshIndicator", "10 hours ago", "pull to refresh implement by VerticalOverDragContainer + VerticalRefreshIndicatorGroup"));

        container.addRefreshIndicator(refreshIndicator);
        container.addRefreshIndicator(loadIndicator);

        refreshIndicator.setRefreshListener(new SimpleVerticalRefreshIndicatorGroup.RefreshListener() {
            @Override
            public void onRefresh() {
                myHandler.sendEmptyMessageDelayed(MyHandler.HANDLER_REFRESH_RESET, 4000);
            }
        });

        loadIndicator.setRefreshListener(new SimpleVerticalRefreshIndicatorGroup.RefreshListener() {
            @Override
            public void onRefresh() {
                myHandler.sendEmptyMessageDelayed(MyHandler.HANDLER_LOAD_RESET, 4000);
            }
        });

    }

    private MyHandler myHandler = new MyHandler(this);

    private static class MyHandler extends WeakHandler<RefreshIndicatorRefreshActivity> {

        private static final int HANDLER_REFRESH_RESET = 0;
        private static final int HANDLER_LOAD_RESET = 1;

        public MyHandler(RefreshIndicatorRefreshActivity host) {
            super(host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, RefreshIndicatorRefreshActivity host) {

            switch (msg.what){
                case HANDLER_REFRESH_RESET:
                    host.refreshIndicator.reset(true);
                    break;
                case HANDLER_LOAD_RESET:
                    host.loadIndicator.reset(true);
                    break;
            }

        }
    }

}
