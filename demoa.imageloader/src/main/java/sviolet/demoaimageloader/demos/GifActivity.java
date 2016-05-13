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

package sviolet.demoaimageloader.demos;

import android.os.Bundle;
import android.widget.ImageView;

import sviolet.demoaimageloader.R;
import sviolet.demoaimageloader.common.DemoDescription;
import sviolet.turquoise.enhance.app.TAppCompatActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.x.imageloader.TILoader;
import sviolet.turquoise.x.imageloader.entity.Params;

/**
 * Load GIF
 * <p/>
 * Created by S.Violet on 2016/3/8.
 */

@DemoDescription(
        title = "GIF Load Usage",
        type = "",
        info = "How to load GIF with TurquoiseImageLoader"
)

@ResourceId(R.layout.gif_main)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class GifActivity extends TAppCompatActivity {

    @ResourceId(R.id.gif_main_imageview1)
    private ImageView imageView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadGIF();
    }

    /**
     * <p>TILoader支持GIF加载, 但基础包不加载GIF图片. 请根据实际情况选择, 如果需要, 请追加插件包.</p>
     *
     * <p>工程同时依赖"turquoise.imageloader"和"turquoise.imageloader.plugin"即可, TILoader会自动加载插件
     * 模块, 并提供GIF加载支持. 无需其他配置, 使用方式不变.</p>
     *
     * <pre>{@code
     *     //基础包
     *     compile project(':turquoise')
     *     compile project(':turquoise.imageloader')
     *     //插件包(可选,增加GIF支持等)
     *     compile project(':turquoise.imageloader.plugin')
     * }</pre>
     *
     */
    private void loadGIF(){
        /**
         * 像加载普通图片一样加载GIF即可
         */
        String url1 = "https://camo.githubusercontent.com/d406ac5a03a2b1fa5cf41fadc8d2408cb8709bdc/68747470733a2f2f6431337961637572716a676172612e636c6f756466726f6e742e6e65742f75736572732f3132353035362f73637265656e73686f74732f313635303331372f7265616c6573746174652d70756c6c5f312d322d332e676966";
        Params params = new Params.Builder()
                .setIndispensable()//设置为重要任务, 有双倍的超时时间, 采用专用的低网速策略(延长加载时间)
                .build();
        TILoader.node(this).load(url1, params, imageView1);
    }

}
