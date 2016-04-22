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
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import sviolet.demoaimageloader.R;
import sviolet.demoaimageloader.common.DemoDescription;
import sviolet.demoaimageloader.demos.extra.AsyncImageItem;
import sviolet.demoaimageloader.demos.extra.ListViewAdapter;
import sviolet.demoaimageloader.demos.extra.MyNetworkLoadHandler;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.x.imageloader.TILoader;
import sviolet.turquoise.x.imageloader.drawable.def.CommonLoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;

@DemoDescription(
        title = "ListViewDemo",
        type = "",
        info = "loading image in ListView by TILoader"
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

    private ListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TILoader.node(this).setting(new NodeSettings.Builder()
                .setNetworkLoadHandler(new MyNetworkLoadHandler())
                .setLoadingDrawableFactory(new CommonLoadingDrawableFactory().setAnimationEnabled(true).setBackgroundColor(0xFFF0F0F0))
                .setBackgroundColor(0xFFF0F0F0)
                .setMemoryQueueSize(15)
                .setDiskQueueSize(20)
                .setNetQueueSize(25)
                .build());

        //设置适配器, 传入图片加载器, 图片解码工具
        adapter = new ListViewAdapter(this, makeItemList());
        listView.setAdapter(adapter);
        listView.setOnScrollListener(TILoader.node(this).newPauseOnListScrollListener());
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
        item.setTitle("Title of ListViewDemo " + String.valueOf(id));
        item.setContent("Content of ListViewDemo content of ListViewDemo content of ListViewDemo " + String.valueOf(id));
        return item;
    }
}
