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

package sviolet.turquoise.x.imageloader.drawable;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import sviolet.turquoise.utilx.lifecycle.listener.Destroyable;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.Params;

/**
 * <p>create drawable for failed status</p>
 *
 * <p>you must use this {@link TIBitmapDrawable} instead of {@link BitmapDrawable}
 * to implements {@link LoadingDrawableFactory}/{@link BackgroundDrawableFactory}/{@link FailedDrawableFactory}</p>
 *
 * <p>implement notes::</p>
 *
 * <p>1.failedDrawable's size match Params->reqWidth/reqHeight, in any case</p>
 *
 * Created by S.Violet on 2016/3/16.
 */
public interface FailedDrawableFactory extends Destroyable {

    /**
     * create a drawable for failed state
     * @param applicationContext applicationContext
     * @param context context of activity, might be null
     * @param params params of stub
     * @param logger logger
     * @return Drawable
     */
    Drawable create(Context applicationContext, Context context, Params params, TLogger logger);

}
