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
 * <p>parameter of loading</p>
 *
 * Created by S.Violet on 2016/2/16.
 */
public class Params {

    private static class Values{

        private int reqWidth = SIZE_MATCH_RESOURCE;
        private int reqHeight = SIZE_MATCH_RESOURCE;
        private boolean sizeMatchView = true;
        private DecodeHandler.DecodeStrategy decodeStrategy = DecodeHandler.DecodeStrategy.APPROXIMATE_SCALE;
        private DecodeHandler.Interceptor decodeInterceptor;
        private Bitmap.Config bitmapConfig = DEFAULT_BITMAP_CONFIG;

        /**
         * you must implement cloning method, including all values.
         * TILoader will get copy by this method before loading.
         */
        public Values copy() {
            Values newValues = new Values();
            newValues.reqWidth = reqWidth;
            newValues.reqHeight = reqHeight;
            newValues.sizeMatchView = sizeMatchView;
            newValues.decodeStrategy = decodeStrategy;
            newValues.decodeInterceptor = decodeInterceptor;
            newValues.bitmapConfig = bitmapConfig;
            return newValues;
        }
    }

    public static class Builder{

        private Values values;

        public Builder(){
            values = new Values();
        }

        /**
         * <p>There are two modes to determine the size of loading image.</p>
         *
         * <p>If you call this method, means you choose mode of DimensionSpecified.</p>
         *
         * <p>*******************************************************************</p>
         *
         * <p>SizeMatchView Mode:: default mode, do not call this method (setReqSize(int, int)).</p>
         *
         * <p>Scene: For fixed size View (not wrap_content) or fixed size LoadingDrawable. In this mode, view will
         * match it's own size or match loadingDrawable's size, and image will match View's size.</p>
         *
         * <p>WARNING: In this mode, If your view's size is wrap_content, and LoadingDrawable's size is -1 in the meanwhile,
         * TILoader will skip loading.</p>
         *
         * <p>*******************************************************************</p>
         *
         * <p>DimensionSpecified Mode:: set by call this method (setReqSize(int, int)).</p>
         *
         * <p>Scene: Know pictures size in advance, or set a size which you want. In this mode,
         * view / LoadingDrawable / FailedDrawable / loaded image, all of them will match this reqSize.
         * you can set view wrap_content. Image will match reqSize.</p>
         *
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
         * <p>if you set sizeMatchView true, the reqSize will follow view's size, true by default</p>
         *
         * @param matchView true:image size match view
         */
        private Builder setSizeMatchView(boolean matchView){
            values.sizeMatchView = matchView;
            return this;
        }

        /**
         * <p>decoding strategy</p>
         *
         * <p>APPROXIMATE_SCALE::scale image appropriately by reqWidth/reqHeight, to save memory</p>
         *
         * <p>ACCURATE_SCALE::scale image to reqWidth * reqHeight accurately</p>
         *
         * <p>NO_SCALE::do not scale, keep origin size</p>
         *
         * @param decodeStrategy see:{@link DecodeHandler.DecodeStrategy}
         */
        public Builder setDecodeStrategy(DecodeHandler.DecodeStrategy decodeStrategy){
            values.decodeStrategy = decodeStrategy;
            return this;
        }

        /**
         * set BitmapConfig
         * @param bitmapConfig bitmapConfig
         */
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

        /**
         * @return build Params
         */
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

    public DecodeHandler.DecodeStrategy getDecodeStrategy(){
        return values.decodeStrategy;
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
            if (width > 1 && height > 1){
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
