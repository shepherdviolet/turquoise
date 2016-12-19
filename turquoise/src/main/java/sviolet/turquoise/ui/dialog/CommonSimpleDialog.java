/*
 * Copyright (C) 2015-2016 S.Violet
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

package sviolet.turquoise.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import sviolet.turquoise.ui.util.StateListDrawableUtils;
import sviolet.turquoise.util.droid.MeasureUtils;

/**
 * <p>简单的Dialog</p>
 *
 * <pre>{@code
 *      CommonSimpleDialog.Builder builder = new CommonSimpleDialog.Builder();
 *      builder.setTitle("权限申请");
 *      builder.setContent("为了能够帮您定位到当前的城市, 需要申请以下权限, 请通过");
 *      builder.setRightButton("确定", new SimpleDialogBuilder.Callback() {
 *          @Override
 *          public void callback() {
 *              Toast.makeText(getApplicationContext(), "ok button", Toast.LENGTH_SHORT).show();
 *          }
 *      });
 *      builder.build(this).show();
 * }</pre>
 *
 * Created by S.Violet on 2016/12/19.
 */
public class CommonSimpleDialog extends Dialog {

    private Info info;

    private CommonSimpleDialog(Context context, Info info) {
        super(context);
        this.info = info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题

        //temp
        LinearLayout.LayoutParams params;
        final int screenWidth = MeasureUtils.getScreenWidth(getContext()) > MeasureUtils.dp2px(getContext(), 400) ?
                MeasureUtils.dp2px(getContext(), 400) : MeasureUtils.getScreenWidth(getContext());
        final int dp10 = MeasureUtils.dp2px(getContext(), 10);
        final int dp20 = MeasureUtils.dp2px(getContext(), 20);
        final int dp50 = MeasureUtils.dp2px(getContext(), 50);
        final int dp60 = MeasureUtils.dp2px(getContext(), 60);

        //实例化控件
        TextView titleView = new TextView(getContext());
        TextView contentView = new TextView(getContext());
        TextView leftButton = new TextView(getContext());
        TextView middleButton = new TextView(getContext());
        TextView rightButton = new TextView(getContext());
        LinearLayout container = new LinearLayout(getContext());
        ScrollView contentScrollView = new MaxHeightScrollView(getContext(), (int) (screenWidth * 0.5f));
        LinearLayout buttonLinearLayout = new LinearLayout(getContext());

        //容器
        params = new LinearLayout.LayoutParams((int) (screenWidth * 0.85f), LinearLayout.LayoutParams.WRAP_CONTENT);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(0xFFFFFFFF);
        addContentView(container, params);

        //标题
        if (info.title != null) {
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp60);
            titleView.setTextColor(0xFF909090);
            titleView.setTextSize(20f);
            titleView.setPadding(dp20, dp10, dp20, 0);
            titleView.setGravity(Gravity.CENTER_VERTICAL);
            titleView.setMaxLines(1);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            titleView.setText(info.title);
            TextPaint textPaint = titleView.getPaint();
            textPaint.setFakeBoldText(true);
            container.addView(titleView, params);
        } else {
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp20);
            container.addView(new View(getContext()), params);
        }

        //内容

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        contentScrollView.setMinimumHeight(dp50);
        container.addView(contentScrollView, params);

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        contentView.setTextColor(0xFF505050);
        contentView.setTextSize(17f);
        contentView.setPadding(dp20, 0, dp20, 0);
        contentView.setText(info.content);
        contentScrollView.addView(contentView, params);

        //按钮

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp50);
        buttonLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        container.addView(buttonLinearLayout, params);

        if (info.leftButtonStr != null) {
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            final SimpleDialogBuilder.Callback leftButtonCallback = info.leftButtonCallback;
            leftButton.setTextColor(0xFF209090);
            leftButton.setTextSize(16f);
            leftButton.setPadding(dp20, 0, dp20, 0);
            leftButton.setGravity(Gravity.CENTER);
            leftButton.setMaxLines(1);
            leftButton.setEllipsize(TextUtils.TruncateAt.END);
            leftButton.setBackgroundDrawable(StateListDrawableUtils.createPressSelector(new ColorDrawable(0x00000000), new ColorDrawable(0x08000000)));
            leftButton.setClickable(true);
            leftButton.setText(info.leftButtonStr);
            leftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if(leftButtonCallback != null){
                        leftButtonCallback.callback();
                    }
                }
            });
            buttonLinearLayout.addView(leftButton, params);
        }

        params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        buttonLinearLayout.addView(new View(getContext()), params);

        if (info.middleButtonStr != null) {
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            final SimpleDialogBuilder.Callback middleButtonCallback = info.middleButtonCallback;
            middleButton.setTextColor(0xFF209090);
            middleButton.setTextSize(16f);
            middleButton.setPadding(dp20, 0, dp20, 0);
            middleButton.setGravity(Gravity.CENTER);
            middleButton.setMaxLines(1);
            middleButton.setEllipsize(TextUtils.TruncateAt.END);
            middleButton.setBackgroundDrawable(StateListDrawableUtils.createPressSelector(new ColorDrawable(0x00000000), new ColorDrawable(0x08000000)));
            middleButton.setClickable(true);
            middleButton.setText(info.middleButtonStr);
            middleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if(middleButtonCallback != null){
                        middleButtonCallback.callback();
                    }
                }
            });
            buttonLinearLayout.addView(middleButton, params);
        }

        if (info.rightButtonStr != null) {
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            final SimpleDialogBuilder.Callback rightButtonCallback = info.rightButtonCallback;
            rightButton.setTextColor(0xFF209090);
            rightButton.setTextSize(16f);
            rightButton.setPadding(dp20, 0, dp20, 0);
            rightButton.setGravity(Gravity.CENTER);
            rightButton.setMaxLines(1);
            rightButton.setEllipsize(TextUtils.TruncateAt.END);
            rightButton.setBackgroundDrawable(StateListDrawableUtils.createPressSelector(new ColorDrawable(0x00000000), new ColorDrawable(0x08000000)));
            rightButton.setClickable(true);
            rightButton.setText(info.rightButtonStr);
            rightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if(rightButtonCallback != null){
                        rightButtonCallback.callback();
                    }
                }
            });
            buttonLinearLayout.addView(rightButton, params);
        }

        //取消

        if (info.cancelable){
            final SimpleDialogBuilder.Callback cancelCallback = info.cancelCallback;
            setCancelable(true);//允许取消
            setCanceledOnTouchOutside(true);//允许点外部取消
            setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dismiss();
                    if (cancelCallback != null){
                        cancelCallback.callback();
                    }
                }
            });
        }else{
            setCancelable(false);//禁止取消
            setCanceledOnTouchOutside(false);//禁止点外部取消
        }

        this.info = null;//销毁信息
    }

    /**
     * 可以限定最大高度的ScrollView
     */
    private class MaxHeightScrollView extends ScrollView{

        private int maxHeight;

        public MaxHeightScrollView(Context context, int maxHeight) {
            super(context);
            this.maxHeight = maxHeight;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private static class Info{

        private String title;
        private String content;
        private String leftButtonStr;
        private String middleButtonStr;
        private String rightButtonStr;
        private boolean cancelable = true;

        private SimpleDialogBuilder.Callback leftButtonCallback;
        private SimpleDialogBuilder.Callback middleButtonCallback;
        private SimpleDialogBuilder.Callback rightButtonCallback;
        private SimpleDialogBuilder.Callback cancelCallback;

    }

    public static class Builder implements SimpleDialogBuilder{

        private Info info = new Info();

        @Override
        public void setTitle(String title) {
            info.title = title;
        }

        @Override
        public void setContent(String content) {
            info.content = content;
        }

        @Override
        public void setLeftButton(String msg, Callback callback) {
            info.leftButtonStr = msg;
            info.leftButtonCallback = callback;
        }

        @Override
        public void setMiddleButton(String msg, Callback callback) {
            info.middleButtonStr = msg;
            info.middleButtonCallback = callback;
        }

        @Override
        public void setRightButton(String msg, Callback callback) {
            info.rightButtonStr = msg;
            info.rightButtonCallback = callback;
        }

        @Override
        public void setCancelCallback(boolean cancelable, Callback callback) {
            info.cancelable = cancelable;
            info.cancelCallback = callback;
        }

        @Override
        public Dialog build(Context context) {
            return new CommonSimpleDialog(context, info);
        }

    }

}
