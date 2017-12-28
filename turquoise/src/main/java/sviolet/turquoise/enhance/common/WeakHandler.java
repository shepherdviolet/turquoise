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

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 内置弱引用宿主对象的Handler<p/>
 *
 * 内部维护一个弱引用的宿主(host)对象, 通常用于持有Activity/Fragment等Context对象. 当Handler处理消息时,
 * 若宿主对象已不存在(被GC回收), 则回调{@link WeakHandler#handleMessageWithoutHost(Message)}方法处理
 * 消息; 若宿主对象存在, 则回调{@link WeakHandler#handleMessageWithHost(Message, Object)}方法处理, 并
 * 将宿主对象作为第二个参数传入, 此时宿主对象不为空, 无需判断.<p/>
 *
 * 为防止Handler持有Context造成内存泄露, 规范化Handler写法, 代码示例如下:<p/>
 *
 * <pre>{@code
 *
 *      //实例化并传入宿主Activity, 宿主会被Handler弱引用
 *      private final MyHandler handler = new MyHandler(this);
 *
 *      //静态内部类(static class), 不使用匿名内部类. 泛型由传入的宿主(host)决定, 通常为Activity/Fragment
 *      private static class MyHandler extends WeakHandler<MyActivity>{
 *
 *          //消息编号统一定义在内部, 定义为静态常量
 *          private static final int WHAT_REFRESH = 1;
 *
 *          //选择一个构造器复写, 不要使用Callback方式, (Callback作为匿名内部类容易直接引用外部对象)
 *          public MyHandler(MyActivity host) {
 *              super(host);
 *          }
 *
 *          //当弱引用的宿主(host)存在时, 会回调该方法处理消息, 宿主作为参数传入(不为null)
 *          protected void handleMessageWithHost(Message msg, MyActivity host) {
 *              synchronized (this) {
 *                  switch (msg.what) {
 *                      case WHAT_REFRESH:
 *                          host.refresh();//调用宿主方法
 *                          break;
 *                      default:
 *                          break;
 *                  }
 *              }
 *          }
 *
 *          //当弱引用的宿主(host)不存在(被GC回收), 会回调该方法处理消息
 *          protected void handleMessageWithoutHost(Message msg) {
 *
 *          }
 *      }
 * }</pre>
 *
 * Created by S.Violet on 2015/11/29.
 */
public abstract class WeakHandler<T> extends Handler {

    private WeakReference<T> host;

    /**
     * @param host 弱引用宿主对象, 通常为Activity/Fragment
     */
    public WeakHandler(T host) {
        super();
        this.host = new WeakReference<T>(host);
    }

    /**
     * @param looper Looper, Looper.getMainLooper或Looper.myLooper();
     * @param host 弱引用宿主对象, 通常为Activity/Fragment
     */
    public WeakHandler(Looper looper, T host) {
        super(looper);
        this.host = new WeakReference<T>(host);
    }

    /**
     * @return 获取弱引用宿主对象, 可能为null, 注意判断
     */
    protected T getHost(){
        if (host != null) {
            return host.get();
        }
        return null;
    }

    /**
     * [已改造]<br/>
     * 判断宿主对象是否存在, 分发到不同的方法处理消息<br/>
     */
    @Override
    public final void handleMessage(Message msg) {
        final T host = getHost();
        if (host != null){
            handleMessageWithHost(msg, host);
        }else{
            handleMessageWithoutHost(msg);
        }
    }

    /**
     * 处理消息时, 宿主(host)对象存在的情况回调该方法处理消息
     * @param msg 消息
     * @param host 宿主对象, 不为空
     */
    protected abstract void handleMessageWithHost(Message msg, T host);

    /**
     * 处理消息时, 宿主(host)对象不存在(被GC回收)的情况回调该方法处理消息, 可不复写
     * @param msg 消息
     */
    protected void handleMessageWithoutHost(Message msg){

    }

}
