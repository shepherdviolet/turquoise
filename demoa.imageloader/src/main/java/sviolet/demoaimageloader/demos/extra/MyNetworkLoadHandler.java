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

package sviolet.demoaimageloader.demos.extra;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sviolet.demoaimageloader.R;
import sviolet.turquoise.util.common.BitmapUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.EngineCallback;

/**
 * Created by S.Violet on 2016/4/20.
 */
public class MyNetworkLoadHandler implements NetworkLoadHandler {

    private Random random = new Random(System.currentTimeMillis());
    private int index = 0;
    private int resourceIds[] = {R.mipmap.async_image_1, R.mipmap.async_image_2, R.mipmap.async_image_3, R.mipmap.async_image_4, R.mipmap.async_image_5};
    private ExecutorService pool = Executors.newCachedThreadPool();

    @Override
    public void onHandle(Context applicationContext, final Context context, final Task.Info taskInfo, final EngineCallback<Result> callback, TLogger logger) {

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
                callback.setOnCancelListener(new Runnable() {
                    @Override
                    public void run() {
                        thread.interrupt();
                    }
                });
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    //返回异常
                    callback.setResultCanceled();
                    //结束加载
                    return;
                }

                //模拟网络加载/////////////////////////////////////////////
                //模拟网络加载失败的情况
                if(getRandomInt(100) > 5) {
                    //加载成功
                    //模拟网络加载, 从资源中获取图片, 注意要根据需求尺寸解析合适大小的Bitmap,以节省内存
//                    Bitmap bitmap = BitmapUtils.decodeFromResource(context.getResources(), resourceIds[index], reqWidth, reqHeight);
                    Bitmap bitmap = BitmapUtils.drawTextOnResource(context.getResources(), resourceIds[index], taskInfo.getParams().getReqWidth(), taskInfo.getParams().getReqHeight(), taskInfo.getUrl(), 0, 50, 50f, 0xFF000000);

                    //转为byteArray
                    byte[] data = null;
                    try {
                        data = BitmapUtils.bitmapToByteArray(bitmap, Bitmap.CompressFormat.JPEG, 70, true);
                    } catch (IOException e) {
                        callback.setResultFailed(e);
                        return;
                    }

                    //bytes
//                    callback.setResultSucceed(new Result(data));
                    //inputStream
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                    callback.setResultSucceed(new Result(inputStream));
                }else{
                    //加载失败
                    callback.setResultFailed(new RuntimeException("time out : " + taskInfo.getUrl()));//返回异常
                }
            }
        });

        index = (index + 1) % 5;
    }

    private int getRandomInt(int max){
        synchronized (this){
            return random.nextInt(max);
        }
    }

}
