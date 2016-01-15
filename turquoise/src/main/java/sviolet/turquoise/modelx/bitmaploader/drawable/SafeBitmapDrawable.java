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

package sviolet.turquoise.modelx.bitmaploader.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import java.io.InputStream;

import sviolet.turquoise.util.log.TLogger;

/**
 * 安全的BitmapDrawable<br/>
 * <br/>
 * 防回收崩溃, 当Bitmap被回收时, 绘制空白, 不会崩溃<Br/>
 * 可复写onDrawError()方法实现重新加载.<br/>
 *
 * Created by S.Violet on 2015/10/21.
 */
public class SafeBitmapDrawable extends BitmapDrawable {

    private TLogger logger = TLogger.get(this);//日志打印器
    private boolean drawEnable = true;//允许绘制
    private boolean matchParent = false;//尺寸填充父控件(match_parent)

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
                /*
                    解决小米等手机, 绘制recycled的Bitmap时不会抛出异常的问题
                */
                if (getBitmap() != null && getBitmap().isRecycled()){
                    throw new Exception("[SafeBitmapDrawable]draw: bitmap is recycled");
                }
            } catch (Exception e) {
                //禁止绘制
                drawEnable = false;
                logger.d("draw: error, catch exception: " + e.getMessage());
                onDrawError(canvas, e);
            }
        }else{
            logger.d("draw: skip, because of exception");
        }
    }

    /**
     * 绘制错误时调用该方法, 可复写该方法实现重新加载
     */
    protected void onDrawError(Canvas canvas, Exception e){

    }

    @Override
    public int getIntrinsicWidth() {
        if (matchParent)
            return -1;
        return super.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        if (matchParent)
            return -1;
        return super.getIntrinsicHeight();
    }

    /**
     * 设置尺寸是否填充父控件(match_parent), 默认false
     */
    public SafeBitmapDrawable setMatchParent(boolean matchParent){
        this.matchParent = matchParent;
        return this;
    }

}
