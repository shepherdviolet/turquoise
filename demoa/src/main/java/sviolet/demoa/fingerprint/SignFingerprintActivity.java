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
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
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
import sviolet.thistle.util.crypto.ECDSACipher;
import sviolet.thistle.util.crypto.ECDSAKeyGenerator;
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

    private TLogger logger = TLogger.get(this);

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        //指纹识别和AndroidKeyStore必须在API23以上, 指纹识别必须判断硬件是否支持且用户录入了指纹
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
            @TargetApi(Build.VERSION_CODES.M)
            public void onClick(View v) {
                if (!signing){
                    //调用指纹认证进行签名
                    callFingerprint();
                } else {
                    //取消指纹认证
                    cancellationSignal.cancel();
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void callFingerprint() {
        //从AndroidKeyStore加载私钥
        Signature signature;
        try {
            signature = AndroidKeyStoreUtils.loadEcdsaSha256Signature("fingerprint_ecdsa");
        } catch (AndroidKeyStoreUtils.KeyLoadException e) {
            Toast.makeText(SignFingerprintActivity.this, "密钥加载失败, 请重新申请密钥", Toast.LENGTH_SHORT).show();
            signTextView.setText("密钥加载失败, 请重新申请密钥");
            logger.e(e);
            return;
        } catch (AndroidKeyStoreUtils.KeyNotFoundException e) {
            Toast.makeText(SignFingerprintActivity.this, "请先申请密钥", Toast.LENGTH_SHORT).show();
            signTextView.setText("请先申请密钥");
            logger.e(e);
            return;
        }
        signing = true;
        cancellationSignal = new CancellationSignal();
        signButton.setText("Cancel");
        signTextView.setText("请按压指纹传感器");
        //调用指纹传感器
        callFingerprintAuthenticate(signature);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void callFingerprintAuthenticate(Signature signature) {
        FingerprintUtils.authenticate(SignFingerprintActivity.this, signature, cancellationSignal, null,
                new FingerprintUtils.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        //指纹认证失败/取消(多次失败后终止认证, 可能会锁一段时间)
                        Toast.makeText(SignFingerprintActivity.this, errString + ":" + errorCode, Toast.LENGTH_SHORT).show();
                        signTextView.setText(errString + ":" + errorCode);
                        signButton.setText("Sign");
                        signing = false;
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                        //指纹认证提示信息
                        Toast.makeText(SignFingerprintActivity.this, helpString + ":" + helpCode, Toast.LENGTH_SHORT).show();
                        signTextView.setText(helpString + ":" + helpCode);
                    }

                    @Override
                    public void onAuthenticationSucceeded(Signature signature, Cipher cipher, Mac mac) {
                        //指纹认证成功
                        try {
                            //在此处对数据签名, AndroidKeyStore必须在指纹认证后方可使用私钥
                            signature.update(msgEditText.getText().toString().getBytes("UTF-8"));
                            byte[] sign = signature.sign();
                            Toast.makeText(SignFingerprintActivity.this, "数据签名成功", Toast.LENGTH_SHORT).show();
                            signTextView.setText(ByteUtils.bytesToHex(sign));
                            logger.i("sign:" + ByteUtils.bytesToHex(sign));
                            //模拟服务端验签, 客户端无需该操作
                            verifySign(msgEditText.getText().toString().getBytes("UTF-8"), sign);
                        } catch (Exception e) {
                            Toast.makeText(SignFingerprintActivity.this, "数据签名失败, 请重新申请密钥", Toast.LENGTH_SHORT).show();
                            signTextView.setText("数据签名失败, 请重新申请密钥");
                            logger.e(e);
                        }
                        signButton.setText("Sign");
                        signing = false;
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        //指纹认证失败(单次失败, 还能尝试多次)
                        Toast.makeText(SignFingerprintActivity.this, "无法识别的指纹, 请重试", Toast.LENGTH_SHORT).show();
                        signTextView.setText("无法识别的指纹, 请重试");
                    }
                });
    }

    /**
     * 模拟服务端验签, 客户端无需该操作
     */
    private void verifySign(byte[] msg, byte[] sign){
        logger.i("Mock verify sign: start");
        String storedPublicKey = getSharedPreferences("fingerprint_key", Context.MODE_PRIVATE).getString("fingerprint_ecdsa_public_key", null);
        if (storedPublicKey == null){
            logger.e("Mock verify sign: public key is null");
            return;
        }
        try {
            boolean valid = ECDSACipher.verify(msg, sign, ECDSAKeyGenerator.generatePublicKeyByX509(ByteUtils.hexToBytes(storedPublicKey)), ECDSACipher.SIGN_ALGORITHM_ECDSA_SHA256);
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
