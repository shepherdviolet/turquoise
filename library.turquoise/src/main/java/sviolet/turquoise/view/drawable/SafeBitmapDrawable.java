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

package sviolet.turquoise.view.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import java.io.InputStream;

import sviolet.turquoise.utils.Logger;

/**
 * 安全的BitmapDrawable<br/>
 * <br/>
 * 简易地防回收崩溃, 当Bitmap被回收时, 绘制空白, 不会崩溃, 但并不会重新加载图片<Br/>
 *
 * Created by S.Violet on 2015/10/21.
 */
public class SafeBitmapDrawable extends BitmapDrawable {

    private Logger logger;//日志打印器
    private boolean drawEnable = true;//允许绘制

    public SafeBitmapDrawable(Resources res) {
        super(res);
    }

    public SafeBitmapDrawable(Bitmap bitmap) {
        super(bitmap);
    }

    public SafeBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    public SafeBitmapDrawable(String filepath) {
        super(filepath);
    }

    public SafeBitmapDrawable(Resources res, String filepath) {
        super(res, filepath);
    }

    public SafeBitmapDrawable(InputStream is) {
        super(is);
    }

    public SafeBitmapDrawable(Resources res, InputStream is) {
        super(res, is);
    }

    @Override
    public void draw(Canvas canvas) {
        if (drawEnable) {
            try {
                super.draw(canvas);
            } catch (Exception e) {
                drawEnable = false;//禁止绘制
                if (getLogger() != null) {
                    getLogger().e("[SafeBitmapDrawable]draw error, catch exception", e);
                }
            }
        }else{
            if (getLogger() != null) {
                getLogger().d("[SafeBitmapDrawable]draw skip, because of exception");
            }
        }
    }

    public SafeBitmapDrawable setLogger(Logger logger){
        this.logger = logger;
        return this;
    }

    private Logger getLogger(){
        return logger;
    }
}
