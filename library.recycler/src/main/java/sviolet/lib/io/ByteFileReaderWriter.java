package sviolet.lib.io;

/*
 * 《byte读写》
 */

import java.io.*;

public class ByteFileReaderWriter{
	
	File data;
	public boolean isNew=false;
	
	public ByteFileReaderWriter(String path,String filename) throws IOException{
		File dir=new File(path);
		if(!dir.exists()){
			dir.mkdirs();
		}
		data=new File(path+"/"+filename);
		if(!data.exists()){
			data.createNewFile();
			isNew=true;
		}
		else if(data.length()==0)isNew=true;
	}
	public void write(byte[] bytes) throws IOException{
		FileOutputStream outs;
		outs = new FileOutputStream(data);
		outs.write(bytes);
		outs.flush();
		outs.close();
	}
	public byte[] read() throws IOException{
		FileInputStream in=new FileInputStream(data);  
		ByteArrayOutputStream out=new ByteArrayOutputStream(1024);  
		byte[] temp=new byte[1024];  
		int size=0;  
		while((size=in.read(temp))!=-1)  
		{  
			out.write(temp,0,size);  
		}  
		byte[] bytes=out.toByteArray();  
		in.close(); 
		out.flush();
		out.close();
		return bytes;  
	}
	public void delete(){
		if(data.exists()){
			data.delete();
		}
	}
}
