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

package sviolet.demoa.image.utils;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sviolet.demoa.R;
import sviolet.turquoise.utils.bitmap.BitmapUtils;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoader;
import sviolet.turquoise.utils.bitmap.loader.entity.BitmapRequest;
import sviolet.turquoise.utils.bitmap.loader.handler.NetLoadHandler;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoaderMessenger;

/**
 * BitmapLoader网络加载处理器, 自定义实现
 * Created by S.Violet on 2015/7/7.
 */
public class MyNetLoadHandler implements NetLoadHandler {

    //用于随机生成图片, 供模拟网络加载
    private Random random = new Random(System.currentTimeMillis());
    private int index = 0;
    private int resourceIds[] = {R.mipmap.async_image_1, R.mipmap.async_image_2, R.mipmap.async_image_3, R.mipmap.async_image_4, R.mipmap.async_image_5};
    private ExecutorService pool = Executors.newCachedThreadPool();

    public MyNetLoadHandler(){

    }

    @Override
    public void loadFromNet(final Context context, BitmapLoader loader, final BitmapRequest request, final BitmapLoaderMessenger messenger) {

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
//                    Bitmap bitmap = BitmapUtils.decodeFromResource(context.getResources(), resourceIds[index], reqWidth, reqHeight);
                    Bitmap bitmap = BitmapUtils.drawTextOnResource(context.getResources(), resourceIds[index], request.getReqWidth(), request.getReqHeight(), request.getUrl(), 0, 50, 50f, 0xFF000000);

                    //转为byteArray
                    byte[] data = null;
                    try {
                        data = BitmapUtils.bitmapToByteArray(bitmap, Bitmap.CompressFormat.JPEG, 70, true);
                    } catch (IOException e) {
                        messenger.setResultFailed(e);
                        return;
                    }

                    messenger.setResultSucceed(data);
                }else{
                    //加载失败
                    messenger.setResultFailed(new RuntimeException("time out : " + request.getUrl()));//返回异常
                }
            }
        });

        index = (index + 1) % 5;
    }

    @Override
    public void onDestroy() {
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
