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

package sviolet.demoa.refresh;

import android.os.Bundle;
import android.os.Message;
import android.widget.ListView;
import android.widget.Toast;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.EmulateListAdapter;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.async.WeakHandler;
import sviolet.turquoise.ui.viewgroup.refresh.VerticalOverDragContainer;
import sviolet.turquoise.x.common.tlogger.TLogger;

@DemoDescription(
        title = "OverDrag Demo",
        type = "View",
        info = "common usage of VerticalOverDragContainer"
)

/**
 * VerticalOverDragContainer基本用法示例<br/>
 *
 * Created by S.Violet on 2016/11/10
 */
@ResourceId(R.layout.refresh_overdrag)
@ActivitySettings(
        statusBarColor = 0xFF30C0C0,
        navigationBarColor = 0xFF30C0C0
)
public class OverDragRefreshActivity extends TActivity {

    @ResourceId(R.id.refresh_overdrag_container)
    private VerticalOverDragContainer container;
    @ResourceId(R.id.refresh_overdrag_listview)
    private ListView listView;

    private TLogger logger = TLogger.get(this);

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        //listView初始化
        listView.setAdapter(new EmulateListAdapter(this, 15,
                "OverDrag", "10 hours ago", "common usage of VerticalOverDragContainer"));

        //VerticalOverDragContainer监听器(刷新指示器)
        container.addRefreshIndicator(new VerticalOverDragContainer.RefreshIndicator() {
            @Override
            public void onStateChanged(int state) {
                //状态变更
            }

            @Override
            public void onScroll(int scrollY) {
                /**
                 * 越界拖动时, 容器控件的滚动位置, 一般用于实现下拉刷新/上拉加载, 刷新指示器可以根据此回调,
                 * 实现从顶部进入(底部进入), 或实现指示器跟着越界拖动的程度进行变化的动画效果
                 */
                logger.i("scrollY:" + scrollY);
            }

            @Override
            public boolean onTopPark() {
                //顶部PARK事件
                Toast.makeText(getApplicationContext(), "TOP PARK", Toast.LENGTH_SHORT).show();
                /**
                 * 模拟刷新流程处理, 等待刷新完成后, 必须调用container.resetTopPark()方法, 重置容器控件的PARK状态, 容器控件会弹回初始状态,
                 * 在状态重置前, 容器控件将不会再触发TOP PARK事件, 必须在重置状态后才能触发
                 */
                myHandler.sendEmptyMessageDelayed(MyHandler.HANDLER_TOP_PARK_RESET, 2000);
                /**
                 * (1) 返回true时, 表明接受了该事件, 容器进入顶部PARK状态, 并阻断后续触发的顶部PARK事件(不管怎么
                 * 拖动都不会再触发顶部PARK事件), 直到调用{@link VerticalOverDragContainer#resetTopPark()}
                 * 方法解除PARK状态并弹回. 例如:监听器中开始刷新流程, 返回true, 等待刷新流程结束后, 调用
                 * {@link VerticalOverDragContainer#resetTopPark()}方法, 容器会弹回初始状态, 并开始接受
                 * 下一个PARK事件.
                 * (2) 返回false时, 表明监听器不处理该事件, 容器不进入PARK状态, 无需调用
                 * {@link VerticalOverDragContainer#resetTopPark()}方法重置, 容器会继续响应接下来的顶部
                 * PARK事件.
                 */
                return true;
            }

            @Override
            public boolean onBottomPark() {
                //底部PARK事件
                Toast.makeText(getApplicationContext(), "BOTTOM PARK", Toast.LENGTH_SHORT).show();
                /**
                 * 模拟加载流程处理, 等待加载完成后, 必须调用container.resetBottomPark()方法, 重置容器控件的PARK状态, 容器控件会弹回初始状态,
                 * 在状态重置前, 容器控件将不会再触发BOTTOM PARK事件, 必须在重置状态后才能触发
                 */
                myHandler.sendEmptyMessageDelayed(MyHandler.HANDLER_BOTTOM_PARK_RESET, 2000);
                /**
                 * (1) 返回true时, 表明接受了该事件, 容器进入底部PARK状态, 并阻断后续触发的底部PARK事件(不管怎么
                 * 拖动都不会再触发底部PARK事件), 直到调用{@link VerticalOverDragContainer#resetBottomPark()}
                 * 方法解除PARK状态并弹回. 例如:监听器中开始加载流程, 返回true, 等待加载流程结束后, 调用
                 * {@link VerticalOverDragContainer#resetBottomPark()}方法, 容器会弹回初始状态, 并开始接受
                 * 下一个PARK事件.
                 * (2) 返回false时, 表明监听器不处理该事件, 容器不进入PARK状态, 无需调用
                 * {@link VerticalOverDragContainer#resetBottomPark()}方法重置, 容器会继续响应接下来的底部
                 * PARK事件.
                 */
                return true;
            }

            @Override
            public void setContainer(VerticalOverDragContainer container) {
                //一般用于实现下拉刷新/上拉加载指示器, 容器控件会将自己通过该方法传递给指示器控件, 便于回调
            }

            @Override
            public void onTopParkAutoReset() {
                //长时间没重置状态, 容器会自动重置, 通过该方法通知指示器
            }

            @Override
            public void onBottomParkAutoReset() {
                //长时间没重置状态, 容器会自动重置, 通过该方法通知指示器
            }

            @Override
            public void onTopParkIgnore() {
                //过于频繁刷新
            }

            @Override
            public void onBottomParkIgnore() {
                //过于频繁加载
            }
        });
    }

    private MyHandler myHandler = new MyHandler(this);

    private static class MyHandler extends WeakHandler<OverDragRefreshActivity> {

        private static final int HANDLER_TOP_PARK_RESET = 0;
        private static final int HANDLER_BOTTOM_PARK_RESET = 1;

        public MyHandler(OverDragRefreshActivity host) {
            super(host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, OverDragRefreshActivity host) {

            switch (msg.what){
                case HANDLER_TOP_PARK_RESET:
                    host.container.resetTopPark();//必须调用该方法重置
                    Toast.makeText(host.getApplicationContext(), "TOP RESET", Toast.LENGTH_SHORT).show();
                    break;
                case HANDLER_BOTTOM_PARK_RESET:
                    host.container.resetBottomPark();//必须调用该方法重置
                    Toast.makeText(host.getApplicationContext(), "BOTTOM RESET", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }

}
