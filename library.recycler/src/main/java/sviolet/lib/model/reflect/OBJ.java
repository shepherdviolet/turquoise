package sviolet.lib.model.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 
 * Object对象反射助手<p>
 * new Class[]{String.class, integer.class}<p>
 * new Object[]{"test", new Integer(1)}<p>
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

public class OBJ {
	
	private Object obj;
	
	/**
	 * [static]根据类名获得实例
	 * 
	 * @param className
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static Object newInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		Class<?> c = Class.forName(className);
		return newInstance(c);
	}
	
	/**
	 * [static]根据类名获得实例(含参数构造器)
	 * 
	 * @param className
	 * @param params
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 */
	public static Object newInstance(String className, Object[] params) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Class<?> c = Class.forName(className);
		return newInstance(c, params);
	}
	
	/**
	 * [static]根据类获得实例
	 * 
	 * @param c
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static Object newInstance(Class<?> c) throws InstantiationException, IllegalAccessException{
		return c.newInstance();
	}
	
	/**
	 * 
	 * [static]根据类获得实例(含参数构造器)
	 * 
	 * @param c
	 * @param params
	 * @return
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static Object newInstance(Class<?> c, Object[] params) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Class<?>[] paramTypes = new Class[params.length];
		for(int i = 0 ; i < paramTypes.length ; i++){
			paramTypes[i] = params[i].getClass();
		}
		Constructor<?> constructor = c.getDeclaredConstructor(paramTypes);
		return constructor.newInstance(params);
	}
	
	public OBJ(Object obj){
		this.obj = obj;
	}
	
	public Object getObject(){
		return this.obj;
	}
	
	/**
	 * 执行Object的方法
	 * 
	 * @param name
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Object invoke(String name) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Method method = obj.getClass().getDeclaredMethod(name);
		method.setAccessible(true);//取消检查提高速度
		return method.invoke(obj);
	}
	
	/**
	 * 执行Object的方法
	 * 
	 * @param name
	 * @param param1
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Object invoke(String name, Object param1) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Method method = obj.getClass().getDeclaredMethod(name, param1.getClass());
		method.setAccessible(true);//取消检查提高速度
		return method.invoke(obj, param1);
	}
	
	/**
	 * 执行Object的方法
	 * 
	 * @param name
	 * @param param1
	 * @param param2
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Object invoke(String name, Object param1, Object param2) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Method method = obj.getClass().getDeclaredMethod(name, param1.getClass(), param2.getClass());
		method.setAccessible(true);//取消检查提高速度
		return method.invoke(obj, param1, param2);
	}
	
	/**
	 * 执行Object的方法
	 * 
	 * @param name
	 * @param param1
	 * @param param2
	 * @param param3
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Object invoke(String name, Object param1, Object param2, Object param3) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Method method = obj.getClass().getDeclaredMethod(name, param1.getClass(), param2.getClass(), param3.getClass());
		method.setAccessible(true);//取消检查提高速度
		return method.invoke(obj, param1, param2, param3);
	}
	
	/**
	 * 执行Object的方法,
	 * obj.invoke("run", new Object[]{"test","test2"});
	 * 
	 * @param name
	 * @param params
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public Object invoke(String name, Object[] params) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Class<?>[] paramTypes = new Class[params.length];
		for(int i = 0 ; i < paramTypes.length ; i++){
			paramTypes[i] = params[i].getClass();
		}
		Method method = obj.getClass().getDeclaredMethod(name, paramTypes);
		method.setAccessible(true);//取消检查提高速度
		return method.invoke(obj, params);
	}
	
	/**
	 * 获得Object的成员变量
	 * 
	 * @param name
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public Object get(String name) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		Field field = obj.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.get(obj);
	}
	
	/**
	 * 给Object的成员变量赋值
	 * 
	 * @param name
	 * @param value
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void set(String name, Object value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		Field field = obj.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(obj, value);
	}
	
	/**
	 * 取得父类
	 * @return
	 */
	public Class<?> getSuperClass(){
		return obj.getClass().getSuperclass();
	}
	
	/**
	 * 取得类接口
	 * @return
	 */
	public Class<?>[] getInterfaces(){
		return obj.getClass().getInterfaces();
	}
	
	/**
	 * 取得成员公有属性
	 * @return
	 */
	public Field[] getFields(){
		return obj.getClass().getFields();
	}
	
	public String getClassName(){
		return obj.getClass().getName();
	}
	
	public String getClassSimpleName(){
		return obj.getClass().getSimpleName();
	}
}
