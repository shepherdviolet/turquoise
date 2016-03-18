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

package sviolet.turquoise.x.imageloader.stub;

import android.view.View;
import android.widget.ImageView;

import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.entity.Params;

/**
 *
 * Created by S.Violet on 2016/2/23.
 */
public class StubFactoryImpl implements StubFactory {

    private StubFactory customStubFactory;

    @Override
    public Stub newLoadStub(String url, Params params, View view, Params defParams) {
        if (url == null){
            throw new RuntimeException("[TILoader]can't load image without url!");
        }
        if (view == null){
            throw new RuntimeException("[TILoader]can't load image into a null View!");
        }
        if (params == null){
            params = defParams;
        }
        Stub stub = null;
        if (customStubFactory != null){
            stub = customStubFactory.newLoadStub(url, params, view, defParams);
        }
        if (stub == null){
            stub = newLoadStubInner(url, params, view, defParams);
        }
        if (stub == null){
            throw new RuntimeException("[TILoader]unsupported view:<" + view.getClass().getName() + ">, can't load image into it, 0x00");
        }
        return stub;
    }

    protected Stub newLoadStubInner(String url, Params params, View view, Params defParams){
        if (view instanceof ImageView){
            return new ImageViewLoadStub(url, params, view);
        }
        return null;
    }

    @Override
    public Stub newLoadBackgroundStub(String url, Params params, View view, Params defParams) {
        if (url == null){
            throw new RuntimeException("[TILoader]can't load image without url!");
        }
        if (view == null){
            throw new RuntimeException("[TILoader]can't load image into a null View!");
        }
        if (params == null){
            params = defParams;
        }
        Stub stub = null;
        if (customStubFactory != null){
            stub = customStubFactory.newLoadBackgroundStub(url, params, view, defParams);
        }
        if (stub == null){
            stub = newLoadBackgroundStubInner(url, params, view, defParams);
        }
        if (stub == null){
            throw new RuntimeException("[TILoader]unsupported view:<" + view.getClass().getName() + ">, can't load background image into it, 0x01");
        }
        return stub;
    }

    protected Stub newLoadBackgroundStubInner(String url, Params params, View view, Params defParams){
        return new LoadBackgroundStub(url, params, view);
    }

    @Override
    public Stub newExtractStub(String url, Params params, OnLoadedListener listener, Params defParams) {
        if (url == null){
            throw new RuntimeException("[TILoader]can't load image without url!");
        }
        if (listener == null){
            throw new RuntimeException("[TILoader]can't load image into a null View!");
        }
        if (params == null){
            params = defParams;
        }
        Stub stub = null;
        if (customStubFactory != null){
            stub = customStubFactory.newExtractStub(url, params, listener, defParams);
        }
        if (stub == null){
            stub = newExtractStubInner(url, params, listener, defParams);
        }
        if (stub == null){
            throw new RuntimeException("[TILoader]unsupported listener:<" + listener.getClass().getName() + ">, can't extract image, 0x02");
        }
        return stub;
    }

    protected Stub newExtractStubInner(String url, Params params, OnLoadedListener listener, Params defParams){
        return new ExtractStub(url, params, listener);
    }

    public void setCustomStubFactory(StubFactory factory) {
        this.customStubFactory = factory;
    }

}
