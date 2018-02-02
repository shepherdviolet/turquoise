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

package sviolet.turquoise.x.imageloader.entity;

import android.graphics.Bitmap;
import android.view.View;

import java.util.Map;

import sviolet.turquoise.util.bitmap.BitmapUtils;
import sviolet.turquoise.x.imageloader.drawable.FailedDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.LoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;

/**
 * <p>parameter of loading</p>
 *
 * Created by S.Violet on 2016/2/16.
 */
public class Params {

    private static class Values{

        private SourceType sourceType = DEFAULT_SOURCE_TYPE;
        private int reqWidth = SIZE_MATCH_RESOURCE;
        private int reqHeight = SIZE_MATCH_RESOURCE;
        private boolean sizeMatchView = true;

        private BitmapUtils.InSampleQuality decodeInSampleQuality = BitmapUtils.InSampleQuality.MEDIUM;
        private DecodeHandler.DecodeScaleStrategy decodeScaleStrategy = DecodeHandler.DecodeScaleStrategy.NO_SCALE;
        private DecodeHandler.Interceptor decodeInterceptor;
        private Bitmap.Config bitmapConfig = DEFAULT_BITMAP_CONFIG;

        private boolean indispensable = false;
        private boolean skipSameUrlInSameView = false;

        private int extensionLoadingDrawableFactoryIndex = -1;
        private int extensionFailedDrawableFactoryIndex = -1;
        private int extensionBackgroundDrawableFactoryIndex = -1;

        private int imageAppearDuration = -1;

        private Map<String, Object> extras;

        /**
         * you must implement cloning method, including all values.
         * TILoader will get copy by this method before loading.
         */
        public Values copy() {
            Values newValues = new Values();
            newValues.sourceType = sourceType;
            newValues.reqWidth = reqWidth;
            newValues.reqHeight = reqHeight;
            newValues.sizeMatchView = sizeMatchView;

            newValues.decodeInSampleQuality = decodeInSampleQuality;
            newValues.decodeScaleStrategy = decodeScaleStrategy;
            newValues.decodeInterceptor = decodeInterceptor;
            newValues.bitmapConfig = bitmapConfig;

            newValues.indispensable = indispensable;
            newValues.skipSameUrlInSameView = skipSameUrlInSameView;

            newValues.extensionLoadingDrawableFactoryIndex = extensionLoadingDrawableFactoryIndex;
            newValues.extensionFailedDrawableFactoryIndex = extensionFailedDrawableFactoryIndex;
            newValues.extensionBackgroundDrawableFactoryIndex = extensionBackgroundDrawableFactoryIndex;

            newValues.imageAppearDuration = imageAppearDuration;

            //reference copy
            newValues.extras = extras;
            return newValues;
        }
    }

    public static class Builder{

        private Values values;

        public Builder(){
            values = new Values();
        }

        /**
         * <p>Specify where to get the image.</p>
         *
         * TODO document
         *
         * @param sourceType Specify where to get the image. HTTP_GET by default.
         */
        public Builder setSourceType(SourceType sourceType) {
            if (sourceType == null){
                throw new RuntimeException("[Params]sourceType is null");
            }
            values.sourceType = sourceType;
            return this;
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
         * <p>There are two steps in DecodeHandler: decode and scale.</p>
         *
         * <p>Step 1: Decode........................................................................</p>
         *
         * <p>Decode image from file/bytes, keep aspect ratio. The inSampleSize is calculated according
         * to the reqWidth/reqHeight, and you will get smaller image which take up less memory. In general,
         * we use InSampleQuality.MEDIUM option.</p>
         *
         * Params->decodeInSampleQuality:<br/>
         * BitmapUtils.InSampleQuality.ORIGINAL: decode into original size (highest quality), take up more memory.<br/>
         * BitmapUtils.InSampleQuality.HIGH: calculate appropriate inSampleSize (higher quality) and decode into smaller image, take up less memory.<br/>
         * BitmapUtils.InSampleQuality.MEDIUM: calculate appropriate inSampleSize (medium quality) and decode into smaller image, take up less memory, default option<br/>
         * BitmapUtils.InSampleQuality.LOW: calculate appropriate inSampleSize (low quality) and decode into smaller image, take up less memory.<br/>
         *
         * <p>Step 2: Scale........................................................................</p>
         *
         * <p>Scale image to specified size. In general, we use DecodeScaleStrategy.NO_SCALE.</p>
         *
         * NO_SCALE::do not scale, keep decoded size, keep aspect ratio, DEFAULT option.<br/>
         * SCALE_FIT_WIDTH_HEIGHT:scale image to reqWidth * reqHeight, ignore aspect ratio.<br/>
         * SCALE_FIT_WIDTH:scale image's width to reqWidth, keep aspect ratio<br/>
         * SCALE_FIT_HEIGHT:scale image's height to reqHeight, keep aspect ratio<br/>
         *
         * @param decodeInSampleQuality BitmapUtils.InSampleQuality.MEDIUM by default
         */
        public Builder setDecodeInSampleQuality(BitmapUtils.InSampleQuality decodeInSampleQuality){
            values.decodeInSampleQuality = decodeInSampleQuality;
            return this;
        }

        /**
         * <p>There are two steps in DecodeHandler: decode and scale.</p>
         *
         * <p>Step 1: Decode........................................................................</p>
         *
         * <p>Decode image from file/bytes, keep aspect ratio. The inSampleSize is calculated according
         * to the reqWidth/reqHeight, and you will get smaller image which take up less memory. In general,
         * we use InSampleQuality.MEDIUM option.</p>
         *
         * Params->decodeInSampleQuality:<br/>
         * BitmapUtils.InSampleQuality.ORIGINAL: decode into original size (highest quality), take up more memory.<br/>
         * BitmapUtils.InSampleQuality.HIGH: calculate appropriate inSampleSize (higher quality) and decode into smaller image, take up less memory.<br/>
         * BitmapUtils.InSampleQuality.MEDIUM: calculate appropriate inSampleSize (medium quality) and decode into smaller image, take up less memory, default option<br/>
         * BitmapUtils.InSampleQuality.LOW: calculate appropriate inSampleSize (low quality) and decode into smaller image, take up less memory.<br/>
         *
         * <p>Step 2: Scale........................................................................</p>
         *
         * <p>Scale image to specified size. In general, we use DecodeScaleStrategy.NO_SCALE.</p>
         *
         * NO_SCALE::do not scale, keep decoded size, keep aspect ratio, DEFAULT option.<br/>
         * SCALE_FIT_WIDTH_HEIGHT:scale image to reqWidth * reqHeight, ignore aspect ratio.<br/>
         * SCALE_FIT_WIDTH:scale image's width to reqWidth, keep aspect ratio<br/>
         * SCALE_FIT_HEIGHT:scale image's height to reqHeight, keep aspect ratio<br/>
         *
         * @param decodeScaleStrategy NO_SCALE by default
         */
        public Builder setDecodeScaleStrategy(DecodeHandler.DecodeScaleStrategy decodeScaleStrategy){
            values.decodeScaleStrategy = decodeScaleStrategy;
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
         * <p>"Indispensable" task will keep loading as far as possible, it has double connection-timeout & read-timeout,
         * and loading with {@link LowNetworkSpeedStrategy.Type#INDISPENSABLE_TASK} strategy.
         * Generally, it is used for loading large image and gif.</p>
         *
         * <p>You should use this option with caution, NetEngine's thread will be occupied by "dispensable" task
         * for a long time.</p>
         *
         * <p>False by default.</p>
         *
         * @see LowNetworkSpeedStrategy
         */
        public Builder setIndispensable(){
            values.indispensable = true;
            return this;
        }

        /**
         * <p>[Senior Params]For TILoader.node(context).load(...)/loadBackground(...) only, there is no effective in "extract" way.</p>
         *
         * <p>In generally, when you call TILoader.node(context).load(...)/loadBackground(...), we will
         * cancel previous task of target view, and launch new task in any case, even if the previous task's
         * url is same as new task. If you refresh UI, you will find the phenomenon of image blinking.</p>
         *
         * <p>In some environments, for instance, you want to refresh a ListView by calling Adapter.notifyDataSetChanges,
         * to avoid the phenomenon of image blinking, you can set this option ({@link Builder#skipSameUrlInSameView()}).
         * In this mode, if the url of previous task is same as new task, we will continue the previous one,
         * and cancel the new one.</p>
         *
         * <p>WARNING:: If you set this option ({@link Builder#skipSameUrlInSameView()}), you will not be able to
         * change loading params, we will cancel the new task always. You should not set this option
         * if you want to change load params of same url in same target view.</p>
         *
         */
        public Builder skipSameUrlInSameView(){
            values.skipSameUrlInSameView = true;
            return this;
        }

        /**
         * Specify extension LoadingDrawableFactory to display different effects in the same Node.
         * First, you should set {@link NodeSettings.Builder#setExtensionLoadingDrawableFactory(int, LoadingDrawableFactory)},
         * than you can set {@link Params.Builder#useExtensionLoadingDrawableFactory(int)} to specify
         * the extensionIndex, and the loading drawable will create by the extension factory.
         * @param extensionIndex extensionIndex relating to {@link NodeSettings.Builder#setExtensionLoadingDrawableFactory(int, LoadingDrawableFactory)}
         */
        public Builder useExtensionLoadingDrawableFactory(int extensionIndex){
            if (extensionIndex < 0){
                throw new RuntimeException("[Params]extensionIndex must >= 0");
            }
            values.extensionLoadingDrawableFactoryIndex = extensionIndex;
            return this;
        }

        /**
         * Specify extension FailedDrawableFactory to display different effects in the same Node.
         * First, you should set {@link NodeSettings.Builder#setExtensionFailedDrawableFactory(int, FailedDrawableFactory)},
         * than you can set {@link Params.Builder#useExtensionFailedDrawableFactory(int)} to specify
         * the extensionIndex, and the failed drawable will create by the extension factory.
         * @param extensionIndex extensionIndex relating to {@link NodeSettings.Builder#setExtensionFailedDrawableFactory(int, FailedDrawableFactory)}
         */
        public Builder useExtensionFailedDrawableFactory(int extensionIndex){
            if (extensionIndex < 0){
                throw new RuntimeException("[Params]extensionIndex must >= 0");
            }
            values.extensionFailedDrawableFactoryIndex = extensionIndex;
            return this;
        }

        /**
         * Specify extension BackgroundDrawableFactory to display different effects in the same Node.
         * First, you should set {@link NodeSettings.Builder#setExtensionBackgroundImageResId(int, int)}
         * or {@link NodeSettings.Builder#setExtensionBackgroundColor(int, int)},
         * than you can set {@link Params.Builder#useExtensionBackgroundDrawableFactory(int)} to specify
         * the extensionIndex, and the background will create by the extension factory.
         * @param extensionIndex extensionIndex relating to {@link NodeSettings.Builder#setExtensionBackgroundImageResId(int, int)}
         * or {@link NodeSettings.Builder#setExtensionBackgroundColor(int, int)}
         */
        public Builder useExtensionBackgroundDrawableFactory(int extensionIndex){
            if (extensionIndex < 0){
                throw new RuntimeException("[Params]extensionIndex must >= 0");
            }
            values.extensionBackgroundDrawableFactoryIndex = extensionIndex;
            return this;
        }

        /**
         * @param imageAppearDuration duration of image appear animation, {@value NodeSettings#DEFAULT_IMAGE_APPEAR_DURATION} by default
         */
        public Builder setImageAppearDuration(int imageAppearDuration){
            values.imageAppearDuration = imageAppearDuration;
            return this;
        }

        public Builder setExtras(Map<String, Object> extras){
            values.extras = extras;
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

    public static final SourceType DEFAULT_SOURCE_TYPE = SourceType.HTTP_GET;
    public static final int SIZE_MATCH_RESOURCE = 0;//size match resource (origin size)
    public static final Bitmap.Config DEFAULT_BITMAP_CONFIG = Bitmap.Config.RGB_565;

    //constructor////////////////////////////////////////////////////////////

    private Values values;

    private Params(Values values){
        this.values = values;
    }

    //getter/////////////////////////////////////////////////////////////////

    public SourceType getSourceType(){
        return values.sourceType;
    }

    public int getReqWidth() {
        return values.reqWidth;
    }

    public int getReqHeight() {
        return values.reqHeight;
    }

    public boolean isSizeMatchView(){
        return values.sizeMatchView;
    }

    public BitmapUtils.InSampleQuality getDecodeInSampleQuality(){
        return values.decodeInSampleQuality;
    }

    public DecodeHandler.DecodeScaleStrategy getDecodeScaleStrategy(){
        return values.decodeScaleStrategy;
    }

    public Bitmap.Config getBitmapConfig(){
        return values.bitmapConfig;
    }

    public DecodeHandler.Interceptor getDecodeInterceptor(){
        return values.decodeInterceptor;
    }

    public boolean isIndispensable(){
        return values.indispensable;
    }

    public boolean isSkipSameUrlInSameView(){
        return values.skipSameUrlInSameView;
    }

    public int getExtensionLoadingDrawableFactoryIndex(){
        return values.extensionLoadingDrawableFactoryIndex;
    }

    public int getExtensionFailedDrawableFactoryIndex(){
        return values.extensionFailedDrawableFactoryIndex;
    }

    public int getExtensionBackgroundDrawableFactoryIndex(){
        return values.extensionBackgroundDrawableFactoryIndex;
    }

    public int getImageAppearDuration(){
        return values.imageAppearDuration;
    }

    public Map<String, Object> getExtras(){
        return values.extras;
    }

    public Object getExtra(String key){
        if (values.extras != null){
            return values.extras.get(key);
        }
        return null;
    }

    public Integer getExtraInteger(String key){
        if (values.extras != null){
            Object value = values.extras.get(key);
            if (value instanceof Integer){
                return (Integer) value;
            }
            return null;
        }
        return null;
    }

    public String getExtraString(String key){
        if (values.extras != null){
            Object value = values.extras.get(key);
            if (value instanceof String){
                return (String) value;
            }
            return null;
        }
        return null;
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
        builder.append("@");
        builder.append(getSourceType());
        if (values.decodeInterceptor != null) {
            builder.append("@");
            builder.append(values.decodeInterceptor.getClass().hashCode());
        }
        return builder.toString();
    }

    public Params copy() {
        return new Params(values.copy());
    }

    //inner class//////////////////////////////////////////////////////////////////

    /**
     * Source type of image
     */
    public enum SourceType {
        /**
         * Get image from network (http get)
         */
        HTTP_GET,

        /**
         * Get image from local disk
         */
        LOCAL_DISK,

        /**
         * Get image from assets
         */
        APK_ASSETS
    }

}
