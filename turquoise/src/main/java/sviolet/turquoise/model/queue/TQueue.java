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

package sviolet.turquoise.model.queue;

import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import sviolet.turquoise.model.thread.LazySingleThreadPool;
import sviolet.turquoise.utils.common.WeakHandler;

/**
 * 任务队列 (必须在主线程实例化)<br/>
 * <br/>
 * [concurrencyVolumeMax]:<br/>
 * 最大并发量, 同时执行的任务数<br/>
 * <br/>
 * [volumeMax]:<br/>
 * 最大任务量, 等待队列的最大容积, 当等待队列超过限制时, 会取消优先级
 * 最低的任务, 顺序队列中后进的任务优先级最低, 逆序队列中先进的任务
 * 优先级最低.
 * <br/>
 * 1.顺序队列<br/>
 * 先进先执行
 * <br/>
 * 2.逆序队列<br/>
 * 后进先执行
 * 
 * @author S.Violet
 *
 */

public class TQueue {

    /**
     * 替代策略
     * 新的任务替换老的任务, 并取消老的任务
     */
    public static final int KEY_CONFLICT_POLICY_DISPLACE = 0;
    /**
     * 跟随策略
     * 新的任务跟随老的任务, 当老的任务完成后, 跟随者也得到相应的回调
     */
    public static final int KEY_CONFLICT_POLICY_FOLLOW = 1;
    /**
     * 取消策略
     * 新的任务取消, 老的任务继续执行(若老任务已被取消, 则移除老任务, 加入新任务)
     */
    public static final int KEY_CONFLICT_POLICY_CANCEL = 2;

	//Setting//////////////////////////////////////////////////////

	private boolean reverse = false;//逆序
    private boolean waitCancelingTask = false;//等待取消中的任务
	private int concurrencyVolumeMax = 1;//最大并发量
    private int volumeMax = Integer.MAX_VALUE;//最大任务量

    private int keyConflictPolicy = KEY_CONFLICT_POLICY_DISPLACE;//任务(key)冲突策略
	
	//Variable//////////////////////////////////////////////////////

    private LazySingleThreadPool dispatchThreadPool;//调度线程池
    private ExecutorService taskThreadPool;//任务线程池

	private LinkedHashMap<String, TTask> waittingTasks;//等待队列
    private LinkedHashMap<String, TTask> runningTasks;//执行队列

    /**
     * reverse : 逆序队列<br/>
     * 设置为true后, 先执行后加入的任务(TTask)<Br/>
     *
     * @param reverse 逆序队列
     * @param concurrencyVolumeMax 最大并发量
     */
    public TQueue(boolean reverse, int concurrencyVolumeMax){
        this.reverse = reverse;
        waittingTasks = new LinkedHashMap<>(0, 0.75f, true);
        runningTasks = new LinkedHashMap<>(0);
        setConcurrencyVolumeMax(concurrencyVolumeMax);
    }

	/*****************************************************
	 * Public
	 */

    /**
     * [同步]队列中增加任务并执行<br/>
     * 线程同步操作, 可能会阻塞<br/>
     * @param task
     */
    public void put(String key, TTask task) {
        if (key == null)
            throw new RuntimeException("[TQueue]key is null", new NullPointerException());
        if (task == null)
            throw new RuntimeException("[TQueue]task is null", new NullPointerException());

        //给任务设置队列对象(用于回调)
        task.setQueue(this);
        task.setKey(key);
        synchronized (TQueue.this){
            //是否存在同名任务
            if (runningTasks.containsKey(key)){
                if (keyConflictPolicy == KEY_CONFLICT_POLICY_DISPLACE){
                    TTask oldTask;
                    if (waitCancelingTask){
                        oldTask = runningTasks.get(key);
                    }else{
                        oldTask = runningTasks.remove(key);
                    }
                    if (oldTask != null)
                        oldTask.cancel();
                    //继续执行,新任务加入等待队列
                }else if(keyConflictPolicy == KEY_CONFLICT_POLICY_FOLLOW){
                    if (runningTasks.get(key).getState() >= TTask.STATE_COMPLETE){
                        //若正在执行的原任务已被取消或已执行完毕, 则移除原任务
                        runningTasks.remove(key);
                        //继续执行,新任务加入等待队列
                    }else {
                        //新的任务跟随老的任务
                        runningTasks.get(key).addFollower(task);
                        return;
                    }
                }else{//KEY_CONFLICT_POLICY_CANCEL
                    if (runningTasks.get(key).getState() >= TTask.STATE_COMPLETE){
                        //若正在执行的原任务已被取消或已执行完毕, 则移除原任务
                        runningTasks.remove(key);
                        //继续执行,新任务加入等待队列
                    }else {
                        //新任务取消
                        task.cancel();
                        return;
                    }
                }
            }
            if (waittingTasks.containsKey(key)){
                if (keyConflictPolicy == KEY_CONFLICT_POLICY_DISPLACE){
                    //移除同名原有任务, 加入新任务
                    TTask removeTask = waittingTasks.remove(key);
                    removeTask.cancel();
                    //继续执行,新任务加入等待队列
                }else if(keyConflictPolicy == KEY_CONFLICT_POLICY_FOLLOW){
                    //获取老任务
                    for (Map.Entry<String, TTask> entry : waittingTasks.entrySet()){
                        if(key.equals(entry.getKey())){
                            //新的任务跟随老任务
                            entry.getValue().addFollower(task);
                            break;
                        }
                    }
                    return;
                }else{//KEY_CONFLICT_POLICY_CANCEL
                    //新任务取消
                    task.cancel();
                    return;
                }
            }

            trimWattingTasks();
            waittingTasks.put(key, task);//加入等待队列

        }
        notifyDispatchTask();//触发任务调度
    }

    /**
     * 等待队列满时, 清除一个优先级最低的任务
     */
    private void trimWattingTasks() {
        //等待队列超出限制时, 清除优先级最低的任务
        if (waittingTasks.size() >= volumeMax) {
            if (reverse) {
                //取队列顶部(最早任务)
                Map.Entry<String, TTask> firstEntry = null;
                for (Map.Entry<String, TTask> entry : waittingTasks.entrySet()) {
                    firstEntry = entry;
                    break;
                }
                TTask cancelTask = waittingTasks.remove(firstEntry.getKey());
                cancelTask.cancel();
            } else {
                //取队列底部(最新任务)
                Map.Entry<String, TTask> lastEntry = null;
                for (Map.Entry<String, TTask> entry : waittingTasks.entrySet()) {
                    lastEntry = entry;
                }
                TTask cancelTask = waittingTasks.remove(lastEntry.getKey());
                cancelTask.cancel();
            }
        }
    }

    /**
	 * 通知队列进行调度<br>
	 * <br>
	 * 同时多次调用只会执行两次<br>
	 * Task会自动触发该方法<br>
	 */
	public void notifyDispatchTask(){
		//开启线程执行调度任务
		executeDispatch(new Runnable() {
            @Override
            public void run() {
                synchronized (TQueue.this) {
                    dispatchTask();
                }
            }
        });
	}
	
	/**
	 * 获取队列中的总任务数<br>
	 * 总任务数, 包括等待/开始/执行/结束(尚未被清除)的任务<br>
	 * @return
	 */
	public int getCurrentVolume(){
		return getCurrentRunningVolume() + getCurrentWaittingVolume();
	}

    /**
     * 获取队列中的等待任务数<br/>
     * @return
     */
    public int getCurrentWaittingVolume(){
        return waittingTasks.size();
    }

    /**
     * 获取队列中的执行任务数<br/>
     * 开始/执行/结束(尚未被清除)的任务<br>
     * @return
     */
    public int getCurrentRunningVolume(){
        return runningTasks.size();
    }
	
	/**
	 * [同步]获取当前并发量(正在执行的任务数, 除去执行完毕未被清除的)<br/>
     * 线程同步操作, 可能会阻塞<br/>
	 * @return
	 */
	public int getCurrentConcurrencyVolume(){
		int count = 0;
        synchronized (TQueue.this) {
            //遍历任务队列, 对开始和执行状态计数
            for (Map.Entry<String, TTask> entry : runningTasks.entrySet()) {
                TTask task = entry.getValue();
                if (waitCancelingTask){
                    //等待取消中任务模式下, STATE_CANCELING状态也计入并发量
                    if (task.getState() == TTask.STATE_PRE_EXECUTE || task.getState() == TTask.STATE_EXECUTING || task.getState() == TTask.STATE_POST_EXECUTE || task.getState() == TTask.STATE_CANCELING)
                        count++;
                }else {
                    if (task.getState() == TTask.STATE_PRE_EXECUTE || task.getState() == TTask.STATE_EXECUTING || task.getState() == TTask.STATE_POST_EXECUTE)
                        count++;
                }
            }
        }
		return count;
	}

    /**
     * 设置队列最大容量(等待队列)<br/>
     * 当等待队列中的任务超过设定值, 优先级最低的任务会被取消,
     * 正序队列最后加入的任务优先级最低, 逆序队列最先加入的任务
     * 优先级最低
     *
     * @param volumeMax
     */
    public TQueue setVolumeMax(int volumeMax){
        this.volumeMax = volumeMax;
        return this;
    }

    /**
     * 获得队列最大容量(等待队列)
     * @return
     */
    public int getVolumeMax(){
        return volumeMax;
    }

	/**
	 * 设置最大并发量(队列允许的最大并发量)
	 * @param concurrencyVolumeMax
	 */
	public TQueue setConcurrencyVolumeMax(int concurrencyVolumeMax){
		this.concurrencyVolumeMax = concurrencyVolumeMax;
        return this;
	}

	/**
	 * 获取最大并发量参数(队列允许的最大并发量)
	 * @return
	 */
	public int getConcurrencyVolumeMax(){
		return concurrencyVolumeMax;
	}

    /**
     * [同步]取消指定标签的任务<br/>
     * 线程同步操作, 可能会阻塞<br/>
     */
    public void cancel(String key) {
        synchronized (TQueue.this) {
            if (runningTasks.containsKey(key)){
                TTask task;
                if (waitCancelingTask) {
                    //等待取消任务
                    task = runningTasks.get(key);
                }else{
                    //不等待取消任务
                    task = runningTasks.remove(key);
                }
                if (task != null) {
                    task.cancel();
                }
            }
            if (waittingTasks.containsKey(key)){
                TTask task = waittingTasks.remove(key);
                if (task != null)
                    task.cancel();
            }
        }
    }

    /**
	 * 取消所有任务
	 */
	public void cancelAll(){
        synchronized (TQueue.this) {
            for (Map.Entry<String, TTask> entry : runningTasks.entrySet()) {
                TTask task = entry.getValue();
                if (task != null)
                    task.cancel();
            }
            for (Map.Entry<String, TTask> entry : waittingTasks.entrySet()) {
                TTask task = entry.getValue();
                if (task != null)
                    task.cancel();
            }
            runningTasks.clear();
            waittingTasks.clear();
        }
	}

    /**
     * [异步]取消所有任务
     */
    public void asyncCancelAll(){
        executeDispatch(new Runnable() {
            @Override
            public void run() {
                cancelAll();
            }
        });
    }

    /**
     * [同步]优先处理指定任务<br/>
     * 将指定任务排到第一位<br/>
     * 线程同步操作, 可能会阻塞<br/>
     *
     * @param key
     */
    public void preferred(String key) {
        synchronized (TQueue.this){
            if (reverse) {
                waittingTasks.get(key);
            }else{
                for (Object taskKey : waittingTasks.keySet().toArray()) {
                    if (!taskKey.equals(key))
                        waittingTasks.get(taskKey);
                }
            }
        }
    }

    /**
     * [重要]销毁队列
     */
    public void destroy(){
        cancelAll();
        if (taskThreadPool != null)
            taskThreadPool.shutdown();
        if (dispatchThreadPool != null)
            dispatchThreadPool.shutdown();
    }

    /**
     * 强制销毁队列
     */
    public void forceDestroy(){
        cancelAll();
        if (taskThreadPool != null)
            taskThreadPool.shutdownNow();
        if (dispatchThreadPool != null)
            dispatchThreadPool.shutdownNow();
    }

    /**
     * [慎用]等待取消中的任务(默认false)<br/>
     * <br/>
     * 若设置为true, STATE_CANCELING状态的任务将不会被调度器从
     * 执行队列中移除, 且该状态的任务计入任务并发数, 直到任务进程
     * 结束, 任务状态变为STATE_CANCELED后, 才会被移除.
     * 设置了该功能后, 取消中的任务将会占用并发数, 若大量的取消任务
     * 占满执行队列, 且进程一直不结束, 将会阻碍其他任务的执行. 因此,
     * 必须设法中断进程执行.<br/>
     * <br/>
     * 若设置为false, STATE_CANCELING状态的任务会和STATE_CANCELED
     * 状态的任务一样, 从执行队列中移除. 但也必须注意, 合适的情况下
     * 中断被取消的进程, 否则会有大量的线程消耗资源.
     *
     * @param waitCancelingTask 默认false
     */
    public TQueue waitCancelingTask(boolean waitCancelingTask){
        this.waitCancelingTask = waitCancelingTask;
        return this;
    }

    /**
     * 同名(key)任务冲突处理策略<br/>
     * 当新任务加入队列时, 发现等待队列或执行任务中, 存在与新任务同名(key)老任务的情况<br/>
     * <Br/>
     * KEY_CONFLICT_POLICY_DISPLACE = 0;<br/>
     * 替代策略:默认值,将老任务取消(cancel)并移除队列, 将新任务加入队列<br/>
     * <Br/>
     * KEY_CONFLICT_POLICY_FOLLOW = 1;<br/>
     * 跟随策略:新任务自身不执行, 作为老任务的跟随者, 当老任务完成后, 会回调跟随者的onPostExecute方法,
     * 并传入相同的结果<Br/>
     * KEY_CONFLICT_POLICY_CANCEL = 2;<br/>
     * 取消策略:新任务不执行直接取消(cancel), 老任务继续执行<Br/>
     *
     * @param keyConflictPolicy 策略
     */
    public TQueue setKeyConflictPolicy(int keyConflictPolicy){
        this.keyConflictPolicy = keyConflictPolicy;
        return this;
    }

    /**************************************************
	 * Private
	 */
	
	/**
	 * 任务调度(同时多次触发, 只会执行一次)<br>
	 * <br>
	 * 1.清除执行完毕的任务<br>
	 * 2.启动等待的任务(按顺序和优先级)<br>
	 * <br>
	 * double-check::<br>
	 * concurrencyVolume先获取一次, 内循环体中不获取concurrencyVolume, 这样可以减少
	 * 遍历次数, 在外循环体结束时, 再获取一次concurrencyVolume, 若此时concurrencyVolume
	 * 有变化, 且带宽未占满, 启动更多的任务<br>
	 * 
	 */
	private void dispatchTask(){
        //清理执行队列
        cleanRunningTasks();
        //当前执行队列任务数
		int concurrencyVolume = getCurrentRunningVolume();
		//唤醒等待的任务
		//double-check
		while(waittingTasks.size() > 0 && concurrencyVolume < concurrencyVolumeMax){
			while(waittingTasks.size() > 0 && concurrencyVolume < concurrencyVolumeMax){
                if (reverse){
                    //取队列底部(最新任务)
                    Map.Entry<String, TTask> lastEntry = null;
                    for (Map.Entry<String, TTask> entry : waittingTasks.entrySet()) {
                        lastEntry = entry;
                    }
                    concurrencyVolume = startTask(concurrencyVolume, lastEntry);
                }else{
                    //取队列顶部(最早任务)
                    Map.Entry<String, TTask> firstEntry = null;
                    for (Map.Entry<String, TTask> entry : waittingTasks.entrySet()) {
                        firstEntry = entry;
                        break;
                    }
                    concurrencyVolume = startTask(concurrencyVolume, firstEntry);
                }
			}
            //检查调度完成后, 理论并发数是否等于实际并发数
            if (concurrencyVolume != getCurrentConcurrencyVolume()){
                //遍历执行队列, 清除已完成的任务
                cleanRunningTasks();
                //当前执行队列任务数
                concurrencyVolume = getCurrentRunningVolume();
            }
		}
    }

    /**
     * 启动任务
     * @param concurrencyVolume 并发量
     * @param entry 任务Entry
     * @return 递增后的并发量
     */
    private int startTask(int concurrencyVolume, Map.Entry<String, TTask> entry) {
        if (entry != null) {
            String key = entry.getKey();
            TTask task = waittingTasks.remove(key);
            //是否存在同名任务
            if (runningTasks.containsKey(key)){
                //是否覆盖同名任务
                if (keyConflictPolicy == KEY_CONFLICT_POLICY_DISPLACE){
                    TTask oldTask = runningTasks.remove(key);
                    oldTask.cancel();
                    //启动任务
                    if (task.start()) {
                        //启动成功
                        runningTasks.put(key, task);
                        concurrencyVolume++;//并发数+1
                    }
                }else if(keyConflictPolicy == KEY_CONFLICT_POLICY_FOLLOW){
                    //新的任务跟随老的任务
                    runningTasks.get(key).addFollower(task);
                }else{
                    //新任务取消
                    task.cancel();
                }
            }else {
                //启动任务
                if (task.start()) {
                    //启动成功
                    runningTasks.put(key, task);
                    concurrencyVolume++;//并发数+1
                }
            }
        }
        return concurrencyVolume;
    }

    /**
     * 清除执行队列中 已完成/已取消的任务
     */
    private void cleanRunningTasks() {
        //遍历执行队列, 清除已完成的任务
        List<String> removeKeyList = new ArrayList<String>();
        for (Map.Entry<String, TTask> entry : runningTasks.entrySet()) {
            if (waitCancelingTask){
                //等待取消中任务模式下, 移除完成/已取消的任务
                if (entry.getValue().getState() == TTask.STATE_COMPLETE || entry.getValue().getState() == TTask.STATE_CANCELED) {
                    removeKeyList.add(entry.getKey());
                }
            }else {
                //移除完成/取消中/已取消的任务
                if (entry.getValue().getState() >= TTask.STATE_COMPLETE) {
                    removeKeyList.add(entry.getKey());
                }
            }
        }
        for (String key : removeKeyList) {
            runningTasks.remove(key);
        }
    }

    /**
     * 线程执行调度任务
     * @param runnable
     */
    private void executeDispatch(Runnable runnable){
        if (dispatchThreadPool == null){
            synchronized (this) {
                if (dispatchThreadPool == null) {
                    dispatchThreadPool = new LazySingleThreadPool();
                }
            }
        }
        try {
            dispatchThreadPool.execute(runnable);
        }catch(RejectedExecutionException ignored){
        }
    }

    /**************************************************************************
     * HANDLER
     * TQueue实例化在主线程, TTask启动时利用TQueue中的Handler, 将
     * onPreExecute和onPostExecute加入主线程队列执行
     */

    /**
     * [由TTask调用]<br/>
     * 用线程池执行任务
     *
     * @param runnable
     */
    protected void ttask_execute(Runnable runnable){
        if (taskThreadPool == null){
            synchronized (this) {
                if (taskThreadPool == null) {
                    taskThreadPool = Executors.newCachedThreadPool();
                }
            }
        }
        try {
            taskThreadPool.execute(runnable);
        }catch(RejectedExecutionException ignored){
        }
    }

    /**
     * [由TTask调用]<br/>
     * 将task推至主线程启动<br/>
     *
     * @param task
     */
    protected void ttask_postStart(TTask task){
        Message msg = mHandler.obtainMessage();
        msg.what = MyHandler.HANDLER_TASK_START;
        msg.obj = task;
        msg.sendToTarget();
    }

    /**
     * [由TTask调用]<br/>
     * 将task推至主线程完成<br/>
     *
     * @param task
     */
    protected void ttask_postComplete(TTask task){
        Message msg = mHandler.obtainMessage();
        msg.what = MyHandler.HANDLER_TASK_COMPLETE;
        msg.obj = task;
        msg.sendToTarget();
    }

    private final MyHandler mHandler = new MyHandler(Looper.getMainLooper(), this);//主线程处理

    private static class MyHandler extends WeakHandler<TQueue>{

        private static final int HANDLER_TASK_START = 0;//任务启动(主线程执行任务过程)
        private static final int HANDLER_TASK_COMPLETE = 1;//任务完成(主线程执行onPostExecute)

        public MyHandler(Looper looper, TQueue host) {
            super(looper, host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, TQueue host) {
            switch (msg.what) {
                case HANDLER_TASK_START:
                    if (msg.obj != null && msg.obj instanceof TTask) {
                        ((TTask)msg.obj).process();
                    }
                    break;
                case HANDLER_TASK_COMPLETE:
                    if (msg.obj != null && msg.obj instanceof TTask) {
                        ((TTask)msg.obj).afterProcess();
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
