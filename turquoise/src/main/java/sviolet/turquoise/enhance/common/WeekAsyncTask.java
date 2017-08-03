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

package sviolet.turquoise.enhance.common;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * <p>内置弱引用宿主对象的AsyncTask</p>
 *
 * <p>内部维护一个弱引用的宿主(host)对象, 通常用于持有Activity/Fragment等Context对象. 当处理结果时,
 * 若宿主对象已不存在(被GC回收), 则回调onPostExecuteWithoutHost方法处理;
 * 若宿主对象存在, 则回调onPostExecuteWithHost方法处理, 并将宿主对象作为第二个参数传入,
 * 此时宿主对象不为空, 无需判断.</p>
 *
 * <pre>{@code
 *
 *  //execute task
 *  new MyTask(MyActivity.this).execute("param");
 *
 *  //define static class
 *  private static class MyTask extends WeekAsyncTask<MyActivity, String, Integer, byte[]> {
 *      public MyTask(MyActivity myActivity) {
 *          super(myActivity);
 *      }
 *      protected byte[] doInBackgroundEnhanced(String... params) throws BackgroundException{
 *          try {
 *              ......
 *          } catch (SomeException e) {
 *              //使用ExceptionWrapper包装的异常, 程序会在UI线程回调onExceptionWithHost/onExceptionWithoutHost方法, 用于异常展示
 *              throw new ExceptionWrapper(e);
 *          }
 *      }
 *      protected void onPostExecuteWithHost(byte[] bytes, MyActivity host) {
 *          host.refresh(bytes);//调用宿主的方法
 *      }
 *      protected void onExceptionWithHost(Throwable throwable, MyActivity host){
 *          //处理doInBackgroundEnhanced中用ExceptionWrapper包装的异常
 *      }
 *  }
 * }</pre>
 *
 * Created by S.Violet on 2017/8/3.
 */
public abstract class WeekAsyncTask<Host, Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private WeakReference<Host> host;
    private Throwable throwable;

    public WeekAsyncTask(Host host) {
        super();
        this.host = new WeakReference<>(host);
    }

    /**
     * @return 获取弱引用宿主对象, 可能为null, 注意判断
     */
    protected Host getHost(){
        if (host != null)
            return host.get();
        return null;
    }

    /**
     * [已改造]<br/>
     * 记录由BackgroundException包装的异常, 后续会回调onExceptionWithHost/onExceptionWithoutHost
     */
    @Override
    protected final Result doInBackground(Params[] params){
        try {
            return doInBackgroundEnhanced(params);
        } catch (ExceptionWrapper e) {
            this.throwable = e.getCause();
            throw e;
        }
    }

    /**
     * 子线程处理流程.
     *
     * <pre>{@code
     *      protected byte[] doInBackgroundEnhanced(String... params) throws BackgroundException{
     *          try {
     *              ......
     *          } catch (SomeException e) {
     *              //使用ExceptionWrapper包装的异常, 程序会在UI线程回调onExceptionWithHost/onExceptionWithoutHost方法, 用于异常展示
     *              throw new ExceptionWrapper(e);
     *          }
     *      }
     * }</pre>
     *
     */
    protected abstract Result doInBackgroundEnhanced(Params[] params) throws ExceptionWrapper;

    /**
     * [已改造]<br/>
     * 判断宿主对象是否存在, 分发到不同的方法处理结果<br/>
     */
    @Override
    public final void onPostExecute(Result result) {
        final Host host = getHost();
        if (host != null){
            onPostExecuteWithHost(result, host);
            onFinishWithHost(host);
        }else{
            onPostExecuteWithoutHost(result);
            onFinishWithoutHost();
        }

    }

    /**
     * 处理结果时, 宿主(host)对象存在的情况回调该方法处理
     * @param result 结果
     * @param host 宿主对象, 不为空
     */
    protected abstract void onPostExecuteWithHost(Result result, Host host);

    /**
     * 处理结果时, 宿主(host)对象不存在(被GC回收)的情况回调该方法处理, 可不复写
     * @param result 结果
     */
    protected void onPostExecuteWithoutHost(Result result){

    }

    /**
     * [已改造]<br/>
     * 因异常导致取消事件将被分发到onExceptionWithHost/onExceptionWithoutHost方法
     */
    @Override
    protected final void onCancelled(Result result) {
        final Host host = getHost();
        if (host != null) {
            if (throwable != null) {
                onExceptionWithHost(throwable, host);
            } else {
                onCancelledWithHost(result, host);
            }
            onFinishWithHost(host);
        } else {
            if (throwable != null) {
                onExceptionWithoutHost(throwable);
            } else {
                onCancelledWithoutHost(result);
            }
            onFinishWithoutHost();
        }
    }

    /**
     * [已改造]<br/>
     * 禁用无参onCancelled
     */
    @Override
    protected final void onCancelled() {

    }

    /**
     * 处理doInBackgroundEnhanced中使用ExceptionWrapper包装的异常, host存在的情况下
     */
    protected void onExceptionWithHost(Throwable throwable, Host host){

    }

    /**
     * 处理doInBackgroundEnhanced中使用ExceptionWrapper包装的异常, host不存在的情况下
     */
    protected void onExceptionWithoutHost(Throwable throwable){

    }

    /**
     * 任务被取消, host存在的情况下
     */
    protected void onCancelledWithHost(Result result, Host host) {

    }

    /**
     * 任务被取消, host不存在的情况下
     */
    protected void onCancelledWithoutHost(Result result) {

    }

    /**
     * 任务结束, 无论成功/异常/取消, 最终都会回调该方法
     */
    protected void onFinishWithHost(Host host){

    }

    /**
     * 任务结束, 无论成功/异常/取消, 最终都会回调该方法
     */
    protected void onFinishWithoutHost(){

    }

    /**
     * 用于包装doInBackgroundEnhanced中抛出的异常, 并回调onExceptionWithHost/onExceptionWithoutHost方法
     */
    public static class ExceptionWrapper extends RuntimeException {
        public ExceptionWrapper(Throwable t){
            super(t);
        }
    }

}
