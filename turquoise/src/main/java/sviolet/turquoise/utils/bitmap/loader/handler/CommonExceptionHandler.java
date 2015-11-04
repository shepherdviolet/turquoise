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

package sviolet.turquoise.utils.bitmap.loader.handler;

import android.content.Context;

import sviolet.turquoise.utils.bitmap.loader.BitmapLoader;

/**
 * 普通异常处理器<Br/>
 *
 * 通常用于实现错误日志的输出过程.<br/>
 *
 * @see DefaultCommonExceptionHandler
 *
 * Created by S.Violet on 2015/11/4.
 */
public interface CommonExceptionHandler {

    /**
     * 普通异常, 不包括磁盘缓存打开异常和磁盘缓存写入异常, 通常只需要打印日志或提醒即可
     *
     * @param context 上下文(可能为空)
     * @param bitmapLoader 图片加载器
     * @param throwable 异常
     */
    public void onCommonException(Context context, BitmapLoader bitmapLoader, Throwable throwable);

}