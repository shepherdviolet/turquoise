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

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;

/**
 * <p>WebView简易示例</p>
 *
 * <p>-------------------------------------------------------------------------------------------------------------</p>
 *
 * <p>漏洞笔记:</p>
 *
 * <p>
 *  安全公告编号:CNTA-2018-0005
 *      2017年12月7日，国家信息安全漏洞共享平台（CNVD）接收到腾讯玄武实验室报送的Android WebView存在跨域访问漏洞(CNVD-2017-36682)。攻击者利用该漏洞，可远程获取用户隐私数据（包括手机应用数据、照片、文档等敏感信息），还可窃取用户登录凭证，在受害者毫无察觉的情况下实现对APP用户账户的完全控制。由于该组件广泛应用于Android平台，导致大量APP受影响，构成较为严重的攻击威胁。
 *  一、漏洞情况分析
 *      WebView是Android用于显示网页的控件，是一个基于Webkit引擎、展现web页面的控件。WebView控件功能除了具有一般View的属性和设置外，还可对URL请求、页面加载、渲染、页面交互进行处理。
 *  该漏洞产生的原因是在Android应用中，WebView开启了file域访问，允许file域访问http域，且未对file域的路径进行严格限制所致。攻击者通过URL Scheme的方式，可远程打开并加载恶意HTML文件，远程获取APP中包括用户登录凭证在内的所有本地敏感数据。
 *  漏洞触发成功前提条件如下：
 *      1.WebView中setAllowFileAccessFromFileURLs 或setAllowUniversalAccessFromFileURLsAPI配置为true；
 *      2.WebView可以直接被外部调用，并能够加载外部可控的HTML文件。
 *      CNVD对相关漏洞综合评级为“高危”。
 *  二、漏洞影响范围
 *      漏洞影响使用WebView控件，开启file域访问并且未按安全策略开发的Android应用APP。
 *  三、漏洞修复建议
 *      厂商暂未发布解决方案，临时解决方案如下：
 *      1. file域访问为非功能需求时，手动配置setAllowFileAccessFromFileURLs或setAllowUniversalAccessFromFileURLs两个API为false。（Android4.1版本之前这两个API默认是true，需要显式设置为false）
 *      2. 若需要开启file域访问，则设置file路径的白名单，严格控制file域的访问范围，具体如下：
 *          （1）固定不变的HTML文件可以放在assets或res目录下，file:///android_asset和file:///android_res 在不开启API的情况下也可以访问；
 *          （2）可能会更新的HTML文件放在/data/data/(app) 目录下，避免被第三方替换或修改；
 *          （3）对file域请求做白名单限制时，需要对“../../”特殊情况进行处理，避免白名单被绕过。
 *      3. 避免App内部的WebView被不信任的第三方调用。排查内置WebView的Activity是否被导出、必须导出的Activity是否会通过参数传递调起内置的WebView等。
 *      4. 建议进一步对APP目录下的敏感数据进行保护。客户端APP应用设备相关信息（如IMEI、IMSI、Android_id等）作为密钥对敏感数据进行加密。使攻击者难以利用相关漏洞获得敏感信息。</p>
 *
 * @author S.Violet
 */

//Demo描述
@DemoDescription(
        title = "WebView Demo",
        type = "View",
        info = "Demo of WebView"
)
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
    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        WebSettings settings = webView.getSettings();

        //避免跨域访问漏洞(实际上默认一般就是false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(false);
            settings.setAllowUniversalAccessFromFileURLs(false);
        }

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSaveFormData(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setJavaScriptEnabled(true);
//		settings.setSupportZoom(true);
//		settings.setBuiltInZoomControls(true);

        webView.removeJavascriptInterface("searchBoxJavaBridge_");

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
