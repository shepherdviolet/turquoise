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

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.common.statics.SpecialResourceId;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.node.NodeController;

/**
 * Load Task<br/>
 * loading image/background to View<br/>
 *
 * Created by S.Violet on 2016/2/19.
 */
public abstract class LoadTask extends AbsTask {

    private WeakReference<View> view;

    private final ReentrantLock viewLock = new ReentrantLock();

    public LoadTask(String url, Params params, View view){
        super(url, params);
        this.view = new WeakReference<>(view);
    }

    @Override
    public void initialize(NodeController controller) {
        super.initialize(controller);
        bindView(getView());
    }

    @Override
    public void load() {
        final View view = getView();
        if (view == null){
            onDestroy();
            return;
        }
        super.load();
    }

    @Override
    public void reload() {
        super.reload();
    }

    /***********************************************************
     * protected
     */

    protected void bindView(final View view){
        if (view == null){
            onDestroy();
            return;
        }
        try{
            viewLock.lock();
            //get Task from View Tag
            Object tag = view.getTag(SpecialResourceId.ViewTag.TILoaderTask);
            //destroy obsolete Task
            if (tag != null && tag instanceof Task) {
                ((Task) tag).onDestroy();
            }
            //bind Task on View
            view.setTag(SpecialResourceId.ViewTag.TILoaderTask, this);
        }finally {
            viewLock.unlock();
        }
    }

    /***********************************************************
     * Getter
     */

    protected View getView(){
        if (view != null){
            return view.get();
        }
        return null;
    }

    @Override
    public Type getType() {
        return Type.LOAD;
    }
}
