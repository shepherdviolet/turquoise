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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.engine.Engine;

/**
 *
 * Created by S.Violet on 2016/2/29.
 */
public class NodeManager implements ComponentManager.Component {

    private ComponentManager manager;

    private final HashMap<String, Node> nodes = new HashMap<>();

    private final ReentrantLock nodesLock = new ReentrantLock();

    public NodeManager(ComponentManager manager){
        this.manager = manager;
    }

    @Override
    public void init() {

    }

    public Node fetchNode(Context context) {
        if (context == null){
            throw new RuntimeException("[NodeManager]can not fetch Node with out Context");
        }
        final String nodeId = parseNodeId(context);
        Node node = null;
        try{
            nodesLock.lock();
            node = nodes.get(nodeId);
            if (node == null){
                node = manager.getNodeFactory().newNode(nodeId);
                nodes.put(nodeId, node);
                node.attachLifeCycle(context);
            }
        }finally {
            nodesLock.unlock();
        }
        return node;
    }

    public List<NodeTask> pullNodeTasks(Engine.Type type){
        List<NodeTask> taskList = new ArrayList<>();
        try{
            nodesLock.lock();
            for (Map.Entry<String, Node> entry : nodes.entrySet()){
                NodeTask task = entry.getValue().pullNodeTask(type);
                if (task != null){
                    taskList.add(task);
                }
            }
        }finally {
            nodesLock.unlock();
        }
        return taskList;
    }

    public void response(NodeTask task){
        String nodeId = task.getNodeId();
        Node node;
        try{
            nodesLock.lock();
            node = nodes.get(nodeId);
        }finally {
            nodesLock.unlock();
        }
        if (node != null){
            node.response(task);
        }
    }

    public void scrapNode(Node node){
        if (node == null){
            return;
        }
        try{
            nodesLock.lock();
            nodes.remove(node.getId());
        }finally {
            nodesLock.unlock();
        }
    }

    private String parseNodeId(Context context){
        return String.valueOf(context);
    }

}
