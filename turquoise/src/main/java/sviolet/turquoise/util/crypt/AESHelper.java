/*
 * Copyright (C) 2015 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.turquoise.util.crypt;

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
