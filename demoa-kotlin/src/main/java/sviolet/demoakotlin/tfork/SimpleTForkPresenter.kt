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

package sviolet.demoakotlin.tfork

import android.graphics.Bitmap
import sviolet.turquoise.enhance.app.mvp.TPresenter
import sviolet.turquoise.utilx.tfork.TForkConfigure
import sviolet.turquoise.utilx.tfork.fork
import sviolet.turquoise.utilx.tlogger.logd
import sviolet.turquoise.utilx.tlogger.loge
import sviolet.turquoise.utilx.tlogger.logw

/**
 * Created by S.Violet on 2017/5/31.
 */
class SimpleTForkPresenter(view: SimpleTForkActivity) : TPresenter<SimpleTForkActivity>(view){

    fun queryInfo(url: String){
        TForkConfigure.MAX_THREAD_NUM=10
        logd("start fork")
        var result: String?
        fork ({
            logd("in fork")
            logd("load bitmap")
            val bitmap = it.await<Bitmap> (5000, {
//                TILoader.extract(viewLayer, url, null, object: OnLoadedListener<Unit>(){
//                    override fun onLoadSucceed(url: String?, params: Params?, resource: ImageResource?) {
//                        it.callback(resource?.resource as Bitmap)
//                    }
//                    override fun onLoadCanceled(url: String?, params: Params?) {
//                        it.callback(Exception("load canceled"))
//                    }
//                })

            }, {
                loge("error from await", it)
                return@await true
            })

            if (bitmap == null){
                loge("bitmap is null")
            }

            logd("callback ui")

            it.ui {
                logd("callback ui, thread:${Thread.currentThread()}")
                this@SimpleTForkPresenter.viewLayer.setImage(bitmap)
                throw Exception("hello")
            }

        }, {exception, isTimeout ->
            if (isTimeout) logw("timeout") else loge(exception)
            return@fork true
        })
        logd("below fork")
    }

}

