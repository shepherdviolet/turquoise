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
public class TaskGroup {

    private Set<Task> taskSet = new HashSet<>();

    private final ReentrantLock setLock = new ReentrantLock();

    public TaskGroup(){

    }

    /**
     * @param task add the task into group, non-repetitive(Set)
     */
    public void add(Task task){
        if (task == null)
            return;
        try{
            setLock.lock();
            taskSet.add(task);
        }finally {
            setLock.unlock();
        }
    }

    private boolean hasTask(){
        try{
            setLock.lock();
            return taskSet.size() > 0;
        }finally {
            setLock.unlock();
        }
    }

    /**
     * callback all task to onLoadSucceed, this method can only invoke once, all tasks will be removed from this Group after callback
     * @param resource loaded Image, may be null
     */
    public void onLoadSucceed(ImageResource<?> resource) {
        List<Task> tasks = new ArrayList<>();
        while(hasTask()) {
            try {
                setLock.lock();
                for (Task task : taskSet) {
                    tasks.add(task);
                }
                taskSet.clear();
            } finally {
                setLock.unlock();
            }
            for (Task task : tasks) {
                task.onLoadSucceed(resource);
            }
            tasks.clear();
        }
    }

    /**
     * callback all task to onLoadFailed, this method can only invoke once, all tasks will be removed from this Group after callback
     */
    public void onLoadFailed() {
        List<Task> tasks = new ArrayList<>();
        while(hasTask()) {
            try {
                setLock.lock();
                for (Task task : taskSet) {
                    tasks.add(task);
                }
                taskSet.clear();
            } finally {
                setLock.unlock();
            }
            for (Task task : tasks) {
                task.onLoadFailed();
            }
            tasks.clear();
        }
    }

    /**
     * callback all task to onLoadCanceled, this method can only invoke once, all tasks will be removed from this Group after callback
     */
    public void onLoadCanceled() {
        List<Task> tasks = new ArrayList<>();
        while(hasTask()) {
            try {
                setLock.lock();
                for (Task task : taskSet) {
                    tasks.add(task);
                }
                taskSet.clear();
            } finally {
                setLock.unlock();
            }
            for (Task task : tasks) {
                task.onLoadCanceled();
            }
            tasks.clear();
        }
    }
}
