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

package sviolet.demoa.other;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.other.utils.MultiItemViewPagerAdapter;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.ui.drawable.SafeBitmapDrawable;
import sviolet.turquoise.ui.util.motion.ClickDetector;
import sviolet.turquoise.ui.util.ViewCommonUtils;
import sviolet.turquoise.util.bitmap.CachedBitmapUtils;
import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.util.droid.MeasureUtils;
import sviolet.turquoise.utilx.lifecycle.LifeCycleUtils;

@DemoDescription(
        title = "Multi item in ViewPager Demo",
        type = "Other",
        info = "Multi item in ViewPager"
)

/**
 * ViewPager中显示多个Item, 实现画廊效果<br/>
 *
 * Created by S.Violet on 2016/11/23.
 */
@ResourceId(R.layout.other_multiitem_viewpager)
@ActivitySettings(
        statusBarColor = 0xFF30C0C0,
        navigationBarColor = 0xFF30C0C0
)
public class MultiItemViewPagerOtherActivity extends TActivity {

    @ResourceId(R.id.other_multiitem_viewpager_background)
    private ImageView background;
    @ResourceId(R.id.other_multiitem_viewpager_container)
    private View container;//容器布局
    @ResourceId(R.id.other_multiitem_viewpager_viewpager)
    private ViewPager viewPager;//ViewPager

    private List<Integer> imageResIdList;

    private CachedBitmapUtils cachedBitmapUtils;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        cachedBitmapUtils = new CachedBitmapUtils(getApplicationContext(), 0.1f, 0f);
        LifeCycleUtils.attach(this, cachedBitmapUtils);

        viewPager.setOffscreenPageLimit(10);//设置缓存页面数(重要), 根据画廊一页能显示的Item数来设置, 设置约两页的Item数效果比较好, 但是相对占内存
        viewPager.setPageMargin(MeasureUtils.dp2px(getApplicationContext(), 6));//每一页之间的间距(重要)
        viewPager.setPageTransformer(true, new ScalePageTransformer());//设置动画
        viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);//隐藏越界滚动效果

        //模拟图片数据
        imageResIdList = createImageResIdList();

        //设置适配器
        viewPager.setAdapter(new MultiItemViewPagerAdapter(this, imageResIdList, cachedBitmapUtils));
        //设置触摸监听器(重要)
        container.setOnTouchListener(initTouchListener());

        //设置翻页监听
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //此处处理翻页事件, 比如显示对应页面的信息等
                Bitmap bitmap = cachedBitmapUtils.decodeFromResource(
                        String.valueOf(imageResIdList.get(position)),
                        getResources(),
                        imageResIdList.get(position));
                background.setImageDrawable(new SafeBitmapDrawable(getResources(), bitmap));
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //实现选中页面点击监听.
        viewPager.setOnTouchListener(new View.OnTouchListener() {

            private ClickDetector clickDetector = new ClickDetector(getApplicationContext());
            private int[] viewLocationCache = new int[2];//复用

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (clickDetector.detect(event)){
                    //判断点击坐标在ViewPager上
                    if (ViewCommonUtils.isPointOnView(viewPager, event.getRawX(), event.getRawY(), viewLocationCache)){
                        //此处处理选中页面点击事件
                        Toast.makeText(getApplicationContext(), "click:" + viewPager.getCurrentItem(), Toast.LENGTH_SHORT).show();
                    }
                }
                return false;//千万不要消费掉事件, 不然ViewPager本身的触摸逻辑都不会执行了
            }
        });

        Bitmap bitmap = cachedBitmapUtils.decodeFromResource(
                String.valueOf(imageResIdList.get(0)),
                getResources(),
                imageResIdList.get(0));
        background.setImageDrawable(new SafeBitmapDrawable(getResources(), bitmap));

    }

    /**
     * 模拟产生图片资源ID列表
     */
    private List<Integer> createImageResIdList(){
        List<Integer> list = new ArrayList<>();
        list.add(R.mipmap.async_image_1);
        list.add(R.mipmap.async_image_2);
        list.add(R.mipmap.async_image_3);
        list.add(R.mipmap.async_image_4);
        list.add(R.mipmap.async_image_5);
        list.add(R.mipmap.async_image_1);
        list.add(R.mipmap.async_image_2);
        list.add(R.mipmap.async_image_3);
        list.add(R.mipmap.async_image_4);
        list.add(R.mipmap.async_image_5);
        list.add(R.mipmap.async_image_1);
        list.add(R.mipmap.async_image_2);
        list.add(R.mipmap.async_image_3);
        list.add(R.mipmap.async_image_4);
        list.add(R.mipmap.async_image_5);
        return list;
    }

    /**
     * <p>重要步骤, 给容器布局设置触摸监听器</p>
     *
     * <p>1.将容器的触摸事件传递给ViewPager.</p>
     *
     * <p>ViewPager一般只显示一个Item(除非复写了PagerAdapter.getPageWidth方法), 利用android:clipChildren="false"参数,
     * 使得ViewPager控件范围以外的部分也能够被绘制出来, 在ViewPager外面套一层容器Layout, 容器的尺寸就是多ItemViewPager
     * 想要的尺寸, 而ViewPager本身的尺寸其实就变成一个Item的尺寸. 因此, 为了使得ViewPager外, 容器内的触摸事件能够传递给
     * ViewPager, 还需要手动将触摸事件传递给ViewPager.</p>
     *
     * <p>2.实现点击切换页面效果.</p>
     *
     */
    private View.OnTouchListener initTouchListener() {
        return new View.OnTouchListener() {

            private ClickDetector clickDetector = new ClickDetector(getApplicationContext());
            private int[] viewLocationCache = new int[2];//复用

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //将容器的触摸事件传递给ViewPager
                viewPager.dispatchTouchEvent(event);
                //实现点击切换页面效果
                if (clickDetector.detect(event)) {
                    //遍历子View, 判断点击到的是哪个, 并跳转到该页面
                    for (int i = 0; i < viewPager.getChildCount(); i++) {
                        View view = viewPager.getChildAt(i);
                        if (ViewCommonUtils.isPointOnView(view, event.getRawX(), event.getRawY(), viewLocationCache)) {
                            viewPager.setCurrentItem((Integer) view.getTag());
                            break;
                        }
                    }
                }
                return true;//消费事件
            }
        };
    }

    /**
     * 动画
     */
    private static class ScalePageTransformer implements ViewPager.PageTransformer {

        private static final float MAX_SCALE = 1.0f;
        private static final float MIN_SCALE = 0.7f;
        private static final float SLOPE = MAX_SCALE - MIN_SCALE;

        @Override
        public void transformPage(View page, float position) {
            //API10不支持scale
            if (DeviceUtils.getVersionSDK() <= 10){
                return;
            }

            //控制范围
            if (position < -1) {
                position = -1;
            } else if (position > 1) {
                position = 1;
            }

            //计算scale
            float tempScale = position < 0 ? 1 + position : 1 - position;
            float scaleValue = MIN_SCALE + tempScale * SLOPE;
            //缩放
            page.setScaleX(scaleValue);
            page.setScaleY(scaleValue);
            //平移(实现靠底部效果)
            page.setTranslationY((int) (page.getHeight() * (1 - scaleValue)) / 2);

            //API19以下requestLayout
            if (DeviceUtils.getVersionSDK() < Build.VERSION_CODES.KITKAT) {
                page.getParent().requestLayout();
            }
        }
    }

}
