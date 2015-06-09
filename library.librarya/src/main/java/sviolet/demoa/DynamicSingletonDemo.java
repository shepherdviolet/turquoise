package sviolet.demoa;

/**
 * 线程安全的动态单例Demo<p>
 * 单例可变更
 * 
 * @author S.Violet
 * 
 */
public class DynamicSingletonDemo {

	/**
	 * 静态实例<br>
	 * 
	 * volatile声明:<p>
	 * 1.声明该对象为多变的, 为多线程所操作的, 直接从内存读取, 不会从线程副本中读取<p>
	 * 2.禁止指令重排序优化, 对该对象的读操作不会被重排序到赋值操作前<br>
	 * 
	 * 实例化过程(new)并非原子操作, 实际分为三个步骤:<p>
	 * 1.分配内存<p>
	 * 2.调用构造函数初始化成员变量, 形成实例<p>
	 * 3.将实例指向分配的内存<p>
	 * volatile声明可以防止2-3之间的重排序
	 */
	private volatile static DynamicSingletonDemo INSTANCE = null;
	
	/**
	 *私有构造函数<p>
	 * 初始化
	 */
	private DynamicSingletonDemo(){
		//初始化操作
	}
	
	/**
	 * 获取实例<p>
	 * 利用synochronized声明保证线程安全<p>
	 * @return
	 */
	public static DynamicSingletonDemo getInstance(){
		//Double-Check 既保证实例化过程中的线程同步, 又使得实例化后无需再加锁
		if(INSTANCE == null){
			synchronized (DynamicSingletonDemo.class) {
				if(INSTANCE == null){
					INSTANCE = new DynamicSingletonDemo();
				}
			}
		}
		return INSTANCE;
	}
	
	/**
	 * [拓展]<p>
	 * 重置实例<p>
	 * 重新创建不同的实例
	 */
	public static void resetInstance(){
		synchronized (DynamicSingletonDemo.class) {
			//原实例资源回收
			//......
			//置空
			INSTANCE = null;
			//创建新实例
			INSTANCE = new DynamicSingletonDemo();
		}
	}
	
}
