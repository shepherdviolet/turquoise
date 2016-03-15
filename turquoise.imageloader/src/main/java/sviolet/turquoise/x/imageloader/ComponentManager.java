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

import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.server.CacheServer;
import sviolet.turquoise.x.imageloader.server.DiskEngine;
import sviolet.turquoise.x.imageloader.server.NetEngine;
import sviolet.turquoise.x.imageloader.entity.EngineSettings;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.node.NodeManager;

/**
 * <p>manage all component</p>
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
     * Components
     **********************************************************************************************/

    private final CacheServer cacheServer = new CacheServer();
    private final NodeManager nodeManager = new NodeManager();
    private final DiskEngine diskEngine = new DiskEngine();
    private final NetEngine netEngine = new NetEngine();

    /**********************************************************************************************
     * Settings
     **********************************************************************************************/

    private EngineSettings engineSettings;

    /**********************************************************************************************
     * Extras
     **********************************************************************************************/

    private final Params defaultParams = new Params.Builder().build();
    private final TLogger logger = TLogger.get(TILoader.class, TILoader.TAG);
    private WeakReference<Context> contextImage;

    private boolean componentsInitialized = false;
    private final ReentrantLock componentsInitializeLock = new ReentrantLock();

    /**********************************************************************************************
     * Components Operations
     **********************************************************************************************/

    public CacheServer getCacheServer() {
        return cacheServer;
    }

    public NodeManager getNodeManager(){
        return nodeManager;
    }

    public DiskEngine getDiskEngine() {
        return diskEngine;
    }

    public NetEngine getNetEngine() {
        return netEngine;
    }

    /**********************************************************************************************
     * Settings Operations
     **********************************************************************************************/

    public boolean settingEngine(EngineSettings settings){
        boolean result = false;
        if (!componentsInitialized){
            try{
                componentsInitializeLock.lock();
                if (!componentsInitialized){
                    this.engineSettings = settings;
                    result = true;
                }else{
                    getLogger().e("[TILoader]setting failed, you should invoke TILoader.setting() before TILoader used (load image)");
                }
            }finally {
                componentsInitializeLock.unlock();
            }
        }else{
            getLogger().e("[TILoader]setting failed, you should invoke TILoader.setting() before TILoader used (load image)");
        }
        return result;
    }

    /**
     * component must be initialized before get EngineSettings
     * @return get EngineSettings
     */
    public EngineSettings getEngineSettings(){
        return engineSettings;
    }

    /**********************************************************************************************
     * public
     **********************************************************************************************/

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

    private void onInitialize(){
        if (engineSettings == null) {
            engineSettings = new EngineSettings.Builder().build();
        }
        engineSettings.init(ComponentManager.getInstance());
        cacheServer.init(ComponentManager.getInstance());
        nodeManager.init(ComponentManager.getInstance());
        diskEngine.init(ComponentManager.getInstance());
        netEngine.init(ComponentManager.getInstance());
    }

    public Params getDefaultParams(){
        return defaultParams;
    }

    /**
     * @return get logger
     */
    public TLogger getLogger(){
        if (getEngineSettings().isLogEnabled()) {
            return logger;
        }else{
            return TLogger.getDisabledLogger();
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
