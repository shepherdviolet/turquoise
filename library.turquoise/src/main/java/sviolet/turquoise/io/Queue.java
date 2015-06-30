package sviolet.turquoise.io;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务队列<br>
 * <br>
 * 任务按顺序/优先级执行, 并允许限制任务并发量<br>
 * 调用addTask()方法加入任务并自动执行<br>
 * 
 * @author S.Violet
 *
 */

public class Queue {
	
	//Setting//////////////////////////////////////////////////////
	
	private int concurrencyVolumeMax = 3;//最大并发量
	
	//Variable//////////////////////////////////////////////////////
	
	private List<Task> taskList = new ArrayList<Task>();//任务队列
	
	private int dispatchCounter = 0;//防止dispatch重复执行
	
	/*****************************************************
	 * Public
	 */
	
	/**
	 * 默认最大并发量3
	 */
	public Queue(){
		
	}
	
	/**
	 * @param concurrencyVolumeMax 最大并发量
	 */
	public Queue(int concurrencyVolumeMax){
		setConcurrencyVolumeMax(concurrencyVolumeMax);
	}
	
	/**
	 * 队列中增加任务并执行
	 * @param task
	 */
	public Task addTask(Task task){
		task.setQueue(this);//给任务设置队列对象(用于回调)
		taskList.add(task);//队列中加入任务
		notifyDispatchTask();//触发任务调度
		return task;
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
				synchronized (Queue.this) {
					dispatchTask();
				}
			}
		}).start();;
	}
	
	/**
	 * 获取队列中的任务数<br>
	 * 总任务数, 包括等待/开始/执行/结束(尚未被清除)的任务<br>
	 * @return
	 */
	public int getTaskVolume(){
		return taskList.size();
	}
	
	/**
	 * 获取当前并发量(正在执行的任务数)<br>
	 * @return
	 */
	public int getConcurrencyVolume(){
		int count = 0;
		//遍历任务队列, 对开始和执行状态计数
		for(Task task : taskList){
			if(task.getState() == Task.STATE_STARTING || task.getState() == Task.STATE_RUNNING)
				count++;
		}
		return count;
	}
	
	/**
	 * 设置最大并发量(队列允许的最大并发量)
	 * @param concurrencyVolumeMax
	 */
	public void setConcurrencyVolumeMax(int concurrencyVolumeMax){
		this.concurrencyVolumeMax = concurrencyVolumeMax;
	}
	
	/**
	 * 获取最大并发量参数(队列允许的最大并发量)
	 * @return
	 */
	public int getConcurrencyVolumeMax(){
		return concurrencyVolumeMax;
	}

	/**
	 * 取消所有包含tag标签的任务
	 */
	public void cancelAll(Object tag){
		synchronized (Queue.this) {
			try{
				for(Task task : taskList){
					if (task.containTag(tag))
						task.cancel();
				}
			}catch(Exception e){
			}
		}
	}

	/**
	 * 取消所有任务
	 */
	public void cancelAll(){
		synchronized (Queue.this) {
			try{
				for(Task task : taskList){
					task.cancel();
				}
			}catch(Exception e){
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
		//遍历任务队列, 清除已完成的任务
		for(int i = 0 ; i < taskList.size() ; i++){
			Task task = taskList.get(i);
			if(task.getState() == Task.STATE_COMPLETE){
				taskList.remove(i);
				i--;
			}
		}
		//当前并发数
		int concurrencyVolume = getConcurrencyVolume();
		//唤醒等待的任务
		//double-check
		while(taskList.size() - concurrencyVolume > 0 && concurrencyVolume < concurrencyVolumeMax){
			while(taskList.size() - concurrencyVolume > 0 && concurrencyVolume < concurrencyVolumeMax){
				int index = highestPriorityTaskIndex();//取最先执行的等待任务
				//无可执行任务, 结束调度
				if(index < 0){
					dispatchCounter--;
					return;
				}
				taskList.get(index).start();//执行任务
				concurrencyVolume++;//并发数+1
			}
			//当前并发数
			concurrencyVolume = getConcurrencyVolume();
		}
		dispatchCounter--;//消费掉计数
	}
	
	/**
	 * 查找优先度最高的等待任务<br>
	 * <br>
	 * 从任务列表中, 找到优先度最高的等待任务(最先执行)<br>
	 */
	private int highestPriorityTaskIndex(){
		int index = -1;
		int highestPriority = Integer.MIN_VALUE;
		for(int i = 0 ; i < taskList.size() ; i++){
			Task task = taskList.get(i);
			if(task.getState() == Task.STATE_WAITTING && task.getPriority() > highestPriority){
				index = i;
				highestPriority = task.getPriority();
			}
		}
		return index;
	}
	
}
