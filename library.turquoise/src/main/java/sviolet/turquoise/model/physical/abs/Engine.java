package sviolet.turquoise.model.physical.abs;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 物理引擎接口<p>
 * 注意:运动停止后务必调用stop()<p>
 * 
 * [[与lite的区别]]本引擎物理算法在子线程中进行, 输出时通过handler调用UI线程处理onOutput
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */
public abstract class Engine {
	
	private Handle mHandle;
	private long messageCounter = 0;
	protected Inputer mInputer;
	protected Outputer mOutputer;
	private List<Block> blocks = new ArrayList<Block>();
	
	private boolean isStatic = true;//是否静止状态
	
	/**
	 * 输入(Inputer绑定后自动调用)
	 * 
	 * @param time
	 */
	public void input(long time){
		isStatic = false;
		if(mHandle == null || mHandle.isReleased())
			onInput(time);
	}
	
	/**
	 * 通知UI线程执行onStop()方法
	 */
	protected void output(){
		messageCounter++;
		handler.sendEmptyMessage(WHAT_OUTPUT);
	}
	
	/**
	 * 停止(Inputer/Outputer回调onStop(),handler清空)
	 */
	public void stop(){
		onStop();
	}
	
	/**
	 * 销毁(Inputer回调onStop,handler清空)<p>
	 * 不回调Outputer的onStop
	 */
	public void destroy(){
		if(mInputer != null)
			mInputer.onStop();
		handler.removeCallbacksAndMessages(null);
	}
	
	////////////////////////////////////////////////////////
	
	/**
	 * 输入接口(当输入时调用)
	 * @param time
	 */
	protected abstract void onInput(long time);
	
	/**
	 * 输出接口(当输出时调用)
	 */
	public abstract void onOutput();
	
	/**
	 * 当停止时处理(I/O回调onStop(),handler清空)
	 */
	public void onStop(){
		if(mInputer != null)
			mInputer.onStop();
		if(mOutputer != null)
			mOutputer.onStop();
		handler.removeCallbacksAndMessages(null);
		isStatic = true;
	}
	
	/**
	 * 当触摸移动时
	 */
	public abstract void onHandleMove(float step);
	
	/**
	 * 当触摸松开时
	 */
	public abstract void onHandleRelease(float acceleration);
	
////////////////////////////////////////////////////////
	
	/**
	 * 绑定输入器
	 */
	public void setInputer(Inputer mInputer){
		this.mInputer = mInputer;
		this.mInputer.setEngine(this);
	}
	
	/**
	 * 绑定输出器
	 * 
	 * @param mOutputer
	 */
	public void setOutputer(Outputer mOutputer){
		this.mOutputer = mOutputer;
	}
	
	/**
	 * 绑定手势输入(把手)
	 */
	public void setHandle(Handle mHandle){
		this.mHandle = mHandle;
		this.mHandle.setEngine(this);
	}
	
	////////////////////////////////////////////////////////
	
	/**
	 * 添加一个阻碍区块
	 * @param block
	 */
	public void addBlock(Block block){
		blocks.add(block);
	}
	
	/**
	 * 清除所有阻碍区块
	 */
	public void removeAllBlocks(){
		blocks.clear();
	}
	
	////////////////////////////////////////////////////////
	
	/**
	 * 是否静止状态
	 * @return
	 */
	public boolean isStatic(){
		return isStatic;
	}
	
	////////////////////////////////////////////////////////
	
	private final int WHAT_OUTPUT = 1;
	
	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			messageCounter--;
			if(messageCounter > 0)
				return;
			
			synchronized (Engine.this) {
				switch (msg.what) {
				case WHAT_OUTPUT:
					onOutput();
					break;
				default:
					break;
				}
			}
		}
	};
}
