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

package sviolet.turquoise.x.imageloader.stub.support;

import android.view.View;
import android.widget.ImageView;

import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.entity.ServerSettings;
import sviolet.turquoise.x.imageloader.stub.Stub;
import sviolet.turquoise.x.imageloader.stub.StubFactory;

/**
 * <p>Stub factory</p>
 *
 * @author S.Violet
 */
public final class StubFactoryImpl extends StubFactory {

    private StubFactory customStubFactory;
    private int urlLengthLimit = ServerSettings.DEFAULT_URL_LENGTH_LIMIT;

    @Override
    public final Stub newLoadStub(String url, Params params, View view) {
        //check input
        checkInput(url, view);
        //copy params
        if (params == null){
            params = new Params.Builder().build();
        }else{
            params = params.copy();
        }
        //invoke custom factory
        Stub stub = null;
        if (customStubFactory != null){
            stub = customStubFactory.newLoadStub(url, params, view);
        }
        if (stub == null){
            stub = newLoadStubInner(url, params, view);
        }
        if (stub == null){
            throw new RuntimeException("[TILoader]unsupported view:<" + view.getClass().getName() + ">, can't load image into it, 0x00");
        }
        return stub;
    }

    protected final Stub newLoadStubInner(String url, Params params, View view){
        if (view instanceof ImageView){
            return new ImageViewLoadStub(url, params, (ImageView) view);
        }
        return null;
    }

    @Override
    public final Stub newLoadBackgroundStub(String url, Params params, View view) {
        //check input
        checkInput(url, view);
        //copy params
        if (params == null){
            params = new Params.Builder().build();
        }else{
            params = params.copy();
        }
        //invoke custom factory
        Stub stub = null;
        if (customStubFactory != null){
            stub = customStubFactory.newLoadBackgroundStub(url, params, view);
        }
        if (stub == null){
            stub = newLoadBackgroundStubInner(url, params, view);
        }
        if (stub == null){
            throw new RuntimeException("[TILoader]unsupported view:<" + view.getClass().getName() + ">, can't load background image into it, 0x01");
        }
        return stub;
    }

    protected final Stub newLoadBackgroundStubInner(String url, Params params, View view){
        return new BackgroundLoadStub(url, params, view);
    }

    @Override
    public final Stub newExtractStub(String url, Params params, OnLoadedListener listener) {
        //check input
        checkInput(url, listener);
        //copy params
        if (params == null){
            params = new Params.Builder().build();
        }else{
            params = params.copy();
        }
        //invoke custom factory
        Stub stub = null;
        if (customStubFactory != null){
            stub = customStubFactory.newExtractStub(url, params, listener);
        }
        if (stub == null){
            stub = newExtractStubInner(url, params, listener);
        }
        if (stub == null){
            throw new RuntimeException("[TILoader]unsupported listener:<" + listener.getClass().getName() + ">, can't extract image, 0x02");
        }
        return stub;
    }

    private void checkInput(String url, View view) {
        if (url == null){
            throw new RuntimeException("[TILoader]can't load image without url!");
        }
        if (url.length() > urlLengthLimit){
            throw new RuntimeException("[TILoader]Url is too long! limit:" + urlLengthLimit);
        }
        if (view == null){
            throw new RuntimeException("[TILoader]can't load image into a null View!");
        }
    }

    private void checkInput(String url, OnLoadedListener listener) {
        if (url == null){
            throw new RuntimeException("[TILoader]can't load image without url!");
        }
        if (url.length() > urlLengthLimit){
            throw new RuntimeException("[TILoader]Url is too long! limit:" + urlLengthLimit);
        }
        if (listener == null){
            throw new RuntimeException("[TILoader]can't extract image without listener!");
        }
    }

    protected final Stub newExtractStubInner(String url, Params params, OnLoadedListener listener){
        return new ExtractStub(url, params, listener);
    }

    public final void setCustomStubFactory(StubFactory factory) {
        this.customStubFactory = factory;
    }

    public void setUrlLengthLimit(int urlLengthLimit){
        this.urlLengthLimit = urlLengthLimit;
    }

}
