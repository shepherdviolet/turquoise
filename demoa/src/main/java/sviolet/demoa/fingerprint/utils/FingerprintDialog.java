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

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.os.CancellationSignal;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.Mac;

import sviolet.demoa.R;
import sviolet.thistle.util.conversion.Base64Utils;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.utils.InjectUtils;
import sviolet.turquoise.enhance.async.WeakHandler;
import sviolet.turquoise.util.crypto.AndroidKeyStoreUtils;
import sviolet.turquoise.util.droid.FingerprintUtils;
import sviolet.turquoise.utilx.lifecycle.LifeCycle;
import sviolet.turquoise.utilx.lifecycle.LifeCycleUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * 指纹识别窗口
 */
@RequiresApi(api = Build.VERSION_CODES.M)
@ResourceId(R.layout.fingerprint_sign_dialog)
public class FingerprintDialog extends Dialog implements LifeCycle {

    private TLogger logger = TLogger.get(this);

    private static boolean fingerprintLock = false;

    private boolean signEnabled = true;//true:启用签名
    private String title;//标题
    private String message;//待签名信息
    private String id;//用户ID
    private Callback callback;//回调

    private CancellationSignal cancellationSignal = new CancellationSignal();//取消信号
    private boolean cancelled = false;

    @ResourceId(R.id.fingerprint_sign_dialog_cancel)
    private View cancelView;
    @ResourceId(R.id.fingerprint_sign_dialog_title)
    private TextView titleView;
    @ResourceId(R.id.fingerprint_sign_dialog_notice)
    private TextView noticeView;

    private Animation shakeAnimation;

    FingerprintDialog(@NonNull Activity activity, @Nullable String title, @Nullable String id, @Nullable String message, boolean signEnabled, @NonNull Callback callback) {
        super(activity);
        this.signEnabled = signEnabled;
        this.title = title != null ? title : "Fingerprint";
        this.message = message != null ? message : "";
        this.id = id;
        this.callback = callback;

        //绑定生命周期
        LifeCycleUtils.attach(activity, this);
    }

    @Override
    public void show() {
        //防止同时启动指纹认证
        if (fingerprintLock){
            logger.e("duplicate fingerprint dialog, skip this");
            return;
        }
        fingerprintLock = true;

        try {
            super.show();
        } catch (Exception e) {
            logger.e("error while show dialog", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题
        InjectUtils.inject(this);

        shakeAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_shake_x);
        titleView.setText(title);
        noticeView.setText("请按压指纹传感器");
        setCancelable(true);//允许取消
        setCanceledOnTouchOutside(false);//允许点外部取消
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //取消
                FingerprintDialog.this.onCancel();
            }
        });
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消
                FingerprintDialog.this.onCancel();
            }
        });

        //从AndroidKeyStore加载私钥
        Signature signature = null;

        if (signEnabled) {
            try {
                signature = FingerprintSuite.getPrivateKeySignature(getContext(), id);
            } catch (AndroidKeyStoreUtils.KeyLoadException e) {
                onError("指纹密钥过期, 请在应用中重新开启指纹认证", false);
                FingerprintSuite.disable(getContext(), id);
                logger.e(e);
                return;
            } catch (AndroidKeyStoreUtils.KeyNotFoundException e) {
                onError("指纹密钥不存在, 请在应用重新开启指纹认证", false);
                FingerprintSuite.disable(getContext(), id);
                logger.e(e);
                return;
            }
        }

        //指纹认证
        FingerprintSuite.authenticate(getContext(), signature, cancellationSignal, new FingerprintUtils.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                //指纹认证失败/取消(多次失败后终止认证, 可能会锁一段时间)
                onError(errString.toString(), true);
            }
            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                //指纹认证提示信息
                onFailed(helpString.toString());
            }
            @Override
            public void onAuthenticationSucceeded(Signature signature, Cipher cipher, Mac mac) {
                onSucceed(signature);
            }
            @Override
            public void onAuthenticationFailed() {
                //指纹认证失败(单次失败, 还能尝试多次)
                onFailed("无法识别的指纹, 请重试");
            }
        });

    }

    private void onSucceed(Signature signature){
        //指纹认证成功

        String sign = null;
        String publicKey = null;

        if (signEnabled) {
            try {
                //在此处对数据签名, AndroidKeyStore必须在指纹认证后方可使用私钥
                signature.update(message.getBytes("UTF-8"));
                sign = Base64Utils.encodeToString(signature.sign());
                publicKey = FingerprintSuite.getPublicKey(getContext(), id);
            } catch (Exception e) {
                onError("数据签名错误, 请在应用中重新开启指纹认证", false);
                FingerprintSuite.disable(getContext(), id);
                logger.e(e);
                return;
            }
        }

        //回调
        callback.onSucceeded(message, sign, publicKey);
        dismiss();

        //解锁
        fingerprintLock = false;
    }

    private void onError(String message, boolean retryable){

        //回调
        if (!cancelled) {
            callback.onError(message, retryable);
        } else {
            callback.onCanceled();
        }
        dismiss();

        //解锁
        fingerprintLock = false;
    }

    private void onCancel(){
        //取消指纹认证
        cancelled = true;
        cancellationSignal.cancel();
    }

    private void onFailed(String message){
        //错误提示
        noticeView.setTextColor(0xFFFF6969);
        noticeView.setText(message);
        shakeAnimation.reset();
        noticeView.startAnimation(shakeAnimation);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
        //Activity销毁时, 取消指纹认证
        onCancel();
    }

    @Override
    public void dismiss() {
        //保证在onCreate时dismiss也能成功关闭窗口
        dismissHandler.sendEmptyMessage(0);
    }

    private void superDismiss(){
        super.dismiss();
    }

    private DismissHandler dismissHandler = new DismissHandler(this);

    private static class DismissHandler extends WeakHandler<FingerprintDialog>{
        private DismissHandler(FingerprintDialog host) {
            super(Looper.getMainLooper(), host);
        }
        @Override
        protected void handleMessageWithHost(Message msg, FingerprintDialog host) {
            host.superDismiss();
        }
    }

    /**
     * 回调
     */
    public interface Callback{

        /**
         * 认证成功
         * @param message 原数据
         * @param sign 签名数据
         * @param publicKey 公钥
         */
        void onSucceeded(String message, String sign, String publicKey);

        /**
         * 认证失败
         * @param message 错误信息
         * @param retryable true:能够再次发起指纹认证 false:不能再次发起指纹认证,因为密钥错误,程序自动关闭了认证开关,需要客户重新开启
         */
        void onError(String message, boolean retryable);

        /**
         * 认证取消
         */
        void onCanceled();

    }

}
