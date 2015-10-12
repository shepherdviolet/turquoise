package sviolet.turquoise.utils.bitmap.loader;

import android.graphics.Bitmap;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BitmapLoader结果容器<br/>
 * 用途:<Br/>
 * 1.同步/异步返回结果:setResultSucceed/setResultFailed/setResultCanceled</><br/>
 * 2.判断当前任务是否被取消:canceled<br/>
 * 3.可设置任务取消监听器.配合异步网络框架使用,回调方法中终止网络请求:setOnCancelListenner<br/>
 * <Br/>
 * BitmapLoader加载任务中的getResult方法会阻塞, 一直到setResult或setThrowable方法被调用<br/>
 */
public class BitmapLoaderHolder {

    public static final int RESULT_NULL = 0;//无结果
    public static final int RESULT_SUCCEED = 1;//结果:加载成功
    public static final int RESULT_FAILED = 2;//结果:加载失败
    public static final int RESULT_CANCELED = 3;//结果:加载取消

    private int result = RESULT_NULL;//结果状态

    private boolean isCancelling = false;//是否正在被取消(被cancel()方法取消)
    private boolean hasInterrupted = false;//是否被中断(被interrupt方法中断)

    private Bitmap bitmap;//结果图
    private Throwable throwable;//异常
    private Runnable onCancelListenner;//取消监听器

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    /**
     * 返回结果:加载成功<br/>
     */
    public void setResultSucceed(Bitmap bitmap){
        lock.lock();
        try{
            //若get()阻塞被中断后, set(Bitmap),为防止内存泄露,将Bitmap回收
            if (hasInterrupted) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
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
     * 返回结果:加载失败<br/>
     */
    public void setResultFailed(Throwable t){
        lock.lock();
        try{
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
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
     * 返回结果:加载取消
     */
    public void setResultCanceled(){
        lock.lock();
        try{
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
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
     * 仅表示该任务是否需要进行取消处理, 不表示已被取消成功<br/>
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
    public void setOnCancelListenner(Runnable onCancelListenner){
        this.onCancelListenner = onCancelListenner;
    }

    /**
     * 阻塞等待结果
     */
    int getResult(){
        lock.lock();
        try{
            if (result == RESULT_NULL && !hasInterrupted)
                condition.await();
            return result;
        } catch (InterruptedException ignored) {
            hasInterrupted = true;
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
     * 1.置为被取消状态<br/>
     * 2.回调取消监听器<br/>
     */
    void cancel(){
        lock.lock();
        try{
            isCancelling = true;
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
            this.bitmap = null;
            this.throwable = null;
            this.onCancelListenner = null;
        } finally {
            lock.unlock();
        }
    }

}

