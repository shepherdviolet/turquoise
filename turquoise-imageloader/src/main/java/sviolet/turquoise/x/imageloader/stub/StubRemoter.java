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

package sviolet.turquoise.x.imageloader.stub;

import android.view.View;

import java.lang.ref.WeakReference;

import sviolet.turquoise.x.imageloader.TILoaderUtils;
import sviolet.turquoise.x.imageloader.entity.LoadProgress;

/**
 * <p>Stub Remoter</p>
 *
 * 0.get url of loading<br/>
 * 1.get state of loading<br/>
 * 2.get progress of loading<br/>
 * 3.relaunch canceled task<br/>
 *
 * Created by S.Violet on 2016/5/9.
 */
public class StubRemoter {

    public static final StubRemoter NULL_STUB_REMOTER = new StubRemoter(null);
    private WeakReference<Stub> stub;

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * Get stub remoter from view, uses:<br/>
     * 0.get url of loading<br/>
     * 1.get state of loading<br/>
     * 2.get progress of loading<br/>
     * 3.relaunch canceled task<br/>
     * @param view view
     * @return return NULL_STUB_REMOTER if failed
     */
    public static StubRemoter getFromView(View view){
        return TILoaderUtils.getStubRemoter(view);
    }

    StubRemoter(Stub stub){
        this.stub = new WeakReference<>(stub);
    }

    /**
     * @return get loading url, might be null
     */
    public String getUrl(){
        Stub stub = this.stub.get();
        if (stub != null){
            return stub.getUrl();
        }
        return null;
    }

    /**
     *  INITIAL = 0;<br/>
     *  LAUNCHING = 1;<br/>
     *  LOADING = 2;<br/>
     *  LOAD_SUCCEED = 3;<br/>
     *  LOAD_FAILED = 4;<br/>
     *  LOAD_CANCELED = 5;<br/>
     *  DESTROYED = 6;<br/>
     *  @see sviolet.turquoise.x.imageloader.stub.Stub.State
     *  @return loading state
     */
    public int getLoadState(){
        Stub stub = this.stub.get();
        if (stub != null){
            return stub.getState();
        }
        return Stub.State.DESTROYED;
    }

    /**
     * @return load progress info of stub, might be null
     */
    public LoadProgress.Info getLoadProgress(){
        Stub stub = this.stub.get();
        if (stub != null){
            return stub.getLoadProgress().getInfo();
        }
        return null;
    }

    /**
     * reload View which has been canceled, no effect if image is loading or loaded succeed
     * @return true:this view can be reload (load canceled), false:this view can't be reload (is loading or loaded succeed)
     */
    public boolean relaunch(){
        Stub stub = this.stub.get();
        if (stub != null){
            return stub.relaunch(false) == Stub.LaunchResult.SUCCEED;
        }
        return false;
    }

}
