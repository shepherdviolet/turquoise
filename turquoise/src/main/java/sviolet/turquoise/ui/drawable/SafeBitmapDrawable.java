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

package sviolet.turquoise.ui.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import java.io.InputStream;

/**
 * 安全的BitmapDrawable, 在Bitmap被回收时, 会显示默认颜色防止崩溃
 *
 * Created by S.Violet on 2016/11/24.
 */

public class SafeBitmapDrawable extends BitmapDrawable {

    private boolean crashed = false;
    private int crashColor = 0xFFD0D0D0;

    public SafeBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    public SafeBitmapDrawable(Resources res, String filepath) {
        super(res, filepath);
    }

    public SafeBitmapDrawable(Resources res, InputStream is) {
        super(res, is);
    }

    @Override
    public void draw(Canvas canvas) {
        if (!crashed) {
            try {
                super.draw(canvas);
            } catch (Exception ignore) {
                crashed = true;
            }
        }
        if (crashed){
            canvas.drawColor(crashColor);
        }
    }

    /**
     * @param crashColor 设置崩溃时的颜色
     */
    public SafeBitmapDrawable setCrashColor(int crashColor){
        this.crashColor = crashColor;
        return this;
    }

}
