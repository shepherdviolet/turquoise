package sviolet.turquoise.utils.bitmap.loader;

import android.graphics.Bitmap;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 结果容器<br/>
 * get方法会阻塞, 一直到set方法执行后
 */
public class BitmapLoaderResultHolder {

    private boolean hasInterrupted = false;//是否被打断
    private boolean hasSet = false;//是否设置了值

    private Bitmap result;//结果

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public void set(Bitmap result){
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

    @Deprecated
    Bitmap get(){
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

    @Deprecated
    void interrupt(){
        lock.lock();
        try{
            hasInterrupted = true;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

}

