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

package sviolet.turquoise.modelx.bitmaploader.handler;

import android.content.Context;

import sviolet.turquoise.modelx.bitmaploader.BitmapLoader;
import sviolet.turquoise.modelx.bitmaploader.entity.BitmapRequest;

/**
 * 磁盘缓存异常处理器<p/>
 *
 * 实现磁盘缓存打开失败 和 磁盘缓存写入失败的异常处理, 其中"磁盘缓存打开失败"务必妥善处理.<br/>
 *
 * @see DefaultDiskCacheExceptionHandler
 * @deprecated if you app's api level above 11, use TILoader instead
 *
 * Created by S.Violet on 2015/11/4.
 */
@Deprecated
public interface DiskCacheExceptionHandler {

    /**
     * 实现磁盘缓存打开异常的处理<p/>
     *
     * 磁盘缓存打开失败, 将导致BitmapLoader无法使用, 必须妥善处理这种异常<p/>
     *
     * 实现建议:<Br/>
     * 方式1:提示客户"内存已满"或"重启手机", 且图片将无法显示.<br/>
     * 方式2:提示客户"内存已满"或"重启手机", 询问是否依然加载图片(无缓存模式), 当客户选择是, 将BitmapLoader
     *      设置为磁盘缓存禁用模式(Bitmap.setDiskCacheDisabled()), 并再次启用(Bitmap.open()).<Br/>
     * 方式3:静默处理, 直接启动磁盘缓存禁用模式.<br/>
     *
     * @param context 上下文(可能为空)
     * @param bitmapLoader 图片加载器
     * @param throwable 异常
     */
    void onCacheOpenException(Context context, BitmapLoader bitmapLoader, Throwable throwable);

    /**
     * 实现磁盘缓存写入异常的处理, 通常只需要打印日志或提醒即可
     *
     * @param context 上下文(可能为空)
     * @param bitmapLoader 图片加载器
     * @param throwable 异常
     */
    void onCacheWriteException(Context context, BitmapLoader bitmapLoader, BitmapRequest request, Throwable throwable);

    /**
     * 当BitmapLoader销毁时,会回调该方法,用于销毁处理器成员<br/>
     * 可不实现<Br/>
     */
    void onDestroy();

}
