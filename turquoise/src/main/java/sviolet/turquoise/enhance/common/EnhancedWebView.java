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

package sviolet.turquoise.enhance.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import java.lang.reflect.Method;

import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * (修正三星输入法在WebView中number类型无小数点的bug)
 * (新旧API均用evaluateJavascript()执行JS)
 *
 * @author S.Violet
 */

public class EnhancedWebView extends WebView {

    private TLogger logger = TLogger.get(this);

    private boolean useLoadUrlMethod = false;//使用loadUrl加载JS

    public EnhancedWebView(Context context) {
        super(context);
        checkJsMethod();
    }

    public EnhancedWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        checkJsMethod();
    }

    public EnhancedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        checkJsMethod();
    }

    /**
     * 判断用哪种方法加载JS(evaluateJavascript/loadUrl)
     */
    private void checkJsMethod() {
        if (hasMethod(WebView.class, "evaluateJavascript", new Class[]{String.class, ValueCallback.class})) {
            logger.i("web view has method : evaluateJavascript");//有evaluateJavascript方法
            useLoadUrlMethod = false;
        } else {
            logger.i("web view has no method : evaluateJavascript");//无evaluateJavascript方法
            useLoadUrlMethod = true;
        }
    }

    private boolean hasMethod(Class<?> clazz, String methodName, Class<?>[] params) {
        try {
            Method method = clazz.getMethod(methodName, params);
            return method != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * 捕获服务器传下来的"number"类型,改为"numberDecimal"类型
     * (三星输入法number类型无小数点,numberDecimal有小数点)
     */
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection connection = super.onCreateInputConnection(outAttrs);
        //判断是否为number类型
        if (outAttrs.inputType != 0 && isContain(outAttrs.inputType, InputType.TYPE_CLASS_NUMBER)) {
            outAttrs.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER;
        }
        return connection;
    }

    private boolean isContain(int a, int b) {
        int result = a | b;
        return result == a || result == b;
    }

    /**
     * 执行JS(旧版本API会自动调用loadUrl,但不会回调resultCallback)
     */
    @Override
    @SuppressLint("NewApi")
    public void evaluateJavascript(String script, ValueCallback<String> resultCallback) {
        if (!TextUtils.isEmpty(script)) {
            if (useLoadUrlMethod) {
                //使用loadUrl方法
                logger.i("loadUrl method to execute js");
                loadUrl("javascript:" + script);
            } else {
                try {
                    //尝试使用evaluateJavascript方法
                    logger.i("evaluateJavascript method to execute js");
                    super.evaluateJavascript(script, resultCallback);
                } catch (Exception e) {//调用evaluateJavascript错误,则使用loadUrl
                    logger.e("evaluateJavascript method called failed, try to use loadUrl");
                    loadUrl("javascript:" + script);
                    useLoadUrlMethod = true;
                }
            }
        }
    }

}