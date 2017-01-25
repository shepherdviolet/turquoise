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

package sviolet.turquoise.ui.util.motion;

import java.util.concurrent.atomic.AtomicLong;

import sviolet.turquoise.util.common.DateTimeUtils;

/**
 * <p>多重点击过滤器, 用于避免双击或多击</p>
 *
 * <pre>{@code
 *      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 *          //tryHandle返回false时, 表示本次点击为多击, 跳过后续的处理流程即可
 *          if (!multiClickFilter.tryHandle()){
 *              return;
 *          }
 *          //处理点击事件
 *      }
 * }</pre>
 *
 * Created by S.Violet on 2017/1/25.
 */
public class MultiClickFilter {

    private long interval;

    private AtomicLong lastTime = new AtomicLong(0);

    public MultiClickFilter() {
        this(500);
    }

    /**
     * @param interval 有效点击的最小间隔时间ms, >0
     */
    public MultiClickFilter(long interval) {
        if (interval <= 0){
            throw new IllegalArgumentException("[MultiClickFilter]interval must > 0");
        }
        this.interval = interval;
    }

    /**
     * @return true:有效点击 false:无效点击(多重点击, 需要跳过处理)
     */
    public boolean tryHandle(){
        long time = DateTimeUtils.getCurrentTimeMillis();
        long lastTime = this.lastTime.get();
        if (time - lastTime < interval){
            return false;
        }
        return this.lastTime.compareAndSet(lastTime, time);
    }

}
