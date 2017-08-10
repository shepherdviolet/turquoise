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

package sviolet.demoa.fingerprint.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;

import java.security.KeyStore;

import sviolet.thistle.util.conversion.Base64Utils;
import sviolet.turquoise.util.crypto.AndroidKeyStoreUtils;
import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.util.droid.FingerprintUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * 指纹识别封装套件
 *
 * Created by S.Violet on 2017/8/10.
 */
public class FingerprintSuite {

    private static final String SHARED_PREFERENCES_NAME = "fingerprint";
    private static final String SHARED_PREFERENCES_KEY_ENABLED = "enabled";
    private static final String SHARED_PREFERENCES_KEY_PUBLIC_KEY = "key";

    private static final String ANDROID_KEY_STORE_NAME = "fingerprint-private-key";

    /**
     * 判断当前指纹识别状态
     * @param context context
     */
    public static CheckResult check(Context context){
        //指纹识别和AndroidKeyStore必须在API23以上, 指纹识别必须判断硬件是否支持且用户录入了指纹
        if (!FingerprintUtils.isHardwareDetected(context)){
            return CheckResult.HARDWARE_UNDETECTED;
        }
        if (!FingerprintUtils.hasEnrolledFingerprints(context)){
            return CheckResult.NO_ENROLLED_FINGERPRINTS;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String enabled = sharedPreferences.getString(SHARED_PREFERENCES_KEY_ENABLED, "");
        String publicKey = sharedPreferences.getString(SHARED_PREFERENCES_KEY_PUBLIC_KEY, "");
        return "true".equals(enabled) && !"".equals(publicKey) && isPrivateKeyExists() ? CheckResult.ENABLED : CheckResult.DISABLED;
    }

    public enum CheckResult {

        //设备支持指纹, 指纹已录入, 指纹认证已开启
        ENABLED(0, "", ""),
        //设备支持指纹, 指纹已录入, 指纹认证已关闭
        DISABLED(1, "指纹认证已关闭, 请在应用中开启", "Fingerprint is disabled in this APP"),
        //设备支持指纹, 指纹未录入
        NO_ENROLLED_FINGERPRINTS(2, "请在手机设置中开启并录入指纹", "Enroll fingerprints in system settings first"),
        //设备不支持指纹
        HARDWARE_UNDETECTED(3, "抱歉，指纹识别不支持您的手机", "Sorry, fingerprint hardware undetected in your device");

        private int code;
        private String messageCn;
        private String messageEn;

        CheckResult(int code, String messageCn, String messageEn) {
            this.code = code;
            this.messageCn = messageCn;
            this.messageEn = messageEn;
        }

        /**
         * 获取错误状态信息
         */
        public String getMessage(Context context) {
            if (context != null && DeviceUtils.isLocaleZhCn(context)) {
                return messageCn;
            } else {
                return messageEn;
            }
        }

        /**
         * 获取状态码
         */
        public int getCode(){
            return code;
        }

        /**
         * true:设备支持指纹, 指纹已录入, 指纹认证已开启
         */
        public boolean isEnabled(){
            return this == ENABLED;
        }

    }

    public static void enable(Context context, KeyApplyCallback callback){
        if (context == null || callback == null){
            throw new IllegalArgumentException("context or callback is null");
        }
        new KeyApplyTask(context, callback).execute();
    }

    private static class KeyApplyTask extends AsyncTask<Integer, Integer, byte[]> {

        @SuppressLint("StaticFieldLeak")
        private Context context;
        private KeyApplyCallback callback;

        public KeyApplyTask(Context context, KeyApplyCallback callback) {
            super();
            this.context = context;
            this.callback = callback;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.M)
        protected byte[] doInBackground(Integer... args) {
            try {
                //在AndroidKeyStore中生成ECDSA公私钥, 私钥存在TEE中不可读取, 公钥可读取
                return AndroidKeyStoreUtils.genEcdsaSha256SignKey(ANDROID_KEY_STORE_NAME).getEncoded();
            } catch (AndroidKeyStoreUtils.KeyGenerateException e) {
                TLogger.get(this).e(e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            if (bytes == null){
                callback.onFailed();
                return;
            }

            String publicKey = Base64Utils.encodeToString(bytes);
            TLogger.get(this).d("public key:" + publicKey);
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            sharedPreferences.edit()
                    .putString(SHARED_PREFERENCES_KEY_ENABLED, "true")
                    .putString(SHARED_PREFERENCES_KEY_PUBLIC_KEY, publicKey)
                    .apply();

            callback.onSucceed(publicKey);
        }

    }

    public interface KeyApplyCallback{
        void onSucceed(String publicKey);
        void onFailed();
    }

    public static void disable(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(SHARED_PREFERENCES_KEY_ENABLED, "false")
                .putString(SHARED_PREFERENCES_KEY_PUBLIC_KEY, "")
                .apply();
    }

    /**
     * 获取公钥
     */
    public static String getPublicKey(Context context){
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(SHARED_PREFERENCES_KEY_PUBLIC_KEY, "");
    }

    private static boolean isPrivateKeyExists(){
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return keyStore.getKey(ANDROID_KEY_STORE_NAME, null) != null;
        } catch (Exception e) {
            return false;
        }
    }

}
