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
 */

package sviolet.turquoise.utils.crypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSACipher {
	
	public static final String PADDING_MODE = "RSA/ECB/PKCS1Padding";
	//public static final String PADDING_MODE = "RSA/ECB/NoPadding";
	//public static final String PADDING_MODE = "RSA";
	public static final String SIGN_MODE = "MD5withRSA";
	//public static final String SIGN_MODE = "SHA1withRSA";
	
	/**
	 * String-byte[]转换乱码解决：
	 * 
	 * byte[] b = String.getByte("gb2312");//或utf-8
	 * String s = new String(byte[],"gb2312");//或utf-8
	 * 
	 */
	  
    /**
     * 
     * 用私钥对信息生成数字签名 <p>
     *  
     * @param data 需要签名的明文
     * @param privateKey 私钥
     *  
     * @return 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     * @throws SignatureException 
     */  
    public static byte[] sign(byte[] data, RSAPrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{  
        Signature signature = Signature.getInstance("MD5withRSA");  
        signature.initSign(privateKey);
        signature.update(data);  
        return signature.sign();
    }  
  
    /** *//** 
     * <p> 
     * 校验数字签名 
     * </p> 
     *  
     * @param data 需要签名的明文
     * @param publicKey 公钥
     * @param sign 数字签名 
     *  
     * @return 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     * @throws SignatureException 
     *  
     */  
    public static boolean verify(byte[] data, RSAPublicKey publicKey, byte[] sign) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{  
        Signature signature = Signature.getInstance("MD5withRSA");  
        signature.initVerify(publicKey);  
        signature.update(data);  
        return signature.verify(sign);  
    }  

    /** *//** 
     * <P> 
     * 私钥解密 
     * </p> 
     *  
     * @param data 已加密数据
     * @param privateKey 私钥
     * @return 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws IOException 
     */  
    public static byte[] decrypt(byte[] data, RSAPrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException{
    	
        Cipher cipher = Cipher.getInstance(PADDING_MODE);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        
        int inputLen = data.length;  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        int offSet = 0;  
        byte[] cache;  
        int i = 0;
        int block = privateKey.getModulus().bitLength() / 8;//解密块和密钥等长
        
        // 对数据分段解密
        while (inputLen - offSet > 0) {  
            if (inputLen - offSet > block) {  
                cache = cipher.doFinal(data, offSet, block);  
            } else {  
                cache = cipher.doFinal(data, offSet, inputLen - offSet);  
            }  
            out.write(cache, 0, cache.length);  
            i++;  
            offSet = i * block;  
        }  
        byte[] decryptedData = out.toByteArray();  
        out.close();  
        return decryptedData;  
    }  
 
    /** *//** 
     * <p> 
     * 公钥加密 
     * </p> 
     *  
     * @param data 源数据 
     * @param publicKey 公钥
     * @return 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws IOException 
     */  
	public static byte[] encrypt(byte[] data, RSAPublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException{  
        Cipher cipher = Cipher.getInstance(PADDING_MODE);  
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        
        int inputLen = data.length;  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        int offSet = 0;  
        byte[] cache;  
        int i = 0;
        int block = publicKey.getModulus().bitLength() / 8 - 11;//加密块比密钥长度小11
        
        // 对数据分段加密  
        while (inputLen - offSet > 0) {  
            if (inputLen - offSet > block) {  
                cache = cipher.doFinal(data, offSet, block);  
            } else {  
                cache = cipher.doFinal(data, offSet, inputLen - offSet);  
            }  
            out.write(cache, 0, cache.length);  
            i++;  
            offSet = i * block;  
        }  
        byte[] encryptedData = out.toByteArray();  
        out.close();  
        return encryptedData;  
    }  
    
    /** *//** 
     * <P> 
     * 公钥解密*
     * </p> 
     *  
     * @param data 已加密数据
     * @param publicKey 公钥
     * @return 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws IOException 
     */  
    public static byte[] decrypt(byte[] data, RSAPublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException{
    	
        Cipher cipher = Cipher.getInstance(PADDING_MODE);
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        
        int inputLen = data.length;  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        int offSet = 0;  
        byte[] cache;  
        int i = 0;
        int block = publicKey.getModulus().bitLength() / 8;//解密块和密钥等长
        
        // 对数据分段解密
        while (inputLen - offSet > 0) {  
            if (inputLen - offSet > block) {  
                cache = cipher.doFinal(data, offSet, block);  
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);  
            }  
            out.write(cache, 0, cache.length);  
            i++;  
            offSet = i * block;  
        }  
        byte[] decryptedData = out.toByteArray();  
        out.close();  
        return decryptedData;  
    }  
 
    /** *//** 
     * <p> 
     * 私钥加密*
     * </p> 
     *  
     * @param data 源数据 
     * @param privateKey 私钥
     * @return 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws IOException 
     */  
    public static byte[] encrypt(byte[] data, RSAPrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException{  
        Cipher cipher = Cipher.getInstance(PADDING_MODE);  
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        
        int inputLen = data.length;  
        ByteArrayOutputStream out = new ByteArrayOutputStream();  
        int offSet = 0;  
        byte[] cache;  
        int i = 0;
        int block = privateKey.getModulus().bitLength() / 8 - 11;//加密块比密钥长度小11
        
        // 对数据分段加密  
        while (inputLen - offSet > 0) {  
            if (inputLen - offSet > block) {  
                cache = cipher.doFinal(data, offSet, block);  
            } else {  
                cache = cipher.doFinal(data, offSet, inputLen - offSet);  
            }  
            out.write(cache, 0, cache.length);  
            i++;  
            offSet = i * block;  
        }  
        byte[] encryptedData = out.toByteArray();  
        out.close();  
        return encryptedData;  
    }  
}
