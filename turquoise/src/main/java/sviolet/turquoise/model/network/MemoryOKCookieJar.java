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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import sviolet.turquoise.common.statics.StringConstants;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * <p>[OKHttp:CookieJar]在内存中保持Cookie</p>
 *
 * <p>required: com.squareup.okhttp3:okhttp</p>
 *
 * <pre>{@code
 *      new OkHttpClient.Builder()
 *          .cookieJar(new MemoryOKCookieJar())
 *          .build();
 * }</pre>
 *
 * Created by S.Violet on 2017/2/15.
 */
public class MemoryOKCookieJar implements CookieJar {

    protected TLogger logger = TLogger.get(this, StringConstants.OK_HTTP_TAG);

    protected Map<String, List<Cookie>> cookieDataMap = new HashMap<>();
    private CookieFilter cookieFilter;

    @Override
    public final void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        logger.d("[MemoryOKCookieJar]new cookie received, url:" + url.host());
        //拦截
        if (interceptNewCookieReceived(url.host(), cookies)) {
            logger.d("[MemoryOKCookieJar]new cookie saved, url:" + url.host());
            //保存
            cookieDataMap.put(url.host(), cookies);
            afterNewCookieSaved();
        }
    }

    @Override
    public final List<Cookie> loadForRequest(HttpUrl url) {
        //获取
        List<Cookie> cookies = cookieDataMap.get(url.host());
        if (cookies == null){
            logger.d("[MemoryOKCookieJar]send empty cookie, url:" + url.host());
            return new ArrayList<>();
        }
        logger.d("[MemoryOKCookieJar]send cookie from cookieJar, url:" + url.host());
        //没有过滤器直接返回
        if (cookieFilter == null){
            return cookies;
        }
        //过滤器过滤
        List<Cookie> result = new ArrayList<>();
        for (Cookie cookie : cookies){
            if (cookieFilter.filter(cookie.name())){
                result.add(cookie);
            }
        }
        return result;
    }

    /**
     * 当服务端返回新的cookie时, 该方法会拦截事件, 若返回true, 则保存cookie, 返回false, 则不保存该cookie
     */
    protected boolean interceptNewCookieReceived(String hostUrl, List<Cookie> cookies){
        return true;
    }

    /**
     * 当服务端返回的新cookie被保存在内存中后, 会回调该方法
     */
    protected void afterNewCookieSaved(){

    }

    /**
     * 设置cookie过滤器
     */
    public void setCookieFilter(CookieFilter cookieFilter) {
        this.cookieFilter = cookieFilter;
    }

    public interface CookieFilter{

        /**
         * 根据key判断cookie值是否要送给服务端, true:送给服务端 false:不送给服务端
         */
        boolean filter(String cookieKeyName);

    }

}
