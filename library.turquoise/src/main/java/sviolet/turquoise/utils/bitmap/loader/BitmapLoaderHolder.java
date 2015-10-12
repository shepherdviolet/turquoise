package sviolet.turquoise.utils.bitmap.loader;

import android.graphics.Bitmap;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BitmapLoader结果容器<br/>
 * 用途:<Br/>
 * 1.同步/异步返回结果(或异常)</><br/>
 * 2.判断当前任务是否被取消<br/>
 * 3.可设置任务取消监听器.配合异步网络框架使用,回调方法中终止网络请求即可<br/>
 * <Br/>
 * BitmapLoader加载任务中的getResult方法会阻塞, 一直到setResult或setThrowable方法被调用<br/>
 */
public class BitmapLoaderHolder {

    private boolean hasSet = false;//是否设置了值
    private boolean hasCanceled = false;//是否被取消
    private boolean hasInterrupted = false;//是否被中断

    private Bitmap result;//结果
    private Throwable throwable;//异常
    private Runnable onCancelListenner;//取消监听器

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    /**
     * 返回结果<br/>
     */
    public void setResult(Bitmap result){
        lock.lock();
        try{
            //若get()阻塞被中断后, set(Bitmap),为防止内存泄露,将Bitmap回收
            if (hasInterrupted) {
                if (result != null && !result.isRecycled()) {
                    result.recycle();
                }
                return;
            }
            this.result = result;
            this.hasSet = true;
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    /**
     * 返回结果(异常)
     */
    public void setThrowable(Throwable t){
        lock.lock();
        try{
            if (result != null && !result.isRecycled()) {
                result.recycle();
            }
            this.result = null;
            this.throwable = t;
            this.hasSet = true;
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }

    /**
     * 判断任务当前是否被取消
     * @return true:已被取消
     */
    public boolean canceled(){
        lock.lock();
        try{
            return hasCanceled;
        }finally {
            lock.unlock();
        }
    }

    /**
     * 设置取消监听器<br/>
     * 当任务被取消时, 会回调该监听器, 注意监听器线程可能为UI线程<br/>
     */
    public void setOnCancelListenner(Runnable onCancelListenner){
        this.onCancelListenner = onCancelListenner;
    }

    /**
     * 阻塞等待结果
     */
    Bitmap getResult(){
        lock.lock();
        try{
            if (!hasSet && !hasInterrupted)
                condition.await();
            return result;
        } catch (InterruptedException ignored) {
            hasInterrupted = true;
        } finally {
            lock.unlock();
        }
        return null;
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
     * 1.置为被取消状态<br/>
     * 2.回调取消监听器<br/>
     */
    void cancel(){
        lock.lock();
        try{
            hasCanceled = true;
            if (onCancelListenner != null)
                onCancelListenner.run();
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
            hasInterrupted = true;//标识为被中断
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
            this.result = null;
            this.throwable = null;
            this.onCancelListenner = null;
        } finally {
            lock.unlock();
        }
    }

}

