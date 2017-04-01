/*
 * Copyright (C) 2015-2017 S.Violet
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

package sviolet.turquoise.util.bitmap.discolor;

import sviolet.turquoise.util.bitmap.BitmapUtils;

/**
 * [变色过滤器]将不透明部分的颜色都替换成指定颜色, 透明部分仍然保持透明
 *
 * Created by S.Violet on 2017/4/1.
 */
public class SketchDiscolorFilter implements BitmapUtils.DiscolorFilter {

    private int color;
    private boolean transparent;

    /**
     * @param color 替换的颜色(transparent=true时, 该颜色的alpha值无效, 被原色覆盖)
     * @param transparent true:替换后的颜色继承原色的alpha, false:完全替换为指定颜色
     */
    public SketchDiscolorFilter(int color, boolean transparent) {
        this.color = color;
        this.transparent = transparent;
    }

    @Override
    public int filter(int color) {
        int alpha = color & 0xff000000;
        if (alpha == 0){
            return color;
        }
        if (transparent){
            return (this.color & 0x00ffffff) | alpha;
        } else {
            return this.color;
        }
    }

}
