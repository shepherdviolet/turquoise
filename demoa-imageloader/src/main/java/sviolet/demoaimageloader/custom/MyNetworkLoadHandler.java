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

package sviolet.demoaimageloader.custom;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import sviolet.demoaimageloader.R;
import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;
import sviolet.turquoise.util.bitmap.BitmapUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.handler.common.AbstractNetworkLoadHandler;
import sviolet.turquoise.x.imageloader.handler.common.NetworkCallback;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * 自定义实现网络加载: 加载本地资源图片模拟网络加载
 *
 * @author S.Violet
 */
public class MyNetworkLoadHandler extends AbstractNetworkLoadHandler {

    private Random random = new Random(System.currentTimeMillis());
    private int index = 0;
    private int[] resourceIds = {R.mipmap.async_image_1, R.mipmap.async_image_2, R.mipmap.async_image_3, R.mipmap.async_image_4, R.mipmap.async_image_5};
    private ExecutorService pool = ThreadPoolExecutorUtils.createCached(0, Integer.MAX_VALUE, 60L, "TLoaderDemo-MyNetworkLoadHandler-%d");
    private float textSize = 100f;

    public MyNetworkLoadHandler(){

    }

    public MyNetworkLoadHandler(float textSize, int[] resourceIds){
        this.textSize = textSize;
        this.resourceIds = resourceIds;
    }

    /**
     * 特别注意:
     *
     * 任何处理结果, 都必须通过callback.setResultSucceed()/callback.setResultFailed()/callback.setResultCanceled()返回结果.
     * 为了支持第三方网络框架的异步加载, 加载线程会挂起, 等待callback返回数据, 当使用callback.setResult...方法返回结果时, 加载
     * 线程才会继续执行, 因此必须保证每种情况都使用callback返回结果, 包括异常.
     *
     * <pre><@code
     *      public void onHandle(Context applicationContext, Context context, Task.Info taskInfo, NetworkCallback<Result> callback, long connectTimeout, long readTimeout, long imageDataLengthLimit, TLogger logger) {
     *          try{
     *              //load by third party network utils
     *              //don't forget set timeout
     *              XXX.get(url, connectTimeout, readTimeout, new OnFinishListener(){
     *                  public void onSucceed(InputStream inputStream){
     *                      //return result
     *                      callback.setResultSucceed(new WriteResult(inputStream));
     *                  }
     *                  public void onFailed(Exception e){
     *                      //return result
     *                      callback.setResultFailed(e);
     *                  }
     *              });
     *          }catch(Exception e){
     *              //return result
     *              callback.setResultFailed(e);
     *          }
     *      }
     * </pre>
     *
     * 注意根据connectTimeout/readTimeout设置超时.
     */
    @Override
    public void onHandle(Context applicationContext, final Context context, final Task.Info taskInfo, final NetworkCallback<Result> callback, long connectTimeout, long readTimeout, long imageDataLengthLimit, TLogger logger) {

        //模拟异步执行
        pool.execute(new Runnable() {
            @Override
            public void run() {

                //模拟网络耗时////////////////////////////////////////////

                //随机延时
                long duration = getRandomInt(400) + 100;
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    //任何异常都需要捕获并返回处理结果,
                    callback.setResultCanceled();
                    return;
                }

                //模拟网络加载/////////////////////////////////////////////
                //模拟网络加载失败的情况
                if(getRandomInt(100) > 5) {
                    //加载成功
                    //模拟网络加载, 从资源中获取图片
//                    Bitmap bitmap = BitmapUtils.decodeFromResource(context.getResources(), resourceIds[index], 0, 0);
                    //模拟网络加载, 从资源获取图片, 并绘制url在图上
                    Bitmap bitmap = BitmapUtils.drawTextOnResource(context.getResources(), resourceIds[index], 0, 0, taskInfo.getUrl(), 0, textSize, textSize, 0xFF000000);

                    //转为byteArray
                    byte[] data = null;
                    try {
                        data = BitmapUtils.bitmapToByteArray(bitmap, Bitmap.CompressFormat.JPEG, 70, true);
                    } catch (IOException e) {
                        //任何异常都需要捕获并返回处理结果,
                        callback.setResultFailed(e);
                        return;
                    }

                    //bytes方式返回
//                    callback.setResultSucceed(new WriteResult(data));
                    //inputStream方式返回
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                    callback.setResultSucceed(new Result(inputStream));
                }else{
                    //加载失败
                    callback.setResultFailed(new RuntimeException("time out : " + taskInfo.getUrl()));//返回异常
                }
            }
        });

        index = (index + 1) % resourceIds.length;
    }

    private int getRandomInt(int max){
        synchronized (this){
            return random.nextInt(max);
        }
    }

}
