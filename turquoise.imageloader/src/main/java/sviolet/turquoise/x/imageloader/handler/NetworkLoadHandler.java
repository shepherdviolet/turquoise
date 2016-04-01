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

package sviolet.turquoise.x.imageloader.handler;

import android.content.Context;

import java.io.InputStream;

import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.EngineCallback;

/**
 *
 * Created by S.Violet on 2016/2/19.
 */
public interface NetworkLoadHandler {

    /**
     * load from net
     * @param applicationContext application context
     * @param context activity context, maybe null
     * @param task task
     * @param callback callback
     * @param logger logger
     */
    void onHandle(Context applicationContext, Context context, Task task, EngineCallback<Result> callback, TLogger logger);

    /**
     * network loading result (on succeed)
     */
    class Result{

        public static final int UNKNOWN_LENGTH = -1;

        private ResultType type = ResultType.NULL;
        private byte[] bytes;
        private InputStream inputStream;
        private int length = UNKNOWN_LENGTH;

        Result(byte[] bytes){
            if (bytes == null){
                this.type = ResultType.NULL;
                return;
            }
            this.type = ResultType.BYTES;
            this.bytes = bytes;
            this.length = bytes.length;
        }

        Result(InputStream inputStream){
            if (inputStream == null){
                this.type = ResultType.NULL;
                return;
            }
            this.type = ResultType.INPUTSTREAM;
            this.inputStream = inputStream;
        }

        public ResultType getType() {
            return type;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }

    /**
     * network loading result type
     */
    enum ResultType{
        NULL,
        BYTES,
        INPUTSTREAM
    }

}
