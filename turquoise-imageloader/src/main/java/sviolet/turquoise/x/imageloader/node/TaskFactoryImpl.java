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

import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.stub.Stub;
import sviolet.turquoise.x.imageloader.stub.StubGroup;

/**
 * <p>Task Factory</p>
 *
 * Created by S.Violet on 2016/3/3.
 */
public class TaskFactoryImpl implements TaskFactory {

    private ComponentManager manager;

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
    }

    @Override
    public Task newTask(NodeController controller, Stub stub, StubGroup stubGroup) {
        return new Task(controller.getNodeId(), stub.getType(), stub.getUrl(), stub.getParams(), stub.getKey(), stub.getResourceKey(), stub.getLoadProgress().reset(), stubGroup.getIndispensableState());
    }

}
