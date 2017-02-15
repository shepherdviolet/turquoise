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

package sviolet.turquoise.model.network;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * <p>[OKHttp:CookieJar]在内存中保持Cookie, 并能够与WebView中的cookie同步</p>
 *
 * <p>required: com.squareup.okhttp3:okhttp</p>
 *
 * <pre>{@code
 *      new OkHttpClient.Builder()
 *          .cookieJar(new WebViewSyncOKCookieJar())
 *          .build();
 * }</pre>
 *
 * Created by S.Violet on 2017/2/15.
 */
public class WebViewSyncOKCookieJar extends MemoryOKCookieJar {

    public void syncFromWebView(Context context, String url) throws InvalidUrlException {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null){
            throw new InvalidUrlException(url);
        }
        logger.d("[WebViewSyncOKCookieJar]sync from WebView, url:" + httpUrl.host());
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        String cookieString = cookieManager.getCookie(httpUrl.host());
        if (cookieString == null){
            logger.d("[WebViewSyncOKCookieJar]no cookie in WebView, url:" + httpUrl.host());
            return;
        }
        List<Cookie> cookieList = parseCookieFromWebView(httpUrl, cookieString);
        if (cookieList == null){
            logger.d("[WebViewSyncOKCookieJar]invalid cookie in WebView, url:" + httpUrl.host());
            return;
        }
        cookieDataMap.put(httpUrl.host(), cookieList);
        logger.d("[WebViewSyncOKCookieJar]sync from WebView succeed, url:" + httpUrl.host());
    }

    public void syncToWebView(Context context, String url) throws InvalidUrlException {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null){
            throw new InvalidUrlException(url);
        }
        logger.d("[WebViewSyncOKCookieJar]sync to WebView, url:" + httpUrl.host());
        List<Cookie> cookieList = cookieDataMap.get(httpUrl.host());
        if (cookieList == null || cookieList.size() <= 0){
            CookieSyncManager.createInstance(context);
            CookieSyncManager.getInstance().startSync();
            CookieManager.getInstance().removeAllCookie();
            CookieManager.getInstance().removeSessionCookie();
            logger.d("[WebViewSyncOKCookieJar]sync to WebView succeed, 0 cookie, url:" + httpUrl.host());
        } else {
            CookieSyncManager.createInstance(context);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeAllCookie();
            for (Cookie cookie : cookieList){
                if (cookie == null){
                    continue;
                }
                cookieManager.setCookie(httpUrl.host(), cookie.toString());
            }
            CookieSyncManager.getInstance().sync();
            logger.d("[WebViewSyncOKCookieJar]sync to WebView succeed, " + cookieList.size() + " cookie, url:" + httpUrl.host());
        }
    }

    protected List<Cookie> parseCookieFromWebView(HttpUrl httpUrl, String webViewCookieString){
        if (webViewCookieString == null || webViewCookieString.length() <= 0){
            return null;
        }
        //切割cookie字符串
        String[] cookieArray = webViewCookieString.split(";");
        if (cookieArray.length <= 0){
            return null;
        }
        //遍历
        List<Cookie> cookieList = new ArrayList<>();
        for (String cookieString : cookieArray){
            Cookie cookie = Cookie.parse(httpUrl, cookieString);
            if (cookie == null){
                continue;
            }
            cookieList.add(cookie);
        }
        return cookieList;
    }

    public static class InvalidUrlException extends Exception{

        private InvalidUrlException(String url){
            super("[WebViewSyncOKCookieJar]invalid url:" + url);
        }

    }

}
