package sviolet.turquoise.io;

import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

import sviolet.turquoise.app.CommonException;

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
	public static final int STATE_STARTING = 1;//正在启动
	public static final int STATE_RUNNING = 2;//正在执行
	public static final int STATE_COMPLETE = 3;//执行完毕
	
	//setting//////////////////////////////////////////

	private Object params;//参数
	private Object result;//结果
	private TQueue queue;//任务队列
	private long timeOutSet = -1;//任务超时设置
	private String key;//标签
	private boolean cancelable = true;//是否允许被取消(中止)
	
	//var//////////////////////////////////////////

	private int state = STATE_WAITTING;//执行状态
	private boolean isCanceled = false;//是否被取消(中止)
	private Thread taskThread;//任务线程
	private Timer timeOutTimer;//超时计时器
	
	/*********************************************
	 * GETSET
	 */
	
	/**
	 * 获取当前任务状态<br>
	 * STATE_WAITTING = 0;//等待被启动<br>
	 * STATE_STARTING = 1;//正在启动<br>
	 * STATE_RUNNING = 2;//正在执行<br>
	 * STATE_COMPLETE = 3;//执行完毕<br>
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
		return queue;
	}

	/**
	 * 设置绑定队列(用于回调)
	 * @param queue
	 */
	public void setQueue(TQueue queue) {
		this.queue = queue;
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
	 * 获取任务执行的线程<br>
	 * 任务开始前/结束后返回null<br>
	 * @return
	 */
	public Thread getTaskThread(){
		return taskThread;
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
	
	/*********************************************
	 * public
	 */

	/**
	 * 开始任务
	 */
	public void start(){
		if(state != STATE_WAITTING)
			throw new CommonException("[TTask]can not start a Task which state != STATE_WAITTING");

		if (queue != null) {
			state++;
			queue.taskStart(this);
		}else {
			setStopState();
		}
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
		isCanceled = true;
		setStopState();
	}
	
	/**
	 * 任务当前是否被取消(超时或手动取消)
	 * @return
	 */
	public boolean isCanceled(){
		return isCanceled;
	}
	
	/**
	 * 回收资源(新线程)[复写]<br>
	 * <br>
	 * 在新线程结束前调用, 复写此方法回收资源, 请勿回收结果信息,
	 * 因为此时onPostExecute()尚未调用, 该方法仅用于回收任务过程中的垃圾,
	 * 线程调用, 不占用主线程资源, 结果信息请在onPostExecute()中回收
	 */
	public void onRecycle(){
		
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
     * 因此如有必要, 请判断TTask.isCanceled();
	 */
	public abstract void onPostExecute(Object result);
	
	/**************************************************************************
	 * Private
	 */
	
	/**
	 * 任务执行过程(主线程)
	 */
	protected void process(){
		onPreExecute(params);
		resetTimeOutTimer();
		taskThread = new Thread(new Runnable() {
			@Override
			public void run() {
				state++;
				result = TTask.this.doInBackground(params);
				setStopState();
                if (queue != null){
					queue.taskComplete(TTask.this);
				}
			}
		});
		taskThread.start();
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
				if(state ==STATE_STARTING || state == STATE_RUNNING){
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
	 *设置为结束状态<br>
	 *<br>
	 *1.设置任务状态为完成<br>
	 *2.置空任务线程<br>
	 *3.回收过程资源(不要回收结果信息)<br>
	 *4.通知队列调度<br>
	 */
	private void setStopState(){
		state = STATE_COMPLETE;
		cancelTimeOutTimer();
		taskThread = null;
		onRecycle();
		if(queue != null){
			queue.notifyDispatchTask();
		}
	}

	/**
	 * 销毁任务(主线程)
	 */
	protected void onDestroy(){
		queue = null;
		taskThread = null;//任务线程
		timeOutTimer = null;//超时计时器
		params = null;
		result = null;
	}

	protected Object getResult(){
		return result;
	}

}
