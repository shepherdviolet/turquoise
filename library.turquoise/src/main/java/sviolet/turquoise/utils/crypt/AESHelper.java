package sviolet.turquoise.utils.crypt;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class AESHelper extends AESCipher {
	public static final String Code="utf-8";//utf-8/gb2312
	
	public static byte[] encrypt(String in, byte[] password) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException{
		return encrypt(in.getBytes(Code), password);
	}
	
	public static String decrypt2string(byte[] in, byte[] password) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
		return new String(decrypt(in, password),Code);
	}

	/**
	 * 利用SHA1摘要算法获得128位固定密钥<p>
	 * 
	 * 该方法产生的密钥全平台一致(安全性有待考证)
	 * 
	 * @param input
	 * @return
	 */
	public static byte[] makeShaKey(String input){
		byte[] sha = DigestCipher.digest(input, DigestCipher.TYPE_SHA1);
		byte[] password = new byte[16];
		int offset = 0;
		for(int i = 0 ; i < sha.length ; i++){
			if((i + 1) % 5 != 0){
				password[offset] = sha[i];
				offset++;
			}
		}
		return password;
	}
}
