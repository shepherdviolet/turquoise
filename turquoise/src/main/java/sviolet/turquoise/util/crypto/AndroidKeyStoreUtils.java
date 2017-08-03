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

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;

import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;

/**
 * TEE秘钥保存, 可配合FingerprintUtils进行指纹身份认证
 *
 * Created by S.Violet on 2017/8/2.
 */
public class AndroidKeyStoreUtils {

    /**
     * 在AndroidKeyStore中生成SHA256withRSA签名秘钥, 较慢, 建议放在子线程进行
     * @param keyStoreName 秘钥名称
     * @return 公钥
     * @throws KeyGenerateException 秘钥生成异常
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static PublicKey genRsaSha256SignKey(String keyStoreName) throws KeyGenerateException {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(
                            keyStoreName,
                            KeyProperties.PURPOSE_SIGN)
                            .setDigests(KeyProperties.DIGEST_SHA256)
                            .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                            .setUserAuthenticationRequired(true)
                            .build()
            );
            return keyPairGenerator.generateKeyPair().getPublic();
        } catch (Exception e) {
            throw new KeyGenerateException("Error while genRsaSha256SignKey", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Signature loadRsaSha256Signature(String keyStoreName) throws KeyLoadException, KeyNotFoundException {
        PrivateKey privateKey;
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            privateKey = (PrivateKey) keyStore.getKey(keyStoreName, null);
        } catch (Exception e) {
            throw new KeyLoadException("Error while load key from AndroidKeyStore", e);
        }
        if (privateKey == null){
            throw new KeyNotFoundException("Can't find " + keyStoreName + " in AndroidKeyStore");
        }
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            return signature;
        } catch (Exception e) {
            throw new KeyLoadException("Error while parsing key from AndroidKeyStore", e);
        }
    }

    /**
     * 在AndroidKeyStore中生成SHA256withECDSA签名秘钥, 较慢, 建议放在子线程进行
     * @param keyStoreName 秘钥名称
     * @return 公钥
     * @throws KeyGenerateException 秘钥生成异常
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static PublicKey genEccSha256SignKey(String keyStoreName) throws KeyGenerateException {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyPairGenerator keyPairGenerator =  KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(
                            keyStoreName,
                            KeyProperties.PURPOSE_SIGN)
                            .setDigests(KeyProperties.DIGEST_SHA256)
                            .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                            .setUserAuthenticationRequired(true)
                            .build()
            );
            return keyPairGenerator.generateKeyPair().getPublic();
        } catch (Exception e) {
            throw new KeyGenerateException("Error while genEccSha256SignKey", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Signature loadEccSha256Signature(String keyStoreName) throws KeyLoadException, KeyNotFoundException {
        PrivateKey privateKey;
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            privateKey = (PrivateKey) keyStore.getKey(keyStoreName, null);
        } catch (Exception e) {
            throw new KeyLoadException("Error while load key from AndroidKeyStore", e);
        }
        if (privateKey == null){
            throw new KeyNotFoundException("Can't find " + keyStoreName + " in AndroidKeyStore");
        }
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            return signature;
        } catch (Exception e) {
            throw new KeyLoadException("Error while parsing key from AndroidKeyStore", e);
        }
    }

    /**
     * 秘钥生成异常
     */
    public static class KeyGenerateException extends Exception {
        public KeyGenerateException(String message) {
            super(message);
        }
        public KeyGenerateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 秘钥加载异常
     */
    public static class KeyLoadException extends Exception {
        public KeyLoadException(String message) {
            super(message);
        }
        public KeyLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 秘钥不存在
     */
    public static class KeyNotFoundException extends Exception {
        public KeyNotFoundException(String message) {
            super(message);
        }
        public KeyNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
