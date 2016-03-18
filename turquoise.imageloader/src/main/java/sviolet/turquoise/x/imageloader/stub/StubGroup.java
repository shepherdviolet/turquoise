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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.x.imageloader.entity.ImageResource;

/**
 *
 * Created by S.Violet on 2016/3/3.
 */
public class StubGroup {

    private Set<Stub> stubSet = new HashSet<>();

    private final ReentrantLock setLock = new ReentrantLock();

    public StubGroup(){

    }

    /**
     * @param stub add the stub into group, non-repetitive(Set)
     */
    public void add(Stub stub){
        if (stub == null)
            return;
        try{
            setLock.lock();
            stubSet.add(stub);
        }finally {
            setLock.unlock();
        }
    }

    private boolean hasStub(){
        try{
            setLock.lock();
            return stubSet.size() > 0;
        }finally {
            setLock.unlock();
        }
    }

    /**
     * callback all stubs to onLoadSucceed, this method can only invoke once, all stubs will be removed from this Group after callback
     * @param resource loaded Image, may be null
     */
    public void onLoadSucceed(ImageResource<?> resource) {
        List<Stub> stubs = new ArrayList<>();
        while(hasStub()) {
            try {
                setLock.lock();
                for (Stub stub : stubSet) {
                    stubs.add(stub);
                }
                stubSet.clear();
            } finally {
                setLock.unlock();
            }
            for (Stub stub : stubs) {
                stub.onLoadSucceed(resource);
            }
            stubs.clear();
        }
    }

    /**
     * callback all stubs to onLoadFailed, this method can only invoke once, all stubs will be removed from this Group after callback
     */
    public void onLoadFailed() {
        List<Stub> stubs = new ArrayList<>();
        while(hasStub()) {
            try {
                setLock.lock();
                for (Stub stub : stubSet) {
                    stubs.add(stub);
                }
                stubSet.clear();
            } finally {
                setLock.unlock();
            }
            for (Stub stub : stubs) {
                stub.onLoadFailed();
            }
            stubs.clear();
        }
    }

    /**
     * callback all stubs to onLoadCanceled, this method can only invoke once, all stubs will be removed from this Group after callback
     */
    public void onLoadCanceled() {
        List<Stub> stubs = new ArrayList<>();
        while(hasStub()) {
            try {
                setLock.lock();
                for (Stub stub : stubSet) {
                    stubs.add(stub);
                }
                stubSet.clear();
            } finally {
                setLock.unlock();
            }
            for (Stub stub : stubs) {
                stub.onLoadCanceled();
            }
            stubs.clear();
        }
    }
}
