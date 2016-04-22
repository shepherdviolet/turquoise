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

import sviolet.turquoise.util.common.BitmapUtils;
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
 * @see BitmapDecodeHandler
 * @deprecated if you app's api level above 15, use TILoader instead
 *
 * Created by S.Violet on 2015/11/5.
 */
@Deprecated
public class DefaultBitmapDecodeHandler implements BitmapDecodeHandler{

    @Override
    public Bitmap onDecode(Context context, BitmapLoader loader, BitmapRequest request, byte[] data) {
        //判断需求尺寸是否生效
        if (request.hasReqDimension())
            return BitmapUtils.decodeFromByteArray(data, request.getReqWidth(), request.getReqHeight(), request.getBitmapConfig());
        else
            return BitmapUtils.decodeFromByteArray(data, 0, 0, request.getBitmapConfig());
    }

    @Override
    public Bitmap onDecode(Context context, BitmapLoader loader, BitmapRequest request, String filePath) {
        //判断需求尺寸是否生效
        if (request.hasReqDimension())
            return BitmapUtils.decodeFromFile(filePath, request.getReqWidth(), request.getReqHeight(), request.getBitmapConfig());
        else
            return BitmapUtils.decodeFromFile(filePath, 0, 0, request.getBitmapConfig());
    }

    @Override
    public void onDestroy() {

    }
}
