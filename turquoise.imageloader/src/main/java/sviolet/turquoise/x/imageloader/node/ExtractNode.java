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

package sviolet.turquoise.x.imageloader.node;

import android.content.Context;

import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.stub.Stub;
import sviolet.turquoise.x.imageloader.stub.StubRemoter;

/**
 * <p>Node for extract</p>
 *
 * Created by S.Violet on 2016/4/22.
 */
public class ExtractNode extends LoadNode {

    ExtractNode(ComponentManager manager, String nodeId) {
        super(manager, nodeId, true);
    }

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * [Initialize Node]this method will initialize Node<br/>
     * extract Image, without memory cache and disk cache<br/>
     * you should recycle Bitmap by yourself<br/>
     * @param url URL
     * @param params loading params
     * @param listener callback when loading succeed / canceled / failed
     * @return {@link StubRemoter}
     */
    public StubRemoter extract(String url, Params params, OnLoadedListener listener) {
        getManager().waitingForInitialized();
        getController().waitingForInitialized();
        Stub stub = getManager().getServerSettings().getStubFactory().newExtractStub(url, params, listener);
        stub.initialize(getController());
        return stub.getStubRemoter();
    }

    @Override
    protected void attachLifeCycle(Context context) {
        //do nothing
    }
}
