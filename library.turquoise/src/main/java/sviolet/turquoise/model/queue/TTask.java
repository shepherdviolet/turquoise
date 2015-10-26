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
 */

package sviolet.turquoise.model.queue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TQueue队列用任务<br>
 * <br>
 *
 * @author S.Violet
 *
 */

public abstract class TTask {

	//static//////////////////////////////////////////

	public static final int STATE_WAITTING = 0;//等待被启动
	public static final int STATE_PRE_EXECUTE = 1;//执行前
	public static final int STATE_EXECUTING = 2;//正在执行
    public static final int STATE_POST_EXECUTE = 3;//执行后
	public static final int STATE_COMPLETE = 4;//执行完毕
    public static final int STATE_CANCELING = 5;//取消中
    public static final int STATE_CANCELED = 6;//已取消

	//setting//////////////////////////////////////////

    private WeakReference<TQueue> mQueue;//队列

	private Object params;//参数
	private Object result;//结果
	private long timeOutSet = -1;//任务超时设置
	private String key;//标签
	private boolean cancelable = true;//是否允许被取消(中止)

	private List<TTask> follower;//跟随的任务

	//var//////////////////////////////////////////

	private int state = STATE_WAITTING;//执行状态
	private Timer timeOutTimer;//超时计时器

	/*********************************************
	 * GETSET
	 */

	/**
	 * 获取当前任务状态<br>
     * STATE_WAITTING = 0;//等待被启动
     * STATE_PRE_EXECUTE = 1;//执行前
     * STATE_EXECUTING = 2;//正在执行
     * STATE_POST_EXECUTE = 3;//执行后
     * STATE_COMPLETE = 4;//执行完毕
     * STATE_CANCELING = 5;//取消中
     * STATE_CANCELED = 6;//已取消
	 *
	 * @return
	 */
	public int getState() {
		return state;
	}

	/**
	 * 获取绑定的队列
	 * @return
	 */
	public TQueue getQueue() {
		if (mQueue != null)
            return mQueue.get();
        return null;
	}

	/**
	 * 设置绑定队列(用于回调)
	 * @param queue
	 */
	public void setQueue(TQueue queue) {
		this.mQueue = new WeakReference<TQueue>(queue);
	}

	/**
	 * 获取参数
	 * @return
	 */
	public Object getParams() {
		return params;
	}

	/**
	 * 设置参数
	 * @param params
	 */
	public TTask setParams(Object params) {
		this.params = params;
		return this;
	}

	/**
	 * 设置任务超时时间, 超时后会调用cancel方法
	 * @param timeout 默认不超时-1
	 */
	public void setTimeOut(long timeout){
		this.timeOutSet = timeout;
	}

	/**
	 * 设置任务是否允许被手动中止(超时中止无视该设置)
	 * @param cancelable 默认true
	 */
	public void setCancelable(boolean cancelable){
		this.cancelable = cancelable;
	}

	/**
	 * 设置标签
	 *
	 * @param key 标签
	 */
	public void setKey(String key){
		this.key = key;
	}

    /**
     * 获得标签
     *
     * @return 标签
     */
    public String getKey(){
        return key;
    }

	/**
	 * 添加一个跟随的任务<br/>
	 *
	 * 当任务完成时, 会回调跟随任务的onFollow
	 *
	 * @param task 跟随的任务
	 */
	protected void addFollower(TTask task){
		synchronized (TTask.this) {
			if (follower == null)
				follower = new ArrayList<TTask>();
			follower.add(task);
            if (task.getFollower() != null){
                for (int i = 0 ; i < task.getFollower().size() ; i++){
                    follower.add(task.getFollower().get(i));
                }
            }
		}
	}

    protected List<TTask> getFollower(){
        return follower;
    }

	/*********************************************
	 * public
	 */

	/**
	 * 开始任务<br/>
     * 任务不能独立执行, 必须TQueue.put(key, TTask)放入队列(自动执行)<br/>
	 *
	 * @return true:启动成功 false:启动失败
	 */
	protected boolean start(){
        synchronized (TTask.this) {
            if (state >= STATE_CANCELING) {
                //已取消的任务不再执行, 启动失败
                return false;
            } else if (state != STATE_WAITTING) {
                //其他非等待状态则抛出异常
                throw new RuntimeException("[TTask]can not start a TTask which state != STATE_WAITTING");
            }
            state = STATE_PRE_EXECUTE;
        }
		if (getQueue() != null) {
            getQueue().ttask_postStart(this);
		}else {
            throw new RuntimeException("[TTask]can not start a TTask without TQueue");
		}
		//启动成功
		return true;
	}

	/**
	 * 中止任务(手动)<br>
	 * 若设置cancelable = false, 则此方法无效<br>
	 */
	public void cancel(){
		if(cancelable)
			onCancel();
	}

	/**
	 * 当终止任务时调用(超时或手动中止)[复写]<br>
	 * <br>
	 * 复写该方法实现任务的中断, 例如网络请求abort等操作,
	 * 父类方法已调用setStopState()设置任务状态为结束,
	 * 并置空线程对象, 通知队列调度
	 */
	public void onCancel(){
        synchronized (TTask.this) {
            if (state == STATE_WAITTING) {
                //取消等待中的任务, 直接置为已取消状态
                state = STATE_CANCELED;
            } else if (state < STATE_COMPLETE) {
                //未完成的任务置为取消中
                state = STATE_CANCELING;
            }
        }
		cancelTimeOutTimer();
		if(getQueue() != null){
            getQueue().notifyDispatchTask();
		}
	}

    /**
     * @return 任务"取消中"/"已取消"状态返回true
     */
    public boolean isCancel(){
        return state >= STATE_CANCELING;
    }

	/******************************************************************
	 * abstract
	 */

	/**
	 * 任务前准备(主线程), 通常为UI操作
	 * @param params
	 * @return
	 */
	public abstract void onPreExecute(Object params);

	/**
	 * 任务(新线程)
	 * @param params
	 * @return
	 */
	public abstract Object doInBackground(Object params);

	/**
	 * 任务后回调(主线程), 通常为UI操作<br/>
     * 执行中的任务被cancel, 任务结束后仍会调用该方法<br/>
     *
     * @param  result doInBackground返回的结果
     * @param isCancel 任务"取消中"/"已取消"状态为true
	 */
	public abstract void onPostExecute(Object result, boolean isCancel);

	/**************************************************************************
	 * Private
	 */

	/**
	 * 任务执行过程(主线程)
	 */
	protected void process(){
        if (checkCancelState())
            return;
        onPreExecute(params);
		resetTimeOutTimer();
		if (getQueue() == null){
			throw new RuntimeException("[TTask]can not start a TTask without TQueue");
		}
        getQueue().ttask_execute(new Runnable() {
            @Override
            public void run() {
                synchronized (TTask.this) {
                    if (checkCancelState())
                        return;
                    state = STATE_EXECUTING;
                }
                result = TTask.this.doInBackground(params);
                cancelTimeOutTimer();
                synchronized (TTask.this) {
                    if (checkCancelState())
                        return;
                    state = STATE_POST_EXECUTE;
                }
                if (getQueue() != null) {
                    getQueue().ttask_postComplete(TTask.this);
                }
            }
        });
	}

    /**
     * 任务执行后的流程(主线程)
     */
    protected void afterProcess(){
        onPostExecute(result, isCancel());
        synchronized (TTask.this) {
            if (state <= STATE_COMPLETE) {
                state = STATE_COMPLETE;
            } else {
                state = STATE_CANCELED;
            }
        }
		if(getQueue() != null){
            getQueue().notifyDispatchTask();
		}

        //执行跟随者
        if (follower != null){
            try{
                for (int i = 0 ; i < follower.size() ; i++){
                    follower.get(i).afterFollowed(result, isCancel());
                }
            }catch(Exception ignored){
            }
        }

        //销毁
        onDestroy();
    }

    /**
     * 跟踪完成后的流程
     */
    protected void afterFollowed(Object result, boolean isCanceled){
        onPostExecute(result, isCanceled);
        synchronized (TTask.this) {
            if (state <= STATE_COMPLETE) {
                state = STATE_COMPLETE;
            } else {
                state = STATE_CANCELED;
            }
        }
        onDestroy();
    }

    /**
     * 检查取消状态, 取消中则置为已取消
     */
    private boolean checkCancelState() {
        //检查取消状态
        if (state >= STATE_CANCELING){
            if(getQueue() != null){
                getQueue().ttask_postComplete(TTask.this);
            }
            return true;
        }
        return false;
    }

    /**
	 * 重置超时计时器
	 */
	private void resetTimeOutTimer() {
		if(timeOutSet <= 0)
			return;
		timeOutTimer = new Timer();
		timeOutTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(state ==STATE_PRE_EXECUTE || state == STATE_EXECUTING){
					TTask.this.onCancel();//直接调用onCancel无视cancelable
				}
			}
		}, timeOutSet);
	}

	/**
	 * 取消超时计时器
	 */
	private void cancelTimeOutTimer(){
		if(timeOutSet <= 0)
			return;
		if(timeOutTimer != null)
			timeOutTimer.cancel();
	}

	/**
	 * 销毁任务(主线程)
	 */
	protected void onDestroy(){
		timeOutTimer = null;//超时计时器
		params = null;
		result = null;
        if (follower != null)
            follower.clear();
	}

}
