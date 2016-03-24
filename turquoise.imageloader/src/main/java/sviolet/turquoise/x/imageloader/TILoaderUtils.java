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

import android.view.View;

import sviolet.turquoise.x.imageloader.entity.ImageResource;

/**
 *
 * Created by S.Violet on 2016/3/15.
 */
public class TILoaderUtils {

    /**
     * check if ImageResource Valid
     * @param resource ImageResource
     * @return true:valid
     */
    public static boolean isImageResourceValid(ImageResource resource){
        return ComponentManager.getInstance().getServerSettings().getImageResourceHandler().isValid(resource);
    }

    /**
     * reload View which has been canceled
     * @param view view
     * @return true:this view can be reload
     */
    public static boolean reloadView(View view){
        return false;
    }

}