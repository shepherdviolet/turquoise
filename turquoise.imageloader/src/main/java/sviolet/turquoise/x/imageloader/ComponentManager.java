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

package sviolet.turquoise.x.imageloader;

import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.engine.CacheEngine;
import sviolet.turquoise.x.imageloader.engine.DiskEngine;
import sviolet.turquoise.x.imageloader.engine.NetEngine;
import sviolet.turquoise.x.imageloader.entity.EngineSettings;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.node.NodeFactory;
import sviolet.turquoise.x.imageloader.node.NodeFactoryImpl;
import sviolet.turquoise.x.imageloader.node.NodeManager;
import sviolet.turquoise.x.imageloader.node.NodeTaskFactory;
import sviolet.turquoise.x.imageloader.node.NodeTaskFactoryImpl;
import sviolet.turquoise.x.imageloader.task.TaskFactory;
import sviolet.turquoise.x.imageloader.task.TaskFactoryImpl;

/**
 * Created by S.Violet on 2016/2/18.
 */
public class ComponentManager {

    /**********************************************************************************************
     * Component Manager Instance
     **********************************************************************************************/

    private static final ComponentManager instance = new ComponentManager();

    private ComponentManager(){

    }

    static ComponentManager getInstance(){
        return instance;
    }

    /**********************************************************************************************
     * Components
     **********************************************************************************************/

    private final NodeManager nodeManager = new NodeManager(ComponentManager.getInstance());
    private final CacheEngine cacheEngine = new CacheEngine(ComponentManager.getInstance());
    private final DiskEngine diskEngine = new DiskEngine(ComponentManager.getInstance());
    private final NetEngine netEngine = new NetEngine(ComponentManager.getInstance());
    private final TaskFactory taskFactory = new TaskFactoryImpl(ComponentManager.getInstance());
    private final NodeFactory nodeFactory = new NodeFactoryImpl(ComponentManager.getInstance());
    private final NodeTaskFactory nodeTaskFactory = new NodeTaskFactoryImpl(ComponentManager.getInstance());

    /**********************************************************************************************
     * Settings
     **********************************************************************************************/

    private EngineSettings engineSettings;

    /**********************************************************************************************
     * Defaults
     **********************************************************************************************/

    private final Params defaultParams = new Params.Builder().build();

    /**********************************************************************************************
     * Extras
     **********************************************************************************************/

    private final TLogger logger = TLogger.get(TILoader.class);

    /**********************************************************************************************
     * Components Operations
     **********************************************************************************************/

    public NodeManager getNodeManager(){
        return nodeManager;
    }

    public CacheEngine getCacheEngine() {
        return cacheEngine;
    }

    public DiskEngine getDiskEngine() {
        return diskEngine;
    }

    public NetEngine getNetEngine() {
        return netEngine;
    }

    public TaskFactory getTaskFactory() {
        return taskFactory;
    }

    public NodeFactory getNodeFactory() {
        return nodeFactory;
    }

    public NodeTaskFactory getNodeTaskFactory(){
        return nodeTaskFactory;
    }

    /**********************************************************************************************
     * Settings Operations
     **********************************************************************************************/

    public boolean settingEngine(EngineSettings settings){
        return true;
    }

    /**********************************************************************************************
     * Defaults
     **********************************************************************************************/

    public Params getDefaultParams(){
        return defaultParams;
    }

    /**********************************************************************************************
     * public
     **********************************************************************************************/

    public TLogger getLogger(){
        return logger;
    }

}
