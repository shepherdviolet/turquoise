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

package sviolet.demoa.fingerprint;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.thistle.util.conversion.StringUtils;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.common.WeekAsyncTask;
import sviolet.turquoise.util.crypto.AndroidKeyStoreUtils;
import sviolet.turquoise.util.droid.FingerprintUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * 申请指纹认证的秘钥
 */
@DemoDescription(
        title = "Fingerprint Apply",
        type = "FIDO",
        info = "Apply a fingerprint key stored in AndroidKeyStore"
)

@ResourceId(R.layout.fingerprint_apply_main)
@ActivitySettings(
        statusBarColor = 0xFF30C0C0,
        navigationBarColor = 0xFF30C0C0
)
public class ApplyFingerprintActivity extends TActivity {

    @ResourceId(R.id.fingerprint_apply_main_apply_button)
    private Button applyButton;
    @ResourceId(R.id.fingerprint_apply_main_public_key_textview)
    private TextView publicKeyTextView;

    private boolean applying = false;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        //指纹识别和AndroidKeyStore必须在API23以上, 指纹识别必须判断硬件是否支持且用户录入了指纹
        if (!FingerprintUtils.isHardwareDetected(this)){
            applyButton.setEnabled(false);
            publicKeyTextView.setText("设备不支持指纹识别");
            return;
        }
        if (!FingerprintUtils.hasEnrolledFingerprints(this)){
            applyButton.setEnabled(false);
            publicKeyTextView.setText("请在设备中开启并录入指纹后使用该功能");
            return;
        }
        //显示之前申请的公钥
        String storedPublicKey = getSharedPreferences("fingerprint_key", Context.MODE_PRIVATE).getString("fingerprint_rsa_public_key", "");
        publicKeyTextView.setText(storedPublicKey);
        TLogger.get(this).i("public key:" + storedPublicKey);
        //申请按钮
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!applying) {
                    applying = true;
                    //异步执行
                    new KeyApplyTask(ApplyFingerprintActivity.this).execute((String) null);
                }
            }
        });
    }

    private static class KeyApplyTask extends WeekAsyncTask<ApplyFingerprintActivity, String, Integer, byte[]> {

        public KeyApplyTask(ApplyFingerprintActivity applyFingerprintActivity) {
            super(applyFingerprintActivity);
        }

        @Override
        @TargetApi(Build.VERSION_CODES.M)
        protected byte[] doInBackgroundEnhanced(String... strings) throws ExceptionWrapper {
            try {
                //在AndroidKeyStore中生成RSA公私钥, 私钥存在TEE中不可读取, 公钥可读取
                return AndroidKeyStoreUtils.genRsaSha256SignKey("fingerprint_rsa").getEncoded();
            } catch (AndroidKeyStoreUtils.KeyGenerateException e) {
                throw new ExceptionWrapper(e);
            }
        }

        @Override
        protected void onPostExecuteWithHost(byte[] bytes, ApplyFingerprintActivity host) {
            //显示公钥
            host.publicKeyTextView.setText(ByteUtils.bytesToHex(bytes));
            TLogger.get(this).i("public key:" + ByteUtils.bytesToHex(bytes));
            SharedPreferences sharedPreferences = host.getSharedPreferences("fingerprint_key", Context.MODE_PRIVATE);
            sharedPreferences.edit()
                    .putString("fingerprint_rsa_public_key", ByteUtils.bytesToHex(bytes))
                    .apply();
        }

        @Override
        protected void onExceptionWithHost(Throwable throwable, ApplyFingerprintActivity host) {
            //显示异常
            host.publicKeyTextView.setText(StringUtils.throwableToString(throwable));
        }

        @Override
        protected void onFinishWithHost(ApplyFingerprintActivity host) {
            host.applying = false;
        }
    }

}
