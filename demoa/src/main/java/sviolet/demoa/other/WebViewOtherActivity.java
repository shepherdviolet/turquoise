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
package sviolet.demoa.other;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import sviolet.demoa.R;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;

/**
 * WebView简易示例
 */
@ResourceId(R.layout.other_webview)
public class WebViewOtherActivity extends TActivity {

    @ResourceId(R.id.other_webview_webview)
    private WebView webView;
    @ResourceId(R.id.other_webview_progressbar)
    private ProgressBar progressBar;

    private String url;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        initView();

        webView.loadUrl("http://www.baidu.com");
    }

    @Override
    protected void afterDestroy() {
        //清除Cookies
//		CookieSyncManager.createInstance(this);
//		CookieManager cookieManager = CookieManager.getInstance();
//		cookieManager.removeAllCookie();
//		CookieSyncManager.getInstance().sync();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return true;
        }
        return false;
    }

    /**
     * 若网页可返回, 则网页返回
     */
    private void goBack() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    /*******************************************
     * init
     */

    /**
     * 初始化显示
     */
    private void initView() {
        WebSettings settings = webView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSaveFormData(false);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(false);
//        settings.setJavaScriptEnabled(true);
//		settings.setSupportZoom(true);
//		settings.setBuiltInZoomControls(true);

        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);
    }

    /*******************************************
     * inter
     */

    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            webView.loadUrl(url);
            return true;
        }
    };

    private WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            //此处可将原生标题设置为网页标题
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(newProgress);
            if (newProgress == 100) {
                progressBar.setVisibility(View.GONE);
            }
            super.onProgressChanged(view, newProgress);
        }

    };

}
