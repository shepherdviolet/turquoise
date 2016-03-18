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

import java.io.InputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.modelx.bitmaploader.BitmapLoader;
import sviolet.turquoise.modelx.bitmaploader.handler.NetLoadHandler;

/**
 * 
 * BitmapLoader异步结果通知器<p/>
 *
 * 用于{@link BitmapLoader}网络加载处理器{@link NetLoadHandler}中任务线程与网络请求线程间的结果通知<br/>
 * <br/>
 * 用途:<Br/>
 * 1.返回处理结果:setResultSucceed/setResultFailed/setResultCanceled<br/>
 *      无论同步处理,还是异步处理,均通过这些方法返回结果,BitmapLoader加载任务会阻塞,
 *      直到setResultSucceed/setResultFailed/setResultCanceled方法被调用.结果仅允许设置一次.<br/>
 *      1)setResultSucceed(byte[]),加载成功,返回图片数据<br/>
 *      2)setResultFailed(Exception),加载失败,返回异常,触发重新加载<br/>
 *      3)setResultCanceled(),加载取消,不触发重新加载<br/>
 *      若加载任务已被取消(isCancelling() = true),但仍使用setResultSucceed返回结果,则数据会被存入
 *      磁盘缓存,但BitmapLoader返回任务取消.<p/>
 *
 * 2.判断任务是否取消中:isCancelling<br/>
 *      加载任务是否被取消.通常用于同步加载场合,从InputStream循环读取数据时,判断isCancelling(),若为
 *      true,则终止读取,并setResultCanceled()返回结果.<p/>
 *
 * 3.设置任务取消监听器:setOnCancelListener<br/>
 *      加载任务被取消时,会回调该监听器.通常用于异步加载场合,例如使用异步网络框架,在该监听器回调方法中
 *      调用框架方法,终止网络请求.<br/>
 * <Br/>
 * 
 *
 * @author S.Violet
 *
 */
public class EngineCallback {

    public static final int RESULT_NULL = 0;//no result
    public static final int RESULT_SUCCEED = 1;//load succeed
    public static final int RESULT_FAILED = 2;//load failed
    public static final int RESULT_CANCELED = 3;//load canceled
    public static final int RESULT_INTERRUPTED = 4;//load interrupted

    private int result = RESULT_NULL;//result state

    private boolean isCancelling = false;//is canceling (by cancel())

    private Object data;//result data (byte[]/InputStream/Exception/null)
    private Runnable onCancelListener;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    EngineCallback(){

    }

    /**
     * set result when load succeed, can only be called once
     * @param data byte[]
     */
    public void setResultSucceed(byte[] data){
        setResultSucceed(data);
    }

    /**
     * set result when load succeed, can only be called once
     * @param data InputStream
     */
    public void setResultSucceed(InputStream data){
        setResultSucceed(data);
    }

    private void setResultSucceed(Object data){
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
            this.data = e;
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
     * @return get loading result (byte[]/InputStream/Exception/null)
     */
    Object getData(){
        lock.lock();
        try{
            return data;
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

