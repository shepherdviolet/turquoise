/*
 * Copyright (C) 2015 S.Violet
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

package sviolet.turquoise.common.statics;

import android.view.View;

import sviolet.turquoise.ui.adapter.RecyclingPagerAdapter;
import sviolet.turquoise.ui.adapter.TRecyclerViewHolder;
import sviolet.turquoise.ui.adapter.TViewHolder;

/**
 * <p>Public constants</p>
 *
 * <p>To avoid constants conflict</p>
 *
 * Created by S.Violet on 2016/1/13.
 */
public class PublicConstants {

    /**
     * Tag id of View, for {@link View#setTag(int, Object)}
     */
    public static class ViewTag{

        /**
         * for {@link TViewHolder}/{@link TRecyclerViewHolder}, bind the ViewHolder into View by setTag method
         */
        public static final int ViewHolder = 0xfff77f01;

        /**
         * for TILoader's Stub : sviolet.turquoise.x.imageloader.stub.Stub, to bind Stub on View as a Tag<br/>
         */
        public static final int TILoaderStub = 0xfff77f02;

        /**
         * to record item position of {@link RecyclingPagerAdapter}
         */
        public static final int RecyclingPagerAdapterPosition = 0xfff77f03;

    }

    /**
     * Request code of Activity
     */
    public static class ActivityRequestCode{

        /**
         * request code of runtime permission task, for {@link sviolet.turquoise.enhance.app.utils.RuntimePermissionManager}
         */
        public static final int RuntimePermissionTaskStart = 17000;//start code, must < 65535
        public static final int RuntimePermissionTaskMax = 100;//quantity

        /**
         * request code of activity result callback manager, for {@link sviolet.turquoise.enhance.app.utils.ActivityResultCallbackManager}
         */
        public static final int ActivityResultCallbackStart = 17200;//start code, must < 65535
        public static final int ActivityResultCallbackMax = 100;//quantity

    }

}
