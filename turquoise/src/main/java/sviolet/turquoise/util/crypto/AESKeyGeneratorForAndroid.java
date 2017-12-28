/*
 * Copyright (C) 2015-2017 S.Violet
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

import sviolet.thistle.util.crypto.AESKeyGenerator;
import sviolet.thistle.util.crypto.DigestCipher;

/**
 * Android专用AES秘钥生成工具
 */
public class AESKeyGeneratorForAndroid {

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
	 * @param bits 秘钥位数(128)
	 */
	@SuppressLint("TrulyRandom")
	public static byte[] generate(byte[] seed, int bits) throws NoSuchProviderException{
		KeyGenerator keyGenerator;
		SecureRandom secureRandom;
		try {
			keyGenerator = KeyGenerator.getInstance(AESKeyGenerator.KEY_ALGORITHM);
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

}
