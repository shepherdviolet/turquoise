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
import android.graphics.Bitmap;

import sviolet.turquoise.modelx.bitmaploader.BitmapLoader;
import sviolet.turquoise.modelx.bitmaploader.entity.BitmapRequest;

/**
 * 图片(Bitmap)解码处理器<p/>
 *
 * 实现将数据解码为图片(Bitmap)的过程. 网络加载和磁盘缓存读取的数据, 均调用该处理器解码图片.
 * 自定义该处理器通常用于改变解码方式, 或对解码后的图片进行特殊处理, 例如:缩放/圆角处理等.<p/>
 *
 * 注意: 在该"图片解码处理器"中进行特殊处理, 仅影响图片显示, 不会改变磁盘缓存中的原始数据,
 * 且图片每次加载均要经过处理, 可能会降低BitmapLoader的性能, 不建议在此处进行复杂的图片处理.
 * 这点与{@link NetLoadHandler}中进行图片特殊处理不同, BitmapDecodeHandler适合保留原始数据,
 * 进行轻量级处理.<p/>
 *
 * @see DefaultBitmapDecodeHandler
 * @deprecated if you app's api level above 11, use TILoader instead
 *
 * Created by S.Violet on 2015/11/5.
 */
@Deprecated
public interface BitmapDecodeHandler {

    /**
     * [网络加载时调用]<Br/>
     * byteArray解码Bitmap
     *
     * @param data byteArray数据(不为空)
     */
    Bitmap onDecode(Context context, BitmapLoader loader, BitmapRequest request, byte[] data) throws Exception;

    /**
     * [磁盘缓存加载时调用]<Br/>
     * 根据文件路径解码Bitmap
     *
     * @param filePath 文件路径
     */
    Bitmap onDecode(Context context, BitmapLoader loader, BitmapRequest request, String filePath) throws Exception;

    /**
     * 当BitmapLoader销毁时,会回调该方法,用于销毁处理器成员<br/>
     * 可不实现<Br/>
     */
    void onDestroy();

}
