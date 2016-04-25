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

package sviolet.turquoise.x.imageloader.drawable;

import android.graphics.drawable.ColorDrawable;

/**
 * Created by S.Violet on 2016/4/25.
 */
public class TIColorDrawable extends ColorDrawable {

    private int fixedWidth = Integer.MIN_VALUE;
    private int fixedHeight = Integer.MIN_VALUE;

    public TIColorDrawable() {
    }

    public TIColorDrawable(int color) {
        super(color);
    }

    @Override
    public int getIntrinsicWidth() {
        if (fixedWidth > Integer.MIN_VALUE)
            return fixedWidth;
        return super.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        if (fixedHeight > Integer.MIN_VALUE)
            return fixedHeight;
        return super.getIntrinsicHeight();
    }

    /**
     * set fixed size
     * @param width width
     * @param height height
     */
    public TIColorDrawable setFixedSize(int width, int height){
        this.fixedWidth = width;
        this.fixedHeight = height;
        return this;
    }

}
