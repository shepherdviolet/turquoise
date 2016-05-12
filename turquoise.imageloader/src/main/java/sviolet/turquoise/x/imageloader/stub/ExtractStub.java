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

import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.node.NodeController;

/**
 * <p>Extract Stub</p>
 *
 * <p>loading image and callback to listener</p>
 *
 * Created by S.Violet on 2016/2/19.
 */
class ExtractStub extends AbsStub {

    private OnLoadedListener listener;

    ExtractStub(String url, Params params, OnLoadedListener listener){
        super(url, params);
        this.listener = listener;
    }

    @Override
    public void initialize(NodeController controller) {
        super.initialize(controller);
        launch();
    }

    /*******************************************************8
     * control inner
     */

    @Override
    protected LaunchResult onLaunch() {
        return super.onLaunch();
    }

    @Override
    protected LaunchResult onRelaunch() {
        return super.onRelaunch();
    }

    /*******************************************************8
     * callbacks inner
     */

    @Override
    protected void onLoadSucceedInner(ImageResource resource) {
        super.onLoadSucceedInner(resource);
        //get & check controller
        NodeController controller = getNodeController();
        if (controller == null){
            onDestroy();
            return;
        }
        //check valid
        if (!controller.getServerSettings().getImageResourceHandler().isValid(resource)){
            shiftSucceedToFailed();
            return;
        }
        if (listener != null){
            listener.onLoadSucceed(getUrl(), getParams(), resource);
        }else{
            getLogger().e("[ExtractStub]missing listener, can't callback(succeed), key:" + getKey());
        }
    }

    @Override
    protected void onLoadFailedInner() {
        super.onLoadFailedInner();
    }

    @Override
    protected void onLoadCanceledInner() {
        super.onLoadCanceledInner();
        if (listener != null){
            listener.onLoadCanceled(getUrl(), getParams());
        }else{
            getLogger().e("[ExtractStub]missing listener, can't callback(canceled), key:" + getKey());
        }
    }

    @Override
    protected void onDestroyInner() {
        super.onDestroyInner();
    }

    /***********************************************************
     * Getter
     */

    @Override
    public Type getType() {
        return Type.EXTRACT;
    }
}
