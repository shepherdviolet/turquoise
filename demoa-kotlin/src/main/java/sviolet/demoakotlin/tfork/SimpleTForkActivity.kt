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
import android.os.Bundle
import android.widget.ImageView
import sviolet.demoakotlin.R
import sviolet.demoakotlin.common.DemoDescription
import sviolet.turquoise.enhance.app.TActivity
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings

/**
 * 临时调试用Activity
 */

@DemoDescription(
        title = "Simple TFork DEMO",
        type = "tfork",
        info = "The activity for Simple TFork DEMO"
)
@ResourceId(R.layout.tfork_simple_main)
@ActivitySettings(
        statusBarColor = 0xFF30C0C0.toInt(),
        navigationBarColor = 0xFF30C0C0.toInt()
)
class SimpleTForkActivity : TActivity() {

    val presenter = SimpleTForkPresenter(this)

    @ResourceId(R.id.tfork_simple_main_imageview)
    var imageView: ImageView? = null

    override fun onInitViews(savedInstanceState: Bundle?) {

        presenter.queryInfo("https://raw.githubusercontent.com/shepherdviolet/static-resources/master/image/logo/slate.jpg")

    }

    fun setImage(bitmap: Bitmap?){
        imageView?.setImageBitmap(bitmap)
    }

    override fun onPresenterRefresh(code: Int) {
        super.onPresenterRefresh(code)
    }

    override fun afterDestroy() {

    }

    override fun onResume() {
        super.onResume()
    }

}