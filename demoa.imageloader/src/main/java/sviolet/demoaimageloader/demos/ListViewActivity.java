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

package sviolet.demoaimageloader.demos;

import android.os.Bundle;
import android.os.Message;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import sviolet.demoaimageloader.R;
import sviolet.demoaimageloader.common.DemoDescription;
import sviolet.demoaimageloader.demos.extra.AsyncImageItem;
import sviolet.demoaimageloader.demos.extra.ListViewAdapter;
import sviolet.demoaimageloader.custom.MyNetworkLoadHandler;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.common.WeakHandler;
import sviolet.turquoise.ui.viewgroup.refresh.CircleDropRefreshIndicator;
import sviolet.turquoise.ui.viewgroup.refresh.VerticalOverDragContainer;
import sviolet.turquoise.x.imageloader.TILoader;
import sviolet.turquoise.x.imageloader.drawable.common.CommonLoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;

@DemoDescription(
        title = "ListView Usage",
        type = "",
        info = "Loading images in ListView by TILoader"
)

/**
 *
 * Created by S.Violet on 2015/7/7.
 */
@ResourceId(R.layout.list_view_main)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class ListViewActivity extends TActivity {

    @ResourceId(R.id.list_view_main_listview)
    private ListView listView;
    @ResourceId(R.id.list_view_main_container)
    private VerticalOverDragContainer verticalOverDragContainer;
    @ResourceId(R.id.common_circledrop_indicator_refresh)
    private CircleDropRefreshIndicator refreshIndicator;

    private ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TILoader.node(this).setting(new NodeSettings.Builder()
                .setNetworkLoadHandler(new MyNetworkLoadHandler())
                .setLoadingDrawableFactory(new CommonLoadingDrawableFactory().setAnimationEnabled(true).setBackgroundColor(0xFFF0F0F0))
                .setBackgroundColor(0xFFF0F0F0)
                .setRequestQueueSize(20)
                .build());

        //设置适配器, 传入图片加载器, 图片解码工具
        adapter = new ListViewAdapter(this, makeItemList());
        listView.setAdapter(adapter);
        listView.setOnScrollListener(TILoader.node(this).newNodeRemoter().getPauseOnListViewScrollListener());//改善流畅度, 非必须

        //添加下拉刷新效果
        verticalOverDragContainer.addRefreshIndicator(refreshIndicator);
        refreshIndicator.setRefreshListener(new CircleDropRefreshIndicator.RefreshListener() {
            @Override
            public void onRefresh() {
                myHandler.sendEmptyMessageDelayed(MyHandler.HANDLER_REFRESH_RESET, 4000);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /****************************************************
     * 模拟数据生成
     */

    private List<AsyncImageItem> makeItemList(){
        List<AsyncImageItem> list = new ArrayList<>();
        for (int i = 0 ; i < 100 ; i++){
            list.add(makeItem(i));
        }
        return list;
    }

    private AsyncImageItem makeItem(int id){
        AsyncImageItem item = new AsyncImageItem();
        for (int i = 0 ; i < 5 ; i++) {
            item.setUrl(i, "http://" + String.valueOf(id) + "-" + String.valueOf(i));
        }
        item.setTitle("List view usage title " + String.valueOf(id));
        item.setContent("This is a demo of how to loading images in list view with turquoise image loader.");
        return item;
    }

    private MyHandler myHandler = new MyHandler(this);

    private static class MyHandler extends WeakHandler<ListViewActivity> {

        private static final int HANDLER_REFRESH_RESET = 0;//模拟刷新流程

        public MyHandler(ListViewActivity host) {
            super(host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, ListViewActivity host) {

            switch (msg.what){
                case HANDLER_REFRESH_RESET:
                    //刷新完必须调用reset方法重置状态
                    host.refreshIndicator.reset();
                    break;
            }

        }
    }

}
