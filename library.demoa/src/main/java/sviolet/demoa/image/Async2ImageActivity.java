package sviolet.demoa.image;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.image.utils.AsyncImageAdapter2;
import sviolet.demoa.image.utils.AsyncImageItem;
import sviolet.demoa.image.utils.BitmapLoaderImplementor;
import sviolet.turquoise.enhance.TActivity;
import sviolet.turquoise.enhance.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.annotation.setting.ActivitySettings;
import sviolet.turquoise.utils.bitmap.BitmapLoader;

@DemoDescription(
        title = "AsyncImageList2",
        type = "Image",
        info = "an Async. Image ListView powered by BitmapLoader and SafeBitmapDrawable"
)

/**
 * 图片动态加载Demo<br/>
 * 内存/磁盘双缓存<br/>
 * BitmapLoader禁用缓存回收站, 配合SafeBitmapDrawable防止Bitmap被回收绘制异常
 *
 * Created by S.Violet on 2015/7/7.
 */
@ResourceId(R.layout.image_async)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class Async2ImageActivity extends TActivity {

    @ResourceId(R.id.image_async_listview)
    private ListView listView;

    private BitmapLoader mBitmapLoader;//图片加载器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            /*
            由于该Demo图片密度大, 最多同时出现四个Item, 每个Item5张图, 因此
            设置等待队列容量为25, 一般的情况下, 设置默认值10足够, 该值设置过大
            会导致快速滑动时, 下载更多的图, 增加流量消耗.
            */
            //初始化图片加载器
            mBitmapLoader = new BitmapLoader(this, "AsyncImageActivity", new BitmapLoaderImplementor(this))
                    /**
                     * 采用SafeBitmapDrawable方式无需回收站
                     */
                    .setRamCache(0.15f, 0)//缓存占15%内存, 禁用回收站
//                    .setRamCache(0.01f, 0)//测试:即使内存不足,显示的Bitmap被回收, 也不会抛异常
                    .setDiskCache(50, 5, 25)//磁盘缓存50M, 5线程磁盘加载, 等待队列容量25
                    .setNetLoad(3, 25)//3线程网络加载, 等待队列容量25
                    .setDiskCacheInner()//强制使用内部储存
                    .setImageQuality(Bitmap.CompressFormat.JPEG, 70)//设置保存格式和质量
//                    .setLogger(getLogger())//打印日志
                    .open();//启动(必须)
            //设置适配器, 传入图片加载器, 图片解码工具
            /**
             * 用AsyncImageAdapter2
             */
            listView.setAdapter(new AsyncImageAdapter2(this, makeItemList(), mBitmapLoader, getCachedBitmapUtils()));
        } catch (IOException e) {
            //磁盘缓存打开失败的情况, 可提示客户磁盘已满等
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁图片加载器(回收位图占用内存)
        mBitmapLoader.destroy();
    }

    /**
     * 模拟数据生成
     * @return
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
            item.setUrl(i, "http://a.b.c/" + String.valueOf(id) + "-" + String.valueOf(i));
            item.setKey(i, null);
        }
        item.setTitle("Title of AsyncImageList " + String.valueOf(id));
        item.setContent("Content of asyncImagelist content of asyncimagelist content of asyncImagelist " + String.valueOf(id));
        return item;
    }
}
