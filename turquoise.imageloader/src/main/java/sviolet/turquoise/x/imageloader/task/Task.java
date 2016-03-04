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

package sviolet.turquoise.x.imageloader.task;

import android.graphics.Bitmap;

import sviolet.turquoise.utilx.lifecycle.listener.Destroyable;
import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.node.NodeController;

/**
 *
 * Created by S.Violet on 2016/2/19.
 */
public interface Task extends Destroyable {

    /**
     * callback when loading succeed
     *
     * @param bitmap loaded Bitmap, may be null
     */
    void onLoadSucceed(Bitmap bitmap);

    /**
     * callback when loading failed
     */
    void onLoadFailed();

    /**
     * callback when loading canceled
     */
    void onLoadCanceled();

    /**
     * initialize process<br/>
     * 1.bind View or Callback<br/>
     * 2.start loading if needed<br/>
     * @param controller use WeakReference to hold the NodeController
     */
    void initialize(NodeController controller);

    /**
     * load image
     */
    void load();

    /**
     * reload image(has times limit)
     */
    void reload();

    Type getType();

    String getUrl();

    Params getParams();

    /**
     * @return identity of Task, or key of MemoryCache
     */
    String getKey();

    /**
     * @return key of DiskCache
     */
    String getDiskKey();

    public enum Type{
        LOAD,
        EXTRACT
    }

}
