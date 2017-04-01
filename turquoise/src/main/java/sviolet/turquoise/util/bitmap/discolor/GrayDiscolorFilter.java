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
 * [变色过滤器]将颜色变为黑白(保持灰度)
 *
 * Created by S.Violet on 2017/4/1.
 */
public class GrayDiscolorFilter implements BitmapUtils.DiscolorFilter {

    @Override
    public int filter(int color) {
        int alpha = color & 0xff000000;
        int red = color & 0x00ff0000 >>> 16;
        int green = color & 0x0000ff00 >>> 8;
        int blue = color & 0x000000ff;
        int gray = (red + green + blue) / 3;
        return alpha | gray << 16 | gray << 8 | gray;
    }

}
