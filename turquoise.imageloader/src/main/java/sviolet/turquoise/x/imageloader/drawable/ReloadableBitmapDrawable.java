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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import sviolet.turquoise.modelx.bitmaploader.drawable.SafeBitmapDrawable;
import sviolet.turquoise.x.imageloader.stub.Stub;

/**
 *
 * Created by S.Violet on 2016/3/16.
 */
public class ReloadableBitmapDrawable extends SafeBitmapDrawable {

    private WeakReference<Stub> stub;

    public ReloadableBitmapDrawable(Resources res) {
        super(res);
    }

    public ReloadableBitmapDrawable(Bitmap bitmap) {
        super(bitmap);
    }

    public ReloadableBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    public ReloadableBitmapDrawable(String filepath) {
        super(filepath);
    }

    public ReloadableBitmapDrawable(Resources res, String filepath) {
        super(res, filepath);
    }

    public ReloadableBitmapDrawable(InputStream is) {
        super(is);
    }

    public ReloadableBitmapDrawable(Resources res, InputStream is) {
        super(res, is);
    }

    @Override
    protected void onDrawError(Canvas canvas, Exception e) {
        final Stub stub = getStub();
        if (stub != null){
            stub.reload();
        }
    }

    private Stub getStub(){
        if (stub != null){
            return stub.get();
        }
        return null;
    }

    public ReloadableBitmapDrawable bindStub(Stub stub){
        this.stub = new WeakReference<>(stub);
        return this;
    }

}
