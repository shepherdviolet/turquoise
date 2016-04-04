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

package sviolet.turquoise.x.imageloader.server;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>is used to callback Engine by asynchronous way, return result</p>
 *
 * @author S.Violet
 *
 */
public class EngineCallback <ResultDataType> {

    public static final int RESULT_NULL = 0;//no result
    public static final int RESULT_SUCCEED = 1;//load succeed
    public static final int RESULT_FAILED = 2;//load failed
    public static final int RESULT_CANCELED = 3;//load canceled
    public static final int RESULT_INTERRUPTED = 4;//load interrupted

    private int result = RESULT_NULL;//result state

    private boolean isCancelling = false;//is canceling (by cancel())

    private ResultDataType data;//result data
    private Exception exception;//result exception
    private Runnable onCancelListener;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    EngineCallback(){

    }

    /**
     * set result when load succeed, can only be called once
     * @param data data
     */
    public void setResultSucceed(ResultDataType data){
        lock.lock();
        try{
            //can only be called once
            if (result != RESULT_NULL) {
                return;
            }
            this.data = data;
            this.result = RESULT_SUCCEED;
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    /**
     * set exception when load failed, can only be called once
     * @param e
     */
    public void setResultFailed(Exception e){
        lock.lock();
        try{
            //can only be called once
            if (result != RESULT_NULL) {
                return;
            }
            this.exception = e;
            this.result = RESULT_FAILED;
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    /**
     * set result when load canceled, can only be called once
     */
    public void setResultCanceled(){
        lock.lock();
        try{
            //can only be called once
            if (result != RESULT_NULL) {
                return;
            }
            this.data = null;
            this.result = RESULT_CANCELED;
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    /**
     * <p>if loading has been canceled by method {@link EngineCallback#cancel()}</p>
     */
    public boolean isCancelling(){
        lock.lock();
        try{
            return isCancelling;
        }finally {
            lock.unlock();
        }
    }

    /**
     * <p>callback when loading canceled by method {@link EngineCallback#cancel()}</p>
     *
     * <p>maybe in UI Thread</p>
     */
    public void setOnCancelListener(Runnable onCancelListener){
        this.onCancelListener = onCancelListener;
    }

    /*************************************************************
     * inner
     */

    /**
     * waiting for result, thread will be block
     */
    int getResult(){
        lock.lock();
        try{
            if (result == RESULT_NULL)
                condition.await();
            return result;
        } catch (InterruptedException ignored) {
            result = RESULT_INTERRUPTED;//中断状态
        } finally {
            lock.unlock();
        }
        return RESULT_CANCELED;
    }

    /**
     * @return get loading result
     */
    ResultDataType getData(){
        lock.lock();
        try{
            return data;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return get loading exception
     */
    Exception getException(){
        lock.lock();
        try{
            return exception;
        } finally {
            lock.unlock();
        }
    }

    /**
     * notify
     */
    void cancel(){
        lock.lock();
        try{
            isCancelling = true;
            if (onCancelListener != null)
                onCancelListener.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 取消任务,并强制中断getResult()等待<br/>
     * 1.取消任务<Br/>
     * 2.中断阻塞<br/>
     */
    void interrupt(){
        lock.lock();
        try{
            cancel();//先取消
            result = RESULT_INTERRUPTED;//标识为被中断
            condition.signalAll();//中断阻塞
        } finally {
            lock.unlock();
        }
    }

    /**
     * 销毁
     */
    void destroy(){
        lock.lock();
        try{
            this.data = null;
            this.onCancelListener = null;
        } finally {
            lock.unlock();
        }
    }

}

