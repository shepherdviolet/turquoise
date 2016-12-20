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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * DES加密工具
 *
 * Created by S.Violet on 2016/12/20.
 */
public class DESCipher {

    public static final String CRYPTO_TRANSFORMATION_DES = "DES";
    public static final String CRYPTO_TRANSFORMATION_DES_EDE_ECB_NOPADDING = "DESede/ECB/NoPadding";
    public static final String CRYPTO_TRANSFORMATION_DES_EDE_ECB_PKCS5 = "DESede/ECB/PKCS5Padding";

    /**
     * @param data 数据
     * @param keyData 秘钥数据
     * @param algorithm 算法
     */
    public static byte[] encrypt(byte[] data, byte[] keyData, String algorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKey keyInstance = new SecretKeySpec(keyData, algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keyInstance);
        return cipher.doFinal(data);
    }

    /**
     * @param data 数据
     * @param keyData 秘钥数据
     * @param algorithm 算法
     */
    public static byte[] decrypt(byte[] data, byte[] keyData, String algorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKey keyInstance = new SecretKeySpec(keyData, algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, keyInstance);
        return cipher.doFinal(data);
    }

    /**
     * @param data 数据
     * @param desedeKey 秘钥数据, hexString
     * @param algorithm 算法, 这里只能用DESede
     */
    public static byte[] encryptByDesedeKey(byte[] data, String desedeKey, String algorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
        DESedeKeySpec desedeKeySpec = new DESedeKeySpec(desedeKey.getBytes("UTF-8"));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        SecretKey keyInstance = keyFactory.generateSecret(desedeKeySpec);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keyInstance);
        return cipher.doFinal(data);
    }

    /**
     * @param data 数据
     * @param desedeKey 秘钥数据, hexString
     * @param algorithm 算法, 这里只能用DESede
     */
    public static byte[] decryptByDesedeKey(byte[] data, String desedeKey, String algorithm) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, UnsupportedEncodingException {
        DESedeKeySpec desedeKeySpec = new DESedeKeySpec(desedeKey.getBytes("UTF-8"));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        SecretKey keyInstance = keyFactory.generateSecret(desedeKeySpec);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, keyInstance);
        return cipher.doFinal(data);
    }

}
