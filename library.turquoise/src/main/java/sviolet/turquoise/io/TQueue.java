package sviolet.turquoise.io;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务队列<br/>
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
	
	//Setting//////////////////////////////////////////////////////

	private boolean reverse = false;//逆序
	private int concurrencyVolumeMax = 1;//最大并发量
    private int volumeMax = Integer.MAX_VALUE;//最大任务量
	
	//Variable//////////////////////////////////////////////////////
	
	private LinkedHashMap<String, TTask> waittingTasks;//等待队列
    private LinkedHashMap<String, TTask> runningTasks;//执行队列
	
	private int dispatchCounter = 0;//防止dispatch重复执行

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
	 * 队列中增加任务并执行
	 * @param task
	 */
	public TTask put(final String key, final TTask task){
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncPut(key, task);
            }
        }).start();
		return task;
	}

    /**
     * [同步]队列中增加任务并执行<br/>
     * 线程同步操作, 可能会阻塞<br/>
     * @param task
     */
    public void syncPut(String key, TTask task) {
        //给任务设置队列对象(用于回调)
        task.setQueue(this);

        synchronized (TQueue.this){
            //等待队列超出限制时, 清除优先级最低的任务
            if (waittingTasks.size() >= volumeMax){
                if (reverse){
                    //取队列顶部(最早任务)
                    Map.Entry<String, TTask> firstEntry = null;
                    for (Map.Entry<String, TTask> entry : waittingTasks.entrySet()) {
                        firstEntry = entry;
                        break;
                    }
                    TTask cancelTask = waittingTasks.remove(firstEntry.getKey());
                    cancelTask.cancel();
                }else{
                    //取队列底部(最新任务)
                    Map.Entry<String, TTask> lastEntry = null;
                    for (Map.Entry<String, TTask> entry : waittingTasks.entrySet()) {
                        lastEntry = entry;
                    }
                    TTask cancelTask = waittingTasks.remove(lastEntry.getKey());
                    cancelTask.cancel();
                }
            }
            waittingTasks.put(key, task);//加入等待队列
        }

        notifyDispatchTask();//触发任务调度
    }

    /**
	 * 通知队列进行调度<br>
	 * <br>
	 * 同时多次调用只会执行一次<br>
	 * Task会自动触发该方法<br>
	 */
	public void notifyDispatchTask(){
		dispatchCounter++;//调用计数
		//开启线程执行调度任务
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (TQueue.this) {
					dispatchTask();
				}
			}
		}).start();
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
                if (task.getState() == TTask.STATE_STARTING || task.getState() == TTask.STATE_RUNNING)
                    count++;
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
	 * 取消指定标签的任务
	 */
	public void cancel(final String key){
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncCancel(key);
            }
        }).start();
	}

    /**
     * [同步]取消指定标签的任务<br/>
     * 线程同步操作, 可能会阻塞<br/>
     */
    public void syncCancel(String key) {
        synchronized (TQueue.this) {
            if (runningTasks.containsKey(key)){
                TTask task = runningTasks.remove(key);
                if (task != null)
                    task.cancel();
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
        new Thread(new Runnable() {
            @Override
            public void run() {
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
        }).start();
	}

    /**
     * 优先处理指定任务<br/>
     * 将指定任务排到第一位<br/>
     *
     * @param key
     */
    public void preferred(final String key){
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncPreferred(key);
            }
        }).start();
    }

    /**
     * [同步]优先处理指定任务<br/>
     * 将指定任务排到第一位<br/>
     * 线程同步操作, 可能会阻塞<br/>
     *
     * @param key
     */
    public void syncPreferred(String key) {
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
		//过滤多次调度请求
		if(dispatchCounter > 1){
			dispatchCounter--;//消费掉计数
			return;
		}
		//遍历执行队列, 清除已完成的任务
        List<String> removeKeyList = new ArrayList<String>();
        for (Map.Entry<String, TTask> entry : runningTasks.entrySet()) {
            if (entry.getValue().getState() == TTask.STATE_COMPLETE){
                removeKeyList.add(entry.getKey());
            }
        }
        for (String key : removeKeyList) {
            runningTasks.remove(key);
        }
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
                    if (lastEntry != null) {
                        String key = lastEntry.getKey();
                        TTask task = waittingTasks.remove(key);
                        runningTasks.put(key, task);
                        task.start();
                        concurrencyVolume++;//并发数+1
                    }
                }else{
                    //取队列顶部(最早任务)
                    Map.Entry<String, TTask> firstEntry = null;
                    for (Map.Entry<String, TTask> entry : waittingTasks.entrySet()) {
                        firstEntry = entry;
                        break;
                    }
                    if (firstEntry != null){
                        String key = firstEntry.getKey();
                        TTask task = waittingTasks.remove(key);
                        runningTasks.put(key, task);
                        task.start();
                        concurrencyVolume++;//并发数+1
                    }
                }
			}
            //检查调度完成后, 理论并发数是否等于实际并发数
            if (concurrencyVolume != getCurrentConcurrencyVolume()){
                //遍历执行队列, 清除已完成的任务
                List<String> keyList = new ArrayList<String>();
                for (Map.Entry<String, TTask> entry : runningTasks.entrySet()) {
                    if (entry.getValue().getState() == TTask.STATE_COMPLETE){
                        keyList.add(entry.getKey());
                    }
                }
                for (String key : keyList) {
                    runningTasks.remove(key);
                }
                //当前执行队列任务数
                concurrencyVolume = getCurrentRunningVolume();
            }
		}
		dispatchCounter--;//消费掉计数
	}
	
}
