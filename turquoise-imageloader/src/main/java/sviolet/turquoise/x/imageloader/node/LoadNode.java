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

package sviolet.turquoise.x.imageloader.node;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import sviolet.turquoise.utilx.lifecycle.LifeCycleUtils;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.server.Engine;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;
import sviolet.turquoise.x.imageloader.stub.Stub;

/**
 * <p>Node for loading</p>
 *
 * Created by S.Violet on 2016/2/18.
 */
public class LoadNode extends Node {

    private ComponentManager manager;
    private NodeController controller;

    LoadNode(ComponentManager manager, String nodeId){
        this(manager, nodeId, false);
    }

    LoadNode(ComponentManager manager, String nodeId, boolean infiniteRequestQueue){
        this.manager = manager;
        this.controller = new NodeControllerImpl(manager, this, nodeId, infiniteRequestQueue);
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
        try {
            Stub stub = manager.getServerSettings().getStubFactory().newLoadStub(url, params, view);
            stub.initialize(controller);
        } catch (Exception e){
            getManager().getLogger().e("[LoadNode]error while creating or initializing LoadStub, url:" + url, e);
        }
    }

    @Override
    public void loadBackground(String url, View view) {
        loadBackground(url, null, view);
    }

    @Override
    public void loadBackground(String url, Params params, View view) {
        manager.waitingForInitialized();
        controller.waitingForInitialized();
        try {
            Stub stub = manager.getServerSettings().getStubFactory().newLoadBackgroundStub(url, params, view);
            stub.initialize(controller);
        } catch (Exception e){
            getManager().getLogger().e("[LoadNode]error while creating or initializing LoadStub, url:" + url, e);
        }
    }

    /********************************************
     * public
     */

    @Override
    public boolean setting(NodeSettings settings) {
        return controller.settingNode(settings);
    }

    /********************************************
     * private
     */

    @Override
    String getId() {
        return controller.getNodeId();
    }

    @Override
    Task pullTask(Engine.Type type) {
        return controller.pullTask(type);
    }

    @Override
    void response(Task task) {
        controller.response(task);
    }

    @Override
    protected void attachLifeCycle(Context context) {
        if (context instanceof Activity){
            LifeCycleUtils.attach((Activity) context, controller);
        }else{
            throw new RuntimeException("[LoadNode]can't attach Node on this Context, class=" + context.getClass().getName());
        }
    }

    protected ComponentManager getManager() {
        return manager;
    }

    protected NodeController getController() {
        return controller;
    }

    /***************************************************
     * NodeRemoter
     */

    /**
     * @see NodeRemoter
     */
    @Override
    public NodeRemoter newNodeRemoter() {
        return controller.newNodeRemoter();
    }

}
