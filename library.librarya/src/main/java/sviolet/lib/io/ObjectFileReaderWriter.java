package sviolet.lib.io;

/*
 * 《对象读写》
 * 
 * 被读写对象范例：
package svx.test.io;

import java.io.Serializable;

public class unit implements Serializable  {
	private static final long serialVersionUID = 1L;
	public String name="test";
	public int age=12;
}
 */

import java.io.*;

public class ObjectFileReaderWriter{
	
	File data;
	public boolean isNew=false;
	
	public ObjectFileReaderWriter(String path,String filename) throws IOException{
		File dir=new File(path);
		if(!dir.exists()){
			dir.mkdir();
		}
		data=new File(path+"/"+filename);
		if(!data.exists()){
			data.createNewFile();
			isNew=true;
		}
		else if(data.length()==0)isNew=true;
	}
	public void write(Object obj){
		ObjectOutputStream outs;
		try {
			outs=new ObjectOutputStream(new FileOutputStream(data));
			outs.writeObject(obj);
			outs.flush();
			outs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Object read(){
		Object obj = null;
		ObjectInputStream ins;
		try {
			ins=new ObjectInputStream(new FileInputStream(data));
			obj =ins.readObject();
			ins.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return obj;
	}
	public void delete(){
		if(data.exists()){
			data.delete();
		}
	}
}
