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

package sviolet.turquoise.x.imageloader.task;

import android.view.View;
import android.widget.ImageView;

import sviolet.turquoise.x.imageloader.ComponentManager;
import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.entity.Params;

/**
 *
 * Created by S.Violet on 2016/2/23.
 */
public class TaskFactoryImpl implements TaskFactory {

    private TaskFactory customTaskFactory;

    @Override
    public Task newLoadTask(String url, Params params, View view, Params defParams) {
        if (url == null){
            throw new RuntimeException("[TILoader]can't load image without url!");
        }
        if (view == null){
            throw new RuntimeException("[TILoader]can't load image into a null View!");
        }
        if (params == null){
            params = defParams;
        }
        Task task = null;
        if (customTaskFactory != null){
            task = customTaskFactory.newLoadTask(url, params, view, defParams);
        }
        if (task == null){
            task = newLoadTaskInner(url, params, view, defParams);
        }
        if (task == null){
            throw new RuntimeException("[TILoader]unsupported view:<" + view.getClass().getName() + ">, can't load image into it, 0x00");
        }
        return task;
    }

    protected Task newLoadTaskInner(String url, Params params, View view, Params defParams){
        if (view instanceof ImageView){
            return new ImageViewLoadTask(url, params, view);
        }
        return null;
    }

    @Override
    public Task newLoadBackgroundTask(String url, Params params, View view, Params defParams) {
        if (url == null){
            throw new RuntimeException("[TILoader]can't load image without url!");
        }
        if (view == null){
            throw new RuntimeException("[TILoader]can't load image into a null View!");
        }
        if (params == null){
            params = defParams;
        }
        Task task = null;
        if (customTaskFactory != null){
            task = customTaskFactory.newLoadBackgroundTask(url, params, view, defParams);
        }
        if (task == null){
            task = newLoadBackgroundTaskInner(url, params, view, defParams);
        }
        if (task == null){
            throw new RuntimeException("[TILoader]unsupported view:<" + view.getClass().getName() + ">, can't load background image into it, 0x01");
        }
        return task;
    }

    protected Task newLoadBackgroundTaskInner(String url, Params params, View view, Params defParams){
        return new LoadBackgroundTask(url, params, view);
    }

    @Override
    public Task newExtractTask(String url, Params params, OnLoadedListener listener, Params defParams) {
        if (url == null){
            throw new RuntimeException("[TILoader]can't load image without url!");
        }
        if (listener == null){
            throw new RuntimeException("[TILoader]can't load image into a null View!");
        }
        if (params == null){
            params = defParams;
        }
        Task task = null;
        if (customTaskFactory != null){
            task = customTaskFactory.newExtractTask(url, params, listener, defParams);
        }
        if (task == null){
            task = newExtractTaskInner(url, params, listener, defParams);
        }
        if (task == null){
            throw new RuntimeException("[TILoader]unsupported listener:<" + listener.getClass().getName() + ">, can't extract image, 0x02");
        }
        return task;
    }

    protected Task newExtractTaskInner(String url, Params params, OnLoadedListener listener, Params defParams){
        return new ExtractTask(url, params, listener);
    }

    public void setCustomTaskFactory(TaskFactory factory) {
        this.customTaskFactory = factory;
    }

}
