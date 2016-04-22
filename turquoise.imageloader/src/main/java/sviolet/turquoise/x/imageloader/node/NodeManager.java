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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.server.Server;

/**
 *
 * Created by S.Violet on 2016/2/29.
 */
public class NodeManager {

    private static final String EXTRACT_NODE_ID = "###ExtractNode###";

    private ComponentManager manager;

    private final Map<String, Node> nodes = new ConcurrentHashMap<>();
    private final ReentrantLock nodesLock = new ReentrantLock();

    public NodeManager(ComponentManager manager){
        this.manager = manager;
    }

    public Node fetchNode(Context context) {
        if (context == null){
            throw new RuntimeException("[NodeManager]can not fetch Node with out Context");
        }
        final String nodeId = parseNodeId(context);
        Node node = nodes.get(nodeId);
        //double check
        if (node == null) {
            try {
                nodesLock.lock();
                node = nodes.get(nodeId);
                if (node == null) {
                    manager.setContextImage(context);
                    manager.setApplicationContextImage(context.getApplicationContext());
                    node = manager.getNodeFactory().newNode(nodeId);
                    nodes.put(nodeId, node);
                    node.attachLifeCycle(context);
                }
            } finally {
                nodesLock.unlock();
            }
        }
        return node;
    }

    public ExtractNode fetchExtractNode(Context context) {
        if (context == null){
            throw new RuntimeException("[NodeManager]can not fetch extract Node with out Context");
        }
        Node node = nodes.get(EXTRACT_NODE_ID);
        //double check
        if (node == null) {
            try {
                nodesLock.lock();
                node = nodes.get(EXTRACT_NODE_ID);
                if (node == null) {
                    manager.setApplicationContextImage(context.getApplicationContext());
                    node = manager.getNodeFactory().newExtractNode(EXTRACT_NODE_ID);
                    nodes.put(EXTRACT_NODE_ID, node);
                }
            } finally {
                nodesLock.unlock();
            }
        }
        if (node instanceof ExtractNode) {
            return (ExtractNode)node;
        }else{
            throw new RuntimeException("[TILoader:NodeManager]fetchExtractNode: can't convert Node to ExtractNode");
        }
    }

    public List<Task> pullTasks(Server.Type type){
        List<Task> taskList = new ArrayList<>();

        for (Map.Entry<String, Node> entry : nodes.entrySet()) {
            Task task = entry.getValue().pullTask(type);
            if (task != null) {
                taskList.add(task);
            }
        }

        return taskList;
    }

    public void response(Task task){
        String nodeId = task.getNodeId();
        Node node = nodes.get(nodeId);

        if (node != null){
            node.response(task);
        }
    }

    public void scrapNode(Node node){
        if (node == null){
            return;
        }
        nodes.remove(node.getId());
    }

    private String parseNodeId(Context context){
        return String.valueOf(context);
    }

}
