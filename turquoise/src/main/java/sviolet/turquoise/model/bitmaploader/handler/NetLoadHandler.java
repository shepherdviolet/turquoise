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

package sviolet.turquoise.model.bitmaploader.handler;

import android.content.Context;

import sviolet.turquoise.model.bitmaploader.BitmapLoader;
import sviolet.turquoise.model.bitmaploader.BitmapLoaderMessenger;
import sviolet.turquoise.model.bitmaploader.entity.BitmapRequest;

/**
 * 网络加载处理器<p/>
 *
 * 实现BitmapLoader从网络加载图片数据的过程. 自定义该处理器, 通常用于改变网络加载方式(第三方网络框架),
 * 或对下载的图片数据进行特殊处理, 例如:缩放/圆角处理等.<p/>
 *
 * 注意: 在该"网络加载处理器"中特殊处理图片数据, 磁盘缓存将保存改变后的数据, 而非原始数据. 这点与在
 * {@link BitmapDecodeHandler}中进行图片特殊处理不同, NetLoadHandler适合进行较为复杂的图片处理,
 * 因为仅影响网络加载时的效率, 磁盘缓存加载时直接加载处理后的数据, 效率较高<p/>
 *
 * @see DefaultNetLoadHandler
 *
 * @author S.Violet
 */
public interface NetLoadHandler {

    /**
     * 实现根据url参数从网络下载图片数据(byte[])并调用通知器
     * BitmapLoaderMessenger.setResultSucceed/setResultFailed/setResultCanceled方法返回结果<br/>
     * <br/>
     * *********************************************************************************<br/>
     * * * 注意::<br/>
     * *********************************************************************************<br/>
     * <br/>
     * 1.网络请求必须做超时处理,否则任务会一直阻塞等待.<br/>
     * <br/>
     * 2.必须使用BitmapLoaderMessenger.setResultSucceed/setResultFailed/setResultCanceled方法返回结果,
     *      若不调用,图片加载任务中的BitmapLoaderMessenger.getResult()会一直阻塞等待结果.<br/>
     *      1)setResultSucceed(byte[]),加载成功,返回图片数据<br/>
     *      2)setResultFailed(Exception),加载失败,返回异常<br/>
     *      3)setResultCanceled(),加载取消<br/>
     *      若加载任务已被取消(isCancelling() = true),但仍使用setResultSucceed返回结果,则图片数据会被存入
     *      磁盘缓存,但BitmapLoader返回任务取消.<br/>
     * <br/>
     * 3.合理地处理加载任务取消的情况.<br/>
     *      网络加载同步方式实现时,设置合理的连接超时,在循环读取数据时,判断messenger.isCancelling(),
     *      当取消时终止读取,并setResultCanceled()返回.异步网络框架实现时,设置取消监听器
     *      messenger.setOnCancelListener(),在回调方法中取消网络加载.<br/>
     *      1)当加载任务取消,终止网络加载,并用BitmapLoaderMessenger.setResultCanceled()返回结果<br/>
     *          采用此种方式,已加载的数据废弃,BitmapLoader作为任务取消处理,不会返回Bitmap.<br/>
     *      2)当加载任务取消,继续完成网络加载,并用BitmapLoaderMessenger.setResultSucceed(Bitmap)返回结果<br/>
     *          采用此种方式,Bitmap会被存入磁盘缓存,但BitmapLoader仍作为任务取消处理,不会返回Bitmap.<br/>
     * <br/>
     * *********************************************************************************<br/>
     * * * 代码示例::<br/>
     * *********************************************************************************<br/>
     * <br/>
     * 1.同步加载方式<br/>
     * <br/>
     *      {@link DefaultNetLoadHandler}<br/>
     * <br/>
     * 2.异步加载方式<br/>
     * <br/>
     * <pre>{@code
     *      public void loadFromNet(final Context context, final BitmapLoader loader, final BitmapRequest request, final BitmapLoaderMessenger messenger) {
     *          //第三方网络工具
     *          final HttpUtils httpUtils = new HttpUtils();
     *          //相关设置
     *          ......
     *          //设置监听器
     *          messenger.setOnCancelListener(new Runnable(){
     *              public void run(){
     *                  //取消加载
     *                  httpUtils.cancel();
     *                  //返回取消结果[必须]
     *                  messenger.setResultCanceled();
     *              }
     *          });
     *          //调用工具异步发送请求
     *          try{
     *              httpUtils.send( ... , new Callback(){
     *                  ... onSucceed(byte[] result){
     *                      //返回成功结果[必须]
     *                      messenger.setResultSucceed(result);
     *                  }
     *                  ... onFailed(...){
     *                      //返回失败结果[必须]
     *                      messenger.setResultFailed(...);
     *                  }
     *              });
     *          }catch(Exception e){
     *              //返回失败结果[必须]
     *              messenger.setResultFailed(e);
     *          }
     *      }
     * }</pre>
     * <Br/>
     *
     * @param loader 图片加载器
     * @param request 图片加载请求参数
     * @param messenger 通知器
     */
    void loadFromNet(Context context, BitmapLoader loader, BitmapRequest request, BitmapLoaderMessenger messenger);

    /**
     * 当BitmapLoader销毁时,会回调该方法,用于销毁处理器成员<br/>
     * 可不实现<Br/>
     */
    void onDestroy();

}
