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
 */

package sviolet.demoa.image.utils;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sviolet.demoa.R;
import sviolet.turquoise.utils.bitmap.BitmapUtils;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoaderImplementor;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoaderMessenger;
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
     * 通常将url进行摘要计算, 得到摘要值作为cacheKey, 根据实际情况实现.<Br/>
     * BitmapLoader中每个位图资源都由url唯一标识, url在BitmapLoader内部
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
     * 并调用通知器BitmapLoaderMessenger.setResultSucceed/setResultFailed/setResultCanceled方法返回结果<br/>
     * <br/>
     * *********************************************************************************<br/>
     * * * 注意::<br/>
     * *********************************************************************************<br/>
     * <br/>
     * 1.网络请求必须做超时处理,否则任务会一直阻塞等待.<br/>
     * <br/>
     * 2.数据解析为Bitmap时,请根据需求尺寸reqWidth和reqHeight解析,以节省内存.<br/>
     * {@link BitmapUtils}<br/>
     *      需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).<br/>
     *      图片解码时根据需求尺寸适当缩,且保持原图长宽比例,解码后Bitmap实际尺寸不等于需求尺寸.设置为0不缩小图片.<Br/>
     * <br/>
     * 3.必须使用BitmapLoaderMessenger.setResultSucceed/setResultFailed/setResultCanceled方法返回结果,
     *      若不调用,图片加载任务中的BitmapLoaderMessenger.getResult()会一直阻塞等待结果.<br/>
     *      1)setResultSucceed(Bitmap),加载成功,返回Bitmap<br/>
     *      2)setResultFailed(Throwable),加载失败,返回异常<br/>
     *      3)setResultCanceled(),加载取消<br/>
     *      若加载任务已被取消(isCancelling() = true),但仍使用setResultSucceed返回结果,则Bitmap会被存入
     *      磁盘缓存,但BitmapLoader返回任务取消.<br/>
     * <br/>
     * 4.合理地处理加载任务取消的情况<br/>
     *      1)当加载任务取消,终止网络加载,并用BitmapLoaderMessenger.setResultCanceled()返回结果<br/>
     *          采用此种方式,已加载的数据废弃,BitmapLoader作为任务取消处理,不会返回Bitmap.<br/>
     *      2)当加载任务取消,继续完成网络加载,并用BitmapLoaderMessenger.setResultSucceed(Bitmap)返回结果<br/>
     *          采用此种方式,Bitmap会被存入磁盘缓存,但BitmapLoader仍作为任务取消处理,不会返回Bitmap.<br/>
     * <br/>
     * @param url url
     * @param reqWidth 请求宽度
     * @param reqHeight 请求高度
     * @param messenger 通知器
     */
    @Override
    public void loadFromNet(final String url, final int reqWidth, final int reqHeight, final BitmapLoaderMessenger messenger) {

        ///////////////////////////////////////////////////
        //同步方式

//        //模拟网络耗时
//        try {
//            Thread.sleep(getRandomInt(400) + 100);
//        } catch (InterruptedException e) {
//        }
//        //模拟网络加载, 从资源中获取图片, 注意要根据需求尺寸解析合适大小的Bitmap,以节省内存
//        messenger.setResult(BitmapUtils.decodeFromResource(context.getResources(), resourceIds[index], reqWidth, reqHeight));

        ///////////////////////////////////////////////////
        //异步方式

        pool.execute(new Runnable() {
            @Override
            public void run() {

                //模拟网络耗时////////////////////////////////////////////

                long duration = getRandomInt(400) + 100;

                //messenger.canceled()判断方式取消任务

//                for (int i = 0 ; i < duration ; i += 10) {
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                    }
//                    //任务取消处理
//                    if (messenger.isCancelling()){
//                        //返回异常
//                        messenger.setResultCanceled();
//                        //结束加载
//                        return;
//                    }
//                }

                //messenger.setOnCancelListenner 设置监听器方式取消任务

                final Thread thread = Thread.currentThread();
                messenger.setOnCancelListener(new Runnable() {
                    @Override
                    public void run() {
                        thread.interrupt();
                    }
                });
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    //返回异常
                    messenger.setResultCanceled();
                    //结束加载
                    return;
                }

                //模拟网络加载/////////////////////////////////////////////
                //模拟网络加载失败的情况
                if(getRandomInt(100) > 5) {
                    //加载成功
                    //模拟网络加载, 从资源中获取图片, 注意要根据需求尺寸解析合适大小的Bitmap,以节省内存
                    Bitmap bitmap = BitmapUtils.decodeFromResource(context.getResources(), resourceIds[index], reqWidth, reqHeight);
//                    Bitmap bitmap = BitmapUtils.drawTextOnResource(context.getResources(), resourceIds[index], reqWidth, reqHeight, url, 0, 50, 50f, 0xFF000000);
                    messenger.setResultSucceed(bitmap);
                }else{
                    //加载失败
                    messenger.setResultFailed(new RuntimeException("time out : " + url));//返回异常
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

    @Override
    public void onDestroy() {
        context = null;
        random = null;
        if (pool != null)
            pool.shutdown();
        pool = null;
    }

    private int getRandomInt(int max){
        synchronized (this){
            return random.nextInt(max);
        }
    }
}
