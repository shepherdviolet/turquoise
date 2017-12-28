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

package sviolet.turquoise.x.imageloader.entity;

import java.lang.ref.WeakReference;

/**
 * <p>callback when image extract succeed or canceled</p>
 *
 * <p>Improper use may cause a memory leak, ExtractNode will hold this listener util extract finished,
 * avoid being an internal class and holding Context/View object, you can use method
 * {@link OnLoadedListener#setWeakRegister(Object)} to hold Context/View by {@link WeakReference},
 * and {@link OnLoadedListener#getWeakRegister()} when callback.</p>
 *
 * <pre>{@code
 *      TILoader.extract(this, url, params, new OnLoadedListener<XXXActivity>() {
 *         protected void onLoadSucceed(String url, Params params, ImageResource resource) {
 *              XXXActivity activity = getWeakRegister();
 *              if (activity != null){
 *                  //do something
 *              }
 *         }
 *         protected void onLoadCanceled(String url, Params params) {
 *
 *         }
 *      }.setWeakRegister(this));
 * }</pre>
 *
 * Created by S.Violet on 2016/2/16.
 */
public abstract class OnLoadedListener<T> {

    private WeakReference<T> weakRegister;

    /**
     * callback when loading succeed
     *
     * @param url URL
     * @param params loading params
     * @param resource loaded Image, may be null
     */
    public abstract void onLoadSucceed(String url, Params params, ImageResource resource);

    /**
     * callback when loading canceled
     *
     * @param url URL
     * @param params loading params
     */
    public abstract void onLoadCanceled(String url, Params params);

    /**
     * @return get object which hold by WeakReference
     */
    public T getWeakRegister() {
        if (weakRegister != null){
            return weakRegister.get();
        }
        return null;
    }

    /**
     * @param weakRegister set object and hold by WeakReference
     */
    public OnLoadedListener<T> setWeakRegister(T weakRegister) {
        this.weakRegister = new WeakReference<>(weakRegister);
        return this;
    }
}
