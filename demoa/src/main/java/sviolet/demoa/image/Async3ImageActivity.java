/*
 * Copyright (C) 2015 S.Violet
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

package sviolet.demoa.image;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.image.utils.AsyncImageAdapter3;
import sviolet.demoa.image.utils.AsyncImageItem;
import sviolet.demoa.image.utils.MyNetLoadHandler;
import sviolet.turquoise.enhanced.TActivity;
import sviolet.turquoise.enhanced.annotation.inject.ResourceId;
import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;
import sviolet.turquoise.utils.bitmap.BitmapUtils;
import sviolet.turquoise.utils.bitmap.loader.SimpleBitmapLoader;
import sviolet.turquoise.utils.bitmap.loader.handler.DefaultDiskCacheExceptionHandler;

@DemoDescription(
        title = "AsyncImageList3",
        type = "Image",
        info = "an Async. Image ListView powered by SimpleBitmapLoader"
)

/**
 * 图片动态加载Demo3<br/>
 * 内存/磁盘双缓存<br/>
 * 采用SimpleBitmapLoader实现, 自带加载失败重载, 推荐此种方式
 *
 * Created by S.Violet on 2015/7/7.
 */
@ResourceId(R.layout.image_async)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class Async3ImageActivity extends TActivity {

    @ResourceId(R.id.image_async_listview)
    private ListView listView;

    private AsyncImageAdapter3 adapter;

    private SimpleBitmapLoader simpleBitmapLoader;//图片加载器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            /*
              1.由于该Demo图片密度大, 最多同时出现四个Item, 每个Item5张图, 因此设置等待队列容量为25,
            一般的情况下, 设置默认值10足够, 该值设置过大会导致快速滑动时, 下载更多的图, 增加流量消耗.
              2.loadingBitmap在SimpleBitmapLoader.destroy时会销毁, 因此直接用BitmapUtils解码.
            */

        //初始化图片加载器
        simpleBitmapLoader = new SimpleBitmapLoader(this, "AsyncImageActivity",
                BitmapUtils.decodeFromResource(getResources(), R.mipmap.async_image_null))
                .setNetLoadHandler(new MyNetLoadHandler(this))//自定义网络加载实现
                .setRamCache(0.15f, 0.15f)//缓存和回收站各占15%内存
//                    .setRamCache(0.004f, 0.004f)//测试:即使内存不足,显示的Bitmap被回收, 也不会抛异常
                .setDiskCache(50, 5, 25)//磁盘缓存50M, 5线程磁盘加载, 等待队列容量25
                .setNetLoad(3, 25)//3线程网络加载, 等待队列容量25
//                    .setDiskCacheInner()//强制使用内部储存
                .setImageQuality(Bitmap.CompressFormat.JPEG, 70)//设置保存格式和质量
//                    .setImageQuality(Bitmap.CompressFormat.PNG, 70)//设置保存格式和质量(透明图需要PNG)
//                    .setWipeOnNewVersion()//设置
                .setLogger(getLogger())//打印日志
                .setAnimationDuration(400)//设置图片淡入动画持续时间400ms
                .setReloadTimesMax(2)//设置图片加载失败重新加载次数限制
                .setDiskCacheExceptionHandler(new DefaultDiskCacheExceptionHandler(new Runnable() {
                    @Override
                    public void run() {
                        if (adapter != null)
                            adapter.notifyDataSetChanged();
                    }
                }));//TODO 注释
        simpleBitmapLoader.open();//启动(必须)

        //设置适配器, 传入图片加载器, 图片解码工具
        adapter = new AsyncImageAdapter3(this, makeItemList(), simpleBitmapLoader);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Activity不再显示时, 压缩图片缓存占用空间, 非必须, 在内存紧张场合适用
        simpleBitmapLoader.reduce();
        //Activity不再显示时, 取消所有加载任务
//        simpleBitmapLoader.cancelAllTasks();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁图片加载器(回收位图占用内存)
        //同时会销毁loadingBitmap
        simpleBitmapLoader.destroy();
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
            item.setUrl(i, "http://a/" + String.valueOf(id) + "-" + String.valueOf(i));
        }
        item.setTitle("Title of AsyncImageList2 " + String.valueOf(id));
        item.setContent("Content of asyncImagelist content of asyncimagelist content of asyncImagelist " + String.valueOf(id));
        return item;
    }
}
