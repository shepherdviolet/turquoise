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

import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.entity.Params;

/**
 * <p>Factory of stubs</p>
 *
 * Created by S.Violet on 2016/2/23.
 */
public abstract class StubFactory {

    /**
     * <p>create stub according to the type of View, for TILoader.node(context).load(...).
     * the stub will load image to view (not background).</p>
     *
     * @param url url
     * @param params params
     * @param view view
     * @return Stub
     */
    public abstract Stub newLoadStub(String url, Params params, View view);

    /**
     * <p>Usually no need to override (just return null).</p>
     * <p>create stub according to the type of View, for TILoader.node(context).loadBackground(...).
     * the stub will load image to view's background.</p>
     *
     * @param url url
     * @param params params
     * @param view view
     * @return Stub
     */
    public Stub newLoadBackgroundStub(String url, Params params, View view){
        return null;
    }

    /**
     * <p>Usually no need to override (just return null).</p>
     * <p>create stub according to the type of listener, for TILoader.extract(...).
     * the stub will extract image and callback listener.</p>
     *
     * @param url url
     * @param params params
     * @param listener listener
     * @return Stub
     */
    public Stub newExtractStub(String url, Params params, OnLoadedListener listener){
        return null;
    }

}
