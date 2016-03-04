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

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.util.conversion.ByteUtils;
import sviolet.turquoise.util.crypt.DigestCipher;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.node.NodeController;

/**
 * Created by S.Violet on 2016/2/26.
 */
public abstract class AbsTask implements Task {

    private static final String NULL = "null";

    private String url;
    private Params params;
    private String key;
    private String diskKey;

    private WeakReference<NodeController> nodeController;

    private State state = State.BEFORE_INIT;

    private final ReentrantLock stateLock = new ReentrantLock();

    public AbsTask(String url, Params params){
        this.url = url;
        this.params = params;
    }

    @Override
    public void initialize(NodeController nodeController) {
        this.nodeController = new WeakReference<>(nodeController);
    }

    @Override
    public void load() {
        final NodeController controller = getNodeController();
        if (controller == null){
            onDestroy();
            return;
        }
        try {
            stateLock.lock();
            controller.executeTask(this);
            state = State.LOADING;
        }finally {
            stateLock.unlock();
        }
    }

    @Override
    public void reload() {

    }

    @Override
    public void onLoadSucceed(Bitmap bitmap) {

    }

    @Override
    public void onLoadFailed() {

    }

    @Override
    public void onLoadCanceled() {

    }

    @Override
    public void onDestroy() {

    }

    /***********************************************************
     * protected
     */

    /***********************************************************
     * Getter
     */

    @Override
    public String getUrl(){
        return this.url;
    }

    @Override
    public Params getParams(){
        return this.params;
    }

    @Override
    public String getKey(){
        if (key == null) {
            if (params == null)
                params = new Params.Builder().build();
            key = getDiskKey() + "@" + Integer.toString(params.getReqWidth()) + "x" + Integer.toString(params.getReqHeight());
        }
        return key;
    }

    @Override
    public String getDiskKey() {
        if (diskKey == null){
            String url = getUrl();
            if (url == null)
                url = NULL;
            diskKey = ByteUtils.byteToHex(DigestCipher.digest(url, DigestCipher.TYPE_SHA1));
        }
        return diskKey;
    }

    protected NodeController getNodeController(){
        if (this.nodeController!= null){
            return nodeController.get();
        }
        return null;
    }

    /***********************************************************
     * Enum
     */

    private enum State{
        BEFORE_INIT,
        INITIALIZED,
        LOADING,
        LOAD_SUCCEED,
        LOAD_FAILED,
        LOAD_CANCELED,
        DESTROYED
    }

}
