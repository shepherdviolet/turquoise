package sviolet.sample;
/**
 * 线程安全的静态单例Demo<p>
 * final单例, 不可变更<p>
 * FinalSingletonDemo类加载时, 并不会实例化内部类FinalSingletonHolder, 
 * 只有当调用getInstance()时, FinalSingletonDemo.INSTANCE才会被实例化
 * 
 * @author S.Violet
 *
 */
public class FinalSingletonDemo {
	
	/**
	 * 私有静态内部类<p>
	 * @author S.Violet
	 */
	private static class FinalSingletonHolder{
		/**
		 * 利用final使得实例创建线程安全<p>
		 * 在内部类加载时, INSTANCE就已经实例化了, 因此不存在线程问题
		 */
		private static final FinalSingletonDemo INSTANCE = new FinalSingletonDemo();
	}
	
	/**
	 *私有构造函数<p>
	 * 初始化
	 */
	private FinalSingletonDemo(){
		//初始化操作
	}
	
	/**
	 * 获得实例
	 * @return
	 */
	public static FinalSingletonDemo getInstance(){
		return FinalSingletonHolder.INSTANCE;
	}
	
}
