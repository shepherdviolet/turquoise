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

import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.Mac;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.util.crypto.AndroidKeyStoreUtils;
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

    private boolean signing = false;
    private CancellationSignal cancellationSignal;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        if (!FingerprintUtils.isHardwareDetected(this)){
            signButton.setEnabled(false);
            signTextView.setText("设备不支持指纹识别");
            return;
        }
        if (!FingerprintUtils.hasEnrolledFingerprints(this)){
            signButton.setEnabled(false);
            signTextView.setText("请在设备中开启并录入指纹后使用该功能");
            return;
        }
        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void onClick(View v) {
                if (!signing){
                    Signature signature;
                    try {
                        signature = AndroidKeyStoreUtils.loadRsaSha256Signature("fingerprint_rsa");
                    } catch (AndroidKeyStoreUtils.KeyLoadException e) {
                        Toast.makeText(SignFingerprintActivity.this, "密钥加载失败, 请重新申请密钥", Toast.LENGTH_SHORT).show();
                        signTextView.setText("密钥加载失败, 请重新申请密钥");
                        TLogger.get(this).e(e);
                        return;
                    } catch (AndroidKeyStoreUtils.KeyNotFoundException e) {
                        Toast.makeText(SignFingerprintActivity.this, "请先申请密钥", Toast.LENGTH_SHORT).show();
                        signTextView.setText("请先申请密钥");
                        TLogger.get(this).e(e);
                        return;
                    }
                    signing = true;
                    cancellationSignal = new CancellationSignal();
                    signButton.setText("Cancel");
                    signTextView.setText("请按压指纹传感器");
                    FingerprintUtils.authenticate(SignFingerprintActivity.this, signature, cancellationSignal, null,
                            new FingerprintUtils.AuthenticationCallback() {
                                @Override
                                public void onAuthenticationError(int errorCode, CharSequence errString) {
                                    Toast.makeText(SignFingerprintActivity.this, errString + ":" + errorCode, Toast.LENGTH_SHORT).show();
                                    signTextView.setText(errString + ":" + errorCode);
                                    signButton.setText("Sign");
                                    signing = false;
                                }
                                @Override
                                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                                    Toast.makeText(SignFingerprintActivity.this, helpString + ":" + helpCode, Toast.LENGTH_SHORT).show();
                                    signTextView.setText(helpString + ":" + helpCode);
                                }
                                @Override
                                public void onAuthenticationSucceeded(Signature signature, Cipher cipher, Mac mac) {
                                    try {
                                        signature.update(msgEditText.getText().toString().getBytes("UTF-8"));
                                        byte[] sign = signature.sign();
                                        Toast.makeText(SignFingerprintActivity.this, "数据签名成功", Toast.LENGTH_SHORT).show();
                                        signTextView.setText(ByteUtils.bytesToHex(sign));
                                        TLogger.get(this).i("sign:" + ByteUtils.bytesToHex(sign));
                                    } catch (Exception e) {
                                        Toast.makeText(SignFingerprintActivity.this, "数据签名失败, 请重新申请密钥", Toast.LENGTH_SHORT).show();
                                        signTextView.setText("数据签名失败, 请重新申请密钥");
                                        TLogger.get(this).e(e);
                                    }
                                    signButton.setText("Sign");
                                    signing = false;
                                }
                                @Override
                                public void onAuthenticationFailed() {
                                    Toast.makeText(SignFingerprintActivity.this, "无法识别的指纹, 请重试", Toast.LENGTH_SHORT).show();
                                    signTextView.setText("无法识别的指纹, 请重试");
                                }
                            });
                } else {
                    cancellationSignal.cancel();
                }
            }
        });
    }

}
