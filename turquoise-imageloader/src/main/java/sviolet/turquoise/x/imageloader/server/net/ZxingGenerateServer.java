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

import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.entity.IndispensableState;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.Server;

/**
 * Generate qr-code image by url value
 *
 * @author S.Violet
 */
public class ZxingGenerateServer implements ComponentManager.Component, Server {

    private ComponentManager manager;
    private NetworkEngine networkEngine;

    @Override
    public void init(ComponentManager manager) {
        this.manager = manager;
        this.networkEngine = (NetworkEngine) manager.getNetworkEngine();
    }

    @Override
    public Type getServerType() {
        return Type.NETWORK_ZXING_GEN;
    }

    private ComponentManager getComponentManager(){
        return manager;
    }

    private NetworkEngine getNetworkEngine() {
        return networkEngine;
    }

    //generateQrCode//////////////////////////////////////////////////////////////////////////////////////

    /**
     * generate qr-code by url value
     */
    void generateQrCode(Task task, IndispensableState indispensableState) {
        //handle data
        getNetworkEngine().handleImageData(task, DecodeHandler.DecodeType.QR_CODE, task.getUrl());
    }

}
