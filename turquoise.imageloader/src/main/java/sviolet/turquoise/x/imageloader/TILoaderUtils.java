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
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.ServerSettings;
import sviolet.turquoise.x.imageloader.stub.Stub;

/**
 *
 * Created by S.Violet on 2016/3/15.
 */
public class TILoaderUtils {

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
    public static boolean isImageResourceEqual(ImageResource<?> src, ImageResource<?> dst){
        ComponentManager.getInstance().waitingForInitialized();
        return ComponentManager.getInstance().getServerSettings().getImageResourceHandler().isEqual(src, dst);
    }

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * recycle imageResource
     * @param resource imageResource which will be recycled
     * @return true:recycled
     */
    public static boolean recycleImageResource(ImageResource<?> resource){
        ComponentManager.getInstance().waitingForInitialized();
        return ComponentManager.getInstance().getServerSettings().getImageResourceHandler().recycle(resource);
    }

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * convert ImageResource to drawable
     * @param resource ImageResource
     * @return drawable
     */
    public static Drawable imageResourceToDrawable(ImageResource<?> resource){
        ComponentManager.getInstance().waitingForInitialized();
        return ComponentManager.getInstance().getServerSettings().getImageResourceHandler().toDrawable(resource);
    }

    /**
     * [Initialize TILoader]this method will initialize TILoader<br/>
     * reload View which has been canceled
     * @param view view
     * @return true:this view can be reload
     */
    public static boolean reloadView(View view){
        ComponentManager.getInstance().waitingForInitialized();
        ComponentManager.getInstance().getLogger().i("[TILoaderUtils]reloadView");
        synchronized (view) {
            //get Stub from View Tag
            Object tag = view.getTag(SpecialResourceId.ViewTag.TILoaderStub);
            //destroy obsolete Stub
            if (tag != null && tag instanceof Stub) {
                return ((Stub) tag).relaunch() == Stub.LaunchResult.SUCCEED;
            }
        }
        return false;
    }

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

}
