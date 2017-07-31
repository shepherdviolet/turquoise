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

import sviolet.thistle.common.entity.Destroyable;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.LoadProgress;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.node.NodeController;
import sviolet.turquoise.x.imageloader.node.Node;

/**
 * <p>Manage image loading process of view or extract affairs: launch, relaunch, reload, update view or callback to listener.
 * Maintain the relationship between the view/listener and the load {@link Node}</p>
 *
 * Created by S.Violet on 2016/2/19.
 */
public interface Stub extends Destroyable {

    /**
     * initialize process<br/>
     * 1.bind View or Callback<br/>
     * 2.start loading if needed<br/>
     * @param controller use WeakReference to hold the NodeController
     */
    void initialize(NodeController controller);

    /******************************************************************
     * callbacks
     */

    /**
     * callback when loading succeed
     *
     * @param resource loaded Image, may be null
     */
    void onLoadSucceed(ImageResource resource);

    /**
     * callback when loading failed
     */
    void onLoadFailed();

    /**
     * callback when loading canceled
     */
    void onLoadCanceled();

    /******************************************************************
     * control
     */

    /**
     * launch loading process, first time effective
     * @return LaunchResult
     */
    LaunchResult launch();

    /**
     * relaunch loading process, effective when launch succeed/failed/canceled
     * @param force false : relaunch only when loading canceled, true : force relaunch when loading succeed/failed/canceled
     * @return LaunchResult
     */
    LaunchResult relaunch(boolean force);

    /***********************************************************
     * params
     */

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
    String getResourceKey();

    int getState();

    LoadProgress getLoadProgress();

    /***********************************************************
     * getter
     */

    StubRemoter getStubRemoter();

    TLogger getLogger();

    /***********************************************************
     * Enum
     */

    enum Type{
        LOAD,
        EXTRACT
    }

    enum LaunchResult{
        //launch succeed, don't launch again
        SUCCEED,
        //try launch again
        RETRY,
        //can't launch any more
        FAILED
    }

    /**
     *  INITIAL = 0;<br/>
     *  LAUNCHING = 1;<br/>
     *  LOADING = 2;<br/>
     *  LOAD_SUCCEED = 3;<br/>
     *  LOAD_FAILED = 4;<br/>
     *  LOAD_CANCELED = 5;<br/>
     *  DESTROYED = 6;<br/>
     */
    class State{
        public static final int INITIAL = 0;
        public static final int LAUNCHING = 1;
        public static final int LOADING = 2;
        public static final int LOAD_SUCCEED = 3;
        public static final int LOAD_FAILED = 4;
        public static final int LOAD_CANCELED = 5;
        public static final int DESTROYED = 6;
    }

}
