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

package sviolet.turquoise.x.imageloader.handler.common;

import android.content.Context;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import sviolet.thistle.util.judge.CheckUtils;
import sviolet.turquoise.x.common.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.node.Task;

/**
 * <p>common implementation of NetworkLoadHandler</p>
 *
 * @author S.Violet
 */
public class CommonNetworkLoadHandler extends AbstractNetworkLoadHandler {

    private static final int MAXIMUM_REDIRECT_TIMES = 5;

    private Map<String, String> headers;

    /**
     * <p>CAUTION:</p>
     *
     * <p>You should call "callback.setResultSucceed()"/"callback.setResultFailed()"/"callback.setResultCanceled()"
     * when process finished, whether loading succeed or failed. if not, NetworkEngine's thread will be block for a long time,
     * until NetworkCallback timeout.Because NetworkEngine will invoke callback.getResult, this method will block thread util you setResult.</p>
     */
    @Override
    public void onHandle(Context applicationContext, Context context, Task.Info taskInfo, NetworkCallback<Result> callback, long connectTimeout, long readTimeout, long imageDataLengthLimit, TLogger logger) {
        try{
            //load
            Result result = load(new URL(taskInfo.getUrl()), null, 0, callback, connectTimeout, readTimeout);
            if (result == null){
                throw new Exception("[CommonNetworkLoadHandler]get a null inputStream");
            }
            //load succeed, callback
            callback.setResultSucceed(result);
        } catch (Exception e) {
            //load canceled/failed, callback
            if (callback.isCancelling()){
                callback.setResultCanceled();
            }else{
                callback.setResultFailed(e);
            }
        }
    }

    /**
     * @param url current url
     * @param prevUrl last url
     * @param redirectTimes redirect times
     * @param callback callback
     * @return WriteResult
     */
    private Result load(URL url, URL prevUrl, int redirectTimes, NetworkCallback<Result> callback, long connectTimeout, long readTimeout) throws Exception {
        //skip when redirect too many times
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
            connection.setConnectTimeout((int) connectTimeout);
            connection.setReadTimeout((int) readTimeout);
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
                //succeed
                Result result = new Result(connection.getInputStream());
                if (CheckUtils.isEmpty(connection.getContentEncoding())){
                    result.setLength(connection.getContentLength());
                }
                return result;
            } else if (statusCode / 100 == 3) {
                //redirect
                String redirectUrl = connection.getHeaderField("Location");
                if (CheckUtils.isEmpty(redirectUrl)) {
                    throw new Exception("[CommonNetworkLoadHandler]redirect url is null");
                }
                return load(new URL(url, redirectUrl), url, redirectTimes + 1, callback, connectTimeout, readTimeout);
            } else if (statusCode == -1) {
                //failed
                throw new Exception("[CommonNetworkLoadHandler]connect failed, statusCode:" + statusCode);
            } else {
                throw new Exception("[CommonNetworkLoadHandler]connect failed, statusCode:" + statusCode + " responseMessage" + connection.getResponseMessage());
            }
        }catch (Exception e){
            //disconnect when exception
            if (connection != null){
                connection.disconnect();
            }
            throw e;
        }
    }

    /**
     * add header of http
     * @param key header key
     * @param value header value
     */
    public CommonNetworkLoadHandler addHeader(String key, String value){
        if (headers == null){
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
    }

}
