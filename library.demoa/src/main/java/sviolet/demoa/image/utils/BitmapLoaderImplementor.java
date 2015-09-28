package sviolet.demoa.image.utils;

import android.content.Context;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sviolet.demoa.R;
import sviolet.turquoise.utils.bitmap.BitmapLoader;
import sviolet.turquoise.utils.bitmap.BitmapUtils;
import sviolet.turquoise.utils.conversion.ByteUtils;
import sviolet.turquoise.utils.crypt.DigestCipher;

/**
 * BitmapLoader实现器
 * Created by S.Violet on 2015/7/7.
 */
public class BitmapLoaderImplementor implements BitmapLoader.Implementor {

    private Context context;

    //用于随机生成图片, 供模拟网络加载
    private Random random = new Random(System.currentTimeMillis());
    private int index = 0;
    private int resourceIds[] = {R.mipmap.async_image_1, R.mipmap.async_image_2, R.mipmap.async_image_3, R.mipmap.async_image_4, R.mipmap.async_image_5};
    private ExecutorService pool = Executors.newCachedThreadPool();

    public BitmapLoaderImplementor(Context context){
        this.context = context;
    }

    /**
     * BitmapLoader根据url和key共同确定一个资源, 通常url用于网路加载连接,
     * key为辅助参数, 根据实际情况使用, BitmapLoader内部使用cacheKey来
     * 确定一个资源, 包括队列中的任务名, 缓存中的key值, 磁盘缓存中的文件名等,
     * 因此, 需要实现这个方法, 根据url和key生成一个cacheKey返回<br/>
     */
    @Override
    public String getCacheKey(String url, String key) {
        //用url做摘要
        return ByteUtils.byteToHex(DigestCipher.digest(url, DigestCipher.TYPE_MD5));
        //直接用自定义key
//        return key;
        //也可以url+key组合做摘要
    }

    /**
     * 根据url和key从网络加载图片, 并写入cacheOutputStream<br/>
     * task可以用来判断任务是否被取消, 建议在网络加载中, 判断task.isCancel()
     * 来终止网络加载.<br/>
     * [此处模拟网络加载]<br/>
     */
    @Override
    public void loadFromNet(String url, String key, final int reqWidth, final int reqHeight, final BitmapLoader.ResultHolder resultHolder) {

        ///////////////////////////////////////////////////
        //同步方式

//        //模拟网络耗时
//        try {
//            Thread.sleep(random.nextInt(500));
//        } catch (InterruptedException e) {
//        }
//        //模拟网络加载, 从资源中获取图片, 注意要根据需求尺寸解析合适大小的Bitmap,以节省内存
//        resultHolder.set(BitmapUtils.decodeFromResource(context.getResources(), resourceIds[index], reqWidth, reqHeight));

        ///////////////////////////////////////////////////
        //异步方式

        pool.execute(new Runnable() {
            @Override
            public void run() {
                //模拟网络耗时
                try {
                    Thread.sleep(random.nextInt(1000));
                } catch (InterruptedException e) {
                }
                //模拟网络加载, 从资源中获取图片, 注意要根据需求尺寸解析合适大小的Bitmap,以节省内存
                resultHolder.set(BitmapUtils.decodeFromResource(context.getResources(), resourceIds[index], reqWidth, reqHeight));
            }
        });


        index = (index + 1) % 5;
    }

    /**
     * 异常处理
     */
    @Override
    public void onException(Throwable throwable) {
        throwable.printStackTrace();
    }

    /**
     * 写入到文件缓存失败
     */
    @Override
    public void onCacheWriteException(Throwable throwable) {
        throwable.printStackTrace();
    }
}
