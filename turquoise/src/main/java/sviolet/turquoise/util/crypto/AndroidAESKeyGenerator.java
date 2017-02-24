/*
 * Copyright (C) 2015-2016 S.Violet
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

package sviolet.turquoise.util.crypto;

import android.annotation.SuppressLint;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Android专用AES秘钥生成工具
 */
public class AndroidAESKeyGenerator {

	/**
	 * <p>生成128位AES密钥(不同系统平台相同seed生成结果可能不同)</p>
	 *
	 * <p>android专用, 相同seed产生相同秘钥</p>
	 *
	 * @param seed 秘钥种子
	 */
	public static byte[] generate(byte[] seed) throws NoSuchProviderException {
		return generate(seed, 128);
	}

	/**
	 * <p>生成AES密钥(不同系统平台相同seed生成结果可能不同)</p>
	 *
	 * <p>android专用, 相同seed产生相同秘钥</p>
	 *
	 * @param seed 秘钥种子
	 * @param bits 秘钥位数(128/192/256)
	 */
	@SuppressLint("TrulyRandom")
	public static byte[] generate(byte[] seed, int bits) throws NoSuchProviderException{
		KeyGenerator keyGenerator;
		SecureRandom secureRandom;
		try {
			keyGenerator = KeyGenerator.getInstance(AESKeyGenerator.AES_KEY_ALGORITHM);
			//指定算法, 保证结果固定
			if (android.os.Build.VERSION.SDK_INT >=  17) {
				secureRandom = SecureRandom.getInstance("SHA1PRNG", "Crypto");
			} else {
				secureRandom = SecureRandom.getInstance("SHA1PRNG");
			}
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		secureRandom.setSeed(seed);
		keyGenerator.init(bits, secureRandom);
		SecretKey secretKey = keyGenerator.generateKey();
		return secretKey.getEncoded();
	}

	/**
	 * 利用SHA256摘要算法计算128位固定密钥, 安全性低, 但保证全平台一致
	 *
	 * @param seed 密码种子
	 */
	public static byte[] generateShaKey128(byte[] seed){
		byte[] sha = DigestCipher.digest(seed, DigestCipher.TYPE_SHA256);
		byte[] password = new byte[16];
		System.arraycopy(sha, 0, password, 0, 16);
		return password;
	}

	/**
	 * 利用SHA256摘要算法计算256位固定密钥, 安全性低, 但保证全平台一致
	 *
	 * @param seed 密码种子
	 */
	public static byte[] generateShaKey256(byte[] seed){
		return DigestCipher.digest(seed, DigestCipher.TYPE_SHA256);
	}
}
