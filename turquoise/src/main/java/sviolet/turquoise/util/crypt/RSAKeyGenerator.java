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

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class RSAKeyGenerator {

	RSAPublicKey publicKey = null;
	RSAPrivateKey privateKey = null;
	
	/**
	 * 生成公钥私钥对(1024位 加密模式)
	 * @throws NoSuchAlgorithmException 
	 */
	public RSAKeyGenerator() throws NoSuchAlgorithmException{
		this(1024);
	}
	
	/**
	 * 生成公钥私钥对
	 * @throws NoSuchAlgorithmException 
	 * @param keySize RSA密钥长度
	 */
	public RSAKeyGenerator(int keySize) throws NoSuchAlgorithmException{
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(keySize);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
	}
	
	/**
	 * 获取公钥
	 */
	public RSAPublicKey getPublicKey(){
		return publicKey;
	}
	
	/**
	 * 获取私钥
	 */
	public RSAPrivateKey getPrivateKey(){
		return privateKey;
	}
	
	/**
	 * 获取模数
	 */
	public BigInteger getModulus(){
		return publicKey.getModulus();
	}
	
	/**
	 * 获取公钥指数
	 */
	public BigInteger getPublicExponent(){
		return publicKey.getPublicExponent();
	}
	
	/**
	 * 获取私钥指数
	 */
	public BigInteger getPrivateExponent(){
		return privateKey.getPrivateExponent();
	}
	/**
	 * 用模和指数生成RSA公钥(不影响对象内部密钥对)
	 * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA/None/NoPadding】
	 * 
	 * @param modulus 模
	 * @param exponent 指数
	 * @return
	 */
	public static RSAPublicKey makePublicKey(BigInteger modulus, BigInteger exponent) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
			return (RSAPublicKey) keyFactory.generatePublic(keySpec);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 用模和指数生成RSA私钥(不影响对象内部密钥对)
	 * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA/None/NoPadding】
	 * 
	 * @param modulus 模
	 * @param exponent 指数
	 * @return
	 */
	public static RSAPrivateKey makePrivateKey(BigInteger modulus, BigInteger exponent) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, exponent);
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
