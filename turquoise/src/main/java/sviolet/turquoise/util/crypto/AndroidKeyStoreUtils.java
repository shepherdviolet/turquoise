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

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * TEE秘钥保存
 * Created by S.Violet on 2017/8/2.
 */
public class AndroidKeyStoreUtils {

    /**
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static KeyPair generateECCKey(String keyStoreName) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException, IOException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        KeyPairGenerator keyPairGenerator =  KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        keyPairGenerator.initialize(
                new KeyGenParameterSpec.Builder(
                        keyStoreName,
                        KeyProperties.PURPOSE_SIGN)
//                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setDigests(KeyProperties.DIGEST_SHA1)
                        .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
//                        .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                        .setUserAuthenticationRequired(true)
                        .build()
        );
        return keyPairGenerator.generateKeyPair();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static PrivateKey loadECCPrivateKey(String keyStoreName) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return (PrivateKey) keyStore.getKey(keyStoreName, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Signature generateECCSignature(String keyStoreName) throws NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, InvalidKeyException {
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(loadECCPrivateKey(keyStoreName));
        return signature;
    }

}
