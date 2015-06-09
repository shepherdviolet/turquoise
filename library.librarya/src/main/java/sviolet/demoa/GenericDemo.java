package sviolet.demoa;

import java.lang.reflect.ParameterizedType;

/**
 * 泛型示例
 * @author S.Violet
 *
 */
public class GenericDemo <T> {

	private Class<T> classT;
	
	/**
	 * 得到子类声明的泛型类型的Class
	 */
	@SuppressWarnings("unchecked")
	public GenericDemo(){
		classT = (Class<T>)((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	/**
	 * 创建classT的实例
	 * 
	 * @return
	 */
	public T getInstance(){
		try {
			return classT.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
