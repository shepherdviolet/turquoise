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

package sviolet.turquoise.x.imageloader.stub;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import sviolet.turquoise.x.imageloader.entity.ImageResource;

/**
 * <p>Group of stubs. All tasks for the same purpose will be bundled into a group.</p>
 *
 * Created by S.Violet on 2016/3/3.
 */
public class StubGroup {

    private Set<Stub> stubSet = Collections.newSetFromMap(new ConcurrentHashMap<Stub, Boolean>());

    public StubGroup(){

    }

    /**
     * @param stub add the stub into group, non-repetitive(Set)
     */
    public void add(Stub stub){
        if (stub == null)
            return;
        stubSet.add(stub);
    }

    private boolean hasStub(){
        return stubSet.size() > 0;
    }

    /**
     * callback all stubs to onLoadSucceed, this method can only invoke once, all stubs will be removed from this Group after callback
     * @param resource loaded Image, may be null
     */
    public void onLoadSucceed(ImageResource<?> resource) {
        for (Stub stub : stubSet) {
            stub.onLoadSucceed(resource);
        }
        stubSet.clear();
    }

    /**
     * callback all stubs to onLoadFailed, this method can only invoke once, all stubs will be removed from this Group after callback
     */
    public void onLoadFailed() {
        for (Stub stub : stubSet) {
            stub.onLoadFailed();
        }
        stubSet.clear();
    }

    /**
     * callback all stubs to onLoadCanceled, this method can only invoke once, all stubs will be removed from this Group after callback
     */
    public void onLoadCanceled() {
        for (Stub stub : stubSet) {
            stub.onLoadCanceled();
        }
        stubSet.clear();
    }

    /**
     * destroy all stubs
     */
    public void onDestroy() {
        for (Stub stub : stubSet) {
            stub.onDestroy();
        }
        stubSet.clear();
    }
}
