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

    @ResourceId(R.id.refresh_refreshindicator_listview)
    private ListView listView;
    @ResourceId(R.id.refresh_refreshindicator_container)
    private VerticalOverDragContainer container;
    @ResourceId(R.id.common_indicator_refresh)
    private SimpleVerticalRefreshIndicatorGroup refreshIndicator;//下拉刷新
    @ResourceId(R.id.common_indicator_load)
    private SimpleVerticalRefreshIndicatorGroup loadIndicator;//上拉加载

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //listView初始化
        listView.setAdapter(new EmulateListAdapter(this, 15,
                "RefreshIndicator", "10 hours ago", "pull to refresh implement by VerticalOverDragContainer + VerticalRefreshIndicatorGroup"));

        //VerticalOverDragContainer添加刷新指示器(此处为VerticalRefreshIndicatorGroup)
        container.addRefreshIndicator(refreshIndicator);//下拉刷新
        container.addRefreshIndicator(loadIndicator);//上拉加载

        //设置刷新监听器
        refreshIndicator.setRefreshListener(new SimpleVerticalRefreshIndicatorGroup.RefreshListener() {
            @Override
            public void onRefresh() {
                /**
                 * 模拟刷新流程处理, 等待刷新完成后, 必须调用refreshIndicator.reset(boolean)方法, 重置容器控件的PARK状态, 容器控件会弹回初始状态,
                 * 在状态重置前, 容器控件将不会再触发TOP PARK事件, 必须在重置状态后才能触发
                 */
                myHandler.sendEmptyMessageDelayed(MyHandler.HANDLER_REFRESH_RESET, 4000);
            }
        });

        //设置刷新监听器
        loadIndicator.setRefreshListener(new SimpleVerticalRefreshIndicatorGroup.RefreshListener() {
            @Override
            public void onRefresh() {
                /**
                 * 模拟加载流程处理, 等待加载完成后, 必须调用loadIndicator.reset(boolean)方法, 重置容器控件的PARK状态, 容器控件会弹回初始状态,
                 * 在状态重置前, 容器控件将不会再触发TOP PARK事件, 必须在重置状态后才能触发
                 */
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
                    //true:加载成功 false:加载失败
                    host.refreshIndicator.reset(true);
                    break;
                case HANDLER_LOAD_RESET:
                    //true:加载成功 false:加载失败
                    host.loadIndicator.reset(true);
                    break;
            }

        }
    }

}
