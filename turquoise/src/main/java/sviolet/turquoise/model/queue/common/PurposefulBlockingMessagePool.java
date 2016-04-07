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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.model.common.LazySingleThreadPool;

/**
 * <p>目的性阻塞消息池</p>
 *
 * <p>*******************************************************************************************</p>
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
 * <p>*******************************************************************************************</p>
 *
 * <p>意外消息:若一个消息在塞入(restock)时, ID未在消息池注册, 则该消息被视为意外消息</p>
 *
 * <p>模式1:意外消息抛弃模式</p>
 *
 * <br>PurposefulBlockingMessagePool pool = PurposefulBlockingMessagePool();</br>
 * <p>用于提前知道ID的情况, 先注册ID, 然后阻塞等待异步操作塞入消息. 该模式下, 意外消息被消息池直接抛弃,
 * wait方法返回false.</p>
 *
 * <p>模式2:意外消息池模式</p>
 *
 * <br>PurposefulBlockingMessagePool pool = PurposefulBlockingMessagePool(long unexpectedItemValidityPeriod, MessageDropListener<I> messageDropListener);</br>
 * <p>用于无法提前知道ID的情况, 设置一个意外消息有效期, 接收到的意外消息将存入意外消息池, 有效期内可以
 * 从意外消息池获取该消息, 失效的消息将被清理任务清理(自动). 手动调用{@link PurposefulBlockingMessagePool#flush()}
 * 可立即启动清理任务.</p>
 *
 * <p>Created by S.Violet on 2016/3/23.</p>
 */
public class PurposefulBlockingMessagePool <K, I> {

    private static final int DEFAULT_LIMIT = 1000;

    private final ReentrantLock lock = new ReentrantLock();//锁
    private final Map<K, Condition> conditionPool = new HashMap<>();//信号池
    private final Map<K, I> itemPool = new HashMap<>();//消息池

    private Map<K, UnexpectedItem<I>> unexpectedItemPool = null;//意外消息池(存放未注册ID的消息)
    private long unexpectedItemValidityPeriod = 0;//意外消息有效期(ms)
    private ReentrantLock unexpectedItemLock = null;
    private LazySingleThreadPool unexpectedItemFlushThreadPool = null;//意外消息池清理线程
    private MessageDropListener<I> messageDropListener = null;//消息从意外消息池被丢弃回调

    private int registerLimit = DEFAULT_LIMIT;//注册等待数上限
    private int messageLimit = DEFAULT_LIMIT;//消息池内消息数上限

    /**
     * 直接丢弃意外消息(未注册ID的塞入消息)
     */
    public PurposefulBlockingMessagePool(){
        this(0, null);
    }

    /**
     * 意外消息(未注册ID的消息)塞入时, 存入意外消息池. 在意外消息过期前, 仍能被获取到, 在意外消息过期后,
     * 会被清理任务清理掉, 清理后将无法获得该消息.
     *
     * @param unexpectedItemValidityPeriod 意外消息有效期 ms >0生效
     */
    public PurposefulBlockingMessagePool(long unexpectedItemValidityPeriod){
        this(unexpectedItemValidityPeriod, null);
    }

    /**
     * 意外消息(未注册ID的消息)塞入时, 存入意外消息池. 在意外消息过期前, 仍能被获取到, 在意外消息过期后,
     * 会被清理任务清理掉, 清理后将无法获得该消息.
     *
     * @param unexpectedItemValidityPeriod 意外消息有效期 ms >0生效
     * @param messageDropListener 当消息从意外消息池被丢弃时回调该监听器
     */
    public PurposefulBlockingMessagePool(long unexpectedItemValidityPeriod, MessageDropListener<I> messageDropListener){
        this.unexpectedItemValidityPeriod = unexpectedItemValidityPeriod;
        setMessageDropListener(messageDropListener);
        if (this.unexpectedItemValidityPeriod > 0) {
            this.unexpectedItemLock = new ReentrantLock();
            this.unexpectedItemFlushThreadPool = new LazySingleThreadPool();
            this.unexpectedItemPool = new HashMap<>();
        }
    }

    /**
     * @param messageDropListener 当消息从意外消息池被丢弃时回调该监听器
     */
    public void setMessageDropListener(MessageDropListener<I> messageDropListener){
        this.messageDropListener = messageDropListener;
    }

    /**
     * @param registerLimit 设置注册等待数上限, 注册等待的消息ID超过限制将会抛出异常
     */
    public void setRegisterLimit(int registerLimit){
        this.registerLimit = registerLimit;
    }

    /**
     * @param messageLimit 意外消息数上限(不包括普通消息池), 超过上限将会抛弃塞入的新消息
     */
    public void setMessageLimit(int messageLimit){
        this.messageLimit = messageLimit;
    }

    /**
     * 注册并阻塞等待消息
     * @param id 指定的ID
     * @param timeout 超时时间
     * @return 指定ID的目标对象(可能为空)
     */
    public I registerAndWait(K id, long timeout) throws OutOfLimitException, TimeoutException{
        register(id);
        return wait(id, timeout);
    }

    /**
     * 注册指定ID, 表明需要目标对象, 注册后该消息池接受该ID目标对象的塞入(restock)
     * @param id 指定的ID
     */
    public void register(K id) throws OutOfLimitException{
        Condition condition = lock.newCondition();
        try{
            lock.lock();
            if (getRegisterCount() > registerLimit){
                throw new OutOfLimitException("[PurposefulBlockingMessagePool]register out of limit, drop this register : " + registerLimit);
            }
            conditionPool.put(id, condition);
        }finally {
            lock.unlock();
            flush();
        }
    }

    /**
     * 阻塞等待并返回指定ID的目标对象, 必须先调用{@link PurposefulBlockingMessagePool#register(Object)}注册等待的ID.
     * @param id 指定的ID
     * @param timeout 超时时间
     * @return 指定ID的目标对象(可能为空)
     */
    public I wait(K id, long timeout) throws TimeoutException{
        final long startMillis = System.currentTimeMillis();
        try{
            lock.lock();
            final Condition condition = conditionPool.get(id);
            if (condition == null){
                throw new RuntimeException("[PurposefulBlockingMessagePool]can't wait() before register()");
            }
            I item;
            while ((item = getItem(id)) == null) {
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
            getItem(id);
            conditionPool.remove(id);
            if (item == null){
                throw new TimeoutException("[PurposefulBlockingMessagePool]waiting for message timeout : " + timeout);
            }
            return item;
        } finally {
            lock.unlock();
            flush();
        }
    }

    /**
     * 向消息池塞入指定ID的目标对象, 若该ID未注册({@link PurposefulBlockingMessagePool#register(Object)}), 或等待已超时, 则塞入无效,
     * 并返回false.
     * @param id 指定ID
     * @param item 目标对象
     * @return true:塞入成功(包括塞入意外消息池) false:塞入失败(消息丢弃)
     */
    public boolean restock(K id, I item) throws OutOfLimitException{
        try{
            lock.lock();
            Condition condition = conditionPool.get(id);
            if (condition != null){
                itemPool.put(id, item);
                condition.signalAll();
                return true;
            }
        }finally {
            lock.unlock();
            flush();
        }
        //意外消息存入意外消息池
        if(unexpectedItemPool != null){
            try{
                unexpectedItemLock.lock();
                if (getUnexpectedItemCount() > messageLimit){
                    throw new OutOfLimitException("[PurposefulBlockingMessagePool]unexpected message out of limit, drop this message : " + messageLimit);
                }
                //放入意外消息池
                unexpectedItemPool.put(id, new UnexpectedItem<>(item));
                return true;
            } finally {
                unexpectedItemLock.unlock();
            }
        }
        return false;
    }

    /**
     * @return 注册的ID数
     */
    public int getRegisterCount(){
        try{
            lock.lock();
            return conditionPool.size();
        }finally {
            lock.unlock();
        }
    }

    /**
     * @return 消息数(非意外消息)
     */
    public int getItemCount(){
        try{
            lock.lock();
            return itemPool.size();
        }finally {
            lock.unlock();
        }
    }

    /**
     * @return 意外消息数
     */
    public int getUnexpectedItemCount(){
        if (unexpectedItemPool == null){
            return 0;
        }
        try{
            unexpectedItemLock.lock();
            return unexpectedItemPool.size();
        }finally {
            unexpectedItemLock.unlock();
        }
    }

    /**
     * 立即启动清理任务, 清理意外消息池中的失效消息(过期)
     */
    public void flush(){
        if (unexpectedItemPool != null){
            unexpectedItemFlushThreadPool.execute(new UnexpectedItemFlushTask());
        }
    }

    private I getItem(K id){
        I item = null;
        UnexpectedItem<I> unexpectedItem = null;
        try {
            lock.lock();
            item = itemPool.remove(id);
        }finally {
            lock.unlock();
        }
        if (unexpectedItemPool != null) {
            try {
                unexpectedItemLock.lock();
                unexpectedItem = unexpectedItemPool.remove(id);
            } finally {
                unexpectedItemLock.unlock();
            }
        }
        if (item != null){
            return item;
        }
        if (unexpectedItem != null){
            return unexpectedItem.getItem();
        }
        return null;
    }

    private class UnexpectedItemFlushTask implements Runnable{
        @Override
        public void run() {
            List<I> overdueItems = null;
            if (messageDropListener != null) {
                overdueItems = new ArrayList<>();
            }
            try{
                unexpectedItemLock.lock();
                List<K> overdueIds = new ArrayList<>();
                for (Map.Entry<K, UnexpectedItem<I>> entry : unexpectedItemPool.entrySet()){
                    UnexpectedItem<I> unexpectedItem = entry.getValue();
                    if (unexpectedItem == null || unexpectedItem.getItem() == null || unexpectedItem.isOverdue(unexpectedItemValidityPeriod)){
                        overdueIds.add(entry.getKey());
                    }
                }
                for (K id : overdueIds){
                    UnexpectedItem<I> unexpectedItem = unexpectedItemPool.remove(id);
                    if (overdueItems != null && unexpectedItem != null && unexpectedItem.getItem() != null){
                        overdueItems.add(unexpectedItem.getItem());
                    }
                }
            } finally {
                unexpectedItemLock.unlock();
            }
            if (overdueItems != null) {
                for (I item : overdueItems) {
                    messageDropListener.onDrop(item);
                }
            }
        }
    }

    private static class UnexpectedItem<I>{

        private I item;
        private long startTime;

        UnexpectedItem(I item){
            this.startTime = System.currentTimeMillis();
            this.item = item;
        }

        I getItem(){
            return item;
        }

        /**
         * 消息是否过期
         * @param unexpectedItemValidityPeriod 意外消息有效期
         * @return true:过期
         */
        boolean isOverdue(long unexpectedItemValidityPeriod){
            return (System.currentTimeMillis() - startTime) > unexpectedItemValidityPeriod;
        }

    }

    public interface MessageDropListener<I>{
        void onDrop(I item);
    }

    /**
     * 注册等待消息数超出限制, 或消息池内消息超过限制(包括意外消息池)
     */
    public static class OutOfLimitException extends Exception{

        public OutOfLimitException(String detailMessage) {
            super(detailMessage);
        }

    }

    /**
     * 阻塞等待超时
     */
    public static class TimeoutException extends Exception{

        public TimeoutException(String detailMessage) {
            super(detailMessage);
        }
    }

}