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

package sviolet.turquoise.model.queue.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>目的性阻塞消息池</p>
 *
 * <p>使用{@link PurposefulBlockingMessagePool#register(Object)}方法注册指定ID, 开始异步操作, 使用
 * {@link PurposefulBlockingMessagePool#wait(Object, long)} 方法等待目标对象返回, 此时线程阻塞. 当
 * 异步操作中, 通过{@link PurposefulBlockingMessagePool#restock(Object, Object)} 方法塞入目标对象后,
 * 原线程继续执行, 返回目标对象.</p>
 *
 * <p><pre>{@code
 *      //注册
 *      queue.register(id);
 *      //异步操作
 *      threadPool.execute(new Runnable(){
 *          public void run(){
 *              //......
 *              queue.restock(id, item);//返回对象
 *          }
 *      });
 *      //阻塞线程, 等待对象返回
 *      Item item = queue.wait(id, 60000);
 *      //对象处理
 *      if(item != null){
 *          //返回结果处理
 *      }else{
 *          //超时
 *      }
 * }</pre></p>
 *
 * <p>Created by S.Violet on 2016/3/23.</p>
 */
public class PurposefulBlockingMessagePool <T> {

    private final ReentrantLock lock = new ReentrantLock();//锁
    private final Map<Object, Condition> conditionPool = new HashMap<>();//信号池
    private final Map<Object, T> itemPool = new HashMap<>();//对象池

    /**
     * 注册指定ID, 表明需要目标对象, 注册后该消息池接受该ID目标对象的塞入(restock)
     * @param id 指定的ID
     */
    public void register(Object id){
        Condition condition = lock.newCondition();
        try{
            lock.lock();
            conditionPool.put(id, condition);
        }finally {
            lock.unlock();
        }
    }

    /**
     * 阻塞等待并返回指定ID的目标对象, 必须先调用{@link PurposefulBlockingMessagePool#register(Object)}注册等待的ID.
     * @param id 指定的ID
     * @param timeout 超时时间
     * @return 指定ID的目标对象(可能为空)
     */
    public T wait(Object id, long timeout){
        final long startMillis = System.currentTimeMillis();
        try{
            lock.lock();
            final Condition condition = conditionPool.get(id);
            if (condition == null){
                throw new RuntimeException("[PurposefulBlockingMessagePool]can't wait() before register()");
            }
            T item;
            while ((item = itemPool.remove(id)) == null) {
                final long remainTimeout = timeout - (System.currentTimeMillis() - startMillis);
                if (remainTimeout <= 0){
                    break;
                }
                try {
                    if (!condition.await(remainTimeout, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            itemPool.remove(id);
            conditionPool.remove(id);
            return item;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 向消息池塞入指定ID的目标对象, 若该ID未注册({@link PurposefulBlockingMessagePool#register(Object)}), 或等待已超时, 则塞入无效,
     * 并返回false.
     * @param id 指定ID
     * @param item 目标对象
     * @return true:塞入成功 false:塞入失败
     */
    public boolean restock(Object id, T item){
        try{
            lock.lock();
            Condition condition = conditionPool.remove(id);
            if (condition != null){
                itemPool.put(id, item);
                condition.signalAll();
                return true;
            }
        }finally {
            lock.unlock();
        }
        return false;
    }

    public int getRegisterCount(){
        try{
            lock.lock();
            return conditionPool.size();
        }finally {
            lock.unlock();
        }
    }

    public int getItemCount(){
        try{
            lock.lock();
            return itemPool.size();
        }finally {
            lock.unlock();
        }
    }

}