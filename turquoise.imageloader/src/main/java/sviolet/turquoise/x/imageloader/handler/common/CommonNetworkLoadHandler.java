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

package sviolet.turquoise.x.imageloader.handler.common;

import android.content.Context;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import sviolet.turquoise.util.common.CheckUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.EngineCallback;

/**
 *
 * Created by S.Violet on 2016/3/22.
 */
public class CommonNetworkLoadHandler implements NetworkLoadHandler {

    private static final int DEFAULT_CONNECTION_TIMEOUT = 3000;
    private static final int DEFAULT_READ_TIMEOUT = 3000;
    private static final int MAXIMUM_REDIRECT_TIMES = 5;

    private Map<String, String> headers;
    private int connectTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private int readTimeout = DEFAULT_READ_TIMEOUT;

    @Override
    public void onHandle(Context applicationContext, Context context, Task.Info taskInfo, EngineCallback<Result> callback, TLogger logger) {
        try{
            InputStream inputStream = load(new URL(taskInfo.getUrl()), null, 0, callback);
            if (inputStream == null){
                throw new Exception("[CommonNetworkLoadHandler]get a null inputStream");
            }
            callback.setResultSucceed(new Result(inputStream));
        } catch (Exception e) {
            if (callback.isCancelling()){
                callback.setResultCanceled();
            }else{
                callback.setResultFailed(e);
            }
        }
    }

    private InputStream load(URL url, URL prevUrl, int redirectTimes, EngineCallback<Result> callback) throws Exception {
        if (redirectTimes >= MAXIMUM_REDIRECT_TIMES) {
            throw new Exception("[CommonNetworkLoadHandler]redirect times > maximum(" + MAXIMUM_REDIRECT_TIMES + ")");
        } else {
            try {
                if (prevUrl != null && url.toURI().equals(prevUrl.toURI())) {
                    throw new Exception("[CommonNetworkLoadHandler]the url redirect to itself, url:" + url.toString());
                }
            } catch (URISyntaxException ignored) {
            }
        }

        HttpURLConnection connection = null;

        try {
            //open
            connection = (HttpURLConnection) url.openConnection();
            //add headers
            if (headers != null) {
                for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                    connection.addRequestProperty(headerEntry.getKey(), headerEntry.getValue());
                }
            }
            //setting
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            //connect
            connection.connect();

            //check cancel state
            if (callback.isCancelling()) {
                throw new Exception("[CommonNetworkLoadHandler]loading is been canceled");
            }

            //check statusCode
            final int statusCode = connection.getResponseCode();
            if (statusCode / 100 == 2) {
                return connection.getInputStream();
            } else if (statusCode / 100 == 3) {
                String redirectUrl = connection.getHeaderField("Location");
                if (CheckUtils.isEmpty(redirectUrl)) {
                    throw new Exception("[CommonNetworkLoadHandler]redirect url is null");
                }
                return load(new URL(url, redirectUrl), url, redirectTimes + 1, callback);
            } else if (statusCode == -1) {
                throw new Exception("[CommonNetworkLoadHandler]connect failed, statusCode:" + statusCode);
            } else {
                throw new Exception("[CommonNetworkLoadHandler]connect failed, statusCode:" + statusCode + " responseMessage" + connection.getResponseMessage());
            }
        }catch (Exception e){
            if (connection != null){
                connection.disconnect();
            }
            throw e;
        }
    }

    public CommonNetworkLoadHandler addHeader(String key, String value){
        if (headers == null){
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
    }

    public CommonNetworkLoadHandler setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public CommonNetworkLoadHandler setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }
}
