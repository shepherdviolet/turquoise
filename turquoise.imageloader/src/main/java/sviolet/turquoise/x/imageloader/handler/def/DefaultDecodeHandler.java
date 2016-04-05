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

package sviolet.turquoise.x.imageloader.handler.def;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;

import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 *
 * Created by S.Violet on 2016/4/1.
 */
public class DefaultDecodeHandler implements DecodeHandler {
    @Override
    public ImageResource decode(Context applicationContext, Context context, Task.Info taskInfo, byte[] data, TLogger logger) {
        return null;
    }

    @Override
    public ImageResource decode(Context applicationContext, Context context, Task.Info taskInfo, File file, TLogger logger) {
        return null;
    }
}
