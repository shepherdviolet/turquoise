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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import sviolet.demoaimageloader.R;
import sviolet.demoaimageloader.common.DemoDescription;
import sviolet.demoaimageloader.custom.MyNetworkLoadHandler;
import sviolet.demoaimageloader.demos.extra.AsyncImageItem;
import sviolet.demoaimageloader.demos.extra.RoundedListAdapter;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.async.WeakHandler;
import sviolet.turquoise.ui.viewgroup.refresh.CircleDropRefreshIndicator;
import sviolet.turquoise.ui.viewgroup.refresh.VerticalOverDragContainer;
import sviolet.turquoise.util.bitmap.BitmapUtils;
import sviolet.turquoise.util.droid.MeasureUtils;
import sviolet.turquoise.x.imageloader.TILoader;
import sviolet.turquoise.x.imageloader.drawable.common.CircleLoadingAnimationDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.common.CommonLoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;

@DemoDescription(
        title = "RoundedList Demo",
        type = "",
        info = "List with rounded corner"
)

/**
 * 圆角图列表Demo
 *
 * Created by S.Violet on 2015/7/7.
 */
@ResourceId(R.layout.rounded_list_main)
@ActivitySettings(
        statusBarColor = 0xFF30C0C0,
        navigationBarColor = 0xFF30C0C0
)
public class RoundedListActivity extends TActivity {

    @ResourceId(R.id.rounded_list_main_listView)
    private ListView listView;
    @ResourceId(R.id.rounded_list_main_container)
    private VerticalOverDragContainer verticalOverDragContainer;
    @ResourceId(R.id.common_circledrop_indicator_refresh)
    private CircleDropRefreshIndicator refreshIndicator;

    private RoundedListAdapter adapter;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        Bitmap loadingBitmap = BitmapUtils.decodeFromResource(getResources(), R.mipmap.rounded_list_loading);
        loadingBitmap = BitmapUtils.toRoundedCorner(loadingBitmap, MeasureUtils.dp2px(this, 2), BitmapUtils.RoundedCornerType.TopLeft_And_TopRight, true);

        TILoader.node(this).setting(new NodeSettings.Builder()
                .setNetworkLoadHandler(new MyNetworkLoadHandler(30f, new int[]{R.mipmap.rounded_list_image1, R.mipmap.rounded_list_image2, R.mipmap.rounded_list_image3, R.mipmap.rounded_list_image4, R.mipmap.rounded_list_image5}))
                .setLoadingDrawableFactory(new CommonLoadingDrawableFactory()
                    .setImageBitmap(loadingBitmap)
                    .setAnimationDrawableFactory(new CircleLoadingAnimationDrawableFactory()
                        .setRadius(0.05f, CircleLoadingAnimationDrawableFactory.SizeUnit.PERCENT_OF_HEIGHT)
                        .setCircleStrokeWidth(0.01f, CircleLoadingAnimationDrawableFactory.SizeUnit.PERCENT_OF_HEIGHT)
                        .setProgressStrokeWidth(0.01f, CircleLoadingAnimationDrawableFactory.SizeUnit.PERCENT_OF_HEIGHT)))
                .setBackgroundColor(0xFFF0F0F0)
                .setRequestQueueSize(5)
                .build());

        //设置适配器, 传入图片加载器, 图片解码工具
        adapter = new RoundedListAdapter(this, makeItemList());
        listView.setAdapter(adapter);
        listView.setOnScrollListener(TILoader.node(this).newNodeRemoter().getPauseOnListViewScrollListener());//改善流畅度, 非必须

        //添加下拉刷新效果
        verticalOverDragContainer.addRefreshIndicator(refreshIndicator);
        refreshIndicator.setRefreshListener(new CircleDropRefreshIndicator.RefreshListener() {
            @Override
            public void onRefresh() {
                myHandler.sendEmptyMessageDelayed(MyHandler.HANDLER_REFRESH_RESET, 4000);
            }

            @Override
            public void onIgnore() {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void afterDestroy() {

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
        for (int i = 0 ; i < 1 ; i++) {
            item.setUrl(i, "http://r" + String.valueOf(id) + "-" + String.valueOf(i));
        }
        item.setTitle("RoundedList Title " + String.valueOf(id));
        item.setContent("State");
        return item;
    }

    private MyHandler myHandler = new MyHandler(this);

    private static class MyHandler extends WeakHandler<RoundedListActivity> {

        private static final int HANDLER_REFRESH_RESET = 0;//模拟刷新流程

        public MyHandler(RoundedListActivity host) {
            super(host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, RoundedListActivity host) {

            switch (msg.what){
                case HANDLER_REFRESH_RESET:
                    //刷新完必须调用reset方法重置状态
                    host.refreshIndicator.reset();
                    break;
            }

        }
    }
}
