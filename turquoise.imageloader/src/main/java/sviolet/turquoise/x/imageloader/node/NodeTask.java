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

import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.server.Server;
import sviolet.turquoise.x.imageloader.task.Task;

/**
 * Created by S.Violet on 2016/2/17.
 */
public class NodeTask {

    private Task.Type type;
    private String url;
    private Params params;
    private String nodeId;
    private String key;
    private String diskKey;

    private Server.Type serverType = Server.Type.CACHE;
    private volatile State state = State.STAND_BY;

    NodeTask(String nodeId, Task.Type type, String key, String diskKey, String url, Params params) {
        this.type = type;
        this.url = url;
        this.params = params;
        this.nodeId = nodeId;
        this.key = key;
        this.diskKey = diskKey;
    }

    public Task.Type getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public Params getParams() {
        return params;
    }

    public String getNodeId(){
        return nodeId;
    }

    public String getKey(){
        return key;
    }

    public Server.Type getServerType() {
        return serverType;
    }

    public void setServerType(Server.Type serverType) {
        this.serverType = serverType;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum State{
        STAND_BY,
        SUCCEED,
        FAILED,
        CANCELED
    }

}
