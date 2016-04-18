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

package sviolet.turquoise.x.imageloader.node;

import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * Created by S.Violet on 2016/2/17.
 */
public class RequestQueueImpl implements RequestQueue {

    private int size;
    private int position = 0;

    private Task[] tasks;

    private final ReentrantLock lock = new ReentrantLock();
    private TLogger logger;

    public RequestQueueImpl(int size, TLogger logger){
        setSize(size);
        this.logger = logger;
    }

    @Override
    public Task put(Task task) {
        logger.d("[RequestQueueImpl]put: put task, task:" + task.getTaskInfo());
        Task oldTask;
        try{
            lock.lock();
            oldTask = tasks[position];
            tasks[position] = task;
            positionIncrease();
        }finally {
            lock.unlock();
        }
        if (oldTask != null){
            logger.d("[RequestQueueImpl]put: drop task, task:" + task.getTaskInfo());
        }
        return oldTask;
    }

    @Override
    public Task get() {
        Task task;
        try{
            lock.lock();
            positionDecrease();
            task = tasks[position];
            tasks[position] = null;
        }finally {
            lock.unlock();
        }
        if (task != null) {
            logger.d("[RequestQueueImpl]get: get task, task:" + task.getTaskInfo());
        }else{
            logger.d("[RequestQueueImpl]get: no task");
        }
        return task;
    }

    public void clear(){
        try{
            lock.lock();
            for (int i = 0 ; i < tasks.length ; i++){
                tasks[i] = null;
            }
            position = 0;
        }finally {
            lock.unlock();
        }
    }

    public void setSize(int size){
        if (size < 1){
            throw new RuntimeException("[RequestQueueImpl]queue size must >= 1");
        }
        if (this.size == size){
            return;
        }
        Task[] newTasks = new Task[size];
        try{
            lock.lock();
            if(this.tasks != null){
                for (int i = size - 1 ; i >= 0 ; i--){
                    Task task = get();
                    if (task == null){
                        break;
                    }
                    newTasks[i] = task;
                }
            }
            this.tasks = newTasks;
            this.size = size;
            this.position = 0;
        }finally {
            lock.unlock();
        }
    }

    private void positionDecrease(){
        position = position < 1 ? size - 1 : position - 1;
    }

    private void positionIncrease(){
        position = position > size - 2 ? 0 : position + 1;
    }
}
