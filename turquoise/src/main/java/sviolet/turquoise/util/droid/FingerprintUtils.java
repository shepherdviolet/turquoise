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
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompatApi23;
import android.support.v4.os.CancellationSignal;
import android.util.Log;

import java.lang.reflect.Method;
import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.Mac;

/**
 * <p>
 *     指纹识别工具<br/>
 *     FingerprintManagerCompat在部分机型上存在误判, 用该工具类代替
 * </p>
 *
 * <p>
 *     因为FingerprintManagerCompat的authenticate方法中, 会判断context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT),
 *     个别手机虽然支持指纹, 但是这个返回的是false, 导致无法使用指纹认证. 为了兼容此类机型, 直接使用FingerprintManager调起认证.<br/>
 *     但是, Android Studio 3.1 (gradle plugin 3.1.x) 以后, Instant Run会扫描工程中所有的代码, 当扫描到FingerprintManager.AuthenticationCallback类时,
 *     如果调试机型版本低于API23, 会报出字节码不存在的错误, 影响调试. 为了解决此问题, 对FingerprintManager的调用采用反射方式,
 *     不直接涉及FingerprintManager.AuthenticationCallback类.
 * </p>
 *
 * @author S.Violet
 */

public class FingerprintUtils {

    /**
     * 无法使用反射方式调用指纹认证
     */
    private static boolean reflectWayFailed = false;

    static {
        /*
            初步判断android.support.v4是否支持反射调起指纹认证
         */
        try {
            Class.forName("android.hardware.fingerprint.FingerprintManager$CryptoObject");
            Class.forName("android.hardware.fingerprint.FingerprintManager$AuthenticationCallback");
            FingerprintManagerCompatApi23.class.getDeclaredMethod("wrapCryptoObject", FingerprintManagerCompatApi23.CryptoObject.class);
            FingerprintManagerCompatApi23.class.getDeclaredMethod("wrapCallback", FingerprintManagerCompatApi23.AuthenticationCallback.class);
        } catch (Throwable t){
            reflectWayFailed = true;
            Log.w("Turquoise", "[FingerprintUtils]we can not authentication by reflect way in current version of android.support.v4", t);
        }
    }

    /**
     * 打印指纹认证支持情况
     */
    @RequiresPermission("android.permission.USE_FINGERPRINT")
    public static String getDebugInfo(@NonNull Context context){
        if (android.os.Build.VERSION.SDK_INT < 23){
            return "unsupported, api < 23";
        }
        FingerprintManager fingerprintManager = (FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
        if (fingerprintManager == null){
            return "unsupported, no fingerprintManager";
        }
        if (!fingerprintManager.isHardwareDetected()){
            return "unsupported, isHardwareDetected false";
        }
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) && reflectWayFailed){
            return "unsupported, no feature and reflect disabled!";
        }

        StringBuilder stringBuilder = new StringBuilder("supported");
        if (fingerprintManager.hasEnrolledFingerprints()){
            stringBuilder.append(", enrolled");
        } else {
            stringBuilder.append(", no enrolled");
        }
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            stringBuilder.append(", reflect way");
        }
        return stringBuilder.toString();
    }

    /**
     * 判断当前设备是否支持指纹
     */
    @RequiresPermission("android.permission.USE_FINGERPRINT")
    public static boolean isHardwareDetected(@NonNull Context context){
        if (android.os.Build.VERSION.SDK_INT < 23){
            return false;
        }
        FingerprintManager fingerprintManager = (FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
        if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()){
            return false;
        }
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) && reflectWayFailed){
            return false;
        }
        return true;
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
        if (fingerprintManager == null || !fingerprintManager.hasEnrolledFingerprints()){
            return false;
        }
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) && reflectWayFailed){
            return false;
        }
        return true;
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
            authenticate(context, null, signature, cancel, handler, callback);
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
            authenticate(context, cipher, null, cancel, handler, callback);
        } else {
            callback.onAuthenticationError(-17700, "Fingerprint requires Android 6.0 or above");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @RequiresPermission("android.permission.USE_FINGERPRINT")
    private static void authenticate(@NonNull Context context,
                             @Nullable Cipher cipher,
                             @Nullable Signature signature,
                             @Nullable CancellationSignal cancel,
                             @Nullable Handler handler,
                             @NonNull final AuthenticationCallback callback){

        //判断是否支持
        FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
            callback.onAuthenticationError(17701, "The device does not support fingerprints");
            return;
        }
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            callback.onAuthenticationError(17702, "No fingerprints enrolled in your device");
            return;
        }

        //如果支持但特性不存在, 尝试使用反射调起
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
            authenticationByCompat(context, cipher, signature, cancel, handler, callback);
        } else {
            authenticationByReflect(context, cipher, signature, cancel, handler, callback, fingerprintManager);
        }

    }

    /**
     * 常规方式调起认证
     */
    private static void authenticationByCompat(@NonNull Context context,
                                               @Nullable Cipher cipher,
                                               @Nullable Signature signature,
                                               @Nullable CancellationSignal cancel,
                                               @Nullable Handler handler,
                                               @NonNull final AuthenticationCallback callback) {

        //crypto
        FingerprintManagerCompat.CryptoObject crypto = null;
        if (cipher != null){
            crypto = new FingerprintManagerCompat.CryptoObject(cipher);
        } else if (signature != null){
            crypto = new FingerprintManagerCompat.CryptoObject(signature);
        }

        //compat
        FingerprintManagerCompat.from(context).authenticate(crypto, 0, cancel, new FingerprintManagerCompat.AuthenticationCallback(){
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                callback.onAuthenticationError(errMsgId, errString);
            }
            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                callback.onAuthenticationHelp(helpMsgId, helpString);
            }
            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                if (result == null || result.getCryptoObject() == null) {
                    callback.onAuthenticationSucceeded(null, null, null);
                } else {
                    callback.onAuthenticationSucceeded(result.getCryptoObject().getSignature(), result.getCryptoObject().getCipher(), result.getCryptoObject().getMac());
                }
            }
            @Override
            public void onAuthenticationFailed() {
                callback.onAuthenticationFailed();
            }
        }, handler);
    }

    /**
     * 反射方式调起认证
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void authenticationByReflect(@NonNull Context context,
                                                @Nullable Cipher cipher,
                                                @Nullable Signature signature,
                                                @Nullable CancellationSignal cancel,
                                                @Nullable Handler handler,
                                                @NonNull final AuthenticationCallback callback,
                                                @NonNull FingerprintManager fingerprintManager) {

        //无法用反射
        if (reflectWayFailed) {
            callback.onAuthenticationError(17701, "The device does not support fingerprints (by reflect way)");
            return;
        }

        try {

            //用FingerprintManagerCompatApi23.wrapCryptoObject方法构建FingerprintManager$CryptoObject

            FingerprintManagerCompatApi23.CryptoObject crypto = null;
            if (cipher != null){
                crypto = new FingerprintManagerCompatApi23.CryptoObject(cipher);
            } else if (signature != null){
                crypto = new FingerprintManagerCompatApi23.CryptoObject(signature);
            }

            Object cryptoObj = null;
            if (crypto != null){
                Method wrapCryptoObjectMethod = FingerprintManagerCompatApi23.class.getDeclaredMethod("wrapCryptoObject", FingerprintManagerCompatApi23.CryptoObject.class);
                wrapCryptoObjectMethod.setAccessible(true);
                cryptoObj = wrapCryptoObjectMethod.invoke(null, crypto);
            }

            //用FingerprintManagerCompatApi23.wrapCallback方法构建FingerprintManager$AuthenticationCallback

            Method wrapCallbackMethod = FingerprintManagerCompatApi23.class.getDeclaredMethod("wrapCallback", FingerprintManagerCompatApi23.AuthenticationCallback.class);
            wrapCallbackMethod.setAccessible(true);

            Object callbackObj = wrapCallbackMethod.invoke(null, new FingerprintManagerCompatApi23.AuthenticationCallback(){
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    callback.onAuthenticationError(errorCode, errString);
                }
                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    callback.onAuthenticationHelp(helpCode, helpString);
                }
                @Override
                public void onAuthenticationSucceeded(FingerprintManagerCompatApi23.AuthenticationResultInternal result) {
                    if (result == null || result.getCryptoObject() == null) {
                        callback.onAuthenticationSucceeded(null, null, null);
                    } else {
                        callback.onAuthenticationSucceeded(result.getCryptoObject().getSignature(), result.getCryptoObject().getCipher(), result.getCryptoObject().getMac());
                    }
                }
                @Override
                public void onAuthenticationFailed() {
                    callback.onAuthenticationFailed();
                }
            });

            //反射调用authenticate方法, 避免出现FingerprintManager$AuthenticationCallback(强制类型转换)

            Method authenticationMethod = FingerprintManager.class.getDeclaredMethod(
                    "authenticate",
                    Class.forName("android.hardware.fingerprint.FingerprintManager$CryptoObject"),
                    android.os.CancellationSignal.class,
                    int.class,
                    Class.forName("android.hardware.fingerprint.FingerprintManager$AuthenticationCallback"),
                    Handler.class);

            authenticationMethod.invoke(
                    fingerprintManager,
                    cryptoObj,
                    cancel != null ? (android.os.CancellationSignal)cancel.getCancellationSignalObject() : null,
                    0,
                    callbackObj,
                    handler);

        } catch (Throwable t){
            reflectWayFailed = true;
            Log.w("Turquoise", "[FingerprintUtils]authentication by reflect failed, does not support fingerprints", t);
            callback.onAuthenticationError(17701, "The device does not support fingerprints (by reflect way)");
        }

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
         * @param signature signature instance to sign data
         * @param cipher cipher instance to encrypt/decrypt data
         * @param mac mac instance
         */
        public void onAuthenticationSucceeded(Signature signature, Cipher cipher, Mac mac) { }

        /**
         * Called when a fingerprint is valid but not recognized.
         */
        public void onAuthenticationFailed() { }
    }

}
