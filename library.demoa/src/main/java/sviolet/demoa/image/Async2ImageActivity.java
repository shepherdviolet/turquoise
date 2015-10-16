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
import sviolet.demoa.image.utils.MyBitmapLoaderImplementor;
import sviolet.turquoise.enhanced.TActivity;
import sviolet.turquoise.enhanced.annotation.inject.ResourceId;
import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;
import sviolet.turquoise.utils.bitmap.BitmapUtils;
import sviolet.turquoise.utils.bitmap.loader.enhanced.AsyncBitmapDrawableLoader;

@DemoDescription(
        title = "AsyncImageList2",
        type = "Image",
        info = "an Async. Image ListView powered by AsyncBitmapDrawableLoader"
)

/**
 * 图片动态加载Demo2<br/>
 * 内存/磁盘双缓存<br/>
 * 采用AsyncBitmapDrawableLoader实现, AsyncBitmapDrawable自带加载失败重载
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

    private AsyncImageAdapter2 adapter;

    private AsyncBitmapDrawableLoader mAsyncBitmapDrawableLoader;//图片加载器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            /*
              1.由于该Demo图片密度大, 最多同时出现四个Item, 每个Item5张图, 因此设置等待队列容量为25,
            一般的情况下, 设置默认值10足够, 该值设置过大会导致快速滑动时, 下载更多的图, 增加流量消耗.
              2.loadingBitmap在AsyncBitmapDrawableLoader.destroy时会销毁, 因此直接用BitmapUtils解码.
            */
            //初始化图片加载器
            mAsyncBitmapDrawableLoader = new AsyncBitmapDrawableLoader(this, "AsyncImageActivity",
                    BitmapUtils.decodeFromResource(getResources(), R.mipmap.async_image_null), new MyBitmapLoaderImplementor(this))
                    .setRamCache(0.15f)//缓存占15%内存(与BitmapLoader不同之处)
//                    .setRamCache(0.004f)//测试:即使内存不足,显示的Bitmap被回收, 也不会抛异常
                    .setDiskCache(50, 5, 25)//磁盘缓存50M, 5线程磁盘加载, 等待队列容量25
                    .setNetLoad(3, 25)//3线程网络加载, 等待队列容量25
                    .setDiskCacheInner()//强制使用内部储存
                    .setImageQuality(Bitmap.CompressFormat.JPEG, 70)//设置保存格式和质量
//                    .setImageQuality(Bitmap.CompressFormat.PNG, 70)//设置保存格式和质量(透明图需要PNG)
//                    .setLogger(getLogger())//打印日志
                    .setAnimationDuration(400)//设置图片淡入动画持续时间400ms
                    .setReloadTimesMax(2)//设置图片加载失败重新加载次数限制
                    .open();//启动(必须)
            //设置适配器, 传入图片加载器, 图片解码工具
            adapter = new AsyncImageAdapter2(this, makeItemList(), mAsyncBitmapDrawableLoader);
            listView.setAdapter(adapter);
        } catch (IOException e) {
            //磁盘缓存打开失败的情况, 可提示客户磁盘已满等
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁图片加载器(回收位图占用内存)
        //同时会销毁loadingBitmap
        mAsyncBitmapDrawableLoader.destroy();
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
