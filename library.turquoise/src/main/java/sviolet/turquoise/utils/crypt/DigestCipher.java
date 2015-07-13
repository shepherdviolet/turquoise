package sviolet.turquoise.utils.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestCipher {
	
	public static final String TYPE_MD5 = "MD5";
	public static final String TYPE_SHA1 = "SHA1";
	
	private static final String ENCODE = "utf-8";
	
	/**
	 * 摘要byte[]
	 * 
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] digest(byte[] input,String type){
		MessageDigest cipher;
		try {
			cipher = MessageDigest.getInstance(type);
			return cipher.digest(input);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 摘要字符串
	 * 
	 * @param input
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] digest(String input,String type){
		MessageDigest cipher;
		try {
			cipher = MessageDigest.getInstance(type);
			return cipher.digest(input.getBytes(ENCODE));
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 摘要文件
	 * 
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] digest(File file,String type){
		FileInputStream fis = null;
		MessageDigest cipher;
		try {
			cipher = MessageDigest.getInstance(type);
			fis = new FileInputStream(file);
			byte[] buffer = new byte[2048];
			int length = -1;
			while((length = fis.read(buffer)) != -1)
				cipher.update(buffer, 0, length);
			return cipher.digest();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
