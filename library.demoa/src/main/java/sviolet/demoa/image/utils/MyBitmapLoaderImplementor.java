package sviolet.demoa.image.utils;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sviolet.demoa.R;
import sviolet.turquoise.utils.bitmap.BitmapUtils;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoaderImplementor;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoaderResultHolder;
import sviolet.turquoise.utils.conversion.ByteUtils;
import sviolet.turquoise.utils.crypt.DigestCipher;

/**
 * BitmapLoader实现器
 * Created by S.Violet on 2015/7/7.
 */
public class MyBitmapLoaderImplementor implements BitmapLoaderImplementor {

    private Context context;

    //用于随机生成图片, 供模拟网络加载
    private Random random = new Random(System.currentTimeMillis());
    private int index = 0;
    private int resourceIds[] = {R.mipmap.async_image_1, R.mipmap.async_image_2, R.mipmap.async_image_3, R.mipmap.async_image_4, R.mipmap.async_image_5};
    private ExecutorService pool = Executors.newCachedThreadPool();

    public MyBitmapLoaderImplementor(Context context){
        this.context = context;
    }

    /**
     * [实现提示]:<br/>
     * 通常将url进行摘要计算, 得到摘要值作为cacheKey, 根据实际情况实现.  <Br/>
     * AsyncBitmapLoader中每个位图资源都由url唯一标识, url在AsyncBitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     *
     * @return 实现根据URL计算并返回缓存Key
     */
    @Override
    public String getCacheKey(String url) {
        //用url做摘要
        return ByteUtils.byteToHex(DigestCipher.digest(url, DigestCipher.TYPE_MD5));
    }

    /**
     * 实现根据url参数从网络下载图片数据, 依照需求尺寸reqWidth和reqHeight解析为合适大小的Bitmap,
     * 并调用结果容器resultHolder.set(Bitmap)方法将Bitmap返回, 若加载失败则set(null)<br/>
     * <br/>
     * 注意:<br/>
     * 1.网络请求注意做超时处理,否则任务可能会一直等待<br/>
     * 2.数据解析为Bitmap时,请根据需求尺寸reqWidth和reqHeight解析, 以节省内存<br/>
     * <br/>
     * 线程会阻塞等待,直到resultHolder.set(Bitmap)方法执行.若任务被cancel,阻塞也会被中断,且即使后续
     * 网络请求返回了Bitmap,也会被Bitmap.recycle().<br/>
     * <br/>
     * 无论同步还是异步的情况,均使用resultHolder.set(Bitmap)返回结果<br/>
     * 同步网络请求:<br/>
     */
    @Override
    public void loadFromNet(final String url, final int reqWidth, final int reqHeight, final BitmapLoaderResultHolder resultHolder) {

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
                    Thread.sleep(random.nextInt(400) + 100);
                } catch (InterruptedException e) {
                }
                //模拟网络加载失败的情况
                if(random.nextInt(10) > 2) {
                    //模拟网络加载, 从资源中获取图片, 注意要根据需求尺寸解析合适大小的Bitmap,以节省内存
                    Bitmap bitmap = BitmapUtils.decodeFromResource(context.getResources(), resourceIds[index], reqWidth, reqHeight);
//                    Bitmap bitmap = BitmapUtils.drawTextOnResource(context.getResources(), resourceIds[index], reqWidth, reqHeight, url, 0, 50, 50f, 0xFF000000);
                    resultHolder.set(bitmap);
                }else{
                    //失败
                    resultHolder.set(null);
                }
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
