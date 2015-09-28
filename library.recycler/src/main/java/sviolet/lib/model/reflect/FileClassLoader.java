package sviolet.lib.model.reflect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 从文件中加载class(支持解密,Android无法使用?)<p>
 * 
 * 例子:<p>
 * 
 * class文件存放位置:<p>
 * D:/TMP/class/test/classloader/Test.class<p>
 * 

			FileClassLoader fileClassLoader = new FileClassLoader("D:/TMP/class","class");//创建classloader对象
			Class<?> c = fileClassLoader.loadClass("test.classloader.Test");//加载class(程序里必须有test.classloader包)
			Constructor<?>[] constructor = c.getConstructors();//得到class所有的构造方法
//			Constructor<?> constructor = c.getConstructor(new Class[]{String.class,String.class});//得到参数为String,String的构造方法
			Method[] methods = c.getMethods();//获得class的所有方法
//			Method method = c.getMethod("getX", new Class[]{String.class,String.class});//获得class名为getX,参数为String,String的方法
//			method.invoke(c, new Object[]{"a","b"});//执行该方法传入a,b参数
			
//			LoaderInterface loaderInterface = (LoaderInterface)c.newInstance();//创建class的实例(用接口接收)
			LoaderInterface loaderInterface = (LoaderInterface)constructor[0].newInstance("a","b");//构造方法创建实例(传入参数)
			
			loaderInterface.run();//执行接口的方法
			for(Method method : methods){//可执行所有方法
				//执行
			}<p>
			
 *
 *	复写decrypt()方法可实现数据解密
 *
 * @author S.Violet (ZhuQinChao)
 *
 */
public class FileClassLoader extends ClassLoader {
	
	private String classFilePath;
	private String postfix;
	
	/**
	 * @param classFilePath class文件目录,例如:D:/classes
	 * @param postfix class文件后缀名,例如:class
	 */
	public FileClassLoader(String classFilePath, String postfix) {
		this.classFilePath = classFilePath;
		postfix = postfix.replace(".", "");
		this.postfix = postfix;
	}

	/**
	 * 复写findClass方法实现文件读取class
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		InputStream is = null;
		try{
			is = new FileInputStream(getClassFile(name));
			byte[] bytes = new byte[is.available()];
			is.read(bytes);
			is.close();
			return defineClass(name, decrypt(bytes), 0, bytes.length);
		}catch(IOException e){
			throw new ClassNotFoundException("class " + name + "not found");
		}
	}
	
	/**
	 * 根据class名得到对应的File对象
	 * 
	 * @param name
	 * @return
	 */
	private File getClassFile(String name){
		name = name.replace('.', File.separatorChar);
		return new File(classFilePath + File.separator + name + "." + postfix);
	}
	
	/**
	 * 需解密复写该方法
	 * 
	 * @param bytes
	 * @return
	 */
	public byte[] decrypt(byte[] bytes){
		return bytes;
	}
}
