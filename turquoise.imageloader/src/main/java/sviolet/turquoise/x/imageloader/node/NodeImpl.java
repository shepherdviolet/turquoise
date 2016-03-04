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

import android.view.View;

import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.engine.Engine;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;
import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.task.Task;

/**
 * Created by S.Violet on 2016/2/18.
 */
public class NodeImpl extends Node {

    private String nodeId;
    private ComponentManager manager;
    private NodeController controller;

    public NodeImpl(ComponentManager manager, String nodeId){
        this.manager = manager;
        this.nodeId = nodeId;
        this.controller = new NodeControllerImpl(manager, this);
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
        Task task = manager.getTaskFactory().newLoadTask(url, params, view);
        task.initialize(controller);
    }

    @Override
    public void loadBackground(String url, View view) {
        loadBackground(url, null, view);
    }

    @Override
    public void loadBackground(String url, Params params, View view) {
        Task task = manager.getTaskFactory().newLoadBackgroundTask(url, params, view);
        task.initialize(controller);
    }

    @Override
    public void extract(String url, Params params, OnLoadedListener listener) {
        Task task = manager.getTaskFactory().newExtractTask(url, params, listener);
        task.initialize(controller);
    }

    @Override
    public void setting(NodeSettings settings) {

    }

    @Override
    protected String getId() {
        return this.toString();
    }

    @Override
    protected NodeTask pullNodeTask(Engine.Type type) {
        return null;
    }

    @Override
    protected void response(NodeTask task) {
        controller.response(task);
    }

    /*********************************************
     * lifecycle
     */

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }

}
