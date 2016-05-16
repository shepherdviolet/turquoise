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

package sviolet.turquoise.x.imageloader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.io.File;
import java.io.IOException;

import sviolet.turquoise.common.statics.SpecialResourceId;
import sviolet.turquoise.util.common.CheckUtils;
import sviolet.turquoise.util.droid.DirectoryUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.utilx.tlogger.TLoggerModule;
import sviolet.turquoise.utilx.tlogger.def.SimpleTLoggerModule;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.ServerSettings;
import sviolet.turquoise.x.imageloader.stub.Stub;
import sviolet.turquoise.x.imageloader.stub.StubRemoter;

/**
 * <p>Utils for TILoader</p>
 *
 * Created by S.Violet on 2016/3/15.
 */
public class TILoaderUtils {

    /***********************************************************************888
     * loading control / get loading info
     */

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * Get stub remoter from view, uses:<br/>
     * 0.get url of loading<br/>
     * 1.get state of loading<br/>
     * 2.get progress of loading<br/>
     * 3.relaunch canceled task<br/>
     * @param view view
     * @return return NULL_STUB_REMOTER if failed
     */
    public static StubRemoter getStubRemoter(View view){
        if (view == null){
            throw new RuntimeException("[TILoaderUtils]can't get stubRemoter from a null View");
        }
        ComponentManager.getInstance().waitingForInitialized();
        synchronized (view) {
            //get Stub from View Tag
            Object tag = view.getTag(SpecialResourceId.ViewTag.TILoaderStub);
            if (tag instanceof Stub) {
                //get remoter
                return ((Stub) tag).getStubRemoter();
            }
        }
        return StubRemoter.NULL_STUB_REMOTER;
    }

    /***********************************************************************888
     * ImageResource
     */

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * check if ImageResource Valid
     * @param resource ImageResource
     * @return true:valid
     */
    public static boolean isImageResourceValid(ImageResource resource){
        ComponentManager.getInstance().waitingForInitialized();
        return ComponentManager.getInstance().getServerSettings().getImageResourceHandler().isValid(resource);
    }

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * judge whether the two resource are equal.
     * @param src resource
     * @param dst resource
     * @return true:equal
     */
    public static boolean isImageResourceEqual(ImageResource src, ImageResource dst){
        ComponentManager.getInstance().waitingForInitialized();
        return ComponentManager.getInstance().getServerSettings().getImageResourceHandler().isEqual(src, dst);
    }

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * recycle imageResource
     * @param resource imageResource which will be recycled
     * @return true:recycled
     */
    public static boolean recycleImageResource(ImageResource resource){
        ComponentManager.getInstance().waitingForInitialized();
        return ComponentManager.getInstance().getServerSettings().getImageResourceHandler().recycle(resource);
    }

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * convert ImageResource to drawable
     * @param context context
     * @param resource ImageResource
     * @param skipDrawingException true:skip drawing exception of drawable, if true, it will not throw exceptions even when bitmap has recycled
     * @return drawable
     */
    public static Drawable imageResourceToDrawable(Context context, ImageResource resource, boolean skipDrawingException){
        ComponentManager.getInstance().waitingForInitialized();
        return ComponentManager.getInstance().getServerSettings().getImageResourceHandler().toDrawable(context, resource, skipDrawingException);
    }

    /***********************************************************************888
     * cache
     */

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * wipe disk cache data. if you try to wipe when TILoader is loading image, it may cause some problems,
     * make sure TILoader is not loading image.
     * @param context context
     * @param subPath default "TILoader" if null
     */
    public static void wipeDiskCache(Context context, String subPath) throws IOException {
        ComponentManager.getInstance().waitingForInitialized();
        //check subPath
        if (CheckUtils.isEmpty(subPath)){
            subPath = ServerSettings.DEFAULT_DISK_CACHE_SUB_PATH;
        }
        //external dir
        File file = DirectoryUtils.getExternalCacheDir(context);
        if (file != null){
            file = new File(file.getAbsoluteFile() + File.separator + subPath);
        }
        if (file != null && file.exists()){
            ComponentManager.getInstance().getDiskCacheServer().wipe(file);
            ComponentManager.getInstance().getLogger().i("[TILoaderUtils]external disk cache wiped");
        }
        //inner dir
        file = new File(DirectoryUtils.getInnerCacheDir(context).getAbsolutePath() + File.separator + subPath);
        if (file.exists()){
            ComponentManager.getInstance().getDiskCacheServer().wipe(file);
            ComponentManager.getInstance().getLogger().i("[TILoaderUtils]inner disk cache wiped");
        }

        ComponentManager.getInstance().getLogger().i("[TILoaderUtils]disk cache wiped");
    }

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * wipe TILoader's memory cache, all images will be recycled
     */
    public static void wipeMemoryCache(){
        ComponentManager.getInstance().waitingForInitialized();
        ComponentManager.getInstance().getMemoryCacheServer().removeAll();
        ComponentManager.getInstance().getLogger().i("[TILoaderUtils]memory cache wiped");
    }

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * get report of memory cache
     */
    public static String getMemoryCacheReport(){
        ComponentManager.getInstance().waitingForInitialized();
        return ComponentManager.getInstance().getMemoryCacheServer().getMemoryReport();
    }

    /***********************************************************************888
     * other
     */

    /**
     * set TILoader's log level, valid only in SimpleTLoggerModule, it will be invalid if you use custom TLoggerModule
     * @param level e.g. TLogger.ERROR | TLogger.INFO
     * @return true:set succeed, module is SimpleTLoggerModule, false:set failed, module is not SimpleTLoggerModule
     */
    public static boolean setLoggerLevel(int level){
        TLoggerModule module = TLogger.getModule();
        //valid in SimpleTLoggerModule
        if (module instanceof SimpleTLoggerModule){
            ((SimpleTLoggerModule) module).addRule(TILoader.TAG, new SimpleTLoggerModule.Rule(level));
            return true;
        }
        return false;
    }

}
