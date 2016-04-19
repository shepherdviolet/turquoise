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

package sviolet.turquoise.x.imageloader.entity;

import android.graphics.Bitmap;
import android.view.View;

import sviolet.turquoise.x.imageloader.handler.DecodeHandler;

/**
 *
 * Created by S.Violet on 2016/2/16.
 */
public class Params {

    private static class Values{

        private int reqWidth = SIZE_MATCH_RESOURCE;
        private int reqHeight = SIZE_MATCH_RESOURCE;
        private boolean sizeMatchView = true;
        private Bitmap.Config bitmapConfig = DEFAULT_BITMAP_CONFIG;
        private DecodeHandler.Interceptor decodeInterceptor;

        /**
         * you must implement cloning method, including all values.
         * TILoader will get copy by this method before loading.
         */
        public Values copy() {
            Values newValues = new Values();
            newValues.reqWidth = reqWidth;
            newValues.reqHeight = reqHeight;
            newValues.sizeMatchView = sizeMatchView;
            newValues.bitmapConfig = bitmapConfig;
            newValues.decodeInterceptor = decodeInterceptor;
            return newValues;
        }
    }

    public static class Builder{

        private Values values;

        public Builder(){
            values = new Values();
        }

        /**
         * if you set reqSize manually by this method, it will set sizeMatchView flag to false
         * @param reqWidth request width, image width will close to this value
         * @param reqHeight request height, image height will close to this value
         */
        public Builder setReqSize(int reqWidth, int reqHeight){
            values.reqWidth = reqWidth;
            values.reqHeight = reqHeight;
            setSizeMatchView(false);
            return this;
        }

        /**
         * if you set sizeMatchView true, the reqSize will follow view's size, true by default
         * @param matchView true:image size match view
         */
        public Builder setSizeMatchView(boolean matchView){
            values.sizeMatchView = matchView;
            return this;
        }

        public Builder setBitmapConfig(Bitmap.Config bitmapConfig){
            if (bitmapConfig == null){
                throw new RuntimeException("[TILoader:Params]setBitmapConfig: bitmapConfig is null");
            }
            values.bitmapConfig = bitmapConfig;
            return this;
        }

        /**
         * this interceptor will invoke after DecodeHandler.onDecode(...)
         * @param interceptor interceptor
         */
        public Builder setDecodeInterceptor(DecodeHandler.Interceptor interceptor){
            values.decodeInterceptor = interceptor;
            return this;
        }

        public Params build(){
            return new Params(values);
        }

    }

    //DEFAULT////////////////////////////////////////////////////////////

    public static final int SIZE_MATCH_RESOURCE = 0;//size match resource (origin size)
    public static final Bitmap.Config DEFAULT_BITMAP_CONFIG = Bitmap.Config.RGB_565;

    //constructor////////////////////////////////////////////////////////////

    private Values values;

    private Params(Values values){
        this.values = values;
    }

    //getter/////////////////////////////////////////////////////////////////

    public int getReqWidth() {
        return values.reqWidth;
    }

    public int getReqHeight() {
        return values.reqHeight;
    }

    public boolean isSizeMatchView(){
        return values.sizeMatchView;
    }

    public Bitmap.Config getBitmapConfig(){
        return values.bitmapConfig;
    }

    public DecodeHandler.Interceptor getDecodeInterceptor(){
        return values.decodeInterceptor;
    }

    //function////////////////////////////////////////////////////////////

    /**
     * @param view view
     * @return true if adjust succeed by view
     */
    public boolean adjustByView(View view){
        if (view == null){
            return false;
        }
        if (isSizeMatchView()){
            int width = view.getWidth();
            int height = view.getHeight();
            if (width > 0 && height > 0){
                values.reqWidth = width;
                values.reqHeight = height;
                return true;
            }
        }
        return false;
    }

    public String getKeySuffix(){
        StringBuilder builder = new StringBuilder("@");
        builder.append(getReqWidth());
        builder.append("x");
        builder.append(getReqHeight());
        if (values.decodeInterceptor != null) {
            builder.append("@");
            builder.append(values.decodeInterceptor.getClass().hashCode());
        }
        return builder.toString();
    }

    public Params copy() {
        return new Params(values.copy());
    }
}
