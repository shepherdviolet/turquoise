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

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import sviolet.turquoise.utilx.lifecycle.LifeCycleUtils;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.server.Engine;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;
import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.stub.Stub;

/**
 *
 * Created by S.Violet on 2016/2/18.
 */
public class NodeImpl extends Node {

    private ComponentManager manager;
    private NodeController controller;

    public NodeImpl(ComponentManager manager, String nodeId){
        this.manager = manager;
        this.controller = new NodeControllerImpl(manager, this, nodeId);
    }

    /********************************************
     * load
     */

    @Override
    public void load(String url, View view) {
        load(url, null, view);
    }

    @Override
    public void load(String url, Params params, View view) {
        manager.waitingForInitialized();
        controller.waitingForInitialized();
        Stub stub = manager.getServerSettings().getStubFactory().newLoadStub(url, params, view, manager.getDefaultParams());
        stub.initialize(controller);
    }

    @Override
    public void loadBackground(String url, View view) {
        loadBackground(url, null, view);
    }

    @Override
    public void loadBackground(String url, Params params, View view) {
        manager.waitingForInitialized();
        controller.waitingForInitialized();
        Stub stub = manager.getServerSettings().getStubFactory().newLoadBackgroundStub(url, params, view, manager.getDefaultParams());
        stub.initialize(controller);
    }

    @Override
    public void extract(String url, Params params, OnLoadedListener listener) {
        manager.waitingForInitialized();
        controller.waitingForInitialized();
        Stub stub = manager.getServerSettings().getStubFactory().newExtractStub(url, params, listener, manager.getDefaultParams());
        stub.initialize(controller);
    }

    @Override
    public boolean setting(NodeSettings settings) {
        return controller.settingNode(settings);
    }

    @Override
    String getId() {
        return controller.getNodeId();
    }

    @Override
    NodeTask pullNodeTask(Engine.Type type) {
        return controller.pullNodeTask(type);
    }

    @Override
    void response(NodeTask task) {
        controller.response(task);
    }

    @Override
    void attachLifeCycle(Context context) {
        if (context instanceof FragmentActivity){
            LifeCycleUtils.attach((FragmentActivity) context, controller);
        }else if (context instanceof Activity){
            LifeCycleUtils.attach((Activity) context, controller);
        }else{
            throw new RuntimeException("[NodeImpl]can't attach Node on this Context, class=" + context.getClass().getName());
        }
    }

}
