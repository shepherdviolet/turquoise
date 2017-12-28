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

package sviolet.turquoise.x.imageloader.server;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * record resource keys which loaded succeed
 *
 * Created by S.Violet on 2016/12/16.
 */
class History {

    private int capacity;
    private int position = 0;

    private Set<String> historySet;
    private String[] historyQueue;

    private final ReentrantLock lock = new ReentrantLock();

    History(int capacity) {
        this.capacity = capacity;
        this.historySet = new HashSet<>(capacity);
        this.historyQueue = new String[capacity];
    }

    void put(String value){
        try {
            lock.lock();
            String oldValue = historyQueue[position];
            if (oldValue != null){
                historySet.remove(oldValue);
            }
            historyQueue[position] = value;
            historySet.add(value);
            positionIncrease();
        } finally {
            lock.unlock();
        }
    }

    boolean contains(String value){
        try {
            lock.lock();
            return historySet.contains(value);
        } finally {
            lock.unlock();
        }
    }

//    private void positionDecrease(){
//        position = position < 1 ? capacity - 1 : position - 1;
//    }

    private void positionIncrease(){
        position = position > capacity - 2 ? 0 : position + 1;
    }

}
