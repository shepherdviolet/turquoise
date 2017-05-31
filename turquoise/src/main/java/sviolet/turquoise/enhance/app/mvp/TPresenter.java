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

package sviolet.turquoise.enhance.app.mvp;

import java.lang.ref.WeakReference;

/**
 * MVP模式Presenter
 * 将Activity视为View层, 只负责视图逻辑, Presenter层负责业务逻辑. Presenter弱引用持有View, View强引用持有
 * Presenter, 当View层销毁时, Presenter层不会强持有View, 避免了Context的内存泄漏.
 *
 * Created by S.Violet on 2017/5/31.
 */
public abstract class TPresenter<V extends TView>{

    private WeakReference<V> viewLayerRef;

    /**
     * @param viewLayer 弱引用持有view层
     */
    public TPresenter(V viewLayer){
        this.viewLayerRef = new WeakReference<>(viewLayer);
    }

    /**
     * @return 获得弱引用持有的View实例, 可能为空
     */
    protected V getViewLayer(){
        return viewLayerRef.get();
    }

    /**
     * 刷新View层
     * @param code 自定义标记
     */
    protected void refreshViewLayer(int code){
        V viewLayer = getViewLayer();
        if (viewLayer != null){
            viewLayer.onPresenterRefresh(code);
        }
    }

}
