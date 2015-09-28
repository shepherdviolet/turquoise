package sviolet.liba.view.physical.model;

/**
 * 
 * 物理引擎输入器<p>
 * 
 * [[与lite的区别]]本引擎物理算法在子线程中进行, 输出时通过handler调用UI线程处理onOutput
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

public abstract class Inputer {
	
	private Engine mEngine;
	
	/**
	 * 停止输入回调
	 */
	protected abstract void onStop();
	
	/**
	 * 加入引擎
	 * 
	 * @param mEngine
	 */
	protected void setEngine(Engine mEngine){
		this.mEngine = mEngine;
	}
	
	/**
	 * 向引擎输入
	 * 
	 * @param time
	 */
	public void input(long time){
		if(mEngine != null && time > 0){
			mEngine.input(time);
		}
	}
}
