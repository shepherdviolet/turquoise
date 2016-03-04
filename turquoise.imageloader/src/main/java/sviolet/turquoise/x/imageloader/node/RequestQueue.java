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

/**
 * Created by S.Violet on 2016/2/17.
 */
public interface RequestQueue {

    /**
     * put a NodeTask into the queue
     * @param task new NodeTask
     * @return return the obsolete task, you should handle it
     */
    NodeTask put(NodeTask task);

    /**
     * @return get a NodeTask from the queue
     */
    NodeTask get();

    /**
     * @param size set the size of waiting queue (>0)
     */
    void setSize(int size);

}
