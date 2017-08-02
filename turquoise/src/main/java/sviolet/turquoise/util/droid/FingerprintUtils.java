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

package sviolet.turquoise.util.droid;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;

import java.security.Signature;

import javax.crypto.Cipher;

/**
 * <p>
 *     指纹识别工具<br/>
 *     FingerprintManagerCompat在部分机型上存在误判, 用该工具类代替
 * </p>
 *
 *
 * Created by S.Violet on 2017/8/2.
 */

public class FingerprintUtils {

    /**
     * 判断当前设备是否支持指纹
     */
    @RequiresPermission("android.permission.USE_FINGERPRINT")
    public static boolean isHardwareDetected(@NonNull Context context){
        if (android.os.Build.VERSION.SDK_INT < 23){
            return false;
        }
        FingerprintManager fingerprintManager = (FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
        return fingerprintManager != null && fingerprintManager.isHardwareDetected();
    }

    /**
     * 判断用户是否录入过指纹
     */
    @RequiresPermission("android.permission.USE_FINGERPRINT")
    public static boolean hasEnrolledFingerprints(@NonNull Context context){
        if (android.os.Build.VERSION.SDK_INT < 23){
            return false;
        }
        FingerprintManager fingerprintManager = (FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
        return fingerprintManager != null && fingerprintManager.hasEnrolledFingerprints();
    }

    /**
     * 指纹验证
     *
     * @param context context
     * @param signature signature instance, optional
     * @param cancel an object that can be used to cancel authentication
     * @param handler an optional handler to handle callback events
     * @param callback an object to receive authentication events
     */
    @RequiresPermission("android.permission.USE_FINGERPRINT")
    public static void authenticate(@NonNull Context context,
                                    @Nullable Signature signature,
                                    @Nullable CancellationSignal cancel,
                                    @Nullable Handler handler,
                                    @NonNull final AuthenticationCallback callback) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            authenticate(context, signature != null ? new FingerprintManager.CryptoObject(signature) : null, cancel, handler, callback);
        } else {
            callback.onAuthenticationError(-17700, "Fingerprint requires Android 6.0 or above");
        }
    }

    /**
     * 指纹验证
     *
     * @param context context
     * @param cipher cipher instance, optional
     * @param cancel an object that can be used to cancel authentication
     * @param handler an optional handler to handle callback events
     * @param callback an object to receive authentication events
     */
    @RequiresPermission("android.permission.USE_FINGERPRINT")
    public static void authenticate(@NonNull Context context,
                                    @Nullable Cipher cipher,
                                    @Nullable CancellationSignal cancel,
                                    @Nullable Handler handler,
                                    @NonNull final AuthenticationCallback callback) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            authenticate(context, cipher != null ? new FingerprintManager.CryptoObject(cipher) : null, cancel, handler, callback);
        } else {
            callback.onAuthenticationError(-17700, "Fingerprint requires Android 6.0 or above");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @RequiresPermission("android.permission.USE_FINGERPRINT")
    private static void authenticate(@NonNull Context context,
                             @Nullable FingerprintManager.CryptoObject crypto,
                             @Nullable CancellationSignal cancel,
                             @Nullable Handler handler,
                             @NonNull final AuthenticationCallback callback){
        FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
            callback.onAuthenticationError(17701, "The device does not support fingerprints");
            return;
        }
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            callback.onAuthenticationError(17702, "No fingerprints enrolled in your device");
            return;
        }
        fingerprintManager.authenticate(crypto, cancel, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                callback.onAuthenticationError(errorCode, errString);
            }
            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                callback.onAuthenticationHelp(helpCode, helpString);
            }
            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                callback.onAuthenticationSucceeded(result);
            }
            @Override
            public void onAuthenticationFailed() {
                callback.onAuthenticationFailed();
            }
        }, handler);
    }

    /**
     * 回调代理
     */
    public static abstract class AuthenticationCallback {
        /**
         * Called when an unrecoverable error has been encountered and the operation is complete.
         * No further callbacks will be made on this object.
         * @param errorCode An integer identifying the error message
         * @param errString A human-readable error string that can be shown in UI
         */
        public abstract void onAuthenticationError(int errorCode, CharSequence errString);

        /**
         * Called when a recoverable error has been encountered during authentication. The help
         * string is provided to give the user guidance for what went wrong, such as
         * "Sensor dirty, please clean it."
         * @param helpCode An integer identifying the error message
         * @param helpString A human-readable string that can be shown in UI
         */
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) { }

        /**
         * Called when a fingerprint is recognized.
         * @param result An object containing authentication-related data
         */
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) { }

        /**
         * Called when a fingerprint is valid but not recognized.
         */
        public void onAuthenticationFailed() { }
    }

}
