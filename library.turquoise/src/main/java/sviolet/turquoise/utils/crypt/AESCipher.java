package sviolet.turquoise.utils.crypt;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESCipher{
	
	//public static final String PADDING_MODE = "AES";
	public static final String PADDING_MODE = "AES/ECB/PKCS5Padding";
	//public static final String VIPARA = "0102030405060708"; 
	
	/**
	 * String-byte[]转换乱码解决：<p>
	 * 
	 * byte[] b = String.getByte("utf-8");//或gb2312<p>
	 * String s = new String(byte[],"utf-8");//或gb2312<p>
	 * 
	 */
	
	/**
	 * 生成随机密钥(不同系统平台相同password生成结果不同,跨平台可以使用MD5/SHA1生成密钥)<p>
	 * 
	 * android4.2以上(API17),系统改为真随机,使用该代码生成密钥每次都不同，需固定请使用sviolet.lib.android.crypt
	 * 
	 * @param password 随机种子
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] makeRandomKey(byte[] password) throws NoSuchAlgorithmException{
		KeyGenerator kgen = KeyGenerator.getInstance("AES"); 
		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");  
        secureRandom.setSeed(password); 
		kgen.init(128,secureRandom); 
		SecretKey secretKey = kgen.generateKey(); 
		return secretKey.getEncoded(); 
	}
	
	/**
	 * 加密
	 * 
	 * @param Content
	 * @param password
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	public static byte[] encrypt(byte[] Content, byte[] password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{ 
		//IvParameterSpec zeroIv =  new  IvParameterSpec(VIPARA.getBytes());  
		SecretKeySpec key = new SecretKeySpec(password, "AES"); 
		Cipher cipher = Cipher.getInstance(PADDING_MODE);// 创建密码器 
		cipher.init(Cipher.ENCRYPT_MODE,key);// 初始化 
		byte[] result = cipher.doFinal(Content); 
		return result; // 加密 
	} 
	
	/**
	 * 解密
	 * 
	 * @param content
	 * @param password
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	public static byte[] decrypt(byte[] content, byte[] password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{ 
		//IvParameterSpec zeroIv =  new  IvParameterSpec(VIPARA.getBytes());  
		SecretKeySpec key = new SecretKeySpec(password, "AES");             
		Cipher cipher = Cipher.getInstance(PADDING_MODE);// 创建密码器 
		cipher.init(Cipher.DECRYPT_MODE,key);// 初始化 
		byte[] result = cipher.doFinal(content); 
		return result; // 加密 
	}
}
