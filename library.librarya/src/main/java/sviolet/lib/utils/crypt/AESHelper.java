package sviolet.lib.utils.crypt;

import java.io.UnsupportedEncodingException;
import java.security.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import sviolet.lib.utils.conversion.ByteJointUtils;

public class AESHelper extends AESCipher {
	public static final String CODE="utf-8";//utf-8/gb2312
	public static final int LOOPS = 500;
	
	public static byte[] encrypt(String in, byte[] password) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException{
		return encrypt(in.getBytes(CODE), password);
	}
	
	public static String decrypt2string(byte[] in, byte[] password) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
		return new String(decrypt(in, password),CODE);
	}

	/**
	 * 利用SHA1摘要算法获得128位固定密钥<p>
	 * 
	 * 该方法产生的密钥全平台一致(安全性有待考证)<p>
	 * 
	 * @param input 密码(建议10位以上)
	 * @return
	 */
	public static byte[] makeShaKey(String input){
		byte[] sha = DigestCipher.digest(input, DigestCipher.TYPE_SHA1);
		return makeShaKey(sha, LOOPS);
	}
	
	/**
	 * 利用SHA1摘要算法获得128位固定密钥
	 * 
	 * 该方法产生的密钥全平台一致(安全性有待考证)
	 * 
	 * @param input 密码(建议10位以上)
	 * @param loops 密码轮数(建议500,轮数越高强度越大速度越慢)
	 * @return
	 */
	public static byte[] makeShaKey(String input, int loops){
		byte[] sha = DigestCipher.digest(input, DigestCipher.TYPE_SHA1);
		return makeShaKey(sha, loops);
	}
	
	/**
	 * 利用SHA1摘要算法获得128位固定密钥
	 * 
	 * 该方法产生的密钥全平台一致(安全性有待考证)
	 * 
	 * @param input 密码(建议10位以上)
	 * @param loops 密码轮数(建议500,轮数越高强度越大速度越慢)
	 * @return
	 */
	public static byte[] makeShaKey(byte[] input, int loops){
		byte[] sha;
		byte[] password = new byte[16];
		
		for(int i = 0 ; i < loops ; i++){
			sha = DigestCipher.digest(input, DigestCipher.TYPE_SHA1);
			input = ByteJointUtils.joint(sha, input);
		}
		input = DigestCipher.digest(input, DigestCipher.TYPE_SHA1);
		int offset = 0;
		for(int i = 0 ; i < input.length ; i++){
			if((i + 1) % 5 != 0){
				password[offset] = input[i];
				offset++;
			}
		}
		return password;
	}
	
}
