package sviolet.liba.utils;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import android.annotation.SuppressLint;
import android.util.Base64;

/**
 * @author SunPeng Email:sunpeng2013@csii.com.cn
 */
public class DESUtils {

	private static final String CHARSET = "UTF-8";
	private static final String ALGORITHM = "DES";
	private static final String TRANSFORMATION = "DES";// DES/ECB/PKCS5Padding
	private static final String KEY = "CSIIYZBK";
	private static SecretKey secretkey = null;

	private static Key getKey() {
		if (secretkey == null) {
			byte[] key = null;
			try {
				key = KEY.getBytes(CHARSET);
				secretkey = new SecretKeySpec(key, ALGORITHM);
			} catch (Exception e) {
			}
		}
		return secretkey;
	}

	/**
	 * 加密
	 */
	@SuppressLint("TrulyRandom")
	public static String encrypt(String source) {
		String result = null;
		byte[] input = null;
		try {
			byte[] center = source.getBytes(CHARSET);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, getKey());
			input = cipher.doFinal(center);
			result = Base64.encodeToString(input, Base64.DEFAULT);
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * 解密
	 */
	public static String decrypt(String source) {
		String result = null;
		byte[] dissect = null;
		try {
			byte[] input = Base64.decode(source.getBytes(CHARSET),
					Base64.DEFAULT);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, getKey());
			dissect = cipher.doFinal(input);
			result = new String(dissect, CHARSET);
		} catch (Exception e) {
		}
		return result;
	}
}
