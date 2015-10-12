package sviolet.turquoise.utils.bitmap.loader;

import android.graphics.Bitmap;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BitmapLoader结果容器<br/>
 * 用途:<Br/>
 * 1.返回结果:setResultSucceed/setResultFailed/setResultCanceled<br/>
 *      无论同步处理,还是异步处理,均通过这些方法返回结果,BitmapLoader加载任务中的getResult方法会阻塞,
 *      直到setResultSucceed/setResultFailed/setResultCanceled方法被调用.<br/>
 *      1)setResultSucceed(Bitmap),加载成功,返回Bitmap<br/>
 *      2)setResultFailed(Throwable),加载失败,返回异常<br/>
 *      3)setResultCanceled(),加载取消<br/>
 *      若加载任务已被取消(isCancelling() = true),但仍使用setResultSucceed返回结果,则Bitmap会被存入
 *      磁盘缓存,BitmapLoader返回任务取消.<br/>
 * 2.判断任务是否取消中:isCancelling<br/>
 *      加载任务是否被取消.通常用于同步加载场合,从InputStream循环读取数据时,判断isCancelling(),若为
 *      true,则终止读取,并setResultCanceled()返回结果.<br/>
 * 3.设置任务取消监听器:setOnCancelListener<br/>
 *      加载任务被取消时,会回调该监听器.通常用于异步加载场合,例如使用异步网络框架,在该监听器回调方法中
 *      调用框架方法,终止网络请求.<br/>
 * <Br/>
 *
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
    private Runnable onCancelListener;//取消监听器

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    /**
     * [重要]返回结果:加载成功<br/>
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
     * [重要]返回结果:加载失败<br/>
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
     * [重要]返回结果:加载取消<br/>
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
            this.onCancelListener = null;
        } finally {
            lock.unlock();
        }
    }

}

