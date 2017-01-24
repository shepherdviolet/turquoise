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
import android.widget.Toast;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.EmulateListAdapter;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.common.WeakHandler;
import sviolet.turquoise.ui.viewgroup.refresh.CircleDropRefreshIndicator;
import sviolet.turquoise.ui.viewgroup.refresh.VerticalOverDragContainer;

@DemoDescription(
        title = "CircleDropRefreshIndicator Demo",
        type = "View",
        info = "pull to refresh implement by VerticalOverDragContainer + CircleDropRefreshIndicator"
)

/**
 * VerticalOverDragContainer+CircleDropRefreshIndicator实现下拉刷新<br/>
 *
 * Created by S.Violet on 2016/11/10
 */
@ResourceId(R.layout.refresh_circledrop)
@ActivitySettings(
        statusBarColor = 0xFF30C0C0,
        navigationBarColor = 0xFF30C0C0
)
public class CircleDropIndicatorRefreshActivity extends TActivity {

    @ResourceId(R.id.refresh_circledrop_listview)
    private ListView listView;
    @ResourceId(R.id.refresh_circledrop_container)
    private VerticalOverDragContainer container;
    @ResourceId(R.id.common_circledrop_indicator_refresh)
    private CircleDropRefreshIndicator refreshIndicator;//下拉刷新
    @ResourceId(R.id.common_circledrop_indicator_load)
    private CircleDropRefreshIndicator loadIndicator;//下拉刷新

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        //listView初始化
        listView.setAdapter(new EmulateListAdapter(this, 15,
                "CircleDropRefresh", "10 hours ago", "pull to refresh pull to refresh pull to refresh pull to refresh pull to refresh pull to refresh "));

        //VerticalOverDragContainer添加刷新指示器(此处为CircleDropRefreshIndicator)
        container.addRefreshIndicator(refreshIndicator);//下拉刷新
        container.addRefreshIndicator(loadIndicator);//上拉加载
        container.setParkInterval(10000);//最短刷新间隔

        //设置刷新监听器
        refreshIndicator.setRefreshListener(new CircleDropRefreshIndicator.RefreshListener() {
            @Override
            public void onRefresh() {
                /**
                 * 模拟刷新流程处理, 等待刷新完成后, 必须调用refreshIndicator.reset()方法, 重置容器控件的PARK状态, 容器控件会弹回初始状态,
                 * 在状态重置前, 容器控件将不会再触发TOP PARK事件, 必须在重置状态后才能触发
                 */
                myHandler.sendEmptyMessageDelayed(MyHandler.HANDLER_REFRESH_RESET, 4000);
            }

            @Override
            public void onIgnore() {
                Toast.makeText(CircleDropIndicatorRefreshActivity.this, "刷新太频繁啦", Toast.LENGTH_SHORT).show();
            }
        });

        //设置刷新监听器
        loadIndicator.setRefreshListener(new CircleDropRefreshIndicator.RefreshListener() {
            @Override
            public void onRefresh() {
                /**
                 * 模拟加载流程处理, 等待加载完成后, 必须调用loadIndicator.reset()方法, 重置容器控件的PARK状态, 容器控件会弹回初始状态,
                 * 在状态重置前, 容器控件将不会再触发TOP PARK事件, 必须在重置状态后才能触发
                 */
                myHandler.sendEmptyMessageDelayed(MyHandler.HANDLER_LOAD_RESET, 4000);
            }

            @Override
            public void onIgnore() {
                Toast.makeText(CircleDropIndicatorRefreshActivity.this, "刷新太频繁啦", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private MyHandler myHandler = new MyHandler(this);

    private static class MyHandler extends WeakHandler<CircleDropIndicatorRefreshActivity> {

        private static final int HANDLER_REFRESH_RESET = 0;
        private static final int HANDLER_LOAD_RESET = 1;

        public MyHandler(CircleDropIndicatorRefreshActivity host) {
            super(host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, CircleDropIndicatorRefreshActivity host) {
            switch (msg.what){
                case HANDLER_REFRESH_RESET:
                    //true:加载成功 false:加载失败
                    host.refreshIndicator.reset();//必须调用该方法重置
                    break;
                case HANDLER_LOAD_RESET:
                    //true:加载成功 false:加载失败
                    host.loadIndicator.reset();//必须调用该方法重置
                    break;
            }

        }
    }

}
