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
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.fingerprint.utils.FingerprintDialog;
import sviolet.demoa.fingerprint.utils.FingerprintSuite;
import sviolet.thistle.util.conversion.Base64Utils;
import sviolet.thistle.util.crypto.ECDSACipher;
import sviolet.thistle.util.crypto.ECDSAKeyGenerator;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.util.droid.FingerprintUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * 指纹签名
 */
@DemoDescription(
        title = "Fingerprint Signature",
        type = "FIDO",
        info = "Signature by Fingerprint protected key"
)

@ResourceId(R.layout.fingerprint_sign_main)
@ActivitySettings(
        statusBarColor = 0xFF30C0C0,
        navigationBarColor = 0xFF30C0C0
)
public class SignFingerprintActivity extends TActivity {

    @ResourceId(R.id.fingerprint_sign_main_msg_edittext)
    private EditText msgEditText;
    @ResourceId(R.id.fingerprint_sign_main_sign_button)
    private Button signButton;
    @ResourceId(R.id.fingerprint_sign_main_sign_textview)
    private TextView signTextView;

    private TLogger logger = TLogger.get(this);

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        //指纹识别和AndroidKeyStore必须在API23以上, 指纹识别必须判断硬件是否支持且用户录入了指纹
        FingerprintSuite.CheckResult checkResult = FingerprintSuite.check(this);
        switch (checkResult) {
            case DISABLED:
            case NO_ENROLLED_FINGERPRINTS:
            case HARDWARE_UNDETECTED:
                signButton.setEnabled(false);
                signTextView.setText(checkResult.getMessage(this));
                return;
            default:
                break;
        }

        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onClick(View v) {
                FingerprintSuite.authenticate(SignFingerprintActivity.this, msgEditText.getText().toString(), new FingerprintDialog.Callback() {
                    @Override
                    public void onSucceeded(String message, String sign, String publicKey) {
                        signTextView.setText(sign);
                        Toast.makeText(SignFingerprintActivity.this, "[指纹]认证成功", Toast.LENGTH_SHORT).show();
                        try {
                            verifySign(message.getBytes("UTF-8"), Base64Utils.decode(sign));
                        } catch (UnsupportedEncodingException e) {
                            logger.e(e);
                        }
                    }
                    @Override
                    public void onError(String message) {
                        Toast.makeText(SignFingerprintActivity.this, "[指纹]" + message, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onCanceled() {
                        Toast.makeText(SignFingerprintActivity.this, "[指纹]认证取消", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 模拟服务端验签, 客户端无需该操作
     */
    private void verifySign(byte[] msg, byte[] sign){
        logger.i("Mock verify sign: start");
        try {
            boolean valid = ECDSACipher.verify(msg, sign, ECDSAKeyGenerator.generatePublicKeyByX509(Base64Utils.decode(FingerprintSuite.getPublicKey(this))), ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256);
            logger.i("Mock verify sign: result:" + valid);
            if (valid) {
                Toast.makeText(SignFingerprintActivity.this, "模拟验签通过", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SignFingerprintActivity.this, "模拟验签失败!!!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            logger.e("Mock verify sign: verify sign failed", e);
        }
    }

}
