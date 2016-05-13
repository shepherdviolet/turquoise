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

import sviolet.turquoise.x.imageloader.entity.IndispensableState;
import sviolet.turquoise.x.imageloader.entity.LoadProgress;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.server.Server;
import sviolet.turquoise.x.imageloader.stub.Stub;

/**
 *
 * <p>Loading Task</p>
 *
 * <p>Context of a loading task.</p>
 *
 * Created by S.Violet on 2016/2/17.
 */
public class Task {

    private Info values;

    Task(String nodeId, Stub.Type type, String url, Params params, String key, String resourceKey, LoadProgress loadProgress, IndispensableState indispensableState) {
        this.values = new Info(nodeId, type, url, params, key, resourceKey, loadProgress, indispensableState);
    }

    @Override
    public String toString() {
        return values.toString();
    }

    public Stub.Type getType() {
        return values.type;
    }

    public String getUrl() {
        return values.url;
    }

    public Params getParams() {
        return values.params;
    }

    public String getNodeId(){
        return values.nodeId;
    }

    public String getKey(){
        return values.key;
    }

    public String getResourceKey(){
        return values.resourceKey;
    }

    public Server.Type getServerType() {
        return values.serverType;
    }

    public void setServerType(Server.Type serverType) {
        values.serverType = serverType;
    }

    public State getState() {
        return values.state;
    }

    public void setState(State state) {
        values.state = state;
    }

    public LoadProgress getLoadProgress(){
        return values.loadProgress;
    }

    public boolean isIndispensable(){
        return values.isIndispensable();
    }

    public NodeSettings getNodeSettings() {
        return values.nodeSettings;
    }

    public void setNodeSettings(NodeSettings nodeSettings) {
        values.nodeSettings = nodeSettings;
    }

    public Info getTaskInfo(){
        return values;
    }

    public enum State{
        STAND_BY,
        SUCCEED,
        FAILED,
        CANCELED
    }

    public static class Info{

        private static final String SEPARATOR = "-";

        //status//////////////////////////
        private Stub.Type type;
        private Server.Type serverType = Server.Type.MEMORY_ENGINE;
        private volatile State state = State.STAND_BY;
        private LoadProgress loadProgress;
        private IndispensableState indispensableState;

        //node//////////////////////////
        private String nodeId;
        private NodeSettings nodeSettings;

        //params//////////////////////////
        private String url;
        private Params params;
        private String key;
        private String resourceKey;

        Info(String nodeId, Stub.Type type, String url, Params params, String key, String resourceKey, LoadProgress loadProgress, IndispensableState indispensableState) {
            this.nodeId = nodeId;
            this.type = type;
            this.url = url;
            this.params = params;
            this.key = key;
            this.resourceKey = resourceKey;
            this.loadProgress = loadProgress;
            this.indispensableState = indispensableState;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("[Task]<");
            builder.append(serverType);
            builder.append(SEPARATOR);
            builder.append(state);
            builder.append(SEPARATOR);
            builder.append(nodeId);
            builder.append("><");
            builder.append(key);
            builder.append("><");
            builder.append(url);
            builder.append(">");
            return builder.toString();
        }

        public Stub.Type getType() {
            return type;
        }

        public Server.Type getServerType() {
            return serverType;
        }

        public State getState() {
            return state;
        }

        public String getNodeId() {
            return nodeId;
        }

        public NodeSettings getNodeSettings() {
            return nodeSettings;
        }

        public String getUrl() {
            return url;
        }

        public Params getParams() {
            return params;
        }

        public String getKey() {
            return key;
        }

        public String getResourceKey() {
            return resourceKey;
        }

        public LoadProgress getLoadProgress(){
            return loadProgress;
        }

        public boolean isIndispensable(){
            return indispensableState.isIndispensable();
        }
    }

}
