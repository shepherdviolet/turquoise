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

package sviolet.demoaimageloader.custom;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.stub.LoadStub;
import sviolet.turquoise.x.imageloader.stub.Stub;
import sviolet.turquoise.x.imageloader.stub.StubFactory;

/**
 * 基础用法:自定义StubFactory: 新增对TextView控件的加载支持.
 *
 * Created by S.Violet on 2016/4/27.
 */
public class MyStubFactory extends StubFactory {

    @Override
    public Stub newLoadStub(String url, Params params, View view) {
        //check view
        if (view instanceof TextView){
            return new TextViewStub(url, params, (TextView) view);
        }
        return null;
    }

    private static class TextViewStub extends LoadStub<TextView>{

        public TextViewStub(String url, Params params, TextView view) {
            super(url, params, view);
        }

        @Override
        protected void setDrawableToView(Drawable drawable, TextView view) {
            //set drawable to view
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            view.setCompoundDrawables(drawable, null, null, null);
        }

    }

}
