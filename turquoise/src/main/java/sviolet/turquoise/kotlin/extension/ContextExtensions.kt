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

package sviolet.turquoise.kotlin.extension

import android.content.Context
import android.widget.Toast

/**
 * Android Toast extensions
 *
 * Created by S.Violet on 2017/5/26.
 */

/**
 * Show short toast
 */
fun Context?.toast(msg: String?){
    if (this == null || msg == null){
        return
    }
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

/**
 * Show long toast
 */
fun Context?.toastl(msg: String?){
    if (this == null || msg == null){
        return
    }
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
