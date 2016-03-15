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

package sviolet.turquoise.x.imageloader.entity;

import android.content.Context;

import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.handler.ImageResourceHandler;
import sviolet.turquoise.x.imageloader.handler.def.DefaultImageResourceHandler;
import sviolet.turquoise.x.imageloader.node.NodeFactory;
import sviolet.turquoise.x.imageloader.node.NodeFactoryImpl;
import sviolet.turquoise.x.imageloader.node.NodeTaskFactory;
import sviolet.turquoise.x.imageloader.node.NodeTaskFactoryImpl;
import sviolet.turquoise.x.imageloader.task.TaskFactory;
import sviolet.turquoise.x.imageloader.task.TaskFactoryImpl;

/**
 *
 * Created by S.Violet on 2016/2/16.
 */
public class EngineSettings implements ComponentManager.Component{

    private ComponentManager manager;
    private Values values;

    private EngineSettings(Values values) {
        this.values = values;
    }

    //settings////////////////////////////////////////////////////////////////////////////

    public boolean isLogEnabled(){
        return values.logEnabled;
    }

    public int getMemoryCacheSize(){
        return values.memoryCacheSize;
    }

    //handler////////////////////////////////////////////////////////////////////////////

    public ImageResourceHandler getImageResourceHandler(){
        return values.imageResourceHandler;
    }

    //factory////////////////////////////////////////////////////////////////////////////

    public TaskFactory getTaskFactory(){
        return values.taskFactory;
    }

    public NodeFactory getNodeFactory(){
        return values.nodeFactory;
    }

    public NodeTaskFactory getNodeTaskFactory(){
        return values.nodeTaskFactory;
    }

    private static class Values{

        //settings////////////////////////////////////////////////////////////////////////////

        private boolean logEnabled = true;
        private int memoryCacheSize = 0;

        //handler////////////////////////////////////////////////////////////////////////////

        private ImageResourceHandler imageResourceHandler = new DefaultImageResourceHandler();

        //factory////////////////////////////////////////////////////////////////////////////

        private final TaskFactory taskFactory = new TaskFactoryImpl();
        private final NodeFactory nodeFactory = new NodeFactoryImpl();
        private final NodeTaskFactory nodeTaskFactory = new NodeTaskFactoryImpl();

    }

    public static class Builder{

        private Values values;

        public Builder(){
            values = new Values();
        }

        public EngineSettings build(){
            return new EngineSettings(values);
        }

        //settings////////////////////////////////////////////////////////////////////////////

        /**
         * set the memory cache size by percent of app's MemoryClass
         * @param context context
         * @param percent percent of app's MemoryClass (0f-0.5f)
         */
        public Builder setMemoryCachePercent(Context context, float percent){
            if (context == null){
                throw new RuntimeException("[EngineSettings]setMemoryCachePercent:　context is null!");
            }
            //控制上下限
            if (percent < 0){
                percent = 0;
            }else if (percent > 0.5f){
                percent = 0.5f;
            }
            //应用可用内存级别
            final int memoryClass = DeviceUtils.getMemoryClass(context);
            //计算缓存大小
            values.memoryCacheSize = (int) (1024 * 1024 * memoryClass * percent);
            return this;
        }

        //handler////////////////////////////////////////////////////////////////////////////

        //factory////////////////////////////////////////////////////////////////////////////

        public Builder setCustomTaskFactory(TaskFactory customTaskFactory){
            values.taskFactory.setCustomTaskFactory(customTaskFactory);
            return this;
        }

    }

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
        values.taskFactory.init(manager);
        values.nodeFactory.init(manager);
        values.nodeTaskFactory.init(manager);
    }

}
