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

import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.suke.widget.SwitchButton;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.fingerprint.utils.EnhancedSwitchButton;
import sviolet.demoa.fingerprint.utils.FingerprintDialog;
import sviolet.demoa.fingerprint.utils.FingerprintSuite;
import sviolet.demoa.fingerprint.utils.LockableSwitchButtonListener;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.common.WeakHandler;
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

    private static final String ID = null;//指纹用户ID

    @ResourceId(R.id.fingerprint_apply_main_switch_button)
    private EnhancedSwitchButton switchButton;
    @ResourceId(R.id.fingerprint_apply_main_public_key_textview)
    private TextView publicKeyTextView;

    private TLogger logger = TLogger.get(this);

    @Override
    protected void onInitViews(Bundle savedInstanceState) {

        //检查指纹认证状态
        FingerprintSuite.CheckResult checkResult = FingerprintSuite.check(this, ID);

        switch (checkResult){
            case ENABLED:
            case DISABLED:
                //允许设置
                switchButton.setEnabled(true);
                //显示开关状态
                switchButton.setChecked(checkResult.isEnabled());
                //读取公钥
                if (checkResult.isEnabled()){
                    String storedPublicKey = FingerprintSuite.getPublicKey(this, ID);
                    publicKeyTextView.setText(storedPublicKey);
                    logger.i("public key:" + storedPublicKey);
                }
                // LockableSwitchButtonListener:当开关触发一次事件后, 开关会被锁定不允许操作, 必须在回调方法onCheckedChangedEnhanced中调用releaseLock
                // 方法才能进行下一次开关操作. 用于防止开关反复操作造成逻辑问题.
                switchButton.setOnCheckedChangeListener(new LockableSwitchButtonListener(switchButton) {
                    @Override
                    public void onCheckedChangedEnhanced(SwitchButton view, boolean isChecked) {
                        if (isChecked){
                            FingerprintSuite.authenticate(ApplyFingerprintActivity.this, "指纹识别", ID, null, false, new FingerprintDialog.Callback() {
                                @Override
                                public void onSucceeded(String message, String sign, String publicKey) {
                                    FingerprintSuite.enable(ApplyFingerprintActivity.this, ID, new FingerprintSuite.KeyApplyCallback() {
                                        @Override
                                        public void onSucceed(String publicKey) {
                                            publicKeyTextView.setText(publicKey);
                                            //释放事件(必须)
                                            releaseLock();
                                        }
                                        @Override
                                        public void onFailed() {
                                            publicKeyTextView.setText("指纹认证开启失败, 密钥生成错误");
                                            //延迟释放事件(必须), 同时设置开关状态为关闭
                                            postEnableFailed();
                                        }
                                    });
                                }
                                @Override
                                public void onError(String message) {
                                    Toast.makeText(ApplyFingerprintActivity.this, "[指纹]验证失败", Toast.LENGTH_SHORT).show();
                                    //延迟释放事件(必须), 同时设置开关状态为关闭
                                    postEnableFailed();
                                }
                                @Override
                                public void onCanceled() {
                                    Toast.makeText(ApplyFingerprintActivity.this, "[指纹]验证取消", Toast.LENGTH_SHORT).show();
                                    //延迟释放事件(必须), 同时设置开关状态为关闭
                                    postEnableFailed();
                                }
                            });
                        }else{
                            FingerprintSuite.disable(ApplyFingerprintActivity.this, ID);
                            publicKeyTextView.setText("");
                            //释放事件(必须)
                            releaseLock();
                        }
                    }

                    private void postEnableFailed() {
                        Message msg = handler.obtainMessage();
                        msg.obj = this;
                        handler.sendMessage(msg);
                    }

                });
                break;
            case HARDWARE_UNDETECTED:
            case NO_ENROLLED_FINGERPRINTS:
                //禁用开关
                switchButton.setEnabled(false);
                publicKeyTextView.setText(checkResult.getMessage(this));
                break;
        }

    }

    private final MyHandler handler = new MyHandler(this);

    private static class MyHandler extends WeakHandler<ApplyFingerprintActivity> {

        private static final int HANDLER_POST_ENABLE_FAILED = 0;

        public MyHandler(ApplyFingerprintActivity host) {
            super(host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, ApplyFingerprintActivity host) {
            switch (msg.what){
                case HANDLER_POST_ENABLE_FAILED:
                    //释放事件(必须)
                    ((LockableSwitchButtonListener)msg.obj).releaseLock();
                    //置为关闭状态
                    host.switchButton.setChecked(false);
                    break;
                default:
                    break;
            }
        }

    }

}
