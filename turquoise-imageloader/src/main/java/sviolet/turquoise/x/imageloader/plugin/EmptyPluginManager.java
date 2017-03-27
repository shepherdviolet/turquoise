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

package sviolet.turquoise.x.imageloader.plugin;

import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.handler.ImageResourceHandler;
import sviolet.turquoise.x.imageloader.server.Engine;

/**
 * <p>Empty plugin interface</p>
 *
 * Created by S.Violet on 2016/5/4.
 */
public class EmptyPluginManager implements PluginInterface {

    @Override
    public Engine newEnhancedMemoryEngine() {
        return null;
    }

    @Override
    public Engine newEnhancedDiskEngine() {
        return null;
    }

    @Override
    public Engine newEnhancedNetEngine() {
        return null;
    }

    @Override
    public ImageResourceHandler newEnhancedImageResourceHandler() {
        return null;
    }

    @Override
    public DecodeHandler newEnhancedDecodeHandler() {
        return null;
    }
}
