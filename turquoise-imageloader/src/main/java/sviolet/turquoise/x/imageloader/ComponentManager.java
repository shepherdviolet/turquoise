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

package sviolet.turquoise.x.imageloader;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.ServerSettings;
import sviolet.turquoise.x.imageloader.node.NodeFactory;
import sviolet.turquoise.x.imageloader.node.NodeFactoryImpl;
import sviolet.turquoise.x.imageloader.node.NodeManager;
import sviolet.turquoise.x.imageloader.server.disk.DiskCacheServer;
import sviolet.turquoise.x.imageloader.server.disk.DiskEngine;
import sviolet.turquoise.x.imageloader.server.disk.DiskLoadServer;
import sviolet.turquoise.x.imageloader.server.Engine;
import sviolet.turquoise.x.imageloader.server.mem.MemoryCacheServer;
import sviolet.turquoise.x.imageloader.server.mem.MemoryEngine;
import sviolet.turquoise.x.imageloader.server.net.NetworkEngine;

/**
 * <p>Manage all components</p>
 *
 * <p>Internal class, not for user interface</p>
 *
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
     * Nodes
     **********************************************************************************************/

    private final NodeManager nodeManager = new NodeManager(this);
    private final NodeFactory nodeFactory = new NodeFactoryImpl(this);

    public NodeManager getNodeManager(){
        return nodeManager;
    }

    public NodeFactory getNodeFactory(){
        return nodeFactory;
    }

    /**********************************************************************************************
     * Components
     **********************************************************************************************/

    private MemoryCacheServer memoryCacheServer;
    private DiskCacheServer diskCacheServer;
    private DiskLoadServer diskLoadServer;
    private Engine memoryEngine;
    private Engine diskEngine;
    private Engine netEngine;

    private ServerSettings serverSettings;

    /**********************************************************************************************
     * Extras
     **********************************************************************************************/

    private final TLogger logger = TLogger.get(TILoader.class);
    private WeakReference<Context> contextImage;
    private WeakReference<Context> applicationContextImage;

    private boolean componentsInitialized = false;
    private final ReentrantLock componentsInitializeLock = new ReentrantLock();

    /**********************************************************************************************
     * Components Operations
     **********************************************************************************************/

    public MemoryCacheServer getMemoryCacheServer() {
        return memoryCacheServer;
    }

    public DiskCacheServer getDiskCacheServer() {
        return diskCacheServer;
    }

    public DiskLoadServer getDiskLoadServer(){
        return diskLoadServer;
    }

    public Engine getMemoryEngine(){
        return memoryEngine;
    }

    public Engine getDiskEngine() {
        return diskEngine;
    }

    public Engine getNetEngine() {
        return netEngine;
    }

    /**
     * component must be initialized before get ServerSettings
     * @return get ServerSettings
     */
    public ServerSettings getServerSettings(){
        return serverSettings;
    }

    /**********************************************************************************************
     * Settings Operations
     **********************************************************************************************/

    /**
     * [Initialize TILoader]this method will initialize TILoader
     * @param settings ServerSettings
     * @return true if setting invalid
     */
    public boolean settingServer(ServerSettings settings){
        boolean result = false;
        if (!componentsInitialized){
            try{
                componentsInitializeLock.lock();
                if (!componentsInitialized){
                    this.serverSettings = settings;
                    result = true;
                }else{
                    getLogger().e("[TILoader]setting failed, you should invoke TILoader.setting() before TILoader initialized (invoke TILoader.setting() or TILoader.node().load() will initialize TILoader)");
                }
            }finally {
                componentsInitializeLock.unlock();
            }
        }else{
            getLogger().e("[TILoader]setting failed, you should invoke TILoader.setting() before TILoader initialized (invoke TILoader.setting() or TILoader.node().load() will initialize TILoader)");
        }
        //initialize
        if (result){
            waitingForInitialized();
        }
        return result;
    }

    /**********************************************************************************************
     * public
     **********************************************************************************************/

    /**
     * check and initialize
     */
    public void waitingForInitialized(){
        if (componentsInitialized){
            return;
        }
        try {
            componentsInitializeLock.lock();
            if (!componentsInitialized){
                onInitialize();
                componentsInitialized = true;
            }
        } finally {
            componentsInitializeLock.unlock();
        }
    }

    /**
     * initialize process
     */
    private void onInitialize(){
        //default serverSettings
        if (serverSettings == null) {
            serverSettings = new ServerSettings.Builder().build();
        }

        //instance components
        memoryCacheServer = new MemoryCacheServer();
        diskCacheServer = new DiskCacheServer();
        diskLoadServer = new DiskLoadServer();
        memoryEngine = new MemoryEngine();
        diskEngine = new DiskEngine();
        netEngine = new NetworkEngine();

        //init components
        serverSettings.init(ComponentManager.getInstance());
        memoryCacheServer.init(ComponentManager.getInstance());
        diskCacheServer.init(ComponentManager.getInstance());
        diskLoadServer.init(ComponentManager.getInstance());
        memoryEngine.init(ComponentManager.getInstance());
        diskEngine.init(ComponentManager.getInstance());
        netEngine.init(ComponentManager.getInstance());
        getLogger().i("[ComponentManager]TILoader initialized");
    }

    /**
     * @return get logger
     */
    public TLogger getLogger(){
        if (getServerSettings().isLogEnabled()) {
            return logger;
        }else{
            return TLogger.get(null);
        }
    }

    /**
     * set a context's image, used to get System Params
     * @param context context
     */
    public void setContextImage(Context context){
        if (context != null){
            contextImage = new WeakReference<Context>(context);
        }
    }

    /**
     * context's image, used to get System Params
     * @return may be null, not reliable
     */
    public Context getContextImage(){
        if (contextImage != null){
            return contextImage.get();
        }
        return null;
    }

    /**
     * set a application context's image, used to get System Params
     * @param context context
     */
    public void setApplicationContextImage(Context context){
        if (context != null){
            applicationContextImage = new WeakReference<Context>(context);
        }
    }

    /**
     * application context's image, used to get System Params
     */
    public Context getApplicationContextImage(){
        if (applicationContextImage != null){
            return applicationContextImage.get();
        }
        return null;
    }

    /**
     * @return true if TILoader initialized
     */
    public boolean isInitialized(){
        try{
            componentsInitializeLock.lock();
            return componentsInitialized;
        }finally {
            componentsInitializeLock.unlock();
        }
    }

    /**********************************************************************************************
     * inner class
     **********************************************************************************************/

    public interface Component{

        /**
         * init component, time consuming operation is prohibited
         */
        void init(ComponentManager manager);

    }

}
