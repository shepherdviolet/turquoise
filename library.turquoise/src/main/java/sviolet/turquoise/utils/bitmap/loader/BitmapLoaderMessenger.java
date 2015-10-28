/*
 * Copyright (C) 2015 S.Violet
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

package sviolet.turquoise.utils.bitmap.loader;

import android.graphics.Bitmap;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * BitmapLoader通知器<br/>
 * 用于BitmapLoader任务线程与异步网络请求间的通知<br/>
 * <br/>
 * 用途:<Br/>
 * 1.返回处理结果:setResultSucceed/setResultFailed/setResultCanceled<br/>
 *      无论同步处理,还是异步处理,均通过这些方法返回结果,BitmapLoader加载任务会阻塞,
 *      直到setResultSucceed/setResultFailed/setResultCanceled方法被调用.结果仅允许设置一次.<br/>
 *      1)setResultSucceed(Bitmap),加载成功,返回Bitmap<br/>
 *      2)setResultFailed(Throwable),加载失败,返回异常<br/>
 *      3)setResultCanceled(),加载取消<br/>
 *      若加载任务已被取消(isCancelling() = true),但仍使用setResultSucceed返回结果,则Bitmap会被存入
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
public class BitmapLoaderMessenger {

    public static final int RESULT_NULL = 0;//无结果
    public static final int RESULT_SUCCEED = 1;//结果:加载成功
    public static final int RESULT_FAILED = 2;//结果:加载失败
    public static final int RESULT_CANCELED = 3;//结果:加载取消
    public static final int RESULT_INTERRUPTED = 3;//结果:加载取消

    private int result = RESULT_NULL;//结果状态

    private boolean isCancelling = false;//是否正在被取消(被cancel()方法取消)

    private Bitmap bitmap;//结果图
    private Throwable throwable;//异常
    private Runnable onCancelListener;//取消监听器

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    BitmapLoaderMessenger(){
        //不允许包外部进行实例化
    }

    /**
     * [重要]返回结果:加载成功<br/>
     */
    public void setResultSucceed(Bitmap bitmap){
        lock.lock();
        try{
            //仅允许设置一次结果
            if (result != RESULT_NULL) {
                return;
            }
            this.bitmap = bitmap;
            this.throwable = null;
            this.result = RESULT_SUCCEED;
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    /**
     * [重要]返回结果:加载失败<br/>
     */
    public void setResultFailed(Throwable t){
        lock.lock();
        try{
            //仅允许设置一次结果
            if (result != RESULT_NULL) {
                return;
            }
            this.bitmap = null;
            this.throwable = t;
            this.result = RESULT_FAILED;
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    /**
     * [重要]返回结果:加载取消<br/>
     */
    public void setResultCanceled(){
        lock.lock();
        try{
            //仅允许设置一次结果
            if (result != RESULT_NULL) {
                return;
            }
            this.bitmap = null;
            this.throwable = null;
            this.result = RESULT_CANCELED;
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    /**
     * 判断任务当前是否正在被取消<br/>
     * 该状态由内部方法cancel()触发, 并非处理最终结果(result), 并非由setResultCanceled决定,
     * 仅表示该任务需要进行取消处理, 不表示已被取消成功<br/>
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
     * 设置取消监听器<br/>
     * 当任务被取消时, 会回调该监听器, 注意监听器线程可能为UI线程<br/>
     */
    public void setOnCancelListener(Runnable onCancelListener){
        this.onCancelListener = onCancelListener;
    }

    /*************************************************************
     * inner
     */

    /**
     * 阻塞等待结果
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

    Bitmap getBitmap(){
        lock.lock();
        try{
            return bitmap;
        } finally {
            lock.unlock();
        }
    }

    Throwable getThrowable(){
        lock.lock();
        try{
            return throwable;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 取消任务<br/>
     * 1.置为正在取消状态<br/>
     * 2.回调取消监听器<br/>
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
            this.bitmap = null;
            this.throwable = null;
            this.onCancelListener = null;
        } finally {
            lock.unlock();
        }
    }

}

