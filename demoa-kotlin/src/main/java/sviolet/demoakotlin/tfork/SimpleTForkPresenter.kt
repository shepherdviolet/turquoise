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
import sviolet.turquoise.kotlin.utilx.tfork.fork
import sviolet.turquoise.utilx.tlogger.logd
import sviolet.turquoise.utilx.tlogger.loge
import sviolet.turquoise.utilx.tlogger.logw
import sviolet.turquoise.x.imageloader.TILoader
import sviolet.turquoise.x.imageloader.entity.ImageResource
import sviolet.turquoise.x.imageloader.entity.OnLoadedListener
import sviolet.turquoise.x.imageloader.entity.Params

/**
 * TFork的简单示例
 *
 * Created by S.Violet on 2017/5/31.
 */
class SimpleTForkPresenter(view: SimpleTForkActivity) : TPresenter<SimpleTForkActivity>(view){

    fun queryInfo(url: String){

        logd("fork: start")
        //fork代码块中的代码会在新线程中执行
        fork ({
            logd("fork: load bitmap")
            //await代码块用于将异步操作转为同步
            //await代码块会在新线程中执行, 同时fork代码块会阻塞直到结果返回
            val bitmap = it.await<Bitmap> ({
                //await代码块
                //异步加载Bitmap
                TILoader.extract(viewLayer, url, null, object: OnLoadedListener<Unit>(){
                    override fun onLoadSucceed(url: String?, params: Params?, resource: ImageResource?) {
                        //所有结果都必须用callback返回, 否则fork代码块会长时间阻塞直到超时
                        it.callback(resource?.resource as Bitmap)
                    }
                    override fun onLoadCanceled(url: String?, params: Params?) {
                        //所有结果都必须用callback返回, 否则fork代码块会长时间阻塞直到超时
                        it.callback(Exception("fork: bitmap load canceled"))
                    }
                })
            }, {
                //await的异常处理块, await代码块中抛出的异常, 会回调该代码块处理
                loge("fork: error from await", it)
                //返回true表示异常处理妥当, 不会再向外抛出异常, 若返回false, 会向外抛出异常
                return@await true
            })

            //await代码块抛出异常, 且异常处理块中返回true时, 返回值为空
            if (bitmap == null){
                loge("fork: loaded bitmap is null")
                return@fork
            }

            logd("fork: callback ui")
            //在主线程执行代码块
            it.ui {
                logd("fork: callback ui, thread:${Thread.currentThread()}")
                //刷新显示
                this@SimpleTForkPresenter.viewLayer.setImage(bitmap)
            }

        }, {exception, isTimeout ->
            //fork的异常处理块, await代码块中抛出的异常, 会回调该代码块处理
            //isTimeout=true时, 表示fork块内部有await/uiAwait发生超时
            if (isTimeout) logw("fork: timeout") else loge(exception)
            //返回true表示异常处理妥当, 不会再向外抛出异常, 若返回false, 会向外抛出异常
            return@fork true
        })

        logd("fork: finish")

    }

}

