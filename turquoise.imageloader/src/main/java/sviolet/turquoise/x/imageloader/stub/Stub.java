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

import sviolet.turquoise.utilx.lifecycle.listener.Destroyable;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.node.NodeController;

/**
 *
 * Created by S.Violet on 2016/2/19.
 */
public interface Stub extends Destroyable {

    /**
     * callback when loading succeed
     *
     * @param resource loaded Image, may be null
     */
    void onLoadSucceed(ImageResource<?> resource);

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
     * @return return true when load valid
     */
    boolean load();

    /**
     * reload image(has times limit)
     * @return return true when load valid
     */
    boolean reload();

    Type getType();

    String getUrl();

    Params getParams();

    /**
     * @return key of loading task (Cache)
     */
    String getKey();

    /**
     * @return key of target resource (Net/Disk)
     */
    String getDiskKey();

    State getState();

    enum Type{
        LOAD,
        EXTRACT
    }

    /***********************************************************
     * Enum
     */

    enum State{
        BEFORE_INIT,
        INITIALIZED,
        LOADING,
        LOAD_SUCCEED,
        LOAD_FAILED,
        LOAD_CANCELED,
        DESTROYED
    }

}
