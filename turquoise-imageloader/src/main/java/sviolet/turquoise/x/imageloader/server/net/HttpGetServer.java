/*
 * Copyright (C) 2015-2018 S.Violet
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

package sviolet.turquoise.x.imageloader.server.net;

import sviolet.turquoise.x.common.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.entity.IndispensableState;
import sviolet.turquoise.x.imageloader.entity.LowNetworkSpeedStrategy;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.Server;
import sviolet.turquoise.x.imageloader.server.disk.DiskCacheServer;

/**
 * Get image from http
 *
 * @author S.Violet
 */
public class HttpGetServer implements ComponentManager.Component, Server {

    private ComponentManager manager;
    private NetworkEngine networkEngine;

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
        this.networkEngine = (NetworkEngine) manager.getNetworkEngine();
    }

    @Override
    public Type getServerType() {
        return Type.NETWORK_HTTP_GET;
    }

    private ComponentManager getComponentManager(){
        return manager;
    }

    private NetworkEngine getNetworkEngine() {
        return networkEngine;
    }

    //load//////////////////////////////////////////////////////////////////////////////////////

    void load(Task task, final IndispensableState indispensableState) {

        final LowNetworkSpeedStrategy.Configure lowNetworkSpeedConfig = getComponentManager().getServerSettings().getLowNetworkSpeedStrategy()
                .getConfigure(getComponentManager().getApplicationContextImage(), indispensableState);
        if (getComponentManager().getLogger().checkEnable(TLogger.DEBUG)) {
            getComponentManager().getLogger().d("[NetworkEngine]LowNetworkSpeedStrategy:" + lowNetworkSpeedConfig.getType() + ", task:" + task);
        }

        //timeout, indispensable task has double timeout
        final long connectTimeout = indispensableState.isIndispensable() ?
                getNetworkEngine().getNetworkConnectTimeout(task) << 1 :
                getNetworkEngine().getNetworkConnectTimeout(task);
        final long readTimeout = indispensableState.isIndispensable() ?
                getNetworkEngine().getNetworkReadTimeout(task) << 1 :
                getNetworkEngine().getNetworkReadTimeout(task);

        //network loading and disk write
        DiskCacheServer.WriteResult result = getComponentManager().getDiskCacheServer().startWrite(task, new DiskCacheServer.WriteProcess() {
            @Override
            public DiskCacheServer.WriteResult onWrite(Task task, DiskCacheServer.WriterProvider writerProvider) {
                NetworkLoadHandler.HandleResult networkResult = getNetworkEngine().getNetworkLoadHandler(task).onHandle(
                        getComponentManager().getApplicationContextImage(),
                        getComponentManager().getContextImage(),
                        writerProvider,
                        task.getTaskInfo(),
                        indispensableState,
                        lowNetworkSpeedConfig,
                        connectTimeout,
                        readTimeout,
                        getComponentManager().getServerSettings().getImageDataLengthLimit(),
                        getComponentManager().getServerSettings().getExceptionHandler(),
                        getComponentManager().getLogger());
                switch (networkResult) {
                    case SUCCEED:
                        return DiskCacheServer.WriteResult.succeedResult();
                    case FAILED:
                        return DiskCacheServer.WriteResult.failedResult();
                    case CANCELED:
                    default:
                        return DiskCacheServer.WriteResult.canceledResult();
                }
            }
        });
        switch (result.getType()) {
            case SUCCEED:
                getNetworkEngine().handleImageData(task, DecodeHandler.DecodeType.IMAGE_FILE, result.getTargetFile());
                break;
            case FAILED:
                getNetworkEngine().handleFailed(task);
                break;
            case CANCELED:
            default:
                getNetworkEngine().handleCanceled(task);
                break;
        }
    }

}
