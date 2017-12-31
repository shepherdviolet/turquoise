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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.EngineCallback;

/**
 * <p>NetworkLoadHandler on okhttp</p>
 *
 * <p>dependency::com.squareup.okhttp3:okhttp:3.3.1</p>
 *
 * Created by S.Violet on 2016/6/15.
 */
public class OkHttpNetworkLoadHandler implements NetworkLoadHandler {

    private OkHttpClient okHttpClient;

    private Map<String, String> headers;

    public OkHttpNetworkLoadHandler(OkHttpClient okHttpClient){
        if (okHttpClient == null) {
            throw new NullPointerException("[OkHttpNetworkLoadHandler]okHttpClient is null!!!");
        }
        this.okHttpClient = okHttpClient;
    }

    /**
     * <p>CAUTION:</p>
     *
     * <p>You should call "callback.setResultSucceed()"/"callback.setResultFailed()"/"callback.setResultCanceled()"
     * when process finished, whether loading succeed or failed. if not, NetEngine's thread will be block for a long time,
     * until EngineCallback timeout.Because NetEngine will invoke callback.getResult, this method will block thread util you setResult.</p>
     */
    @Override
    public void onHandle(Context applicationContext, Context context, Task.Info taskInfo, final EngineCallback<Result> callback, long connectTimeout, long readTimeout, TLogger logger) {
        Request.Builder requestBuilder = new Request.Builder().url(taskInfo.getUrl());
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
//        requestBuilder.post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), ""));//post报文体
        Request request = requestBuilder.build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body == null){
                        callback.setResultFailed(new Exception("[OkHttpNetworkLoadHandler] error, no response body"));
                        return;
                    }
                    callback.setResultSucceed(new Result(body.byteStream()).setLength((int) body.contentLength()));
                } else {
                    callback.setResultFailed(new Exception("[OkHttpNetworkLoadHandler] error code:" + response.code() + ", error message:" + response.message()));
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback.isCancelling()){
                    callback.setResultCanceled();
                } else {
                    callback.setResultFailed(e);
                }
            }
        });
    }

    /**
     * add header of http
     * @param key header key
     * @param value header value
     */
    public OkHttpNetworkLoadHandler addHeader(String key, String value){
        if (headers == null){
            headers = new HashMap<>(0);
        }
        headers.put(key, value);
        return this;
    }

}
