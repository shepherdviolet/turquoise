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

package sviolet.turquoise.utils.bitmap.loader;

import android.graphics.Bitmap;

/**
 * 图片加载结束监听
 */
public interface OnBitmapLoadedListener {
    /**
     * 加载成功
     *
     * @param params 由load传入的参数,并非Bitmap,通常为ImageView,便于设置图片
     * @param bitmap 加载成功的位图, 可能为null
     */
    void onLoadSucceed(String url, int reqWidth, int reqHeight, Object params, Bitmap bitmap);

    /**
     * 加载失败
     *
     * @param params 由load传入的参数, 并非Bitmap
     */
    void onLoadFailed(String url, int reqWidth, int reqHeight, Object params);

    /**
     * 加载取消
     *
     * @param params 由load传入的参数, 并非Bitmap
     */
    void onLoadCanceled(String url, int reqWidth, int reqHeight, Object params);
}
